package com.farah.foodapp.reel;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.farah.foodapp.R;
import com.farah.foodapp.cart.CartManager;
import com.farah.foodapp.comments.CommentsDialog;
import com.farah.foodapp.menu.RestaurantDetailsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReelsAdapter extends RecyclerView.Adapter<ReelsAdapter.ReelViewHolder> {

    private final Context context;/* is where the app at now,
    and we will always call adapter from reelactivity*/
    private final List<ReelItem> reelList;

    public ReelsAdapter(Context context, List<ReelItem> reelList) {
        this.context = context;
        this.reelList = reelList;
    }

    @NonNull
    @Override
    //pram1: the recycle view pram2:
    public ReelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reel, parent, false);// turn xml to real view
        //flase because we want it to be alone not attached to anything
        return new ReelViewHolder(view);
    }

    public static class ReelViewHolder extends RecyclerView.ViewHolder {/*
    كائن وسيط عشان الريسايكل فيو يتعامل معه بدل ما يتعامل مباشره مع ال فيو*/
        PlayerView playerView;
        TextView tvTitle, tvRestaurant, tvLikeCount, tvCommentCount;
        ImageButton btnLike, btnComment, btnShare;
        Button btnOrder;
        ImageView ivPlayPause;// those are the item reel elements and this function is used to limit the use of find view by id for each vid

        public ReelViewHolder(@NonNull View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.playerView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvRestaurant = itemView.findViewById(R.id.tvRestaurant);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnShare = itemView.findViewById(R.id.btnShare);
            btnOrder = itemView.findViewById(R.id.btnOrder);
            ivPlayPause = itemView.findViewById(R.id.ivPlayPause);
        }
    }

    @Override
    /*this is called by recycleview for each reel ,
    takes the shape view and the position of the reel */
    public void onBindViewHolder(@NonNull ReelViewHolder holder, int position) {
        ReelItem reel = reelList.get(position); // gets the reelitem of the pos
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(uid)
                .collection("favorites")
                .document(reel.getReelId())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        reel.setLiked(true);
                        holder.btnLike.setColorFilter(Color.RED);
                    } else {
                        reel.setLiked(false);
                        holder.btnLike.setColorFilter(Color.WHITE);
                    }
                });

        ExoPlayer player = new ExoPlayer.Builder(context).build();
        holder.playerView.setPlayer(player);// the place where the vid is shown and played by player
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(reel.getVideoUrl()));// change the type of vid so the player can understand
        player.setMediaItem(mediaItem);
        player.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);// repeat when done
        player.prepare();// down vid prep sound and content
        player.pause();

// to show icons when vid is playing or pausing
        holder.playerView.setOnClickListener(v -> {
            if (player.isPlaying()) {
                player.pause();
                holder.ivPlayPause.setImageResource(R.drawable.ic_play);
                holder.ivPlayPause.setVisibility(View.VISIBLE);
            } else {
                player.play();
                holder.ivPlayPause.setImageResource(R.drawable.ic_pause);
                holder.ivPlayPause.setVisibility(View.VISIBLE);
            }
            holder.ivPlayPause.postDelayed(() -> holder.ivPlayPause.setVisibility(View.GONE), 800);
        });
//update ui info
        holder.tvTitle.setText(reel.getTitle());
        holder.tvRestaurant.setText(reel.getRestaurant());
        holder.btnOrder.setText("ORDER NOW");
        holder.tvLikeCount.setText(String.valueOf(reel.getLikesCount()));
        holder.tvCommentCount.setText(String.valueOf(reel.getCommentsCount()));
// open the restu menu if clicked on the name using ret id
        holder.tvRestaurant.setOnClickListener(v -> {
            Intent intent = new Intent(context, RestaurantDetailsActivity.class);
            intent.putExtra("restaurantId", reel.getRestaurantId());
            context.startActivity(intent);
        });


