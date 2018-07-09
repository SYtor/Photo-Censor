package ua.sytor.censor;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int PICK_IMAGE = 1;

    private ImageView imageView;
    private ShapeView shapeView;

    private ViewPager viewPager;

    private BitmapProcessor bitmapProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.image_view);
        shapeView = (ShapeView) findViewById(R.id.shape_view);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));

        shapeView.setOnTouchListener(new ImageTouchListener(imageView, shapeView));

        bitmapProcessor = new BitmapProcessor(this, imageView, shapeView);

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
                try {
                    bitmapProcessor.applySelection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        }

    }

}
