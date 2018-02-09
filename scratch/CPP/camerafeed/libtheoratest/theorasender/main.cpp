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
#define NETWORK_PACKET_BUFFER_SIZE 65307

// from https://stackoverflow.com/questions/3022552/is-there-any-standard-htonl-like-function-for-64-bits-integers-in-c
#define htonll(x) ((1==htonl(1)) ? (x) : ((uint64_t)htonl((x) & 0xFFFFFFFF) << 32) | htonl((x) >> 32))
#define ntohll(x) ((1==ntohl(1)) ? (x) : ((uint64_t)ntohl((x) & 0xFFFFFFFF) << 32) | ntohl((x) >> 32))

#define BUFFERITEM(buffer, offset, type) *((type*) &buffer[offset])

// Represents a network manager.
struct Network {
    int socket_fd;

    std::string send_address;
    int send_port;

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

    bool send(ogg_page* page) {
        // Write the size of the packet data.
        BUFFERITEM(packet_buffer, 0, uint32_t) = htonl(packet->bytes);

        // Write the b_o_s and e_o_s.
        BUFFERITEM(packet_buffer, 4, uint32_t) = htonl(packet->b_o_s);
        BUFFERITEM(packet_buffer, 8, uint32_t) = htonl(packet->e_o_s);

        // Write the granule pos and packet no.
        BUFFERITEM(packet_buffer, 12, uint64_t) = htonll(packet->granulepos);
        BUFFERITEM(packet_buffer, 20, uint64_t) = htonll(packet->packetno);

        // Write the packet data
        printf("> ABOUT TO WRITE PACKET WITH SIZE %ld TO A BUFFER WITH %d SPACE!\n", packet->bytes, NETWORK_PACKET_BUFFER_SIZE);
        memcpy(&packet_buffer[28], packet->packet, packet->bytes);

        struct sockaddr_in send_addr;
        memset((char*)&send_addr, 0, sizeof(send_addr));
        send_addr.sin_family = AF_INET;
        send_addr.sin_port = htons(send_port);
        inet_aton(send_address.c_str(), &send_addr.sin_addr);

        if (sendto(socket_fd, packet_buffer, packet->bytes + 28, 0, (struct sockaddr*) &send_addr, sizeof(send_addr)) < 0) {
            // Send failure
            printf("[!] Failed to send packet!\n");
            return false;
        }

        return true;
    }
};

// Consumes an OGG page, writing it to wherever it is going.
void consume_page(Network* network, ogg_page* page) {
    printf("> Page with size %ld\n", packet->bytes);
    network->send(packet);
}

// Converts a packed RGB24 buffer to a tri-planar YCbCr 4:4:4 buffer.
void rgb_to_ycbcr(uint8_t* rgb_buffer, uint8_t* ycbcr[3]) {
    for (int i = 0; i < WIDTH * HEIGHT; i++) {
        uint8_t r = rgb_buffer[(i*3) + 0];
        uint8_t g = rgb_buffer[(i*3) + 1];
        uint8_t b = rgb_buffer[(i*3) + 2];

        uint8_t y = (uint8_t) (0.299*r + 0.587*g + 0.114*b);
        uint8_t cb = (uint8_t) (128.0 - 0.168736*r - 0.331264*g + 0.5*b);
        uint8_t cr = (uint8_t) (128.0 + 0.5*r - 0.418688*g - 0.081312*b);

        ycbcr[0][i] = y;
        ycbcr[1][i] = cb;
        ycbcr[2][i] = cr;
    }
}

