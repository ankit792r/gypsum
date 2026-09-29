package com.system74.gypsum.views.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.system74.gypsum.views.webview.WebviewComposable

@Composable
fun HomeView() {
    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Text("Home View", modifier = Modifier.padding(paddingValues))
        WebviewComposable()
    }
}