package com.example.rowin.urchinmusicplayer.util;

import android.content.Context;
import android.content.res.Resources;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.support.v7.widget.Toolbar;

/**
 * Created by Rowin on 3/28/2018.
 */

public class WindowUtils {
    private Context context;

    public WindowUtils(Context context){
        this.context = context;
    }

    public void hideSoftKeyboard(View currentFocus){
        if(currentFocus != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getNavigationBarHeight(){
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public float getCenterScreenX(View view){
        return context.getResources().getDisplayMetrics().widthPixels / 2 - getMarginConstraintView(view);
    }

    private float getMarginConstraintView(View view){
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        return convertDpToPx(lp.leftMargin);
    }

    public void setWindowMetrics(Window window, Toolbar toolbar, TabLayout tabs){
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        toolbar.setPadding(0, getStatusBarHeight() , 0, 0);
        toolbar.getLayoutParams().height = toolbar.getLayoutParams().height + getStatusBarHeight();
        tabs.setPadding(0,0,0, getNavigationBarHeight());
    }

    public void setWindowMetrics(Window window, AppBarLayout toolbar){
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        toolbar.setPadding(0, getStatusBarHeight() , 0, 0);
        toolbar.getLayoutParams().height = toolbar.getLayoutParams().height + getStatusBarHeight();
    }

    public float convertDpToPx(int dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

}
