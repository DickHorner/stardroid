/*
 * Copyright (c) 2026 Jasper Luetkens.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 */
package com.google.android.stardroid.pushnav;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.google.android.stardroid.ApplicationConstants;
import com.google.android.stardroid.R;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/** Displays PushNav's calibrated live direction while the search bar is visible. */
public class PushNavNavigationTextView extends TextView {
  private static final String TAG = "PushNavNavigationView";
  private static final long RECONNECT_DELAY_MS = 2000;

  static final class NavigationState {
    final String text;
    final float cameraAngleDeg;

    NavigationState(String text, float cameraAngleDeg) {
      this.text = text;
      this.cameraAngleDeg = cameraAngleDeg;
    }
  }

  private volatile WebSocketClient client;
  private volatile boolean navigationVisible;
  private String lastText;
  private PushNavArrowDrawable arrowDrawable;
  private final Runnable reconnect = this::connect;

  public PushNavNavigationTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public void onVisibilityAggregated(boolean isVisible) {
    super.onVisibilityAggregated(isVisible);
    navigationVisible = isVisible;
    if (isVisible) {
      connect();
    } else {
      disconnect();
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    navigationVisible = false;
    disconnect();
    super.onDetachedFromWindow();
  }

  private void connect() {
    removeCallbacks(reconnect);
    if (!navigationVisible || client != null) {
      return;
    }

    String configuredUrl = PreferenceManager.getDefaultSharedPreferences(getContext())
        .getString(ApplicationConstants.PUSHNAV_SERVER_URL_PREF_KEY, "");
    if (configuredUrl == null || configuredUrl.trim().isEmpty()) {
      updateText(getContext().getString(R.string.pushnav_navigation_not_configured));
      return;
    }

    try {
      URI websocketUri = toWebSocketUri(configuredUrl);
      WebSocketClient newClient = new WebSocketClient(websocketUri) {
        @Override
        public void onOpen(ServerHandshake handshake) {
          updateText(getContext().getString(R.string.pushnav_navigation_waiting));
        }

        @Override
        public void onMessage(String message) {
          NavigationState state = parseNavigationState(message);
          if (state != null) {
            updateNavigation(state);
          }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
          if (client == this) {
            client = null;
            scheduleReconnect();
          }
        }

        @Override
        public void onError(Exception exception) {
          Log.w(TAG, "PushNav WebSocket error", exception);
        }
      };
      newClient.setConnectionLostTimeout(5);
      client = newClient;
      newClient.connect();
    } catch (IllegalArgumentException | URISyntaxException e) {
      Log.w(TAG, "Invalid PushNav WebSocket address", e);
      client = null;
      updateText(getContext().getString(R.string.pushnav_navigation_invalid_address));
    }
  }

  private void disconnect() {
    removeCallbacks(reconnect);
    WebSocketClient activeClient = client;
    client = null;
    if (activeClient != null) {
      activeClient.close();
    }
  }

  private void scheduleReconnect() {
    if (navigationVisible) {
      postDelayed(reconnect, RECONNECT_DELAY_MS);
    }
  }

  private void updateText(String text) {
    post(() -> {
      clearArrow();
      if (!text.equals(lastText)) {
        lastText = text;
        setText(text);
      }
    });
  }

  private void updateNavigation(NavigationState state) {
    post(() -> {
      if (arrowDrawable == null) {
        float density = getResources().getDisplayMetrics().density;
        int sizePx = Math.round(32.0f * density);
        arrowDrawable = new PushNavArrowDrawable(getCurrentTextColor(), 2.5f * density);
        arrowDrawable.setBounds(0, 0, sizePx, sizePx);
        setCompoundDrawablePadding(Math.round(8.0f * density));
        setCompoundDrawablesRelative(arrowDrawable, null, null, null);
      }
      arrowDrawable.setAngleDeg(state.cameraAngleDeg);
      if (!state.text.equals(lastText)) {
        lastText = state.text;
        setText(state.text);
      }
    });
  }

  private void clearArrow() {
    if (arrowDrawable != null) {
      arrowDrawable = null;
      setCompoundDrawablesRelative(null, null, null, null);
    }
  }

  static URI toWebSocketUri(String rawServerUrl) throws URISyntaxException {
    String normalized = PushNavConnectionProbe.normalizeServerUrl(rawServerUrl);
    URI httpUri = new URI(normalized);
    return new URI("ws", null, httpUri.getHost(), httpUri.getPort(), "/ws", null, null);
  }

  static NavigationState parseNavigationState(String message) {
    try {
      JSONObject nav = new JSONObject(message).optJSONObject("nav");
      if (nav == null || !nav.optBoolean("active", false)
          || nav.isNull("separation_deg") || nav.isNull("camera_angle_deg")) {
        return null;
      }

      double separation = nav.getDouble("separation_deg");
      double cameraAngle = nav.getDouble("camera_angle_deg");
      if (!Double.isFinite(separation) || separation < 0.0 || separation > 180.0
          || !Double.isFinite(cameraAngle)) {
        return null;
      }

      String direction = nav.optString("direction_text", "").trim();
      if (direction.isEmpty()) {
        return null;
      }
      if (direction.length() > 80) {
        direction = direction.substring(0, 80);
      }

      String text = String.format(
          Locale.getDefault(), "PushNav: %.1f° · %s", separation, direction);
      return new NavigationState(text, PushNavArrowDrawable.normalizeAngle((float) cameraAngle));
    } catch (JSONException e) {
      Log.w(TAG, "Ignoring invalid PushNav navigation payload", e);
      return null;
    }
  }

  static String parseNavigationText(String message) {
    NavigationState state = parseNavigationState(message);
    return state == null ? null : state.text;
  }
}
