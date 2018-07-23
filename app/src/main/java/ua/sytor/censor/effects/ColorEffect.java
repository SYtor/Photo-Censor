package ua.sytor.censor.effects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import ua.sytor.censor.math.Point;
import ua.sytor.censor.math.Polygon;

public class ColorEffect implements Effect{

    @Override
    public void apply(Context context, Bitmap bitmap, Polygon polygon) {

        int color = context.getSharedPreferences("settings",Context.MODE_PRIVATE)
                .getInt("color", Color.BLACK);

        Point tmpPoint = new Point(0,0);
        for (int x = 0; x < bitmap.getWidth(); x++){
            for (int y = 0; y < bitmap.getHeight(); y++){

                tmpPoint.y = y;
                tmpPoint.x = x;

                if (polygon.contains(tmpPoint))
                    bitmap.setPixel(x,y,color);
            }
        }

    }
}
