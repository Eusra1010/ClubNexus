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
    extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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

    private static final int TYPE_INDIVIDUAL = 0;
    private static final int TYPE_GROUP = 1;

    @Override
    public int getItemViewType(int position) {
        Registration r = registrationList.get(position);
        return (r.getGroupName() != null) ? TYPE_GROUP : TYPE_INDIVIDUAL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        if (viewType == TYPE_GROUP) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_registration_group, parent, false);
            return new GroupViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_registration, parent, false);
            return new IndividualViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder, int position) {

        Registration r = registrationList.get(position);

        if (holder instanceof IndividualViewHolder) {
            IndividualViewHolder h = (IndividualViewHolder) holder;
            h.tvName.setText(r.getFullName());
            h.tvEmail.setText(r.getEmail());
            h.tvContact.setText("Contact: " + r.getContactNo());

            String name = r.getFullName();
            String initial = "?";
            if (name != null && !name.isEmpty()) initial = name.substring(0, 1).toUpperCase();
            h.tvAvatar.setText(initial);

            GradientDrawable bg = (GradientDrawable) h.tvAvatar.getBackground();
            bg.setColor(avatarColors[position % avatarColors.length]);

        } else if (holder instanceof GroupViewHolder) {
            GroupViewHolder h = (GroupViewHolder) holder;
            h.tvGroupName.setText(r.getGroupName());
            h.membersContainer.removeAllViews();
            List<Registration> members = r.getMembers();
            if (members != null) {
                LayoutInflater inflater = LayoutInflater.from(h.itemView.getContext());
                for (Registration m : members) {
                    View row = inflater.inflate(R.layout.item_registration_group_member, h.membersContainer, false);
                    TextView nm = row.findViewById(R.id.tvMemberName);
                    TextView em = row.findViewById(R.id.tvMemberEmail);
                    TextView ct = row.findViewById(R.id.tvMemberContact);
                    nm.setText(m.getFullName());
                    em.setText(m.getEmail());
                    ct.setText(m.getContactNo());
                    h.membersContainer.addView(row);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return registrationList.size();
    }

    static class IndividualViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvEmail, tvContact;
        IndividualViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvStudentName);
            tvEmail = itemView.findViewById(R.id.tvStudentEmail);
            tvContact = itemView.findViewById(R.id.tvStudentContact);
        }
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName;
        ViewGroup membersContainer;
        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            membersContainer = itemView.findViewById(R.id.membersContainer);
        }
    }
}
