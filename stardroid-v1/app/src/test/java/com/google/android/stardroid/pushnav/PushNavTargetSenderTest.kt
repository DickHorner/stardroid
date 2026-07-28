package com.google.android.stardroid.pushnav

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PushNavTargetSenderTest {
  @Test
  fun createRequestBody_containsCoordinates() {
    val body = JSONObject(PushNavTargetSender.createRequestBody(10.6847f, 41.269f))

    assertEquals(10.6847, body.getDouble("ra_deg"), 0.0001)
    assertEquals(41.269, body.getDouble("dec_deg"), 0.0001)
  }

  @Test
  fun createRequestBody_acceptsCoordinateBoundaries() {
    PushNavTargetSender.createRequestBody(0.0f, -90.0f)
    PushNavTargetSender.createRequestBody(359.999f, 90.0f)
  }

  @Test
  fun createRequestBody_rejectsRightAscensionOutsideRange() {
    assertThrows(IllegalArgumentException::class.java) {
      PushNavTargetSender.createRequestBody(360.0f, 0.0f)
    }
  }

  @Test
  fun createRequestBody_rejectsDeclinationOutsideRange() {
    assertThrows(IllegalArgumentException::class.java) {
      PushNavTargetSender.createRequestBody(0.0f, 90.1f)
    }
  }
}
