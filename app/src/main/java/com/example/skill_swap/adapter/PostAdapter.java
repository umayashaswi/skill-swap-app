package com.example.skill_swap.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skill_swap.R;
import com.example.skill_swap.model.Post;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final List<Post> postList;
    private final OnPostActionListener listener;
    private final SimpleDateFormat dateFmt =
            new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault());

    /* ---------- callback interface ---------- */
    public interface OnPostActionListener {
        void onLikeClick(Post post);
        void onResolveClick(Post post);   // still here for future
        void onCommentClick(Post post);   // NEW
    }

    public PostAdapter(List<Post> postList, OnPostActionListener listener) {
        this.postList = postList;
        this.listener = listener;
    }

    @NonNull @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder h, int pos) {
        h.bind(postList.get(pos));
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    /* --------------------------------------------------------------------- */
    class PostViewHolder extends RecyclerView.ViewHolder {

        TextView tvAuthor, tvContent, tvTime, tvLikeCount,
                tvCommentCount, tvResolved;
        ImageButton btnLike, btnComment;

        PostViewHolder(@NonNull View item) {
            super(item);
            tvAuthor       = item.findViewById(R.id.tvAuthor);
            tvContent      = item.findViewById(R.id.tvContent);
            tvTime         = item.findViewById(R.id.tvTime);
            tvLikeCount    = item.findViewById(R.id.tvLikeCount);
            tvCommentCount = item.findViewById(R.id.tvCommentCount);
            tvResolved     = item.findViewById(R.id.tvResolved);
            btnLike        = item.findViewById(R.id.btnLike);
            btnComment     = item.findViewById(R.id.btnComment);
        }

        void bind(Post post) {

            /* --- basic text --- */
            tvAuthor.setText(post.getAuthorName());
            tvContent.setText(post.getContent());
            tvTime.setText(dateFmt.format(post.getTimestamp().toDate()));

            /* --- counts --- */
            int likeSize    = post.getLikes()   != null ? post.getLikes().size()   : 0;
            int commentCount = post.getCommentCount();
            tvLikeCount.setText(String.valueOf(likeSize));
            tvCommentCount.setText(String.valueOf(commentCount));


            /* --- like icon state --- */
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            boolean liked = post.getLikes() != null && post.getLikes().contains(uid);
            btnLike.setImageResource(liked
                    ? R.drawable.ic_like_filled
                    : R.drawable.ic_like_outline);

            btnLike.setOnClickListener(v -> listener.onLikeClick(post));
            /* --- comment button click --- */
            btnComment.setOnClickListener(v -> listener.onCommentClick(post));
            /* --- resolved badge --- */
            tvResolved.setVisibility(post.isResolved() ? View.VISIBLE : View.GONE);
        }
    }
}