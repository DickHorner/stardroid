package com.google.android.stardroid.pushnav

import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URISyntaxException
import java.net.URL

object PushNavConnectionProbe {
  private const val CONNECT_TIMEOUT_MS = 3000
  private const val READ_TIMEOUT_MS = 3000

  @JvmStatic
  fun normalizeServerUrl(rawServerUrl: String?): String {
    val trimmed = rawServerUrl?.trim().orEmpty()
    require(trimmed.isNotEmpty()) { "PushNav server address is empty" }

    try {
      val uri = URI(trimmed)
      require(uri.scheme.equals("http", ignoreCase = true) && uri.host != null) {
        "PushNav server address must use http:// and include a host"
      }
      return buildString {
        append("http://")
        append(uri.host)
        if (uri.port >= 0) {
          append(':')
          append(uri.port)
        }
      }
    } catch (exception: URISyntaxException) {
      throw IllegalArgumentException("PushNav server address is invalid", exception)
    }
  }

  @JvmStatic
  @Throws(IOException::class)
  fun verify(rawServerUrl: String?) {
    val serverUrl = normalizeServerUrl(rawServerUrl)
    val connection = URL("$serverUrl/api/version").openConnection() as HttpURLConnection
    connection.connectTimeout = CONNECT_TIMEOUT_MS
    connection.readTimeout = READ_TIMEOUT_MS
    connection.requestMethod = "GET"
    connection.useCaches = false

    try {
      val responseCode = connection.responseCode
      if (responseCode != HttpURLConnection.HTTP_OK) {
        throw IOException("PushNav returned HTTP $responseCode")
      }

      val response = connection.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
        JSONObject(reader.readText())
      }
      if (response.optString("app") != "pushnav") {
        throw IOException("Server did not identify itself as PushNav")
      }
    } catch (exception: JSONException) {
      throw IOException("PushNav returned invalid JSON", exception)
    } finally {
      connection.disconnect()
    }
  }
}
