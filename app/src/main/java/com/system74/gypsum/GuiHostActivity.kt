package com.system74.gypsum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.system74.gypsum.apps.AppRepository
import com.system74.gypsum.apps.ProcessRunner
import com.system74.gypsum.ui.theme.GypsumTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GuiHostActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appId = intent.getStringExtra(MainActivity.EXTRA_APP_ID)
            ?: run {
                finish()
                return
            }

        setContent {
            GypsumTheme {
                GuiHostScreen(appId = appId, onClose = { finish() })
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuiHostScreen(appId: String, onClose: () -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(appId) }
    var status by remember { mutableStateOf("Starting…") }
    var exitCode by remember { mutableIntStateOf(-1) }
    var running by remember { mutableStateOf(true) }

    LaunchedEffect(appId) {
        val repository = AppRepository(context)
        val app = repository.getApp(appId)

        if (app == null) {
            status = "App not found"
            running = false
            return@LaunchedEffect
        }

        title = app.name
        status = "Process running — graphics surface reserved for future runtime API"

        try {
            val result = withContext(Dispatchers.IO) {
                ProcessRunner.run(
                    executable = app.executable,
                    workingDir = app.installDir,
                )
            }

            exitCode = result.exitCode
            status = result.output.ifBlank {
                "Process exited with code ${result.exitCode}"
            }
        } catch (e: Exception) {
            status = e.message ?: "Failed to run GUI app"
        } finally {
            running = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(title) })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(Color(0xFF101418)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (running) "GUI surface" else "Stopped",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = status,
                        color = Color(0xFFB0BEC5),
                        modifier = Modifier.padding(top = 12.dp, start = 24.dp, end = 24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            if (!running) {
                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally),
                ) {
                    Text("Close")
                }
            }
        }
    }
}
