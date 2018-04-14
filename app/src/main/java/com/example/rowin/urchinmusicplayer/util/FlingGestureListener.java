package com.example.rowin.urchinmusicplayer.util;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.OverScroller;

/**
 * Created by Rowin on 4/8/2018.
 */

public abstract class FlingGestureListener extends GestureDetector.SimpleOnGestureListener{
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 2500;
    public static final int SWIPE_TO_LEFT = 0;
    public static final int SWIPE_TO_RIGHT = 1;

    private int amountPixForDegree;


    //TODO ON SCROLL, FOR EVERY <WIDTH OF VIEW / 180> pixels. View must rotate 1 - 1.5 degree.
    protected FlingGestureListener(View view){
        amountPixForDegree = (int) ((view.getWidth() / 1.5 ) / 180);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }


    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        //Pixels dragged across screen
        float diffX = e2.getX() - e1.getX();
        float degreeScrolled = diffX / amountPixForDegree;

        if(degreeScrolled > 180){
            degreeScrolled = 180;
        } else if (degreeScrolled < -180){
            degreeScrolled = -180;
        }

        if(diffX > 0) {
            onScrollAnimation(degreeScrolled, SWIPE_TO_RIGHT);
        } else {
            onScrollAnimation(degreeScrolled, SWIPE_TO_LEFT);
        }

        return super.onScroll(e1, e2, distanceX, distanceY);
    }


    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        boolean result = false;

        float diffY = e2.getY() - e1.getY();
        float diffX = e2.getX() - e1.getX();

        //If diffX > diffY = user is swiping horizontally.
        if (Math.abs(diffX) > Math.abs(diffY)) {
            //Determines how many pixels user has to swipe before it is counted as a "Fling", also the velocity ( how fast ) the user flings is taken into account.
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                //if user swaps right to left, X value will be bigger than 0, if user swipes left to right value will be smaller than 0;
                if (diffX > 0) {
                    onRightFling();
                } else {
                    onLeftFling();
                }
                result = true;
            }
        }

        return result;
    }

    public abstract void onRightFling();

    public abstract void onLeftFling();

    public abstract void onScrollAnimation(float degreeScrolled, int scrollingDirection);
}

