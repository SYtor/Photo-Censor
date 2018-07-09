package ua.sytor.censor;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TabFragment extends Fragment {



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        LinearLayout viewGroup = (LinearLayout) inflater.inflate(R.layout.tab_layout, container, false);

        Bundle bundle = getArguments();

        int[] stringReses = bundle.getIntArray("titles");
        String[] titles = new String[stringReses.length];
        for (int i = 0; i < titles.length; i++)
            titles[i] = getResources().getString(stringReses[i]);

        int[] iconIds = bundle.getIntArray("icons");
        int[] buttonIds = bundle.getIntArray("buttons");

        for (int i = 0; i < titles.length; i++){

            View item = inflater.inflate(R.layout.tab_item, viewGroup, false);
            ((TextView)item.findViewById(R.id.text)).setText(titles[i]);

            Drawable circle = ContextCompat.getDrawable(getContext(), R.drawable.circle);
            Drawable icon = ContextCompat.getDrawable(getContext(), iconIds[i]);

            LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{circle, icon});
            ImageButton imageButton = item.findViewById(R.id.button);
            imageButton.setImageDrawable(layerDrawable);
            imageButton.setOnClickListener((MainActivity)getActivity());
            imageButton.setId(buttonIds[i]);

            viewGroup.addView(item);

        }

        viewGroup.addView(inflater.inflate(R.layout.tab_switcher,viewGroup,false));

        return viewGroup;
    }

}
