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
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class PushNavConnectionProbeTest {
  @Test
  public void normalizeServerUrl_trimsWhitespaceAndPath() {
    assertEquals(
        "http://192.168.178.50:8765",
        PushNavConnectionProbe.normalizeServerUrl(
            "  http://192.168.178.50:8765/api/version  "));
  }

  @Test
  public void normalizeServerUrl_acceptsHostnameWithoutPort() {
    assertEquals(
        "http://pushnav.local",
        PushNavConnectionProbe.normalizeServerUrl("http://pushnav.local"));
  }

  @Test
  public void normalizeServerUrl_rejectsHttps() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PushNavConnectionProbe.normalizeServerUrl("https://pushnav.local:8765"));
  }

  @Test
  public void normalizeServerUrl_rejectsBlankValue() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PushNavConnectionProbe.normalizeServerUrl("   "));
  }
}
