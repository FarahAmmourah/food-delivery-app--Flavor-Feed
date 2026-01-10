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

        // init the views / connect the java to xml
        recyclerView = findViewById(R.id.recyclerFavorites);

        // make it look like a grid (3 reels in one row)
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // set adapter to fill the recycler view with info
        favoritesAdapter = new FavoritesAdapter(this, favoriteList);
        recyclerView.setAdapter(favoritesAdapter);

        // this layout is set to gone – will show if there is no liked reels
        layoutEmpty = findViewById(R.id.layoutEmpty);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        loadFavorites();
    }

    private void loadFavorites() {
        // user id
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid)
                .collection("favorites")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    // reset list to fill it again with newest info
                    favoriteList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        ReelItem reel = new ReelItem(
                                doc.getString("videoUrl"),
                                doc.getString("title"),
                                doc.getString("restaurant"),
                                0, 0, null, // no need for real num of likes or comments
                                doc.getDouble("price"),
                                doc.getString("restaurantId"),
                                doc.getString("reelId"),
                                doc.getString("imageUrl")
                        );

                        // liked is set true so that when we open no need to check with fb if liked
                        reel.setLiked(true);

                        // add reel to the list
                        favoriteList.add(reel);
                    }

                    // notify adapter so recycler view refreshes
                    favoritesAdapter.notifyDataSetChanged();

                    // handle empty state
                    // if no liked reels → show empty layout
                    if (favoriteList.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("Favorites", "Failed to load favorites", e)
                );
    }
}
