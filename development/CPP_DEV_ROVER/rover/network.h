#ifndef NETWORK_H
#define NETWORK_H

#include <arpa/inet.h>

#include <unordered_map>
#include <cstdint>
#include <cstdlib>

#include "buffer.h"

// Contains all networking stuff.
namespace network {

// The definitions for these are in network.cpp.
extern const int CURRENT_ROVER_PROTOCOL_VERSION;

extern const int HEADER_LENGTH;

// See camera_spec.txt for details about this value.
extern const int CAMERA_PACKET_FRAME_DATA_MAX_SIZE;

// Must be large enough to fit any packet.
extern const int READ_BUFFER_SIZE;

// Delay between consecutive multicast chirps, in milliseconds.
extern const unsigned int CONNECTION_ROVER_CHIRP_DELAY;
// Time passed before connection is considered dead, in milliseconds.
extern const int CONNECTION_TIMEOUT;

extern const char* CONNECTION_DISCOVERY_ADDRES;
extern const int CONNECTION_DISCOVERY_PORT44444;

// Forward declarations of packet classes.
struct PacketHeartbeat;
struct PacketControl;
struct PacketCamera;

// Forward declaration of Manager for PacketHandler.
class Manager;

// Reference to manager, pointer to packet.
template <class P>
using PacketHandler = void (*)(Manager&, P*);

// Represents a packet type.
// These are filled at runtime.
template <class P>
struct PacketType
{
    uint8_t type;
    uint16_t max_size;

    PacketType(uint8_t _type, uint16_t _max_size):
        type(_type), max_size(_max_size) {}

    void (*reader)(P*, Buffer&);
    void (*writer)(P*, Buffer&);

    PacketHandler<P> handler;
};

// Definitions for packet types.

extern PacketType<PacketHeartbeat> PacketTypeHeartbeat;

extern PacketType<PacketControl> PacketTypeControl;

extern PacketType<PacketCamera> PacketTypeCamera;

// Structs for instances of packets.

struct PacketHeartbeat
{
    enum class Direction : uint8_t
    {
        PING = 0,
        PONG = 1
    };

    PacketType<PacketHeartbeat>* type;

    Direction direction;

    PacketHeartbeat():
        type(&PacketTypeHeartbeat) {}
};

struct PacketControl
{
    enum class MovementState : uint8_t
    {
        STOP = 0,
        FORWARD = 1,
        LEFT = 2,
        RIGHT = 3,
        BACKWARD = 4
    };

    PacketType<PacketControl>* type;

    MovementState movement_state;

    PacketControl():
        type(&PacketTypeControl) {}
};

struct PacketCamera
{
    PacketType<PacketCamera>* type;

    uint8_t section_index;
    uint8_t section_count;
    uint16_t size;
    uint8_t* data;

    PacketCamera():
        type(&PacketTypeCamera) {}
};

// Sets the reader and writer for each packet type. Must be called before setting up the Manager.
void register_packet_functions();

// Enumerates the state of the base station/rover connection.
enum class ConnectionState
{
    UNINITIALIZED,
    CONNECTED,
    DISCONNECTED
};

class Manager
{
    public:
        ConnectionState state;
        int socket_fd;
        
        uint64_t last_receive_time;
        uint64_t last_chirp_time;

        uint16_t receive_timestamp, send_timestamp;

        std::string base_station_address;
        int base_station_port;

        void send_raw_packet(uint8_t*, size_t, std::string, int);
        void chirp();

        Manager(std::string);

        template <class P>
        void send_packet(P*);
        void poll();
};

// Declaration here due to template.
template <class P>
void Manager::send_packet(P* packet)
{
    // Allocate enough space for the header and the packet body.
    uint8_t* packet_buffer_back = (uint8_t*) alloca(HEADER_LENGTH + packet->type->max_size);
    Buffer packet_buffer(packet_buffer_back);

    // Fill the header.
    packet_buffer.write_value<uint16_t>(htons(CURRENT_ROVER_PROTOCOL_VERSION));
    packet_buffer.write_value(packet->type->type);
    packet_buffer.write_value<uint16_t>(htons(send_timestamp));

	if (packet->type->type != PacketTypeCamera.type) {
			// Do our own overflow, since its undefined for C++.
			if (send_timestamp == UINT16_MAX)
				send_timestamp = 0;
			else
				send_timestamp++;
	}

    packet->type->writer(packet, packet_buffer);
	size_t packet_size = packet_buffer.index;

    packet_buffer.reset();
    send_raw_packet(packet_buffer.get_pointer(), packet_size, base_station_address, base_station_port);
}

} // namespace network

#endif
