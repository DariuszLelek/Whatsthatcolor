package com.omikronsoft.whatsthatcolor.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;
import com.omikronsoft.whatsthatcolor.R;
import com.omikronsoft.whatsthatcolor.component.CameraMask;
import com.omikronsoft.whatsthatcolor.utility.ViewUtility;

import static com.flurgle.camerakit.CameraKit.Constants.METHOD_STILL;

public class MainActivity extends AppCompatActivity {
    private ImageView imageCamera, imageMask;
    private CameraView cameraView;
    private CameraMask cameraMask;
    private SeekBar maskSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();

        imageCamera = (ImageView) findViewById(R.id.image_camera);
        imageMask = (ImageView) findViewById(R.id.image_mask);
        maskSeekBar = (SeekBar) findViewById(R.id.seek_mask_size);
        cameraView = (CameraView) findViewById(R.id.camera_view);

        cameraView.setMethod(METHOD_STILL);
        imageMask.post(new Runnable() {
            @Override
            public void run() {
                cameraMask = new CameraMask(ViewUtility.getBitmapFromView(imageMask));
            }
        });

        prepareCameraViewListener(cameraView);
        prepareSeekBars();
        setSeekBarDefaultPositions();

        // TODO remove that after testing
        prepareTestButton();
    }

    private void prepareTestButton(){
        Button testButton = (Button)findViewById(R.id.button_take_picture);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.captureImage();
            }
        });
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

    private void prepareSeekBars(){
        maskSeekBar.setMax((CameraMask.MAX_MASK_SCALE_PERCENT));
        maskSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(ViewUtility.canUpdate(imageMask, 200)){
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
    }

    private void setSeekBarDefaultPositions(){
        maskSeekBar.post(new Runnable() {
            @Override
            public void run() {
                maskSeekBar.setProgress(maskSeekBar.getMax()/2);
            }
        });
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

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageCamera.setImageBitmap(crop);
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
        cameraView.stop();
        super.onPause();
    }
}
