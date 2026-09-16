package com.system74.gypsum.apps

import java.io.File

data class HostedApp(
    val id: String,
    val name: String,
    val version: String,
    val kind: AppKind,
    val entry: String,
    val installDir: File,
) {
    val executable: File
        get() = File(installDir, entry)

    val manifestFile: File
        get() = File(installDir, "manifest.json")
}
