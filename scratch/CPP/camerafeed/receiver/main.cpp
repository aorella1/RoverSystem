#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <errno.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>

#include <iostream>
#include <vector>

#include <opencv2/opencv.hpp>

#define CAMERA_PACKET_DATA_MAX_SIZE 40000
#define CAMERA_FRAME_BUFFER_SIZE 4000000
#define CAMERA_FRAME_DELAY 6

#define PACKET_BUFFER_SIZE CAMERA_PACKET_DATA_MAX_SIZE + 5 + 4

#define BUFFERITEM(buf, offset, type) *((type*) &buf[offset])

struct FrameBuffer {
    uint16_t timestamp;
    uint8_t remaining_sections;

    uint8_t* buffer = nullptr;
    size_t buffer_size;

    FrameBuffer() {
    }

    void allocate() {
        timestamp = 0;
        remaining_sections = 0;
        buffer_size = 0;

        buffer = new uint8_t[CAMERA_FRAME_BUFFER_SIZE];
    }

    ~FrameBuffer() {
        if (buffer) delete[] buffer;
    }
};

class FrameBufferContainer {
    FrameBuffer buffers[CAMERA_FRAME_DELAY];

    size_t next_buffer;

public:
    FrameBufferContainer() {
        next_buffer = 0;

        for (int i = 0; i < CAMERA_FRAME_DELAY; i++) {
            buffers[i].allocate();
        }
    }

    void update_buffer(uint16_t timestamp, uint8_t section_id, uint8_t section_count, uint8_t* frame_data, size_t frame_data_size) {
        // Search for a buffer that already has that timestamp.
        int found_buffer_idx = -1;
        for (int i = 0; i < CAMERA_FRAME_DELAY; i++) {
            if (buffers[i].timestamp == timestamp) {
                // Found it!
                found_buffer_idx = i;
                break;
            }
        }

        if (found_buffer_idx == -1) {
            // We did not find a buffer... it is a new frame!

            if (buffers[next_buffer].timestamp != 0) {
                // It is not a new buffer... it already has a frame in it.

                if (buffers[next_buffer].remaining_sections == 0) {
                    // We are ready to push to screen!

                    std::cout << "> Pushing buffer " << next_buffer << "!" << std::endl;
                    std::cout << "    > Timestamp: " << buffers[next_buffer].timestamp << std::endl;
                    std::cout << "    > Size: " << buffers[next_buffer].buffer_size << std::endl;

                    std::cout << "FIRST BYTE OF ARRAY " << (int) buffers[next_buffer].buffer[1566] << std::endl;

                    std::vector<unsigned char> jpeg_buffer(buffers[next_buffer].buffer, buffers[next_buffer].buffer + buffers[next_buffer].buffer_size);

                    std::cout << "FIRST BYTE OF FUCKING SON OF A BITCH " << (int) jpeg_buffer[1566] << std::endl;

                    cv::Mat jpeg_frame = cv::imdecode(jpeg_buffer, CV_LOAD_IMAGE_COLOR);
                    cv::imshow("feed", jpeg_frame);
                    cv::waitKey(20);
                }

                std::cout << "> Dropped frame with timestamp " << buffers[next_buffer].timestamp << std::endl;
            }

            printf("Starting new at %u with ts %u, remaining sec %u, buffer size %u\n", next_buffer, timestamp, section_count - 1, frame_data_size);

            buffers[next_buffer].timestamp = timestamp;
            buffers[next_buffer].remaining_sections = section_count - 1;
            memcpy(buffers[next_buffer].buffer + (CAMERA_PACKET_DATA_MAX_SIZE*section_count), frame_data, frame_data_size);
            buffers[next_buffer].buffer_size = frame_data_size;

            // Reset our next_buffer.
            next_buffer = (next_buffer + 1) % CAMERA_FRAME_DELAY;
        } else {
            // We found our buffer, let's update it!

            std::cout << "Updating buffer " << found_buffer_idx << ": Had remaining " << (int) buffers[found_buffer_idx].remaining_sections << std::endl;

            buffers[found_buffer_idx].remaining_sections--;
            memcpy(buffers[found_buffer_idx].buffer + (CAMERA_PACKET_DATA_MAX_SIZE*section_count), frame_data, frame_data_size);
            buffers[found_buffer_idx].buffer_size += frame_data_size;
        }
    }
};

int main(int argc, char** argv) {
    // Arguments: <bind port>
    if (argc < 2) {
        std::cerr << "[!] Incorrect usage!" << std::endl << argv[0] << " <bind port>" << std::endl;
        return 1;
    }

    // Create a socket file descriptor.
    int socket_fd = socket(AF_INET, SOCK_DGRAM, 0);
    if (socket_fd < 0)
    {
        // Socket open failure
        std::cerr << "[!] Failed to open socket!" << std::endl;
        return 1;
    }

    int port = atoi(argv[1]);
    
     // Set our listening address.
     struct sockaddr_in address;
     // Clear its memory.
     memset((char*)&address, 0, sizeof(address));
     // Use IP addresses.
     address.sin_family = AF_INET;
     // Convert the address "0.0.0.0" to a long representation.
     inet_aton("0.0.0.0", &address.sin_addr);
     // Set the desired listening port.
     address.sin_port = htons(port);
 
     if (bind(socket_fd, (struct sockaddr*) &address, sizeof(address)) < 0)
     {
         // Bind failure
         std::cerr << "[!] Failed to bind socket!" << std::endl;
         return 1;
     }

     FrameBufferContainer buffer_container;

     uint8_t packet_buffer[PACKET_BUFFER_SIZE];
     struct sockaddr src_addr;
     socklen_t src_addr_len;

     for (;;) {
        src_addr_len = sizeof(src_addr);
        ssize_t res = recvfrom(socket_fd, packet_buffer, PACKET_BUFFER_SIZE, MSG_DONTWAIT, &src_addr, &src_addr_len);
        if (res == -1) {
            // Two options here: either its because no packets were around, or there's an actual error...
            if (errno == EAGAIN) {
                // printf("> Nothing to read.\n");
                continue;
            } else {
                // Handle failure
                printf("[!] Failed to receive packet!\n");
                return 1;
            }
        }

        uint16_t version = ntohs(BUFFERITEM(packet_buffer, 0, uint16_t));
        uint8_t packet_type = BUFFERITEM(packet_buffer, 2, uint8_t);
        uint16_t timestamp = ntohs(BUFFERITEM(packet_buffer, 3, uint16_t));

        uint8_t section_id = BUFFERITEM(packet_buffer, 5, uint8_t);
        uint8_t section_count = BUFFERITEM(packet_buffer, 6, uint8_t);
        uint16_t frame_data_size = ntohs(BUFFERITEM(packet_buffer, 7, uint16_t));

        //printf("Received packet with ts %u, s_id %u, s_c %u, fds %u\n", timestamp, section_id, section_count, frame_data_size);
        //printf("   Packet had read length %d\n", res);

        buffer_container.update_buffer(timestamp, section_id, section_count, &packet_buffer[9], frame_data_size);
     }
}