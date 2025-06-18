package com.example.skill_swap.comments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skill_swap.R;
import com.example.skill_swap.model.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/** Displays comments for a single post + lets the user reply inline. */
public class CommentsFragment
        extends Fragment
        implements CommentAdapter.OnReplySendListener {

    /* ------------ arg key ------------ */
    private static final String ARG_POST_ID = "postId";

    /* ------------ views ------------ */
    private RecyclerView recyclerView;
    private EditText     editTextComment;
    private ImageButton  buttonSend;

    /* ------------ data ------------ */
    private final List<Comment> commentList = new ArrayList<>();
    private CommentAdapter      commentAdapter;
    private String              postId;

    /* ------------ Firebase ------------ */
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration    commentsListener;

    /* ==================================================================== */
    public static CommentsFragment newInstance(String postId) {
        Bundle b = new Bundle();
        b.putString(ARG_POST_ID, postId);
        CommentsFragment f = new CommentsFragment();
        f.setArguments(b);
        return f;
    }

    /* ==================================================================== */
    @Nullable
    @Override public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable android.os.Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_comments, container, false);

        /* ---- view refs ---- */
        recyclerView    = v.findViewById(R.id.recyclerViewComments);
        editTextComment = v.findViewById(R.id.editTextComment);
        buttonSend      = v.findViewById(R.id.buttonSendComment);

        /* ---- get postId ---- */
        if (getArguments() == null
                || (postId = getArguments().getString(ARG_POST_ID)) == null) {
            Toast.makeText(getContext(),
                    "No Post ID provided", Toast.LENGTH_SHORT).show();
            return v;
        }

        /* ---- adapter ---- */
        commentAdapter = new CommentAdapter(postId, commentList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(commentAdapter);

        listenForComments();          // realtime updates
        buttonSend.setOnClickListener(x -> sendTopLevelComment());

        return v;
    }

    /* ==================================================================== */
    private void listenForComments() {
        CollectionReference ref = db.collection("posts")
                .document(postId)
                .collection("comments");

        commentsListener = ref
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Toast.makeText(getContext(),
                                "Failed to load comments.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snap == null) return;

                    for (DocumentChange dc : snap.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            Comment c = dc.getDocument().toObject(Comment.class);
                            commentList.add(c);
                            commentAdapter.notifyItemInserted(commentList.size() - 1);
                            recyclerView.scrollToPosition(commentList.size() - 1);
                        }
                    }
                });
    }

    /* ---------- add new top–level comment ---------- */
    private void sendTopLevelComment() {
        String txt = editTextComment.getText().toString().trim();
        if (TextUtils.isEmpty(txt)) {
            Toast.makeText(getContext(),
                    "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid  = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        if (name == null) name = "Anonymous";

        Comment c = new Comment(uid, name, txt);

        db.collection("posts")
                .document(postId)
                .collection("comments")
                .add(c)
                .addOnSuccessListener(x -> editTextComment.setText(""))
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed to post comment.", Toast.LENGTH_SHORT).show());
    }

    /* ---------- callback from adapter when user hits “Send reply” ---------- */
    @Override
    public void onReplySend(String parentCommentId, String replyText) {

        String uid  = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        if (name == null) name = "Anonymous";

        Comment reply = new Comment(uid, name, replyText);

        db.collection("posts")
                .document(postId)
                .collection("comments")
                .document(parentCommentId)
                .collection("replies")
                .add(reply)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed to send reply.", Toast.LENGTH_SHORT).show());
    }

    /* ---------- clean-up ---------- */
    @Override public void onDestroyView() {
        super.onDestroyView();
        if (commentsListener != null) commentsListener.remove();
    }
}