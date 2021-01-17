package com.messenger.mand.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.preference.PreferenceManager;

import com.messenger.mand.Fragments.SettingsFragment;
import com.messenger.mand.Interactions.DatabaseInteraction;
import com.messenger.mand.Interactions.DataInteraction;
import com.messenger.mand.R;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences preferences;

    SharedPreferences.OnSharedPreferenceChangeListener listener = (prefs, key) -> {
        if (key.equals("theme")) {
            boolean isNight = prefs.getBoolean("theme", false);
            if (isNight) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_settings, new SettingsFragment())
                .commit();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        DatabaseInteraction.pushUserStatus(getString(R.string.online));
        super.onStart();
    }

    @Override
    protected void onResume() {
        preferences.registerOnSharedPreferenceChangeListener(listener);
        DatabaseInteraction.pushUserStatus(getString(R.string.online));
        super.onResume();
    }

    @Override
    protected void onPause() {
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
        DatabaseInteraction.pushUserStatus(DataInteraction.getTimeNow());
        super.onPause();
    }
}
