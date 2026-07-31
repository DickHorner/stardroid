package com.google.android.stardroid.observing

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CurrentSearchTargetTest {
    @After
    fun tearDown() {
        CurrentSearchTarget.clear()
    }

    @Test
    fun update_trimsAndExposesTargetName() {
        CurrentSearchTarget.update("  M13  ")

        assertEquals("M13", CurrentSearchTarget.name())
    }

    @Test
    fun clear_removesCurrentTarget() {
        CurrentSearchTarget.update("M13")

        CurrentSearchTarget.clear()

        assertNull(CurrentSearchTarget.name())
    }
}
