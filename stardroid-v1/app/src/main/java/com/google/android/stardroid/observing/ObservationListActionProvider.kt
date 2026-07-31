package com.google.android.stardroid.observing

import android.content.Context
import android.view.ActionProvider
import android.view.LayoutInflater
import android.view.View
import com.google.android.stardroid.R
import com.google.android.stardroid.activities.dialogs.ObservationListDialogFragment

class ObservationListActionProvider(
    private val providerContext: Context,
) : ActionProvider(providerContext) {
    override fun onCreateActionView(): View {
        return LayoutInflater.from(providerContext)
            .inflate(R.layout.observation_list_action_item, null)
    }

    override fun onPerformDefaultAction(): Boolean {
        return ObservationListDialogFragment.show(providerContext)
    }
}
