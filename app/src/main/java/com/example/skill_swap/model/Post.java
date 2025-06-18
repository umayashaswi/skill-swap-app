package com.example.skill_swap.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import java.util.ArrayList;
import java.util.List;

public class Post {
    @DocumentId
    private String postId;
    private String authorId;
    private String authorName;
    private int commentCount;
    private Timestamp timestamp;
    private String content;
    private List<String> tags;
    private List<String> likes;       // list of userIds who liked the post
    private boolean resolved;
    private String visibility;        // "connections" or "public"

    // Empty constructor required for Firestore serialization
    public Post() {}

    // Main constructor used to create new posts
    public Post(String authorId, String authorName,
                String content, List<String> tags, String visibility) {
        this.authorId   = authorId;
        this.authorName = authorName;
        this.content    = content;
        this.tags       = tags == null ? new ArrayList<>() : tags;
        this.likes      = new ArrayList<>();
        this.timestamp  = Timestamp.now();
        this.visibility = visibility;
        this.resolved   = false;
    }

    // Optional constructor if you want to specify timestamp or likes explicitly in future
    public Post(String authorId, String authorName, String content,
                List<String> tags, List<String> likes,
                Timestamp timestamp, boolean resolved, String visibility) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.content = content;
        this.tags = tags == null ? new ArrayList<>() : tags;
        this.likes = likes == null ? new ArrayList<>() : likes;
        this.timestamp = timestamp == null ? Timestamp.now() : timestamp;
        this.resolved = resolved;
        this.visibility = visibility;
    }

    // Getters and setters follow...

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public String getPostId() {
        return postId;
    }
    public void setPostId(String postId) {
        this.postId = postId;
    }
    public String getAuthorId() {
        return authorId;
    }
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
    public String getAuthorName() {
        return authorName;
    }
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    public Timestamp getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public List<String> getTags() {
        return tags;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    public List<String> getLikes() {
        return likes;
    }
    public void setLikes(List<String> likes) {
        this.likes = likes;
    }
    public boolean isResolved() {
        return resolved;
    }
    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }
    public String getVisibility() {
        return visibility;
    }
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
}