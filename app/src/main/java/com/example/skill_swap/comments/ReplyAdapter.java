package com.example.skill_swap.comments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skill_swap.R;
import com.example.skill_swap.model.Comment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> {

    private final List<Comment> replyList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ReplyAdapter(List<Comment> replies) {
        this.replyList = replies;
    }

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reply_item, parent, false);
        return new ReplyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyViewHolder holder, int pos) {
        Comment reply = replyList.get(pos);
        holder.replyText.setText(reply.getCommentText());

        holder.replyName.setText("…");
        db.collection("users").document(reply.getCommenterId()).get()
                .addOnSuccessListener(doc -> {
                    String name = doc.exists() ? doc.getString("name") : "Unknown";
                    holder.replyName.setText(name);
                })
                .addOnFailureListener(e -> holder.replyName.setText("Error"));
    }

    @Override
    public int getItemCount() {
        return replyList.size();
    }

    static class ReplyViewHolder extends RecyclerView.ViewHolder {
        TextView replyName, replyText;

        ReplyViewHolder(@NonNull View v) {
            super(v);
            replyName = v.findViewById(R.id.textViewReplyName);
            replyText = v.findViewById(R.id.textViewReplyText);
        }
    }
}