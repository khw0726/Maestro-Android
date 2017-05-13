package com.kaist.khw.maestrotest;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.support.wearable.view.ConfirmationOverlay;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorManager;


public class MainActivity extends Activity implements SensorEventListener{

    private TextView mTextView;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_NAVIGATE_NEXT:
                // Do something that advances a user View to the next item in an ordered list.
                Log.v("fuckk","nextt");
                return true;
            case KeyEvent.KEYCODE_NAVIGATE_PREVIOUS:
                // Do something that advances a user View to the previous item in an ordered list.
                Log.v("fuckk","previous ");
                return true;
        }
        // If you did not handle it, let it be handled by the next possible element as deemed by the Activity.
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy){
        return;
    }

    @Override
    public final void onSensorChanged(SensorEvent ev){
        String str = "X-axis" + Float.toString(ev.values[0]) + "\nY-axis" + Float.toString(ev.values[1]) + "\nZ-axis" + Float.toString(ev.values[2]);
        //Log.v("Fuck", str);
        mTextView.setText(str);
        return;
    }
}
