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

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    final String TAG = "MainActivity";
    BaseLoaderCallback baseLoaderCallback;
    ArDisplayView2 arDisp;

    Button steering_button, mcs_button, label_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout arViewPane = (FrameLayout)findViewById(R.id.container);

        arDisp = new ArDisplayView2(getApplicationContext(), 0);

//        // opencv load successully -> call back
//        baseLoaderCallback = new BaseLoaderCallback(this) {
//            @Override
//            public void onManagerConnected(int status) {
//                switch (status){
//                    case BaseLoaderCallback.SUCCESS:
//
//                        arDisp.setVisibility(SurfaceView.VISIBLE);
//                        arDisp.enableView();
//                        arDisp.setCvCameraViewListener();
//                        break;
//                    default:
//                        super.onManagerConnected(status);
//                        break;
//                }
//            }
//        };
        arViewPane.addView(arDisp);

        OverlayView overlayContent = new OverlayView(getApplicationContext());
        arViewPane.addView(overlayContent);

        steering_button = (Button)findViewById(R.id.steering_button);
        steering_button.setOnClickListener(this);
        mcs_button = (Button)findViewById(R.id.mcs_button);
        mcs_button.setOnClickListener(this);
        label_button = (Button)findViewById(R.id.label_button);
        label_button.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if(OpenCVLoader.initDebug()){
//            Log.d(TAG,"opencv loaded");
//            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS); // if Opencv load successfully
//        }
//        else{
//            Log.d(TAG,"opencv not loaded");
//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, baseLoaderCallback); // if not
//        }
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.steering_button:
                GlobalValues.template_idx = 0;
                break;
            case R.id.mcs_button:
                GlobalValues.template_idx = 1;
                break;
            case R.id.label_button:
                GlobalValues.template_idx = 2;
                break;
        }
    }
}
