package com.farah.foodapp.chatbot;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserDataManager {

    private FirebaseFirestore db;

    public interface DataLoadCallback {
        void onDataLoaded(String context);
        void onError(String error);
    }

    public UserDataManager() {
        db = FirebaseFirestore.getInstance();
    }

    public void loadUserDataWithOrders(String userId, DataLoadCallback callback) {
        //Load user info
        db.collection("users").document(userId).get()
                .addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) {
                        callback.onDataLoaded("User info not found.\n");
                        return;
                    }

                    StringBuilder context = new StringBuilder();
                    context.append("=== USER INFO ===\n");
                    context.append("Name: ").append(userDoc.getString("name")).append("\n");
                    context.append("Email: ").append(userDoc.getString("email")).append("\n");
                    context.append("Phone: ").append(userDoc.getString("phone")).append("\n");
                    context.append("Location: ").append(userDoc.getString("location")).append("\n\n");

                    //Load user's recent orders
                    db.collection("orders").whereEqualTo("userId", userId).get()
                            .addOnSuccessListener(orderDocs -> {
                                if (orderDocs.isEmpty()) {
                                    context.append("No recent orders.\n");
                                    callback.onDataLoaded(context.toString());
                                    return;
                                }

                                context.append("=== USER ORDERS ===\n");
                                for (QueryDocumentSnapshot orderDoc : orderDocs) {
                                    String restaurant = orderDoc.getString("restaurantName");
                                    String address = orderDoc.getString("address");
                                    String status = orderDoc.getString("status");
                                    Double total = orderDoc.getDouble("total");
                                    String eta = orderDoc.getString("eta");
                                    List<String> items = (List<String>) orderDoc.get("items");

                                    context.append("Restaurant: ").append(restaurant != null ? restaurant : "N/A").append("\n");
                                    context.append("Address: ").append(address != null ? address : "N/A").append("\n");
                                    context.append("ETA: ").append(eta != null ? eta : "N/A").append("\n");
                                    context.append("Status: ").append(status != null ? status : "N/A").append("\n");
                                    context.append("Total: ").append(total != null ? total + " JOD" : "N/A").append("\n");
                                    context.append("Items:\n");
                                    if (items != null) {
                                        for (String item : items) {
                                            context.append(" - ").append(item).append("\n");
                                        }
                                    }
                                    context.append("\n");
                                }

                                callback.onDataLoaded(context.toString());
                            })
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage())); // error loading info
    }
}
