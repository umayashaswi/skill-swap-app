package com.example.skill_swap.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skill_swap.comments.CommentsFragment;
import com.example.skill_swap.R;
import com.example.skill_swap.adapter.PostAdapter;
import com.example.skill_swap.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment implements PostAdapter.OnPostActionListener {

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> postList;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private ListenerRegistration postsListener;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPosts);
        progressBar = view.findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();
        postList = new ArrayList<>();
        adapter = new PostAdapter(postList, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fetchPosts();

        return view;
    }

    private void fetchPosts() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Failed to load posts.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (querySnapshot != null) {
                        postList.clear();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                post.setPostId(doc.getId());
                                postList.add(post);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (postsListener != null) {
            postsListener.remove();
        }
    }

    private void onPostsFetched(QuerySnapshot querySnapshot) {
        postList.clear();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            Post post = doc.toObject(Post.class);
            if (post != null) {
                post.setPostId(doc.getId());
                // set postId from Firestore doc id
                postList.add(post);
            }
        }
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLikeClick(Post post) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        List<String> likes = post.getLikes();

        if (likes.contains(currentUserId)) {
            likes.remove(currentUserId);
        } else {
            likes.add(currentUserId);
        }

        // Update post likes in Firestore
        db.collection("posts").document(post.getPostId())
                .update("likes", likes)
                .addOnSuccessListener(aVoid -> {
                    // Refresh adapter to update like icon and count
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update likes.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onResolveClick(Post post) {
        post.setResolved(!post.isResolved());
        db.collection("posts").document(post.getPostId())
                .update("resolved", post.isResolved())
                .addOnSuccessListener(aVoid -> adapter.notifyDataSetChanged())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update status.", Toast.LENGTH_SHORT).show());
    }
    @Override
    public void onCommentClick(Post post) {
        if (post == null || post.getPostId() == null) return;

        CommentsFragment commentsFragment = CommentsFragment.newInstance(post.getPostId());

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, commentsFragment) // Replace with your container id
                .addToBackStack(null)
                .commit();
    }


}