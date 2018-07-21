package ua.sytor.censor;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class CensorTypeDialog {

    private View view;
    private Spinner spinner;
    private TextView parameterTextView;
    private AlertDialog.Builder dialogBuilder;

    public CensorTypeDialog(Context context){

        view = LayoutInflater.from(context).inflate(R.layout.dialog_censor_type, null);

        spinner = view.findViewById(R.id.spinner);
        parameterTextView = view.findViewById(R.id.parameterTV);

        spinner.setAdapter(new ArrayAdapter<>(context,android.R.layout.simple_spinner_dropdown_item,
                context.getResources().getStringArray(R.array.censor_types)));

        spinner.setOnItemClickListener((adapterView, view, i, l) -> {
            int textId;
            switch (i){
                case 1:
                    textId = R.string.blur_;
                    break;
                case 2:
                    textId = R.string.color;
                    break;
                case 0:
                default:
                    textId = R.string.square_size;
            }
            parameterTextView.setText(textId);
        });

        final SharedPreferences sharedPreferences = context
                .getSharedPreferences("", Context.MODE_PRIVATE);

        dialogBuilder = new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(R.string.apply, (dialogInterface, i) -> {
                    sharedPreferences.edit()
                            .putInt("",spinner.getSelectedItemPosition())
                            .apply();
                })
                .setNegativeButton(R.string.cancel,
                        (dialogInterface, i) -> dialogInterface.dismiss()
                );

        dialogBuilder.create().show();
    }

}
