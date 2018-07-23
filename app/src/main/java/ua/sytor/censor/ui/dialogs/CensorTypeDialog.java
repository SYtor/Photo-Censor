package ua.sytor.censor.ui.dialogs;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import com.thebluealliance.spectrum.SpectrumPalette;

import ua.sytor.censor.BitmapProcessor;
import ua.sytor.censor.R;
import ua.sytor.censor.effects.BlurEffect;
import ua.sytor.censor.effects.ColorEffect;
import ua.sytor.censor.effects.SquareEffect;

public class CensorTypeDialog {

    private SharedPreferences sharedPreferences;

    private View view;
    private Spinner spinner;
    private TextView parameterTextView;
    private AlertDialog.Builder dialogBuilder;

    private ViewGroup numberInput;
    private SeekBar seekBar;
    private SpectrumPalette spectrumPalette;

    private int selectedColor;

    public CensorTypeDialog(Context context, BitmapProcessor bitmapProcessor){


        sharedPreferences = context
                .getSharedPreferences("settings", Context.MODE_PRIVATE);

        initViews(context);

        dialogBuilder = new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(R.string.apply, (dialogInterface, i) -> {
                    spinner.getSelectedItemPosition();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("effectType",spinner.getSelectedItemPosition());
                    switch (spinner.getSelectedItemPosition()){
                        case 0:
                            editor.putInt("square",seekBar.getProgress()+1);
                            bitmapProcessor.setEffect(new SquareEffect());
                        case 1:
                            editor.putInt("blur",seekBar.getProgress()+1);
                            bitmapProcessor.setEffect(new BlurEffect());
                        case 2:
                            editor.putInt("color", selectedColor);
                            bitmapProcessor.setEffect(new ColorEffect());
                    }
                    editor.apply();
                })
                .setNegativeButton(R.string.cancel,
                        (dialogInterface, i) -> dialogInterface.dismiss()
                );

        dialogBuilder.create().show();
    }

    private void initViews(Context context){
        //Inflate dialog view
        view = LayoutInflater.from(context).inflate(R.layout.dialog_censor_type, null);

        //Initialize parameters choosers

        //Number input
        numberInput = (ViewGroup) LayoutInflater.from(context)
                .inflate(R.layout.dialog_seekbar_item,null);
        seekBar = numberInput.findViewById(R.id.seekBar);

        //Color input
        spectrumPalette = new SpectrumPalette(context);
        spectrumPalette.setColors(new int[]{Color.WHITE, Color.BLACK, Color.BLUE, Color.YELLOW});
        spectrumPalette.setOnColorSelectedListener(color -> selectedColor = color);

        //Fill spinner with data
        spinner = view.findViewById(R.id.spinner);
        parameterTextView = view.findViewById(R.id.parameterTV);

        spinner.setAdapter(new ArrayAdapter<>(context,android.R.layout.simple_spinner_dropdown_item,
                context.getResources().getStringArray(R.array.censor_types)));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                spinnerSelect(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinnerSelect(0);
    }

    private void spinnerSelect(int position){
        int textId;
        ((FrameLayout)view.findViewById(R.id.frame)).removeAllViews();
        switch (position){
            case 1:
                textId = R.string.blur_;
                seekBar.setProgress(sharedPreferences.getInt("blur",2));
                ((FrameLayout)view.findViewById(R.id.frame)).addView(numberInput);
                break;
            case 2:
                textId = R.string.color;
                spectrumPalette.setSelectedColor(sharedPreferences.getInt("color",Color.WHITE));
                ((FrameLayout)view.findViewById(R.id.frame)).addView(spectrumPalette);
                break;
            case 0:
            default:
                textId = R.string.square_size;
                seekBar.setProgress(sharedPreferences.getInt("square",2));
                ((FrameLayout)view.findViewById(R.id.frame)).addView(numberInput);
        }
        parameterTextView.setText(textId);
    }

}
