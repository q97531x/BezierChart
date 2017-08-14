package com.weeboos.mylibrary;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bo.wei on 2017/7/3.
 */

public class BezierLineChart extends View {

    private int viewWidth,viewHeight;

    private Paint linePaint,brokenLinePaint,textPaint;

    private Path path;

    private int markCount = 6;

    private ArrayList<String> xDataList = new ArrayList<>();

    //检测的数据list
    private ArrayList<Float> dataList = new ArrayList<>();

    private ArrayList<Point> points = new ArrayList<>();

    private float paddingWidth = dipToPx(20);

    private boolean isClick = false;

    private Point selectPoint;

    private Handler handler;

    private Runnable runnable;

    public BezierLineChart(Context context) {
        this(context,null);
    }

    public BezierLineChart(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BezierLineChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if(attrs!=null){
            //如果attr不为空，则初始化

        }

        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.FILL);

        //虚线paint
        DashPathEffect pathEffect = new DashPathEffect(new float[] { 1,2 }, 1);
        brokenLinePaint = new Paint();
        brokenLinePaint.setAntiAlias(true);
        brokenLinePaint.setStyle(Paint.Style.FILL);
        brokenLinePaint.setPathEffect(pathEffect);

        //textPaint
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(dipToPx(12));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        viewWidth = w;
        viewHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBottomLine(canvas);
        drawMarkLine(canvas);
        drawPoint(canvas);
        drawBezierLine(canvas);

        if(isClick) {
            drawPopWindow(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float xDown = 0,yDown = 0,xUp,yUp;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                xDown = event.getX();
                yDown = event.getY();
            case MotionEvent.ACTION_UP:
                xUp = event.getX();
                yUp = event.getY();
                if(Math.abs(xDown-xUp)<10&&Math.abs(yDown-yUp)<10){
                    //点击事件
                    for (int i = 0;i<points.size();i++){
                        Point point = points.get(i);
                        if(Math.abs(point.getPointX()-xUp)<30&&Math.abs(point.getPointY()-yUp)<30){
                            //点击了这个点
                            selectPoint = point;
                            showView();
                        }
                    }

                }
                break;
        }
        return true;
    }

    private void showView() {
        isClick = true;
        //取消runable
        if(handler!=null&&runnable!=null){
            handler.removeCallbacks(runnable);
        }
        invalidate();
    }

    public void setData(ArrayList<String> xList, ArrayList<Float> dataList){
        xDataList = xList;
        this.dataList = dataList;
        invalidate();
    }

