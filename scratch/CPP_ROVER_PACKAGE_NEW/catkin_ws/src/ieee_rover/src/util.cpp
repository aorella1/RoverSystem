#include "util.h"

uint64_t millisecond_time() {
    struct timespec  ts;
    clock_gettime(CLOCK_REALTIME, &ts);

    return (ts.tv_sec) * 1000 + (ts.tv_nsec) / 1000000;
}