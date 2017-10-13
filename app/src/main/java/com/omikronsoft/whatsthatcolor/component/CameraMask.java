package com.omikronsoft.whatsthatcolor.component;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Created by Dariusz Lelek on 10/11/2017.
 * dariusz.lelek@gmail.com
 */

public class CameraMask {
    public final static int MAX_MASK_SCALE_PERCENT = 50;
    private final static int MIN_MASK_SCALE_PERCENT = 1;
    private final static float PAINT_STROKE_WIDTH = 4F;

    private final Paint paint = new Paint();

    private RectF currentMaskRect;

    private final int paintColor;
    private final Bitmap cameraMaskBackground;
    private int width, height, width2, height2;

    public CameraMask(Bitmap cameraMaskBackground, int paintColor) {
        this.cameraMaskBackground = cameraMaskBackground;
        this.paintColor = paintColor;

        preparePaint();
        prepareBackgroundConstants();
    }

    public RectF getCurrentMaskRect() {
        return currentMaskRect;
    }

    public Bitmap getMaskBitmap(int maskScale) {
        return getBitmapWithMask(getValidScale(maskScale));
    }

    private Bitmap getBitmapWithMask(int scale) {
        Bitmap bitmap = Bitmap.createBitmap(cameraMaskBackground);
        Canvas canvas = new Canvas(bitmap);

        float left = width2 - width2 * scale / 100F;
        float top = height2 - height2 * scale / 100F;
        float right = width - left;
        float bottom = height - top;

        currentMaskRect = new RectF(left, top, right, bottom);

        canvas.drawLine(left, top, right, top, paint);
        canvas.drawLine(left, bottom, right, bottom, paint);
        canvas.drawLine(left, top, left, bottom, paint);
        canvas.drawLine(right, top, right, bottom, paint);

        return bitmap;
    }

    private int getValidScale(int maskScale) {
        return maskScale < MIN_MASK_SCALE_PERCENT ? MIN_MASK_SCALE_PERCENT : (maskScale > MAX_MASK_SCALE_PERCENT ? MAX_MASK_SCALE_PERCENT : maskScale);
    }

    private void prepareBackgroundConstants() {
        width = cameraMaskBackground.getWidth();
        width2 = width / 2;

        height = cameraMaskBackground.getHeight();
        height2 = height / 2;
    }

    private void preparePaint() {
        paint.setColor(paintColor);
        paint.setStrokeWidth(PAINT_STROKE_WIDTH);
    }
}
