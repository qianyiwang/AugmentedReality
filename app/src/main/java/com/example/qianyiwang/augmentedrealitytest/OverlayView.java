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
    int match_count;

    public OverlayView(Context context) {
        super(context);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                match_count = intent.getIntExtra("match_info", 0);
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

        canvas.drawText(match_count+"", 60, 60, contentPaint);
        if(match_count>90){
            canvas.drawCircle(canvas.getWidth()/2, canvas.getHeight()/2, 30, focusPaint);
            canvas.drawText(GlobalValues.display_message, canvas.getWidth()*4/5-50, canvas.getHeight()*2/5-20, contentPaint);
            canvas.drawLine(canvas.getWidth()/2, canvas.getHeight()/2, canvas.getWidth()*3/5, canvas.getHeight()*2/5, focusPaint);
            canvas.drawLine(canvas.getWidth()*3/5, canvas.getHeight()*2/5, canvas.getWidth()*4/5, canvas.getHeight()*2/5, focusPaint);
        }
    }
}
