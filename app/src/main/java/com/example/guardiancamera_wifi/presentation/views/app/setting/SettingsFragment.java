package com.example.guardiancamera_wifi.presentation.views.app.setting;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.example.guardiancamera_wifi.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, null);
    }
}
