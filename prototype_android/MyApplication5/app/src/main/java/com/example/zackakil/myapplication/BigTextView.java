package com.example.zackakil.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class BigTextView extends AppCompatActivity {

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    private BluetoothSocket soc = null;

    public BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BTMODULEUUID);
        } catch (Exception e) {
            Log.e("My APp", "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    protected void onStop() {
        // call the superclass method first
        super.onStop();


        try{
            soc.close();

        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_big_text_view);


        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progresValue, boolean fromUser) {

                        sendDownBluetooth((char) progresValue);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });


        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Context context = getApplicationContext();
            Toast.makeText(context, "Bluetooth not found", Toast.LENGTH_SHORT).show();
        }


        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d("My App", deviceName);
                Log.d("My App", deviceHardwareAddress);
                if(device.getName().equals("HC-06")) {
                    Toast.makeText(getBaseContext(), "Found device", Toast.LENGTH_SHORT).show();
                    try{
                        soc = createBluetoothSocket(device);
                        soc.connect();

                } catch (IOException e) {
                    Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                }

            }
            }

        }
    }



    public void sendStuffOnBluetooh(View view){
        /**
         * Send somethiing to arduino using bluetooth
         */
        EditText editText = (EditText) findViewById(R.id.bthEditText);
        String message = editText.getText().toString();
        Log.d("My App", message );

        sendDownBluetooth(message.getBytes());
    }



    private void sendDownBluetooth(char bytes){

        sendDownBluetooth( String.valueOf(bytes).getBytes() );
    }

    private void sendDownBluetooth(byte[] bytes){

        if (soc.isConnected()){

            try {
                OutputStream stream = soc.getOutputStream();

                stream.write(bytes);

            }catch (IOException e){
                Toast.makeText(getBaseContext(), "IO exception!", Toast.LENGTH_SHORT).show();
            }

        }else{
            Toast.makeText(getBaseContext(), "Not connected", Toast.LENGTH_SHORT).show();
        }

    }

}
