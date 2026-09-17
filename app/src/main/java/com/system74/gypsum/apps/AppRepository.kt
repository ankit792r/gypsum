package com.system74.gypsum.apps

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import java.io.File
import java.util.UUID

class AppRepository(private val context: Context) {
    private val appsRoot: File
        get() = File(context.filesDir, "apps").also { it.mkdirs() }

    fun listApps(): List<HostedApp> {
        val root = appsRoot
        if (!root.exists()) {
            return emptyList()
        }

        return root.listFiles()
            ?.filter { it.isDirectory }
            ?.mapNotNull { dir -> readApp(dir) }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()
    }

    fun getApp(appId: String): HostedApp? {
        return readApp(File(appsRoot, appId))
    }

    fun installFromUri(uri: Uri, name: String, kind: AppKind = AppKind.CLI): HostedApp {
        val id = generateId(name)
        val installDir = File(appsRoot, id)
        installDir.mkdirs()

        val entryRelative = "bin/lib${slugFromName(name)}.so"
        val entryFile = File(installDir, entryRelative)
        entryFile.parentFile?.mkdirs()

        context.contentResolver.openInputStream(uri)?.use { input ->
            entryFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Unable to read selected file")

        val manifest = HostedApp(
            id = id,
            name = name,
            version = "1.0.0",
            kind = kind,
            entry = entryRelative,
            installDir = installDir,
        )

        writeManifest(manifest)
        return manifest
    }

    fun uninstall(appId: String): Boolean {
        val dir = File(appsRoot, appId)
        return if (dir.exists()) {
            dir.deleteRecursively()
        } else {
            false
        }
    }

    private fun readApp(dir: File): HostedApp? {
        val manifestFile = File(dir, "manifest.json")
        if (!manifestFile.exists()) {
            return null
        }

        return try {
            val json = JSONObject(manifestFile.readText())
            parseManifest(json, dir.name).copy(installDir = dir)
        } catch (_: Exception) {
            null
        }
    }

    private fun parseManifest(json: JSONObject, fallbackId: String): HostedApp {
        return HostedApp(
            id = json.optString("id", fallbackId),
            name = json.getString("name"),
            version = json.optString("version", "1.0.0"),
            kind = AppKind.fromManifest(json.optString("kind", "cli")),
            entry = json.optString("entry", "bin/main"),
            installDir = File(appsRoot, json.optString("id", fallbackId)),
        )
    }

    private fun writeManifest(app: HostedApp) {
        val json = JSONObject()
            .put("id", app.id)
            .put("name", app.name)
            .put("version", app.version)
            .put("entry", app.entry)
            .put("kind", AppKind.toManifest(app.kind))

        app.manifestFile.writeText(json.toString(2))
    }

    private fun generateId(name: String): String {
        return "${slugFromName(name)}-${UUID.randomUUID().toString().take(8)}"
    }

    private fun slugFromName(name: String): String {
        return name.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { "app" }
    }
}
