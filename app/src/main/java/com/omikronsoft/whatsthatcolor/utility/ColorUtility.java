package com.omikronsoft.whatsthatcolor.utility;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by Dariusz Lelek on 10/12/2017.
 * dariusz.lelek@gmail.com
 */

public class ColorUtility {
    private final static int pixelsToSkip = 2;

    public static int getAverageColor(Bitmap bitmap){
        int red = 0;
        int green = 0;
        int blue = 0;
        int n = 0;

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        int[] pixels = new int[width * height];

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < pixels.length; i += pixelsToSkip) {
            int color = pixels[i];
            red += Color.red(color);
            green += Color.green(color);
            blue += Color.blue(color);
            n++;
        }
        return Color.rgb(red / n, green / n, blue / n);
    }
}
