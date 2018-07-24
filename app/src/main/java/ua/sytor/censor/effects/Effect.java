package ua.sytor.censor.effects;

import android.content.Context;
import android.graphics.Bitmap;
import ua.sytor.censor.math.Polygon;

public interface Effect {

    int SQUARES = 0;
    int BLUR = 1;
    int COLOR_FILL = 2;

    void apply(Context context, Bitmap bitmap, Polygon polygon);
}
