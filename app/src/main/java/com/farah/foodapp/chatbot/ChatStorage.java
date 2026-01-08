package com.farah.foodapp.chatbot;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ChatStorage {

    private static final String PREF_NAME = "chat_history";
    private static final String KEY_MESSAGES = "messages";

    public static void saveMessages(Context context, List<Message> messages) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Convert messages list to JSON string
        Gson gson = new Gson();
        String json = gson.toJson(messages);

        // Save JSON string in SharedPreferences
        editor.putString(KEY_MESSAGES, json);
        editor.apply();
    }

    // Load the list of messages from SharedPreferences
    public static List<Message> loadMessages(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String json = prefs.getString(KEY_MESSAGES, null);

        // If no messages saved yet, return empty list
        if (json == null) return new ArrayList<>();

        // Convert JSON string back to List<Message>
        Gson gson = new Gson();
        Type type = new TypeToken<List<Message>>() {}.getType();
        return gson.fromJson(json, type);
    }


    // Clear all saved chat history
    public static void clearHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_MESSAGES).apply();
    }
}
