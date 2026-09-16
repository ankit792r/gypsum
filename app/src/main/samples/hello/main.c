#include <stdio.h>

__attribute__((visibility("default")))
int gypsum_main(int argc, char *argv[]) {
    printf("Hello from Gypsum hosted app!\n");
    printf("Loaded via dlopen (Android Bionic).\n");

    for (int i = 1; i < argc; i++) {
        printf("argv[%d] = %s\n", i, argv[i]);
    }

    return 0;
}
