package com.messenger.mand.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.messenger.mand.fragments.ChatsFragment;
import com.messenger.mand.fragments.ProfileFragment;
import com.messenger.mand.fragments.UsersFragment;
import com.messenger.mand.interactions.DataInteraction;
import com.messenger.mand.interactions.DatabaseInteraction;
import com.messenger.mand.interactions.locale.LocaleHelper;
import com.messenger.mand.interfaces.DataPasser;
import static com.messenger.mand.values.Navigation.*;
import com.messenger.mand.R;

import org.jetbrains.annotations.NotNull;

public class DashboardActivity extends AppCompatActivity implements DataPasser {
    private final String TAG = DashboardActivity.class.toString();

    private FragmentManager fragmentManager;
    private ChipNavigationBar bottomNav;
    private boolean isExit = false;
    String position;
    String lang;
    SharedPreferences preferences;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean isNight = preferences.getBoolean("theme", false);

        if (isNight) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        bottomNav = findViewById(R.id.bottomActionBar);

        try {
            position = getIntent().getStringExtra("position");
            Log.e(TAG, "Position of fragment is -> " + position);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        if (savedInstanceState == null && position == null) {
            bottomNav.setItemSelected(R.id.navigation_chats, true);
            fragmentManager = getSupportFragmentManager();
            ChatsFragment chatsFragment = new ChatsFragment();
            fragmentManager.beginTransaction().replace(R.id.fragment_container, chatsFragment).commit();

        } else if (position != null) {
            changeViewPosition(position);
        }

        bottomNav.setOnItemSelectedListener(i -> {
            Fragment fragment = null;
            if (i == R.id.navigation_chats) {
                fragment = new ChatsFragment();
            } else if (i == R.id.navigation_users) {
                fragment = new UsersFragment();
            } else if (i == R.id.navigation_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
            }
        });
    }

    @Override
    protected final void attachBaseContext(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        lang = preferences.getString("language", "");

        if (!lang.equals("")) {
            LocaleHelper.onAttach(context, lang);
        }
        super.attachBaseContext(context);
    }

    @Override
    public final boolean onOptionsItemSelected(@NotNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.item_settings) {
            gotoSettingActivity();
        } else if (id == R.id.item_exit) {
            dialogWindowBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected final void onStart() {
        super.onStart();
        isExit = false;
        checkUserStatus();
        DatabaseInteraction.pushUserStatus("online");
    }

    @Override
    protected final void onResume() {
        super.onResume();
        isExit = false;
        DatabaseInteraction.pushUserStatus("online");
    }

    @Override
    protected final void onPause() {
        super.onPause();
        if (!isExit) DatabaseInteraction.pushUserStatus(DataInteraction.getTimeNow());
    }

    @Override
    public final void onBackPressed() {
        dialogWindowBackPressed();
    }

    @Override
    public final void onDataPass(String data) {
        changeViewPosition(data);
    }

    private void changeViewPosition(@NotNull String data) {
        Fragment fragment;
        if (data.equals(LINK_USERS)) {
            fragment = new UsersFragment();
            bottomNav.setItemSelected(R.id.navigation_users, true);
        } else if (data.equals(LINK_PROFILE)) {
            fragment = new ProfileFragment();
            bottomNav.setItemSelected(R.id.navigation_profile, true);
        } else  {
            fragment = new ChatsFragment();
            bottomNav.setItemSelected(R.id.navigation_chats, true);
        }
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            signOutFromApp();
        }
    }

    private void gotoSettingActivity() {
        Intent intentSettings = new Intent(DashboardActivity.this,
                SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentSettings);
        finish();
    }

    private void dialogWindowBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(DashboardActivity.this).inflate(R.layout.warning_dialog,
                findViewById(R.id.layoutDialogContainer));
        builder.setView(view);
        ((TextView) view.findViewById(R.id.textTitle)).setText(getResources().getString(R.string.logout));
        ((TextView) view.findViewById(R.id.textMessage)).setText(getResources().getString(R.string.logout_text));
        ((Button) view.findViewById(R.id.buttonNo)).setText(getResources().getString(R.string.no));
        ((Button) view.findViewById(R.id.buttonYes)).setText(getResources().getString(R.string.yes));
        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_exit);

        final AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonNo).setOnClickListener(v -> alertDialog.dismiss());

        view.findViewById(R.id.buttonYes).setOnClickListener(v -> {
            isExit = true;
            alertDialog.dismiss();
            DatabaseInteraction.pushUserStatus(DataInteraction.getTimeNow());
            signOutFromApp();
        });
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }

    private void signOutFromApp() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(DashboardActivity.this,
                StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
