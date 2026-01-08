package com.farah.foodapp.admin.activeorders;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.farah.foodapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActiveOrdersActivity extends Fragment {

    private RecyclerView recyclerView;
    private ActiveOrdersAdapter adapter;
    private final List<OrderAdmin> orders = new ArrayList<>();
    private FirebaseFirestore firestore;
    private TextView tvNoOrders;
    private ListenerRegistration activeOrdersListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_active_orders, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewActiveOrders);
        tvNoOrders = view.findViewById(R.id.tvNoOrders);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ActiveOrdersAdapter(orders);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        String restaurantId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // fetch restaurant's name based on its id
        firestore.collection("restaurants")
                .document(restaurantId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String restaurantName = doc.getString("name");
                        if (restaurantName != null && !restaurantName.isEmpty()) {
                            listenToActiveOrders(restaurantName);
                        } else {
                            Toast.makeText(getContext(), "Restaurant name missing", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Restaurant not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to fetch restaurant: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );

        return view;
    }

    private void listenToActiveOrders(String restaurantName) {
        if (activeOrdersListener != null) {
            activeOrdersListener.remove();
        }

        activeOrdersListener = firestore.collection("orders")
                .whereEqualTo("restaurantName", restaurantName)
                .whereIn("status", Arrays.asList("Preparing", "Pending"))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            tvNoOrders.setText("Error loading orders: " + e.getMessage());
                            tvNoOrders.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            return;
                        }

                        if (snapshots == null || snapshots.isEmpty()) {
                            orders.clear();
                            adapter.notifyDataSetChanged();
                            recyclerView.setVisibility(View.GONE);
                            tvNoOrders.setVisibility(View.VISIBLE);
                            return;
                        }

                        List<OrderAdmin> tempOrders = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            OrderAdmin order = doc.toObject(OrderAdmin.class);
                            order.setId(doc.getId());
                            tempOrders.add(order);
                        }

                        fetchUserNames(tempOrders);
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchUserNames(List<OrderAdmin> tempOrders) {
        if (tempOrders.isEmpty()) {
            orders.clear();
            adapter.notifyDataSetChanged();
            recyclerView.setVisibility(View.GONE);
            tvNoOrders.setVisibility(View.VISIBLE);
            return;
        }

        final int[] remaining = {tempOrders.size()};

        for (OrderAdmin order : tempOrders) {
            firestore.collection("users")
                    .document(order.getUserId())
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            order.setCustomerName(userDoc.getString("name"));
                        } else {
                            order.setCustomerName("Unknown");
                        }
                    })
                    .addOnCompleteListener(task -> {
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            orders.clear();
                            orders.addAll(tempOrders);
                            adapter.notifyDataSetChanged();
                            recyclerView.setVisibility(View.VISIBLE);
                            tvNoOrders.setVisibility(View.GONE);
                        }
                    });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (activeOrdersListener != null) {
            activeOrdersListener.remove();
            activeOrdersListener = null;
        }
    }
}
