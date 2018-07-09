package ua.sytor.censor;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int PICK_IMAGE = 1;

    private ImageView imageView;
    private ShapeView shapeView;

    private ViewPager viewPager;

    private BitmapProcessor bitmapProcessor;

    private ImageButton imageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.image_view);
        shapeView = (ShapeView) findViewById(R.id.shape_view);
        imageButton = (ImageButton) findViewById(R.id.image_button);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));

        shapeView.setOnTouchListener(new ImageTouchListener(imageView, shapeView));

        bitmapProcessor = new BitmapProcessor(this, imageView, shapeView);


        Drawable circle = ContextCompat.getDrawable(this, R.drawable.circle);
        circle.setColorFilter(ContextCompat.getColor(this,R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        Drawable downArrow = ContextCompat.getDrawable(this, R.drawable.ic_keyboard_arrow_down_black_24dp);
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{circle,downArrow});

        imageButton.setImageDrawable(layerDrawable);
        imageButton.setOnTouchListener(new ImageButtonTouch());


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)    {
        if (requestCode == PICK_IMAGE) {
            bitmapProcessor.setUri(data.getData());
            try {
                bitmapProcessor.updateImageView();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v){

        switch (v.getId()){
            case R.id.apply_changes:
                bitmapProcessor.applySelection();
                break;
            case R.id.select_image:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                break;
            case R.id.clear_selection:
                shapeView.resetShape();
                shapeView.invalidate();
                break;
            case R.id.turn_left:
                bitmapProcessor.rotate(-90);
                break;
            case R.id.turn_right:
                bitmapProcessor.rotate(90);
                break;
        }

    }

    private class ImageButtonTouch implements View.OnTouchListener{

        private boolean isHide;

        private GestureDetectorCompat gestureDetector;
        private ConstraintLayout.LayoutParams layoutParams;

        ImageButtonTouch(){
            gestureDetector = new GestureDetectorCompat(MainActivity.this, new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
            layoutParams = (ConstraintLayout.LayoutParams) imageButton.getLayoutParams();
            isHide = false;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            if (gestureDetector.onTouchEvent(motionEvent)){
                if (isHide){
                    viewPager.animate().translationY(0);
                    findViewById(R.id.constraint).animate().translationY(0);
                }else{
                    viewPager.animate().translationY(viewPager.getHeight());
                    findViewById(R.id.constraint).animate().translationY(viewPager.getHeight());
                }
                isHide = !isHide;
                return true;
            }

            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    return true;
                case MotionEvent.ACTION_UP:
                    return true;
                case MotionEvent.ACTION_MOVE:
                    layoutParams.horizontalBias = motionEvent.getRawX() / shapeView.getWidth();
                    imageButton.setLayoutParams(layoutParams);
                    return true;
            }

            return false;
        }
    }

}
