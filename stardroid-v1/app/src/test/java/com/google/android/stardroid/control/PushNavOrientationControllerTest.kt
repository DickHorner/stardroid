package com.google.android.stardroid.control

import com.google.android.stardroid.math.Vector3
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PushNavOrientationControllerTest {
    @Test
    fun toWebSocketUri_usesConfiguredHostAndPort() {
        assertEquals(
            "ws://192.168.178.50:8765/ws",
            PushNavOrientationController.toWebSocketUri(
                "http://192.168.178.50:8765",
            ).toString(),
        )
    }

    @Test
    fun parsePointing_convertsRaDecAndZeroRoll() {
        val payload =
            """{"pointing":{"valid":true,"ra_deg":0.0,"dec_deg":0.0,"roll_deg":0.0}}"""

        val pointing = PushNavOrientationController.parsePointing(payload)

        assertVector(1.0f, 0.0f, 0.0f, pointing?.lineOfSight)
        assertVector(0.0f, 0.0f, 1.0f, pointing?.perpendicular)
    }

    @Test
    fun parsePointing_appliesRollEastOfNorth() {
        val payload =
            """{"pointing":{"valid":true,"ra_deg":0.0,"dec_deg":0.0,"roll_deg":90.0}}"""

        val pointing = PushNavOrientationController.parsePointing(payload)

        assertVector(1.0f, 0.0f, 0.0f, pointing?.lineOfSight)
        assertVector(0.0f, 1.0f, 0.0f, pointing?.perpendicular)
    }

    @Test
    fun parsePointing_ignoresUnsolvedPayload() {
        val payload =
            """{"pointing":{"valid":false,"ra_deg":0.0,"dec_deg":0.0,"roll_deg":0.0}}"""

        assertNull(PushNavOrientationController.parsePointing(payload))
    }

    @Test
    fun parsePointing_ignoresCoordinatesOutsideCelestialRanges() {
        assertNull(
            PushNavOrientationController.parsePointing(
                """{"pointing":{"valid":true,"ra_deg":360.0,"dec_deg":0.0,"roll_deg":0.0}}""",
            ),
        )
        assertNull(
            PushNavOrientationController.parsePointing(
                """{"pointing":{"valid":true,"ra_deg":0.0,"dec_deg":90.1,"roll_deg":0.0}}""",
            ),
        )
    }

    private fun assertVector(
        expectedX: Float,
        expectedY: Float,
        expectedZ: Float,
        actual: Vector3?,
    ) {
        assertEquals(expectedX, actual?.x ?: Float.NaN, 0.0001f)
        assertEquals(expectedY, actual?.y ?: Float.NaN, 0.0001f)
        assertEquals(expectedZ, actual?.z ?: Float.NaN, 0.0001f)
    }
}
