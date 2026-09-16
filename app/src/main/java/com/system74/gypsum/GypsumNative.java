package com.system74.gypsum;

import com.v7878.foreign.Arena;
import com.v7878.foreign.FunctionDescriptor;
import com.v7878.foreign.Linker;
import com.v7878.foreign.MemoryLayout;
import com.v7878.foreign.MemorySegment;
import com.v7878.foreign.SymbolLookup;
import com.v7878.foreign.ValueLayout;

import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

/**
 * Java bindings for libgyruntime.so via PanamaPort FFM.
 * MethodHandle calls must live in Java — Kotlin wraps arguments as Object[].
 */
public final class GypsumNative {
    private static final int HOST_OUTPUT_SIZE = 64 * 1024;

    private static final MemoryLayout RT_CONTEXT = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("version"),
            ValueLayout.JAVA_INT.withName("capabilities")
    );

    private static final MemoryLayout C_STRING = MemoryLayout.sequenceLayout(
            Long.MAX_VALUE,
            ValueLayout.JAVA_BYTE
    );

    private static final MethodHandle RT_INIT;
    private static final MethodHandle RT_VERSION;
    private static final MethodHandle RT_RUN_HOSTED;

    static {
        System.loadLibrary("gyruntime");

        Linker linker = Linker.nativeLinker();
        SymbolLookup lookup = SymbolLookup.loaderLookup();

        RT_INIT = linker.downcallHandle(
                lookup.findOrThrow("rt_init"),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS.withTargetLayout(RT_CONTEXT)
                )
        );

        RT_VERSION = linker.downcallHandle(
                lookup.findOrThrow("rt_version"),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS.withTargetLayout(C_STRING)
                )
        );

        RT_RUN_HOSTED = linker.downcallHandle(
                lookup.findOrThrow("rt_run_hosted"),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.JAVA_LONG,
                        ValueLayout.ADDRESS
                )
        );
    }

    private GypsumNative() {
    }

    public static String getVersion() throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment ctx = arena.allocate(RT_CONTEXT);
            RT_INIT.invoke(ctx);
            MemorySegment versionPtr = (MemorySegment) RT_VERSION.invoke();
            return versionPtr.getString(0);
        }
    }

    public static HostedRunResult runHosted(String libraryPath) throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            byte[] pathBytes = libraryPath.getBytes(StandardCharsets.UTF_8);
            MemorySegment pathSegment = arena.allocate(pathBytes.length + 1L);
            for (int i = 0; i < pathBytes.length; i++) {
                pathSegment.set(ValueLayout.JAVA_BYTE, i, pathBytes[i]);
            }
            pathSegment.set(ValueLayout.JAVA_BYTE, pathBytes.length, (byte) 0);
            MemorySegment outputSegment = arena.allocate(HOST_OUTPUT_SIZE);
            MemorySegment exitCodeSegment = arena.allocate(ValueLayout.JAVA_INT);

            int status = (int) RT_RUN_HOSTED.invoke(
                    pathSegment,
                    outputSegment,
                    (long) HOST_OUTPUT_SIZE,
                    exitCodeSegment
            );

            int exitCode = exitCodeSegment.get(ValueLayout.JAVA_INT, 0);
            String output = outputSegment.getString(0);
            return new HostedRunResult(status, exitCode, output);
        }
    }

    public record HostedRunResult(int status, int exitCode, String output) {
    }
}
