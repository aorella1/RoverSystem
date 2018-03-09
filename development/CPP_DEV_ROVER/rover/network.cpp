#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <errno.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>

#include <cstring>
#include <string>

#include "network.h"
#include "util.h"

namespace network {

const int CURRENT_ROVER_PROTOCOL_VERSION = 5;

const int HEADER_LENGTH = 5;

// See camera_spec.txt for details about this value.
const int CAMERA_PACKET_FRAME_DATA_MAX_SIZE = 40000;

// Must be large enough to fit any packet.
const int READ_BUFFER_SIZE = 65000;

// Time passed before connection is considered dead, in milliseconds.
const int CONNECTION_TIMEOUT = 6000;

// Port on which to listen.
const int CONNECTION_PORT = 44444;

PacketType<PacketHeartbeat> PacketTypeHeartbeat(0, 1);
PacketType<PacketControl> PacketTypeControl(1, 1);
PacketType<PacketCamera> PacketTypeCamera(2, 4 + CAMERA_PACKET_FRAME_DATA_MAX_SIZE);

void register_packet_functions() {
    PacketTypeHeartbeat.reader = [](PacketHeartbeat* packet, Buffer& buffer) {
        packet->direction = buffer.read_value<PacketHeartbeat::Direction>();
    };
    PacketTypeHeartbeat.writer = [](PacketHeartbeat* packet, Buffer& buffer) {
        buffer.write_value(packet->direction);
    };

    PacketTypeControl.reader = [](PacketControl* packet, Buffer& buffer) {
        packet->movement_state = buffer.read_value<PacketControl::MovementState>();
    };
    PacketTypeControl.writer = [](PacketControl* packet, Buffer& buffer) {
        buffer.write_value(packet->movement_state);
    };

    PacketTypeCamera.reader = [](PacketCamera* packet, Buffer& buffer) {
        packet->section_index = buffer.read_value<uint8_t>();
        packet->section_count = buffer.read_value<uint8_t>();
        packet->size = ntohs(buffer.read_value<uint16_t>());
        packet->data = buffer.get_pointer();
    };
    PacketTypeCamera.writer = [](PacketCamera* packet, Buffer& buffer) {
        buffer.write_value(packet->section_index);
        buffer.write_value(packet->section_count);
        buffer.write_value(htons(packet->size));
        buffer.write_bytes(packet->data, packet->size);
    };
}

Manager::Manager(std::string address_string)
{
    receive_timestamp = 0;
    send_timestamp = 0;

    last_receive_time = 0;
    last_chirp_time = 0;

    state = ConnectionState::UNINITIALIZED;

    socket_fd = socket(AF_INET, SOCK_DGRAM, 0);
    if (socket_fd < 0)
    {
        // Socket open failure
        printf("[!] Failed to open socket!\n");
    }

    // Begin listening
    struct sockaddr_in address;
    memset((char*)&address, 0, sizeof(address));
    address.sin_family = AF_INET;
    inet_aton(address_string.c_str(), &address.sin_addr);
    address.sin_port = CONNECTION_PORT;

    if (bind(socket_fd, (struct sockaddr*) &address, sizeof(address)) < 0)
    {
        // Bind failure
        printf("[!] Failed to bind socket!\n");
    }

    socklen_t addr_len = sizeof(address);
    // Get info about the port we were given, reusing our sockaddr_in from above.
    getsockname(socket_fd, (struct sockaddr*) &address, &addr_len);

    printf("> Bound to port %d\n", ntohs(address.sin_port));
}

void Manager::send_raw_packet(uint8_t* buffer, size_t size, std::string address, int port)
{
    // Create an address structure.
    struct sockaddr_in send_addr;
    // Initialize it to zero.
    memset((char*)&send_addr, 0, sizeof(send_addr));
    // Specify the IP protocol.
    send_addr.sin_family = AF_INET;
    // Specify the remote port.
    send_addr.sin_port = htons(port);
    // Specify the remote address. This will parse and set it for us.
    inet_aton(address.c_str(), &send_addr.sin_addr);

    // Send the packet.
    if (sendto(socket_fd, buffer, HEADER_LENGTH + size, 0, (struct sockaddr*) &send_addr, sizeof(send_addr)) < 0)
    {
        // Send failure
        printf("[!] Failed to send packet!\n");
    }
}

void Manager::poll()
{
    uint8_t buffer_back[READ_BUFFER_SIZE];
    struct sockaddr src_addr;
    socklen_t src_addr_len;

    ssize_t res;

    while (1)
    {
        src_addr_len = sizeof(src_addr);

        res = recvfrom(socket_fd, buffer_back, READ_BUFFER_SIZE, MSG_DONTWAIT, &src_addr, &src_addr_len);
        if (res == -1)
        {
            // Two options here: either its because no packets were around, or there's an actual error...
            if (errno == EAGAIN)
            {
                // printf("> Nothing to read.\n");
                break;
            }
            else
            {
                // Handle failure
                printf("[!] Failed to receive packet!\n");
                break;
            }
        }

        // Make a note of the time, since we have received a packet.
        last_receive_time = millisecond_time();

        // Get sender information
        struct sockaddr_in src_addr_in = *((struct sockaddr_in*) &src_addr);
        int port = (int) ntohs(src_addr_in.sin_port);
        std::string address(inet_ntoa(src_addr_in.sin_addr));

        if (state == ConnectionState::UNINITIALIZED || state == ConnectionState::DISCONNECTED) {
            base_station_address = address;
            base_station_port = port;

            state = ConnectionState::CONNECTED;

            printf("> Found base station at %s:%d\n", address.c_str(), port);
        }

        // We have to convert non-byte values from network order (Big Endian) to host order.
        Buffer buffer(buffer_back);
        uint16_t version = ntohs(buffer.read_value<uint16_t>());
        uint8_t type = buffer.read_value<uint8_t>();
        uint16_t timestamp = ntohs(buffer.read_value<uint16_t>());

        // Handle the version.
        if (version != CURRENT_ROVER_PROTOCOL_VERSION)
        {
            printf("[!] Ignoring packet with incorrect version!\n");
            continue;
        }

        // Handle the timestamp.... unless its a camera packet.
        if (timestamp == 0)
        {
            receive_timestamp = 0;
        }
        else
        {
            if (timestamp <= receive_timestamp && type != PacketTypeCamera.type)
            {
                printf("[!] Ignoring out-of-order packet!\n");
                continue;
            }
        }

        receive_timestamp = timestamp;

        switch (type) {
        case 0: 
        {
            PacketHeartbeat p;
            PacketTypeHeartbeat.reader(&p, buffer);
            PacketTypeHeartbeat.handler(*this, &p);
            break;
        }
        case 1:
        {
            PacketControl p;
            PacketTypeControl.reader(&p, buffer);
            PacketTypeControl.handler(*this, &p);            
            break;
        }
        case 2:
        {
            PacketCamera p;
            PacketTypeCamera.reader(&p, buffer);
            PacketTypeCamera.handler(*this, &p);            
            break;
        }
        default:
            printf("[!] Unknown packet type %d encountered!\n", type);
            continue;
        }
    }

    // We have to check the time since the last packet was received if we are connected.
    if (state == ConnectionState::CONNECTED && millisecond_time() - last_receive_time >= CONNECTION_TIMEOUT) {
        printf("[!] Connection to base station timed out! Disconnected.\n");
        state = ConnectionState::DISCONNECTED;
    }
}

} // namespace network
