// File: app/src/main/java/com/example/swoop/ui/SettingsFragment.kt
package com.example.swoop.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.text.method.ScrollingMovementMethod
import androidx.core.text.HtmlCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.swoop.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<Preference>("pref_instructions")?.setOnPreferenceClickListener {
            showScrollableDialog(
                R.string.title_instructions,
                R.string.pref_instructions_text
            )
            true
        }

        findPreference<Preference>("pref_game_rules")?.setOnPreferenceClickListener {
            showScrollableDialog(
                R.string.title_game_rules,
                R.string.pref_game_rules_text
            )
            true
        }
    }

    private fun showScrollableDialog(titleRes: Int, htmlStringRes: Int) {
        // Inflate our scrollable layout
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_scrollable_text, null, false)

        // Find the TextView and render HTML into it
        val tvContent = dialogView.findViewById<TextView>(R.id.tv_dialog_content)
        val rawHtml = getString(htmlStringRes)
        tvContent.text = HtmlCompat.fromHtml(rawHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
        tvContent.movementMethod = ScrollingMovementMethod()

        // Build & show the dialog
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(titleRes)
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}
