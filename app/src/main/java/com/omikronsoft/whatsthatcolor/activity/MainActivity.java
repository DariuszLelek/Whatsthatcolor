package com.omikronsoft.whatsthatcolor.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;
import com.omikronsoft.whatsthatcolor.R;
import com.omikronsoft.whatsthatcolor.component.CameraMask;
import com.omikronsoft.whatsthatcolor.utility.color.ColorRange;
import com.omikronsoft.whatsthatcolor.utility.color.ColorUtility;
import com.omikronsoft.whatsthatcolor.utility.ViewUtility;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.flurgle.camerakit.CameraKit.Constants.METHOD_STILL;

public class MainActivity extends AppCompatActivity {
    private ImageView imageAverageColor, imageColor, imageMask;
    private CameraView cameraView;
    private CameraMask cameraMask;
    private SeekBar maskSeekBar;
    private ToggleButton toggleButton;
    private TextView textColor;

    private final int cameraMaskUpdateDelayMS = 200;
    private final int cameraCapturePictureDelayMS = 500;

    private ScheduledExecutorService executor;
    private ColorUtility colorUtility;

    private ColorRange currentColorRange = ColorRange.MAX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();

        colorUtility = new ColorUtility(getApplicationContext());

        imageAverageColor = (ImageView) findViewById(R.id.image_average_color);
        maskSeekBar = (SeekBar) findViewById(R.id.seek_mask_size);
        textColor = (TextView) findViewById(R.id.text_color);

        prepareImageMask();
        prepareItemsWithListeners();
        prepareSeekBar();
    }

    private void prepareImageMask() {
        imageMask = (ImageView) findViewById(R.id.image_mask);
        imageMask.post(new Runnable() {
            @Override
            public void run() {
                int maskColor = ContextCompat.getColor(getApplicationContext(), R.color.color_mask_rectangle);
                cameraMask = new CameraMask(ViewUtility.getBitmapFromView(imageMask), maskColor);
            }
        });
    }

    private void prepareItemsWithListeners() {
        prepareImageCloseListener((ImageView) findViewById(R.id.image_close));

        imageColor = (ImageView) findViewById(R.id.image_color);
        prepareImageColorListener(imageColor);

        toggleButton = (ToggleButton) findViewById(R.id.toggle_button);
        prepareToggleButtonListener(toggleButton);

        cameraView = (CameraView) findViewById(R.id.camera_view);
        cameraView.setMethod(METHOD_STILL);
        prepareCameraViewListener(cameraView);
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PackageManager.PERMISSION_GRANTED);
        }
    }

    private void prepareSeekBar() {
        maskSeekBar.setMax((CameraMask.MAX_MASK_SCALE_PERCENT));
        maskSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (ViewUtility.canUpdate(imageMask, cameraMaskUpdateDelayMS)) {
                    ViewUtility.updateViewWithCameraMaskValue(imageMask, cameraMask, progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ViewUtility.updateViewWithCameraMaskValue(imageMask, cameraMask, maskSeekBar.getProgress());
            }
        });

        setSeekBarDefaultPosition();
    }

    private void setSeekBarDefaultPosition() {
        maskSeekBar.post(new Runnable() {
            @Override
            public void run() {
                maskSeekBar.setProgress(maskSeekBar.getMax() / 2);
            }
        });
    }

    private void flipCurrentColorRange() {
        currentColorRange = currentColorRange == ColorRange.MAX ? ColorRange.MIN : ColorRange.MAX;
    }

    private int getCurrentColorRangeMipmap() {
        return currentColorRange == ColorRange.MAX ? R.mipmap.color_max : R.mipmap.color_min;
    }

    private void prepareImageColorListener(ImageView view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipCurrentColorRange();
                imageColor.setImageResource(getCurrentColorRangeMipmap());
            }
        });
    }

    private void prepareImageCloseListener(ImageView view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });
    }

    private void prepareToggleButtonListener(ToggleButton toggleButton) {
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    executor = Executors.newScheduledThreadPool(1);
                    executor.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            cameraView.captureImage();
                        }
                    }, 0, cameraCapturePictureDelayMS, TimeUnit.MILLISECONDS);
                } else {
                    shutDownExecutor();
                }
            }
        });
    }

    private void shutDownExecutor() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    private void prepareCameraViewListener(CameraView cameraView) {
        cameraView.setCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] picture) {
                final Bitmap cropBitmap = getCropBitmap(picture);
                final Bitmap colorBitmap = getPlainColorBitmap();
                final Canvas canvas = new Canvas(colorBitmap);
                int avgColor = colorUtility.getAverageColor(cropBitmap);
                final String colorName = colorUtility.getColorNameFromColor(avgColor, currentColorRange);

                canvas.drawColor(avgColor);

                setColorInfo(colorName, colorBitmap);
            }
        });
    }

    private Bitmap getPlainColorBitmap() {
        int width = imageAverageColor.getWidth();
        int height = imageAverageColor.getHeight();

        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    private void setColorInfo(final String colorName, final Bitmap colorBitmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textColor.setText(colorName);
                imageAverageColor.setImageBitmap(colorBitmap);
            }
        });
    }

    private Bitmap getCropBitmap(byte[] picture) {
        final Bitmap result = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        final RectF maskRect = cameraMask.getCurrentMaskRect();

        int height = result.getHeight();
        int width = result.getWidth();
        int mWidth = (int) maskRect.width();
        int mHeight = (int) maskRect.height();

        return Bitmap.createBitmap(result, (width / 2) - mWidth / 2, (height / 2) - mHeight / 2, mWidth, mHeight);
    }

    private void finishActivity(){
        toggleButton.setChecked(false);
        shutDownExecutor();
        cameraView.stop();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        // TODO check if can be done without finishing activity (got problems with on resume otherwise)
        finishActivity();
        super.onPause();
    }
}
