package ua.sytor.censor.effects;

import android.content.Context;
import android.graphics.Bitmap;
import ua.sytor.censor.math.Polygon;

public interface Effect {
    void apply(Context context, Bitmap bitmap, Polygon polygon);
}
