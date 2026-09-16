#include "runtime.h"

#include <android/log.h>
#include <dlfcn.h>
#include <errno.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

#define LOG_TAG "gypsum-runtime"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

static int g_initialized = 0;

typedef int (*hosted_entry_fn)(int, char **);

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

static void write_output(char *output, size_t output_size, const char *message) {
    if (output == NULL || output_size == 0) {
        return;
    }
    snprintf(output, output_size, "%s", message != NULL ? message : "unknown error");
}

RT_EXPORT int rt_run_hosted(
    const char *path,
    char *output,
    size_t output_size,
    int *exit_code
) {
    if (path == NULL || output == NULL || output_size == 0 || exit_code == NULL) {
        return -1;
    }

    output[0] = '\0';
    *exit_code = -1;

    void *handle = dlopen(path, RTLD_NOW);
    if (handle == NULL) {
        write_output(output, output_size, dlerror());
        return -1;
    }

    hosted_entry_fn entry = (hosted_entry_fn)dlsym(handle, "gypsum_main");
    if (entry == NULL) {
        entry = (hosted_entry_fn)dlsym(handle, "main");
    }
    if (entry == NULL) {
        write_output(output, output_size, "Missing exported gypsum_main() or main() symbol");
        dlclose(handle);
        return -1;
    }

    int pipefd[2];
    if (pipe(pipefd) != 0) {
        write_output(output, output_size, strerror(errno));
        dlclose(handle);
        return -1;
    }

    int saved_stdout = dup(STDOUT_FILENO);
    if (saved_stdout < 0) {
        close(pipefd[0]);
        close(pipefd[1]);
        write_output(output, output_size, strerror(errno));
        dlclose(handle);
        return -1;
    }

    dup2(pipefd[1], STDOUT_FILENO);
    close(pipefd[1]);

    char prog[] = "gypsum";
    char *argv[] = {prog, NULL};
    *exit_code = entry(1, argv);

    fflush(stdout);
    dup2(saved_stdout, STDOUT_FILENO);
    close(saved_stdout);

    ssize_t total = 0;
    while (total < (ssize_t)output_size - 1) {
        ssize_t n = read(pipefd[0], output + total, output_size - 1 - (size_t)total);
        if (n <= 0) {
            break;
        }
        total += n;
    }
    close(pipefd[0]);
    output[total] = '\0';

    dlclose(handle);
    return 0;
}
