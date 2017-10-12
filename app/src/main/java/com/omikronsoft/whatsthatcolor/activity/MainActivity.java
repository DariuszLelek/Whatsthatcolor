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
import android.widget.ToggleButton;

import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;
import com.omikronsoft.whatsthatcolor.R;
import com.omikronsoft.whatsthatcolor.component.CameraMask;
import com.omikronsoft.whatsthatcolor.utility.ColorUtility;
import com.omikronsoft.whatsthatcolor.utility.ViewUtility;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.flurgle.camerakit.CameraKit.Constants.METHOD_STILL;

public class MainActivity extends AppCompatActivity {
    private ImageView imageAverageColor;
    private ImageView imageMask;
    private CameraView cameraView;
    private CameraMask cameraMask;
    private SeekBar maskSeekBar;
    private ToggleButton toggleButton;

    private final int cameraMaskUpdateDelayMS = 200;
    private final int cameraCapturePictureDelayMS = 500;
    private ScheduledExecutorService executor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();

        imageAverageColor = (ImageView) findViewById(R.id.image_average_color);
        imageMask = (ImageView) findViewById(R.id.image_mask);
        maskSeekBar = (SeekBar) findViewById(R.id.seek_mask_size);
        cameraView = (CameraView) findViewById(R.id.camera_view);

        ImageView imageClose = (ImageView) findViewById(R.id.image_close);
        toggleButton = (ToggleButton) findViewById(R.id.toggle_button);

        cameraView.setMethod(METHOD_STILL);
        imageMask.post(new Runnable() {
            @Override
            public void run() {
                int maskColor = ContextCompat.getColor(getApplicationContext(), R.color.color_mask_rectangle);
                cameraMask = new CameraMask(ViewUtility.getBitmapFromView(imageMask), maskColor);
            }
        });

        prepareImageCloseListener(imageClose);
        prepareToggleButtonListener(toggleButton);
        prepareCameraViewListener(cameraView);
        prepareSeekBar();
    }

    private void requestPermissions(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PackageManager.PERMISSION_GRANTED);
        }
    }

    private void prepareSeekBar(){
        maskSeekBar.setMax((CameraMask.MAX_MASK_SCALE_PERCENT));
        maskSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(ViewUtility.canUpdate(imageMask, cameraMaskUpdateDelayMS)){
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

    private void setSeekBarDefaultPosition(){
        maskSeekBar.post(new Runnable() {
            @Override
            public void run() {
                maskSeekBar.setProgress(maskSeekBar.getMax()/2);
            }
        });
    }

    private void prepareImageCloseListener(ImageView view){
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shutDownExecutor();
                cameraView.stop();
                finish();
            }
        });
    }

    private void prepareToggleButtonListener(ToggleButton toggleButton){
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    executor = Executors.newScheduledThreadPool(1);
                    executor.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            cameraView.captureImage();
                        }
                    }, 0, cameraCapturePictureDelayMS, TimeUnit.MILLISECONDS);
                }else{
                    shutDownExecutor();
                }
            }
        });
    }

    private void shutDownExecutor(){
        if(executor != null && !executor.isShutdown()){
            executor.shutdownNow();
        }
    }

    private void prepareCameraViewListener(CameraView cameraView){
        cameraView.setCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] picture) {
                final Bitmap result = BitmapFactory.decodeByteArray(picture, 0, picture.length);
                RectF maskRect = cameraMask.getCurrentMaskRect();

                int height = result.getHeight();
                int width = result.getWidth();
                int mWidth = (int)maskRect.width();
                int mHeight = (int)maskRect.height();

                final Bitmap crop = Bitmap.createBitmap(result, (width/2) - mWidth/2, (height/2) - mHeight/2, mWidth, mHeight);
                final Bitmap colorBitmap =  Bitmap.createBitmap(imageAverageColor.getWidth(), imageAverageColor.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(colorBitmap);
                canvas.drawColor(ColorUtility.getAverageColor(crop));

                crop.recycle();
                result.recycle();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageAverageColor.setImageBitmap(colorBitmap);
                    }
                });
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        toggleButton.setChecked(false);
        shutDownExecutor();

        cameraView.stop();
        super.onPause();
    }
}
