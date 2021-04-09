package com.messenger.mand.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.messenger.mand.fragments.SettingsFragment;
import com.messenger.mand.interactions.DatabaseInteraction;
import com.messenger.mand.interactions.DataInteraction;
import static com.messenger.mand.values.Navigation.*;
import com.messenger.mand.R;
import com.messenger.mand.interactions.locale.LocaleHelper;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    String lang;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
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

    SharedPreferences.OnSharedPreferenceChangeListener listener = (prefs, key) -> {
        if (key.equals("theme")) {
            boolean isNight = prefs.getBoolean("theme", false);
            if (isNight) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        } else if (key.equals("language")) {
            lang = prefs.getString("language", "");
            Intent refresh = new Intent(this, SettingsActivity.class);
            finish();
            startActivity(refresh);
        }
    };

    @Override
    public final boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            gotoNavActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public final void onBackPressed() {
        gotoNavActivity();
    }

    @Override
    protected final void onStart() {
        super.onStart();
        DatabaseInteraction.pushUserStatus("online");
    }

    @Override
    protected final void onResume() {
        super.onResume();
        preferences.registerOnSharedPreferenceChangeListener(listener);
        DatabaseInteraction.pushUserStatus("online");
    }

    @Override
    protected final void onPause() {
        super.onPause();
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
        DatabaseInteraction.pushUserStatus(DataInteraction.getTimeNow());
    }

    @Override
    protected final void attachBaseContext(Context newBase) {
        LocaleHelper.onAttach(newBase, lang);
        super.attachBaseContext(newBase);
    }

    private void gotoNavActivity() {
        Intent intent = new Intent(getBaseContext(), DashboardActivity.class);
        intent.putExtra("position", LINK_PROFILE);
        startActivity(intent);
        finish();
    }
}
