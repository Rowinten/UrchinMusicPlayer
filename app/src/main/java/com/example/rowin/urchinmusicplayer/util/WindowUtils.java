package com.example.rowin.urchinmusicplayer.util;

import android.content.Context;
import android.support.design.widget.TabLayout;
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

    public void setWindowMetrics(Window window, Toolbar toolbar, TabLayout tabs){
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        toolbar.setPadding(0, getStatusBarHeight() , 0, 0);
        toolbar.getLayoutParams().height = toolbar.getLayoutParams().height + getStatusBarHeight();
        tabs.setPadding(0,0,0, getNavigationBarHeight());
    }

}
