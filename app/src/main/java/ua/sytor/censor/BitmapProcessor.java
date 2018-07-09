package ua.sytor.censor;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ua.sytor.censor.sromku.Point;
import ua.sytor.censor.sromku.Polygon;

public class BitmapProcessor {

    private Context context;
    private ImageView imageView;
    private ShapeView shapeView;


    private Uri uri;

    private File tmp;

    private int imageWidth, imageHeight;

    private int squareSize;

    public BitmapProcessor(Context context, ImageView imageView, ShapeView shapeView){
        this.context = context;
        this.imageView = imageView;
        this.shapeView = shapeView;
        tmp = null;
        squareSize = 10;
    }

    public void updateImageView() throws IOException {
        if (uri == null)
            return;

        if (tmp == null) createTmpFileCopy();

        if (imageWidth * imageHeight > 1280 * 720){
            Log.wtf("123","big img" + imageWidth + " " + imageHeight);
            imageView.setImageBitmap(decodeSampledBitmap(tmp));
        }
        else
            imageView.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(tmp)));
    }

    private void createTmpFileCopy() throws IOException {
        tmp = File.createTempFile("tmp",null,context.getCacheDir());
        FileOutputStream outputStream = new FileOutputStream(tmp);
        InputStream inputStream = context.getContentResolver().openInputStream(uri);

        byte[] b = new byte[8192];
        for (int r; (r = inputStream.read(b)) != -1;)
            outputStream.write(b, 0, r);
    }

    private Bitmap decodeSampledBitmap(File file) throws FileNotFoundException {

        final BitmapFactory.Options options = new BitmapFactory.Options();

        int inSampleSize = 1;

        int tmpHeight = imageHeight, tmpWidth = imageWidth;

        while (tmpHeight > 1280 || tmpWidth > 1280){
            inSampleSize *= 2;
            tmpHeight /=2;
            tmpWidth /=2;
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(new FileInputStream(file),new Rect(), options);
    }


    public void onDestroy(){
        if (tmp!=null) tmp.delete();
    }



    public void applySelection() throws IOException {

        //Change touch coordinates to image coordinates

        List<PointF> list = new ArrayList<>();
        list.addAll(shapeView.getPointList());

        for (PointF point : list){

            //Coordinates to imageview origin
            point.x = point.x + shapeView.getX() - imageView.getX();
            point.y = point.y + shapeView.getY() - imageView.getY();
            //Coordinates to bitamp origin
            point.x = point.x / imageView.getWidth() * imageWidth;
            point.y = point.y / imageView.getHeight() * imageHeight;

        }

        Polygon.Builder polygonBuilder = new Polygon.Builder();
        for (PointF point : list)
            polygonBuilder.addVertex(new Point(point.x,point.y));
        Polygon polygon = polygonBuilder.build();

        //Read input file

        InputStream inputStream = new FileInputStream(tmp);
        BitmapRegionDecoder bitmapRegionDecoder = BitmapRegionDecoder.newInstance(inputStream, false);
        Rect rect = new Rect();

        //Drawing objects

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        Point tmpPoint = new Point(0,0);
        Canvas canvas = new Canvas();

        for (int i = 0; i < imageHeight; i = i + 512){
            for (int j = 0; j < imageWidth; j = j + 512){

                rect.left = j;
                rect.bottom = Math.min(i + 512, imageHeight);
                rect.right = Math.min(j + 512, imageWidth);
                rect.top = i;

                Bitmap bitmap = bitmapRegionDecoder.decodeRegion(rect, null);

                canvas.setBitmap(bitmap.copy(Bitmap.Config.ARGB_8888,true));

                int squareHalf = squareSize / 2;

                for (int x = rect.left + squareHalf; x < rect.right; x += squareSize){
                    for (int y = rect.top + squareHalf; y < rect.bottom; y += squareSize){

                        tmpPoint.y = y;
                        tmpPoint.x = x;

                        if (polygon.contains(tmpPoint)){

                            //Get color
                            float redValue = 0;
                            float blueValue = 0;
                            float greenValue = 0;
                            for (int px = x - squareHalf; px < Math.min(rect.right, x + squareHalf); px++){
                                for (int py = y - squareHalf; py < Math.min(rect.bottom, x + squareHalf) ; py++){
                                    int pixel = bitmap.getPixel(px - rect.left, py - rect.top);
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

                imageView.setImageBitmap(bitmap);
                return;

            }
        }

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



    private void editImage(){

        Drawable drawable = imageView.getDrawable();
        int imageWidth = drawable.getIntrinsicWidth();
        int imageHeight = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(imageWidth,imageHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);

        List<PointF> list = new ArrayList<>();
        list.addAll(shapeView.getPointList());

        //points position to imageview perspective
        for (PointF point : list){

            //Coordinates to imageview origin
            point.x = point.x + shapeView.getX() - imageView.getX();
            point.y = point.y + shapeView.getY() - imageView.getY();
            //Coordinates to bitamp origin
            point.x = point.x / imageView.getWidth() * imageWidth;
            point.y = point.y / imageView.getHeight() * imageHeight;

        }

        makeMosaic(bitmap, canvas, list);

        imageView.setImageBitmap(bitmap);

    }

    private void makeMosaic(Bitmap bitmap, Canvas canvas, List<PointF> points){

        int squareSize = 24 ;
        int halfSquare = squareSize/2;

        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];

        Polygon.Builder polygonBuilder = new Polygon.Builder();
        for (PointF point : points){
            polygonBuilder.addVertex(new Point(point.x,point.y));
        }

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        Polygon polygon = polygonBuilder.build();

        Point tmpPoint = new Point(0,0);
        //Divide all bitmap to squares
        for (int i = halfSquare; i + halfSquare < bitmap.getHeight(); i += squareSize){
            for (int j = halfSquare; j + halfSquare < bitmap.getWidth(); j += squareSize){
                tmpPoint.y = i;
                tmpPoint.x = j;
                if (polygon.contains(tmpPoint)){

                    //Get color
                    float redValue = 0;
                    float blueValue = 0;
                    float greenValue = 0;
                    for (int z = i - halfSquare; z < i + halfSquare; z++){
                        for (int k = j - halfSquare; k < j + halfSquare; k++){
                            int pixel = bitmap.getPixel(k,z);
                            redValue += (float) Color.red(pixel) / (squareSize * squareSize);
                            blueValue += (float) Color.blue(pixel) / (squareSize * squareSize);
                            greenValue += (float) Color.green(pixel) / (squareSize * squareSize);
                        }
                    }
                    paint.setColor(Color.rgb((int)redValue,(int)greenValue,(int)blueValue));

                    //Paint color square
                    canvas.drawRect(j-halfSquare,i-halfSquare,j+halfSquare,i+halfSquare,paint);

                }

            }
        }

    }


}
