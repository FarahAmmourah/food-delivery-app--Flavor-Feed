package com.farah.foodapp.cart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.farah.foodapp.R;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    // Listener interface to notify when cart changes (item added/removed)
    public interface OnCartChangedListener {
        void onCartUpdated();
    }

    private Context context;
    private List<CartItem> cartItems;
    private OnCartChangedListener listener;

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartChangedListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.tvCartName.setText(item.getName() + " (" + item.getSize() + ")");
        holder.tvCartRestaurant.setText(item.getRestaurantName());
        holder.tvCartQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvCartPrice.setText("JOD " + String.format("%.2f", item.getPrice() * item.getQuantity()));

        // Load food image using Glide (with placeholder/error image)
        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_food_placeholder)
                .error(R.drawable.ic_food_placeholder)
                .into(holder.imgCartFood);

        holder.btnDecrease.setOnClickListener(v -> {
            CartManager.decreaseItem(item);// Decrease quantity
            notifyDataSetChanged();// Refresh the RecyclerView
            if (listener != null) listener.onCartUpdated();// Notify listener
        });

        holder.btnIncrease.setOnClickListener(v -> {
            CartManager.increaseItem(item);// increase quantity
            notifyDataSetChanged();// Refresh the recycle view
            if (listener != null) listener.onCartUpdated();// Notify listener
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    // ViewHolder class to hold item views
    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCartFood;
        TextView tvCartName, tvCartRestaurant, tvCartQuantity, tvCartPrice;
        ImageButton btnDecrease, btnIncrease;
        CardView cardView;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views by ID
            imgCartFood = itemView.findViewById(R.id.imgCartFood);
            tvCartName = itemView.findViewById(R.id.tvCartName);
            tvCartRestaurant = itemView.findViewById(R.id.tvCartRestaurant);
            tvCartQuantity = itemView.findViewById(R.id.tvCartQuantity);
            tvCartPrice = itemView.findViewById(R.id.tvCartPrice);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            cardView = (CardView) itemView;// Cast parent layout: View -> CardView
        }
    }
}
