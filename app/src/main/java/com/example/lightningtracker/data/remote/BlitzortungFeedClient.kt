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
        val knownServers = listOf(
            "ws1.blitzortung.org",
            "ws2.blitzortung.org",
            "ws3.blitzortung.org",
            "ws4.blitzortung.org",
            "ws5.blitzortung.org",
            "ws6.blitzortung.org",
            "ws7.blitzortung.org",
            "ws8.blitzortung.org"
        )

        val serverUrls = mutableListOf<String>()
        // Add standard port 443 for all servers
        for (host in knownServers) {
            serverUrls.add("wss://$host:443/")
        }

        // Add specific port ranges
        val portRanges = mapOf(
            "ws1.blitzortung.org" to (8050..8053),
            "ws2.blitzortung.org" to (8060..8063),
            "ws3.blitzortung.org" to (8070..8073),
            "ws4.blitzortung.org" to (8080..8083),
            "ws5.blitzortung.org" to (8090..8093),
            "ws6.blitzortung.org" to (8050..8053),
            "ws7.blitzortung.org" to (8060..8063),
            "ws8.blitzortung.org" to (8070..8073)
        )

        for ((host, ports) in portRanges) {
            for (port in ports) {
                serverUrls.add("wss://$host:$port/")
            }
        }
        servers = serverUrls.shuffled()
        Timber.d("Built new server list with ${servers.size} total endpoints.")
    }

    private fun decode(data: String): String {
        if (data.isEmpty()) return ""

        val dict = mutableMapOf<Int, String>()
        val result = mutableListOf<String>()

        var oldPhrase = data[0].toString()
        result.add(oldPhrase)
        var currChar = oldPhrase

        var code = 256

        for (i in 1 until data.length) {
            val currCode = data[i].code
            val phrase = if (currCode < 256) {
                data[i].toString()
            } else {
                dict[currCode] ?: (oldPhrase + currChar)
            }
            result.add(phrase)
            currChar = phrase[0].toString()
            dict[code] = oldPhrase + currChar
            code++
            oldPhrase = phrase
        }

        return result.joinToString("")
    }

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Timber.d("WebSocket connection opened on ${servers.getOrNull(currentServerIndex)}")
            currentServerIndex = 0 // Reset for next time
            webSocket.send("""{"a":111}""")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Timber.d("Received raw text message.")
            // The server sends obfuscated data as a text string, so we need to decode it from here.
            val decodedData = decode(text)
            Timber.d("Received decoded message: $decodedData")
            try {
                val strike = moshi.adapter(LightningStrikeDto::class.java).lenient().fromJson(decodedData)
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

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            // This is likely no longer used as data comes in as text, but we'll keep it for logging just in case.
            Timber.d("Received unexpected binary message: ${bytes.hex()}")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            val url = servers.getOrNull(currentServerIndex) ?: "unknown URL"
            Timber.e(t, "WebSocket failure on $url")
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