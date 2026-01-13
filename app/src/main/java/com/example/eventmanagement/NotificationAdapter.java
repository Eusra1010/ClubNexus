package com.example.eventmanagement;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter
        extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<Notification> list;
    public interface NotificationActionListener { void onMarkRead(Notification n); }
    private final NotificationActionListener listener;

    public NotificationAdapter(List<Notification> list, NotificationActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder h, int pos) {

        Notification n = list.get(pos);

        h.tvTitle.setText(
                n.getTitle() != null ? n.getTitle() : "Notification"
        );

        h.tvMessage.setText(
                n.getMessage() != null ? n.getMessage() : ""
        );

        String time = new SimpleDateFormat(
                "dd MMM, hh:mm a",
                Locale.getDefault()
        ).format(new Date(n.getTimestamp()));

        h.tvTime.setText(time);

        String emoji = "🔔";
        int color = Color.parseColor("#673AB7");

        String type = n.getType() != null ? n.getType() : "";

        switch (type) {
            case "REGISTRATION":
                emoji = "✅";
                color = Color.parseColor("#4CAF50");
                break;
            case "PAYMENT":
            case "PAYMENT_SENT":
                emoji = "💳";
                color = Color.parseColor("#1E88E5");
                break;
            case "REMINDER":
            case "EVENT_REMINDER":
                emoji = "⏰";
                color = Color.parseColor("#FB8C00");
                break;
            case "CANCELLED":
            case "EVENT_CANCELLED":
                emoji = "❌";
                color = Color.parseColor("#E53935");
                break;
            case "ANNOUNCEMENT":
            case "EVENT_UPDATE":
                emoji = "🔔";
                color = Color.parseColor("#673AB7");
                break;
        }

        h.tvIcon.setText(emoji);

        if (h.tvIcon.getBackground() instanceof GradientDrawable) {
            ((GradientDrawable) h.tvIcon.getBackground()).setColor(color);
        }

        h.tvTitle.setAlpha(n.isRead() ? 0.7f : 1f);
        h.btnMarkRead.setVisibility(n.isRead() ? View.GONE : View.VISIBLE);
        h.btnMarkRead.setOnClickListener(v -> {
            if (listener != null) listener.onMarkRead(n);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvTitle, tvMessage, tvTime, btnMarkRead;

        ViewHolder(View v) {
            super(v);
            tvIcon = v.findViewById(R.id.tvIcon);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvTime = v.findViewById(R.id.tvTime);
            btnMarkRead = v.findViewById(R.id.btnMarkRead);
        }
    }
}
