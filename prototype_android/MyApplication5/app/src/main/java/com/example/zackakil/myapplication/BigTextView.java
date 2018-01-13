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

    private BluetoothTripod mBluetoothTripod;

    @Override
    protected void onStop() {
        // call the superclass method first
        super.onStop();
        mBluetoothTripod.close();

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

        mBluetoothTripod = new BluetoothTripod("HC-06");

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

    private void sendDownBluetooth(byte[] bytes) {
        mBluetoothTripod.sendDownBluetooth(bytes);
    }

    private void sendDownBluetooth(char bytes) {
        mBluetoothTripod.sendDownBluetooth(bytes);
    }

}
