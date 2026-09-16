package com.system74.gypsum.apps

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

data class ProcessResult(
    val exitCode: Int,
    val output: String,
)

object ProcessRunner {
    fun makeExecutable(file: File) {
        if (!file.setExecutable(true, false)) {
            throw IllegalStateException("Failed to mark executable: ${file.absolutePath}")
        }
    }

    fun run(
        executable: File,
        workingDir: File,
        args: List<String> = emptyList(),
        onOutput: ((String) -> Unit)? = null,
    ): ProcessResult {
        require(executable.exists()) { "Executable not found: ${executable.absolutePath}" }

        makeExecutable(executable)

        val command = buildList {
            add(executable.absolutePath)
            addAll(args)
        }

        val process = ProcessBuilder(command)
            .directory(workingDir)
            .redirectErrorStream(true)
            .start()

        val output = StringBuilder()
        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            var line = reader.readLine()
            while (line != null) {
                output.appendLine(line)
                onOutput?.invoke(line)
                line = reader.readLine()
            }
        }

        val exitCode = process.waitFor()
        return ProcessResult(exitCode = exitCode, output = output.toString().trimEnd())
    }
}
