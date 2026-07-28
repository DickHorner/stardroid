package com.google.android.stardroid.pushnav

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.google.android.stardroid.ApplicationConstants
import com.google.android.stardroid.R
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONException
import org.json.JSONObject
import java.net.URI
import java.net.URISyntaxException
import java.util.Locale

class PushNavNavigationTextView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : TextView(context, attrs) {
  data class NavigationState(
    val text: String,
    val cameraAngleDeg: Float
  )

  @Volatile
  private var client: WebSocketClient? = null

  @Volatile
  private var navigationVisible = false

  private var lastText: String? = null
  private var arrowDrawable: PushNavArrowDrawable? = null
  private val reconnect = Runnable { connect() }

  override fun onVisibilityAggregated(isVisible: Boolean) {
    super.onVisibilityAggregated(isVisible)
    navigationVisible = isVisible
    if (isVisible) {
      connect()
    } else {
      disconnect()
    }
  }

  override fun onDetachedFromWindow() {
    navigationVisible = false
    disconnect()
    super.onDetachedFromWindow()
  }

  private fun connect() {
    removeCallbacks(reconnect)
    if (!navigationVisible || client != null) {
      return
    }

    val configuredUrl = PreferenceManager.getDefaultSharedPreferences(context)
      .getString(ApplicationConstants.PUSHNAV_SERVER_URL_PREF_KEY, "")
    if (configuredUrl.isNullOrBlank()) {
      updateText(context.getString(R.string.pushnav_navigation_not_configured))
      return
    }

    try {
      val websocketUri = toWebSocketUri(configuredUrl)
      val newClient = object : WebSocketClient(websocketUri) {
        override fun onOpen(handshake: ServerHandshake) {
          updateText(context.getString(R.string.pushnav_navigation_waiting))
        }

        override fun onMessage(message: String) {
          parseNavigationState(message)?.let(::updateNavigation)
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
    updateText(context.getString(R.string.pushnav_navigation_invalid_address))
  }

  private fun disconnect() {
    removeCallbacks(reconnect)
    val activeClient = client
    client = null
    activeClient?.close()
  }

  private fun scheduleReconnect() {
    if (navigationVisible) {
      postDelayed(reconnect, RECONNECT_DELAY_MS)
    }
  }

  private fun updateText(text: String) {
    post {
      clearArrow()
      if (text != lastText) {
        lastText = text
        setText(text)
      }
    }
  }

  private fun updateNavigation(state: NavigationState) {
    post {
      val arrow = arrowDrawable ?: createArrow().also { arrowDrawable = it }
      arrow.setAngleDeg(state.cameraAngleDeg)
      if (state.text != lastText) {
        lastText = state.text
        text = state.text
      }
    }
  }

  private fun createArrow(): PushNavArrowDrawable {
    val density = resources.displayMetrics.density
    val sizePx = (32.0f * density).toInt()
    return PushNavArrowDrawable(currentTextColor, 2.5f * density).also { arrow ->
      arrow.setBounds(0, 0, sizePx, sizePx)
      compoundDrawablePadding = (8.0f * density).toInt()
      setCompoundDrawablesRelative(arrow, null, null, null)
    }
  }

  private fun clearArrow() {
    if (arrowDrawable != null) {
      arrowDrawable = null
      setCompoundDrawablesRelative(null, null, null, null)
    }
  }

  companion object {
    private const val TAG = "PushNavNavigationView"
    private const val RECONNECT_DELAY_MS = 2000L

    @JvmStatic
    @Throws(URISyntaxException::class)
    fun toWebSocketUri(rawServerUrl: String?): URI {
      val normalized = PushNavConnectionProbe.normalizeServerUrl(rawServerUrl)
      val httpUri = URI(normalized)
      return URI("ws", null, httpUri.host, httpUri.port, "/ws", null, null)
    }

    @JvmStatic
    fun parseNavigationState(message: String): NavigationState? {
      try {
        val nav = JSONObject(message).optJSONObject("nav") ?: return null
        if (!nav.optBoolean("active", false) || nav.isNull("separation_deg") ||
          nav.isNull("camera_angle_deg")) {
          return null
        }

        val separation = nav.getDouble("separation_deg")
        val cameraAngle = nav.getDouble("camera_angle_deg")
        if (!separation.isFinite() || separation !in 0.0..180.0 || !cameraAngle.isFinite()) {
          return null
        }

        val direction = nav.optString("direction_text", "").trim().take(80)
        if (direction.isEmpty()) {
          return null
        }

        val text = String.format(
          Locale.getDefault(),
          "PushNav: %.1f° · %s",
          separation,
          direction
        )
        return NavigationState(
          text,
          PushNavArrowDrawable.normalizeAngle(cameraAngle.toFloat())
        )
      } catch (exception: JSONException) {
        Log.w(TAG, "Ignoring invalid PushNav navigation payload", exception)
        return null
      }
    }

    @JvmStatic
    fun parseNavigationText(message: String): String? = parseNavigationState(message)?.text
  }
}
