package com.example.healthydiet.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthydiet.R;
import com.example.healthydiet.entity.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_USER = 1;
    private static final int TYPE_AI = 2;
    private List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? TYPE_USER : TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_USER) {
            View view = inflater.inflate(R.layout.item_chat_user, parent, false);
            return new UserMessageHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_chat_ai, parent, false);
            return new AiMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        Log.d("ChatDebug", "绑定位置: " + position +
                " 类型: " + (message.isUser() ? "用户" : "AI"));
        if (holder.getItemViewType() == TYPE_USER) {
            ((UserMessageHolder) holder).bind(message);
        } else {
            ((AiMessageHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserMessageHolder extends RecyclerView.ViewHolder {
        TextView textView;
        UserMessageHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.userMessage);
        }
        void bind(ChatMessage message) {
            textView.setText(message.getContent());
        }
    }

    static class AiMessageHolder extends RecyclerView.ViewHolder {
        TextView textView;
        AiMessageHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.aiMessage);
        }
        void bind(ChatMessage message) {
            textView.setText(message.getContent());
            Log.d("ChatDebug", "绑定AI消息: " + message.getContent());
            // 添加打字机效果（可选）
         //   textView.setAlpha(0f);
         //   textView.animate().alpha(1f).setDuration(200).start();
        }
    }
}