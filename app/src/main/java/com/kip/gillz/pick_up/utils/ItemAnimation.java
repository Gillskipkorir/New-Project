package com.kip.gillz.pick_up.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

public class ItemAnimation {

    /* animation type */
    public static final int BOTTOM_UP = 1;
    public static final int FADE_IN = 2;
    public static final int LEFT_RIGHT = 3;
    public static final int RIGHT_LEFT = 4;
    public static final int NONE = 0;

    /* animation duration */
    private static final long DURATION_IN_FADE_ID = 300;

    public static void animate(View view, int position, int type) {
        switch (type) {


            case FADE_IN:
                animateFadeIn(view, position);
                break;



        }
    }


    private static void animateFadeIn(View view, int position) {
        boolean not_first_item = position == -1;
        position = position + 1;
        view.setAlpha(0.f);
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(view, "alpha", 0.f, 0.5f, 1.f);
        ObjectAnimator.ofFloat(view, "alpha", 0.f).start();
        animatorAlpha.setStartDelay(not_first_item ? DURATION_IN_FADE_ID / 2 : (position * DURATION_IN_FADE_ID / 3));
        animatorAlpha.setDuration(DURATION_IN_FADE_ID);
        animatorSet.play(animatorAlpha);
        animatorSet.start();
    }



}
