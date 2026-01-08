package com.farah.foodapp.cart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.farah.foodapp.CheckoutActivity;
import com.farah.foodapp.R;
import com.farah.foodapp.menu.MenuActivity;
import com.farah.foodapp.profile.ProfileActivity;
import com.farah.foodapp.reel.ReelsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartChangedListener {

    private LinearLayout layoutEmptyCart;
    private TextView tvSubtotal, tvDelivery, tvTotalPrice;
    private Button btnCancelCart, btnOrderNow;
    private BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerCart;
    private CartAdapter cartAdapter;

    private ActivityResultLauncher<Intent> checkoutLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        //initialize views
        layoutEmptyCart = findViewById(R.id.layoutEmptyCart);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvDelivery = findViewById(R.id.tvDelivery);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnCancelCart = findViewById(R.id.btnCancelCart);
        btnOrderNow = findViewById(R.id.btnOrderNow);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        recyclerCart = findViewById(R.id.recyclerCart);

        bottomNavigationView.setSelectedItemId(R.id.nav_cart);
        updateCartBadge();

        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, CartManager.getCartItems(), this);
        recyclerCart.setAdapter(cartAdapter);

        updateCartUI();

        btnCancelCart.setOnClickListener(v -> {
            CartManager.clearCart();
            updateCartUI();
            updateCartBadge();
            Toast.makeText(this, "Cart cleared", Toast.LENGTH_SHORT).show();
        });

        checkoutLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        boolean orderPlaced = result.getData().getBooleanExtra("orderPlaced", false);
                        if (orderPlaced) {
                            updateCartUI();
                            updateCartBadge();
                            Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        btnOrderNow.setOnClickListener(v -> {
            if (CartManager.getSubtotal() > 0) {
                Intent intent = new Intent(this, CheckoutActivity.class);
                checkoutLauncher.launch(intent);
            } else {
                Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show();
            }
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_reels) {
                startActivity(new Intent(this, ReelsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_menu) {
                startActivity(new Intent(this, MenuActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_cart) {
                return true;
            }
            return false;
        });
    }

    private void updateCartUI() {
        String subtotal = String.format("%.2f", CartManager.getSubtotal());
        String delivery = String.format("%.2f", CartManager.getDeliveryFee());
        String total = String.format("%.2f", CartManager.getTotalPrice());

        tvSubtotal.setText("Subtotal: " + subtotal + " JD");
        tvDelivery.setText("Delivery: " + delivery + " JD");
        tvTotalPrice.setText("Total: " + total + " JD");

        if (CartManager.getCartItems().isEmpty()) {
            // if no items show empty cart view
            layoutEmptyCart.setVisibility(LinearLayout.VISIBLE);
            recyclerCart.setVisibility(RecyclerView.GONE);
        } else {
            layoutEmptyCart.setVisibility(LinearLayout.GONE);
            recyclerCart.setVisibility(RecyclerView.VISIBLE);
            cartAdapter.notifyDataSetChanged();
        }
    }

    private void updateCartBadge() {
        int count = CartManager.getTotalQuantity();
        if (count > 0) {
            bottomNavigationView.getOrCreateBadge(R.id.nav_cart).setNumber(count);
        } else {
            bottomNavigationView.removeBadge(R.id.nav_cart);
        }
    }

    @Override
    public void onCartUpdated() {
        updateCartUI();
        updateCartBadge();
    }
}
