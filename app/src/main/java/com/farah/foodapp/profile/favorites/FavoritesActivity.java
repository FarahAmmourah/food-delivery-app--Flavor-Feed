package com.farah.foodapp.profile.favorites;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.farah.foodapp.R;
import com.farah.foodapp.reel.ReelItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FavoritesAdapter favoritesAdapter;
    private List<ReelItem> favoriteList = new ArrayList<>();
    private LinearLayout layoutEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        //init the views
        recyclerView = findViewById(R.id.recyclerFavorites);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        favoritesAdapter = new FavoritesAdapter(this, favoriteList);
        recyclerView.setAdapter(favoritesAdapter);

        layoutEmpty = findViewById(R.id.layoutEmpty);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        loadFavorites();
    }

    private void loadFavorites() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid)
                .collection("favorites")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    favoriteList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        ReelItem reel = new ReelItem(
                                doc.getString("videoUrl"),
                                doc.getString("title"),
                                doc.getString("restaurant"),
                                0, 0, null,
                                doc.getDouble("price"),
                                doc.getString("restaurantId"),
                                doc.getString("reelId"),
                                doc.getString("imageUrl")
                        );
                        reel.setLiked(true);
                        favoriteList.add(reel);
                    }

                    favoritesAdapter.notifyDataSetChanged();

                    //handle empty
                    if (favoriteList.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> Log.e("Favorites", "Failed to load favorites", e));
    }
}
