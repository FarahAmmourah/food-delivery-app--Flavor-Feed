package com.farah.foodapp.profile;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.farah.foodapp.profile.rewards.AvailableRewardsActivity;


/*adapter is used to know how many pages i have
* what content the pager should show
* set content tp each tab*/
public class  ProfilePagerAdapter extends FragmentStateAdapter {

    public ProfilePagerAdapter(@NonNull FragmentActivity fa) {/*this constructor is used only to tell
    where we will use it < call it in the profile activity*/
        super(fa);
    }

    @NonNull
    @Override
    //ViewPager know which fragment to display using
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new AvailableRewardsActivity();
        }
        return new ProfileTabActivity();
    }

    @Override// know how many pages will it display and it is called by viewpager it self when the adapter is created or modified
    public int getItemCount() {
        return 2;// always 2 pages
    }
}
