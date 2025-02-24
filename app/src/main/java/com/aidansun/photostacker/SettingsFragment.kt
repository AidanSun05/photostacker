package com.aidansun.photostacker

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val versionCode = BuildConfig.VERSION_CODE
        val versionName = BuildConfig.VERSION_NAME

        val versionPreference: Preference? = findPreference("version")
        versionPreference?.summary = "$versionName (code: $versionCode)"

        val buildTypePreference: Preference? = findPreference("buildType")
        buildTypePreference?.summary = if (BuildConfig.DEBUG) "Debug" else "Release"
    }
}
