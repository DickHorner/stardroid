package com.google.android.stardroid.observing

import android.content.Context
import android.view.ActionProvider
import android.view.LayoutInflater
import android.view.View
import com.google.android.stardroid.R
import com.google.android.stardroid.activities.dialogs.ObservationListDialogFragment

class ObservationListActionProvider(
    context: Context,
) : ActionProvider(context) {
    override fun onCreateActionView(): View {
        return LayoutInflater.from(context)
            .inflate(R.layout.observation_list_action_item, null)
    }

    override fun onPerformDefaultAction(): Boolean {
        return ObservationListDialogFragment.show(context)
    }
}
