#include <stdio.h>
#include <strings.h>
#include <stdlib.h>

extern "C" {
    #include <libavcodec/avcodec.h>
    #include <libavutil/opt.h>
    #include <libavutil/pixdesc.h>
    #include <libavutil/imgutils.h>
    #include <libavformat/avformat.h>
    #include <libswscale/swscale.h>
}

#include <opencv2/opencv.hpp>

#define BITRATE 1024 * 1024 * 3

#define OUT_PIXEL_FORMAT AV_PIX_FMT_YUV420P
#define IN_PIXEL_FORMAT AV_PIX_FMT_BGR24

#define PACKET_BUFFER 1024 * 1024 * 1

void rgb_to_yuv(SwsContext* ctx, AVFrame* frame, uint8_t* rgb_buffer) {
    const int linesize[1] = { 3 * 640 };

    sws_scale(ctx, (const uint8_t * const *) &rgb_buffer, linesize, 0, 480, frame->data, frame->linesize);
}

int main() {
    avcodec_register_all();

    AVCodec* h264_codec = avcodec_find_encoder(AV_CODEC_ID_H264);
    if (h264_codec == NULL) {
        printf("[!] Failed to load h264 codec!\n");
        return 1;
    }

    for (int i = 0, c = 0; (c = h264_codec->pix_fmts[i]) != -1;i++) {
        printf("SUPPORTED %s\n", av_get_pix_fmt_name((AVPixelFormat) c));   
    }

    AVCodecContext* ctx = avcodec_alloc_context3(h264_codec);
    if (ctx == NULL) {
        printf("[!] Failed to allocate context!\n");
        return 1;
    }

    avcodec_get_context_defaults3(ctx, h264_codec);

    ctx->codec_type = AVMEDIA_TYPE_VIDEO;
    ctx->pix_fmt = OUT_PIXEL_FORMAT;
    ctx->width = 640;
    ctx->height = 480;
    ctx->bit_rate = BITRATE;
    ctx->time_base = (AVRational){1,25};
    ctx->framerate = (AVRational){25,1};
    ctx->max_b_frames = 1;
    av_opt_set(ctx->priv_data, "preset", "slow", 0);

    if (avcodec_open2(ctx, h264_codec, NULL) != 0) {
        printf("[!] Failed to open encoder!\n");
        return 1;
    }

    // For converting OpenCV images to images suitable for encoding.
    SwsContext* sws_ctx = sws_getContext(640, 480, IN_PIXEL_FORMAT, 640, 480, OUT_PIXEL_FORMAT, 0, 0, 0, 0);
    if (sws_ctx == NULL) {
        printf("[!] Failed to get sws context!\n");
        return 1;
    }

    // Allocate a frame to use for encoding.
    AVFrame* avframe = av_frame_alloc();
    if (avframe == NULL) {
        printf("[!] Failed to allocate frame!\n");
        return 1;
    }

    // Fill the frame info about what type of frame we want.
    avframe->width = 640;
    avframe->height = 480;
    avframe->format = OUT_PIXEL_FORMAT;

    // Allocate the actual buffer, with the format above.
    if (av_image_alloc(avframe->data, avframe->linesize, ctx->width, ctx->height, OUT_PIXEL_FORMAT, 32) < 0) {
        printf("[!] Failed to allocate buffer for frame!\n");
        return 1;
    }

    // This is for receiving encoded packets.
    // This can be allocated on the stack.
    AVPacket packet;   
    packet.data = (uint8_t*) av_malloc(PACKET_BUFFER);
    packet.size = PACKET_BUFFER;

    cv::VideoCapture capture(0);
    cv::Mat frame;

    FILE* out = fopen("outputvideo.bat", "wb");

    int max_size = 0;

    for (;;) {
        capture >> frame;

        // Get pointer to OpenCV frame image data.
        unsigned char* frame_data_ptr = frame.ptr<unsigned char>(0);

        rgb_to_yuv(sws_ctx, avframe, (uint8_t*) frame_data_ptr);

        int got_packet;

        av_init_packet(&packet);
        av_free_packet(&packet);

        
        // Send the avframe to the encoder.
        if (avcodec_encode_video2(ctx, &packet, avframe, &got_packet) != 0) {
            printf("[!] Failed to encode frame!\n");
            return 1;
        }

        if (packet.size > max_size) {
            max_size = packet.size;
            printf("> SIZE: %d\n", packet.size);
        }

        fwrite(packet.data, 1, packet.size, out);
        fflush(out);

        cv::imshow("feed", frame);
        if (cv::waitKey(20) == 27) break;
    }

    for (;;) {
        av_init_packet(&packet);
        av_free_packet(&packet);

        int got_packet;        
        avcodec_encode_video2(ctx, &packet, NULL, &got_packet);
        if (packet.size == 0) break;

        fwrite(packet.data, 1, packet.size, out);
        fflush(out);
    }

    uint8_t endcode[] = { 0, 0, 1, 0xb7 };
    fwrite(endcode, 1, sizeof(endcode), out);

    av_frame_free(&avframe);
    fclose(out);

    return 0;
}