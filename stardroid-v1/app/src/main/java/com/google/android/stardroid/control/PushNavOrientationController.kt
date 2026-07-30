package com.google.android.stardroid.control

import android.content.SharedPreferences
import android.os.Handler
import android.util.Log
import com.google.android.stardroid.ApplicationConstants
import com.google.android.stardroid.math.Vector3
import com.google.android.stardroid.pushnav.PushNavConnectionProbe
import com.google.android.stardroid.util.MiscUtil
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONException
import org.json.JSONObject
import java.net.URI
import java.net.URISyntaxException
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin

class PushNavOrientationController @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val handler: Handler,
) : AbstractController() {
    @Volatile
    private var client: WebSocketClient? = null

    private var started = false
    private val reconnect = Runnable { connect() }

    fun isConfigured(): Boolean = configuredServerUrl().isNotBlank()

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (!started) {
            return
        }
        handler.removeCallbacks(reconnect)
        if (enabled) {
            connect()
        } else {
            disconnect()
        }
    }

    override fun start() {
        started = true
        if (enabled) {
            connect()
        }
    }

    override fun stop() {
        started = false
        handler.removeCallbacks(reconnect)
        disconnect()
    }

    private fun connect() {
        handler.removeCallbacks(reconnect)
        if (!started || !enabled || client != null) {
            return
        }

        val serverUrl = configuredServerUrl()
        if (serverUrl.isBlank()) {
            return
        }

        try {
            val newClient = object : WebSocketClient(toWebSocketUri(serverUrl)) {
                override fun onOpen(handshake: ServerHandshake) = Unit

                override fun onMessage(message: String) {
                    val pointing = parsePointing(message) ?: return
                    handler.post {
                        if (started && enabled) {
                            model.setPointing(pointing.lineOfSight, pointing.perpendicular)
                        }
                    }
                }

                override fun onClose(code: Int, reason: String, remote: Boolean) {
                    if (client === this) {
                        client = null
                        scheduleReconnect()
                    }
                }

                override fun onError(exception: Exception) {
                    Log.w(TAG, "PushNav WebSocket error", exception)
                }
            }
            newClient.setConnectionLostTimeout(5)
            client = newClient
            newClient.connect()
        } catch (exception: IllegalArgumentException) {
            handleInvalidAddress(exception)
        } catch (exception: URISyntaxException) {
            handleInvalidAddress(exception)
        }
    }

    private fun handleInvalidAddress(exception: Exception) {
        Log.w(TAG, "Invalid PushNav WebSocket address", exception)
        client = null
    }

    private fun disconnect() {
        val activeClient = client
        client = null
        activeClient?.close()
    }

    private fun scheduleReconnect() {
        if (started && enabled) {
            handler.postDelayed(reconnect, RECONNECT_DELAY_MS)
        }
    }

    private fun configuredServerUrl(): String = sharedPreferences.getString(
        ApplicationConstants.PUSHNAV_SERVER_URL_PREF_KEY,
        "",
    ).orEmpty()

    data class PushNavPointing(
        val lineOfSight: Vector3,
        val perpendicular: Vector3,
    )

    companion object {
        private val TAG = MiscUtil.getTag(PushNavOrientationController::class.java)
        private const val RECONNECT_DELAY_MS = 2_000L

        @JvmStatic
        @Throws(URISyntaxException::class)
        fun toWebSocketUri(rawServerUrl: String?): URI {
            val normalized = PushNavConnectionProbe.normalizeServerUrl(rawServerUrl)
            val httpUri = URI(normalized)
            return URI("ws", null, httpUri.host, httpUri.port, "/ws", null, null)
        }

        @JvmStatic
        fun parsePointing(message: String): PushNavPointing? {
            try {
                val pointing = JSONObject(message).optJSONObject("pointing") ?: return null
                if (!pointing.optBoolean("valid", false)) {
                    return null
                }

                val raDeg = pointing.getDouble("ra_deg")
                val decDeg = pointing.getDouble("dec_deg")
                val rollDeg = pointing.getDouble("roll_deg")
                if (!raDeg.isFinite() || raDeg !in 0.0..<360.0 ||
                    !decDeg.isFinite() || decDeg !in -90.0..90.0 ||
                    !rollDeg.isFinite()) {
                    return null
                }

                val ra = Math.toRadians(raDeg)
                val dec = Math.toRadians(decDeg)
                val roll = Math.toRadians(rollDeg)
                val cosDec = cos(dec)
                val sinDec = sin(dec)
                val cosRa = cos(ra)
                val sinRa = sin(ra)

                val lineOfSight = Vector3(
                    (cosDec * cosRa).toFloat(),
                    (cosDec * sinRa).toFloat(),
                    sinDec.toFloat(),
                )
                val north = Vector3(
                    (-sinDec * cosRa).toFloat(),
                    (-sinDec * sinRa).toFloat(),
                    cosDec.toFloat(),
                )
                val east = Vector3(
                    (-sinRa).toFloat(),
                    cosRa.toFloat(),
                    0.0f,
                )
                val perpendicular = north * cos(roll).toFloat() + east * sin(roll).toFloat()
                perpendicular.normalize()

                return PushNavPointing(lineOfSight, perpendicular)
            } catch (exception: JSONException) {
                Log.w(TAG, "Ignoring invalid PushNav pointing payload", exception)
                return null
            }
        }
    }
}