// when liked put reel in favspage
        holder.btnLike.setOnClickListener(v -> {
            if (uid == null) return; // no user

            if (reel.isLiked()) {// if it was liked then white and remove from fav
                holder.btnLike.setColorFilter(Color.WHITE);
                reel.setLikesCount(reel.getLikesCount() - 1);
                reel.setLiked(false);
                db.collection("users").document(uid)
                        .collection("favorites").document(reel.getReelId()).delete();
            } else {
                holder.btnLike.setColorFilter(Color.RED);
                reel.setLikesCount(reel.getLikesCount() + 1);
                reel.setLiked(true);
// add the new liked reel to fav collection using has as firestore
                Map<String, Object> fav = new HashMap<>();
                fav.put("videoUrl", reel.getVideoUrl());
                fav.put("title", reel.getTitle());
                fav.put("restaurant", reel.getRestaurant());
                fav.put("price", reel.getPrice());
                fav.put("reelId", reel.getReelId());

                db.collection("users").document(uid)
                        .collection("favorites").document(reel.getReelId()).set(fav);
            }
            holder.tvLikeCount.setText(String.valueOf(reel.getLikesCount()));
        });

        holder.btnComment.setOnClickListener(v -> {// when comm press the dialog opens and the data is retrived from firestore
            CommentsDialog dialog = new CommentsDialog(
                    context,
                    (List<Object>) (List<?>) reel.getComments(),
                    reel,
                    (ReelsActivity) context
            );

            dialog.show();
        });
// share content with other apps
        holder.btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check this out: " + reel.getVideoUrl());
            context.startActivity(Intent.createChooser(shareIntent, "Share Reel via"));
        });
// when press order now we go to resturant menu and bring info
        holder.btnOrder.setOnClickListener(v -> {
            db.collection("restaurants")
                    .document(reel.getRestaurantId())
                    .collection("menu")
                    .whereEqualTo("name", reel.getTitle())
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {// bring meal details
                            DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                            String name = doc.getString("name");
                            Double smallPrice = doc.getDouble("smallPrice");
                            Double largePrice = doc.getDouble("largePrice");
                            String imageUrl = doc.getString("imageUrl");
                            String restaurantId = doc.getString("restaurantId");
                            if (smallPrice == null) smallPrice = 0.0;
                            if (largePrice == null) largePrice = smallPrice;

                            String finalName = name;// meal name
                            String finalImageUrl = imageUrl;
                            Double finalSmallPrice = smallPrice;
                            Double finalLargePrice = largePrice;
                            // open dialog to choose size
                            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_choose_size, null);
                            AlertDialog dialog = new AlertDialog.Builder(context)
                                    .setView(dialogView)
                                    .create();

                            Button btnSmall = dialogView.findViewById(R.id.btnSmall);
                            Button btnLarge = dialogView.findViewById(R.id.btnLarge);
                            TextView title = dialogView.findViewById(R.id.tvDialogTitle);

                            title.setText("Choose Size"+" of "+finalName);

                            btnSmall.setText("Small - $" + finalSmallPrice);
                            btnLarge.setText("Large - $" + finalLargePrice);

                            btnSmall.setOnClickListener(v1 -> {// add item to cart
                                CartManager.addItem(
                                        finalName, reel.getRestaurant(), "Small", finalSmallPrice, finalImageUrl, reel.getRestaurantId() );
                                Toast.makeText(context, finalName + " (Small) added to cart!", Toast.LENGTH_SHORT).show();
                                if (context instanceof ReelsActivity) {
                                    ((ReelsActivity) context).updateCartBadge();
                                }
                                dialog.dismiss();
                            });

                            btnLarge.setOnClickListener(v2 -> {
                                CartManager.addItem(finalName, reel.getRestaurant(), "Large", finalLargePrice, finalImageUrl, reel.getRestaurantId());
                                Toast.makeText(context, finalName + " (Large) added to cart!", Toast.LENGTH_SHORT).show();
                                if (context instanceof ReelsActivity) {
                                    ((ReelsActivity) context).updateCartBadge();
                                }
                                dialog.dismiss();
                            });

                            dialog.show();
                        } else {
                            Toast.makeText(context, "Item not found in menu!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {/*this function is called by recycle view to
     know how many elements of present */
        return reelList.size();
    }

    @Override
    // this func is called we user scroll to reuse the view instead of creating new one
    public void onViewRecycled(@NonNull ReelViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.playerView.getPlayer() != null) {/*when scrolled if there is st playing
        release it and set it to null*/
            holder.playerView.getPlayer().release();
            holder.playerView.setPlayer(null);
        }
    }


}