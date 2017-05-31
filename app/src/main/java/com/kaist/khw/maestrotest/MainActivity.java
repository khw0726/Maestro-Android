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
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.ConfirmationOverlay;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.widget.Toast;


public class MainActivity extends WearableActivity implements SensorEventListener{

    private TextView mTextView;
    private Button mInsecureSettingButton;
    private Button mDisconnectButton;
    private Button mSecureSettingButton;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private boolean isPointer;
//    private double dY, dZ;
//    private double vY, vZ;
//    private long timestamp;
    private int isSwipe = 0;
//    private int isYAcc = 0;
//    private int isZAcc = 0;
//    private OrientationEventListener oel;

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

                mTextView = (TextView) stub.findViewById(R.id.text);
                mInsecureSettingButton = (Button)findViewById(R.id.Insecure_button);
                mInsecureSettingButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent serverIntent = new Intent(v.getContext(), DeviceListActivity.class);
                        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);

                    }
                });
                mSecureSettingButton = (Button)findViewById(R.id.secure_button);
                mSecureSettingButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent serverIntent = new Intent(v.getContext(), DeviceListActivity.class);
                        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);


                    }
                });
                mDisconnectButton = (Button)findViewById(R.id.disconnect_button);
                mDisconnectButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ensureDiscoverable();
                    }
                });

            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }
    protected void onResume(){
        super.onResume();
 //       mBluetoothAdapter.enable();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
//        vY = 0;
//        vZ = 0;
//        dY = 0;
//        dZ = 0;
        isSwipe = 0;
        isPointer = false;
//        oel.enable();
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_NAVIGATE_NEXT:
                // Do something that advances a user View to the next item in an ordered list.
//                Log.v("fuckk","nextt");
                sendMessage(3);
                Intent intent = new Intent(this,TouchpadActivity.class);
                startActivity(intent);
//                if(!isPointer){
//                    mSensorManager.unregisterListener(this);
//                    mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
//                } else {
//                    mSensorManager.unregisterListener(this);
//                    mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
//                }

//                vY = 0;
//                vZ = 0;
//                dY = 0;
//                dZ = 0;
//                timestamp = System.nanoTime();
                isPointer = false;

                return true;
            case KeyEvent.KEYCODE_NAVIGATE_PREVIOUS:
                // Do something that advances a user View to the previous item in an ordered list.
                Log.v("fuckk","previous ");
                sendMessage(4);
                isPointer = true;
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
            double aY = Math.round(ev.values[1] * 100d) / 100d;
            double aZ = Math.round(ev.values[2] * 100d) / 100d;
//            if(isPointer){
//                long elaspedTime = (ev.timestamp - timestamp);
//                Log.v("time", Double.toString(elaspedTime));
//                timestamp = ev.timestamp;
//                vY += aY * elaspedTime;
//                vZ += aZ * elaspedTime;
//
//                dY = vY * elaspedTime;
//                dZ = vZ * elaspedTime;
//                if(Math.abs(aZ) > 1){
//                    if(aZ > 0){
//                        if(isZAcc == 0){
//                            isZAcc = 1;
//                        } else if(isZAcc == 1){
//                            isZAcc = 1;
//                        } else if(isZAcc == -1){
//                            isZAcc = 2;
//                            aZ = 0;
//                        } else if(isZAcc == 2){
//                            aZ = 0;
//                        }
//                    } else {
//                        if(isZAcc == 0){
//                            isZAcc = -1;
//                        } else if(isZAcc == -1){
//                            isZAcc = -1;
//                        } else if(isZAcc == 1){
//                            isZAcc = 2;
//                            aZ = 0;
//                        } else if(isZAcc == 2){
//                            aZ = 0;
//                        }
//                    }
//                } else {
//                    if(isZAcc == 2) {
//                        isZAcc = 0;
//                        aZ = 0;
//                    }
//                }
//                if(Math.abs(aY) > 1){
//                    if(aY > 0){
//                        if(isYAcc == 0){
//                            isYAcc = 1;
//                        } else if(isYAcc == 1){
//                            isYAcc = 1;
//                        } else if(isYAcc == -1){
//                            isYAcc = 2;
//                            aY = 0;
//                        } else if(isYAcc == 2){
//                            aY = 0;
//                        }
//                    } else {
//                        if(isYAcc == 0){
//                            isYAcc = -1;
//                        } else if(isYAcc == -1){
//                            isYAcc = -1;
//                        } else if(isYAcc == 1){
//                            isYAcc = 2;
//                            aY = 0;
//                        } else if(isYAcc == 2){
//                            aY = 0;
//                        }
//                    }
//                } else {
//                    if(isYAcc == 2){
//                        aY = 0;
//                        isYAcc = 0;
//                    }
//                }
//                if(aZ > 0 && aY > 0){
//                    if(isZAcc == -1){
//                        isZAcc = 2;
//                        aZ = 0;
//                    }
//                    if(isYAcc == -1){
//                        isYAcc = 2;
//                        aY = 0;
//                    }
//                    sendMessage(aY, aZ, elaspedTime);
//                }
//                sendMessage(aY, aZ, elaspedTime);
//                Log.v("ACC", "aY " + Double.toString(aY) + " aZ " + Double.toString(aZ));
//                Log.v("dT", Long.toString(elaspedTime));
////                Log.v("velocity", "VY:" + Double.toString(vY) + "VZ:" + Double.toString(vZ));
////                Log.v("dist", "Y:" + Double.toString(dY) + "Z:" + Double.toString(dZ));

//            } else {
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
//            }

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
  //      mBluetoothAdapter.disable();
//        oel.disable();
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
    private void sendMessage(double dY, double dZ, long dT){
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        String str = "M" + String.valueOf(dY) + "," + String.valueOf(dZ) + "," + Long.toString(dT) + "\n";
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



}
