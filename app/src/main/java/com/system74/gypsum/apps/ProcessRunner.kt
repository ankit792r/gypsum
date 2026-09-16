package com.system74.gypsum.apps

import com.system74.gypsum.GypsumNative
import java.io.File

data class ProcessResult(
    val exitCode: Int,
    val output: String,
)

object ProcessRunner {
    /**
     * Android 10+ blocks execve() from app storage (SELinux W^X).
     * Hosted apps are loaded in-process via dlopen() instead.
     */
    fun run(
        executable: File,
        workingDir: File,
        args: List<String> = emptyList(),
        onOutput: ((String) -> Unit)? = null,
    ): ProcessResult {
        require(executable.exists()) {
            "Hosted library not found: ${executable.absolutePath}"
        }

        if (!workingDir.isDirectory) {
            throw IllegalStateException("Working directory not found: ${workingDir.absolutePath}")
        }

        if (args.isNotEmpty()) {
            // Argument forwarding can be added once the native runner supports it.
        }

        val result = GypsumNative.runHosted(executable.absolutePath)

        if (result.status != 0 && result.output.isBlank()) {
            return ProcessResult(
                exitCode = -1,
                output = buildString {
                    appendLine("Failed to load hosted library.")
                    appendLine()
                    appendLine("Hosted apps must be Android .so libraries exporting gypsum_main():")
                    appendLine("  - Built for Bionic (/system/bin/linker64)")
                    appendLine("  - PIE shared object (-shared -fPIC)")
                    appendLine("  - Exported symbol: gypsum_main")
                }.trimEnd(),
            )
        }

        onOutput?.invoke(result.output)
        return ProcessResult(exitCode = result.exitCode, output = result.output.trimEnd())
    }
}
