#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <strings.h>
#include <arpa/inet.h>

#include <string>

#include <ogg/ogg.h>
#include <theora/theoraenc.h>
#include <theora/theoradec.h>

#include <opencv2/opencv.hpp>

// Width and height of the camera feed.
#define WIDTH 640
#define HEIGHT 480

// Quality of the stream, in the range [0, 63].
// Higher values denote higher quality.
#define STREAM_QUALITY 60

// The string which is associated with our video stream.
// It is recorded as the "vendor" of the stream; aka, what
// software encoded the video stream.
#define VENDOR_STRING "BUMARSROVER"

// The maximum size of a network packet.
// This also happens to be the max size of an ogg_packet, IN THIS EXAMPLE.
#define NETWORK_PACKET_BUFFER_SIZE 65307

// from https://stackoverflow.com/questions/3022552/is-there-any-standard-htonl-like-function-for-64-bits-integers-in-c
#define htonll(x) ((1==htonl(1)) ? (x) : ((uint64_t)htonl((x) & 0xFFFFFFFF) << 32) | htonl((x) >> 32))
#define ntohll(x) ((1==ntohl(1)) ? (x) : ((uint64_t)ntohl((x) & 0xFFFFFFFF) << 32) | ntohl((x) >> 32))

#define BUFFERITEM(buffer, offset, type) *((type*) &buffer[offset])

// Converts a tri-planar YCbCr 4:4:4 theora buffer to a packed RGB24 buffer.
void ycbcr_to_rgb(th_ycbcr_buffer ycbcr_buffer, uint8_t* rgb_buffer) {
    for (int i = 0; i < WIDTH * HEIGHT; i++) {
        uint8_t y  = ycbcr_buffer[0].data[i];
        uint8_t cb = ycbcr_buffer[1].data[i];
        uint8_t cr = ycbcr_buffer[2].data[i];

        uint8_t r = (uint8_t) (y + 1.402f * (cr - 128.0f));
        uint8_t g = (uint8_t) (y - 0.344136f * (cb - 128.0f) - 0.714136f * (cr - 128.0f));
        uint8_t b = (uint8_t) (y + 1.772f * (cb - 128.0f));

        rgb_buffer[(i*3) + 0] = r;
        rgb_buffer[(i*3) + 1] = g;
        rgb_buffer[(i*3) + 2] = b;
    }
}

// Represents a network manager.
struct Network {
    int socket_fd;

    uint8_t packet_buffer[NETWORK_PACKET_BUFFER_SIZE];

    bool init(int port) {
        socket_fd = socket(AF_INET, SOCK_DGRAM, 0);
        if (socket_fd < 0) {
            // Socket open failure
            printf("[!] Failed to open socket!\n");
            return false;
        }

        struct sockaddr_in address;
        memset((char*)&address, 0, sizeof(address));
        address.sin_family = AF_INET;
        inet_aton("0.0.0.0", &address.sin_addr);
        address.sin_port = htons(port);

        if (bind(socket_fd, (struct sockaddr*) &address, sizeof(address)) < 0) {
            // Bind failure
            printf("[!] Failed to bind socket!\n");
            return false;
        }

        return true;
    }

    // This assumes that packet->packet has been allocated!
    bool receive_packet(ogg_packet* packet) {
        uint8_t packet_buffer[NETWORK_PACKET_BUFFER_SIZE];
        struct sockaddr src_addr;
        socklen_t src_addr_len;

        for (;;) {
            src_addr_len = sizeof(src_addr);
            ssize_t res = recvfrom(socket_fd, packet_buffer, NETWORK_PACKET_BUFFER_SIZE, MSG_DONTWAIT, &src_addr, &src_addr_len);
            if (res == -1) {
                // Two options here: either its because no packets were around, or there's an actual error...
                if (errno == EAGAIN) {
                    // printf("> Nothing to read.\n");
                    continue;
                } else {
                    // Handle failure
                    printf("[!] Failed to receive packet!\n");
                    return false;
                }
            }

            // Fill the packet info
            packet->bytes = ntohl(BUFFERITEM(packet_buffer, 0, uint32_t));
            packet->b_o_s = ntohl(BUFFERITEM(packet_buffer, 4, uint32_t));
            packet->e_o_s = ntohl(BUFFERITEM(packet_buffer, 8, uint32_t));
            packet->granulepos = ntohll(BUFFERITEM(packet_buffer, 12, uint64_t));
            packet->packetno = ntohll(BUFFERITEM(packet_buffer, 20, uint64_t));

            // Copy packet data
            memcpy(packet->packet, &packet_buffer[28], packet->bytes);

            return true;
        }

        return true;
    }
};

int main(int argc, char** argv) {
    // Args: <bind port>
    if (argc < 2) {
        fprintf(stderr, "[!] Incorrect usage!\nUsage: %s <bind port>\n", argv[0]);
        return 1;
    }

    int bind_port = atoi(argv[1]);

    Network network;

    if (!network.init(bind_port)) return 1;

    ogg_packet receive_packet;
    receive_packet.packet = (uint8_t*) malloc(NETWORK_PACKET_BUFFER_SIZE); // Also the size of an OGG packet.

    // First, we gotta get that header stuff.
    th_info stream_info;
    th_info_init(&stream_info); // We must.

    th_comment stream_comment;
    th_setup_info* stream_setup_info = NULL; // We must.

    for (;;) {
        if (!network.receive_packet(&receive_packet)) {
            return 1;
        }

        int res = th_decode_headerin(&stream_info, &stream_comment, &stream_setup_info, &receive_packet);
        if (res < 0) {
            // Error
            fprintf(stderr, "[!] Failed to decode header!\n");
            return 1;
        }

        if (res == 0) {
            // We have the first frame packet. Let's exit headerland.
            break;
        }
    }

    printf("> We have stream from %s!\n", stream_comment.vendor);

    // Set up the context stuff.
    th_dec_ctx* decoder_context = th_decode_alloc(&stream_info, stream_setup_info);

    // Read the frames!

    cv::Mat frame_mat(HEIGHT, WIDTH, CV_8UC3);
    th_ycbcr_buffer frame_buffer;

    for (;;) {
        // Decode packet!
        int res = th_decode_packetin(decoder_context, &receive_packet, NULL);
        
        if (res < 0) {
            fprintf(stderr, "[!] Failed to decode packet!\n");
            return 1;
        }

        // We are good! Get a frame!
        th_decode_ycbcr_out(decoder_context, frame_buffer);

        // Convert to RGB.
        ycbcr_to_rgb(frame_buffer, frame_mat.ptr<uint8_t>(0));

        // Show the frame!
        cv::imshow("feed", frame_mat);
        if (cv::waitKey(20) == 27) break;
    }

    th_decode_free(decoder_context);
    th_setup_free(stream_setup_info);
    free(receive_packet.packet);
}