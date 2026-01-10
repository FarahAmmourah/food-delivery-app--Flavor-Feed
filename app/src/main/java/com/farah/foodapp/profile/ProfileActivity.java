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

        //init views
        tvAvatar = findViewById(R.id.tv_avatar);
        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);

        setupBottomNav();
        setupTabs();
        loadUserProfile();
    }

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
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        ProfilePagerAdapter adapter = new ProfilePagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Profile");
                    break;
                case 1:
                    tab.setText("Rewards");
                    break;
            }
        }).attach();
    }

    //set up profile info
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
                            tvAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
                        }
                    }
                });
    }
}
