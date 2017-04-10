package com.example.qianyiwang.augmentedrealitytest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by qianyiwang on 4/4/17.
 */

public class ArDisplayView2 extends JavaCameraView implements CameraBridgeViewBase.CvCameraViewListener2{

    Mat mRgba, templ;
    Size newSize;
    BaseLoaderCallback baseLoaderCallback;
    final String TAG = "ArDisplayView2";
    Intent broadCastIntent;
    public static final String BROADCAST_ACTION = "boardcast_message";
    Context mContext;

    public ArDisplayView2(final Context context, final int cameraId) {
        super(context, cameraId);

        mContext = context;
        // opencv load successully -> call back
        setCvCameraViewListener(this);
        baseLoaderCallback = new BaseLoaderCallback(context) {
            @Override
            public void onManagerConnected(int status) {
                switch (status){
                    case BaseLoaderCallback.SUCCESS:

                        setVisibility(SurfaceView.VISIBLE);
                        enableView();

                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };

        if(OpenCVLoader.initDebug()){
            Log.d(TAG,"opencv loaded");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS); // if Opencv load successfully
        }
        else{
            Log.d(TAG,"opencv not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, context, baseLoaderCallback); // if not
        }
        broadCastIntent = new Intent(BROADCAST_ACTION);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

        // initial cameraview variables here
        newSize = new Size(120, 60);
        mRgba = new Mat(newSize, CvType.CV_8UC4);
//        templ = Imgcodecs.imread("/sdcard/Pictures/templ.JPG"); // needs to add storage permission in the manifests
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

//        Log.e(TAG, GlobalValues.template_idx+"");

        switch (GlobalValues.template_idx){
            case 0:
                templ = Imgcodecs.imread("/sdcard/Pictures/steeringwheel.JPG");
                GlobalValues.display_message = "STEERING WHEEL";
                break;
            case 1:
                templ = Imgcodecs.imread("/sdcard/Pictures/mcs.JPG");
                GlobalValues.display_message = "MCS";
                break;
            case 2:
                templ = Imgcodecs.imread("/sdcard/Pictures/templ.JPG");
                GlobalValues.display_message = "FORD UM Dearborn Label";
                break;
        }

        drawFeatureMatches(mRgba, templ);
        return mRgba;
    }

    private void drawFeatureMatches(Mat frame, Mat img2){
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);

        //first image
        Mat img1 = frame;
//        Imgproc.cvtColor(img1, img1, CV_8U);
        Mat descriptors1 = new Mat();
        MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
        detector.detect(img1, keypoints1);
        descriptor.compute(img1, keypoints1, descriptors1);

        //second image
//        Mat img2 = Imgcodecs.imread("/sdcard/Pictures/templ.jpg");
        Mat descriptors2 = new Mat();
        MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
        detector.detect(img2, keypoints2);
        descriptor.compute(img2, keypoints2, descriptors2);

        //matcher image descriptors
        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(descriptors1,descriptors2,matches);

        // calculate total number of matches and good matches
        double max_dist = 0;
        double min_dist = 100;

        List<DMatch> matchesList = matches.toList();

        // Quick calculation of max and min distances between keypoints
        try{
            for( int i = 0; i < descriptors2.rows(); i++ ) {
                double dist = matchesList.get(i).distance;
                if( dist < min_dist ) min_dist = dist;
                if( dist > max_dist ) max_dist = dist;
            }

            // calculate good matches
            LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
            for( int i = 0; i < descriptors2.rows(); i++ )
                if( matchesList.get(i).distance <= 3*min_dist ) good_matches.addLast( matchesList.get(i));

            KeyPoint[] kp = keypoints1.toArray();
            double x=0, y=0;
            ArrayList<Double> xList = new ArrayList<>();
            ArrayList<Double> yList = new ArrayList<>();
            for(int i = 0; i<good_matches.size(); i++){
                int idx = good_matches.get(i).queryIdx;
                x = kp[idx].pt.x;
                y = kp[idx].pt.y;
                xList.add(x);
                yList.add(y);
            }
            Collections.sort(xList);
            Collections.sort(yList);

            int xIdx = (int) Math.floor(xList.size()/2);
            int yIdx = (int) Math.floor(yList.size()/2);
            broadCastIntent.putExtra("match_info", good_matches.size()+","+xList.get(xIdx)+","+yList.get(yIdx));
//            broadCastIntent.putExtra("match_info", good_matches.size());
            mContext.sendBroadcast(broadCastIntent);
        }
        catch (Exception e){

        }
    }
}
