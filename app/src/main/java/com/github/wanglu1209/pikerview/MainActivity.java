package com.github.wanglu1209.pikerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<String> data = new ArrayList<>();
        data.add("Android");
        data.add("Python");
        data.add("C#");
        data.add("Java");
        data.add("C++");
        data.add("Go");
        data.add("Object-C");
        data.add("JavaScript");
        data.add("C");
        final PickerView pv = (PickerView) findViewById(R.id.pv);
        pv.setData(data);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "" + pv.getPosition(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
