package com.messenger.mand.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.messenger.mand.Interactions.DatabaseInteraction;
import com.messenger.mand.Interactions.DateInteraction;
import com.messenger.mand.R;

import java.util.Objects;

import static com.messenger.mand.Activities.MainActivity.themePosition;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        if (intent != null) {
            themePosition = intent.getIntExtra("Theme", 0);
        }
        if (themePosition == 0) {
            setTheme(R.style.AppThemeLight);
        } else {
            setTheme(R.style.AppThemeNight);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goToMainActivity();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            goToMainActivity();
            return true;
        }
        return false;
    }

    private void goToMainActivity() {
        Intent intentSet = new Intent(SettingsActivity.this, MainActivity.class);
        intentSet.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intentSet);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        DatabaseInteraction dbi = new DatabaseInteraction();
        dbi.pushUserStatus(DateInteraction.getTimeNow());
    }

    @Override
    protected void onResume() {
        super.onResume();
        DatabaseInteraction dbi = new DatabaseInteraction();
        dbi.pushUserStatus("online");
    }
}
