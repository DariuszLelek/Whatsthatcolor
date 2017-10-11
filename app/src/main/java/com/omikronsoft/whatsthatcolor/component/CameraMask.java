package com.omikronsoft.whatsthatcolor.component;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import static android.R.attr.width;

/**
 * Created by Dariusz Lelek on 10/11/2017.
 * dariusz.lelek@gmail.com
 */

public class CameraMask {
    private final static float MAX_MASK_SCALE = 0.5F;
    private final static float MIN_MASK_SCALE = 0.01F;
    private final static float PAINT_STROKE_WIDTH = 1F;
    private final static int PAINT_COLOR = Color.RED;

    private final Paint paint = new Paint();

    private Bitmap cameraMaskBackground;
    private int width, height, width2, height2;

    public CameraMask(Bitmap cameraMaskBackground) {
        this.cameraMaskBackground = cameraMaskBackground;

        preparePaint();
        prepareBackgroundConstants();
    }


    public Bitmap getMaskBitmap(float maskScale){
        return getMaskWithRect(getValidScale(maskScale));
    }

    private Bitmap getMaskWithRect(float scale){
        Bitmap bitmap = Bitmap.createBitmap(cameraMaskBackground);
        Canvas canvas = new Canvas(bitmap);

        // TODO use this(move somewhere else than this class) to crop from original image
        float left = width2 * scale;
        float top = height2 * scale;
        float right = width - left;
        float bottom = height - top;

        canvas.drawLine(left, top, right, top, paint);
        canvas.drawLine(left, bottom, right, bottom, paint);
        canvas.drawLine(left, top, left, bottom, paint);
        canvas.drawLine(right, top, right, bottom, paint);

        //canvas.drawRect(left, top, width - left, height - top, paint);
        return bitmap;
    }

    private float getValidScale(float maskScale){
        return maskScale < MIN_MASK_SCALE ? MIN_MASK_SCALE : (maskScale > MAX_MASK_SCALE ? MAX_MASK_SCALE : maskScale);
    }

    private void prepareBackgroundConstants(){
        width = cameraMaskBackground.getWidth();
        width2 = width/2;

        height = cameraMaskBackground.getHeight();
        height2 = height/2;
    }

    private void preparePaint(){
        paint.setColor(PAINT_COLOR);
        paint.setStrokeWidth(PAINT_STROKE_WIDTH);
    }
}
