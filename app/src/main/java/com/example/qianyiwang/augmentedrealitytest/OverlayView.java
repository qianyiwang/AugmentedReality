package com.example.qianyiwang.augmentedrealitytest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

/**
 * Created by qianyiwang on 4/4/17.
 */

public class OverlayView extends View{

    BroadcastReceiver broadcastReceiver;
    String match_info;
    int match_count;
    int match_idx;
    float x,y;

    public OverlayView(Context context) {
        super(context);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                match_info = intent.getStringExtra("match_info");
                match_idx = Integer.parseInt(match_info.split(",")[0]);
                match_count = Integer.parseInt(match_info.split(",")[1]);
                x = Float.parseFloat(match_info.split(",")[2]);
                y = Float.parseFloat(match_info.split(",")[3]);
                invalidate();
            }
        };
        context.registerReceiver(broadcastReceiver, new IntentFilter(ArDisplayView2.BROADCAST_ACTION));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        contentPaint.setTextAlign(Paint.Align.CENTER);
        contentPaint.setTextSize(40);
        contentPaint.setColor(Color.RED);

        Paint focusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        focusPaint.setColor(Color.YELLOW);
        focusPaint.setStrokeWidth(10);

        canvas.drawText(match_idx+", "+match_count+", "+x+", "+y,200, 60, contentPaint);
        if(match_count>90){
            canvas.drawCircle(x, y, 30, focusPaint);
            canvas.drawText(GlobalValues.display_message, x+500-50, y-100-20, contentPaint);
            canvas.drawLine(x, y, x+200, y-100, focusPaint);
            canvas.drawLine(x+200, y-100, x+500, y-100, focusPaint);
        }

    }
}
