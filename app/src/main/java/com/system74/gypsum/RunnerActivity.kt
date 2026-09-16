package com.system74.gypsum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.system74.gypsum.apps.AppRepository
import com.system74.gypsum.apps.ProcessRunner
import com.system74.gypsum.ui.theme.GypsumTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RunnerActivity : ComponentActivity() {
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
                CliRunnerScreen(appId = appId)
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CliRunnerScreen(appId: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var title by remember { mutableStateOf(appId) }
    var output by remember { mutableStateOf("Running…\n") }
    var exitCode by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(appId) {
        val repository = AppRepository(context)
        val app = repository.getApp(appId) ?: run {
            output = "App not found: $appId"
            return@LaunchedEffect
        }

        title = app.name
        output = "$ ${app.executable.name}\n"

        val result = withContext(Dispatchers.IO) {
            ProcessRunner.run(
                executable = app.executable,
                workingDir = app.installDir,
                onOutput = null,
            )
        }

        output += result.output
        if (result.output.isNotEmpty()) {
            output += "\n"
        }
        output += "\n[exit ${result.exitCode}]"
        exitCode = result.exitCode
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (exitCode == null) {
                            "$title (running)"
                        } else {
                            "$title (exit $exitCode)"
                        },
                    )
                },
            )
        },
    ) { innerPadding ->
        Text(
            text = output,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
