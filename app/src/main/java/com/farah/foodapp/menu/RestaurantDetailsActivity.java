package com.farah.foodapp.menu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.farah.foodapp.R;
import com.farah.foodapp.cart.CartActivity;
import com.farah.foodapp.cart.CartManager;
import com.farah.foodapp.profile.ProfileActivity;
import com.farah.foodapp.reel.ReelsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class RestaurantDetailsActivity extends AppCompatActivity {

    private TextView tvRestaurantName, tvAddress;
    private RecyclerView recyclerViewMenu;
    private FoodAdapter adapter;
    private ArrayList<FoodItem> menuList;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        tvRestaurantName = findViewById(R.id.tvRestaurantName);
        tvAddress = findViewById(R.id.tvAddress);
        recyclerViewMenu = findViewById(R.id.recyclerViewMenu);

        recyclerViewMenu.setLayoutManager(new LinearLayoutManager(this));
        menuList = new ArrayList<>();
        adapter = new FoodAdapter(this, menuList, true);

        recyclerViewMenu.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        String restaurantId = getIntent().getStringExtra("restaurantId");
        if (restaurantId != null) {
            loadRestaurantDetails(restaurantId);
            loadMenuItems(restaurantId);

            Button btnShowChart = findViewById(R.id.btnShowChart);
            btnShowChart.setOnClickListener(v -> {
                Intent intent = new Intent(this, RatingsChartActivity.class);
                intent.putExtra("restaurantId", restaurantId);
                startActivity(intent);
            });
        } else {
            Toast.makeText(this, "No restaurant found!", Toast.LENGTH_SHORT).show();
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setBackgroundColor(getResources().getColor(R.color.primary));
        bottomNavigationView.setItemIconTintList(getResources().getColorStateList(R.color.primaryForeground));
        bottomNavigationView.setItemTextColor(getResources().getColorStateList(R.color.primaryForeground));
        bottomNavigationView.setSelectedItemId(R.id.nav_menu);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_reels) {
                startActivity(new Intent(this, ReelsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_menu) {
                return true;
            }
            return false;
        });

        updateCartBadge();
    }

    private void loadRestaurantDetails(String restaurantId) {
        db.collection("restaurants").document(restaurantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String address = documentSnapshot.getString("address");
                        tvRestaurantName.setText(name);
                        tvAddress.setText(address);
                    }
                });
    }

    private void loadMenuItems(String restaurantId) {
        db.collection("restaurants").document(restaurantId)
                .collection("menu")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    menuList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        String description = doc.getString("description");
                        String imageUrl = doc.getString("imageUrl");
                        double smallPrice = doc.getDouble("smallPrice") != null ? doc.getDouble("smallPrice") : 0.0;
                        double largePrice = doc.getDouble("largePrice") != null ? doc.getDouble("largePrice") : 0.0;
                        float rating = doc.getDouble("rating") != null ? doc.getDouble("rating").floatValue() : 0f;

                        FoodItem item = new FoodItem(
                                name != null ? name : "",
                                description != null ? description : "",
                                imageUrl != null ? imageUrl : "",
                                rating,
                                "",
                                smallPrice,
                                largePrice
                        );

                        item.setId(doc.getId());
                        item.setRestaurantId(restaurantId);

                        menuList.add(item);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading menu", Toast.LENGTH_SHORT).show());
    }

    public void updateCartBadge() {
        int count = CartManager.getTotalQuantity();
        if (bottomNavigationView != null) {
            if (count > 0) {
                bottomNavigationView.getOrCreateBadge(R.id.nav_cart).setNumber(count);
            } else {
                bottomNavigationView.removeBadge(R.id.nav_cart);
            }
        }
    }
}
