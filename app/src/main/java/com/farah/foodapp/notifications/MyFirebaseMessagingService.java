package com.farah.foodapp.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.farah.foodapp.R;
import com.farah.foodapp.orders.OrdersActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    // Channel ID for notifications (used for Android 8.0+)
    private static final String CHANNEL_ID = "default_channel";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = null;
        String body = null;

        //get title and body from notification payload
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        // if payload exist but no title or body
        if (!remoteMessage.getData().isEmpty()) {
            if (title == null) title = remoteMessage.getData().get("title");
            if (body == null) body = remoteMessage.getData().get("message");
        }

        // Default values if title or body are missing
        if (title == null) title = "Notification";
        if (body == null) body = "No message body";

        showNotification(title, body);
    }

    // Called when FCM generates a new token (device registration token)
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        // If user is logged in, save the token in Firestore
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("fcmToken", token);
        }
    }

    private void showNotification(String title, String message) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // For Android 8.0+ we need to create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Default Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, OrdersActivity.class);

        // (FLAG_ACTIVITY_CLEAR_TOP) ->
        // If the activity already exists in the back stack,
        // all activities above it are removed.

        //FLAG_ACTIVITY_SINGLE_TOP
        //If the activity is already on top, don’t create a new instance.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_app)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Show the notification (using current time as unique ID)
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
