package ua.sytor.censor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ua.sytor.censor.effects.Effect;
import ua.sytor.censor.sromku.Point;
import ua.sytor.censor.sromku.Polygon;
import ua.sytor.censor.ui.ShapeView;

public class BitmapProcessor {

    private final static int maxBitmapSize = 2048 * 2048;

    private Context context;
    private ImageView imageView;
    private ShapeView shapeView;

    private Uri uri;
    private int imageWidth, imageHeight;
    private int squareSize;

    private Bitmap bitmap;

    private Effect effect;

    public BitmapProcessor(Context context, ImageView imageView, ShapeView shapeView){
        this.context = context;
        this.imageView = imageView;
        this.shapeView = shapeView;
        squareSize = 10;
    }

    public void updateImageView() throws IOException {
        if (uri == null)
            return;

        InputStream is = context.getContentResolver().openInputStream(uri);

        if (imageWidth * imageHeight > maxBitmapSize){
            Log.wtf("123","big img" + imageWidth + " " + imageHeight);
            bitmap = decodeSampledBitmap(is);
        }
        else
            bitmap = BitmapFactory.decodeStream(is);

        imageView.setImageBitmap(bitmap);
    }

    private Bitmap decodeSampledBitmap(InputStream inputStream) throws FileNotFoundException {

        final BitmapFactory.Options options = new BitmapFactory.Options();

        int inSampleSize = 1;

        int tmpHeight = imageHeight, tmpWidth = imageWidth;

        while (tmpHeight * tmpWidth > 2048 * 2048){
            inSampleSize *= 2;
            tmpHeight /=2;
            tmpWidth /=2;
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(inputStream,new Rect(), options);
    }


    public void applySelection() {

        //Read input file

        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        //Change touch coordinates to image coordinates

        List<PointF> list = new ArrayList<>();
        list.addAll(shapeView.getPointList());

        for (PointF point : list){

            //Coordinates to imageview origin
            point.x = point.x + shapeView.getX() - imageView.getX();
            point.y = point.y + shapeView.getY() - imageView.getY();
            //Coordinates to bitamp origin
            point.x = point.x / imageView.getWidth() * width;
            point.y = point.y / imageView.getHeight() * height;

        }

        Polygon.Builder polygonBuilder = new Polygon.Builder();
        for (PointF point : list)
            polygonBuilder.addVertex(new Point(point.x,point.y));
        Polygon polygon = polygonBuilder.build();


        //Drawing objects

        effect.apply(bitmap, polygon);

        imageView.setImageBitmap(bitmap);

    }


    public void setUri(Uri uri) {
        this.uri = uri;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri),new Rect(),options);
            imageHeight = options.outHeight;
            imageWidth = options.outWidth;
        } catch (FileNotFoundException e) {
            imageHeight = -1;
            imageWidth = -1;
        }

    }

    public Uri getUri() {
        return uri;
    }


    public void rotate(float degrees){

        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        imageView.setImageBitmap(bitmap);

    }

    public void setEffect(Effect effect) {
        this.effect = effect;
    }
}
