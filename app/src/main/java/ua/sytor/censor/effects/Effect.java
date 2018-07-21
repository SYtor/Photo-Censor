package ua.sytor.censor.effects;

import android.graphics.Bitmap;
import ua.sytor.censor.sromku.Polygon;

public interface Effect {
    void apply(Bitmap bitmap, Polygon polygon);
}
