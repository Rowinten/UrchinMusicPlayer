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

    public void verticalSlideAnimation(View frontView, View backView){
        setFrontOut.setTarget(frontView);
        setBackIn.setTarget(backView);
        setFrontOut.start();
        setBackIn.start();
    }

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

    public void albumImageScaleIncreaseAnimation(ImageView imageView, int fromY, int toY, float toXDelta, float increaseXTimes){
        AnimationSet animSet = new AnimationSet(true);
        animSet.setFillAfter(true);
        animSet.setDuration(250);

        ScaleAnimation scale = new ScaleAnimation(1f, increaseXTimes, 1f, increaseXTimes, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, -0.05f);
        animSet.addAnimation(scale);
        TranslateAnimation translate = new TranslateAnimation( 0, toXDelta, fromY, toY);
        animSet.addAnimation(translate);
        imageView.startAnimation(animSet);
    }

    public AnimationSet albumImageScaleDecreaseAnimationSet(float toXDelta, float decreaseXtimes, int fromY, int toY){
        AnimationSet animSet = new AnimationSet(true);
        animSet.setFillAfter(true);
        animSet.setDuration(175);
        ScaleAnimation scale = new ScaleAnimation(decreaseXtimes, 1f, decreaseXtimes, 1f,  Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0f);
        animSet.addAnimation(scale);
        TranslateAnimation translate = new TranslateAnimation( toXDelta, 0 , fromY, toY);
        animSet.addAnimation(translate);
        return animSet;
    }

    private void startVectorDrawableAnimation(Drawable drawable){
        if(drawable instanceof Animatable){
            ((Animatable) drawable).start();
        }
    }

}
