package com.system74.gypsum.views.webview

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebviewComposable() {
    AndroidView(
        factory = { context ->
           return@AndroidView WebView(context).apply {
               settings.javaScriptEnabled = true
               settings.loadWithOverviewMode = true
               settings.useWideViewPort = true
               settings.setSupportZoom(false)

               webViewClient = WebViewClient()

               loadUrl("file:///android_asset/index.html")
           }
        },
        update = {
            // it.loadUrl("https://google.com")
            // it.loadUrl("file:///android_asset/index.html")
        }
    )

}