package com.example.rowin.urchinmusicplayer.util;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.View;
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

    //TODO fix this animation, as it does not completely flip over.
    public void changeAlbumPictureAnimation(ImageView albumPictureView, Bitmap newAlbumImage){
        setFrontOut.setTarget(albumPictureView);
        setFrontOut.start();

        albumPictureView.setImageBitmap(newAlbumImage);

        setBackIn.setTarget(albumPictureView);
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

    private void startVectorDrawableAnimation(Drawable drawable){
        if(drawable instanceof Animatable){
            ((Animatable) drawable).start();
        }
    }
}
