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
import android.view.OrientationEventListener;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorManager;


public class MainActivity extends Activity implements SensorEventListener{

    private TextView mTextView;
    private SensorManager mSensorManager;
    private Sensor mSensor;//, mAccSensor, mMagSensor, mRotVecSensor;
//    private final float[] rotationMatrix = new float[9];
//    private final float[] orientationAngles = new float[3];
//    private final float[] accelerometerReading = new float[3];
//    private final float[] magnetometerReading = new float[3];

    private int angle;
    private boolean isSwipe = false;
    private OrientationEventListener oel;
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
//        mRotSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
//        mRotVecSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
//        Log.v("mRotVec", mRotVecSensor == null ? "true": "false");
//        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        mMagSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

//        oel = new OrientationEventListener(getApplicationContext()) {
//            @Override
//            public void onOrientationChanged(int orientation) {
//                Log.v("Orient", Integer.toString(orientation));
//                angle = orientation; //(orientation == -1) ? 90 : orientation - 180;
//            }
//        };


    }
    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
//        oel.enable();
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
    }

    @Override
    public final void onSensorChanged(SensorEvent ev){
        if(ev.sensor == mSensor) {
            if(Math.abs(ev.values[2]) > 5) {
                String str = "X-axis" + Float.toString(ev.values[0]) + "\nY-axis" + Float.toString(ev.values[1]) + "\nZ-axis" + Float.toString(ev.values[2]);
                Log.v("Fuck", str);
            }
        }
    }
//    public void updateOrientationAngles(){
////        mSensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
////        mSensorManager.getOrientation(rotationMatrix, orientationAngles);
//        String str = "X-angle " + Float.toString(orientationAngles[0]) + "Y-angle " + Float.toString(orientationAngles[1]) +"Z-angle " + Float.toString(orientationAngles[2]);
//        Log.v("orientation", str);
//    }

    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
//        oel.disable();
    }
}
