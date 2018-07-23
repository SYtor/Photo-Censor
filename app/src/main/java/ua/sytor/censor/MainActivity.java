package ua.sytor.censor;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.IOException;

import ua.sytor.censor.effects.BlurEffect;
import ua.sytor.censor.effects.ColorEffect;
import ua.sytor.censor.effects.SquareEffect;
import ua.sytor.censor.selectors.PolygonTouchListener;
import ua.sytor.censor.selectors.RectangleTouchListener;
import ua.sytor.censor.ui.dialogs.CensorTypeDialog;
import ua.sytor.censor.ui.ShapeView;
import ua.sytor.censor.ui.dialogs.SelectorEditDialog;
import ua.sytor.censor.ui.tabs.PagerAdapter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int PICK_IMAGE = 1;

    private ImageView imageView;
    private ShapeView shapeView;

    private ViewPager viewPager;

    private BitmapProcessor bitmapProcessor;

    private ImageButton pagerHideButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.image_view);
        shapeView = findViewById(R.id.shape_view);
        pagerHideButton = findViewById(R.id.image_button);

        bitmapProcessor = new BitmapProcessor(this, imageView, shapeView);

        viewPager = findViewById(R.id.view_pager);
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        SharedPreferences sharedPref = getSharedPreferences("settings",MODE_PRIVATE);
        String[] selectorTitles = getResources().getStringArray(R.array.selector_types);
        if (sharedPref.getString("selectionType",selectorTitles[0]).equals(selectorTitles[0]))
            shapeView.setOnTouchListener(new PolygonTouchListener(shapeView));
        else
            shapeView.setOnTouchListener(new RectangleTouchListener(shapeView));
        shapeView.setColor(sharedPref.getInt("selectorColor", Color.WHITE));

        switch (sharedPref.getInt("effectType",0)){
            case 0:
                bitmapProcessor.setEffect(new SquareEffect());
                break;
            case 1:
                bitmapProcessor.setEffect(new BlurEffect());
                break;
            case 2:
                bitmapProcessor.setEffect(new ColorEffect());
        }

        pagerHideButton.setOnTouchListener(new ImageButtonTouch());
        updateHideButtonDrawable(true);

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
                if (!bitmapProcessor.isBitmapLoaded()) return;
                bitmapProcessor.applySelection();
                shapeView.resetShape();
                break;
            case R.id.select_image:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                break;
            case R.id.clear_selection:
                shapeView.resetShape();
                break;
            case R.id.turn_left:
                if (!bitmapProcessor.isBitmapLoaded()) return;
                bitmapProcessor.rotate(-90);
                break;
            case R.id.turn_right:
                if (!bitmapProcessor.isBitmapLoaded()) return;
                bitmapProcessor.rotate(90);
                break;
            case R.id.switch_tab:
                viewPager.setCurrentItem((viewPager.getCurrentItem() + 1) % 2);
                break;
            case R.id.censor_type:
                displayCensorTypeDialog();
                break;
            case R.id.selection_settings:
                selectionSettingsDialog();
                break;
            case R.id.export_image:
                if (!bitmapProcessor.isBitmapLoaded()) return;
                saveFile();
                break;
        }

    }

    private void updateHideButtonDrawable(boolean isVisible){
        Drawable circle = ContextCompat.getDrawable(this, R.drawable.circle);
        circle.setColorFilter(ContextCompat.getColor(this,R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        Drawable arrowDrawable = isVisible ?
                ContextCompat.getDrawable(this, R.drawable.ic_arrow_down) :
                ContextCompat.getDrawable(this, R.drawable.ic_arrow_up);
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{circle,arrowDrawable});

        pagerHideButton.setImageDrawable(layerDrawable);
    }

    private void setPagerVisibility(boolean isVisible){

        updateHideButtonDrawable(isVisible);

        if (isVisible){
            viewPager.animate().translationY(0);
            findViewById(R.id.constraint).animate().translationY(0);
        }else{
            viewPager.animate().translationY(viewPager.getHeight());
            findViewById(R.id.constraint).animate().translationY(viewPager.getHeight());
        }
    }

    private void displayCensorTypeDialog(){
        new CensorTypeDialog(this, bitmapProcessor);
    }

    private void selectionSettingsDialog(){
        new SelectorEditDialog(this, shapeView);
    }

    private void saveFile(){

    }

    private class ImageButtonTouch implements View.OnTouchListener{

        private boolean visibility;

        private GestureDetectorCompat gestureDetector;
        private ConstraintLayout.LayoutParams layoutParams;

        private boolean toListen;
        private float pressedCoordinate;

        ImageButtonTouch(){
            gestureDetector = new GestureDetectorCompat(MainActivity.this, new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
            layoutParams = (ConstraintLayout.LayoutParams) pagerHideButton.getLayoutParams();
            visibility = true;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            if (gestureDetector.onTouchEvent(motionEvent)){
                setPagerVisibility(visibility);
                visibility = !visibility;
                return true;
            }

            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressedCoordinate = motionEvent.getRawX();
                    toListen = true;
                    return true;
                case MotionEvent.ACTION_UP:
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (!toListen) return true;
                    if (Math.abs(motionEvent.getRawX() - pressedCoordinate) > shapeView.getWidth() / 4){
                        layoutParams.horizontalBias = layoutParams.horizontalBias == 1 ? 0 : 1;
                        pagerHideButton.setLayoutParams(layoutParams);
                        toListen = false;
                    }

                    return true;
            }

            return false;
        }
    }

}
