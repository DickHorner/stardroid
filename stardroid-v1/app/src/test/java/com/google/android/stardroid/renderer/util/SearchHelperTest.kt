package com.google.android.stardroid.renderer.util

import com.google.android.stardroid.math.Matrix4x4
import com.google.android.stardroid.math.Vector3
import kotlin.math.cos
import kotlin.math.sin
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchHelperTest {
    @Test
    fun centeredUsesTenthDegreeThreshold() {
        val helper = SearchHelper()
        helper.setTarget(Vector3(1.0f, 0.0f, 0.0f), "target")

        helper.setLookDirection(vectorAtAngleDegrees(0.09))
        helper.checkState()
        assertTrue(helper.targetInFocusRadius())

        helper.setLookDirection(vectorAtAngleDegrees(0.16))
        helper.checkState()
        assertFalse(helper.targetInFocusRadius())
    }

    @Test
    fun centeredStateHasSmallExitHysteresis() {
        val helper = SearchHelper()
        helper.setTarget(Vector3(1.0f, 0.0f, 0.0f), "target")

        helper.setLookDirection(vectorAtAngleDegrees(0.09))
        helper.checkState()
        assertTrue(helper.targetInFocusRadius())

        helper.setLookDirection(vectorAtAngleDegrees(0.14))
        helper.checkState()
        assertTrue(helper.targetInFocusRadius())

        helper.setLookDirection(vectorAtAngleDegrees(0.16))
        helper.checkState()
        assertFalse(helper.targetInFocusRadius())
    }

    @Test
    fun targetOnScreenRequiresFrontFacingPositionInsideMargins() {
        val helper = SearchHelper()
        helper.setTransform(Matrix4x4.createIdentity())

        helper.setTarget(Vector3(0.2f, 0.1f, 1.0f), "visible")
        assertTrue(helper.isTargetOnScreen)

        helper.setTarget(Vector3(10.0f, 0.0f, 1.0f), "outside")
        assertFalse(helper.isTargetOnScreen)

        helper.setTarget(Vector3(0.0f, 0.0f, -1.0f), "behind")
        assertFalse(helper.isTargetOnScreen)
    }

    private fun vectorAtAngleDegrees(degrees: Double): Vector3 {
        val radians = Math.toRadians(degrees)
        return Vector3(
            cos(radians).toFloat(),
            sin(radians).toFloat(),
            0.0f,
        )
    }
}
