package ua.sytor.censor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter{

    private TabFragment tabFragment, tabFragment2;

    public PagerAdapter(FragmentManager fm) {
        super(fm);

        Bundle b1 = new Bundle();
        b1.putIntArray("titles", new int[]{R.string.select_image, R.string.export_image, R.string.apply_changes, R.string.clear_selection});
        b1.putIntArray("icons", new int[]{R.drawable.ic_photo_select,R.drawable.ic_save,R.drawable.ic_done,R.drawable.ic_clear});
        b1.putIntArray("buttons", new int[]{R.id.select_image, R.id.export_image, R.id.apply_changes, R.id.clear_selection});
        b1.putInt("buttonResourceDrawable", R.drawable.arrow_right);
        tabFragment = new TabFragment();
        tabFragment.setArguments(b1);

        Bundle b2 = new Bundle();
        b2.putIntArray("titles", new int[]{R.string.turn_left, R.string.turn_right, R.string.censor_type, R.string.selection_setting});
        b2.putIntArray("icons", new int[]{R.drawable.ic_rotate_left,R.drawable.ic_rotate_right,R.drawable.circle,R.drawable.ic_crop});
        b2.putIntArray("buttons", new int[]{R.id.turn_left, R.id.turn_right, R.id.censor_type, R.id.selection_settings});
        b2.putInt("buttonResourceDrawable", R.drawable.arrow_left);
        tabFragment2 = new TabFragment();
        tabFragment2.setArguments(b2);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0)
            return tabFragment;
        else
            return tabFragment2;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
