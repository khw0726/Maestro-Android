package com.kaist.khw.maestrotest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.ConfirmationOverlay;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.widget.Toast;


public class MainActivity extends WearableActivity implements SensorEventListener{

    /* Layout and filters */
    private LinearLayout mTouchBoard;
    private Button mInsecureSettingButton;
    private Button mStartButton;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private GestureDetector mDetector;

    /*Internal States*/
    private int touchpadMode;
    private int isSwipe = 0;
    /* Consts for internal state*/
    private static final int CONNECTION = 1;
    private static final int START= 2;
    private static final int TOUCHPAD = 3;
    private static final int POINTER = 4;
    private static final int PEN = 5;


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

                mInsecureSettingButton = (Button)findViewById(R.id.Insecure_button);
                mInsecureSettingButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent serverIntent = new Intent(v.getContext(), DeviceListActivity.class);
                        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);

                    }
                });
                mStartButton = (Button)findViewById(R.id.start_button);
                mStartButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //send message to start presentation
                    }
                });

            }
        });

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
        public boolean onSingleTapUp(MotionEvent ev){
            //send message for click
            Log.d("MyGestureListener", "Single Tap");
            return true;
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY){
            sendMessage(distanceX, distanceY);
            Log.d("MyGestureListener", Double.toString(distanceX) + " " + Double.toString(distanceY));
            return true;
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
                setTouchBoard();
                sendMessage(3);

                return true;
            case KeyEvent.KEYCODE_NAVIGATE_PREVIOUS:
                // Do something that advances a user View to the previous item in an ordered list.
                Log.v("fuckk","previous ");
                sendMessage(4);
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
            double aZ = Math.round(ev.values[2] * 100d) / 100d;

            if(Math.abs(ev.values[2]) > 5) {
                String str = "X-axis" + Float.toString(ev.values[0]) + "\nY-axis" + Float.toString(ev.values[1]) + "\nZ-axis" + Float.toString(ev.values[2]);
                Log.v("sensorValue", str);
                if(aZ > 0){
                    if(isSwipe == 0){
                        isSwipe = 1;
                        sendMessage(1);
                        Log.v("next", "next");
                    } else if(isSwipe == 1){
                        Log.v("next ignoreing...", "next ignoreing...");
                    } else if(isSwipe == -1){
                        isSwipe = 2;
                        Log.v("Ignore next", "Ignore next");
                    }
                } else {
                    if(isSwipe == 0){
                        isSwipe = -1;
                        sendMessage(2);
                        Log.v("prev", "prev");
                    } else if (isSwipe == 1){
                        Log.v("Ignore prev", "Ignore prev");
                        isSwipe = 2;
                    }
                }
            } else {
                if(isSwipe == 2){
                    isSwipe = 0;
                    Log.v("Activate", "Activate");
                }
            }
        }
    }

    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this);
  //      mBluetoothAdapter.disable();
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
                mInsecureSettingButton.setVisibility(View.GONE);
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
                   mDetector.onTouchEvent(event);
//                           return v.onTouchEvent(event);
                   return true;
               }
           }
        );
    }

//    public boolean onBoardTouchEvent(MotionEvent ev){
//        int action = MotionEventCompat.getActionMasked(ev);
//
//        switch (action){
//            case (MotionEvent.ACTION_DOWN) :
//                Log.d("touchpad","Action was DOWN");
//                return true;
//
//            case MotionEvent.ACTION_MOVE:
//                Log.d("Touchpad", Float.toString(ev.getX()) + Float.toString(ev.getY()));
//                if(ev.getHistorySize() != 0) {
//                    int numLastHistory = ev.getHistorySize() - 1;
//                    double diffX = ev.getX() - ev.getHistoricalX(numLastHistory);
//                    double diffY = ev.getY() - ev.getHistoricalY(numLastHistory);
//                    sendMessage(diffX, diffY);
//                } else {
//                    sendMessage(ev.getX(), ev.getY());
//                }
//                return true;
//            default:
//                return super.onTouchEvent(ev);
//        }
//    }


}
