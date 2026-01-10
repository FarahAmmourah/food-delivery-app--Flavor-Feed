package com.farah.foodapp.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.farah.foodapp.R;
import com.farah.foodapp.chatbot.AIChatbotActivity;
import com.farah.foodapp.orders.OrdersActivity;
import com.farah.foodapp.profile.favorites.FavoritesActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class ProfileTabActivity extends Fragment {

    private TextView tvTotalOrders;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.profile_tab, container, false);

        //find and assign views by id
        tvTotalOrders = view.findViewById(R.id.tv_total_orders);
        LinearLayout layoutSettings = view.findViewById(R.id.layout_settings);//these are layouts but actlike button
        LinearLayout layoutOrderHistory = view.findViewById(R.id.layout_order_history);//these are layouts but actlike button
        LinearLayout layoutFavorites = view.findViewById(R.id.layout_favorites);//these are layouts but actlike button
        LinearLayout layoutChatbot = view.findViewById(R.id.layout_chatbot);//these are layouts but actlike button

        layoutChatbot.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AIChatbotActivity.class);
            startActivity(intent);
        });

        layoutSettings.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ChangePasswordActivity.class)));

        layoutOrderHistory.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), OrdersActivity.class)));

        layoutFavorites.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), FavoritesActivity.class)));

        loadTotalOrders();

        return view;
    }

    private void loadTotalOrders() {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseFirestore.getInstance()
                .collection("orders")
                .whereEqualTo("userId", uid)// bring the order that has a certain id
                .get()
                .addOnSuccessListener(query -> tvTotalOrders.setText(String.valueOf(query.size())))
                .addOnFailureListener(e -> tvTotalOrders.setText("0"));
    }
}
