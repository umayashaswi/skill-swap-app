package com.example.skill_swap.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

public class Comment {
    @DocumentId
    private String commentId;
    private String commenterId;
    private String commenterName;
    private String commentText;
    private Timestamp timestamp;

    public Comment() {}  // Required by Firestore

    public Comment(String commenterId, String commenterName, String commentText) {
        this.commenterId = commenterId;
        this.commenterName = commenterName;
        this.commentText = commentText;
        this.timestamp = Timestamp.now();
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommenterId() {
        return commenterId;
    }

    public void setCommenterId(String commenterId) {
        this.commenterId = commenterId;
    }

    public String getCommenterName() {
        return commenterName;
    }

    public void setCommenterName(String commenterName) {
        this.commenterName = commenterName;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}