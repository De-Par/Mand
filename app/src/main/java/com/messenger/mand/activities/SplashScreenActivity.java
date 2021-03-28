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

import com.messenger.mand.R;
import static com.messenger.mand.Values.Animation.*;

public class SplashScreenActivity extends AppCompatActivity {

    ImageView imageView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        textView = findViewById(R.id.startTextLabel);
        imageView = findViewById(R.id.startSplashLabel);

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);
        anim.setStartOffset(0);
        anim.setDuration(SPLASH_DELAY);

        imageView.startAnimation(anim);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashScreenActivity.this, StartActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY + 100);
    }
}
