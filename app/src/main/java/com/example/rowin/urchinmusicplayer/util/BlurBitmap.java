package com.example.rowin.urchinmusicplayer.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

/**
 * Created by Rowin on 4/23/2018.
 */

public class BlurBitmap {
    private static final float BITMAP_SCALE = 0.5f;
    private static final float BLUR_RADIUS = 25f;

    /**
     *
     * @param context context of class
     * @param bitmap image that needs to be blurred
     * @return blurred version of image
     */

    public static Bitmap blur(Context context, Bitmap bitmap){
        try{
            RenderScript renderScript = RenderScript.create(context);
            Allocation allocation = Allocation.createFromBitmap(renderScript, bitmap);

            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
            blur.setRadius(BLUR_RADIUS);
            blur.setInput(allocation);

            Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Allocation outAllocation = Allocation.createFromBitmap(renderScript, result);

            blur.forEach(outAllocation);
            outAllocation.copyTo(result);

            renderScript.destroy();

            int width = Math.round(result.getWidth() * BITMAP_SCALE);
            int height = Math.round(result.getHeight() * BITMAP_SCALE);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(result, width, height, false);

            return scaledBitmap;
        }
        catch (Exception e){
            return bitmap;
        }
    }
}
