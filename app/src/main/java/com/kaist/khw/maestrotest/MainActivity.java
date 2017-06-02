package com.kaist.khw.maestrotest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.widget.Toast;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends WearableActivity implements SensorEventListener{

    /* Layout and filters */
    private LinearLayout mTouchBoard;
    private Button mInsecureSettingButton;
    private Button mStartButton;
    private Button mExitButton;
    private TextView mTimerView;
    private TextView mModeTextView;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private GestureDetector mDetector;
    private TimerTask updateTimerText;
    private Timer mTimer;
    private int sensorCount = 5;
    long elapsedSeconds;
    private double[] accHistory;


    /*Internal States*/
    private int touchpadMode;
    private int isSwipe = 0;
    private boolean isScrolling = false;
    /* Consts for internal state*/
    private static final int CONNECTION = 1;
    private static final int START= 2;
    private static final int TOUCHPAD = 3;
    private static final int POINTER = 4;
    private static final int PEN = 5;
    private static final int TIMER = 6;


    /* Bluetooh Setting */
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        setAmbientEnabled();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                mTouchBoard = (LinearLayout)stub.findViewById(R.id.touchboard2);
                mDetector = new GestureDetector(mTouchBoard.getContext(), new MyGestureListener());

                mTimerView = (TextView) findViewById(R.id.timer_text);
                mTimerView.setVisibility(View.GONE);
                mModeTextView = (TextView) findViewById(R.id.mode_text);
                mModeTextView.setVisibility(View.GONE);
                mInsecureSettingButton = (Button)findViewById(R.id.Insecure_button);
                mInsecureSettingButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent serverIntent = new Intent(v.getContext(), DeviceListActivity.class);
                        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);

                    }
                });

                mExitButton = (Button)findViewById(R.id.exit_button);
                mExitButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                mStartButton = (Button)findViewById(R.id.start_button);
                mStartButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //send message to start presentation
                        mTimerView.setVisibility(View.VISIBLE);
                        mStartButton.setVisibility(View.GONE);
                        mInsecureSettingButton.setVisibility(View.GONE);
                        mExitButton.setVisibility(View.GONE);
                        touchpadMode = TIMER;
                        elapsedSeconds = 0;
                        sendMessage("S");
                        mTimer.schedule(updateTimerText, 0, 1000);
                    }
                });
                mStartButton.setVisibility(View.GONE);


            }
        });
        touchpadMode = 1;
        accHistory = new double[5];
        mTimer = new Timer();
        updateTimerText = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        elapsedSeconds += 1;
                        String timeStr = DateUtils.formatElapsedTime(elapsedSeconds);
                        mTimerView.setText(timeStr);
                    }
                });
            }
        };
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent ev){
            return true;
        }
        @Override
        public boolean onSingleTapConfirmed(MotionEvent ev){
            //send message for click
            Log.d("MyGestureListener", "Single Tap");

            sendMessage("C");
            return true;
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY){
            isScrolling = true;
            sendMessage(distanceX, distanceY);
            Log.d("MyGestureListener", Double.toString(distanceX) + " " + Double.toString(distanceY));
            return true;
        }

