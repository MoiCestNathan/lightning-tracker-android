package com.example.lightningtracker.data.remote

import com.example.lightningtracker.data.remote.dto.LightningStrikeDto
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlitzortungFeedClient @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val moshi: Moshi
) {
    private var webSocket: WebSocket? = null
    private val clientScope = CoroutineScope(Dispatchers.IO)

    private val _strikes = MutableSharedFlow<Result<LightningStrikeDto>>(replay = 1)
    val strikes: SharedFlow<Result<LightningStrikeDto>> = _strikes

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Timber.d("WebSocket connection opened.")
            // Subscribe to the feed for North America (region 5)
            webSocket.send("""{"a":5}""")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Timber.d("Received message: $text")
            try {
                val strike = moshi.adapter(LightningStrikeDto::class.java).fromJson(text)
                if (strike != null) {
                    clientScope.launch {
                        _strikes.emit(Result.success(strike))
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse strike data")
                clientScope.launch {
                    _strikes.emit(Result.failure(e))
                }
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Timber.e(t, "WebSocket failure")
            clientScope.launch {
                _strikes.emit(Result.failure(t))
            }
            // Attempt to reconnect after a delay
            clientScope.launch {
                delay(5000) // Wait 5 seconds before reconnecting
                connect()
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Timber.d("WebSocket closing: $code - $reason")
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Timber.d("WebSocket closed: $code - $reason")
        }
    }

    fun connect() {
        webSocket?.close(1000, "Reconnecting")
        
        val request = Request.Builder()
            .url("wss://ws.blitzortung.org:443/")
            .header("Origin", "https://www.blitzortung.org")
            .header("Host", "ws.blitzortung.org")
            .header("User-Agent", "LightningTracker/1.0")
            .header("Sec-WebSocket-Protocol", "lightningmaps.org")
            .build()

        webSocket = okHttpClient.newWebSocket(request, webSocketListener)
        Timber.d("Connecting to WebSocket...")
    }

    fun disconnect() {
        webSocket?.close(1000, "User requested disconnect")
        webSocket = null
    }
} 