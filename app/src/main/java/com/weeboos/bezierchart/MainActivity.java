package com.weeboos.bezierchart;

import android.support.annotation.FloatRange;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.weeboos.mylibrary.BezierLineChart;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private ArrayList<String> dateList = new ArrayList<>(Arrays.asList("6.1","6.2","6.3","6.4","6.5","6.6","6.7"));

    private ArrayList<Float> detectionList = new ArrayList<>(Arrays.asList(20f,70f,58f,85f,80f,72f,69f));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BezierLineChart bezierLineChart = (BezierLineChart)findViewById(R.id.bezier);
        bezierLineChart.setData(dateList,detectionList);
    }
}
