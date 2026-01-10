package com.farah.foodapp.reel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.farah.foodapp.R;
import com.farah.foodapp.cart.CartActivity;
import com.farah.foodapp.cart.CartManager;
import com.farah.foodapp.menu.MenuActivity;
import com.farah.foodapp.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ReelsActivity extends AppCompatActivity {

    private ViewPager2 viewPagerReels;// allows to scroll between reels and display one at a time
    private ReelsAdapter reelsAdapter;//  fills the container with data about the reel
    private List<ReelItem> reelList = new ArrayList<>(); // all reels passed from the firestore

    private static final String PREF_NAME = "reels_prefs";
    private static final String KEY_LAST_POSITION = "last_position";// used yo know what is the last vid opened
    private int lastPosition = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reels);

        viewPagerReels = findViewById(R.id.viewPagerReels);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
        bottomNavigationView.setItemIconTintList(ContextCompat.getColorStateList(this, R.color.primaryForeground));
        bottomNavigationView.setItemTextColor(ContextCompat.getColorStateList(this, R.color.primaryForeground));

        reelsAdapter = new ReelsAdapter(this, reelList);
        viewPagerReels.setAdapter(reelsAdapter);// connect adapter yo reels activity

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);// andro small ram,  PREF_NAME = "reels_prefs"
        lastPosition = prefs.getInt(KEY_LAST_POSITION, 0);//KEY_LAST_POSITION = "last_position"
        // if there is no val return zero

        loadReelsFromFirestore();// method to down db reels

        bottomNavigationView.setSelectedItemId(R.id.nav_reels); // we are in the reels so it must be selected
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            pauseAllVideos();// pause all vid

            if (id == R.id.nav_menu) {
                startActivity(new Intent(this, MenuActivity.class));
                overridePendingTransition(0, 0);// no animation
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_reels) {
                return true;
            }
            return false;
        });

//listener when the user swipe the followinf code will excute
        viewPagerReels.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {/*
        view pager is responsibe for knowing the position of the reel and gets it from the adapter*/

                super.onPageSelected(position);
                lastPosition = position;
                playOnlyCurrent(position); // call func and give it position
                saveLastPosition(position);
            }
        });
//
        updateCartBadge();
    }

    private void saveLastPosition(int position) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        prefs.edit().putInt(KEY_LAST_POSITION, position).apply();
    }

    private void playOnlyCurrent(int position) {/*pager has recycle inside we need
    to call and the first element index zero*/
        RecyclerView recyclerView = (RecyclerView) viewPagerReels.getChildAt(0);
        if (recyclerView == null) return;

// pager has elements check each one to play the one with the wanted position
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View view = recyclerView.getChildAt(i);
            RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(view);
            if (holder instanceof ReelsAdapter.ReelViewHolder) {// check its from type reel
                ReelsAdapter.ReelViewHolder reelHolder = (ReelsAdapter.ReelViewHolder) holder;
                if (reelHolder.getBindingAdapterPosition() == position) {// if the holder has same position
                    if (reelHolder.playerView.getPlayer() != null)// checks if player is ready
                        reelHolder.playerView.getPlayer().play();
                }
                else {// the position is not the same one from the adapter so vid will pause
                    if (reelHolder.playerView.getPlayer() != null)
                        reelHolder.playerView.getPlayer().pause();
                }
            }
        }
    }

    private void pauseAllVideos() {/*this is used to stop all vid so it wont be playing in background
    or if you want to switch pages we call this function */

        RecyclerView recyclerView = (RecyclerView) viewPagerReels.getChildAt(0);
        if (recyclerView == null) return;
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View view = recyclerView.getChildAt(i);
            RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(view);
            if (holder instanceof ReelsAdapter.ReelViewHolder) {
                ReelsAdapter.ReelViewHolder reelHolder = (ReelsAdapter.ReelViewHolder) holder;
                if (reelHolder.playerView.getPlayer() != null)
                    reelHolder.playerView.getPlayer().pause();
            }
        }
    }
// called from on create
    private void loadReelsFromFirestore() {
        FirebaseFirestore.getInstance()
                .collectionGroup("reels")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {// after reels are retrived
                    reelList.clear();// delete old data to read new ones and no redundant

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            String videoUrl = doc.getString("videoUrl");
                            String title = doc.getString("title");
                            String restaurant = doc.getString("restaurant");
                            String restaurantId = doc.getString("restaurantId");
                            String imageUrl = doc.getString("imageUrl");// real one comes from menu

                            Long likesVal = doc.getLong("likesCount");
                            int likes = likesVal != null ? likesVal.intValue() : 0;

                            Long commentsVal = doc.getLong("commentsCount");
                            int commentsCount = commentsVal != null ? commentsVal.intValue() : 0;

                            Double priceVal = doc.getDouble("price");// real one comes from menu
                            double price = priceVal != null ? priceVal : 0.0;

                            List<String> comments = (List<String>) doc.get("comments");
                            if (comments == null) comments = new ArrayList<>();

                            reelList.add(new ReelItem(/*list of rel items objects that takes
                            info from db*/
                                    videoUrl,
                                    title,
                                    restaurant,
                                    likes,
                                    commentsCount,
                                    comments,
                                    price,
                                    restaurantId,
                                    doc.getId(),
                                    imageUrl
                            ));
                        } catch (Exception e) {
                            Log.e("Firestore", "Error parsing document", e);
                        }
                    }

                    reelsAdapter.notifyDataSetChanged();

                    viewPagerReels.post(() -> {
                        viewPagerReels.setCurrentItem(lastPosition, false);
                        playOnlyCurrent(lastPosition);// takes the reels from adap and show them on reel activity
                    });

                    Log.d("Firestore", "Loaded reels count: " + reelList.size());
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to load reels", e));
    }

    public void updateCartBadge() {
        int count = CartManager.getTotalQuantity();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (count > 0) {
            bottomNav.getOrCreateBadge(R.id.nav_cart).setNumber(count);
        } else {
            bottomNav.removeBadge(R.id.nav_cart);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseAllVideos();
    }

    @Override
    protected void onStop() {
        super.onStop();
        pauseAllVideos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewPagerReels.postDelayed(() -> {
            viewPagerReels.setCurrentItem(lastPosition, false);
            playOnlyCurrent(lastPosition);
        }, 250);
    }
    public void notifyReelUpdated() {
        reelsAdapter.notifyDataSetChanged();// redraw the reel again to download new info on screen
    }

}