package com.example.zackakil.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ListActivity extends AppCompatActivity {

    ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mListView = (ListView) findViewById(R.id.myListView);
        String[] testItems = {"Run", "Jog", "Rugby", "Sky", "Sea"};
        ArrayAdapter<String> testArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, testItems);
        mListView.setAdapter(testArrayAdapter);

    }
}
