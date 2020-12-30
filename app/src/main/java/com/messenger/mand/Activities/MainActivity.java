package com.messenger.mand.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.messenger.mand.Adapters.ViewPagerAdapter;
import com.messenger.mand.Animations.DepthPageTransformer;
import com.messenger.mand.Interactions.DateInteraction;
import com.messenger.mand.Interactions.UserInteraction;
import com.messenger.mand.Objects.User;
import com.messenger.mand.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ImageView userPhoto;
    private TextView userName;
    private Menu menu;
    TabLayout tabLayout;
    ViewPager2 viewPager;
    private ArrayList<User> userArrayList;
    static SharedPreferences sharedPreferences;
    private FirebaseUser firebaseUser;
    DatabaseReference dbReference;

    private final static int THEME_LIGHT = 0;
    private final static int THEME_DARK = 1;
    public static int themePosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themePosition = loadInformationTheme();
        if (themePosition == 0) {
            setTheme(R.style.AppThemeLight);
        } else {
            setTheme(R.style.AppThemeNight);
        }
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userPhoto = findViewById(R.id.profile_image);
        userName = findViewById(R.id.username);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        viewPager.setAdapter(new ViewPagerAdapter(this));
        viewPager.setPageTransformer(new DepthPageTransformer());

        if (userArrayList == null) userArrayList = new ArrayList<>();
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText(getText(R.string.chats));
                            tab.setIcon(R.drawable.chat_tab);
                            break;
                        case 1:
                            tab.setText(getText(R.string.users));
                            tab.setIcon(R.drawable.ic_search_icon);
                            break;
                        case 2:
                            tab.setText(getText(R.string.profile));
                            tab.setIcon(R.drawable.ic_person_white);
                            break;
                    }
                });
        tabLayoutMediator.attach();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        dbReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        dbReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                userName.setText(user.getName());

                    if (!user.getAvatar().equals("default")) {
                        Glide.with(getApplicationContext()).load(user.getAvatar()).into(userPhoto);
                    } else {
                        userPhoto.setImageResource(R.mipmap.ic_launcher);
                    }
                    if (!UserInteraction.hasInternetConnection(MainActivity.this)) {
                            Toast.makeText(MainActivity.this, R.string.internet_connection,
                                    Toast.LENGTH_LONG).show();
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus(DateInteraction.getTimeNow());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus("online");
    }

    /**
     * we should delete SUPER to set our actions for this method
     */

    @Override
    public void onBackPressed() {    
        dialogWindowBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       this.menu = menu;
       getMenuInflater().inflate(R.menu.main_menu, menu);
       return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {

            case R.id.menu_icon_main:
                updateMenuIcons();
                return true;

            case R.id.item_choose_theme:
               changeApplicationTheme();
               return true;

           case android.R.id.home:
               dialogWindowBackPressed();
               return true;

           case R.id.item_settings:
               gotoSettingActivity();
               return true;
        }
        return false;
    }

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, StartActivity.class)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        }
    }

    private void updateMenuIcons() {
        MenuItem menuItem = menu.findItem(R.id.item_choose_theme);
        if (themePosition == THEME_DARK) {
            menuItem.setIcon(R.drawable.ic_label_light);
            menuItem.setTitle(R.string.light_theme);
        } else {
            menuItem.setIcon(R.drawable.ic_label_dark);
            menuItem.setTitle(R.string.dark_theme);
        }
    }

    private void saveInformationTheme(int value) {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putInt(firebaseUser.getUid(), value);
        ed.apply();
    }

    private int loadInformationTheme() {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;
        return sharedPreferences.getInt(firebaseUser.getUid(), 0);
    }

    /**
     * do not change this parameter to class cs as it damaged
     */

    private void updateUserStatus(String status) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        reference.updateChildren(hashMap);
    }

    private void exitIntentActivity() {
        updateUserStatus("Last seen: " + DateInteraction.getTimeNow());
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this,
                StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void gotoSettingActivity() {
        Intent intentSettings = new Intent(MainActivity.this,
                SettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK).putExtra("Theme", themePosition);
        startActivity(intentSettings);
    }

    private void changeApplicationTheme() {
        if (themePosition == THEME_DARK) {
            themePosition = THEME_LIGHT;
        } else {
            themePosition = THEME_DARK;
        }
        saveInformationTheme(themePosition);
        updateMenuIcons();
        recreate();
    }

    private void dialogWindowBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_warning_dialog,
                (ConstraintLayout) findViewById(R.id.layoutDialogContainer));
        builder.setView(view);
        ((TextView) view.findViewById(R.id.textTitle)).setText(getResources().getString(R.string.logout));
        ((TextView) view.findViewById(R.id.textMessage)).setText(getResources().getString(R.string.logout_text));
        ((Button) view.findViewById(R.id.buttonNo)).setText(getResources().getString(R.string.no));
        ((Button) view.findViewById(R.id.buttonYes)).setText(getResources().getString(R.string.yes));
        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ic_exit_icon);

        final AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonNo).setOnClickListener(v -> alertDialog.dismiss());

        view.findViewById(R.id.buttonYes).setOnClickListener(v -> {
            try {
                exitIntentActivity();
            } catch (Exception e) {
                FirebaseAuth.getInstance().signOut();
                finish();
            } finally {
                exitIntentActivity();
            }
        });
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }
}
