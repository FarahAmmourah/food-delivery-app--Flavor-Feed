package com.farah.foodapp.admin.admin_profile.specials;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.farah.foodapp.R;
import com.farah.foodapp.admin.managemenu.FoodItemAdmin;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SpecialsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewSpecials;
    private LinearLayout emptyLayout;
    private SpecialsAdapter adapter;
    private List<FoodItemAdmin> specialList;
    private FirebaseFirestore db;

    private Button btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specials);

        recyclerViewSpecials = findViewById(R.id.recyclerViewSpecials);
        emptyLayout = findViewById(R.id.emptyLayout);
        btnBack = findViewById(R.id.btn_back);

        specialList = new ArrayList<>();
        adapter = new SpecialsAdapter(specialList);
        recyclerViewSpecials.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerViewSpecials.setLayoutManager(layoutManager);

        recyclerViewSpecials.setItemAnimator(null);
        db = FirebaseFirestore.getInstance();

        btnBack.setOnClickListener(v -> finish());

        loadSpecials();
    }

    private void loadSpecials() {
        String restaurantId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // load items with rating greater than 6
        db.collection("restaurants")
                .document(restaurantId)
                .collection("menu")
                .whereGreaterThan("rating", 6)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    specialList.clear();
                    specialList.addAll(querySnapshot.toObjects(FoodItemAdmin.class));
                    adapter.notifyDataSetChanged();

                    if (specialList.isEmpty()) {
                        recyclerViewSpecials.setVisibility(View.GONE);
                        emptyLayout.setVisibility(View.VISIBLE);
                    } else {
                        emptyLayout.setVisibility(View.GONE);
                        recyclerViewSpecials.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load specials: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
