package com.system74.gypsum.views.webview

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebMessage
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.JavaScriptReplyProxy
import androidx.webkit.WebMessageCompat
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewCompat
import org.json.JSONObject
import java.lang.ref.WeakReference
import androidx.core.net.toUri

object WebViewBridge {

    private const val ASSET_ORIGIN =
        "https://appassets.androidplatform.net"

    private const val INDEX_URL =
        "$ASSET_ORIGIN/assets/index.html"

    /**
     * Weak reference means this singleton does NOT own the WebView.
     */
    private var webViewRef: WeakReference<WebView>? = null

    /**
     * Current message listener.
     */
    private var messageListener:
            ((type: String, data: JSONObject?) -> Unit)? = null


    private fun attach(webView: WebView) {
        webViewRef = WeakReference(webView)
    }

    private fun detach(webView: WebView) {

        // Only clear if this is the currently attached WebView.
        if (webViewRef?.get() === webView) {
            webViewRef?.clear()
            webViewRef = null
        }

        messageListener = null
    }

    fun onMessage(
        listener: (type: String, data: JSONObject?) -> Unit
    ) {
        messageListener = listener
    }

    fun postMessage(
        type: String,
        data: JSONObject? = null
    ) {

        val webView = webViewRef?.get()
            ?: return

        val message = JSONObject().apply {

            put("type", type)

            if (data != null) {
                put("data", data)
            }
        }

        webView.postWebMessage(
            WebMessage(message.toString()),
            ASSET_ORIGIN.toUri()
        )
    }


    fun postMessage(
        type: String,
        data: String
    ) {

        val webView = webViewRef?.get()
            ?: return

        val message = JSONObject().apply {
            put("type", type)
            put("data", data)
        }

        webView.postWebMessage(
            WebMessage(message.toString()),
            ASSET_ORIGIN.toUri()
        )
    }


    fun isReady(): Boolean {
        return webViewRef?.get() != null
    }

    @SuppressLint("SetJavaScriptEnabled", "RequiresFeature")
    @Composable
    fun WebViewComposable(
        modifier: Modifier = Modifier
    ) {

        AndroidView(
            modifier = modifier,

            factory = { context ->

                val assetLoader =
                    WebViewAssetLoader.Builder()
                        .addPathHandler(
                            "/assets/",
                            WebViewAssetLoader.AssetsPathHandler(context)
                        )
                        .build()

                WebView(context).apply {

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true

                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.setSupportZoom(false)


                    // -----------------------------------------
                    // Asset loader
                    // -----------------------------------------

                    webViewClient = object : WebViewClient() {

                        override fun shouldInterceptRequest(
                            view: WebView,
                            request: WebResourceRequest
                        ): WebResourceResponse? {

                            return assetLoader
                                .shouldInterceptRequest(request.url)
                        }
                    }


                    // -----------------------------------------
                    // JavaScript -> Kotlin
                    // -----------------------------------------

                    WebViewCompat.addWebMessageListener(
                        this,
                        "AndroidBridge",
                        setOf(ASSET_ORIGIN),

                        object : WebViewCompat.WebMessageListener {

                            override fun onPostMessage(
                                view: WebView,
                                message: WebMessageCompat,
                                sourceOrigin: Uri,
                                isMainFrame: Boolean,
                                replyProxy: JavaScriptReplyProxy
                            ) {

                                try {

                                    val json =
                                        JSONObject(
                                            message.data ?: return
                                        )

                                    val type =
                                        json.optString("type")

                                    val data =
                                        json.optJSONObject("data")

                                    messageListener?.invoke(
                                        type,
                                        data
                                    )

                                } catch (e: Exception) {

                                    e.printStackTrace()
                                }
                            }
                        }
                    )

                    attach(this)

                    loadUrl(INDEX_URL)
                }
            }
        )

        DisposableEffect(Unit) {
            onDispose {

                webViewRef?.get()?.let { webView ->

                    // Stop loading
                    webView.stopLoading()

                    // Remove references
                    // webView.webViewClient = null

                    // Remove from singleton
                    detach(webView)
                }
            }
        }
    }
}