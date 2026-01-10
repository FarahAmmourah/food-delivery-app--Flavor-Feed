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

    private TextView tvRestaurantName, tvAddress;// from xml
    private RecyclerView recyclerViewMenu;// from xml
    private FoodAdapter adapter;
    private ArrayList<FoodItem> menuList;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);

        // the back btt onclick
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

<<<<<<< Updated upstream
        //find views
=======
// connect xml labels with java
>>>>>>> Stashed changes
        tvRestaurantName = findViewById(R.id.tvRestaurantName);
        tvAddress = findViewById(R.id.tvAddress);
        recyclerViewMenu = findViewById(R.id.recyclerViewMenu);

        // recycle takes linear so it displays the items in the list verticaly
        recyclerViewMenu.setLayoutManager(new LinearLayoutManager(this));
        menuList = new ArrayList<>();
        adapter = new FoodAdapter(this, menuList, true);

        // conects recycle view with adapter so it can find the data
        //that it wants to display, adap creates item food and fills its data
        recyclerViewMenu.setAdapter(adapter);

<<<<<<< Updated upstream
        //init db
        db = FirebaseFirestore.getInstance();

        //get admin info
=======

        // now download data from store
        db = FirebaseFirestore.getInstance();

        // we opened page using id so < check which one it is from db
>>>>>>> Stashed changes
        String restaurantId = getIntent().getStringExtra("restaurantId");
        if (restaurantId != null) {
            // go to fun and down data
            loadRestaurantDetails(restaurantId);//  brings name and address
            loadMenuItems(restaurantId);// load each meal and puts it in food item

            // opens rating page
            Button btnShowChart = findViewById(R.id.btnShowChart);
            btnShowChart.setOnClickListener(v -> {
                Intent intent = new Intent(this, RatingsChartActivity.class);
                intent.putExtra("restaurantId", restaurantId);// give extra info that this is for specific restu
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

        updateCartBadge();// keep cart uptodate when open the activity
    }

    private void loadRestaurantDetails(String restaurantId) {
        db.collection("restaurants").document(restaurantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");// rest name
                        String address = documentSnapshot.getString("address");// address
                        tvRestaurantName.setText(name);// conect them to xml
                        tvAddress.setText(address);
                    }
                });
    }

    private void loadMenuItems(String restaurantId) {
        db.collection("restaurants").document(restaurantId)
                .collection("menu")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    menuList.clear();// make sure list clear to down data again
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");// meal name
                        String description = doc.getString("description");
                        String imageUrl = doc.getString("imageUrl");
                        double smallPrice = doc.getDouble("smallPrice") != null ? doc.getDouble("smallPrice") : 0.0;
                        double largePrice = doc.getDouble("largePrice") != null ? doc.getDouble("largePrice") : 0.0;
                        float rating = doc.getDouble("rating") != null ? doc.getDouble("rating").floatValue() : 0f;


                        // make new obj from class food item to put meal
                        FoodItem item = new FoodItem(
                                name != null ? name : "",
                                description != null ? description : "",
                                imageUrl != null ? imageUrl : "",
                                rating,
                                "",// no need we are in the rest it self only used in search
                                smallPrice,
                                largePrice
                        );

                        item.setId(doc.getId());// id of the meal is set in food item used in rate
                        item.setRestaurantId(restaurantId);

                        menuList.add(item);
                    }
                    adapter.notifyDataSetChanged();// refresh adapter to notice that data change and refill
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading menu", Toast.LENGTH_SHORT).show());
    }

<<<<<<< Updated upstream
    //update cart counter
=======
    // update small num on the bottomnav
>>>>>>> Stashed changes
    public void updateCartBadge() {
        int count = CartManager.getTotalQuantity();// brings the number of orders in the cart
        if (bottomNavigationView != null) {// protect from crash if the nav not found dont do anything
            if (count > 0) {
                bottomNavigationView.getOrCreateBadge(R.id.nav_cart).setNumber(count);
            } else {
                bottomNavigationView.removeBadge(R.id.nav_cart);
            }
        }
    }
}
