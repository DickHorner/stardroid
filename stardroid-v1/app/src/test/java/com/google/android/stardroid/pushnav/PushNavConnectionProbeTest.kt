package com.google.android.stardroid.pushnav

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PushNavConnectionProbeTest {
  @Test
  fun normalizeServerUrl_trimsWhitespaceAndPath() {
    assertEquals(
      "http://192.168.178.50:8765",
      PushNavConnectionProbe.normalizeServerUrl(
        "  http://192.168.178.50:8765/api/version  "
      )
    )
  }

  @Test
  fun normalizeServerUrl_acceptsHostnameWithoutPort() {
    assertEquals(
      "http://pushnav.local",
      PushNavConnectionProbe.normalizeServerUrl("http://pushnav.local")
    )
  }

  @Test
  fun normalizeServerUrl_rejectsHttps() {
    assertThrows(IllegalArgumentException::class.java) {
      PushNavConnectionProbe.normalizeServerUrl("https://pushnav.local:8765")
    }
  }

  @Test
  fun normalizeServerUrl_rejectsBlankValue() {
    assertThrows(IllegalArgumentException::class.java) {
      PushNavConnectionProbe.normalizeServerUrl("   ")
    }
  }
}
