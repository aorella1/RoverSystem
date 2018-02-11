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

#define CAMERA_PACKET_DATA_MAX_SIZE 50000
#define CAMERA_FRAME_BUFFER_SIZE 4000000

#define PACKET_BUFFER_SIZE CAMERA_PACKET_DATA_MAX_SIZE + 10

#define BUFFERITEM(buf, offset, type) *((type*) &buf[offset])

uint64_t millisecond_time() {
    struct timespec  ts;
    clock_gettime(CLOCK_REALTIME, &ts);

    return (ts.tv_sec) * 1000 + (ts.tv_nsec) / 1000000;
}

int main(int argc, char** argv) {
    // Arguments: <bind port> <receiver address> <receiver port>
    if (argc < 4) {
        std::cerr << "[!] Incorrect usage!" << std::endl << argv[0] << " <bind port> <receiver address> <receiver port>" << std::endl;
        return 1;
    }

    int socket_fd = socket(AF_INET, SOCK_DGRAM, 0);
    if (socket_fd < 0)
    {
        // Socket open failure
        std::cerr << "[!] Failed to open socket!" << std::endl;
        return 1;
    }

    int port = atoi(argv[1]);
    char* receiver_address = argv[2];
    int receiver_port = atoi(argv[3]);

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

    cv::VideoCapture capture(0);

    capture.set(CV_CAP_PROP_FRAME_WIDTH, 1920);
    capture.set(CV_CAP_PROP_FRAME_HEIGHT, 1080);

    cv::Mat frame;
    std::vector<unsigned char> jpeg_buffer;

    uint8_t packet_buffer[PACKET_BUFFER_SIZE];
    uint16_t packet_timestamp = 1;

    std::vector<int> encode_options;
    encode_options.push_back(CV_IMWRITE_JPEG_QUALITY);
    encode_options.push_back(50);

    uint64_t last_time = millisecond_time();
    uint64_t cycles = 0;

    while(cycles < 100) {
        printf("> %f frames per second! %lu\n", (float) cycles / (millisecond_time() - last_time)*1000.0, millisecond_time() - last_time);
        // printf("Time since last capture: %lu\n", (millisecond_time() - last_time));
        // last_time = millisecond_time();

        capture >> frame;

        cv::imencode(".jpg", frame, jpeg_buffer, encode_options);

        uint8_t* frame_buffer = (uint8_t*) jpeg_buffer.data();
        size_t frame_buffer_size = (size_t) jpeg_buffer.size();

        uint8_t packet_count = (uint8_t) ((frame_buffer_size / CAMERA_PACKET_DATA_MAX_SIZE) + 1);

        for (uint8_t section_id = 0; section_id < packet_count; section_id++) {
            // Version
            BUFFERITEM(packet_buffer, 0, uint16_t) = htons(4);
            // Packet Type
            BUFFERITEM(packet_buffer, 2, uint8_t) = 2;
            // Timestamp
            BUFFERITEM(packet_buffer, 3, uint16_t) = htons(packet_timestamp);

            // Section ID
            BUFFERITEM(packet_buffer, 5, uint8_t) = section_id;
            // Section Count
            BUFFERITEM(packet_buffer, 6, uint8_t) = packet_count;

            uint16_t frame_data_size;

            if (section_id < packet_count - 1) {
                frame_data_size = CAMERA_PACKET_DATA_MAX_SIZE;
            } else {
                frame_data_size = (uint16_t) (frame_buffer_size % CAMERA_PACKET_DATA_MAX_SIZE);
            }

            // Frame data size
            BUFFERITEM(packet_buffer, 7, uint16_t) = htons(frame_data_size);

            // Frame data
            memcpy(&packet_buffer[9], frame_buffer, frame_data_size);

            // Move frame_buffer pointer along
            frame_buffer += CAMERA_PACKET_DATA_MAX_SIZE;

            struct sockaddr_in send_addr;
            // Clear the send address.
            memset((char*)&send_addr, 0, sizeof(send_addr));
            // Use IP addresses
            send_addr.sin_family = AF_INET;
            // Configure the port
            send_addr.sin_port = htons(receiver_port);
            // Configure the address
            inet_aton(receiver_address, &send_addr.sin_addr);

            ssize_t sent_size;

            // Send the packet
            if ((sent_size = sendto(socket_fd, packet_buffer, 5 + 4 + CAMERA_PACKET_DATA_MAX_SIZE, 0, (struct sockaddr*) &send_addr, sizeof(send_addr))) < 0)
            {
                // Send failure
                std::cerr << "[!] Failed to send packet!" << std::endl;
                std::cerr << "[!] Errno: " << errno << std::endl;
            }
        }

        if (packet_timestamp == UINT16_MAX) {
            packet_timestamp = 1;
        } else {
            packet_timestamp++;
        }

        cycles++;
    }

    return 0;
}