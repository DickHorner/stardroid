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
  public void parseNavigationText_formatsActiveNavigation() {
    String payload = "{\"nav\":{\"active\":true,\"separation_deg\":12.34,"
        + "\"direction_text\":\"up-left\"}}";

    assertEquals("PushNav: 12.3° · up-left",
        PushNavNavigationTextView.parseNavigationText(payload));
  }

  @Test
  public void parseNavigationText_ignoresMissingTarget() {
    assertNull(PushNavNavigationTextView.parseNavigationText("{\"nav\":null}"));
  }

  @Test
  public void parseNavigationText_ignoresInvalidSeparation() {
    String payload = "{\"nav\":{\"active\":true,\"separation_deg\":181,"
        + "\"direction_text\":\"left\"}}";

    assertNull(PushNavNavigationTextView.parseNavigationText(payload));
  }
}
