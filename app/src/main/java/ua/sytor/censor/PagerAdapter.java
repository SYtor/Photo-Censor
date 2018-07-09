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
        b1.putIntArray("icons", new int[]{R.drawable.ic_photo_select_24dp,R.drawable.ic_save_black_24dp,R.drawable.ic_done_black_24dp,R.drawable.ic_clear_black_24dp});
        b1.putIntArray("buttons", new int[]{R.id.select_image, R.id.export_image, R.id.apply_changes, R.id.clear_selection});
        tabFragment = new TabFragment();
        tabFragment.setArguments(b1);

        Bundle b2 = new Bundle();
        b2.putIntArray("titles", new int[]{R.string.turn_left, R.string.turn_right, R.string.select_color, R.string.select_shape});
        b2.putIntArray("icons", new int[]{R.drawable.ic_rotate_left_black_24dp,R.drawable.ic_rotate_right_black_24dp,R.drawable.circle,R.drawable.ic_crop_square_black_24dp});
        b2.putIntArray("buttons", new int[]{R.id.turn_left, R.id.turn_right, R.id.select_color, R.id.select_shape});
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
