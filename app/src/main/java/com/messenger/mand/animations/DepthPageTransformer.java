package com.messenger.mand.animations;

import android.view.View;

import androidx.viewpager2.widget.ViewPager2;

import org.jetbrains.annotations.NotNull;

/**
 * @author Google Developers, https://developers.google.com/
 */

public class DepthPageTransformer implements ViewPager2.PageTransformer {

   private static final float MIN_SCALE = 0.75f;

   public final void transformPage(@NotNull View view, float position) {
       int pageWidth = view.getWidth();

       if (position < -1) {    // This page is way off-screen to the left.
           view.setAlpha(0f);

       } else if (position <= 0) {    // Use the default slide transition when moving to the left page
           view.setAlpha(1f);
           view.setTranslationX(0f);
           view.setTranslationZ(0f);
           view.setScaleX(1f);
           view.setScaleY(1f);

       } else if (position <= 1) {
           // Fade the page out.
           view.setAlpha(1 - position);
           // Counteract the default slide transition
           view.setTranslationX(pageWidth * -position);
           // Move it behind the left page
           view.setTranslationZ(-1f);
           // Scale the page down (between MIN_SCALE and 1)
           float scaleFactor = MIN_SCALE
                   + (1 - MIN_SCALE) * (1 - Math.abs(position));
           view.setScaleX(scaleFactor);
           view.setScaleY(scaleFactor);

       } else {    // This page is way off-screen to the right.
           view.setAlpha(0f);
       }
   }
}
