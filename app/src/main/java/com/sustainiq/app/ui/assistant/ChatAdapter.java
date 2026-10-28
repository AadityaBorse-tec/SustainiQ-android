package com.sustainiq.app.ui.assistant;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sustainiq.app.R;
import com.sustainiq.app.data.db.ChatMessageEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final List<ChatMessageEntity> messages = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    public void setMessages(List<ChatMessageEntity> newMessages) {
        messages.clear();
        if (newMessages != null) {
            messages.addAll(newMessages);
        }
        notifyDataSetChanged();
    }

    public void addMessage(ChatMessageEntity msg) {
        messages.add(msg);
        notifyItemInserted(messages.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessageEntity msg = messages.get(position);
        holder.bind(msg, timeFormat);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout layoutRoot;
        private final LinearLayout layoutBubble;
        private final TextView tvSender;
        private final TextView tvText;
        private final TextView tvTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutRoot   = itemView.findViewById(R.id.layout_message_root);
            layoutBubble = itemView.findViewById(R.id.layout_bubble);
            tvSender     = itemView.findViewById(R.id.tv_sender_name);
            tvText       = itemView.findViewById(R.id.tv_message_text);
            tvTime       = itemView.findViewById(R.id.tv_message_time);
        }

        void bind(ChatMessageEntity msg, SimpleDateFormat timeFormat) {
            tvText.setText(msg.text);
            tvTime.setText(timeFormat.format(new Date(msg.timestamp)));

            if (msg.isBot) {
                layoutRoot.setGravity(Gravity.START);
                layoutBubble.setBackgroundResource(R.drawable.bg_badge_in_progress);
                tvSender.setText("SUSTAINIQ AI");
                tvSender.setTextColor(Color.parseColor("#2E7D32"));
                tvText.setTextColor(Color.parseColor("#1C2B1F"));
            } else {
                layoutRoot.setGravity(Gravity.END);
                layoutBubble.setBackgroundResource(R.drawable.bg_badge_assigned);
                tvSender.setText("You");
                tvSender.setTextColor(Color.parseColor("#5E35B1"));
                tvText.setTextColor(Color.parseColor("#1C2B1F"));
            }
        }
    }
}
