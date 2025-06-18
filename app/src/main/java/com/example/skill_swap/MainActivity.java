package com.example.skill_swap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.skill_swap.ui.FeedActivity;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class MainActivity extends BaseActivity {

    private TextView welcomeText;
    private Button btnViewProfiles, btnSearchUsers, btnSendRequest;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration notificationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This now automatically inflates the drawer + toolbar + activity_main inside the content_frame
        setContentView(R.layout.activity_main);

        welcomeText = findViewById(R.id.welcomeText);
        btnViewProfiles = findViewById(R.id.btnViewProfiles);
        btnSearchUsers = findViewById(R.id.btnSearchUsers);
        btnSendRequest = findViewById(R.id.btnSendRequest);

        loadWelcomeMessage();

        Button chatButton = findViewById(R.id.btnChatWithConnections);
        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FeedActivity.class);
            startActivity(intent);
        });

        Button btnConnectionStatus = findViewById(R.id.btnConnectionStatus);
        btnConnectionStatus.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ConnectionStatusActivity.class)));

        btnViewProfiles.setOnClickListener(v -> {
            Intent intent = new Intent(this, UsersListActivity.class);
            startActivity(intent);
        });

        btnSearchUsers.setOnClickListener(v ->
                startActivity(new Intent(this, SearchSkillsActivity.class)));

        btnSendRequest.setOnClickListener(v ->
                startActivity(new Intent(this, SentRequestsActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        listenForNotifications();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (notificationListener != null) {
            notificationListener.remove();
            notificationListener = null;
        }
    }

    private void loadWelcomeMessage() {
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            db.collection("users")
                    .document(mAuth.getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        String name = doc.getString("name");
                        welcomeText.setText(getString(R.string.welcome_message, name));
                    });
        } else {
            welcomeText.setText(getString(R.string.welcome_message, ""));
        }
    }

    private void listenForNotifications() {
        if (mAuth == null || mAuth.getCurrentUser() == null) return;

        String myUid = mAuth.getCurrentUser().getUid();

        notificationListener = db.collection("notifications")
                .whereEqualTo("toUid", myUid)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    for (DocumentChange docChange : snapshots.getDocumentChanges()) {
                        if (docChange.getType() == DocumentChange.Type.ADDED) {
                            String message = docChange.getDocument().getString("message");

                            runOnUiThread(() ->
                                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            );

                            // Delete the notification once shown
                            db.collection("notifications")
                                    .document(docChange.getDocument().getId())
                                    .delete();
                        }
                    }
                });
    }
}