//        public boolean onDoubleTapEvent(MotionEvent ev){
//            if(ev.getActionMasked() == MotionEvent.ACTION_UP){
//
////                return true;
//            } else if(ev.getActionMasked() == MotionEvent.ACTION_MOVE){
//                Log.v("TapandDragHistory", Integer.toString(ev.getHistorySize()));
//                if(touchpadMode == TOUCHPAD){
//                    isScrolling = true;
//                    if(ev.getHistorySize() == 0) {
//                        sendMessage("Z");
//                        Log.d("tapanddrag", "start");
//                    }
//                    else {
//                        int historySize = ev.getHistorySize() - 1;
//                        double diffX = ev.getHistoricalX(historySize) - ev.getX();
//                        double diffY = ev.getHistoricalY(historySize) - ev.getY();
//                        sendMessage(diffX, diffY);
//                        Log.d("tapanddrag", "moving");
//                    }
//                }
////                return true;
//            }
//            return true;
//        }

        public boolean onDoubleTap(MotionEvent ev){
            switch(touchpadMode) {
                case 4:
                    isScrolling = false;
                    mModeTextView.setText("TOUCHPAD");
                    touchpadMode--;
                    break;
                case 5:
                    isScrolling = false;
                    mModeTextView.setText("TOUCHPAD");
                    touchpadMode = 3;
                    break;
                case 3:
                    touchpadMode++;
                    mModeTextView.setText("POINTER");
                    break;
//                default:
                    //Should not be here
//                        return true;
            }
            sendMessage(touchpadMode);
            return true;

        }

        @Override
        public void onLongPress(MotionEvent ev){
            if(!isScrolling)
                finish();
            Log.v("LongPress", "fuck");
            return;
        }


    }
    protected void onResume(){
        super.onResume();
 //       mBluetoothAdapter.enable();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        isSwipe = 0;
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_NAVIGATE_NEXT:
                // Do something that advances a user View to the next item in an ordered list.
                mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
                sendMessage(6);
                mTimerView.setVisibility(View.VISIBLE);
                mModeTextView.setVisibility(View.GONE);
                touchpadMode = 6;
                sensorCount = 5;
                return true;
            case KeyEvent.KEYCODE_NAVIGATE_PREVIOUS:
                // Do something that advances a user View to the previous item in an ordered list.
//                Log.v("fuckk","previous ");
                mSensorManager.unregisterListener(this);
                if(touchpadMode == 6){
                    setTouchBoard();
                    mTimerView.setVisibility(View.GONE);
                    mModeTextView.setVisibility(View.VISIBLE);
                    mModeTextView.setText("TOUCHPAD");
                    touchpadMode = 3;
                    sendMessage(3);
                }
                return true;
        }
        // If you did not handle it, let it be handled by the next possible element as deemed by the Activity.
        Log.v("keycode..?", Integer.toString(keyCode));
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy){
    }

    @Override
    public final void onSensorChanged(SensorEvent ev){
        if(ev.sensor == mSensor) {
//            double aY = Math.round(ev.values[1] * 100d) / 100d;
//            double aZ = Math.round(ev.values[2] * 100d) / 100d;
//            accHistory[4] = accHistory[3];
//            accHistory[3] = accHistory[2];
//            accHistory[2] = accHistory[1];
//            accHistory[1] = accHistory[0];
//            accHistory[0] = ev.values[2];
//
//            double accSum = 0;
//            for (double acc : accHistory){
//                Log.v("accHistory", Double.toString(acc));
//                accSum += acc;
//            }
//            Log.v("accHistoryEnd", "end");
//            if(Math.abs(ev.values[2]) > 4 && Math.abs(ev.values[2]) < 10) {
            sensorCount++;
            if(Math.abs(ev.values[2]) > 4 && Math.abs(ev.values[2]) < 10 && sensorCount >5) {
                String str = "X-axis" + Float.toString(ev.values[0]) + "\nY-axis" + Float.toString(ev.values[1]) + "\nZ-axis" + Float.toString(ev.values[2]);
                if (ev.values[2] > 0) {
//                    if(isSwipe == 0){
//                        isSwipe = 1;
//                        sendMessage(1);
//                        Log.v("prev", "prev");
//                        Log.v("sensorValue", str);
//
//                    } else if(isSwipe == 1){
//                        Log.v("prev ignoring...", "prev ignoring...");
////                        Log.v("sensorValue", str);
//
//                    } else if(isSwipe == -1){
//                        isSwipe = 2;
//                        Log.v("Ignore prev", "Ignore prev");
////                        Log.v("sensorValue", str);
//
//                    }
                    sendMessage(1);
                    sensorCount = 0;
                } else {
//                    if(isSwipe == 0){
//                        isSwipe = -1;
//                        sendMessage(2);
//                        Log.v("next", "next");
//                        Log.v("sensorValue", str);
//
//                    } else if (isSwipe == 1){
//                        Log.v("Ignore next", "Ignore next");
////                        Log.v("sensorValue", str);
//
//                        isSwipe = 2;
//                    } else if(isSwipe == -1){
//                        Log.v("next ignoring...", "next ignoring...");
////                        Log.v("sensorValue", str);
//
//                    }
                    sendMessage(2);
                    sensorCount = 0;
                }
            }
//            } else if(Math.abs(ev.values[2]) < 1){
//                if(isSwipe == 2){
//                    isSwipe = 0;
//                    Log.v("Activate", "Activate");
//                }
//            }
        }
    }

    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
  //      mBluetoothAdapter.disable();
    }

    protected void onDestroy(){
        super.onDestroy();
        sendMessage("D");
        mChatService.stop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);
        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    private void sendMessage(String msg) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        msg += "\n";
        // Check that there's actually something to send
        mChatService.write(msg.getBytes());
    }
    private void sendMessage(double dX, double dY){
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        String str = "M" + String.valueOf(dX) + "," + String.valueOf(dY) + "\n";
        mChatService.write(str.getBytes());
    }
    private void sendMessage(int msgCode) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        String code = String.valueOf(msgCode) + "\n";
        // Check that there's actually something to send
        mChatService.write(code.getBytes());
    }
    private final void setStatus(CharSequence subTitle) {
//        final ActionBar actionBar = getActionBar();
//        actionBar.setSubtitle(subTitle);
    }
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus("connected to sth");
                            mInsecureSettingButton.setVisibility(View.GONE);
                            mStartButton.setVisibility(View.VISIBLE);
                            touchpadMode = START;
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus("connecting");
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
//                    setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer

                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Toast.makeText(getApplicationContext(),readMessage,Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                /* Connect µÉ¶§ Mouse point °¡Á®¿À±â  */


                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        Log.v(TAG,"result code  = " + requestCode);
        switch (requestCode) {

            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);

                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }


    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    public void setTouchBoard()
    {
        mTouchBoard.setOnTouchListener(new View.OnTouchListener()
           {
               @Override
               public boolean onTouch(View v, MotionEvent event) {
                   if(event.getPointerCount() > 1){
                       Log.d("MutliTouct!!", "func");
                   }
                   if(event.getPointerCount() > 1 && (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP ) ){
                       if(touchpadMode == TOUCHPAD){
                           touchpadMode = PEN;
                           mModeTextView.setText("PEN");
                           sendMessage("Z");
                       }
                       return true;
                   }

                   if(mDetector.onTouchEvent(event)){
                       return true;
                   }

                   if(touchpadMode == PEN && isScrolling && event.getAction() == MotionEvent.ACTION_UP) {
                       touchpadMode = TOUCHPAD;
                       mModeTextView.setText("TOUCHPAD");
                       isScrolling  = false;
                       sendMessage("X");
                   }
                   return false;
               }

//                   mDetector.onTouchEvent(event);
//                   return true;
           }
        );
    }



}
