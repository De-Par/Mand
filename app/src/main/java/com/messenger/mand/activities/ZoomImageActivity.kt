package com.messenger.mand.activities

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.igreenwood.loupe.extensions.createLoupe
import com.igreenwood.loupe.extensions.setOnViewTranslateListener
import com.messenger.mand.R

class ZoomImageActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        setContentView(R.layout.activity_zoom_image)

        val imageView: ImageView = findViewById(R.id.photo)
        val container: FrameLayout = findViewById(R.id.container_photo)
        val extras = intent.extras

        container.setBackgroundColor(getColor(R.color.black))

        if (extras!!.getInt("icon") == 0) {
            Glide.with(applicationContext).load(extras.getString("photo")).into(imageView)
        } else {
            imageView.setImageDrawable(ContextCompat.getDrawable(applicationContext,
                    R.drawable.profile_image_default))
        }
        createLoupe(imageView, container) {
            //useFlingToDismissGesture = false
            setOnViewTranslateListener(
                    onStart = { },
                    onRestore = { },
                    onDismiss = { finishAfterTransition() }
            )
        }
    }
}