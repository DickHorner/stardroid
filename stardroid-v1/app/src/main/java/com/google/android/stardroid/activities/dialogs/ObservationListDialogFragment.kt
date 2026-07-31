package com.google.android.stardroid.activities.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.app.SearchManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
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
        val isNight = ActivityLightLevelManager.isNightMode(sharedPreferences)
        val contentView = requireActivity().layoutInflater.inflate(
            R.layout.observation_list_dialog,
            null,
        )
        val listView = contentView.findViewById<ListView>(R.id.observation_list_items)
        val emptyView = contentView.findViewById<TextView>(R.id.observation_list_empty_message)
        if (isNight) {
            emptyView.setTextColor(requireContext().getColor(R.color.night_text_color))
        }
        listView.emptyView = emptyView

        lateinit var dialog: AlertDialog
        lateinit var adapter: ObservationListAdapter
        adapter = ObservationListAdapter(
            requireContext(),
            observationListStore.entries().toMutableList(),
            isNight,
        ) { targetName ->
            if (observationListStore.remove(targetName)) {
                adapter.remove(targetName)
                updateClearButton(dialog, adapter.count)
            }
        }
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            adapter.getItem(position)?.let { targetName ->
                dismiss()
                openTarget(targetName)
            }
        }

        dialog = AlertDialog.Builder(requireActivity())
            .setTitle(R.string.observation_list_title)
            .setView(contentView)
            .setNegativeButton(android.R.string.cancel, null)
            .setNeutralButton(R.string.observation_list_clear, null)
            .create()
        dialog.setOnShowListener {
            NightModeHelper.applyAlertDialogNightMode(dialog, isNight)
            updateClearButton(dialog, adapter.count)
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                showClearConfirmation(isNight)
            }
        }
        return dialog
    }

    private fun updateClearButton(dialog: AlertDialog, itemCount: Int) {
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.visibility =
            if (itemCount > 0) View.VISIBLE else View.GONE
    }

    private fun openTarget(targetName: String) {
        val intent = Intent(requireContext(), DynamicStarMapActivity::class.java)
            .setAction(Intent.ACTION_SEARCH)
            .putExtra(SearchManager.QUERY, targetName)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    private fun showClearConfirmation(isNight: Boolean) {
        val confirmation = AlertDialog.Builder(requireActivity())
            .setTitle(R.string.observation_list_clear_confirm_title)
            .setMessage(R.string.observation_list_clear_confirm_message)
            .setNegativeButton(android.R.string.no, null)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                observationListStore.clear()
                dismiss()
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

    private class ObservationListAdapter(
        context: Context,
        entries: MutableList<String>,
        private val isNight: Boolean,
        private val onRemove: (String) -> Unit,
    ) : ArrayAdapter<String>(context, R.layout.observation_list_item, entries) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(
                R.layout.observation_list_item,
                parent,
                false,
            )
            val targetName = getItem(position).orEmpty()
            val nameView = view.findViewById<TextView>(R.id.observation_list_item_name)
            val removeButton = view.findViewById<ImageButton>(R.id.observation_list_item_remove)

            nameView.text = targetName
            removeButton.contentDescription = context.getString(
                R.string.observation_list_remove_item,
                targetName,
            )
            removeButton.setOnClickListener { onRemove(targetName) }

            if (isNight) {
                val color = context.getColor(R.color.night_text_color)
                nameView.setTextColor(color)
                removeButton.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
            } else {
                removeButton.clearColorFilter()
            }
            return view
        }
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
