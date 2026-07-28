package com.google.android.stardroid.pushnav

import android.util.Log
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executor

object PushNavTargetSender {
  private const val TAG = "PushNavTargetSender"
  private const val CONNECT_TIMEOUT_MS = 3000
  private const val READ_TIMEOUT_MS = 3000

  @JvmStatic
  fun sendAsync(
    executor: Executor,
    rawServerUrl: String?,
    raDeg: Float,
    decDeg: Float
  ) {
    if (rawServerUrl.isNullOrBlank()) {
      return
    }
    executor.execute {
      try {
        send(rawServerUrl, raDeg, decDeg)
      } catch (exception: IllegalArgumentException) {
        Log.w(TAG, "Unable to send target to PushNav", exception)
      } catch (exception: IOException) {
        Log.w(TAG, "Unable to send target to PushNav", exception)
      }
    }
  }

  @JvmStatic
  @Throws(IOException::class)
  fun send(rawServerUrl: String?, raDeg: Float, decDeg: Float) {
    validateCoordinates(raDeg, decDeg)
    val serverUrl = PushNavConnectionProbe.normalizeServerUrl(rawServerUrl)
    val body = createRequestBody(raDeg, decDeg).toByteArray(Charsets.UTF_8)
    val connection = URL("$serverUrl/api/goto/set").openConnection() as HttpURLConnection
    connection.connectTimeout = CONNECT_TIMEOUT_MS
    connection.readTimeout = READ_TIMEOUT_MS
    connection.requestMethod = "POST"
    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
    connection.setFixedLengthStreamingMode(body.size)
    connection.doOutput = true
    connection.useCaches = false

    try {
      connection.outputStream.use { output -> output.write(body) }
      val responseCode = connection.responseCode
      if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
        throw IOException("PushNav returned HTTP $responseCode")
      }
    } finally {
      connection.disconnect()
    }
  }

  @JvmStatic
  fun createRequestBody(raDeg: Float, decDeg: Float): String {
    validateCoordinates(raDeg, decDeg)
    return JSONObject()
      .put("ra_deg", raDeg)
      .put("dec_deg", decDeg)
      .toString()
  }

  private fun validateCoordinates(raDeg: Float, decDeg: Float) {
    require(raDeg.isFinite() && raDeg >= 0.0f && raDeg < 360.0f) {
      "Right ascension must be in [0, 360)"
    }
    require(decDeg.isFinite() && decDeg >= -90.0f && decDeg <= 90.0f) {
      "Declination must be in [-90, 90]"
    }
  }
}
