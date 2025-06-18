package com.example.skill_swap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.InboxVH> {

    private final List<InboxMessage> messageList;
    private final FirebaseFirestore db=FirebaseFirestore.getInstance();

    public InboxAdapter(List<InboxMessage> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public InboxVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inbox, parent, false);
        return new InboxVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InboxVH holder, int position) {
        InboxMessage message = messageList.get(position);

        String senderId = message.getFromUserId();

        if (senderId != null) {
            db.collection("users").document(senderId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            holder.tvSender.setText("From: " + (username != null ? username : "Skill Swap User"));
                        } else {
                            holder.tvSender.setText("From: Skill Swap User");
                        }
                    })
                    .addOnFailureListener(e -> holder.tvSender.setText("From: Skill Swap User"));
        } else {
            holder.tvSender.setText("From: Skill Swap User");
        }

        holder.tvSender.setText("From: " + (message.getFromUserId() != null ? message.getFromUserId() : "Skill Swap User"));
        holder.tvShortText.setText(message.getText());
        holder.tvFullText.setText(message.getText());

        // Format timestamp
        // Format timestamp safely
        Date date = message.getTimestampAsDate();
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
            holder.tvTimestamp.setText(sdf.format(date));
        } else {
            holder.tvTimestamp.setText("-");
        }




        // Expand/collapse toggle
        holder.tvFullText.setVisibility(View.GONE);
        holder.tvToggle.setText("Read more");

        holder.tvToggle.setOnClickListener(v -> {
            if (holder.tvFullText.getVisibility() == View.GONE) {
                holder.tvFullText.setVisibility(View.VISIBLE);
                holder.tvShortText.setVisibility(View.GONE);
                holder.tvToggle.setText("Read less");
            } else {
                holder.tvFullText.setVisibility(View.GONE);
                holder.tvShortText.setVisibility(View.VISIBLE);
                holder.tvToggle.setText("Read more");
            }
        });

        // Optional: button hidden
        holder.btnMessageNow.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class InboxVH extends RecyclerView.ViewHolder {
        TextView tvSender, tvTimestamp, tvShortText, tvFullText, tvToggle;
        ImageView imgAvatar;
        Button btnMessageNow;

        public InboxVH(@NonNull View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvShortText = itemView.findViewById(R.id.tvShortText);
            tvFullText = itemView.findViewById(R.id.tvFullText);
            tvToggle = itemView.findViewById(R.id.tvToggle);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            btnMessageNow = itemView.findViewById(R.id.btnMessageNow);
        }
    }
}
