package ua.sytor.censor.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.thebluealliance.spectrum.SpectrumPalette;

import ua.sytor.censor.R;

public class CensorTypeDialog {

    private Context context;

    private View view;
    private Spinner spinner;
    private TextView parameterTextView;
    private AlertDialog.Builder dialogBuilder;

    private EditText numberInput;
    private SpectrumPalette colorInput;

    public CensorTypeDialog(Context context){

        this.context = context;

        //Inflate dialog view
        view = LayoutInflater.from(context).inflate(R.layout.dialog_censor_type, null);

        //Initialize parameters choosers
        numberInput = new EditText(context);
        numberInput.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        colorInput = new SpectrumPalette(context);
        colorInput.setColors(new int[]{Color.WHITE, Color.BLACK, Color.BLUE, Color.YELLOW});
        colorInput.setSelectedColor(Color.WHITE);

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

        final SharedPreferences sharedPreferences = context
                .getSharedPreferences("", Context.MODE_PRIVATE);

        dialogBuilder = new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(R.string.apply, (dialogInterface, i) -> {
                    spinner.getSelectedItemPosition();
                    sharedPreferences.edit()
                            .putInt("",spinner.getSelectedItemPosition())
                            .apply();
                })
                .setNegativeButton(R.string.cancel,
                        (dialogInterface, i) -> dialogInterface.dismiss()
                );

        dialogBuilder.create().show();
    }

    private void spinnerSelect(int position){
        int textId;
        ((FrameLayout)view.findViewById(R.id.frame)).removeAllViews();
        switch (position){
            case 1:
                textId = R.string.blur_;
                ((FrameLayout)view.findViewById(R.id.frame)).addView(numberInput);
                break;
            case 2:
                textId = R.string.color;
                ((FrameLayout)view.findViewById(R.id.frame)).addView(colorInput);
                break;
            case 0:
            default:
                textId = R.string.square_size;
                ((FrameLayout)view.findViewById(R.id.frame)).addView(numberInput);
        }
        parameterTextView.setText(textId);
    }


}
