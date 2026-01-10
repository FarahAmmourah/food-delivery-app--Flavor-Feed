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

    private TextView tvRestaurantName, tvAddress; // from xml
    private RecyclerView recyclerViewMenu;        // from xml
    private FoodAdapter adapter;
    private ArrayList<FoodItem> menuList;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);

        // the back btn onclick
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        // find views / connect xml labels with java
        tvRestaurantName = findViewById(R.id.tvRestaurantName);
        tvAddress = findViewById(R.id.tvAddress);
        recyclerViewMenu = findViewById(R.id.recyclerViewMenu);

        // recycler takes linear so it displays the items in the list vertically
        recyclerViewMenu.setLayoutManager(new LinearLayoutManager(this));

        menuList = new ArrayList<>();
        adapter = new FoodAdapter(this, menuList, true);

        // connects recycler view with adapter so it can find the data
        // that it wants to display, adapter creates item food and fills its data
        recyclerViewMenu.setAdapter(adapter);

        // init db / now download data from firestore
        db = FirebaseFirestore.getInstance();

        // we opened page using id so check which restaurant it is from db
        String restaurantId = getIntent().getStringExtra("restaurantId");
        if (restaurantId != null) {

            // go to functions and download data
            loadRestaurantDetails(restaurantId); // brings name and address
            loadMenuItems(restaurantId);         // load each meal and put it in food item

            // opens rating page
            Button btnShowChart = findViewById(R.id.btnShowChart);
            btnShowChart.setOnClickListener(v -> {
                Intent intent = new Intent(this, RatingsChartActivity.class);
                intent.putExtra("restaurantId", restaurantId); // specific restaurant
                startActivity(intent);
            });

        } else {
            Toast.makeText(this, "No restaurant found!", Toast.LENGTH_SHORT).show();
        }

        // bottom navigation setup
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

        updateCartBadge(); // keep cart up to date when opening the activity
    }

    private void loadRestaurantDetails(String restaurantId) {
        db.collection("restaurants").document(restaurantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");     // restaurant name
                        String address = documentSnapshot.getString("address"); // address
                        tvRestaurantName.setText(name); // connect them to xml
                        tvAddress.setText(address);
                    }
                });
    }

    private void loadMenuItems(String restaurantId) {
        db.collection("restaurants").document(restaurantId)
                .collection("menu")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    // make sure list is clear to download data again
                    menuList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name"); // meal name
                        String description = doc.getString("description");
                        String imageUrl = doc.getString("imageUrl");

                        double smallPrice = doc.getDouble("smallPrice") != null
                                ? doc.getDouble("smallPrice") : 0.0;

                        double largePrice = doc.getDouble("largePrice") != null
                                ? doc.getDouble("largePrice") : 0.0;

                        float rating = doc.getDouble("rating") != null
                                ? doc.getDouble("rating").floatValue() : 0f;

                        // make new object from FoodItem class
                        FoodItem item = new FoodItem(
                                name != null ? name : "",
                                description != null ? description : "",
                                imageUrl != null ? imageUrl : "",
                                rating,
                                "", // no need, we are inside restaurant page
                                smallPrice,
                                largePrice
                        );

                        // set ids for rating and linking
                        item.setId(doc.getId());
                        item.setRestaurantId(restaurantId);

                        menuList.add(item);
                    }

                    // refresh adapter to refill data
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading menu", Toast.LENGTH_SHORT).show()
                );
    }

    // update cart counter / update small num on the bottom nav
    public void updateCartBadge() {
        int count = CartManager.getTotalQuantity(); // number of orders in cart

        if (bottomNavigationView != null) { // protect from crash
            if (count > 0) {
                bottomNavigationView.getOrCreateBadge(R.id.nav_cart).setNumber(count);
            } else {
                bottomNavigationView.removeBadge(R.id.nav_cart);
            }
        }
    }
}
