/*
 * Copyright (c) 2026 Jasper Luetkens.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 */
package com.google.android.stardroid.pushnav;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Sends selected Sky Map targets to PushNav. */
public final class PushNavTargetSender {
  private static final String TAG = "PushNavTargetSender";
  private static final int CONNECT_TIMEOUT_MS = 3000;
  private static final int READ_TIMEOUT_MS = 3000;
  private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

  private PushNavTargetSender() {}

  public static void sendAsync(String rawServerUrl, float raDeg, float decDeg) {
    if (rawServerUrl == null || rawServerUrl.trim().isEmpty()) {
      return;
    }
    EXECUTOR.execute(() -> {
      try {
        send(rawServerUrl, raDeg, decDeg);
      } catch (IllegalArgumentException | IOException e) {
        Log.w(TAG, "Unable to send target to PushNav", e);
      }
    });
  }

  public static void send(String rawServerUrl, float raDeg, float decDeg) throws IOException {
    validateCoordinates(raDeg, decDeg);
    String serverUrl = PushNavConnectionProbe.normalizeServerUrl(rawServerUrl);
    byte[] body = createRequestBody(raDeg, decDeg).getBytes(StandardCharsets.UTF_8);

    HttpURLConnection connection = (HttpURLConnection) new URL(serverUrl + "/api/goto/set")
        .openConnection();
    connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
    connection.setReadTimeout(READ_TIMEOUT_MS);
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
    connection.setFixedLengthStreamingMode(body.length);
    connection.setDoOutput(true);
    connection.setUseCaches(false);

    try {
      try (OutputStream output = connection.getOutputStream()) {
        output.write(body);
      }
      int responseCode = connection.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
        throw new IOException("PushNav returned HTTP " + responseCode);
      }
    } finally {
      connection.disconnect();
    }
  }

  static String createRequestBody(float raDeg, float decDeg) {
    validateCoordinates(raDeg, decDeg);
    return new JSONObject()
        .put("ra_deg", raDeg)
        .put("dec_deg", decDeg)
        .toString();
  }

  private static void validateCoordinates(float raDeg, float decDeg) {
    if (!Float.isFinite(raDeg) || raDeg < 0.0f || raDeg >= 360.0f) {
      throw new IllegalArgumentException("Right ascension must be in [0, 360)");
    }
    if (!Float.isFinite(decDeg) || decDeg < -90.0f || decDeg > 90.0f) {
      throw new IllegalArgumentException("Declination must be in [-90, 90]");
    }
  }
}
