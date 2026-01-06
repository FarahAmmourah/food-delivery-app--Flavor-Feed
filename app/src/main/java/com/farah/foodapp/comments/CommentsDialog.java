package com.farah.foodapp.comments;

import com.farah.foodapp.algorithm.TextVectorizer;
import com.farah.foodapp.algorithm.SentimentInterpreter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.farah.foodapp.R;
import com.farah.foodapp.reel.ReelItem;
import com.farah.foodapp.reel.ReelsActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.internal.LinkedTreeMap;

import java.util.HashMap;
import java.util.List;

public class CommentsDialog extends BottomSheetDialog {

    private List<Object> comments;

    private CommentAdapter adapter;
    private ReelItem reel;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
// contex is the place that the comments dia will appear in
    public CommentsDialog(@NonNull Context context, List<Object> comments, ReelItem reel, ReelsActivity reelsActivity) {
        super(context);
        this.comments = comments;
        this.reel = reel;
    }

    @Override /* inflate / connect ui into java */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
// turn xml in dia into view
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_comments, null);
        setContentView(view);
        // turn the back into transparent
        View bottomSheet = findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            bottomSheet.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }

        RecyclerView recyclerComments = view.findViewById(R.id.recyclerComments);
        EditText etComment = view.findViewById(R.id.etComment);
        Button btnSend = view.findViewById(R.id.btnSend);
        Button btnAnalyze = view.findViewById(R.id.btnAnalyze);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
// create adapter object
        adapter = new CommentAdapter(comments);
        recyclerComments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerComments.setAdapter(adapter);

        btnAnalyze.setOnClickListener(v -> analyzeComments());
        btnSend.setOnClickListener(v -> addNewComment(etComment, recyclerComments));
    }


    private void analyzeComments() {

        if (comments.size() == 0) {
            showSentimentBottomSheet(0, 0, 0);
            return;
        }
//We maintain counters to track the overall sentiment distribution
        int positive = 0;
        int negative = 0;
        int neutral = 0;

        for (Object c : comments) {

            String pureComment = extractPureComment(c);

            try {
                float[] input = TextVectorizer.vectorize(pureComment);// comment will change into vectors of nums

                float[] output = SentimentInterpreter.predict(input, getContext());// call ml model , par2 download the tensor flow model found in assets

                if (output[0] >= output[1] && output[0] >= output[2]) {
                    positive++;
                } else if (output[1] >= output[0] && output[1] >= output[2]) {
                    negative++;
                } else {
                    neutral++;
                }

            } catch (Exception e) {
                e.printStackTrace();
                neutral++;
            }
        }

        showSentimentBottomSheet(positive, negative, neutral);
    }

// this is used to remove the username and take only the comment on tits own
    private String extractPureComment(Object c) {

        if (c instanceof String) {
            String s = (String) c;
            return s.contains(":") ? s.split(":", 2)[1].trim() : s; // will cut the comment
                                                                               // after the first : and takes the second part
        }

        if (c instanceof HashMap) {
            Object val = ((HashMap) c).get("text");       // firebase can give different type of
            return val != null ? val.toString() : "";    //retrivals so i made sure to take only the comment part
        }

        if (c instanceof LinkedTreeMap) {
            Object val = ((LinkedTreeMap) c).get("text");
            return val != null ? val.toString() : "";
        }

        return "";
    }


    private void addNewComment(EditText etComment, RecyclerView recycler) {

        String newComment = etComment.getText().toString().trim();// remove spa from begin and end
        if (newComment.isEmpty()) {
            Toast.makeText(getContext(), "Type a comment first", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();// get current user
        String userName;// empty

        if (user != null) {
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                userName = user.getDisplayName();
            } else if (user.getEmail() != null) {
                userName = user.getEmail();
            } else {
                userName = "Anonymous";
            }
        } else {
            userName = "Anonymous";
        }

        String formattedComment = userName + ": " + newComment;
// only ui not database
        comments.add(formattedComment);
        adapter.notifyItemInserted(comments.size() - 1);// notify adapter to read new comment given position
        recycler.scrollToPosition(comments.size() - 1);// scroll to lastest comm
        etComment.setText("");// comment section rest

        // store new com to db
        if (reel != null && reel.getReelId() != null) {
            db.collection("reels")
                    .document(reel.getReelId())
                    .update("comments", FieldValue.arrayUnion(formattedComment))
                    .addOnSuccessListener(a -> {
                        db.collection("reels")
                                .document(reel.getReelId())
                                .update("commentsCount", comments.size());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to save comment", Toast.LENGTH_SHORT).show());
        }
    }


    private void showSentimentBottomSheet(int posCount, int negCount, int neutralCount) {

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());// get context is used to know where to open the bottom sheet
        View v = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_sentiment_result, null);
        dialog.setContentView(v);

        int total = posCount + negCount + neutralCount;
        if (total == 0) total = 1;// prevent div over 0

        String posP = (int) ((posCount * 100.0 / total)) + "%";
        String negP = (int) ((negCount * 100.0 / total)) + "%";
        String neuP = (int) ((neutralCount * 100.0 / total)) + "%";

        ((android.widget.TextView) v.findViewById(R.id.tvPositive))
                .setText("Positive: " + posCount + " (" + posP + ")");
        ((android.widget.TextView) v.findViewById(R.id.tvNegative))
                .setText("Negative: " + negCount + " (" + negP + ")");
        ((android.widget.TextView) v.findViewById(R.id.tvNeutral))
                .setText("Neutral: " + neutralCount + " (" + neuP + ")");

        Button btnClose = v.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(x -> dialog.dismiss());

        dialog.show();
    }
}
