package com.google.android.stardroid.observing

import android.content.Context
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import com.google.android.stardroid.R
import com.google.android.stardroid.activities.util.ActivityLightLevelManager
import dagger.hilt.android.EntryPointAccessors

class ObservationListAddButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AppCompatImageButton(context, attrs), SharedPreferences.OnSharedPreferenceChangeListener {
    private val entryPoint: ObservationListEntryPoint by lazy {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            ObservationListEntryPoint::class.java,
        )
    }

    init {
        setOnClickListener { addCurrentTarget() }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        entryPoint.sharedPreferences().registerOnSharedPreferenceChangeListener(this)
        applyNightTint()
    }

    override fun onDetachedFromWindow() {
        entryPoint.sharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
        super.onDetachedFromWindow()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (key == ActivityLightLevelManager.LIGHT_MODE_KEY) {
            applyNightTint()
        }
    }

    private fun addCurrentTarget() {
        val result = entryPoint.observationListStore().add(CurrentSearchTarget.name())
        val message = when (result) {
            ObservationListStore.AddResult.ADDED -> R.string.observation_list_target_added
            ObservationListStore.AddResult.ALREADY_PRESENT -> R.string.observation_list_target_already_added
            ObservationListStore.AddResult.INVALID_NAME -> R.string.observation_list_no_active_target
            ObservationListStore.AddResult.LIST_FULL -> R.string.observation_list_full
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun applyNightTint() {
        if (ActivityLightLevelManager.isNightMode(entryPoint.sharedPreferences())) {
            setColorFilter(context.getColor(R.color.night_text_color), PorterDuff.Mode.MULTIPLY)
        } else {
            clearColorFilter()
        }
    }
}
