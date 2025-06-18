package com.example.skill_swap.comments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skill_swap.R;
import com.example.skill_swap.model.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shows comments + inline reply UI, and streams each comment’s
 * replies into an inner RecyclerView in real-time.
 */
public class CommentAdapter
        extends RecyclerView.Adapter<CommentAdapter.CommentVH> {

    /* --- callback sent back to the host (Activity/Fragment) ------------- */
    public interface OnReplySendListener {
        void onReplySend(String parentCommentId, String replyText);
    }

    /* -------------------------------------------------------------------- */
    private final String               postId;          // <-- NEW
    private final List<Comment>        comments;
    private final OnReplySendListener  replyListener;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    /** keeps active listeners so we can stop them when the view is recycled */
    private final Map<String, ListenerRegistration> replyStreams =
            new ConcurrentHashMap<>();

    private final SimpleDateFormat sdf =
            new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());

    public CommentAdapter(String postId,
                          List<Comment> comments,
                          OnReplySendListener listener) {
        this.postId        = postId;
        this.comments      = comments;
        this.replyListener = listener;
    }

    /* -------------------------------------------------------------------- */
    @NonNull @Override
    public CommentVH onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_item, parent, false);
        return new CommentVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentVH h, int pos) {

        Comment c = comments.get(pos);
        h.bindMain(c);

        /* ---------- nested replies list ---------- */
        // Stop an old stream (if this recycled view was used before)
        if (h.activeListener != null) h.activeListener.remove();

        List<Comment> replies = new ArrayList<>();
        ReplyAdapter replyAdapter = new ReplyAdapter(replies);
        h.repliesRv.setLayoutManager(
                new LinearLayoutManager(h.itemView.getContext()));
        h.repliesRv.setAdapter(replyAdapter);

        ListenerRegistration lr = db.collection("posts")
                .document(postId)
                .collection("comments")
                .document(c.getCommentId())
                .collection("replies")
                .orderBy("timestamp")
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;
                    for (DocumentChange dc : snap.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            Comment reply = dc.getDocument().toObject(Comment.class);
                            replies.add(reply);
                        }
                    }
                    replyAdapter.notifyDataSetChanged();
                });

        h.activeListener = lr;                 // remember so we can remove later
        replyStreams.put(c.getCommentId(), lr);

        /* ---------- reply UI ---------- */
        h.replyBtn.setOnClickListener(v -> {
            if (h.replyBox.getVisibility() == View.GONE) {
                h.replyBox.setVisibility(View.VISIBLE);
                h.replyEt.requestFocus();
            } else {
                h.replyBox.setVisibility(View.GONE);
            }
        });

        h.sendReplyBtn.setOnClickListener(v -> {
            String txt = h.replyEt.getText().toString().trim();
            if (txt.isEmpty()) {
                Toast.makeText(v.getContext(),
                        "Reply cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            replyListener.onReplySend(c.getCommentId(), txt);
            h.replyEt.setText("");
            h.replyBox.setVisibility(View.GONE);
        });
    }

    @Override public int getItemCount() { return comments.size(); }

    /** Remove listeners when a view is recycled to avoid leaks */
    @Override public void onViewRecycled(@NonNull CommentVH holder) {
        super.onViewRecycled(holder);
        if (holder.activeListener != null) holder.activeListener.remove();
    }

    /* =====================  View-holder  ====================== */
    static class CommentVH extends RecyclerView.ViewHolder {

        /* main comment */
        TextView nameTv, textTv, timeTv;
        Button   replyBtn;
        /* reply input */
        LinearLayout replyBox;
        EditText     replyEt;
        ImageButton  sendReplyBtn;
        /* inner list */
        RecyclerView repliesRv;
        /* firestore listener handle */
        ListenerRegistration activeListener;

        CommentVH(@NonNull View v) {
            super(v);
            nameTv       = v.findViewById(R.id.textViewCommenterName);
            textTv       = v.findViewById(R.id.textViewCommentText);
            timeTv       = v.findViewById(R.id.textViewTimestamp);
            replyBtn     = v.findViewById(R.id.buttonReply);
            replyBox     = v.findViewById(R.id.layoutReplyInput);
            replyEt      = v.findViewById(R.id.editTextReply);
            sendReplyBtn = v.findViewById(R.id.buttonSendReply);
            repliesRv    = v.findViewById(R.id.recyclerViewReplies);
        }

        void bindMain(Comment c) {
            textTv.setText(c.getCommentText());
            timeTv.setText(c.getTimestamp() != null
                    ? new SimpleDateFormat("dd MMM HH:mm",
                    Locale.getDefault()).format(c.getTimestamp().toDate())
                    : "");
            nameTv.setText(c.getCommenterName() == null
                    ? "…" : c.getCommenterName());
        }
    }
}
