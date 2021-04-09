package com.messenger.mand.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.messenger.mand.R
import com.messenger.mand.values.Animation

class SplashScreenActivity : AppCompatActivity() {
    private var imageView: ImageView? = null
    private var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_splash_screen)
        textView = findViewById(R.id.startTextLabel)
        imageView = findViewById(R.id.startSplashLabel)
        val anim = AnimationUtils.loadAnimation(this, R.anim.anim_rotate)
        anim.startOffset = 0
        anim.duration = Animation.SPLASH_DELAY.toLong()
        imageView?.startAnimation(anim)
        Handler().postDelayed({
            val intent = Intent(this@SplashScreenActivity, StartActivity::class.java)
            startActivity(intent)
            finish()
        }, (Animation.SPLASH_DELAY + 100).toLong())
    }
}