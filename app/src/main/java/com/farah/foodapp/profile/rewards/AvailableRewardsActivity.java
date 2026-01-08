package com.farah.foodapp.profile.rewards;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.farah.foodapp.R;
import com.farah.foodapp.menu.RestaurantDetailsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.*;

public class AvailableRewardsActivity extends Fragment {

    private FirebaseFirestore db;
    private String currentUserId;

    //UI elements
    private ProgressBar progressRewards;
    private TextView tvRewardCounter;
    private LinearLayout topRestaurantsContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_available_rewards, container, false);

        //initialize firebase
        db = FirebaseFirestore.getInstance();
        //get current user id
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //find UI elements by id
        progressRewards = view.findViewById(R.id.progress_rewards);
        tvRewardCounter = view.findViewById(R.id.tv_reward_counter);
        topRestaurantsContainer = view.findViewById(R.id.topRestaurantsContainer);

        //emojis
        TextView e1 = view.findViewById(R.id.emoji1);
        TextView e2 = view.findViewById(R.id.emoji2);
        TextView e3 = view.findViewById(R.id.emoji3);
        TextView e4 = view.findViewById(R.id.emoji4);

        // start animation
        Animation dance = AnimationUtils.loadAnimation(getContext(), R.anim.dance);
        e1.startAnimation(dance);

        Animation dance2 = AnimationUtils.loadAnimation(getContext(), R.anim.dance);
        dance2.setStartOffset(150);
        e2.startAnimation(dance2);

        Animation dance3 = AnimationUtils.loadAnimation(getContext(), R.anim.dance);
        dance3.setStartOffset(300);
        e3.startAnimation(dance3);

        Animation dance4 = AnimationUtils.loadAnimation(getContext(), R.anim.dance);
        dance4.setStartOffset(450);
        e4.startAnimation(dance4);

        // Load user orders and render loyalty & rewards
        loadOrdersData();

        // Set click listeners for reward icons
        setupRewardClickListeners(view);

        return view;
    }

    //Load all orders for the current user and calculate stats
    private void loadOrdersData() {
        db.collection("orders")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Map to hold order counts per restaurant
                    HashMap<String, RestaurantStats> restaurantMap = new HashMap<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String restaurantName = doc.getString("restaurantName");
                        String logoUrl = doc.getString("restaurantLogoUrl");
                        String id = doc.getString("restaurantId");
                        if (restaurantName == null) continue;

                        RestaurantStats stats = restaurantMap.getOrDefault(
                                restaurantName,
                                new RestaurantStats(restaurantName, logoUrl, 0, id)
                        );
                        stats.count++;
                        restaurantMap.put(restaurantName, stats);
                    }

                    // Sort restaurants by most orders
                    List<RestaurantStats> list = new ArrayList<>(restaurantMap.values());
                    list.sort((a, b) -> Integer.compare(b.count, a.count));

                    // Render top restaurants in UI
                    renderTopRestaurants(list);

                    // Update loyalty progress and locked reward UI
                    updateProgress(querySnapshot.size(), restaurantMap.size());
                    updateLockedRewardSection(querySnapshot.size(), restaurantMap.size());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
                );
    }

    //Render top restaurants with stars, logos, and order counts
    private void renderTopRestaurants(List<RestaurantStats> list) {
        if (topRestaurantsContainer == null || getContext() == null) return;
        topRestaurantsContainer.removeAllViews();

        for (RestaurantStats r : list) {
            // Inflate single restaurant item layout
            View item = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_restaurant_progress, topRestaurantsContainer, false);

            ImageView logo = item.findViewById(R.id.imgRestaurantLogo);
            TextView name = item.findViewById(R.id.tvRestaurantName);
            TextView count = item.findViewById(R.id.tvOrderCount);
            TextView remaining = item.findViewById(R.id.tvRemaining);
            LinearLayout starContainer = item.findViewById(R.id.starContainer);
            Button btnOrder = item.findViewById(R.id.btnOrder);

            name.setText(r.name);
            count.setText("You ordered " + r.count + " times");

            // Star & reward logic
            int goal = 5;// orders needed for a reward
            int starsFilled = r.count % goal;
            int remainingToNext = goal - starsFilled;

            // Add empty stars to UI
            starContainer.removeAllViews();
            List<ImageView> stars = new ArrayList<>();
            for (int i = 0; i < goal; i++) {
                ImageView star = new ImageView(getContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(64, 64);
                lp.setMargins(3, 0, 3, 0);
                star.setLayoutParams(lp);
                star.setImageResource(R.drawable.ic_star_outline2);
                star.setScaleX(0f);
                star.setScaleY(0f);
                starContainer.addView(star);
                stars.add(star);
            }

            // Animate filled stars
            starContainer.postDelayed(() -> {
                for (int i = 0; i < starsFilled; i++) {
                    final ImageView s = stars.get(i);
                    s.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(250)
                            .setStartDelay(i * 150)
                            .withStartAction(() -> s.setImageResource(R.drawable.ic_star_filled))
                            .start();
                }
            }, 200);

            // Remaining orders message
            if (starsFilled == 0) {
                remaining.setText("Make your first order to start earning rewards!");
            } else if (remainingToNext == 0) {
                remaining.setText("🎉 Reward unlocked!");
            } else {
                remaining.setText(remainingToNext + " orders left to next reward");
            }

            // Load restaurant logo
            if (r.logoUrl != null && !r.logoUrl.isEmpty()) {
                Glide.with(this).load(r.logoUrl).into(logo);
            } else {
                logo.setImageResource(R.drawable.ic_restaurant);
            }

            // Handle order button click to open restaurant details
            btnOrder.setOnClickListener(v -> {
                if (r.id != null && !r.id.isEmpty()) {
                    Intent intent = new Intent(getContext(), RestaurantDetailsActivity.class);
                    intent.putExtra("restaurantId", r.id);
                    startActivity(intent);
                } else {
                    //find restaurant ID from Firestore
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("restaurants")
                            .whereEqualTo("name", r.name)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    String foundId = querySnapshot.getDocuments().get(0).getId();
                                    Intent intent = new Intent(getContext(), RestaurantDetailsActivity.class);
                                    intent.putExtra("restaurantId", foundId);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(getContext(),
                                            "Restaurant \"" + r.name + "\" not found",
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(),
                                            "Error loading restaurant: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show()
                            );
                }
            });

            topRestaurantsContainer.addView(item);
        }
    }

    //Update global loyalty progress and level stars
    private void updateProgress(int totalOrders, int uniqueRestaurants) {
        TextView tvLevel = getView().findViewById(R.id.tv_loyalty_level);
        LinearLayout globalStarContainer = getView().findViewById(R.id.globalStarContainer);

        // Calculate loyalty level based on total orders
        int levelIndex = 0;
        levelIndex = totalOrders / 10;


        String[] levelNames = {"Bronze", "Silver", "Gold", "Platinum", "Diamond", "Legend"};
        levelIndex = Math.min(levelIndex, levelNames.length - 1);
        String currentLevel = levelNames[levelIndex];

        // Progress percentage toward next level
        int currentThreshold = levelIndex * 10;
        int nextThreshold = (levelIndex + 1) * 10;
        int progressPercent = (int) (((float) (totalOrders - currentThreshold) / (nextThreshold - currentThreshold)) * 100);
        if (progressPercent > 100) progressPercent = 100;

        //update UI
        tvRewardCounter.setText("You made " + totalOrders + " orders across " + uniqueRestaurants + " restaurants");
        tvLevel.setText("Level: " + currentLevel);

        switch (currentLevel) {
            case "Silver": tvLevel.setTextColor(Color.parseColor("#C0C0C0")); break;
            case "Gold": tvLevel.setTextColor(Color.parseColor("#FFD700")); break;
            case "Platinum": tvLevel.setTextColor(Color.parseColor("#E5E4E2")); break;
            case "Diamond": tvLevel.setTextColor(Color.parseColor("#00FFFF")); break;
            case "Legend": tvLevel.setTextColor(Color.parseColor("#FF4500")); break;
            default: tvLevel.setTextColor(getResources().getColor(R.color.primary)); break;
        }

        // Animate global stars
        int totalStars = 5;
        globalStarContainer.removeAllViews();
        List<ImageView> stars = new ArrayList<>();

        for (int i = 0; i < totalStars; i++) {
            ImageView star = new ImageView(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(64, 64);
            lp.setMargins(6, 0, 6, 0);
            star.setLayoutParams(lp);
            star.setImageResource(R.drawable.ic_star_outline2);
            star.setScaleX(0f);
            star.setScaleY(0f);
            globalStarContainer.addView(star);
            stars.add(star);
        }

        int filledStars = (int) Math.ceil((progressPercent / 100.0) * totalStars);
        if (filledStars > totalStars) filledStars = totalStars;

        // Animate filled stars
        int finalFilledStars = filledStars;
        globalStarContainer.postDelayed(() -> {
            for (int i = 0; i < finalFilledStars; i++) {
                final ImageView s = stars.get(i);
                s.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(250)
                        .setStartDelay(i * 150)
                        .withStartAction(() -> s.setImageResource(R.drawable.ic_star_filled))
                        .start();
            }
            for (int i = finalFilledStars; i < totalStars; i++) {
                final ImageView s = stars.get(i);
                s.animate().scaleX(1f).scaleY(1f).setDuration(200).setStartDelay(finalFilledStars * 150).start();
            }
        }, 200);

        progressRewards.setProgress(progressPercent);

        // Show legendary badge if level is Legend
        if (currentLevel.equals("Legend")) {
            showLegendBadge();
        }
    }


    //Update the locked/unlocked reward section UI
    private void updateLockedRewardSection(int totalOrders, int uniqueRestaurants) {
        View root = getView();
        if (root == null) return;

        TextView tvTitle = root.findViewById(R.id.tvLockedRewardTitle);
        TextView tvSubtitle = root.findViewById(R.id.tvLockedRewardSubtitle);
        ImageView imgGift = root.findViewById(R.id.imgRewardGift);
        ImageView imgLock = root.findViewById(R.id.imgLockOverlay);

        // Next reward goal is the next multiple of 5
        int nextGoal = ((totalOrders / 5) + 1) * 5;
        int remaining = nextGoal - totalOrders;
        if (remaining < 0) remaining = 0;

        if (remaining == 0) {
            tvTitle.setText("🎉 Reward Unlocked!");
            tvTitle.setTextColor(getResources().getColor(R.color.primary));
            tvSubtitle.setText("You can now redeem your free reward.");
            imgLock.setVisibility(View.GONE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                imgGift.setRenderEffect(null); // remove blur
            } else {
                imgGift.animate().alpha(1f).setDuration(400).start();
            }

            // Add small scaling animation to gift
            imgGift.animate()
                    .scaleX(1.2f).scaleY(1.2f)
                    .setDuration(300)
                    .withEndAction(() -> imgGift.animate().scaleX(1f).scaleY(1f).setDuration(250))
                    .start();

        } else {
            tvTitle.setText("Order " + remaining + " more " + (remaining == 1 ? "time" : "times") + " to unlock your reward");
            tvTitle.setTextColor(Color.parseColor("#D32F2F"));
            tvSubtitle.setText("Earn a free meal worth up to $25 once unlocked");
            imgLock.setVisibility(View.VISIBLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                imgGift.setRenderEffect(RenderEffect.createBlurEffect(15f, 15f, Shader.TileMode.CLAMP));
            } else {
                imgGift.animate().alpha(0.4f).setDuration(400).start();
            }
        }
    }

    //Setup click listeners for reward icons
    private void setupRewardClickListeners(View root) {
        int[] rewardIds = {R.id.ic_delivery, R.id.ic_discount, R.id.ic_meal};
        String[] rewardNames = {"Free Delivery", "Discount 20%", "Free Meal"};

        for (int i = 0; i < rewardIds.length; i++) {
            int id = rewardIds[i];
            String rewardName = rewardNames[i];

            View rewardView = root.findViewById(id);
            if (rewardView != null) {
                rewardView.setOnClickListener(v -> showRewardDialog(rewardName));
            }
        }
    }


    //Show dialog to select restaurant to redeem reward
    private void showRewardDialog(String rewardName) {
        if (getContext() == null) return;

        db.collection("orders")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<String> restaurants = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String name = doc.getString("restaurantName");
                        if (name != null && !restaurants.contains(name)) {
                            restaurants.add(name);
                        }
                    }

                    if (restaurants.isEmpty()) {
                        Toast.makeText(getContext(), "No restaurants found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    CharSequence[] items = restaurants.toArray(new CharSequence[0]);

                    new AlertDialog.Builder(getContext())
                            .setTitle("Use " + rewardName + " at which restaurant?")
                            .setItems(items, (dialog, which) -> {
                                String chosenRestaurant = items[which].toString();

                                new AlertDialog.Builder(getContext())
                                        .setTitle("Confirm")
                                        .setMessage("Use " + rewardName + " at " + chosenRestaurant + "?")
                                        .setPositiveButton("Use", (d, w) -> redeemReward(rewardName, chosenRestaurant))
                                        .setNegativeButton("Cancel", null)
                                        .show();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load restaurants", Toast.LENGTH_SHORT).show()
                );
    }

    //Redeem reward and save to Firestore
    private void redeemReward(String rewardName, String restaurantName) {
        if (currentUserId == null) return;

        HashMap<String, Object> rewardData = new HashMap<>();
        rewardData.put("userId", currentUserId);
        rewardData.put("rewardName", rewardName);
        rewardData.put("restaurantName", restaurantName);
        rewardData.put("usedAt", System.currentTimeMillis());
        rewardData.put("status", "used");

        db.collection("reward_redemptions")
                .add(rewardData)
                .addOnSuccessListener(doc ->
                        Toast.makeText(getContext(), "Reward used at " + restaurantName, Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to redeem reward", Toast.LENGTH_SHORT).show()
                );
    }

    //Show dialog when user reaches Legend level
    private void showLegendBadge() {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("🏆 Legendary Status!")
                .setMessage("Congratulations! You’ve made 100+ orders and achieved LEGEND level. Enjoy lifetime perks and exclusive offers!")
                .setPositiveButton("Awesome!", null)
                .show();
    }

    //Class to hold restaurant stats for rendering
    private static class RestaurantStats {
        String name;
        String logoUrl;
        int count;
        String id;

        public RestaurantStats(String name, String logoUrl, int count, String id) {
            this.name = name;
            this.logoUrl = logoUrl;
            this.count = count;
            this.id = id;
        }
    }
}
