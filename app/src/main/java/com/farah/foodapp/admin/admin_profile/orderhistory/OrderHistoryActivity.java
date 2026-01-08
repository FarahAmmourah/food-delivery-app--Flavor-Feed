package com.farah.foodapp.admin.admin_profile.orderhistory;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.farah.foodapp.R;
import com.farah.foodapp.admin.activeorders.OrderAdmin;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerOrders;
    private OrderHistoryAdapter adapter;
    private List<OrderAdmin> orderList = new ArrayList<>();
    private TextView tvNoOrders;
    private Button btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        recyclerOrders = findViewById(R.id.recyclerOrders);
        tvNoOrders = findViewById(R.id.tvNoOrders);
        btnBack = findViewById(R.id.btn_back);

        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderHistoryAdapter(orderList);
        recyclerOrders.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        fetchRestaurantNameAndLoadOrders();
    }

    private void fetchRestaurantNameAndLoadOrders() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("restaurants")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String restaurantName = doc.getString("name");
                        if (restaurantName != null) {
                            loadHistoryOrders(restaurantName);
                        } else {
                            Toast.makeText(this, "Restaurant name not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error fetching restaurant: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void loadHistoryOrders(String restaurantName) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("orders")
                .get()
                .addOnSuccessListener(query -> {
                    orderList.clear();
                    List<OrderAdmin> tempOrders = new ArrayList<>();

                    // Filter orders by restaurant and status
                    for (QueryDocumentSnapshot doc : query) {
                        String docRestaurant = doc.getString("restaurantName");
                        if (docRestaurant == null || !docRestaurant.equals(restaurantName)) continue;

                        OrderAdmin order = doc.toObject(OrderAdmin.class);
                        order.setId(doc.getId());

                        Long createdAtMillis = doc.getLong("createdAt");
                        if (createdAtMillis != null) {
                            order.setCreatedAt(createdAtMillis);
                        }

                        String address = doc.getString("address");
                        if (address != null) {
                            order.setCustomerAddress(address);
                        }

                        // Add only completed or cancelled orders
                        if (order.getStatus() != null &&
                                (order.getStatus().equalsIgnoreCase("completed") ||
                                        order.getStatus().equalsIgnoreCase("cancelled"))) {
                            tempOrders.add(order);
                        }
                    }

                    final int[] remaining = {tempOrders.size()};
                    if (remaining[0] == 0) {
                        // No orders to show
                        tvNoOrders.setVisibility(TextView.VISIBLE);
                        recyclerOrders.setVisibility(RecyclerView.GONE);
                        return;
                    }

                    for (OrderAdmin order : tempOrders) {
                        String userId = order.getUserId();
                        if (userId == null) {
                            remaining[0]--;
                            continue;
                        }

                        // Fetch customer names
                        firestore.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        order.setCustomerName(userDoc.getString("name"));
                                    }
                                })
                                .addOnCompleteListener(task -> {
                                    remaining[0]--;
                                    if (remaining[0] == 0) {
                                        // When all user names are fetched
                                        orderList.clear();
                                        orderList.addAll(tempOrders);

                                        Collections.sort(orderList, (o1, o2) -> {
                                            if (o1.getCreatedAt() == null || o2.getCreatedAt() == null) return 0;
                                            return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                                        });

                                        // Show orders or no orders message
                                        if (orderList.isEmpty()) {
                                            tvNoOrders.setVisibility(TextView.VISIBLE);
                                            recyclerOrders.setVisibility(RecyclerView.GONE);
                                        } else {
                                            tvNoOrders.setVisibility(TextView.GONE);
                                            recyclerOrders.setVisibility(RecyclerView.VISIBLE);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                    }

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading orders: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
