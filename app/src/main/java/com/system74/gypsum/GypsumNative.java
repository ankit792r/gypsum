package com.system74.gypsum;

import com.v7878.foreign.Arena;
import com.v7878.foreign.FunctionDescriptor;
import com.v7878.foreign.Linker;
import com.v7878.foreign.MemoryLayout;
import com.v7878.foreign.MemorySegment;
import com.v7878.foreign.SymbolLookup;
import com.v7878.foreign.ValueLayout;

import java.lang.invoke.MethodHandle;

/**
 * Java bindings for libgyruntime.so via PanamaPort FFM.
 * MethodHandle calls must live in Java — Kotlin wraps arguments as Object[].
 */
public final class GypsumNative {
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
}
