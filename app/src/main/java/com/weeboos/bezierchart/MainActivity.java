package com.weeboos.bezierchart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.weeboos.mylibrary.BezierLineChart;

public class MainActivity extends AppCompatActivity {
    private String[] dateList = {"6.1","6.2","6.3","6.4","6.5","6.6","6.7"};

    private int[] detectionList = {20,70,58,85,80,72,69};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BezierLineChart bezierLineChart = (BezierLineChart)findViewById(R.id.bezier);
        bezierLineChart.setData(dateList,detectionList);
    }
}
