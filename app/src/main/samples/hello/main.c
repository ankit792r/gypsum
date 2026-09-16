#include <stdio.h>

int main(int argc, char *argv[]) {
    printf("Hello from Gypsum hosted binary!\n");
    printf("Compiled for Android / Bionic.\n");

    for (int i = 1; i < argc; i++) {
        printf("argv[%d] = %s\n", i, argv[i]);
    }

    return 0;
}
