package com.example.rowin.urchinmusicplayer.util;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by Rowin on 3/14/2018.
 */

//Reads certain type of color from a bitmap object
public class ColorReader {

    //Gets dominant color from a bitmap
    public int getDominantColor(Bitmap bitmap) {
        if (bitmap == null) {
            return Color.TRANSPARENT;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = width * height;
        int pixels[] = new int[size];
        //Bitmap bitmap2 = bitmap.copy(Bitmap.Config.ARGB_43444, false);
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int color;
        int r = 0;
        int g = 0;
        int b = 0;
        int a;
        int count = 0;
        for (int pixel : pixels) {
            color = pixel;
            a = Color.alpha(color);
            if (a > 0) {
                r += Color.red(color);
                g += Color.green(color);
                b += Color.blue(color);
                count++;
            }
        }
        r /= count;
        g /= count;
        b /= count;
        r = (r << 16) & 0x00FF0000;
        g = (g << 8) & 0x0000FF00;
        b = b & 0x000000FF;
        color = 0xFF000000 | r | g | b;
        return color;
    }

    public int getComplimentedColor(int color) {
        // get existing colors
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int blue = Color.blue(color);
        int green = Color.green(color);


        //formula to detect luminance of a given color
        double luma = 0.2126 * red + 0.7152 * green + 0.0722 * blue;


        //Checks if luminance = lower than 40 ( too dark ) or higher than 220 ( too light )
        if(luma < 40 || luma > 220){
            red = 51;
            green = 102;
            blue = 255;
        } else {
            //Adds value to the dominant value of a color, making it pop out more.
            if (red > blue && red > green && red + 60 < 255) {
                red = red + 50;
            }

            if (green > red && green > blue && green + 60 < 255) {
                green = green + 50;
            }

            if (blue > red && blue > green && blue + 60 < 255) {
                blue = blue + 50;
            }
        }


        return Color.argb(alpha, red, green, blue);
    }


}
