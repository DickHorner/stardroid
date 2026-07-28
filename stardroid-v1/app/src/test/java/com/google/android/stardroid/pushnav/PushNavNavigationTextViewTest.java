/*
 * Copyright (c) 2026 Jasper Luetkens.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 */
package com.google.android.stardroid.pushnav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class PushNavNavigationTextViewTest {
  @Test
  public void toWebSocketUri_usesConfiguredHostAndPort() throws Exception {
    assertEquals(
        "ws://192.168.178.50:8765/ws",
        PushNavNavigationTextView.toWebSocketUri("http://192.168.178.50:8765").toString());
  }

  @Test
  public void parseNavigationState_formatsTextAndAngle() {
    String payload = "{\"nav\":{\"active\":true,\"separation_deg\":12.34,"
        + "\"direction_text\":\"up-left\",\"camera_angle_deg\":315.0}}";

    PushNavNavigationTextView.NavigationState state =
        PushNavNavigationTextView.parseNavigationState(payload);

    assertEquals("PushNav: 12.3° · up-left", state.text);
    assertEquals(315.0f, state.cameraAngleDeg, 0.001f);
  }

  @Test
  public void parseNavigationState_normalizesNegativeAngle() {
    String payload = "{\"nav\":{\"active\":true,\"separation_deg\":2.0,"
        + "\"direction_text\":\"up-left\",\"camera_angle_deg\":-45.0}}";

    PushNavNavigationTextView.NavigationState state =
        PushNavNavigationTextView.parseNavigationState(payload);

    assertEquals(315.0f, state.cameraAngleDeg, 0.001f);
  }

  @Test
  public void parseNavigationState_ignoresMissingTarget() {
    assertNull(PushNavNavigationTextView.parseNavigationState("{\"nav\":null}"));
  }

  @Test
  public void parseNavigationState_ignoresMissingCameraAngle() {
    String payload = "{\"nav\":{\"active\":true,\"separation_deg\":12.0,"
        + "\"direction_text\":\"left\"}}";

    assertNull(PushNavNavigationTextView.parseNavigationState(payload));
  }

  @Test
  public void parseNavigationState_ignoresInvalidSeparation() {
    String payload = "{\"nav\":{\"active\":true,\"separation_deg\":181,"
        + "\"direction_text\":\"left\",\"camera_angle_deg\":270.0}}";

    assertNull(PushNavNavigationTextView.parseNavigationState(payload));
  }

  @Test
  public void normalizeAngle_wrapsFullTurns() {
    assertEquals(5.0f, PushNavArrowDrawable.normalizeAngle(365.0f), 0.001f);
    assertEquals(355.0f, PushNavArrowDrawable.normalizeAngle(-5.0f), 0.001f);
  }
}
