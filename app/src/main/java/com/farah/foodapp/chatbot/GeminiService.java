package com.farah.foodapp.chatbot;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GeminiService {
    // API key for Google Gemini AI
    private static final String API_KEY = "AIzaSyBHp5funWWqbJct3XoPY7UcDPMsgHGhUeQ";

    // This variable represents the Gemini AI model and works asynchronously (Async)
    // This means when we send a message to the AI, the app doesn’t freeze
    // and we can get the response whenever it’s ready
    private final GenerativeModelFutures model;

    // This Executor handles running the AI responses on a background thread
    // so the main UI thread stays smooth and responsive
    private final Executor executor;


    // Callback interface to return the AI response or error
    public interface ChatCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public GeminiService() {
        // Create a GenerativeModel instance for "gemini-2.5-flash"
        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", API_KEY);

        // Wrap the model
        model = GenerativeModelFutures.from(gm);

        // Create a single-thread executor for async callback handling
        //this executor runs the response callbacks in the background safely.
        executor = Executors.newSingleThreadExecutor();
    }

    public void sendMessage(String userMessage, String context, ChatCallback callback) {
        // Build the full prompt for the AI
        String fullPrompt = "You are a helpful restaurant assistant.\n\n" +
                context + "\n\nUser: " + userMessage +
                "\nProvide a helpful, friendly, concise answer.";

        // Wrap the prompt into a Content object
        Content content = new Content.Builder()
                .addText(fullPrompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    callback.onSuccess(result.getText());
                } catch (Exception e) {
                    callback.onError("Exception: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onError("Failure: " + t.getMessage());
            }
        }, executor);

    }
}
