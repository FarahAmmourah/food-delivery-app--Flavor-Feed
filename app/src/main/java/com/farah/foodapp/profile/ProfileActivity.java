package com.farah.foodapp.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.farah.foodapp.R;
import com.farah.foodapp.cart.CartActivity;
import com.farah.foodapp.chatbot.AIChatbotActivity;
import com.farah.foodapp.menu.MenuActivity;
import com.farah.foodapp.reel.ReelsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvAvatar, tvUsername, tvEmail, tvPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
<<<<<<< Updated upstream

        //init views
=======
// connect to xml
>>>>>>> Stashed changes
        tvAvatar = findViewById(R.id.tv_avatar);
        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);

        setupBottomNav();// this is to make the nav bar work
        setupTabs();// define which page to open when you choose a tab
        loadUserProfile();// brings user info and fill the views
    }


    //checks what nav is cliced and open the other page
    private void setupBottomNav() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_reels) {
                startActivity(new Intent(this, ReelsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_menu) {
                startActivity(new Intent(this, MenuActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return id == R.id.nav_profile;
        });
    }

    private void setupTabs() {
        TabLayout tabLayout = findViewById(R.id.tabLayout);// profile and reward tab in the profile activity
        ViewPager2 viewPager = findViewById(R.id.viewPager);// the space where the conent will be presented in the activity

        ProfilePagerAdapter adapter = new ProfilePagerAdapter(this);// create adapter
        viewPager.setAdapter(adapter);// give pager adapter to know what to show

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:// position 0 is profile
                    tab.setText("Profile");
                    break;
                case 1:// position 1 is rewards
                    tab.setText("Rewards");
                    break;
            }
        }).attach();//Start listening and linking TabLayout with ViewPager
    }

<<<<<<< Updated upstream
    //set up profile info
=======

    //this is used to down user info and connect it with views
>>>>>>> Stashed changes
    private void loadUserProfile() {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        String email = document.getString("email");
                        String phone = document.getString("phone");

                        tvUsername.setText(name != null ? name : "");
                        tvEmail.setText(email != null ? email : "");
                        tvPhone.setText(phone != null ? phone : "");

                        if (name != null && !name.isEmpty()) {
                            tvAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());// the pofile icon is set to be the first letter of the name
                        }
                    }
                });
    }
}
