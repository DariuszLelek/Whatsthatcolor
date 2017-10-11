package com.omikronsoft.whatsthatcolor.utility;

import android.graphics.Bitmap;
import android.view.View;

/**
 * Created by Dariusz Lelek on 10/11/2017.
 * dariusz.lelek@gmail.com
 */

public class ViewUtility {

    public static Bitmap getBitmapFromView(View view){
        Bitmap bitmap;
        view.buildDrawingCache();
        bitmap = view.getDrawingCache();
        view.buildDrawingCache(false);
        return bitmap;
    }
}

