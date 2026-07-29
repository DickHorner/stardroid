package com.google.android.stardroid.pushnav

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PushNavNavigationTextViewTest {
  @Test
  fun toWebSocketUri_usesConfiguredHostAndPort() {
    assertEquals(
      "ws://192.168.178.50:8765/ws",
      PushNavNavigationTextView.toWebSocketUri(
        "http://192.168.178.50:8765"
      ).toString()
    )
  }

  @Test
  fun parseNavigationState_formatsTextAndAngle() {
    val previousLocale = Locale.getDefault()
    try {
      Locale.setDefault(Locale.US)
      val payload = """{"nav":{"active":true,"separation_deg":12.34,"direction_text":"up-left","camera_angle_deg":315.0}}"""

      val state = PushNavNavigationTextView.parseNavigationState(payload)

      assertEquals("PushNav: 12.3° · up-left", state?.text)
      assertEquals(315.0f, state?.cameraAngleDeg ?: Float.NaN, 0.001f)
    } finally {
      Locale.setDefault(previousLocale)
    }
  }

  @Test
  fun parseNavigationState_normalizesNegativeAngle() {
    val payload = """{"nav":{"active":true,"separation_deg":2.0,"direction_text":"up-left","camera_angle_deg":-45.0}}"""

    val state = PushNavNavigationTextView.parseNavigationState(payload)

    assertEquals(315.0f, state?.cameraAngleDeg ?: Float.NaN, 0.001f)
  }

  @Test
  fun parseNavigationState_ignoresMissingTarget() {
    assertNull(PushNavNavigationTextView.parseNavigationState("""{"nav":null}"""))
  }

  @Test
  fun parseNavigationState_ignoresMissingCameraAngle() {
    val payload = """{"nav":{"active":true,"separation_deg":12.0,"direction_text":"left"}}"""

    assertNull(PushNavNavigationTextView.parseNavigationState(payload))
  }

  @Test
  fun parseNavigationState_ignoresInvalidSeparation() {
    val payload = """{"nav":{"active":true,"separation_deg":181,"direction_text":"left","camera_angle_deg":270.0}}"""

    assertNull(PushNavNavigationTextView.parseNavigationState(payload))
  }

  @Test
  fun normalizeAngle_wrapsFullTurns() {
    assertEquals(5.0f, PushNavArrowDrawable.normalizeAngle(365.0f), 0.001f)
    assertEquals(355.0f, PushNavArrowDrawable.normalizeAngle(-5.0f), 0.001f)
  }
}
