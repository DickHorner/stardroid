package com.google.android.stardroid.observing

import android.content.SharedPreferences
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ObservationListEntryPoint {
    fun observationListStore(): ObservationListStore
    fun sharedPreferences(): SharedPreferences
}
