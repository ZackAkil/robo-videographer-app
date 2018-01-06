package com.example.zackakil.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ListActivity extends AppCompatActivity {

    ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mListView = (ListView) findViewById(R.id.myListView);
        String[] testItems = {"Run", "Jog", "Rugby", "Sky", "Sea"};
        ArrayAdapter<String> testArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, testItems);
        mListView.setAdapter(testArrayAdapter);


        mListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String item = String.valueOf(adapterView.getItemAtPosition(i));
                        Toast.makeText(getApplicationContext(), item, Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }
}
