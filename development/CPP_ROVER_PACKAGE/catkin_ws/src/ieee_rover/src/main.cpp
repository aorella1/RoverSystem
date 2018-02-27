#include <cstdlib>
#include <string>
#include <vector>
#include <unistd.h>
#include <sstream>

#include <iostream>

#include "network.h"
#include "camera.h"
#include "util.h"

#include "ros/ros.h"
#include "geometry_msgs/Twist.h"
#include "serial/serial.h"

struct Vector3{
    float x;
    float y;
    float z;
}

struct Twist{
    Vector3 linear;
    Vector3 angular;
}

// Camera dimentions.
const int CAMERA_WIDTH = 848;
const int CAMERA_HEIGHT = 480;

ros::Publisher chatter_pub;

using network::PacketHeartbeat;
using network::PacketControl;
using network::PacketCamera;

serial::Serial my_serial("/dev/ttyACM0", 9600, serial::Timeout::simpleTimeout(1000));

/*
void moveCallback(const geometry_msgs::Twist& msg){

    unsigned char dataFF[2], dataFE[2], dataFD[2], dataFC[2], data02[2], data03[2];
    dataFF[0] = 0xFF; dataFF[1]=0;
    dataFE[0] = 0xFE; dataFE[1]=0;
    dataFD[0] = 0xFD; dataFD[1]=0;
    dataFC[0] = 0xFC; dataFC[1]=0;
    data02[0] = 0x02; data02[1]=0;
    data03[0] = 0x03; data03[1]=0;

    printf("ros_message.linear.y: %f ros_message.angular.z: %f \n", msg.linear.y, ros_message.angular.z);

    int test;

    test = (int)(ros_message.linear.y*32.0) + (ros_message.angular.z/2);
    test = (test * 4);
    if (test<0) test = 2-test;
    dataFF[0] = test;
    printf("left motor: %1x\n", dataFF[0]);
    my_serial.write(dataFF,1);
    test = (int)(ros_message.linear.y*32.0) - (ros_message.angular.z/2);
    test = (test * 4);
    if (test<0) test = 2-test;
    dataFF[0] = test+1;
    printf("right motor: %1x\n", dataFF[0]);
    my_serial.write(dataFF,1);
}
*/

static void handle_heartbeat(network::Manager& manager, PacketHeartbeat* packet)
{
    if (packet->direction == PacketHeartbeat::Direction::PING) {
        
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

    /*
    if (packet->movement_state == lastState)
    {
        return;
    }
    */

    std::string message;
    geometry_msgs::Twist ros_message;
    ros_message.linear.x=0;
    ros_message.linear.y=0;
    ros_message.linear.z=0;
    ros_message.angular.x=0;
    ros_message.angular.y=0;
    ros_message.angular.z=0;
/*
    switch (packet->movement_state)
    {
        case PacketControl::MovementState::STOP:
            message = "stop";
            break;
        case PacketControl::MovementState::FORWARD:
            ros_message.linear.y=1;
            message = "move forward";
            break;
        case PacketControl::MovementState::LEFT:
            ros_message.angular.z=-5;
            message = "turn left";
            break;
        case PacketControl::MovementState::RIGHT:
            ros_message.angular.z=5;
            message = "turn right";
            break;
        case PacketControl::MovementState::BACKWARD:
            ros_message.linear.y=-1;
            message = "move backward";
            break;
        default:
            message = "?";
            break;
    }
    */

    int in = packet->movement_state;
    int top = (in / 16) - 8;
    int bot = (in & 15) - 8;
    
    printf("top %d, bot %d\n", top, bot);

    int linear=0, angular=0;

    if (top>0 && bot>0) {
        if (top>bot) linear = top; else linear = bot;
    }

    if (top<0 && bot<0) {
        if (top<bot) linear = top; else linear = bot;
    }

    angular = top-bot;

    ros_message.linear.y = ((float)linear)/8.0;
    ros_message.angular.z= angular;

    //chatter_pub.publish(ros_message);
    unsigned char dataFF[2], dataFE[2], dataFD[2], dataFC[2], data02[2], data03[2];
    dataFF[0] = 0xFF; dataFF[1]=0;
    dataFE[0] = 0xFE; dataFE[1]=0;
    dataFD[0] = 0xFD; dataFD[1]=0;
    dataFC[0] = 0xFC; dataFC[1]=0;
    data02[0] = 0x02; data02[1]=0;
    data03[0] = 0x03; data03[1]=0;

    printf("ros_message.linear.y: %f ros_message.angular.z: %f \n", ros_message.linear.y, ros_message.angular.z);

    int test;

    test = (int)(ros_message.linear.y*32.0) + (ros_message.angular.z/2);
    test = (test * 4);
    if (test<0) test = 2-test;
    dataFF[0] = test;
    printf("left motor: %1x\n", dataFF[0]);
    my_serial.write(dataFF,1);
    test = (int)(ros_message.linear.y*32.0) - (ros_message.angular.z/2);
    test = (test * 4);
    if (test<0) test = 2-test;
    dataFF[0] = test+1;
    printf("right motor: %1x\n", dataFF[0]);
    my_serial.write(dataFF,1);

    ros::spinOnce();

    //printf("> Received CONTROL packet: %s\n", message.c_str());

    //lastState = packet->movement_state;
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

int main(int argc, char **argv)
{
    // Initialize the packet readers and writers.
    network::register_packet_functions();
    ros::init(argc, argv, "rover_server");
    
    ros::NodeHandle n;
    
    chatter_pub = n.advertise<geometry_msgs::Twist>("move", 1000);
    ros::Rate loop_rate(10);

    // Bind to our listening port.
    network::Manager manager("0.0.0.0");

    // Register packet handlers.
    network::PacketTypeHeartbeat.handler = handle_heartbeat;
    network::PacketTypeControl.handler = handle_control;
/*
    // Set up camera feed.
    camera::CaptureSession camera(CAMERA_WIDTH, CAMERA_HEIGHT);

    if (!camera.open("/dev/video0")) return 1;
    if (!camera.check_capabilities()) return 1;
    if (!camera.init_buffers()) return 1;
    if (!camera.start_stream()) return 1;

    printf("> Frame size: %lu\n", camera.image_size);
    
    // Create buffer for image data.
    uint8_t* frame_buffer_back = (uint8_t*) malloc(camera.image_size);
*/
    // For cycles per second tracking.
    uint64_t start_time = millisecond_time();
    uint64_t last_time = millisecond_time();
    uint64_t cycles = 0;

    while (1)
    {
        manager.poll();
/*
        // Only send frames if we are connected.
        if (manager.state == network::ConnectionState::CONNECTED) {
            grab_frame(manager, camera, frame_buffer_back);            
        } else {
            // // Only chirp once in a while.
            // if (millisecond_time() - manager.last_chirp_time >= network::CONNECTION_ROVER_CHIRP_DELAY)
            //     manager.chirp();
        }
        */
        if (millisecond_time() - last_time >= 1000) {
            std::cout << "> " << ((float) cycles / (millisecond_time() - start_time)*1000.0) << " cycles per second at millisecond mark " << (millisecond_time() - start_time) << std::endl;
            last_time = millisecond_time();
        }
        cycles++;
    }

    //free(frame_buffer_back);
}
