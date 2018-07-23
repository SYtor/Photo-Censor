package ua.sytor.censor.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class ShapeView extends View {

    private static final float circleRadius = 14;
    private static final float selectMaxDistance = 50;

    private List<PointF> pointList;
    private boolean isClosed;

    private Paint paint;

    public ShapeView(Context context) {
        super(context);
        init();
    }

    public ShapeView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        init();
    }

    private void init(){
        pointList = new ArrayList<>();
        isClosed = false;
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        invalidate();
    }

    private float distance(float x1, float y1, float x2, float y2){
        return (float) Math.sqrt(Math.pow(x1 - x2,2)+Math.pow(y1 - y2,2));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int size = pointList.size();
        if (size == 0)
            return;

        PointF prevPoint = null;
        for (int i = 0; i < size; i++){
            PointF point = pointList.get(i);
            canvas.drawCircle(point.x, point.y, circleRadius, paint);
            if (i != 0)
                canvas.drawLine(prevPoint.x, prevPoint.y, point.x, point.y, paint);
            prevPoint = point;
        }

        if (isClosed){
            PointF firstP = pointList.get(0);
            PointF lastP = pointList.get(size-1);
            canvas.drawLine(firstP.x,firstP.y,lastP.x,lastP.y,paint);
        }

    }

    public List<PointF> getPointList() {
        return pointList;
    }

    public void addPoint(PointF point){
        pointList.add(point);
    }

    public PointF selectPoint(float x, float y){

        for (PointF point : pointList){
            if (distance(x,y,point.x,point.y) < selectMaxDistance)
                return point;
        }

        return null;

    }

    public boolean isFirstPointSelected(float x, float y){

        if (pointList.isEmpty()) return false;
        PointF point = pointList.get(0);
        return distance(x, y, point.x, point.y) < selectMaxDistance && pointList.size() > 1;

    }

    public void resetShape(){
        pointList.clear();
        isClosed = false;
        invalidate();
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setColor(int color){
        paint.setColor(color);
    }
}
