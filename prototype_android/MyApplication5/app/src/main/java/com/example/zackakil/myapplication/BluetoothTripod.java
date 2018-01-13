package com.example.zackakil.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

/**
 * Created by zackakil on 13/01/2018.
 */

public class BluetoothTripod {

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    private BluetoothSocket soc = null;


    BluetoothTripod(String btDeviceName){



        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.e("My App", "Bluetooth not found");
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d("My App", deviceName);
                Log.d("My App", deviceHardwareAddress);
                if(device.getName().equals(btDeviceName)) {
                    Log.e("My App", "Found device");
                    try{
                        soc = createBluetoothSocket(device);
                        soc.connect();

                    } catch (IOException e) {
                        Log.e("My App", "Socket creation failed");
                    }

                }
            }

        }


    }


    public BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BTMODULEUUID);
        } catch (Exception e) {
            Log.e("My App", "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private void sendDownBluetooth(char bytes){

        sendDownBluetooth( String.valueOf(bytes).getBytes() );
    }

    private void sendDownBluetooth(byte[] bytes){

        if (this.soc.isConnected()){

            try {
                OutputStream stream = this.soc.getOutputStream();

                stream.write(bytes);

            }catch (IOException e){
                Log.e("My App", "IO exception!",e);
            }

        }else{
            Log.e("My App", "Not connected");
        }

    }

}
