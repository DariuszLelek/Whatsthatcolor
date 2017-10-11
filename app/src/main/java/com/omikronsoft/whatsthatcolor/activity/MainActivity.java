package com.omikronsoft.whatsthatcolor.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
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

    // TODO clean the mess in this cass before start working

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PackageManager.PERMISSION_GRANTED);
        }

        cameraView = (CameraView) findViewById(R.id.camera_view);
        cameraView.setMethod(METHOD_STILL);

        imageCamera = (ImageView) findViewById(R.id.image_camera);
        imageMask = (ImageView) findViewById(R.id.image_mask);
        maskSeekBar = (SeekBar) findViewById(R.id.seek_mask_size);

        // TODO handle this max to be same as in CameraMask
        maskSeekBar.setMax(50);


        imageMask.post(new Runnable() {
            @Override
            public void run() {
                cameraMask = new CameraMask(ViewUtility.getBitmapFromView(imageMask));
            }
        });


        Button takePic = (Button) findViewById(R.id.button_take_picture);

        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageMask.setImageBitmap(cameraMask.getMaskBitmap(maskSeekBar.getProgress() / 100F));

                cameraView.captureImage();
            }
        });

        cameraView.setCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(byte[] picture) {

                // test preview
                final Bitmap result = BitmapFactory.decodeByteArray(picture, 0, picture.length);

                int height = result.getHeight();
                int width = result.getWidth();

                final Bitmap crop = Bitmap.createBitmap(result, (width/2) - 30, (height/2) - 30,60, 60);

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
