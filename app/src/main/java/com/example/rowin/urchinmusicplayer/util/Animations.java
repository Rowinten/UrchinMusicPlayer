package com.example.rowin.urchinmusicplayer.util;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.example.rowin.urchinmusicplayer.R;

/**
 * Created by Rowin on 3/2/2018.
 */

public class Animations {
    private AnimatorSet setFrontOut, setBackIn;
    private Context context;

    public Animations(Context context){
        this.context = context;
        loadAnimations();
    }

    private void loadAnimations(){
        setFrontOut = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.front_flip_imageview);
        setBackIn = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.back_flip_imageview);
    }

    public void fadeInAnimation(final View view){
        view.animate()
                .alpha(1f)
                .setDuration(0)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        view.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
    }

    public void fadeOutAnimation(final View view){
        view.animate()
                .alpha(0f)
                .setDuration(175)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        view.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
    }

    public void frontToBackAnimation(View albumImageHolderFront, View albumImageHolderBack){
        setFrontOut.setTarget(albumImageHolderFront);
        setBackIn.setTarget(albumImageHolderBack);
        setFrontOut.start();
        setBackIn.start();
    }

    public void backToFrontAnimation(View albumImageHolderBack, View albumImageHolderFront){
        setFrontOut.setTarget(albumImageHolderBack);
        setBackIn.setTarget(albumImageHolderFront);
        setFrontOut.start();
        setBackIn.start();
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

    public void albumImageScaleIncreaseAnimation(ImageView imageView, float toXDelta){
        AnimationSet animSet = new AnimationSet(true);
        animSet.setFillAfter(true);
        animSet.setDuration(175);
        ScaleAnimation scale = new ScaleAnimation(1f, 6f, 1f, 6f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0f);
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
