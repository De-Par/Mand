package com.messenger.mand.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.messenger.mand.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public final void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
