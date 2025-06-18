package com.example.skill_swap.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.skill_swap.R;
import com.example.skill_swap.model.Post;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NewPostActivity extends AppCompatActivity {

    private EditText etPostContent;
    private Button btnSubmitPost;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        etPostContent = findViewById(R.id.etPostContent);
        btnSubmitPost = findViewById(R.id.btnSubmitPost);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        btnSubmitPost.setOnClickListener(v -> submitPost());
    }

    private void submitPost() {
        String content = etPostContent.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            etPostContent.setError("Post content cannot be empty");
            return;
        }

        btnSubmitPost.setEnabled(false);

        String userId = auth.getCurrentUser().getUid();

        // Fetch user profile to get the name
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("name");
                        if (userName == null || userName.isEmpty()) {
                            userName = "Unknown User";
                        }

                        List<String> tags = new ArrayList<>();  // You can add tags UI later
                        String visibility = "public";  // Update as per your UI

                        Post post = new Post(userId, userName, content, tags, visibility);

                        db.collection("posts")
                                .add(post)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(this, "Post created!", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to create post: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    btnSubmitPost.setEnabled(true);
                                });

                    } else {
                        Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
                        btnSubmitPost.setEnabled(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch user profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSubmitPost.setEnabled(true);
                });
    }

}