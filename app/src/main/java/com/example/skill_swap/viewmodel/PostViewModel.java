package com.example.skill_swap.viewmodel;

import com.example.skill_swap.repo.PostRepository;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import com.example.skill_swap.model.Post;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class PostViewModel extends ViewModel {

    private final PostRepository repository = new PostRepository();
    private final MutableLiveData<List<Post>> _posts = new MutableLiveData<>();
    public LiveData<List<Post>> posts = _posts;

    private ListenerRegistration postListener;

    public PostViewModel() {
        loadPosts();
    }

    public void loadPosts() {
        postListener = repository.feedQuery().addSnapshotListener((value, error) -> {
            if (error != null) {
                _posts.setValue(null);
                return;
            }
            List<Post> postList = new ArrayList<>();
            if (value != null) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Post post = doc.toObject(Post.class);
                    // PostViewModel.java  (inside loadPosts → onPostsFetched loop)
                    if (post != null) {
                        post.setPostId(doc.getId());   // <-- use the correct setter
                        postList.add(post);
                    }

                }
            }
            _posts.setValue(postList);
        });
    }

    public void toggleLike(@NonNull String postId, boolean like) {
        repository.toggleLike(postId, like);
    }

    public void markResolved(@NonNull String postId, boolean resolved) {
        repository.markResolved(postId, resolved);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (postListener != null) {
            postListener.remove();
        }
    }
}
