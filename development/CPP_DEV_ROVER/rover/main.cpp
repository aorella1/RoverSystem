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

// Max number of possible cameras.
const int MAX_CAMERAS = 9;

using network::PacketHeartbeat;
using network::PacketControl;
using network::PacketCamera;
using network::PacketInput;

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

static std::vector<camera::CaptureSession*> available_cameras;
static int selected_camera = 0;

static void open_cameras() {
    for (int i = 0; i < MAX_CAMERAS; i++)
    {
        char path[100];
        sprintf(path, "/dev/video%d", i);

        camera::CaptureSession* camera = new camera::CaptureSession(CAMERA_WIDTH, CAMERA_HEIGHT);

        if (!camera->open(path) || !camera->check_capabilities() 
            || !camera->init_buffers() || !camera->start_stream())
        {
            delete camera;
            continue;
        }

        printf("> Found camera %s\n", path);

        available_cameras.push_back(camera);
    }
}

static void handle_control(network::Manager& manager, PacketControl* packet) {
    // We do not use this.
    (void)manager;

    selected_camera = packet->selected_camera;
    
    if ((unsigned int) selected_camera >= available_cameras.size()) return;

    printf("> Updated selected camera to %u\n", selected_camera);
}

static void handle_input(network::Manager& manager, PacketInput* packet) {
    // We do not use this.
    (void)manager;
    (void)packet;

    // Read the values of the packet here.
    // Then change movement as you need based upon what those values are.
    // Use the input namespace to help with values.
    // Ex: input::get_controller_button(packet->controller_buttons, input::ControllerButton::A)
}

static void grab_frame(network::Manager& manager, camera::CaptureSession& camera) {
    static uint16_t next_frame_id = 0;

    uint8_t* frame_buffer = camera.frame_buffer;
    
    size_t frame_size = camera.grab_frame();
    if (frame_size == 0) {
        std::cerr << "[!] Failed to grab frame!" << std::endl;
        return;
    }

    // Create the packets needed.
    int num_packets = (frame_size + (network::CAMERA_PACKET_FRAME_DATA_MAX_SIZE - 1)) / network::CAMERA_PACKET_FRAME_DATA_MAX_SIZE;

    // Send all but the last packet.
    for (int i = 0; i < num_packets - 1; i++) {
        PacketCamera camera_packet;
        camera_packet.frame_id = next_frame_id;
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
    camera_packet.frame_id = next_frame_id;
    camera_packet.section_index = (uint8_t) (num_packets - 1);
    camera_packet.section_count = (uint8_t) num_packets;
    camera_packet.size = (uint16_t) (frame_size % network::CAMERA_PACKET_FRAME_DATA_MAX_SIZE);
    camera_packet.data = frame_buffer;

    manager.send_packet(&camera_packet);

    // Do our own overflow, since its undefined for C++.
    if (next_frame_id == UINT16_MAX)
        next_frame_id = 0;
    else
        next_frame_id++;
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
    network::PacketTypeInput.handler = handle_input;

    open_cameras();

    // For cycles per second tracking.
    uint64_t start_time = millisecond_time();
    uint64_t last_time = millisecond_time();
    uint64_t cycles = 0;

    while (1)
    {
        manager.poll();

        // Only send frames if we are connected.
        if (manager.state == network::ConnectionState::CONNECTED) {
            grab_frame(manager, *available_cameras[selected_camera]);
        }

        if (millisecond_time() - last_time >= 1000) {
            // Print average cycles per second every second (or so).
            std::cout << "> " << ((float) cycles / (millisecond_time() - start_time)*1000.0) << " cycles per second at millisecond mark " << (millisecond_time() - start_time) << std::endl;
            last_time = millisecond_time();
        }
        cycles++;
    }
}
