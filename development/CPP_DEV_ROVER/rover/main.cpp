#include <cstdlib>
#include <string>
#include <vector>
#include <unistd.h>

#include <iostream>

#include "network.h"
#include "camera.h"
#include "util.h"

// Camera dimentions.
const int CAMERA_WIDTH = 640;
const int CAMERA_HEIGHT = 360;

using network::PacketHeartbeat;
using network::PacketControl;
using network::PacketCamera;

static void handle_heartbeat(network::Manager& manager, PacketHeartbeat* packet)
{
    if (packet->direction == PacketHeartbeat::Direction::PING) {
        printf("> Received ping... responding!\n");
        
        PacketHeartbeat response;
        response.direction = PacketHeartbeat::Direction::PONG;

        manager.send_packet(&response);
    } else {
        printf("> Our ping was answered by a PONG!\n");
    }
}

static PacketControl::MovementState lastState = PacketControl::MovementState::STOP;

static void handle_control(network::Manager& manager, PacketControl* packet) {
    // We do not use these.
    (void)manager;

    if (packet->movement_state == lastState)
    {
        return;
    }

    std::string message;

    switch (packet->movement_state)
    {
        case PacketControl::MovementState::STOP:
            message = "stop";
            break;
        case PacketControl::MovementState::FORWARD:
            message = "move forward";
            break;
        case PacketControl::MovementState::LEFT:
            message = "turn left";
            break;
        case PacketControl::MovementState::RIGHT:
            message = "turn right";
            break;
        case PacketControl::MovementState::BACKWARD:
            message = "move backward";
            break;
        default:
            message = "?";
            break;
    }

    printf("> Received CONTROL packet: %s\n", message.c_str());

    lastState = packet->movement_state;
}

static void grab_frame(network::Manager& manager, camera::CaptureSession& camera, uint8_t* frame_buffer_back) {
    uint8_t* frame_buffer = frame_buffer_back;
    
    size_t frame_size = camera.grab_frame(frame_buffer_back);
    if (frame_size == 0) {
        std::cerr << "[!] Failed to grab frame!" << std::endl;
        return;
    }

    // Create the packets needed.
    int num_packets = (frame_size + (network::CAMERA_PACKET_FRAME_DATA_MAX_SIZE - 1)) / network::CAMERA_PACKET_FRAME_DATA_MAX_SIZE;

    // Send all but the last packet.
    for (int i = 0; i < num_packets - 1; i++) {
        PacketCamera camera_packet;
        camera_packet.section_index = (uint8_t) i;
        camera_packet.section_count = (uint8_t) num_packets;
        camera_packet.size = (uint16_t) network::CAMERA_PACKET_FRAME_DATA_MAX_SIZE;
        camera_packet.data = frame_buffer;

        frame_buffer += network::CAMERA_PACKET_FRAME_DATA_MAX_SIZE;

        // This is for now... java is too slow! We need to get packet size down.
        usleep(10 * 1000);

        manager.send_packet(&camera_packet);            
    }

    // Send the last packet.
    PacketCamera camera_packet;
    camera_packet.section_index = (uint8_t) (num_packets - 1);
    camera_packet.section_count = (uint8_t) num_packets;
    camera_packet.size = (uint16_t) (frame_size % network::CAMERA_PACKET_FRAME_DATA_MAX_SIZE);
    camera_packet.data = frame_buffer;

    manager.send_packet(&camera_packet);

    // Manually increment timestamp
    // Do our own overflow, since its undefined for C++.
    if (manager.send_timestamp == UINT16_MAX)
        manager.send_timestamp = 0;
    else
        manager.send_timestamp++;

}

int main()
{
    // Initialize the packet readers and writers.
    network::register_packet_functions();

    // Bind to our listening port.
    network::Manager manager("0.0.0.0");

    // Register packet handlers.
    network::PacketTypeHeartbeat.handler = handle_heartbeat;
    network::PacketTypeControl.handler = handle_control;

    // Set up camera feed.
    camera::CaptureSession camera(CAMERA_WIDTH, CAMERA_HEIGHT);

    if (!camera.open("/dev/video0")) return 1;
    if (!camera.check_capabilities()) return 1;
    if (!camera.init_buffers()) return 1;
    if (!camera.start_stream()) return 1;

    printf("> Frame size: %lu\n", camera.image_size);
    
    // Create buffer for image data.
    uint8_t* frame_buffer_back = (uint8_t*) malloc(camera.image_size);

    // For cycles per second tracking.
    uint64_t start_time = millisecond_time();
    uint64_t last_time = millisecond_time();
    uint64_t cycles = 0;

    while (1)
    {
        manager.poll();

        // Only send frames if we are connected.
        if (manager.state == network::ConnectionState::CONNECTED) {
            grab_frame(manager, camera, frame_buffer_back);            
        } else {
            // Only chirp once in a while.
            if (millisecond_time() - manager.last_chirp_time >= network::CONNECTION_ROVER_CHIRP_DELAY)
                manager.chirp();
        }
        
        if (millisecond_time() - last_time >= 1000) {
            std::cout << "> " << ((float) cycles / (millisecond_time() - start_time)*1000.0) << " cycles per second at millisecond mark " << (millisecond_time() - start_time) << std::endl;
            last_time = millisecond_time();
        }
        cycles++;
    }

    free(frame_buffer_back);
}
