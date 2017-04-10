package com.example.qianyiwang.augmentedrealitytest;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import org.opencv.android.JavaCameraView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.imgcodecs.Imgcodecs;

public class MainActivity extends AppCompatActivity{

    final String TAG = "MainActivity";
    BaseLoaderCallback baseLoaderCallback;
    ArDisplayView2 arDisp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout arViewPane = (FrameLayout)findViewById(R.id.container);

        arDisp = new ArDisplayView2(getApplicationContext(), 0);

        arViewPane.addView(arDisp);

        OverlayView overlayContent = new OverlayView(getApplicationContext());
        arViewPane.addView(overlayContent);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(arDisp!=null){
            arDisp.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(arDisp!=null){
            arDisp.disableView();
        }
    }
}
