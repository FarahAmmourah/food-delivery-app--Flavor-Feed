package com.farah.foodapp.menu;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MenuActivity extends AppCompatActivity {

    private RecyclerView recyclerMenu;
    private FoodAdapter adapter;
    private List<FoodItem> foodList;
    private EditText etSearch;
    private BottomNavigationView bottomNavigationView;

    private FirebaseFirestore firestore;
    private String restaurantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);// connect to xml

        recyclerMenu = findViewById(R.id.recyclerMenu);// connect to recycle in xml
        recyclerMenu.setLayoutManager(new LinearLayoutManager(this));// vertical view

        etSearch = findViewById(R.id.etSearch);
//Adapter creation
        foodList = new ArrayList<>();//empty list
        adapter = new FoodAdapter(this, foodList, false);// give list to adapter
        recyclerMenu.setAdapter(adapter);// connect adapter to recycle view

// load info
        firestore = FirebaseFirestore.getInstance();// connect to firestore
        restaurantId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        loadMenuFromFirestore();/*this function brings data from menu
        make it into food item put it in lit gives it to adapter*/


        //how search works?
        etSearch.addTextChangedListener(new TextWatcher() {/*when user adds letters to search
        textwatcher implements 3 func*/
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            // not used ,compile error if removed

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
                //calls the filter in adapter when user enter letter
            }

            @Override
            public void afterTextChanged(Editable s) { }
            // not used ,compile error if removed
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_menu);

        updateCartBadge();// called when we open this activity to refresh counter

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_reels) {
                startActivity(new Intent(this, ReelsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_menu) {
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void loadMenuFromFirestore() {
        firestore.collectionGroup("menu")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    foodList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        FoodItem item = doc.toObject(FoodItem.class);
                        item.setId(doc.getId()); // meal id
                        item.setRestaurantId(doc.getString("restaurantId"));
                        item.setRestaurantName(doc.getString("restaurantName"));
                        foodList.add(item);

                    }

                    adapter.setFoodListFull(foodList);
                    adapter.notifyDataSetChanged();

                 // tells to research again after the meals are loaded
                    adapter.getFilter().filter(etSearch.getText().toString());

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load all menu items: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
// change the number on the nav bar when new added
    public void updateCartBadge() {
        int count = CartManager.getTotalQuantity();
        if (count > 0) {
            bottomNavigationView.getOrCreateBadge(R.id.nav_cart).setNumber(count);
        } else {
            bottomNavigationView.removeBadge(R.id.nav_cart);
        }
    }
}
