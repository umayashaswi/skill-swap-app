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

import com.example.skill_swap.R;
import com.example.skill_swap.adapter.PostAdapter;
import com.example.skill_swap.comments.CommentsFragment;
import com.example.skill_swap.model.Post;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ConnectionsFeedFragment extends Fragment implements PostAdapter.OnPostActionListener {

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> postList;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    public ConnectionsFeedFragment() {
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

        fetchConnectionsPosts();

        return view;
    }

    private void fetchConnectionsPosts() {
        progressBar.setVisibility(View.VISIBLE);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("connection_requests")
                .whereEqualTo("status", "Accepted")
                .whereArrayContains("uids", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> connections = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        List<String> uids = (List<String>) doc.get("uids");
                        if (uids != null) {
                            for (String uid : uids) {
                                if (!uid.equals(currentUserId) && !connections.contains(uid)) {
                                    connections.add(uid);
                                }
                            }
                        }
                    }

                    if (connections.isEmpty()) {
                        postList.clear();
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "No connections found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    fetchPostsFromConnections(connections);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to load connections: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }



    private void fetchPostsFromConnections(List<String> connections) {
        int chunkSize = 10;
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        for (int i = 0; i < connections.size(); i += chunkSize) {
            List<String> chunk = connections.subList(i, Math.min(i + chunkSize, connections.size()));
            Task<QuerySnapshot> task = db.collection("posts")
                    .whereIn("userId", chunk)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get();
            tasks.add(task);
        }

        Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(results -> {
                    postList.clear();
                    for (Object result : results) {
                        QuerySnapshot querySnapshot = (QuerySnapshot) result;
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                post.setPostId(doc.getId());
                                postList.add(post);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to load connections' posts.", Toast.LENGTH_SHORT).show();
                });
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

        db.collection("posts").document(post.getPostId())
                .update("likes", likes)
                .addOnSuccessListener(aVoid -> adapter.notifyDataSetChanged())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update likes.", Toast.LENGTH_SHORT).show());
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

        getChildFragmentManager()

                .beginTransaction()
                .replace(R.id.fragment_container, commentsFragment)
                .addToBackStack(null)
                .commit();
    }
}
