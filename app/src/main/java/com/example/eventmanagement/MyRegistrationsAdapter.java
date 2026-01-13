package com.example.eventmanagement;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyRegistrationsAdapter extends RecyclerView.Adapter<MyRegistrationsAdapter.VH> {

    public interface ActionsListener {
        void onOpen(MyRegistrationItem item);
        void onPay(MyRegistrationItem item);
        void onCancel(MyRegistrationItem item);
    }

    private final List<MyRegistrationItem> list;
    private final ActionsListener listener;

    public MyRegistrationsAdapter(List<MyRegistrationItem> list, ActionsListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_registration, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MyRegistrationItem item = list.get(position);

        h.tvEventName.setText(item.getEventName());
        h.tvDate.setText(safe(item.getEventDate()));
        h.tvStatus.setText(safe(item.getStatus() == null ? "Scheduled" : item.getStatus()));
        h.tvPaid.setText(item.isPaid() ? "Paid" : "Unpaid");
        h.tvPaid.setBackgroundResource(item.isPaid() ? R.drawable.bg_chip_green : R.drawable.bg_chip_red);
        h.tvPaid.setTextColor(Color.WHITE);

        // Buttons styling
        styleButton(h.btnOpen, Color.parseColor("#1976D2"));   // blue
        styleButton(h.btnPay, Color.parseColor("#2E7D32"));    // green
        styleButton(h.btnCancel, Color.parseColor("#C62828")); // red

        // Add simple icons for better affordance
        h.btnOpen.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_view, 0, 0, 0);
        h.btnPay.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_send, 0, 0, 0);
        h.btnCancel.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
        h.btnOpen.setCompoundDrawablePadding(8);
        h.btnPay.setCompoundDrawablePadding(8);
        h.btnCancel.setCompoundDrawablePadding(8);

        h.btnOpen.setOnClickListener(v -> {
            if (listener != null) listener.onOpen(item);
        });

        h.btnPay.setVisibility(item.isPaid() ? View.GONE : View.VISIBLE);
        h.btnPay.setOnClickListener(v -> {
            if (listener != null) listener.onPay(item);
        });

        h.btnCancel.setOnClickListener(v -> {
            if (listener != null) listener.onCancel(item);
        });

        // Show registration type badge
        if (item.isGroupLeader()) {
            String name = item.getLeaderName() != null && !item.getLeaderName().isEmpty() ? item.getLeaderName() : "Group Leader";
            h.tvType.setText(name);
            StringBuilder sb = new StringBuilder();
            if (item.getLeaderEmail() != null && !item.getLeaderEmail().isEmpty()) sb.append("Email: ").append(item.getLeaderEmail()).append("\n");
            if (item.getLeaderContact() != null && !item.getLeaderContact().isEmpty()) sb.append("Contact: ").append(item.getLeaderContact()).append("\n");
            if (item.getLeaderDepartment() != null && !item.getLeaderDepartment().isEmpty()) sb.append("Dept: ").append(item.getLeaderDepartment());
            if (item.getLeaderBatch() != null && !item.getLeaderBatch().isEmpty()) sb.append("  ·  Batch: ").append(item.getLeaderBatch());
            if (item.getLeaderUniversity() != null && !item.getLeaderUniversity().isEmpty()) sb.append("\nUniv: ").append(item.getLeaderUniversity());
            h.tvLeaderInfo.setVisibility(sb.length() > 0 ? View.VISIBLE : View.GONE);
            h.tvLeaderInfo.setText(sb.toString());
        } else {
            h.tvType.setText("Individual");
            h.tvLeaderInfo.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvEventName, tvDate, tvStatus, tvPaid, tvType, tvLeaderInfo;
        TextView btnOpen, btnPay, btnCancel;
        VH(View v) {
            super(v);
            tvEventName = v.findViewById(R.id.tvEventName);
            tvDate = v.findViewById(R.id.tvDate);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvPaid = v.findViewById(R.id.tvPaid);
            tvType = v.findViewById(R.id.tvType);
            tvLeaderInfo = v.findViewById(R.id.tvLeaderInfo);
            btnOpen = v.findViewById(R.id.btnOpen);
            btnPay = v.findViewById(R.id.btnPay);
            btnCancel = v.findViewById(R.id.btnCancel);
        }
    }

    private static void styleButton(TextView tv, int color) {
        if (tv.getBackground() instanceof GradientDrawable) {
            ((GradientDrawable) tv.getBackground()).setColor(color);
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
