package com.farah.foodapp.chatbot;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class RestaurantDataManager {
    private FirebaseFirestore db;
    private StringBuilder restaurantContext;

    // Callback interface to notify when data is loaded or an error occurs
    public interface DataLoadCallback {
        void onDataLoaded(String context);
        void onError(String error);
    }

    public RestaurantDataManager() {
        db = FirebaseFirestore.getInstance();
        restaurantContext = new StringBuilder();
    }

    public void loadRestaurantData(DataLoadCallback callback) {
        restaurantContext = new StringBuilder();
        restaurantContext.append("=== RESTAURANTS ===\n");

        db.collection("restaurants").get()
                .addOnSuccessListener(queryDocs -> {
                    if (queryDocs.isEmpty()) {
                        callback.onDataLoaded("No restaurants found.");
                        return;
                    }

                    // Track how many restaurants are left to process (because menu loading is async)
                    final int[] remaining = {queryDocs.size()};

                    for (QueryDocumentSnapshot restaurantDoc : queryDocs) {
                        String name = restaurantDoc.getString("name");
                        String address = restaurantDoc.getString("address");
                        String cuisine = restaurantDoc.getString("cuisine");

                        restaurantContext.append("Name: ").append(name != null ? name : "N/A").append("\n");
                        restaurantContext.append("Address: ").append(address != null ? address : "N/A").append("\n");
                        restaurantContext.append("Cuisine: ").append(cuisine != null ? cuisine : "N/A").append("\n");

                        restaurantDoc.getReference().collection("menu").get()
                                .addOnSuccessListener(menuDocs -> {
                                    if (!menuDocs.isEmpty()) {
                                        restaurantContext.append("Menu:\n");
                                        for (QueryDocumentSnapshot menuDoc : menuDocs) {
                                            String dish = menuDoc.getString("name");
                                            String description = menuDoc.getString("description");
                                            String imageUrl = menuDoc.getString("imageUrl");
                                            Double smallPrice = menuDoc.getDouble("smallPrice");
                                            Double largePrice = menuDoc.getDouble("largePrice");
                                            Double rating = menuDoc.getDouble("rating");
                                            Long ratingCount = menuDoc.getLong("ratingCount");

                                            restaurantContext.append(" - ").append(dish != null ? dish : "N/A");

                                            if (smallPrice != null || largePrice != null) {
                                                restaurantContext.append(" (");
                                                if (smallPrice != null) restaurantContext.append("small $").append(smallPrice);
                                                if (largePrice != null)
                                                    restaurantContext.append(smallPrice != null ? ", " : "")
                                                            .append("large $").append(largePrice);
                                                restaurantContext.append(")");
                                            }

                                            if (description != null && !description.isEmpty()) {
                                                restaurantContext.append(" - ").append(description);
                                            }

                                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                                restaurantContext.append(" [Image: ").append(imageUrl).append("]");
                                            }

                                            if (rating != null && ratingCount != null) {
                                                restaurantContext.append(" - Rating: ").append(rating)
                                                        .append(" (").append(ratingCount).append(" reviews)");
                                            }

                                            restaurantContext.append("\n");
                                        }
                                    } else {
                                        restaurantContext.append("Menu: None\n");
                                    }

                                    restaurantContext.append("\n");
                                    remaining[0]--;

                                    // If all restaurants are processed, call the callback
                                    if (remaining[0] == 0) {
                                        callback.onDataLoaded(restaurantContext.toString());
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    restaurantContext.append("Menu: Failed to load\n\n");
                                    remaining[0]--;
                                    if (remaining[0] == 0) {
                                        callback.onDataLoaded(restaurantContext.toString());
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage())); // error loading restaurants
    }
}
