package com.google.android.stardroid.observing

import androidx.preference.PreferenceManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ObservationListStoreTest {
    private val preferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.getApplication())
    }
    private lateinit var store: ObservationListStore

    @Before
    fun setUp() {
        preferences.edit().clear().commit()
        store = ObservationListStore(preferences)
    }

    @After
    fun tearDown() {
        preferences.edit().clear().commit()
    }

    @Test
    fun add_preservesInsertionOrder() {
        assertEquals(ObservationListStore.AddResult.ADDED, store.add("M13"))
        assertEquals(ObservationListStore.AddResult.ADDED, store.add("M29"))
        assertEquals(ObservationListStore.AddResult.ADDED, store.add("M52"))

        assertEquals(listOf("M13", "M29", "M52"), store.entries())
    }

    @Test
    fun add_rejectsCaseInsensitiveDuplicates() {
        assertEquals(ObservationListStore.AddResult.ADDED, store.add("M13"))

        assertEquals(
            ObservationListStore.AddResult.ALREADY_PRESENT,
            store.add(" m13 "),
        )
        assertEquals(listOf("M13"), store.entries())
    }

    @Test
    fun clear_removesEveryEntry() {
        store.add("M13")
        store.add("M29")

        store.clear()

        assertEquals(emptyList<String>(), store.entries())
    }
}
