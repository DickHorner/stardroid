/*
 * Copyright (c) 2026 Jasper Luetkens.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 */
package com.google.android.stardroid.pushnav;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Checks whether a configured LAN endpoint identifies itself as PushNav. */
public final class PushNavConnectionProbe {
  private static final int CONNECT_TIMEOUT_MS = 3000;
  private static final int READ_TIMEOUT_MS = 3000;

  private PushNavConnectionProbe() {}

  public static String normalizeServerUrl(String rawServerUrl) {
    String trimmed = rawServerUrl == null ? "" : rawServerUrl.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("PushNav server address is empty");
    }

    try {
      URI uri = new URI(trimmed);
      if (!"http".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
        throw new IllegalArgumentException(
            "PushNav server address must use http:// and include a host");
      }
      int port = uri.getPort();
      String normalized = "http://" + uri.getHost() + (port >= 0 ? ":" + port : "");
      return normalized;
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("PushNav server address is invalid", e);
    }
  }

  public static void verify(String rawServerUrl) throws IOException {
    String serverUrl = normalizeServerUrl(rawServerUrl);
    HttpURLConnection connection = (HttpURLConnection) new URL(serverUrl + "/api/version")
        .openConnection();
    connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
    connection.setReadTimeout(READ_TIMEOUT_MS);
    connection.setRequestMethod("GET");
    connection.setUseCaches(false);

    try {
      int responseCode = connection.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        throw new IOException("PushNav returned HTTP " + responseCode);
      }

      JSONObject response = new JSONObject(readBody(connection.getInputStream()));
      if (!"pushnav".equals(response.optString("app"))) {
        throw new IOException("Server did not identify itself as PushNav");
      }
    } catch (JSONException e) {
      throw new IOException("PushNav returned invalid JSON", e);
    } finally {
      connection.disconnect();
    }
  }

  private static String readBody(InputStream inputStream) throws IOException {
    StringBuilder body = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        body.append(line);
      }
    }
    return body.toString();
  }
}
