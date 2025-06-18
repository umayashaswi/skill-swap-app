package com.example.skill_swap;


import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import android.util.Log;

import java.util.List;

public class ChatActivity extends BaseActivity {

    private TextView chatWithText;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLayoutInflater().inflate(R.layout.activity_chat, findViewById(R.id.content_frame));
        setTitle("Chat");

        chatWithText = findViewById(R.id.chatWithText);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        String myUid = auth.getCurrentUser().getUid();
        String otherUid = getIntent().getStringExtra("uid");
        final String usernameFinal = getIntent().getStringExtra("username");  // keep final

        if (otherUid == null) {
            Toast.makeText(this, "Invalid chat user.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Use an array to hold mutable boolean for lambda
        final boolean[] isConnected = {false};

        db.collection("connection_requests")
                .whereEqualTo("status", "Accepted")
                .whereArrayContains("uids", myUid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        List<String> uids = (List<String>) doc.get("uids");
                        if (uids != null && uids.contains(otherUid)) {
                            isConnected[0] = true;
                            break;
                        }
                    }

                    if (isConnected[0]) {
                        String nameToShow = (usernameFinal == null) ? "Unknown" : usernameFinal;
                        chatWithText.setText("Chatting with " + nameToShow);
                        // TODO: Load/send messages from here
                    } else {
                        Toast.makeText(this, "You are not connected with this user.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Connection check failed", e);
                    Toast.makeText(this, "Failed to verify connection: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });

    }

}
