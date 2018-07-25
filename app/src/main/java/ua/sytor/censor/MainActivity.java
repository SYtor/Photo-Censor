package ua.sytor.censor;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.IntentCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.io.FileNotFoundException;

import ua.sytor.censor.effects.BlurEffect;
import ua.sytor.censor.effects.ColorEffect;
import ua.sytor.censor.effects.Effect;
import ua.sytor.censor.effects.SquareEffect;
import ua.sytor.censor.selectors.PolygonTouchListener;
import ua.sytor.censor.selectors.RectangleTouchListener;
import ua.sytor.censor.selectors.Selector;
import ua.sytor.censor.ui.dialogs.CensorTypeDialog;
import ua.sytor.censor.ui.ShapeView;
import ua.sytor.censor.ui.dialogs.SelectorEditDialog;
import ua.sytor.censor.ui.tabs.PagerAdapter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int PICK_IMAGE = 1;
    public static final int PERMISSION_REQUEST = 123;

    private ImageView imageView;
    private ShapeView shapeView;

    private ViewPager viewPager;
    private ImageButton pagerHideButton;

    ProgressBar progressBar;

    private InterstitialAd interstitialAd;

    private BitmapProcessor bitmapProcessor;

    private boolean isLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkWritePermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST);
        }

        imageView = findViewById(R.id.image_view);
        shapeView = findViewById(R.id.shape_view);

        viewPager = findViewById(R.id.view_pager);
        pagerHideButton = findViewById(R.id.image_button);

        progressBar = findViewById(R.id.progress);

        bitmapProcessor = ViewModelProviders.of(this).get(BitmapProcessor.class);
        bitmapProcessor.onImageChanges()
                .observe(this, bitmap -> {
                    imageView.setImageBitmap(bitmap);
                    setLoading(false);
                });

        initViews();

        isLoading = false;

        rateDialog();

        //Ads

        MobileAds.initialize(this, getString(R.string.app_id));
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.ad_unit_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)    {

        switch (requestCode){

            case PICK_IMAGE:
                setLoading(true);
                try {
                    bitmapProcessor.loadImage(data.getData());
                } catch (FileNotFoundException e) {
                    Toast.makeText(this, R.string.cant_find_file, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (checkWritePermission()) return;
        PackageManager packageManager = getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        startActivity(mainIntent);
        System.exit(0);
    }

    @Override
    public void onClick(View v){

        if (isLoading){
            Toast.makeText(this, R.string.loading, Toast.LENGTH_SHORT).show();
            return;
        }

        switch (v.getId()){
            case R.id.apply_changes:
                if (!bitmapProcessor.isBitmapLoaded()){
                    Toast.makeText(this, R.string.load_image_first, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!shapeView.isClosed()){
                    Toast.makeText(this, R.string.select_shape_first, Toast.LENGTH_SHORT).show();
                    return;
                }
                setLoading(true);
                bitmapProcessor.applySelection(shapeView, imageView);
                shapeView.resetShape();
                break;
            case R.id.select_image:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), PICK_IMAGE);
                break;
            case R.id.clear_selection:
                shapeView.resetShape();
                break;
            case R.id.turn_left:
                if (!bitmapProcessor.isBitmapLoaded()){
                    Toast.makeText(this, R.string.load_image_first, Toast.LENGTH_SHORT).show();
                    return;
                }
                setLoading(true);
                bitmapProcessor.rotate(-90);
                break;
            case R.id.turn_right:
                if (!bitmapProcessor.isBitmapLoaded()){
                    Toast.makeText(this, R.string.load_image_first, Toast.LENGTH_SHORT).show();
                    return;
                }
                setLoading(true);
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
                if (!bitmapProcessor.isBitmapLoaded()){
                    Toast.makeText(this, R.string.load_image_first, Toast.LENGTH_SHORT).show();
                    return;
                }
                setLoading(true);
                saveFile();

                //Ads
                if (interstitialAd.isLoaded())
                    interstitialAd.show();
                break;
        }

    }

    private void initViews(){
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        SharedPreferences sharedPref = getSharedPreferences("settings",MODE_PRIVATE);

        if (sharedPref.getInt("selectionType", Selector.POLYGON_TYPE) == Selector.POLYGON_TYPE)
            shapeView.setTouch(new PolygonTouchListener(shapeView));
        else
            shapeView.setTouch(new RectangleTouchListener(shapeView));

        shapeView.setColor(sharedPref.getInt("selectorColor", Color.WHITE));

        switch (sharedPref.getInt("effectType", Effect.SQUARES)){
            case Effect.SQUARES:
                bitmapProcessor.setEffect(new SquareEffect());
                break;
            case Effect.BLUR:
                bitmapProcessor.setEffect(new BlurEffect());
                break;
            case Effect.COLOR_FILL:
                bitmapProcessor.setEffect(new ColorEffect());
        }

        pagerHideButton.setOnTouchListener(new ImageButtonTouch());
        updateHideButtonDrawable(true);
    }

    private void setLoading(boolean isLoading){
        this.isLoading = isLoading;
        int visibility = isLoading ? View.VISIBLE : View.GONE;
        shapeView.setTouchability(!isLoading);
        progressBar.setVisibility(visibility);
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

    private void saveFile(){
        bitmapProcessor.saveFile().observe(this, message ->{
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            setLoading(false);
        });
    }

    private void displayCensorTypeDialog(){
        new CensorTypeDialog(this, bitmapProcessor);
    }

    private void selectionSettingsDialog(){
        new SelectorEditDialog(this, shapeView);
    }

    private void rateDialog(){
        SharedPreferences sharedPreferences = getSharedPreferences("info",MODE_PRIVATE);
        int count = sharedPreferences.getInt("count",0);
        if (count == -1) return;

        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (count == 3){
            editor.putInt("count",0).apply();
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle(R.string.rate_this_app)
                    .setMessage(R.string.remind_message)
                    .setPositiveButton(R.string.rate_it_now, (dialogInterface, i) -> {
                        final String appPackageName = getPackageName();
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException e) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                        editor.putInt("count", -1).apply();
                    })
                    .setNegativeButton(R.string.no_thanks, (dialogInterface, i) -> {
                        editor.putInt("count", -1).apply();
                    })
                    .setNeutralButton(R.string.remind_me_later, (dialogInterface, i) -> {
                        dialogInterface.cancel();
                    });
            builder.create().show();
        }
        else
            editor.putInt("count", count+1).apply();

    }

    private boolean checkWritePermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
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
