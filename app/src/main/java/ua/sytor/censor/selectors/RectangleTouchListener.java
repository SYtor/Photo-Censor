package ua.sytor.censor.selectors;

import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

import ua.sytor.censor.ui.ShapeView;

public class RectangleTouchListener implements View.OnTouchListener {

    private ShapeView shapeView;

    //Currently selected point
    private PointF point;

    private List<PointF> list;

    public RectangleTouchListener(ShapeView shapeView) {
        this.shapeView = shapeView;
        list = shapeView.getPointList();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        float x = event.getX(), y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //Check is prev point is selected
                point = shapeView.selectPoint(x,y);

                if (list.size() == 4) return true;

                if (point == null) {
                    //Create second point and set rectangle closed
                    if (shapeView.getPointList().size() == 1) {
                        PointF firstPoint = shapeView.getPointList().get(0);

                        list.add(new PointF(firstPoint.x, y));
                        point = new PointF(x,y);
                        list.add(point);
                        list.add(new PointF(x, firstPoint.y));
                        shapeView.setClosed(true);
                    }else{
                        //Create first point
                        point = new PointF(x, y);
                        shapeView.addPoint(point);
                    }
                }
                shapeView.invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (point != null){
                    //If there is more than 1 point
                    if (list.size() > 1){

                        for (PointF pointF : list){
                            if (point == pointF) continue;
                            if (pointF.x == point.x)
                                pointF.x = x;
                            if (pointF.y == point.y)
                                pointF.y = y;
                        }

                    }
                    point.set(x, y);
                }
                shapeView.invalidate();
                break;
            case MotionEvent.ACTION_UP:
                view.performClick();
                break;
        }

        return true;
    }
}
