#ifndef RUNTIME_H
#define RUNTIME_H

#include <stddef.h>
#include <stdint.h>

#define RT_VERSION_MAJOR 0
#define RT_VERSION_MINOR 1
#define RT_VERSION_PATCH 0

#define RT_EXPORT __attribute__((visibility("default")))

typedef struct {
    uint32_t version;
    uint32_t capabilities;
} RTContext;

RT_EXPORT int rt_init(RTContext *ctx);
RT_EXPORT const char *rt_version(void);
RT_EXPORT void rt_shutdown(void);

/** Load a hosted .so from app storage and call gypsum_main (or main). */
RT_EXPORT int rt_run_hosted(
    const char *path,
    char *output,
    size_t output_size,
    int *exit_code
);

#endif // RUNTIME_H
