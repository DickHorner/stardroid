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

import org.json.JSONObject;
import org.junit.Test;

public class PushNavTargetSenderTest {
  @Test
  public void createRequestBody_containsCoordinates() throws Exception {
    JSONObject body = new JSONObject(PushNavTargetSender.createRequestBody(10.6847f, 41.269f));

    assertEquals(10.6847, body.getDouble("ra_deg"), 0.0001);
    assertEquals(41.269, body.getDouble("dec_deg"), 0.0001);
  }

  @Test
  public void createRequestBody_acceptsCoordinateBoundaries() {
    PushNavTargetSender.createRequestBody(0.0f, -90.0f);
    PushNavTargetSender.createRequestBody(359.999f, 90.0f);
  }

  @Test
  public void createRequestBody_rejectsRightAscensionOutsideRange() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PushNavTargetSender.createRequestBody(360.0f, 0.0f));
  }

  @Test
  public void createRequestBody_rejectsDeclinationOutsideRange() {
    assertThrows(
        IllegalArgumentException.class,
        () -> PushNavTargetSender.createRequestBody(0.0f, 90.1f));
  }
}
