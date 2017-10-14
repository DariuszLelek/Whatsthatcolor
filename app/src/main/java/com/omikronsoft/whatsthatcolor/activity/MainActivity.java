package com.omikronsoft.whatsthatcolor.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.omikronsoft.whatsthatcolor.R;
import com.omikronsoft.whatsthatcolor.component.CameraMask;
import com.omikronsoft.whatsthatcolor.utility.color.ColorRange;
import com.omikronsoft.whatsthatcolor.utility.color.ColorUtility;
import com.omikronsoft.whatsthatcolor.utility.ViewUtility;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.flurgle.camerakit.CameraKit.Constants.METHOD_STILL;

/**
 * Created by Dariusz Lelek on 10/11/2017.
 * dariusz.lelek@gmail.com
 */

public class MainActivity extends AppCompatActivity {
    private ImageView imageAverageColor, imageColor, imageMask;
    private CameraView cameraView;
    private CameraMask cameraMask;
    private SeekBar maskSeekBar;
    private ToggleButton toggleButton;
    private TextView textColor;
    private ScheduledExecutorService executor;
    private ColorUtility colorUtility;
    private AdView adView;

    private ColorRange currentColorRange = ColorRange.MAX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();

        colorUtility = new ColorUtility(getApplicationContext());

        imageAverageColor = (ImageView) findViewById(R.id.image_average_color);
        textColor = (TextView) findViewById(R.id.text_color);
        imageMask = (ImageView) findViewById(R.id.image_mask);

        prepareAds();
        prepareItemsWithListeners();

        updateCameraMask();
    }

    private void updateCameraMask() {
        imageMask.post(new Runnable() {
            @Override
            public void run() {
                if(cameraMask == null){
                    int maskColor = ContextCompat.getColor(getApplicationContext(), R.color.color_mask_rectangle);
                    Bitmap cameraMaskBackground = ViewUtility.getBitmapFromView(imageMask);
                    if(cameraMaskBackground != null){
                        cameraMask = new CameraMask(cameraMaskBackground, maskColor);
                        updateMaskWithProgress(maskSeekBar.getProgress());
                    }
                }
            }
        });
    }

    private void prepareAds(){
        LinearLayout add_holder = (LinearLayout) findViewById(R.id.layout_ad_holder);
        add_holder.addView(getAdView());
    }

    private AdView getAdView(){
        if(adView == null){
            MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.app_id));

            RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            adParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

            adView = new AdView(getApplicationContext());
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(getResources().getString(R.string.add_unit_id));
            adView.setBackgroundColor(Color.TRANSPARENT);
            adView.setLayoutParams(adParams);

            // Test Ads
            // AdRequest adRequest = new AdRequest.Builder().addTestDevice(getResources().getString(R.string.test_device_id)).build();
            AdRequest adRequest = new AdRequest.Builder().build();

            adView.loadAd(adRequest);
        }

        return adView;
    }

    private void updateMaskWithProgress(int progress){
        ViewUtility.updateViewWithCameraMaskValue(imageMask, cameraMask, progress);
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

        maskSeekBar = (SeekBar) findViewById(R.id.seek_mask_size);
        prepareMaskSeekBarListener(maskSeekBar);
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

    private void prepareMaskSeekBarListener(SeekBar maskSeekBar) {
        maskSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (ViewUtility.canUpdate(imageMask, 200)) {
                    updateMaskWithProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateMaskWithProgress(seekBar.getProgress());
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
                    startCapture();
                } else {
                    stopCapture();
                }
            }
        });
    }

    private void startCapture(){
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                cameraView.captureImage();
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    private void stopCapture(){
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
        if(adView != null){
            adView.destroy();
        }

        stopCapture();
        toggleButton.setChecked(false);

        cameraView.stop();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
        updateCameraMask();
    }

    @Override
    protected void onPause() {
        stopCapture();
        toggleButton.setChecked(false);

        cameraView.stop();
        super.onPause();
    }
}
