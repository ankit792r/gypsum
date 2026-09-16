#include "runtime.h"

#include <android/log.h>

#define LOG_TAG "gypsum-runtime"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static int g_initialized = 0;

RT_EXPORT int rt_init(RTContext *ctx) {
    if (ctx != NULL) {
        ctx->version = (RT_VERSION_MAJOR << 16) | (RT_VERSION_MINOR << 8) | RT_VERSION_PATCH;
        ctx->capabilities = 0;
    }

    if (g_initialized) {
        return 0;
    }

    g_initialized = 1;
    LOGI("Gypsum runtime initialized (v%s)", rt_version());
    return 0;
}

RT_EXPORT const char *rt_version(void) {
    return "0.1.0";
}

RT_EXPORT void rt_shutdown(void) {
    if (!g_initialized) {
        return;
    }

    g_initialized = 0;
    LOGI("Gypsum runtime shut down");
}
