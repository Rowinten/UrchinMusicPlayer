package com.example.rowin.urchinmusicplayer.util;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.example.rowin.urchinmusicplayer.R;

/**
 * Created by Rowin on 3/2/2018.
 */

public class Animations {
    private AnimatorSet setFrontOut, setBackIn, slideInRight, slideOutRight, slideInLeft, slideOutLeft;
    private Context context;

    public Animations(Context context){
        this.context = context;
        loadAnimations();
    }

    private void loadAnimations(){
        setFrontOut = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.front_flip_imageview);
        setBackIn = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.back_flip_imageview);
        slideInRight = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.slide_in_right_animation);
        slideOutRight = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.slide_out_right_animation);
        slideInLeft = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.slide_in_left_animation);
        slideOutLeft = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.slide_out_left_animation);
    }

    public void fadeAnimation(final View view, float toAlpha, int duration){
        view.animate()
                .alpha(toAlpha)
                .setDuration(duration);
    }

    public void fadeBackgroundAnimation(final View fadeView, float fromAlpha, float toAlpha, int duration, final boolean fadeOut){
        Animation fadeAnimation = new AlphaAnimation(fromAlpha, toAlpha);
        fadeAnimation.setDuration(duration);

        if(!fadeOut) {
            fadeAnimation.setInterpolator(new DecelerateInterpolator());
        } else {
            fadeAnimation.setInterpolator(new AccelerateInterpolator());
        }

        fadeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(!fadeOut){
                    fadeView.setVisibility(View.VISIBLE);
                } else {
                    fadeView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeView.setAnimation(fadeAnimation);
        fadeAnimation.start();
    }

    public void verticalSlideAnimation(View frontView, View backView){
        setFrontOut.setTarget(frontView);
        setBackIn.setTarget(backView);
        setFrontOut.start();
        setBackIn.start();
    }

    public void slideRightAnimation(View imageBack, View imageFront){
        slideOutRight.setTarget(imageFront);
        slideInRight.setTarget(imageBack);
        slideOutRight.start();
        slideInRight.start();
    }

    public void slideLeftAnimation(View imageBack, View imageFront){
        slideOutLeft.setTarget(imageFront);
        slideInLeft.setTarget(imageBack);
        slideOutLeft.start();
        slideInLeft.start();
    }

//    public void slideImageView(View targetFront, View targetBack, float fromDegree, float toDegree, int duration){
//        ObjectAnimator flipFrontOut = ObjectAnimator.ofFloat(targetFront, "rotationY", fromDegree, toDegree);
//        flipFrontOut.setDuration(duration);
//
//        ObjectAnimator flipBackIn = ObjectAnimator.ofFloat(targetBack, "rotationY", fromDegree, toDegree);
//        flipBackIn.setDuration(duration);
//
//        flipFrontOut.start();
//        flipBackIn.start();
//    }

    public void slideAndFadeImageView(View targetFront, View targetBack, float fromDegree, float toDegree){
        ObjectAnimator flipFrontOut = ObjectAnimator.ofFloat(targetFront, "rotationY", fromDegree, toDegree);
        flipFrontOut.setDuration(400);
        ObjectAnimator flipBackIn = ObjectAnimator.ofFloat(targetBack, "rotationY", fromDegree, toDegree);
        flipBackIn.setDuration(400);

        ObjectAnimator fadeOutAnimator = fadeOutObjectAnimator(targetFront, 0);
        fadeOutAnimator.setStartDelay(200);

        ObjectAnimator fadeInAnimator = fadeInObjectAnimator(targetBack, 0);
        fadeInAnimator.setStartDelay(200);


        fadeOutAnimator.start();
        fadeInAnimator.start();
        flipFrontOut.start();
        flipBackIn.start();
    }

    //Object animators of the above four animations, which are done via an animator xml file
    public ObjectAnimator slideFrontOut(View imageFront, float fromDegree, float toDegree){

        return ObjectAnimator.ofFloat(imageFront, "rotationY", fromDegree, toDegree);
    }

    public ObjectAnimator slideBackIn(View imageBack, float fromDegree, float toDegree){
        ObjectAnimator slideInAnimator = ObjectAnimator.ofFloat(imageBack, "rotationY", fromDegree, toDegree);
        return slideInAnimator;
    }

    /**
     * used to fade out Views
     * @param image image that needs to be faded out
     * @param duration duration of the animation
     * @return the fadeOut Animator
     */
    public ObjectAnimator fadeOutObjectAnimator(View image, int duration){
        ObjectAnimator fadeOutAnimator = ObjectAnimator.ofFloat(image, View.ALPHA, 1f, 0f);
        fadeOutAnimator.setDuration(duration);

        return fadeOutAnimator;
    }


    /**
     * used to fade in Views
     * @param image image that needs to be faded in
     * @param duration duration of the animation
     * @return the fadeIn Animator
     */
    public ObjectAnimator fadeInObjectAnimator(View image, int duration){
        ObjectAnimator fadeInAnimator = ObjectAnimator.ofFloat(image, View.ALPHA, 0f, 1f);
        fadeInAnimator.setDuration(duration);

        return fadeInAnimator;
    }


    public void nextSongAnimation(ImageView nextSongButton){
        Drawable nextSongAnimation = nextSongButton.getDrawable();
        startVectorDrawableAnimation(nextSongAnimation);
    }

    public void playToPauseAnimation(ImageView playButton){
        playButton.setImageResource(R.drawable.play_to_pause_animator);
        Drawable playToPauseAnimation = playButton.getDrawable();
        startVectorDrawableAnimation(playToPauseAnimation);
    }

    public void pauseToPlayAnimation(ImageView playButton){
        playButton.setImageResource(R.drawable.pause_to_play_animator);
        Drawable pauseToPlayAnimation = playButton.getDrawable();
        startVectorDrawableAnimation(pauseToPlayAnimation);
    }

    public void shuffleToCancelAnimation(ImageView shuffleButton){
        shuffleButton.setImageResource(R.drawable.shuffle_to_cancel_animation);
        Drawable shuffleToCancelAnimation = shuffleButton.getDrawable();
        startVectorDrawableAnimation(shuffleToCancelAnimation);
    }

    public void cancelToShuffleAnimation(ImageView shuffleButton){
        shuffleButton.setImageResource(R.drawable.cancel_to_shuffle_animation);
        Drawable cancelToShuffleAnimation = shuffleButton.getDrawable();
        startVectorDrawableAnimation(cancelToShuffleAnimation);
    }

    public void albumImageScaleIncreaseAnimation(ImageView imageView, float toXDelta, float increaseXTimes){
        AnimationSet animSet = new AnimationSet(true);
        animSet.setFillAfter(true);
        animSet.setDuration(175);


        ScaleAnimation scale = new ScaleAnimation(1f, increaseXTimes, 1f, increaseXTimes, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, -0.05f);
        animSet.addAnimation(scale);
        TranslateAnimation translate = new TranslateAnimation( 0, toXDelta , 0, 0);
        animSet.addAnimation(translate);
        imageView.startAnimation(animSet);
    }

    public AnimationSet albumImageScaleDecreaseAnimationSet(float toXDelta){
        AnimationSet animSet = new AnimationSet(true);
        animSet.setFillAfter(true);
        animSet.setDuration(175);
        ScaleAnimation scale = new ScaleAnimation(6f, 1f, 6f, 1f,  Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0f);
        animSet.addAnimation(scale);
        TranslateAnimation translate = new TranslateAnimation( toXDelta, 0 , 0, 0);
        animSet.addAnimation(translate);
        return animSet;
    }

    private void startVectorDrawableAnimation(Drawable drawable){
        if(drawable instanceof Animatable){
            ((Animatable) drawable).start();
        }
    }

}
