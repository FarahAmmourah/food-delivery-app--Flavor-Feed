package com.farah.foodapp.chatbot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.farah.foodapp.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messages;

    // Different view types for user, bot, and typing indicator
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;
    private static final int VIEW_TYPE_TYPING = 3;

    public ChatAdapter(List<Message> messages) {
        this.messages = messages;
    }

    //decide which layout to use
    @Override
    public int getItemViewType(int position) {
        Message msg = messages.get(position);
        if (msg.isTyping()) return VIEW_TYPE_TYPING;
        return msg.isUser() ? VIEW_TYPE_USER : VIEW_TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_TYPING) {
            View view = inflater.inflate(R.layout.item_typing, parent, false);
            return new TypingViewHolder(view);
        }

        int layout = (viewType == VIEW_TYPE_USER) ? R.layout.chat_item_user : R.layout.chat_item_bot;
        View view = inflater.inflate(layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);

        if (holder instanceof MessageViewHolder) {
            ((MessageViewHolder) holder).textViewMessage.setText(msg.getText());
        } else if (holder instanceof TypingViewHolder) {
            ((TypingViewHolder) holder).bind(msg);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
        }
    }

    static class TypingViewHolder extends RecyclerView.ViewHolder {

        TypingViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bind(Message msg) {
            View container = itemView.findViewById(R.id.typingDotsContainer);
            container.setVisibility(msg.isTyping() ? View.VISIBLE : View.GONE);
        }
    }
}
