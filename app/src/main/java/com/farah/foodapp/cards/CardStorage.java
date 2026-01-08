package com.farah.foodapp.cards;

import android.content.Context;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CardStorage {

    private static final String USERS_COLLECTION = "cards";

    public interface CardSaveCallback {
        void onComplete(boolean success);
    }

    // save user cards in firebase
    public static void saveCard(Context context, String last4, String expiry, String holderName, CardSaveCallback callback) {
        if (context == null) {
            if (callback != null) callback.onComplete(false);
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show();
            if (callback != null) callback.onComplete(false);
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CardModel card = new CardModel(null, last4, expiry, holderName);
        CollectionReference userCards = db.collection(USERS_COLLECTION)
                .document(userId)
                .collection("userCards");

        userCards
                .add(card)
                .addOnSuccessListener(docRef -> {
                    if (callback != null) callback.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onComplete(false);
                });
    }

    public interface CardListCallback {
        void onCardsLoaded(List<CardModel> cards);
    }


    public static ListenerRegistration getCards(Context context, CardListCallback callback) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            if (callback != null) callback.onCardsLoaded(new ArrayList<>());
            return null;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        return db.collection(USERS_COLLECTION)
                .document(userId)
                .collection("userCards")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null || querySnapshot == null) {
                        if (callback != null) callback.onCardsLoaded(new ArrayList<>());
                        return;
                    }

                    List<CardModel> cards = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Map<String, Object> data = doc.getData();
                        if (data != null) {
                            String last4 = data.get("cardNumber") != null ? String.valueOf(data.get("cardNumber")) : "";
                            String expiry = data.get("expiry") != null ? String.valueOf(data.get("expiry")) : "";
                            String holder = data.get("holderName") != null ? String.valueOf(data.get("holderName")) : "";
                            cards.add(new CardModel(doc.getId(), last4, expiry, holder));
                        }
                    }

                    if (callback != null) callback.onCardsLoaded(cards);
                });
    }

    public static void deleteCard(String userId, String cardId, Runnable onComplete) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(USERS_COLLECTION)
                .document(userId)
                .collection("userCards")
                .document(cardId)
                .delete()
                .addOnSuccessListener(v -> { if (onComplete != null) onComplete.run(); })
                .addOnFailureListener(e -> { if (onComplete != null) onComplete.run(); });
    }

    public static class CardModel {
        private String id;
        private String cardNumber;
        private String expiry;
        private String holderName;

        public CardModel() {}

        public CardModel(String id, String cardNumber, String expiry, String holderName) {
            this.id = id;
            this.cardNumber = cardNumber;
            this.expiry = expiry;
            this.holderName = holderName;
        }

        public String getId() { return id; }
        public String getCardNumber() { return cardNumber; }
        public String getExpiry() { return expiry; }
        public String getHolderName() { return holderName; }
    }
}
