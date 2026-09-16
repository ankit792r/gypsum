package com.system74.gypsum.apps

enum class AppKind {
    CLI,
    GUI,
    ;

    companion object {
        fun fromManifest(value: String): AppKind {
            return when (value.lowercase()) {
                "gui" -> GUI
                else -> CLI
            }
        }

        fun toManifest(value: AppKind): String {
            return when (value) {
                CLI -> "cli"
                GUI -> "gui"
            }
        }
    }
}
