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
import okio.ByteString
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class BlitzortungFeedClient @Inject constructor(
    @Named("WebSocketClient") private val okHttpClient: OkHttpClient,
    private val moshi: Moshi
) {
    private var webSocket: WebSocket? = null
    private val clientScope = CoroutineScope(Dispatchers.IO)

    private var servers = emptyList<String>()
    private var currentServerIndex = 0

    private val _strikes = MutableSharedFlow<Result<LightningStrikeDto>>(replay = 1)
    val strikes: SharedFlow<Result<LightningStrikeDto>> = _strikes

    private fun buildServerList() {
        val serverUrls = mutableListOf<String>()
        val ports = (8050..8090).shuffled()
        for (port in ports) {
            for (i in 1..4) {
                serverUrls.add("wss://ws$i.blitzortung.org:$port/")
            }
        }
        servers = serverUrls.shuffled()
        Timber.d("Built new server list with ${servers.size} total endpoints.")
    }

    private fun decode(data: ByteArray): String {
        val e = mutableMapOf<Int, String>()
        val d = data.map { it.toInt() and 0xff } // Ensure bytes are treated as unsigned
        if (d.isEmpty()) return ""
        var c = d[0]
        var f = c
        val g = mutableListOf<String>()
        g.add(c.toChar().toString())
        var h = 256
        for (i in 1 until d.size) {
            val byteVal = d[i]
            val aStr = e[byteVal] ?: if (h > byteVal) {
                byteVal.toChar().toString()
            } else {
                (f.toChar().toString() + c.toChar())
            }
            g.add(aStr)
            c = aStr[0].code
            e[h] = f.toChar().toString() + c.toChar()
            h++
            f = byteVal
        }
        return g.joinToString("")
    }

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Timber.d("WebSocket connection opened on ${servers.getOrNull(currentServerIndex)}")
            currentServerIndex = 0 // Reset for next time
            webSocket.send("""{"a":111}""")
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            val decodedData = decode(bytes.toByteArray())
            Timber.d("Received decoded message: $decodedData")
            try {
                val strike = moshi.adapter(LightningStrikeDto::class.java).fromJson(decodedData)
                if (strike != null) {
                    clientScope.launch {
                        _strikes.emit(Result.success(strike))
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse strike data from decoded JSON")
                clientScope.launch {
                    _strikes.emit(Result.failure(e))
                }
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Timber.e(t, "WebSocket failure on ${servers.getOrNull(currentServerIndex)}")
            clientScope.launch {
                _strikes.emit(Result.failure(t))
            }

            currentServerIndex++
            clientScope.launch {
                delay(1000)
                this@BlitzortungFeedClient.webSocket = null
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
        if (webSocket != null) {
            Timber.d("WebSocket already connected or connecting.")
            return
        }

        clientScope.launch {
            if (servers.isEmpty() || currentServerIndex >= servers.size) {
                buildServerList()
                currentServerIndex = 0
            }

            val url = servers[currentServerIndex]
            Timber.d("Connecting to WebSocket at $url (attempt ${currentServerIndex + 1}/${servers.size})")

            val request = Request.Builder()
                .url(url)
                .build()

            webSocket = okHttpClient.newWebSocket(request, webSocketListener)
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "User requested disconnect")
        webSocket = null
    }
} 