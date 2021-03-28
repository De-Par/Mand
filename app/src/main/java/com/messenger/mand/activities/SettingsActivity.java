package com.messenger.mand.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.messenger.mand.Fragments.SettingsFragment;
import com.messenger.mand.Interactions.DatabaseInteraction;
import com.messenger.mand.Interactions.DataInteraction;
import static com.messenger.mand.Values.Navigation.*;
import com.messenger.mand.R;

import java.util.Locale;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences preferences;

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

    SharedPreferences.OnSharedPreferenceChangeListener listener = (prefs, key) -> {
        if (key.equals("theme")) {
            boolean isNight = prefs.getBoolean("theme", false);
            if (isNight) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        } else if (key.equals("language_preference")) {
            String lang = prefs.getString("language_preference", "ru");
            setLocale(lang);
        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(getBaseContext(), NavigationActivity.class);
            intent.putExtra("position", LINK_PROFILE);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        DatabaseInteraction.pushUserStatus("online");
        super.onStart();
    }

    @Override
    protected void onResume() {
        preferences.registerOnSharedPreferenceChangeListener(listener);
        DatabaseInteraction.pushUserStatus("online");
        super.onResume();
    }

    @Override
    protected void onPause() {
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
        DatabaseInteraction.pushUserStatus(DataInteraction.getTimeNow());
        super.onPause();
    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, SettingsActivity.class);
        finish();
        startActivity(refresh);
    }
}
