package com.omikronsoft.whatsthatcolor.utility.color;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.omikronsoft.whatsthatcolor.R;

/**
 * Created by Dariusz Lelek on 10/12/2017.
 * dariusz.lelek@gmail.com
 */

public class ColorUtility {
    private static final int pixelsToSkip = 2;

    private final ColorNamePair[] colorDefinitionsMin;
    private final ColorNamePair[] colorDefinitionsMax;

    public ColorUtility(Context context) {
        colorDefinitionsMin = getColorDefinitions(context, R.array.color_min_name, R.array.color_min_value);
        colorDefinitionsMax = getColorDefinitions(context, R.array.color_max_name, R.array.color_max_value);
    }

    private String getColorNameFromRgb(int r, int g, int b, ColorRange colorRange) {
        ColorNamePair closestMatch = null;
        int minMSE = Integer.MAX_VALUE;
        int mse;

        for (ColorNamePair c : getDefinitionsByColorRange(colorRange)) {
            mse = c.computeMSE(r, g, b);
            if (mse < minMSE) {
                minMSE = mse;
                closestMatch = c;
            }
        }

        if (closestMatch != null) {
            return closestMatch.getName();
        } else {
            return "Unknown";
        }
    }

    private ColorNamePair[] getDefinitionsByColorRange(ColorRange colorRange){
        return colorRange == ColorRange.MIN ? colorDefinitionsMin : colorDefinitionsMax;
    }

    public String getColorNameFromColor(int color, ColorRange colorRange) {
        return getColorNameFromRgb(Color.red(color), Color.green(color),
                Color.blue(color), colorRange);
    }

    private class ColorNamePair {
        int r, g, b;
        String name;

        ColorNamePair(String name, int color) {
            this.r = Color.red(color);
            this.g = Color.green(color);
            this.b = Color.blue(color);
            this.name = name;
        }

        int computeMSE(int pixR, int pixG, int pixB) {
            return (((pixR - r) * (pixR - r) + (pixG - g) * (pixG - g) + (pixB - b)
                    * (pixB - b)) / 3);
        }

        String getName() {
            return name;
        }
    }

    public int getAverageColor(Bitmap bitmap){
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

    private ColorNamePair[] getColorDefinitions(Context context, int colorNameArrayR, int colorValueArrayR){
        String[] colorNames = context.getResources().getStringArray(colorNameArrayR);
        String[] colorValues = context.getResources().getStringArray(colorValueArrayR);

        int size = Math.min(colorNames.length, colorValues.length);
        ColorNamePair[] colorDefinitions = new ColorNamePair[size];

        for(int i=0; i<size; i++){
            int colorValue = Color.parseColor(colorValues[i]);
            colorDefinitions[i] = new ColorNamePair(colorNames[i], colorValue);
        }

        return colorDefinitions;
    }

}