    private void drawPopWindow(Canvas canvas){
        //绘制过选中点的直线
        linePaint.setColor(Color.GRAY);
        linePaint.setStrokeWidth(dipToPx(2));
        canvas.drawLine(selectPoint.getPointX(),viewHeight*0.8f/markCount,selectPoint.getPointX(),viewHeight*0.8f,linePaint);
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setColor(Color.BLACK);
        textPaint.setColor(Color.WHITE);

        if(selectPoint!=null){
            if(Build.VERSION.SDK_INT >=21) {
                canvas.drawRoundRect(selectPoint.getPointX() - dipToPx(80), selectPoint.getPointY() - dipToPx(20), selectPoint.getPointX() - dipToPx(10), selectPoint.getPointY() + dipToPx(20), 10, 10, linePaint);
            }else {
                RectF rectF = new RectF();
                rectF.left = selectPoint.getPointX() - dipToPx(80);
                rectF.top = selectPoint.getPointY() - dipToPx(20);
                rectF.right = selectPoint.getPointX() - dipToPx(10);
                rectF.bottom = selectPoint.getPointY() + dipToPx(20);
                canvas.drawRoundRect(rectF,10,10,linePaint);
            }
            Path path = new Path();
            path.moveTo(selectPoint.getPointX()-dipToPx(10),selectPoint.getPointY()-dipToPx(6));
            path.lineTo(selectPoint.getPointX()-dipToPx(4),selectPoint.getPointY());
            path.lineTo(selectPoint.getPointX()-dipToPx(10),selectPoint.getPointY()+dipToPx(6));
            path.close();
            canvas.drawPath(path,linePaint);

            Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.drawable.position);
            canvas.drawBitmap(bmp,selectPoint.getPointX() - dipToPx(76),selectPoint.getPointY()- dipToPx(18),new Paint());
            canvas.drawText("收缩压",selectPoint.getPointX()-dipToPx(40),selectPoint.getPointY()- dipToPx(6),textPaint);
            canvas.drawText("122 mmHg",selectPoint.getPointX()-dipToPx(44),selectPoint.getPointY()+dipToPx(12),textPaint);

            //两秒后隐藏popWindow
            if(handler==null) {
                handler = new Handler();
                if(runnable == null){
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            isClick = false;
                            invalidate();
                        }
                    };
                }
            }else {
                handler.postDelayed(runnable,2000L);
            }

        }
    }

    private void drawBezierLine(Canvas canvas) {
        if(path == null){
            path = new Path();
        }
        linePaint.setColor(Color.RED);
        linePaint.setStrokeWidth(dipToPx(3));
        linePaint.setStyle(Paint.Style.STROKE);

        Point previousPoint = null;
        Point prePreviousPoint = null;
        Point currentPoint;
        Point nextPoint;
        path.reset();
        for(int i = 0;i<points.size();i++) {
                currentPoint = points.get(i);
                if(i==0){
                    previousPoint = points.get(i);
                    prePreviousPoint = points.get(i);
                }else if(i==1){
                    previousPoint = points.get(i-1);
                    prePreviousPoint = points.get(i-1);
                }else if(i>1&&i<points.size()){
                    previousPoint = points.get(i-1);
                    prePreviousPoint = points.get(i-2);
                }
                if(i==points.size()-1){
                    nextPoint = points.get(points.size()-1);
                }else {
                    nextPoint = points.get(i+1);
                }
                if(i==0){
                    path.moveTo(points.get(i).getPointX(),points.get(i).getPointY());
                }else {
                    // 求出控制点坐标
                    float firstDiffX = (currentPoint.getPointX() - prePreviousPoint.getPointX());
                    float firstDiffY = (currentPoint.getPointY() - prePreviousPoint.getPointY());
                    float secondDiffX = (nextPoint.getPointX() - previousPoint.getPointX());
                    float secondDiffY = (nextPoint.getPointY() - previousPoint.getPointY());
                    float firstControlPointX = previousPoint.getPointX() + (0.2f * firstDiffX);
                    float firstControlPointY = previousPoint.getPointY() + (0.2f * firstDiffY);
                    float secondControlPointX = currentPoint.getPointX() - (0.2f * secondDiffX);
                    float secondControlPointY = currentPoint.getPointY() - (0.2f * secondDiffY);
                    path.cubicTo(firstControlPointX, firstControlPointY, secondControlPointX, secondControlPointY,
                            currentPoint.getPointX(), currentPoint.getPointY());
                }

                canvas.drawPath(path,linePaint);
        }
        path.reset();
    }

    //画曲线上的端点
    private void drawPoint(Canvas canvas) {

        points.clear();
        for(int i = 0;i<xDataList.size();i++){
            float detectionY = (markCount-(dataList.get(i)/20))*viewHeight*0.8f/markCount-(viewHeight*0.8f/markCount)*(dataList.get(i)%20)/20;
            float detectionX = 2*paddingWidth+(viewWidth-3*paddingWidth)*i/(xDataList.size()-1);
            Point point = new Point(detectionX,detectionY);
            points.add(point);
            linePaint.setStyle(Paint.Style.FILL);
            linePaint.setColor(Color.WHITE);
            canvas.drawCircle(detectionX,detectionY,dipToPx(8),linePaint);
            linePaint.setColor(Color.GREEN);

            canvas.drawCircle(detectionX,detectionY,dipToPx(5),linePaint);

        }
    }

    private void drawMarkLine(Canvas canvas) {
        linePaint.setStrokeWidth(dipToPx(1));
        linePaint.setColor(0xff02bbb7);
        for(int i = 1;i<markCount;i++) {
            canvas.drawLine(dipToPx(20),viewHeight*0.8f*(markCount-i)/markCount,viewWidth,viewHeight*0.8f*(markCount-i)/markCount,linePaint);
            canvas.drawText(""+i*20,dipToPx(10),viewHeight*0.8f*(markCount-i)/markCount,textPaint);
        }
    }

    //画底部的基线
    private void drawBottomLine(Canvas canvas) {
        linePaint.setStrokeWidth(dipToPx(1));
        linePaint.setColor(Color.BLACK);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(dipToPx(12));
        canvas.drawLine(paddingWidth,viewHeight*0.8f,viewWidth,viewHeight*0.8f,linePaint);

        float coordinateX;
        for(int i = 0;i<xDataList.size();i++){
            //绘制文本
            coordinateX = 2*paddingWidth+(viewWidth-3*paddingWidth)*i/(xDataList.size()-1);
            canvas.drawText(xDataList.get(i),coordinateX,viewHeight*0.8f+dipToPx(20),textPaint);
        }
    }

    /**
     * dip 转换成px
     *
     * @param dip
     * @return
     */
    private int dipToPx(float dip)
    {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }
}
