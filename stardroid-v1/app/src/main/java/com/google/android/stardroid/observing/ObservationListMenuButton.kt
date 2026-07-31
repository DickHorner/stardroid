package com.google.android.stardroid.observing

import android.content.Context
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import com.google.android.stardroid.R
import com.google.android.stardroid.activities.dialogs.ObservationListDialogFragment
import com.google.android.stardroid.activities.util.ActivityLightLevelManager
import dagger.hilt.android.EntryPointAccessors

class ObservationListMenuButton @JvmOverloads constructor(
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
        setOnClickListener { ObservationListDialogFragment.show(context) }
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

    private fun applyNightTint() {
        if (ActivityLightLevelManager.isNightMode(entryPoint.sharedPreferences())) {
            setColorFilter(context.getColor(R.color.night_text_color), PorterDuff.Mode.MULTIPLY)
        } else {
            clearColorFilter()
        }
    }
}
