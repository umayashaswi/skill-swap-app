package com.example.skill_swap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class SkillUserAdapter extends RecyclerView.Adapter<SkillUserAdapter.VH> {

    public interface OnCardClick { void onClick(User u); }
    public interface OnSendClick { void onClick(User u); }

    private final List<User> list;
    private final OnCardClick cardCb;
    private final OnSendClick sendCb;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public SkillUserAdapter(List<User> list, OnCardClick cardCb, OnSendClick sendCb) {
        this.list = list;
        this.cardCb = cardCb;
        this.sendCb = sendCb;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvSkill, tvSwaps, tvExperience;
        RatingBar rating;
        Button btnSend;
        ImageView imgProfile;

        VH(@NonNull View v) {
            super(v);
            tvName       = v.findViewById(R.id.tvNameRow);
            tvSkill      = v.findViewById(R.id.tvSkillRow);
            rating       = v.findViewById(R.id.ratingRow);
            btnSend      = v.findViewById(R.id.btnSendRow);
            tvSwaps      = v.findViewById(R.id.tvSwapsRow);
            tvExperience = v.findViewById(R.id.tvExperienceRow);
            imgProfile   = v.findViewById(R.id.imgProfileRow);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_user_skills, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        User u = list.get(pos);

        h.tvName.setText(u.getName());
        h.tvSkill.setText(u.getSkillsOffered());
        h.rating.setRating(u.getRating());
        h.tvSwaps.setText(u.getTotalSwaps() + " swap" + (u.getTotalSwaps() == 1 ? "" : "s") + " completed");

        h.tvExperience.setText(u.getYearsOfExperience() + " yrs experience");

        if (u.getPhotoUrl() != null && !u.getPhotoUrl().isEmpty()) {
            Glide.with(h.imgProfile.getContext())
                    .load(u.getPhotoUrl())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(h.imgProfile);
        } else {
            h.imgProfile.setImageResource(R.drawable.ic_profile_placeholder);
        }

        h.itemView.setOnClickListener(v -> cardCb.onClick(u));
        refreshSendButton(h.btnSend, u, sendCb);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void refreshSendButton(Button btn, User target, OnSendClick cb) {
        String me = auth.getUid();
        String toUid = target.getUid();

        if (me == null || me.equals(toUid)) {
            btn.setEnabled(false);
            btn.setText("SWAP");
            btn.setOnClickListener(null);
            return;
        }

        btn.setEnabled(false);
        btn.setText("Checking…");
        btn.setOnClickListener(null);

        db.collection("requests")
                .whereEqualTo("fromUid", me)
                .whereEqualTo("toUid", toUid)
                .whereIn("status", Arrays.asList("pending", "accepted", "Pending", "Accepted"))
                .limit(1)
                .get()
                .addOnSuccessListener(sentSnap -> {
                    if (!sentSnap.isEmpty()) {
                        String st = sentSnap.getDocuments().get(0).getString("status");
                        btn.setText(st.equalsIgnoreCase("pending") ? "Pending" : "Swapped");
                        return;
                    }

                    db.collection("requests")
                            .whereEqualTo("fromUid", toUid)
                            .whereEqualTo("toUid", me)
                            .whereIn("status", Arrays.asList("accepted", "Accepted"))
                            .limit(1)
                            .get()
                            .addOnSuccessListener(recvSnap -> {
                                if (!recvSnap.isEmpty()) {
                                    btn.setText("Swapped");
                                } else {
                                    btn.setText("SWAP");
                                    btn.setEnabled(true);
                                    btn.setOnClickListener(v -> cb.onClick(target));
                                }
                            })

                            .addOnFailureListener(e -> {
                                btn.setText("SWAP");
                                btn.setEnabled(true);
                                btn.setOnClickListener(v -> cb.onClick(target));
                            });
                })
                .addOnFailureListener(e -> {
                    btn.setText("SWAP");
                    btn.setEnabled(true);
                    btn.setOnClickListener(v -> cb.onClick(target));
                });
    }
}
