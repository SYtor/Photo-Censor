package ua.sytor.censor.effects;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import ua.sytor.censor.sromku.Point;
import ua.sytor.censor.sromku.Polygon;

public class SquareEffect implements Effect {

    @Override
    public void apply(Bitmap bitmap, Polygon polygon) {

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        Point tmpPoint = new Point(0,0);
        Canvas canvas = new Canvas(bitmap);

        int squareSize = Math.min(bitmap.getWidth(),bitmap.getHeight()) / 10;

        int squareHalf = squareSize / 2;

        for (int x = squareHalf; x < bitmap.getWidth(); x += squareHalf){
            for (int y = squareHalf; y < bitmap.getHeight(); y += squareHalf){

                tmpPoint.y = y;
                tmpPoint.x = x;

                if (polygon.contains(tmpPoint)){

                    //Get color
                    float redValue = 0;
                    float blueValue = 0;
                    float greenValue = 0;
                    for (int px = x - squareHalf; px < Math.min(bitmap.getWidth(), x + squareHalf); px++){
                        for (int py = y - squareHalf; py < Math.min(bitmap.getHeight(), y + squareHalf) ; py++){
                            int pixel = bitmap.getPixel(px,py);
                            redValue += (float) Color.red(pixel) / (squareSize * squareSize);
                            blueValue += (float) Color.blue(pixel) / (squareSize * squareSize);
                            greenValue += (float) Color.green(pixel) / (squareSize * squareSize);
                        }
                    }
                    paint.setColor(Color.rgb((int)redValue,(int)greenValue,(int)blueValue));

                    //Paint color square
                    canvas.drawRect(x - squareHalf, y + squareHalf, x + squareHalf, y - squareHalf, paint);

                }
            }
        }
    }

}
