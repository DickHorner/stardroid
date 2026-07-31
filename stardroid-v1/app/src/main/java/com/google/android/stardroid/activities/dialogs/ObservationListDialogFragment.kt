package com.google.android.stardroid.activities.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.app.SearchManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.android.stardroid.R
import com.google.android.stardroid.activities.DynamicStarMapActivity
import com.google.android.stardroid.activities.util.ActivityLightLevelManager
import com.google.android.stardroid.activities.util.NightModeHelper
import com.google.android.stardroid.observing.ObservationListStore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ObservationListDialogFragment : DialogFragment() {
    @Inject
    lateinit var observationListStore: ObservationListStore

    @Inject
    lateinit var sharedPreferences: android.content.SharedPreferences

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val entries = observationListStore.entries()
        val isNight = ActivityLightLevelManager.isNightMode(sharedPreferences)
        val builder = AlertDialog.Builder(requireActivity())
            .setTitle(R.string.observation_list_title)
            .setNegativeButton(android.R.string.cancel, null)

        if (entries.isEmpty()) {
            builder.setMessage(R.string.observation_list_empty)
        } else {
            builder.setItems(entries.toTypedArray()) { dialog, index ->
                dialog.dismiss()
                openTarget(entries[index])
            }
            builder.setNeutralButton(R.string.observation_list_clear, null)
        }

        val dialog = builder.create()
        dialog.setOnShowListener {
            NightModeHelper.applyAlertDialogNightMode(dialog, isNight)
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setOnClickListener {
                showClearConfirmation(dialog, isNight)
            }
        }
        return dialog
    }

    private fun openTarget(targetName: String) {
        val intent = Intent(requireContext(), DynamicStarMapActivity::class.java)
            .setAction(Intent.ACTION_SEARCH)
            .putExtra(SearchManager.QUERY, targetName)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    private fun showClearConfirmation(parentDialog: AlertDialog, isNight: Boolean) {
        val confirmation = AlertDialog.Builder(requireActivity())
            .setTitle(R.string.observation_list_clear_confirm_title)
            .setMessage(R.string.observation_list_clear_confirm_message)
            .setNegativeButton(android.R.string.no, null)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                observationListStore.clear()
                parentDialog.dismiss()
                Toast.makeText(
                    requireContext(),
                    R.string.observation_list_cleared,
                    Toast.LENGTH_SHORT,
                ).show()
            }
            .create()
        confirmation.setOnShowListener {
            NightModeHelper.applyAlertDialogNightMode(confirmation, isNight)
        }
        confirmation.show()
    }

    companion object {
        private const val TAG = "ObservationListDialog"

        @JvmStatic
        fun newInstance(): ObservationListDialogFragment = ObservationListDialogFragment()

        fun show(context: Context): Boolean {
            val activity = context.findFragmentActivity() ?: return false
            if (activity.supportFragmentManager.findFragmentByTag(TAG) == null) {
                newInstance().show(activity.supportFragmentManager, TAG)
            }
            return true
        }

        private fun Context.findFragmentActivity(): FragmentActivity? {
            var current: Context? = this
            while (current is ContextWrapper) {
                if (current is FragmentActivity) {
                    return current
                }
                current = current.baseContext
            }
            return current as? FragmentActivity
        }
    }
}
