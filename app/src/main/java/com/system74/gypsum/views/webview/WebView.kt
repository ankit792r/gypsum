package com.system74.gypsum.views.webview

import android.annotation.SuppressLint
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewCompat.addWebMessageListener

@SuppressLint("SetJavaScriptEnabled", "RequiresFeature")
@Composable
fun WebviewComposable() {
    AndroidView(
        factory = { context ->
            val assetsLoader = WebViewAssetLoader.Builder()
                .addPathHandler(
                    "/assets/",
                    WebViewAssetLoader.AssetsPathHandler(context)
                )
                .build()

            val webviews = WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.setSupportZoom(false)

                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest
                    ): WebResourceResponse? {
                        return assetsLoader.shouldInterceptRequest(request.url)
                    }

                    override fun onRenderProcessGone(
                        view: WebView?,
                        detail: RenderProcessGoneDetail?
                    ): Boolean {
                        return super.onRenderProcessGone(view, detail)
                    }

                }
            }

            addWebMessageListener(
                webviews,
                "AndroidBridge",
                setOf("https://appassets.androidplatform.net")
            ) { view, message, sourceOrigin, isMainFrame, replyProxy ->
                run {
                    println(message.data)
                    replyProxy.postMessage(  """{"status":"success","message":"Hello from Kotlin"}""")
                }
            }

            webviews.loadUrl("https://appassets.androidplatform.net/assets/index.html")

            webviews
        },
    )

}