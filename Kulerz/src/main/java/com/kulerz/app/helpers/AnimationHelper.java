package com.kulerz.app.helpers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

public class AnimationHelper
{

    /**
     * Crossfading two views with property animator
     * @param fadeIn
     * @param fadeOut
     */
    public static void crossfade(final View fadeIn, final View fadeOut) {
        fadeIn.setAlpha(0f);
        fadeIn.setVisibility(View.VISIBLE);
        fadeIn.animate().alpha(1f).setDuration(200).setListener(null);
        fadeOut.animate().alpha(0f).setDuration(200).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fadeOut.setVisibility(View.GONE);
            }
        });
    }

}
