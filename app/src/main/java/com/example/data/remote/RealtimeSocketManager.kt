package com.example.data.remote

import android.content.Context
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class RealtimeSpkData(
    val unitKode: String,
    val nomorSpk: String,
    val statusSpk: String = "SPK_TERBIT",
    val rencanaId: Long = 0L,
    val serverPlanId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class RealtimeDeleteData(
    val deletedId: String = "",
    val numericId: Long = 0L,
    val unitKode: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Real-time Native WebSocket & Socket.IO Client connected to Web Admin.
 * Listens directly to real-time events:
 * - SPK_PUBLISHED / SPK_TERBIT / UPDATE_RENCANA / RENCANA_STATUS_UPDATED
 * - RENCANA_KERJA_DELETED (penghapusan rencana kerja oleh Admin Web)
 * and dispatches them to Room SQLite and UI state.
 */
class RealtimeSocketManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    companion object {
        const val BASE_URL = "https://1-portalce.ai.studio"
    }

    private val client = OkHttpClient.Builder()
        .pingInterval(15, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // Infinite for persistent WebSocket
        .build()

    private var webSocket: WebSocket? = null
    private var socketIo: Socket? = null
    private var isManuallyClosed = false

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _spkPublishedEvents = MutableSharedFlow<RealtimeSpkData>(extraBufferCapacity = 64)
    val spkPublishedEvents = _spkPublishedEvents.asSharedFlow()

    private val _rencanaDeletedEvents = MutableSharedFlow<RealtimeDeleteData>(extraBufferCapacity = 64)
    val rencanaDeletedEvents = _rencanaDeletedEvents.asSharedFlow()

    var onConnectedCallback: (() -> Unit)? = null
    var onRencanaDeletedCallback: ((RealtimeDeleteData) -> Unit)? = null

    val socket: Socket?
        get() = socketIo

    private fun extractFirstNonEmpty(vararg sources: JSONObject?, keys: Array<String>): String {
        for (src in sources) {
            if (src == null) continue
            for (key in keys) {
                if (src.has(key) && !src.isNull(key)) {
                    val str = src.optString(key, "").trim()
                    if (str.isNotBlank() && !str.equals("null", ignoreCase = true)) {
                        return str
                    }
                }
            }
        }
        return ""
    }

    fun connect(
        serverUrl: String = BASE_URL,
        onSpkReceived: ((RealtimeSpkData) -> Unit)? = null,
        onDeletedReceived: ((RealtimeDeleteData) -> Unit)? = null
    ) {
        disconnect()
        isManuallyClosed = false

        val cleanUrl = if (serverUrl.isBlank()) BASE_URL else serverUrl.trim().removeSuffix("/")

        // 1. Inisialisasi Socket.IO Client (standar Web Admin)
        try {
            val opts = IO.Options().apply {
                reconnection = true
                reconnectionAttempts = 10
                reconnectionDelay = 2000
                timeout = 10000
            }
            val sIo = IO.socket(cleanUrl, opts)
            socketIo = sIo

            sIo.on(Socket.EVENT_CONNECT) {
                Log.d("RealtimeSocketManager", "✅ Socket.IO connected to: $cleanUrl")
                _isConnected.value = true
                try {
                    onConnectedCallback?.invoke()
                } catch (e: Exception) {
                    Log.e("RealtimeSocketManager", "Error in onConnectedCallback", e)
                }
            }

            sIo.on(Socket.EVENT_DISCONNECT) {
                Log.d("RealtimeSocketManager", "🔌 Socket.IO disconnected")
                _isConnected.value = false
            }

            // Real-time Listener SPK
            val spkEventNames = arrayOf(
                "SPK_PUBLISHED", "SPK_TERBIT", "UPDATE_RENCANA",
                "RENCANA_STATUS_UPDATED", "RENCANA_KERJA_UPDATED"
            )
            for (evtName in spkEventNames) {
                sIo.on(evtName) { args ->
                    handleSocketIoSpk(args, onSpkReceived)
                }
            }

            // 2. REAL-TIME LISTENER PENGHAPUSAN (SOCKET.IO)
            // Sesuai instruksi: socket.on("RENCANA_KERJA_DELETED")
            sIo.on("RENCANA_KERJA_DELETED") { args ->
                try {
                    if (args != null && args.isNotEmpty()) {
                        val data = when (val first = args[0]) {
                            is JSONObject -> first
                            is String -> JSONObject(first)
                            else -> JSONObject()
                        }
                        val deletedId = extractFirstNonEmpty(data, keys = arrayOf("id", "id_rencana_kerja", "rencana_id", "plan_id"))
                        val unitKode = extractFirstNonEmpty(data, keys = arrayOf("unit_kode", "kode_unit", "unit", "kodeUnit", "unitKode"))
                        val numId = deletedId.filter { it.isDigit() }.toLongOrNull() ?: 0L

                        Log.d("RealtimeSocketManager", "🗑️ Socket.IO RENCANA_KERJA_DELETED: id=$deletedId, unit=$unitKode")
                        val delData = RealtimeDeleteData(deletedId, numId, unitKode)
                        scope.launch {
                            _rencanaDeletedEvents.emit(delData)
                        }
                        onRencanaDeletedCallback?.invoke(delData)
                        onDeletedReceived?.invoke(delData)
                    }
                } catch (e: Exception) {
                    Log.e("RealtimeSocketManager", "Error handling RENCANA_KERJA_DELETED in Socket.IO", e)
                }
            }

            sIo.connect()
        } catch (e: Exception) {
            Log.w("RealtimeSocketManager", "Socket.IO connection warning: ${e.localizedMessage}")
        }

        // 2. Native WebSocket Fallback (/ws)
        try {
            val wsUrl = when {
                cleanUrl.startsWith("wss://") -> if (cleanUrl.endsWith("/ws")) cleanUrl else "$cleanUrl/ws"
                cleanUrl.startsWith("ws://") -> if (cleanUrl.endsWith("/ws")) cleanUrl else "$cleanUrl/ws"
                cleanUrl.startsWith("https://") -> cleanUrl.replace("https://", "wss://") + "/ws"
                cleanUrl.startsWith("http://") -> cleanUrl.replace("http://", "ws://") + "/ws"
                else -> "wss://$cleanUrl/ws"
            }

            Log.d("RealtimeSocketManager", "🔌 Connecting native WebSocket to: $wsUrl")

            val request = Request.Builder()
                .url(wsUrl)
                .build()

            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d("RealtimeSocketManager", "✅ Native WebSocket connected to Web Admin: $wsUrl")
                    _isConnected.value = true
                    try {
                        onConnectedCallback?.invoke()
                    } catch (e: Exception) {
                        Log.e("RealtimeSocketManager", "Error in onConnectedCallback", e)
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.d("RealtimeSocketManager", "📩 Received WS message: $text")
                    try {
                        val root = JSONObject(text)
                        val event = root.optString("event", root.optString("type", ""))

                        val dataObj = root.optJSONObject("data")
                        val payloadObj = root.optJSONObject("payload")

                        // Cek Event Penghapusan Rencana Kerja (RENCANA_KERJA_DELETED)
                        val isDeleteEvent = event.equals("RENCANA_KERJA_DELETED", ignoreCase = true) ||
                                event.equals("DELETE_RENCANA", ignoreCase = true) ||
                                event.equals("RENCANA_DELETED", ignoreCase = true)

                        if (isDeleteEvent) {
                            val deletedId = extractFirstNonEmpty(
                                dataObj, payloadObj, root,
                                keys = arrayOf("id", "id_rencana_kerja", "rencana_id", "plan_id")
                            )
                            val unitKode = extractFirstNonEmpty(
                                dataObj, payloadObj, root,
                                keys = arrayOf("unit_kode", "kode_unit", "unit", "kodeUnit", "unitKode", "nama_unit")
                            )
                            val numId = deletedId.filter { it.isDigit() }.toLongOrNull() ?: 0L

                            Log.d("RealtimeSocketManager", "🗑️ Native WS RENCANA_KERJA_DELETED: id=$deletedId, unit=$unitKode")
                            val delData = RealtimeDeleteData(deletedId, numId, unitKode)
                            scope.launch {
                                _rencanaDeletedEvents.emit(delData)
                            }
                            onRencanaDeletedCallback?.invoke(delData)
                            onDeletedReceived?.invoke(delData)
                            return
                        }

                        // Cek Event SPK
                        val nomorSpk = extractFirstNonEmpty(
                            dataObj, payloadObj, root,
                            keys = arrayOf("nomor_spk", "nomorSpk", "no_spk", "spk", "spk_number", "noSpk")
                        )

                        val unitKode = extractFirstNonEmpty(
                            dataObj, payloadObj, root,
                            keys = arrayOf("kode_unit", "unit_kode", "unit", "kodeUnit", "unitKode", "nama_unit")
                        )

                        val statusSpkRaw = extractFirstNonEmpty(
                            dataObj, payloadObj, root,
                            keys = arrayOf("status_spk", "statusSpk", "status", "status_tahap")
                        )
                        val statusSpk = if (statusSpkRaw.isNotBlank() && !statusSpkRaw.equals("MENUNGGU_SPK", ignoreCase = true)) {
                            statusSpkRaw
                        } else {
                            "SPK_TERBIT"
                        }

                        val rawId = extractFirstNonEmpty(
                            dataObj, payloadObj, root,
                            keys = arrayOf("id", "id_rencana_kerja", "rencana_id", "plan_id", "id_rencana")
                        )

                        val numericId = rawId.filter { it.isDigit() }.toLongOrNull() ?: 0L

                        val isSpkRelated = event.equals("SPK_PUBLISHED", ignoreCase = true) ||
                                event.equals("SPK_TERBIT", ignoreCase = true) ||
                                event.equals("UPDATE_RENCANA", ignoreCase = true) ||
                                event.equals("RENCANA_STATUS_UPDATED", ignoreCase = true) ||
                                event.equals("RENCANA_KERJA_UPDATED", ignoreCase = true) ||
                                nomorSpk.isNotBlank()

                        if (isSpkRelated && nomorSpk.isNotBlank()) {
                            Log.d("RealtimeSocketManager", "🎯 SPK Event: unit=$unitKode, spk=$nomorSpk, id=$rawId, event=$event")
                            val spkData = RealtimeSpkData(
                                unitKode = unitKode,
                                nomorSpk = nomorSpk,
                                statusSpk = statusSpk,
                                rencanaId = numericId,
                                serverPlanId = rawId
                            )
                            scope.launch {
                                _spkPublishedEvents.emit(spkData)
                            }
                            onSpkReceived?.invoke(spkData)
                        }
                    } catch (e: Exception) {
                        Log.e("RealtimeSocketManager", "Error parsing WebSocket message", e)
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d("RealtimeSocketManager", "WebSocket closing: $code / $reason")
                    _isConnected.value = false
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d("RealtimeSocketManager", "WebSocket closed: $code / $reason")
                    _isConnected.value = false
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.w("RealtimeSocketManager", "WebSocket connection failure: ${t.localizedMessage}")
                    _isConnected.value = false
                    if (!isManuallyClosed) {
                        scope.launch {
                            delay(3000)
                            if (!isManuallyClosed) {
                                connect(serverUrl, onSpkReceived, onDeletedReceived)
                            }
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("RealtimeSocketManager", "Failed to connect WebSocket to $serverUrl", e)
        }
    }

    private fun handleSocketIoSpk(args: Array<Any>?, onSpkReceived: ((RealtimeSpkData) -> Unit)?) {
        try {
            if (args != null && args.isNotEmpty()) {
                val data = when (val first = args[0]) {
                    is JSONObject -> first
                    is String -> JSONObject(first)
                    else -> JSONObject()
                }
                val nomorSpk = extractFirstNonEmpty(data, keys = arrayOf("nomor_spk", "nomorSpk", "no_spk", "spk", "spk_number", "noSpk"))
                val unitKode = extractFirstNonEmpty(data, keys = arrayOf("kode_unit", "unit_kode", "unit", "kodeUnit", "unitKode", "nama_unit"))
                val statusSpkRaw = extractFirstNonEmpty(data, keys = arrayOf("status_spk", "statusSpk", "status"))
                val statusSpk = if (statusSpkRaw.isNotBlank() && !statusSpkRaw.equals("MENUNGGU_SPK", ignoreCase = true)) statusSpkRaw else "SPK_TERBIT"
                val rawId = extractFirstNonEmpty(data, keys = arrayOf("id", "id_rencana_kerja", "rencana_id", "plan_id"))
                val numericId = rawId.filter { it.isDigit() }.toLongOrNull() ?: 0L

                if (nomorSpk.isNotBlank()) {
                    val spkData = RealtimeSpkData(
                        unitKode = unitKode,
                        nomorSpk = nomorSpk,
                        statusSpk = statusSpk,
                        rencanaId = numericId,
                        serverPlanId = rawId
                    )
                    scope.launch {
                        _spkPublishedEvents.emit(spkData)
                    }
                    onSpkReceived?.invoke(spkData)
                }
            }
        } catch (e: Exception) {
            Log.e("RealtimeSocketManager", "Error handling Socket.IO SPK event", e)
        }
    }

    fun dispatchSpkPublished(unitKode: String, nomorSpk: String, rencanaId: Long) {
        val event = RealtimeSpkData(
            unitKode = unitKode,
            nomorSpk = nomorSpk,
            rencanaId = rencanaId
        )
        scope.launch {
            _spkPublishedEvents.emit(event)
        }
    }

    fun dispatchRencanaDeleted(deletedId: String, unitKode: String) {
        val numId = deletedId.filter { it.isDigit() }.toLongOrNull() ?: 0L
        val event = RealtimeDeleteData(deletedId, numId, unitKode)
        scope.launch {
            _rencanaDeletedEvents.emit(event)
        }
        onRencanaDeletedCallback?.invoke(event)
    }

    fun disconnect() {
        isManuallyClosed = true
        try {
            socketIo?.disconnect()
            socketIo?.off()
        } catch (e: Exception) {
            Log.e("RealtimeSocketManager", "Error disconnecting Socket.IO", e)
        } finally {
            socketIo = null
        }

        try {
            webSocket?.close(1000, "Normal Closure")
        } catch (e: Exception) {
            Log.e("RealtimeSocketManager", "Error while closing WebSocket", e)
        } finally {
            webSocket = null
            _isConnected.value = false
        }
    }
}
