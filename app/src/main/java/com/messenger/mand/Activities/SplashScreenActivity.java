package com.messenger.mand.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.messenger.mand.Objects.Constants;
import com.messenger.mand.R;

public class SplashScreenActivity extends AppCompatActivity {

    Animation topAnim;
    Animation bottomAnim;
    ImageView imageView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        textView = findViewById(R.id.startTextLabel);
        imageView = findViewById(R.id.startSplashLabel);

        topAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.splash_image);
        bottomAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.splash_text);

        imageView.startAnimation(topAnim);
        textView.startAnimation(bottomAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenActivity.this, StartActivity.class);
                startActivity(intent);
                finish();
            }
        }, Constants.SPLASH_DELAY);
    }
}
