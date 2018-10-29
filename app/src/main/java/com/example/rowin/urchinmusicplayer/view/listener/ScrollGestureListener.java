package com.example.rowin.urchinmusicplayer.view.listener;

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

public abstract class ScrollGestureListener extends GestureDetector.SimpleOnGestureListener{

    //@AmountPixForDegree, amount of pixels needed to scroll one degree
    //@AmountPixForAlpha, amount pixels needed to scroll to change alpha with 0.01f
    private int amountPixForDegree;
    private int amountPixForAlpha;

    protected ScrollGestureListener(View view){
        amountPixForDegree = (int) ((view.getWidth() / 1.5 ) / 180);
        amountPixForAlpha = (int) ((view.getWidth() / 1.5) / 100);
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
        float alphaScrolled = (diffX / amountPixForAlpha) / 100;

        if(degreeScrolled > 180){
            degreeScrolled = 180;
        } else if (degreeScrolled < -180){
            degreeScrolled = -180;
        }

        float toDegreeFront = 0 + degreeScrolled;
        float toDegreeBackRight = 180 + degreeScrolled;
        float toDegreeBackLeft = -180 + degreeScrolled;


        if(alphaScrolled > 1){
            alphaScrolled = 1;
        } else if(alphaScrolled < -1){
            alphaScrolled = -1;
        }


        float toAlphaLeft = 1 + alphaScrolled;
        float toAlphaRight = 1 - alphaScrolled;

        if (diffX > 0) {
            onRightScroll(toDegreeFront, toDegreeBackRight, toAlphaRight, degreeScrolled);
        } else {
            onLeftScroll(toDegreeFront, toDegreeBackLeft,  toAlphaLeft, degreeScrolled);
        }

        return super.onScroll(e1, e2, distanceX, distanceY);
    }


    public abstract void onLeftScroll(float toDegreeFront, float toDegreeBackLeft, float toAlpha, float degreeScrolled);

    public abstract void onRightScroll(float toDegreeFront, float toDegreeBackRight, float toAlpha, float degreeScrolled);
}

