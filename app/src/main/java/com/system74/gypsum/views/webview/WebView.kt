package com.system74.gypsum.views.webview

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.JavaScriptReplyProxy
import androidx.webkit.WebMessageCompat
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewCompat

@SuppressLint("SetJavaScriptEnabled", "RequiresFeature")
@Composable
fun WebviewComposable() {
    AndroidView(
        factory = { context ->
            val assetLoader = WebViewAssetLoader.Builder()
                .addPathHandler(
                    "/assets/",
                    WebViewAssetLoader.AssetsPathHandler(context)
                )
                .build()

            val webView = WebView(context)

            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true

            webView.webViewClient = object : WebViewClient() {

                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ): WebResourceResponse? {
                    return assetLoader.shouldInterceptRequest(request.url)
                }
            }

            WebViewCompat.addWebMessageListener(
                webView,
                "AndroidBridge",
                setOf("https://appassets.androidplatform.net")
            ) { view, message, sourceOrigin, isMainFrame, replyProxy ->
                println(
                    "Received from JS: ${message.data}"
                )

                // Reply directly to the JavaScript caller
                replyProxy.postMessage(
                    """{"type":"HELLO_RESPONSE","message":"Hello from Kotlin"}"""
                )
            }

            webView.loadUrl(
                "https://appassets.androidplatform.net/assets/index.html"
            )

            webView
        },
    )

}