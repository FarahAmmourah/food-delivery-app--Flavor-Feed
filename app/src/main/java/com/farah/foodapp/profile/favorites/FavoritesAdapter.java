package com.farah.foodapp.profile.favorites;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.farah.foodapp.R;
import com.farah.foodapp.reel.ReelItem;
import com.farah.foodapp.reel.ReelsActivity;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavViewHolder> {

    private Context context;
    private List<ReelItem> favoriteList;

    public FavoritesAdapter(Context context, List<ReelItem> favoriteList) {
        this.context = context;
        this.favoriteList = favoriteList;
    }

    @NonNull
    @Override
    public FavViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_reel, parent, false);
        return new FavViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavViewHolder holder, int position) {
        ReelItem reel = favoriteList.get(position);

        //helps loading images
        Glide.with(context)
                .load(reel.getVideoUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(holder.imgThumbnail);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReelsActivity.class);
            intent.putExtra("openReelId", reel.getReelId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    public static class FavViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        public FavViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
        }
    }
}
