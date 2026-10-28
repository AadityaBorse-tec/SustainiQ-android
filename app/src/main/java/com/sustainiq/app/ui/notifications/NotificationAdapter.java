package com.sustainiq.app.ui.notifications;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sustainiq.app.R;
import com.sustainiq.app.data.db.NotificationEntity;
import com.sustainiq.app.ui.reports.ReportDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    private List<NotificationEntity> items = new ArrayList<>();
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onClick(NotificationEntity n);
    }

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<NotificationEntity> list) {
        items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NotificationEntity n = items.get(position);
        h.tvTitle.setText(n.title);
        h.tvMessage.setText(n.message);
        h.tvTime.setText(formatTime(n.createdAt));
        h.dotIndicator.setVisibility(n.isRead ? View.INVISIBLE : View.VISIBLE);
        h.unreadBadge.setVisibility(n.isRead ? View.INVISIBLE : View.VISIBLE);
        h.itemView.setAlpha(n.isRead ? 0.6f : 1.0f);
        h.itemView.setOnClickListener(v -> listener.onClick(n));
    }

    @Override
    public int getItemCount() { return items.size(); }

    private String formatTime(long millis) {
        long diff = System.currentTimeMillis() - millis;
        long minutes = diff / 60000;
        if (minutes < 1)   return "Just now";
        if (minutes < 60)  return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24)    return hours + "h ago";
        long days = hours / 24;
        return days + "d ago";
    }

    static class VH extends RecyclerView.ViewHolder {
        View dotIndicator, unreadBadge;
        TextView tvTitle, tvMessage, tvTime;

        VH(@NonNull View v) {
            super(v);
            dotIndicator = v.findViewById(R.id.view_notif_dot);
            unreadBadge  = v.findViewById(R.id.view_unread_badge);
            tvTitle      = v.findViewById(R.id.tv_notif_title);
            tvMessage    = v.findViewById(R.id.tv_notif_message);
            tvTime       = v.findViewById(R.id.tv_notif_time);
        }
    }
}
