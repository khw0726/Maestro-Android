package com.kaist.khw.maestrotest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;

public class TouchpadActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touchpad);
//        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
//        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
//            @Override
//            public void onLayoutInflated(WatchViewStub stub) {
//                mTextView = (TextView) stub.findViewById(R.id.text);
//            }
//        });
    }
     public boolean onTouchEvent(MotionEvent ev){
        int action = MotionEventCompat.getActionMasked(ev);

         switch (action){
             case MotionEvent.ACTION_MOVE:
                 Log.d("Touchpad", Float.toString(ev.getX()) + Float.toString(ev.getY()));
                 return true;
             default:
                 return super.onTouchEvent(ev);
         }
     }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_NAVIGATE_NEXT:
                finish();
                return true;
            case KeyEvent.KEYCODE_NAVIGATE_PREVIOUS:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }



}
