package ua.sytor.censor;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import ua.sytor.censor.effects.Effect;
import ua.sytor.censor.math.Point;
import ua.sytor.censor.math.Polygon;
import ua.sytor.censor.ui.ShapeView;

public class BitmapProcessor extends AndroidViewModel{

    private final static int maxBitmapSize = 2048 * 2048;

    private MutableLiveData<Bitmap> bitmapMutableLiveData;

    private int imageWidth, imageHeight;
    private Bitmap bitmap;

    private Effect effect;

    public BitmapProcessor(Application application){
        super(application);
    }

    public LiveData<Bitmap> onImageChanges(){
        if (bitmapMutableLiveData == null) bitmapMutableLiveData = new MutableLiveData<>();
        return bitmapMutableLiveData;
    }

    public void loadImage(Uri uri) throws FileNotFoundException{
        InputStream is = getApplication().getContentResolver().openInputStream(uri);

        new Thread(() -> {

            if (imageWidth * imageHeight > maxBitmapSize){
                Log.wtf("123","big img" + imageWidth + " " + imageHeight);
                bitmap = decodeSampledBitmap(is);
            }
            else
                bitmap = BitmapFactory.decodeStream(is);

            bitmapMutableLiveData.postValue(bitmap);

        }).start();


    }

    private Bitmap decodeSampledBitmap(InputStream inputStream){

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


    public void applySelection(ShapeView shapeView, ImageView imageView) {

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
        new Thread(() -> {
            effect.apply(getApplication(), bitmap, polygon);
            bitmapMutableLiveData.postValue(bitmap);
        }).start();

    }


    public void rotate(float degrees){

        new Thread(() -> {
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
            bitmapMutableLiveData.postValue(bitmap);
        }).start();


    }

    public void setEffect(Effect effect) {
        this.effect = effect;
    }

    public Effect getEffect() {
        return effect;
    }

    public boolean isBitmapLoaded(){
        return bitmap != null;
    }

    public MutableLiveData<String> saveFile(){

        final MutableLiveData<String> message = new MutableLiveData<>();

        new Thread(() -> {

            try {
                Date currentTime = Calendar.getInstance().getTime();
                File file = new File(Environment.getExternalStorageDirectory()
                        + "/PhotoCensor/IMG" + new SimpleDateFormat("YYYYMMdd-kkmmss")
                                .format(currentTime) + ".png");

                Log.wtf("123",file.getAbsolutePath());

                file.getParentFile().mkdirs();
                if (file.createNewFile()){
                    OutputStream fileOutputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, fileOutputStream);
                    message.postValue(getApplication().getString(R.string.saved_to) +
                            file.getAbsolutePath());
                }else
                    message.postValue(getApplication().getString(R.string.cant_create_file));

            } catch (IOException e) {
                message.postValue(getApplication().getString(R.string.error) + e.getMessage());
            }
        }).start();

        return message;
    }
}
