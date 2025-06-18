package com.example.skill_swap.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.skill_swap.BaseActivity;
import com.example.skill_swap.R;
import com.example.skill_swap.adapter.PostAdapter;
import com.example.skill_swap.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends BaseActivity implements PostAdapter.OnPostActionListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private final List<Post> postList = new ArrayList<>();
    private PostAdapter adapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration postsListener; // <-- keeps snapshot listener

    /* -------------------------------------------------- */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        recyclerView = findViewById(R.id.rvFeed);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        adapter = new PostAdapter(postList, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.fabNewPost)
                .setOnClickListener(v -> startActivity(
                        new Intent(this, NewPostActivity.class)));

        swipeRefresh.setOnRefreshListener(this::restartListener);

        startListening(); // begin real-time updates
    }

    /* ------------------ Snapshot listener ------------- */
    private void startListening() {
        swipeRefresh.setRefreshing(true);

        postsListener = db.collection("posts")
                .whereEqualTo("visibility", "public")   // <-- only public posts
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Toast.makeText(this,
                                "Failed to load posts: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                        return;
                    }

                    postList.clear();
                    if (snap != null) {
                        for (DocumentSnapshot doc : snap.getDocuments()) {
                            Post p = doc.toObject(Post.class);
                            if (p != null) {
                                p.setPostId(doc.getId());
                                postList.add(p);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                });
    }

    /** Called by SwipeRefreshLayout */
    private void restartListener() {
        if (postsListener != null) postsListener.remove();
        startListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (postsListener != null) postsListener.remove();   // avoid memory leak
    }

    /* ------------------ Like / Resolve handlers -------- */
    @Override
    public void onLikeClick(Post post) {
        var currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = currentUser.getUid();

        List<String> likes = post.getLikes();

        if (likes.contains(uid)) likes.remove(uid);
        else likes.add(uid);

        db.collection("posts").document(post.getPostId())
                .update("likes", likes)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update likes.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onResolveClick(Post post) {
        post.setResolved(!post.isResolved());
        db.collection("posts").document(post.getPostId())
                .update("resolved", post.isResolved())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update status.", Toast.LENGTH_SHORT).show());
    }

    /* ------------- Implement missing onCommentClick -------- */
    @Override
    public void onCommentClick(Post post) {
        // Example: Open a CommentsActivity to view/add comments on this post
        Intent intent = new Intent(this, CommentsActivity.class);
        intent.putExtra("postId", post.getPostId());
        startActivity(intent);
    }
}