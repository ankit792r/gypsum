package com.system74.gypsum

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.system74.gypsum.apps.AppKind
import com.system74.gypsum.apps.HostedApp
import com.system74.gypsum.ui.HomeScreen
import com.system74.gypsum.ui.HomeViewModel
import com.system74.gypsum.ui.ImportBinaryDialog
import com.system74.gypsum.ui.theme.GypsumTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val viewModel: HomeViewModel by viewModels()
    private val importUriState = mutableStateOf<Uri?>(null)

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) {
            return@registerForActivityResult
        }

        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        } catch (_: SecurityException) {
            // Non-persistable URIs are still readable for this session.
        }

        importUriState.value = uri
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            val version = withContext(Dispatchers.Default) {
                GypsumRuntime.getVersion()
            }
            viewModel.setRuntimeVersion(version)
        }

        setContent {
            GypsumTheme {
                val uiState by viewModel.uiState.collectAsState()
                val importUri by importUriState

                HomeScreen(
                    uiState = uiState,
                    onRunApp = ::launchApp,
                    onInstallSample = { viewModel.installBundled("hello-cli") },
                    onImportBinary = {
                        importLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                    },
                    onUninstall = viewModel::uninstall,
                    onDismissMessage = viewModel::clearMessage,
                )

                importUri?.let { uri ->
                    ImportBinaryDialog(
                        onDismiss = { importUriState.value = null },
                        onConfirm = { name, kind ->
                            viewModel.importBinary(uri, name, kind)
                            importUriState.value = null
                        },
                    )
                }
            }
        }
    }

    private fun launchApp(app: HostedApp) {
        val target = when (app.kind) {
            AppKind.CLI -> RunnerActivity::class.java
            AppKind.GUI -> GuiHostActivity::class.java
        }

        startActivity(
            Intent(this, target).putExtra(EXTRA_APP_ID, app.id),
        )
    }

    companion object {
        const val EXTRA_APP_ID = "app_id"
    }
}
