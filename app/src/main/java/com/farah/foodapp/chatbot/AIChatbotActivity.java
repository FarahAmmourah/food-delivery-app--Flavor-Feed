package com.farah.foodapp.chatbot;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.farah.foodapp.R;
import com.farah.foodapp.CheckoutActivity;
import com.farah.foodapp.cart.CartManager;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class AIChatbotActivity extends AppCompatActivity {
    // Request code for automatic order
    // (can be any uncommon code)
    private static final int REQUEST_AUTO_ORDER = 1001;


    private RecyclerView recyclerViewChat;
    private EditText editTextMessage;
    private ChatAdapter chatAdapter;
    private List<Message> messages;

    private GeminiService geminiService;// Service to communicate with AI
    private RestaurantDataManager dataManager;// Load restaurant info

    private String restaurantContextFinal = "";// Full restaurant info
    private String userContextFinal = "";// Full user info (preferences/orders)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        //Initialize Views
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        ImageButton buttonSend = findViewById(R.id.buttonSend);
        ImageButton buttonBack = findViewById(R.id.buttonBack);
        ImageButton buttonClear = findViewById(R.id.buttonClear);

        // Load previous chat history
        messages = ChatStorage.loadMessages(this);

        chatAdapter = new ChatAdapter(messages);

        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);
        recyclerViewChat.scrollToPosition(messages.size() - 1); // scroll to last message

        // Button click listeners
        buttonSend.setOnClickListener(v -> sendMessage());
        buttonBack.setOnClickListener(v -> finish());
        buttonClear.setOnClickListener(v -> clearChat());

        geminiService = new GeminiService(); //Initialize AI Service
        dataManager = new RestaurantDataManager(); // initialize restaurant data loader

        // Load full conversation context (restaurant + user info)
        loadFullContext();
    }

    private void loadFullContext() {
        startTypingAnimation();// show typing dots

        dataManager.loadRestaurantData(new RestaurantDataManager.DataLoadCallback() {
            @Override
            public void onDataLoaded(String restaurantContext) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                UserDataManager userDataManager = new UserDataManager();

                userDataManager.loadUserDataWithOrders(userId, new UserDataManager.DataLoadCallback() {
                    @Override
                    public void onDataLoaded(String userContext) {
                        runOnUiThread(() -> {
                            stopTypingAnimation();
                            restaurantContextFinal = restaurantContext;
                            userContextFinal = userContext;
                            addBotMessage("Hi! I'm your AI restaurant assistant. What can i do for you today? ");
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            stopTypingAnimation();
                            addBotMessage("Failed to load user info.");
                        });
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    stopTypingAnimation();
                    addBotMessage("Failed to load restaurant info.");
                });
            }
        });
    }

    private void sendMessage() {
        String userMsg = editTextMessage.getText().toString().trim();
        if (userMsg.isEmpty()) return;

        addUserMessage(userMsg);
        editTextMessage.setText("");

        startTypingAnimation();

        String fullPrompt = buildConversationPrompt(userMsg); // build full context prompt

        // Send message to AI service
        geminiService.sendMessage(userMsg, fullPrompt, new GeminiService.ChatCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    stopTypingAnimation();
                    addBotMessage(response);// show AI response
                    checkForOrderCommand(response);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    stopTypingAnimation();
                    addBotMessage("Error: " + error);
                });
            }
        });
    }

    //Build prompt for AI
    private String buildConversationPrompt(String latestUserMessage) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(restaurantContextFinal).append("\n");
        prompt.append(userContextFinal).append("\n");
        prompt.append("Conversation:\n");
        for (Message msg : messages) {
            prompt.append(msg.isUser() ? "User: " : "Bot: ").append(msg.getText()).append("\n");
        }
        prompt.append("User: ").append(latestUserMessage).append("\n");
        prompt.append("Respond in Jordanian Arabic, friendly and concise, using casual spoken expressions.");
        return prompt.toString();
    }

    private void addUserMessage(String text) {
        messages.add(new Message(text, true));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        recyclerViewChat.scrollToPosition(messages.size() - 1);

        ChatStorage.saveMessages(this, messages); // save chat locally
    }

    private void addBotMessage(String text) {
        messages.add(new Message(text, false));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        recyclerViewChat.scrollToPosition(messages.size() - 1);

        ChatStorage.saveMessages(this, messages);
    }

    // Check if AI response contains "place order" commands
    private void checkForOrderCommand(String aiResponse) {
        String msg = aiResponse.toLowerCase();

        if (
                msg.contains("place my order") ||
                        msg.contains("order now")
        ) {
            runOnUiThread(() -> {
                if (CartManager.getCartItems().isEmpty()) {
                    addBotMessage("Your Cart is Empty!");
                } else {
                    addBotMessage("Processing your order...");
                    placeOrderWithChat();
                }
            });
        }
    }

    // Open CheckoutActivity to place order automatically
    private void placeOrderWithChat() {
        if (CartManager.getCartItems().isEmpty()) {
            addBotMessage("Your Cart is Empty!");
            return;
        }

        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra("autoPlaceOrder", true);
        startActivityForResult(intent, REQUEST_AUTO_ORDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_AUTO_ORDER && resultCode == RESULT_OK && data != null) {
            boolean placed = data.getBooleanExtra("orderPlaced", false);
            if (placed) {
                addBotMessage("Your order has been placed successfully!");
            } else {
                addBotMessage("Failed to place your order.");
            }
        }
    }

    private void startTypingAnimation() {
        View container = findViewById(R.id.typingDotsContainer);
        container.setVisibility(View.VISIBLE);

        int[] dotIds = {R.id.dot1, R.id.dot2, R.id.dot3};
        for (int i = 0; i < dotIds.length; i++) {
            TextView dot = findViewById(dotIds[i]);
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.dance);
            anim.setStartOffset(i * 200);
            dot.startAnimation(anim);
        }
    }
    private void stopTypingAnimation() {
        View container = findViewById(R.id.typingDotsContainer);
        container.setVisibility(View.GONE);

        int[] dotIds = {R.id.dot1, R.id.dot2, R.id.dot3};
        for (int id : dotIds) {
            TextView dot = findViewById(id);
            dot.clearAnimation();
        }
    }
    private void clearChat() {
        ChatStorage.clearHistory(this);
        messages.clear();
        chatAdapter.notifyDataSetChanged();
        addBotMessage("Chat history cleared.");
    }
}
