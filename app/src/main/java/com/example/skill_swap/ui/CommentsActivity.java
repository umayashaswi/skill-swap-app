package com.example.skill_swap.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skill_swap.R;
import com.example.skill_swap.comments.CommentAdapter;
import com.example.skill_swap.model.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {

    /* ---------------- views & helpers ---------------- */
    private RecyclerView           recyclerView;
    private CommentAdapter         commentAdapter;
    private final List<Comment>    commentList = new ArrayList<>();

    private EditText    editTextComment;
    private ImageButton buttonSend;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String postId;

    /* ⚡ cached user name */
    private String currentUserName = "Anonymous";

    /* ================================================= */
    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_comments);

        postId = getIntent().getStringExtra("postId");
        if (postId == null) {
            Toast.makeText(this, "No post ID provided", Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        recyclerView    = findViewById(R.id.recyclerViewComments);
        editTextComment = findViewById(R.id.editTextComment);
        buttonSend      = findViewById(R.id.buttonSendComment);

        commentAdapter = new CommentAdapter(
                postId,
                commentList,
                (parentId, txt) -> sendReplyToFirestore(parentId, txt));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(commentAdapter);

        fetchCurrentUserName();   // <─ fetch once
        loadComments();
        buttonSend.setOnClickListener(v -> postComment());
    }

    /* ---------------- username from Firestore -------- */
    private void fetchCurrentUserName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;   // not expected here

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String n = doc.getString("name");
                    if (n != null && !n.trim().isEmpty()) {
                        currentUserName = n.trim();
                    } else {          // fallback to email prefix
                        String email = user.getEmail();
                        if (email != null && email.contains("@"))
                            currentUserName = email.substring(0, email.indexOf("@"));
                    }
                });
    }

    /* ---------------- load comments stream ----------- */
    private void loadComments() {
        db.collection("posts").document(postId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) {
                        Toast.makeText(this, "Failed to load comments", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (DocumentChange dc : snap.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            Comment c = dc.getDocument().toObject(Comment.class);
                            c.setCommentId(dc.getDocument().getId());
                            commentList.add(c);
                        }
                    }
                    commentAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(commentList.size()-1);
                });
    }

    /* ---------------- add new top-level comment ------- */
    private void postComment() {
        String txt = editTextComment.getText().toString().trim();
        if (TextUtils.isEmpty(txt)) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid  = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Comment comment = new Comment(uid, currentUserName, txt);

        CollectionReference commentsRef = db.collection("posts")
                .document(postId)
                .collection("comments");
        DocumentReference  postRef     = db.collection("posts")
                .document(postId);

        commentsRef.add(comment)
                .addOnSuccessListener(docRef -> {
                    editTextComment.setText("");
                    postRef.update("commentCount", FieldValue.increment(1));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show());
    }

    /* ---------------- add reply to a comment --------- */
    private void sendReplyToFirestore(String parentId, String txt) {
        String uid  = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Comment reply = new Comment(uid, currentUserName, txt);

        db.collection("posts").document(postId)
                .collection("comments").document(parentId)
                .collection("replies")
                .add(reply)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to send reply: "+e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}


