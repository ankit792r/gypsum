package com.system74.gypsum

import com.v7878.foreign.Arena
import com.v7878.foreign.FunctionDescriptor
import com.v7878.foreign.Linker
import com.v7878.foreign.MemoryLayout
import com.v7878.foreign.MemorySegment
import com.v7878.foreign.SymbolLookup
import com.v7878.foreign.ValueLayout.ADDRESS
import com.v7878.foreign.ValueLayout.JAVA_BYTE
import com.v7878.foreign.ValueLayout.JAVA_INT
import java.lang.invoke.MethodHandle

object GypsumRuntime {
    private val rtContextLayout = MemoryLayout.structLayout(
        JAVA_INT.withName("version"),
        JAVA_INT.withName("capabilities"),
    )

    private val cStringLayout = MemoryLayout.sequenceLayout(Long.MAX_VALUE, JAVA_BYTE)

    private val rtInit: MethodHandle
    private val rtVersion: MethodHandle

    init {
        System.loadLibrary("gyruntime")

        val linker = Linker.nativeLinker()
        val lookup = SymbolLookup.loaderLookup()

        rtInit = linker.downcallHandle(
            lookup.findOrThrow("rt_init"),
            FunctionDescriptor.of(JAVA_INT, ADDRESS.withTargetLayout(rtContextLayout)),
        )

        rtVersion = linker.downcallHandle(
            lookup.findOrThrow("rt_version"),
            FunctionDescriptor.of(ADDRESS.withTargetLayout(cStringLayout)),
        )
    }

    fun getVersion(): String {
        return try {
            Arena.ofConfined().use { arena ->
                val ctx = arena.allocate(rtContextLayout)
                rtInit.invoke(ctx)
                val versionPtr = rtVersion.invoke() as MemorySegment
                versionPtr.getString(0)
            }
        } catch (e: Throwable) {
            throw RuntimeException("Failed to call native runtime via FFM", e)
        }
    }
}
