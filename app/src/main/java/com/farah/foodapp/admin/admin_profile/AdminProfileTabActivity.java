package com.farah.foodapp.admin.admin_profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.farah.foodapp.R;
import com.farah.foodapp.admin.activeorders.OrderAdmin;
import com.farah.foodapp.admin.admin_profile.orderhistory.OrderHistoryActivity;
import com.farah.foodapp.admin.admin_profile.specials.SpecialsActivity;
import com.farah.foodapp.profile.ChangePasswordActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminProfileTabActivity extends Fragment {

    private TextView tvTotalOrders;
    private LinearLayout layoutOrderHistory, layoutSpecials, layoutSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_tab_admin, container, false);

        //initialize views
        tvTotalOrders = view.findViewById(R.id.tv_total_orders);
        layoutOrderHistory = view.findViewById(R.id.layout_order_history);
        layoutSpecials = view.findViewById(R.id.layout_specials);
        layoutSettings = view.findViewById(R.id.layout_settings);

        layoutOrderHistory.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), OrderHistoryActivity.class)));

        layoutSpecials.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), SpecialsActivity.class)));

        layoutSettings.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ChangePasswordActivity.class)));

        fetchRestaurantNameAndLoadOrders();

        return view;
    }

    private void fetchRestaurantNameAndLoadOrders() {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseFirestore.getInstance()
                .collection("restaurants")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String restaurantName = doc.getString("name");
                        if (restaurantName != null) {
                            loadTotalOrders(restaurantName);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error fetching restaurant: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void loadTotalOrders(String restaurantName) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("orders")
                .get()
                .addOnSuccessListener(query -> {
                    List<OrderAdmin> tempOrders = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : query) {
                        String docRestaurant = doc.getString("restaurantName");
                        if (docRestaurant == null || !docRestaurant.trim().equalsIgnoreCase(restaurantName.trim()))
                            continue;

                        OrderAdmin order = doc.toObject(OrderAdmin.class);
                        order.setId(doc.getId());
                        tempOrders.add(order);
                    }

                    int totalOrders = tempOrders.size();
                    tvTotalOrders.setText(String.valueOf(totalOrders));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading orders: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
