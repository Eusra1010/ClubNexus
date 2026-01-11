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

public class RegistrationAdapter
        extends RecyclerView.Adapter<RegistrationAdapter.ViewHolder> {

    private final List<Registration> registrationList;

    private final int[] avatarColors = {
            Color.parseColor("#1E88E5"),
            Color.parseColor("#43A047"),
            Color.parseColor("#FB8C00"),
            Color.parseColor("#8E24AA"),
            Color.parseColor("#E53935"),
            Color.parseColor("#00897B")
    };

    public RegistrationAdapter(List<Registration> registrationList) {
        this.registrationList = registrationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_registration, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        Registration r = registrationList.get(position);

        holder.tvName.setText(r.getFullName());
        holder.tvEmail.setText(r.getEmail());
        holder.tvContact.setText("Contact: " + r.getContactNo());

        String name = r.getFullName();
        String initial = "?";

        if (name != null && !name.isEmpty()) {
            initial = name.substring(0, 1).toUpperCase();
        }

        holder.tvAvatar.setText(initial);

        GradientDrawable bg = (GradientDrawable) holder.tvAvatar.getBackground();
        bg.setColor(avatarColors[position % avatarColors.length]);
    }

    @Override
    public int getItemCount() {
        return registrationList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvAvatar, tvName, tvEmail, tvContact;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvStudentName);
            tvEmail = itemView.findViewById(R.id.tvStudentEmail);
            tvContact = itemView.findViewById(R.id.tvStudentContact);
        }
    }
}
