package com.system74.gypsum.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.system74.gypsum.apps.AppKind
import com.system74.gypsum.apps.HostedApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onRunApp: (HostedApp) -> Unit,
    onImportBinary: () -> Unit,
    onUninstall: (String) -> Unit,
    onDismissMessage: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            onDismissMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Gypsum")
                        if (uiState.runtimeVersion.isNotEmpty()) {
                            Text(
                                text = "Runtime v${uiState.runtimeVersion}",
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onImportBinary) {
                Icon(Icons.Default.Add, contentDescription = "Import binary")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.apps.isEmpty() -> {
                EmptyAppsState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onImportBinary = onImportBinary,
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.apps, key = { it.id }) { app ->
                        AppCard(
                            app = app,
                            onRun = { onRunApp(app) },
                            onUninstall = { onUninstall(app.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyAppsState(
    modifier: Modifier = Modifier,
    onImportBinary: () -> Unit,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No hosted apps installed",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Import an Android .so library that exports gypsum_main().",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )
        TextButton(onClick = onImportBinary) {
            Text("Import binary")
        }
    }
}

@Composable
private fun AppCard(
    app: HostedApp,
    onRun: () -> Unit,
    onUninstall: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = app.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${app.kind.name} · v${app.version}",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = app.id,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row {
                IconButton(onClick = onRun) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Run")
                }
                IconButton(onClick = onUninstall) {
                    Icon(Icons.Default.Delete, contentDescription = "Uninstall")
                }
            }
        }
    }
}

@Composable
fun ImportBinaryDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, kind: AppKind) -> Unit,
) {
    var name by remember { mutableStateOf("Imported App") }
    var kind by remember { mutableStateOf(AppKind.CLI) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import binary") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("App name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { kind = AppKind.CLI }) {
                        Text(if (kind == AppKind.CLI) "[CLI]" else "CLI")
                    }
                    TextButton(onClick = { kind = AppKind.GUI }) {
                        Text(if (kind == AppKind.GUI) "[GUI]" else "GUI")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), kind) },
                enabled = name.isNotBlank(),
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
