package ua.sytor.censor.selectors;

import android.graphics.PointF;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import ua.sytor.censor.ui.ShapeView;

public class PolygonTouchListener implements View.OnTouchListener{

    private GestureDetectorCompat gestureDetectorCompat;

    private ShapeView shapeView;

    //Currently selected point
    private PointF point;

    public PolygonTouchListener(ShapeView shapeView){
        this.shapeView = shapeView;
        gestureDetectorCompat = new GestureDetectorCompat(shapeView.getContext(), new SingleTapConfirm());
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        float x = event.getX(), y = event.getY();

        //single click
        if (gestureDetectorCompat.onTouchEvent(event)){
            if (shapeView.isFirstPointSelected(x,y)){
                Log.wtf("123","true");
                shapeView.setClosed(true);
                shapeView.invalidate();
                return true;
            }
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //Check is prev point is selected
                point = shapeView.selectPoint(x,y);
                //or create new point
                if (point == null && !shapeView.isClosed()){
                    point = new PointF(x,y);
                    shapeView.addPoint(point);
                }
                shapeView.invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (point != null)
                    point.set(x, y);
                shapeView.invalidate();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        return true;
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }
}
