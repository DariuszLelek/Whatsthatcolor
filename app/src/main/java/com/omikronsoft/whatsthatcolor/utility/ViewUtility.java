package com.omikronsoft.whatsthatcolor.utility;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.omikronsoft.whatsthatcolor.component.CameraMask;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dariusz Lelek on 10/11/2017.
 * dariusz.lelek@gmail.com
 */

public class ViewUtility {
    private final static Map<View, DateTime> viewLastUpdate = new HashMap<>();

    public static Bitmap getBitmapFromView(View view) {
        Bitmap bitmap;
        view.buildDrawingCache();
        bitmap = view.getDrawingCache();
        view.buildDrawingCache(false);
        return bitmap;
    }

    public static void updateViewWithCameraMaskValue(ImageView view, CameraMask cameraMask, int value) {
        view.setImageBitmap(cameraMask.getMaskBitmap(value));
        viewLastUpdate.put(view, DateTime.now());
    }

    public static boolean canUpdate(View view, int delayMS) {
        if (!viewLastUpdate.containsKey(view)) {
            viewLastUpdate.put(view, DateTime.now());
            return true;
        } else {
            return viewLastUpdate.get(view).plusMillis(delayMS).isBefore(DateTime.now());
        }
    }
}

