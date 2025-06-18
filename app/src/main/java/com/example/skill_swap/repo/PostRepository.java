package com.example.skill_swap.repo;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.skill_swap.model.Comment;
import com.example.skill_swap.model.Post;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class PostRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth      auth = FirebaseAuth.getInstance();
    private final CollectionReference postsRef = db.collection("posts");

    // ---------------- create ----------------
    public Task<DocumentReference> createPost(Post post) {
        return postsRef.add(post);
    }

    // ---------------- feed query ----------------
    public Query feedQuery() {
        String uid = auth.getCurrentUser().getUid();
        // show public OR connections; we'll filter the connections in code later
        return postsRef
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .whereIn("visibility", java.util.Arrays.asList("public", "connections"));
    }

    // ---------------- like toggle ----------------
    public Task<Void> toggleLike(@NonNull String postId, boolean like) {
        String uid = auth.getCurrentUser().getUid();
        FieldValue op = like ? FieldValue.arrayUnion(uid) : FieldValue.arrayRemove(uid);
        return postsRef.document(postId).update("likes", op);
    }

    // ---------------- mark resolved -------------
    public Task<Void> markResolved(@NonNull String postId, boolean value) {
        return postsRef.document(postId).update("resolved", value,
                "resolvedTimestamp", Timestamp.now());
    }

    // ---------------- add comment ---------------
    public Task<DocumentReference> addComment(@NonNull String postId,
                                              Comment comment) {
        return postsRef.document(postId)
                .collection("comments")
                .add(comment);
    }

    // ---------------- stream comments -----------
    public Query commentsQuery(@NonNull String postId) {
        return postsRef.document(postId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING);
    }
}