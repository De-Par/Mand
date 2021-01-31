package com.messenger.mand.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.messenger.mand.Interactions.LanguageContextWrapper;
import com.messenger.mand.Interactions.UserInteraction;
import com.messenger.mand.R;

import java.util.Locale;

public class StartActivity extends AppCompatActivity {

    private Button login, register;
    private TextView exit;
    private Animation buttonAnimation;
    private byte stateButton = 0;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null && UserInteraction.hasInternetConnection(getApplicationContext())) {
            Intent intent = new Intent(StartActivity.this, NavigationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

         if (!UserInteraction.hasInternetConnection(getApplicationContext())) {
             Toast.makeText(StartActivity.this, R.string.internet_connection,
                    Toast.LENGTH_LONG).show();
        }

        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        exit = findViewById(R.id.exit);

        buttonAnimation = AnimationUtils.loadAnimation(getBaseContext(), R.anim.anim_scale_button_pressing);

        login.setOnClickListener(v -> {
            stateButton = 1;
            login.startAnimation(buttonAnimation);
        });

        register.setOnClickListener(v -> {
            stateButton = 2;
            register.startAnimation(buttonAnimation);
        });

        exit.setOnClickListener(v -> {
            stateButton = 0;
            exit.startAnimation(buttonAnimation);
        });

       buttonAnimation.setAnimationListener(new Animation.AnimationListener() {
           @Override
           public void onAnimationStart(Animation animation) {}

           @Override
           public void onAnimationEnd(Animation animation) {
               switch (stateButton) {
                   case 1:
                       startActivity(new Intent(StartActivity.this, LoginActivity.class));
                       break;
                   case 2:
                       startActivity(new Intent(StartActivity.this, RegisterActivity.class));
                       break;
                   default:
                       shutdownApp();
               }
           }

           @Override
           public void onAnimationRepeat(Animation animation) {}
       });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageContextWrapper.wrap(newBase, "ru"));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(StartActivity.this, R.string.bye,
                Toast.LENGTH_SHORT).show();
        finishAffinity();
    }

    private void shutdownApp() {
        Toast.makeText(StartActivity.this, R.string.bye,
                Toast.LENGTH_SHORT).show();
        finishAffinity();
    }
}
