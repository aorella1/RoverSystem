#include <stdlib.h>
#include <stdio.h>
#include <string.h>

extern "C" {
    #include <libavcodec/avcodec.h>
    #include <libavutil/pixdesc.h>
}

#include <opencv2/opencv.hpp>

#define WIDTH 640
#define HEIGHT 480

#define TARGET_BITRATE (5 * 1024 * 1024)
#define FPS 25

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

int main() {
    avcodec_register_all();

    AVCodec* encoder_codec = avcodec_find_encoder(AV_CODEC_ID_VP9);
    if (encoder_codec == nullptr) {
        fprintf(stderr, "[!] Failed to find VP9 codec!\n");
        return 1;
    }

    printf("> Found VP9 codec %s.\n", encoder_codec->name);

    for (int i = 0; encoder_codec->pix_fmts[i] != -1; i++) {
        printf("    > Supports %s pixel format.\n", av_get_pix_fmt_name(encoder_codec->pix_fmts[i]));
    }

    AVCodecContext* encoder_context = avcodec_alloc_context3(encoder_codec);
    if (encoder_context == nullptr) {
        fprintf(stderr, "[!] Failed to allocate encoding context!\n");
        return 1;
    }

    // Set up the context.
    encoder_context->bit_rate = TARGET_BITRATE;
    encoder_context->width = WIDTH;
    encoder_context->height = HEIGHT;
    encoder_context->framerate = (AVRational){FPS, 1};
    encoder_context->time_base = (AVRational){1, FPS};
    encoder_context->gop_size = 10;
    encoder_context->max_b_frames = 1;
    encoder_context->pix_fmt = AV_PIX_FMT_YUV444P;

    if (avcodec_open2(encoder_context, encoder_codec, NULL) < 0) {
        fprintf(stderr, "[!] Failed to open the encoder!\n");
        return 1;
    }

    AVFrame* encoder_frame = av_frame_alloc();
    if (encoder_frame == nullptr) {
        fprintf(stderr, "[!] Failed to allocate frame!\n");
        return 1;
    }

    encoder_frame->format = encoder_context->pix_fmt;
    encoder_frame->width = WIDTH;
    encoder_frame->height = HEIGHT;

    AVPacket encoder_packet;

    if (av_frame_get_buffer(encoder_frame, 32) < 0) {
        fprintf(stderr, "[!] Failed to get frame buffer!\n");
        return 1;
    }

    cv::VideoCapture capture(0);
    cv::Mat frame_mat;

    capture.set(CV_CAP_PROP_FRAME_WIDTH, WIDTH);
    capture.set(CV_CAP_PROP_FRAME_HEIGHT, HEIGHT);

    int64_t timestamp = 0;
    for (;;) {
        capture >> frame_mat;

        if (av_frame_make_writable(frame) < 0) {
            fprintf(stderr, "[!] Frame is not writeable!\n");
            return 1;
        }

        rgb_to_ycbcr(frame_mat.ptr<uint8_t>(0), frame->data);

        frame->pts = timestamp++;
    }

    return 0;
}