int main(int argc, char** argv) {
    // Args: <bind port> <receiver address> <receiver port>
    if (argc < 4) {
        fprintf(stderr, "[!] Incorrect usage! %s <bind port> <receiver address> <receiver port>\n", argv[0]);
        return 1;
    }

    int bind_port = atoi(argv[1]);

    Network network;
    network.send_address = std::string(argv[2]);
    network.send_port = atoi(argv[3]);

    if (!network.init(bind_port)) return 1;

    // OpenCV stuff.
    cv::VideoCapture capture(0);
    cv::Mat frame_mat;

    if (!capture.isOpened()) {
        fprintf(stderr, "[!] Failed to open OpenCV capture!\n");
        return 1;
    }

    capture.set(CV_CAP_PROP_FRAME_WIDTH, WIDTH);
    capture.set(CV_CAP_PROP_FRAME_HEIGHT, HEIGHT);

    // OGG stream stuff. We need this. We doooooo.
    ogg_stream_state oss;
    // Completely arbitrary 14.
    ogg_stream_init(&oss, 14);

    ogg_page output_page;

    // Encoding format information.
    th_info encoding_info;

    // Init this before we mess it up.
    th_info_init(&encoding_info);

    // These have to be a multiple of 16, so we make them large enough
    // to hold our desired width and height.
    encoding_info.frame_width = WIDTH + (WIDTH % 16);
    encoding_info.frame_height = HEIGHT + (HEIGHT % 16);

    // The width and height of the actual frame.
    encoding_info.pic_width = WIDTH;
    encoding_info.pic_height = HEIGHT;

    // Offsets... we don't want any.
    encoding_info.pic_x = 0;
    encoding_info.pic_y = 0;

    // We do not specify the color space, since its assumed to be RGB24 eventually.
    encoding_info.colorspace = TH_CS_UNSPECIFIED;

    // Three planes: Y, Cb, Cr. Each one specifies a value for every pixel.
    // Since we get information in RGB24, this is ideal.
    encoding_info.pixel_fmt = TH_PF_444;

    // We want variable bit rate. That way, we can specify the quality.
    encoding_info.target_bitrate = 0;

    // Specify quality. The bit rate will vary but quality will not.
    encoding_info.quality = STREAM_QUALITY;

    // Set the FPS we want.
    encoding_info.fps_numerator = 30;
    encoding_info.fps_denominator = 1;

    // Allocate an encoding context with the settings specified above.
    th_enc_ctx* encoding_context = th_encode_alloc(&encoding_info);
    if (encoding_context == NULL) {
        fprintf(stderr, "[!] Failed to allocate encoding context!\n");
        return 1;
    }

    // We need to give the stream a comment, but we don't have anything to
    // say (except who we are).
    th_comment stream_comment;
    stream_comment.user_comments = NULL;
    stream_comment.comment_lengths = NULL;
    stream_comment.comments = 0;
    stream_comment.vendor = VENDOR_STRING;

    // The packet structure which will hold each ogg packet as it is encoded.
    ogg_packet current_ogg_packet;

    // A buffer for YCbCr frame information. An RGB -> YCbCr conversion is done for each
    // frame, and this holds the result.
    uint8_t* frame_ycbcr_buffer[3];

    frame_ycbcr_buffer[0] = (uint8_t*) malloc(WIDTH * HEIGHT);
    frame_ycbcr_buffer[1] = (uint8_t*) malloc(WIDTH * HEIGHT);
    frame_ycbcr_buffer[2] = (uint8_t*) malloc(WIDTH * HEIGHT);

    // A "proxy buffer" that will be filled with the appropriate pointers to the above buffer.
    th_ycbcr_buffer frame_proxy_buffer;

    // Width, height, stride (width), data.
    frame_proxy_buffer[0] = { WIDTH, HEIGHT, WIDTH, frame_ycbcr_buffer[0] };
    frame_proxy_buffer[1] = { WIDTH, HEIGHT, WIDTH, frame_ycbcr_buffer[1] };
    frame_proxy_buffer[2] = { WIDTH, HEIGHT, WIDTH, frame_ycbcr_buffer[2] };

    // Call this until it is done to write all header information to the stream.
    for (;;) {
        int res = th_encode_flushheader(encoding_context, &stream_comment, &current_ogg_packet);
        if (res < 0) {
            fprintf(stderr, "[!] Failed to flush header!\n");
            return 1;
        }

        consume_packet(&network, &current_ogg_packet);

        // We are done when res == 0.
        if (res == 0) break;
    }

    bool quit = false;

    for (;;) {
        capture >> frame_mat;

        // Convert the current frame to YCbCr and put it in our buffer.
        rgb_to_ycbcr(frame_mat.ptr<uint8_t>(0), frame_ycbcr_buffer);

        // Encode the frame.
        if (th_encode_ycbcr_in(encoding_context, frame_proxy_buffer) != 0) {
            fprintf(stderr, "[!] Failed to encode frame!\n");
            return 1;
        }

        // Flush all available packets.
        for (;;) {
            int res = th_encode_packetout(encoding_context, quit ? 1 : 0, &current_ogg_packet);
            if (res < 0) {
                fprintf(stderr, "[!] Failed to flush packet!\n");
                return 1;
            }

            ogg_stream_packetin(&oss, &current_ogg_packet);

            if (ogg_stream_pageout(&oss, &output_page) != 0) {
                // We are good to write to the network!
                consume_page(&network, &output_page);
            }

            if (res == 0) break;
        }

        if (quit) break;

        cv::imshow("feed", frame_mat);
        if (cv::waitKey(20) == 27) quit = true;
    }

    // Free our encoding context.
    th_encode_free(encoding_context);

    ogg_stream_destroy(&oss);

    return 0;
}