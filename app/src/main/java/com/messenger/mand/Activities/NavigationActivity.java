package com.messenger.mand.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.messenger.mand.Fragments.ChatsFragment;
import com.messenger.mand.Fragments.ProfileFragment;
import com.messenger.mand.Fragments.UsersFragment;
import com.messenger.mand.Interactions.DataInteraction;
import com.messenger.mand.Interactions.DatabaseInteraction;
import com.messenger.mand.Interactions.LanguageContextWrapper;
import com.messenger.mand.Interfaces.DataPasser;
import com.messenger.mand.Objects.Constants;
import com.messenger.mand.R;

public class NavigationActivity extends AppCompatActivity implements DataPasser {

    private FragmentManager fragmentManager;
    private ChipNavigationBar bottomNav;

    private boolean isExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        bottomNav = findViewById(R.id.bottomActionBar);

        if (savedInstanceState == null) {
            bottomNav.setItemSelected(R.id.navigation_chats, true);
            fragmentManager = getSupportFragmentManager();
            ChatsFragment chatsFragment = new ChatsFragment();
            fragmentManager.beginTransaction().replace(R.id.fragment_container, chatsFragment).commit();
        }

        bottomNav.setOnItemSelectedListener(i -> {
            Fragment fragment = null;
            switch (i) {
                case R.id.navigation_chats:
                    fragment = new ChatsFragment();
                    break;
                case R.id.navigation_users:
                    fragment = new UsersFragment();
                    break;
                case R.id.navigation_profile:
                    fragment = new ProfileFragment();
                    break;
            }
            if (fragment != null) {
                fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageContextWrapper.wrap(newBase,"en"));
    }

    @Override
    protected void onStart() {
        isExit = false;
        checkUserStatus();
        DatabaseInteraction.pushUserStatus(getString(R.string.online));
        DataInteraction.deleteCache(getApplicationContext());
        super.onStart();
    }

    @Override
    protected void onResume() {
        isExit = false;
        DatabaseInteraction.pushUserStatus(getString(R.string.online));
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (!isExit) DatabaseInteraction.pushUserStatus(DataInteraction.getTimeNow());
        DataInteraction.deleteCache(getApplicationContext());
        super.onPause();
    }

    @Override
    public void onBackPressed() {    
        dialogWindowBackPressed();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main_menu, menu);
//        return true;
//    }

    @Override
    public void onDataPass(String data) {
        if (data.equals("goto_profile")) {
            bottomNav.setItemSelected(R.id.navigation_profile, true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.item_settings) {
            gotoSettingActivity();
        } else if (id == R.id.item_exit) {
            dialogWindowBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            exitIntentActivity();
        }
    }

    private void gotoSettingActivity() {
        Intent intentSettings = new Intent(NavigationActivity.this,
                SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentSettings);
    }

    private void dialogWindowBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NavigationActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(NavigationActivity.this).inflate(R.layout.warning_dialog, findViewById(R.id.layoutDialogContainer));
        builder.setView(view);
        ((TextView) view.findViewById(R.id.textTitle)).setText(getResources().getString(R.string.logout));
        ((TextView) view.findViewById(R.id.textMessage)).setText(getResources().getString(R.string.logout_text));
        ((Button) view.findViewById(R.id.buttonNo)).setText(getResources().getString(R.string.no));
        ((Button) view.findViewById(R.id.buttonYes)).setText(getResources().getString(R.string.yes));
        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_exit_icon);

        final AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonNo).setOnClickListener(v -> alertDialog.dismiss());

        view.findViewById(R.id.buttonYes).setOnClickListener(v -> {
            isExit = true;
            DatabaseInteraction.pushUserStatus(DataInteraction.getTimeNow());
            exitIntentActivity();
        });
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }

    private void exitIntentActivity() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(NavigationActivity.this,
                StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
