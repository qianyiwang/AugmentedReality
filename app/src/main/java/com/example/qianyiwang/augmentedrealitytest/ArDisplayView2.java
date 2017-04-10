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

    Mat mRgba, templ1, templ2, templ3;
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
        templ1 = Imgcodecs.imread("/sdcard/Pictures/steeringwheel3.JPG");
        templ2 = Imgcodecs.imread("/sdcard/Pictures/mcs3.JPG");
//        templ3 = Imgcodecs.imread("/sdcard/Pictures/templ.JPG");

//        templ1 = Imgcodecs.imread("/sdcard/Download/steeringwheel3.JPG");
//        templ2 = Imgcodecs.imread("/sdcard/Download/mcs3.JPG");

        Mat[] templs = {templ1, templ2};

        MatchInfo[] match_info = drawFeatureMatches(mRgba, templs);

        try{
            if(match_info[0].match_count>=match_info[1].match_count){
                GlobalValues.display_message = "Ford Steering Wheel";
                broadCastIntent.putExtra("match_info", match_info[0].match_idx+","+match_info[0].match_count+","+match_info[0].center_x+","+match_info[0].center_y);
                mContext.sendBroadcast(broadCastIntent);
            }
            else{
                GlobalValues.display_message = "Ford MCS";
                broadCastIntent.putExtra("match_info", match_info[1].match_idx+","+match_info[1].match_count+","+match_info[1].center_x+","+match_info[1].center_y);
                mContext.sendBroadcast(broadCastIntent);
            }
        } catch (Exception e){}
        return mRgba;
    }

    private MatchInfo[] drawFeatureMatches(Mat frame, Mat[] templs){

        int[] matchVal = new int[templs.length];
        MatchInfo[] matchInfos = new MatchInfo[2];

        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);

        //first image
        Mat img1 = frame;
        Mat descriptors1 = new Mat();
        MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
        detector.detect(img1, keypoints1);
        descriptor.compute(img1, keypoints1, descriptors1);

        // Quick calculation of max and min distances between keypoints

        for (int j=0; j<templs.length; j++){

            MatchInfo matchInfo = new MatchInfo();

            //template image
            Mat descriptors2 = new Mat();
            MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
            detector.detect(templs[j], keypoints2);
            descriptor.compute(templs[j], keypoints2, descriptors2);


            //matcher image descriptors
            MatOfDMatch matches = new MatOfDMatch();
            matcher.match(descriptors1,descriptors2,matches);

            // calculate total number of matches and good matches
            double max_dist = 0;
            double min_dist = 100;

            List<DMatch> matchesList = matches.toList();

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

                matchInfo.match_idx = j;
                matchInfo.match_count = good_matches.size();
                matchInfo.center_x = xList.get(xIdx);
                matchInfo.center_y = yList.get(yIdx);
                matchInfos[j] = matchInfo;
            }
            catch (Exception e){
            }
        }

        return matchInfos;
    }

    public class MatchInfo{
        int match_idx;
        int match_count;
        double center_x, center_y;
    }
}
