package edu.nd.ble_adv;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


public class MainActivity extends AppCompatActivity {

    /** Debug Tag **/
    private static final String TAG = MainActivity.class.getSimpleName();

    /** UUID Declaration **/
    public static final ParcelUuid UserID_UUID = ParcelUuid.fromString("00001831-0000-1000-8000-00805f9b34fb");

    /** BLE Declaration **/
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    /** layout Variables Declaration **/
    private EditText mEdit;
    private Button Start_Adv;
    private Button Stop_Adv;

    private boolean BLE_status = FALSE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** Check BT Permission **/
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        /** Set Click Listener for buttons **/
        Start_Adv = (Button)findViewById(R.id.start_adv);
        Start_Adv.setOnClickListener(Start_Adv_Listener);
        Stop_Adv = (Button)findViewById(R.id.stop_adv);
        Stop_Adv.setOnClickListener(Stop_Adv_Listener);
        mEdit = (EditText)findViewById(R.id.editText);

        /** BLE Settings **/
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

    }

    /** Click Listener **/
    private final View.OnClickListener Start_Adv_Listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.v(TAG, String.valueOf(mEdit.getText().toString().length()));
            if (mEdit.getText().toString().length() != 4){// validate the length of UserID
                Toast.makeText(getApplicationContext(),"Invalid UserID!", Toast.LENGTH_SHORT).show();
            }else{
                if (BLE_status == FALSE){
                    startAdvertising(mEdit.getText().toString().getBytes());
                    Toast.makeText(getApplicationContext(),"BLE Advertising started!",Toast.LENGTH_SHORT).show();
                }else{
                    stopAdvertising();
                    startAdvertising(mEdit.getText().toString().getBytes());
                    Toast.makeText(getApplicationContext(),"Restart advertising with new UserID..",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private final View.OnClickListener Stop_Adv_Listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            stopAdvertising();
        }
    };

    /** BLE Advertising **/
    public void startAdvertising(byte[] payload){
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        if (mBluetoothLeAdvertiser == null) return;

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY) //3 modes: LOW_POWER, BALANCED, LOW_LATENCY
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH) // ULTRA_LOW, LOW, MEDIUM, HIGH
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(UserID_UUID)
                .addServiceData(UserID_UUID,payload)
                .build();

        mBluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);
    }


    public void stopAdvertising() {
        if (mBluetoothLeAdvertiser == null) return;
        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        Log.i(TAG, "LE Advertise Stopped.");
        BLE_status = FALSE;
    }

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(TAG, "LE Advertise Started.");
            BLE_status = TRUE;
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.w(TAG, "LE Advertise Failed: " + errorCode);
            BLE_status = FALSE;
        }
    };

}
