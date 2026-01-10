package com.farah.foodapp.orders;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.farah.foodapp.R;
import com.farah.foodapp.profile.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    private OrdersAdapter adapter;
    private ListenerRegistration orderStatusListener;

    private final List<OrderModel> orderList = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        RecyclerView recyclerOrders = findViewById(R.id.recyclerOrders);
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));

        adapter = new OrdersAdapter(orderList);
        recyclerOrders.setAdapter(adapter);

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });

        listenForOrderStatusChanges();
    }

    @Override
    protected void onStart() {
        super.onStart();
        listenForOrderStatusChanges();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (orderStatusListener != null) orderStatusListener.remove();
    }
    private void listenForOrderStatusChanges() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        orderStatusListener = FirebaseFirestore.getInstance()
                .collection("orders")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    orderList.clear(); // populate history
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        OrderModel order = dc.getDocument().toObject(OrderModel.class);
                        order.setId(dc.getDocument().getId());
                        orderList.add(order);

                        // Only send notifications for modified orders
                        if (dc.getType() == DocumentChange.Type.MODIFIED) {
                            String status = dc.getDocument().getString("status");
                            Boolean notified = dc.getDocument().getBoolean("notifiedCustomer");

                            if (notified != null && notified) continue;

                            if ("Completed".equals(status) || "Cancelled".equals(status)) {
                                showCustomerNotification(
                                        "Order Update",
                                        "Your order has been " + status
                                );
                                dc.getDocument().getReference()
                                        .update("notifiedCustomer", true);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    //Set notifications
    private void showCustomerNotification(String title, String message) {
        String channelId = "customer_channel";

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Customer Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }
        //build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.logo_app)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
