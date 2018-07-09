package ua.sytor.censor;

import android.graphics.PointF;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class ImageTouchListener implements View.OnTouchListener{

    private GestureDetectorCompat gestureDetectorCompat;

    private ImageView imageView;
    private ShapeView shapeView;

    //Currently selected point
    private PointF point;

    ImageTouchListener(ImageView imageView, ShapeView shapeView){
        this.imageView = imageView;
        this.shapeView = shapeView;
        gestureDetectorCompat = new GestureDetectorCompat(shapeView.getContext(), new SingleTapConfirm());
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        float x = event.getX(), y = event.getY();
        Log.wtf("123","x = " + x + " y = " + y);

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
