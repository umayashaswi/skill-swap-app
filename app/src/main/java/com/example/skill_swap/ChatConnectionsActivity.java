package com.example.skill_swap;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatConnectionsActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private final List<User> connections = new ArrayList<>();
    private ConnectionsAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private final Set<String> seenUserIds = new HashSet<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_connections);

        setTitle("Chat with Connections");

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.connectionsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConnectionsAdapter(this, connections);
        recyclerView.setAdapter(adapter);

        loadConnections();  // 🔑
    }

    /** Load all accepted connections involving the current user */
    private void loadConnections() {
        final String myUid = auth.getCurrentUser().getUid();

        db.collection("connection_requests")
                .whereEqualTo("status", "Accepted")
                .whereIn("fromUid", Arrays.asList(myUid, "otherUid" ))
                .whereIn("toUid",  Arrays.asList(myUid, "otherUid"))
                .addSnapshotListener((snap, err) -> {          // ← use snapshotListener
                    if (err != null) {
                        Log.e("Firestore", "Listen failed: ", err);
                        return;
                    }
                    if (snap == null) return;

                    connections.clear();
                    seenUserIds.clear();

                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        String fromUid = doc.getString("fromUid");
                        String toUid   = doc.getString("toUid");
                        if (fromUid == null || toUid == null) continue;

                        String otherUid = myUid.equals(fromUid) ? toUid : fromUid;
                        if (!seenUserIds.add(otherUid)) continue;   // true if not present

                        db.collection("users").document(otherUid)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    if (!userDoc.exists()) return;
                                    String name = userDoc.getString("name");
                                    connections.add(new User(otherUid,
                                            name == null ? otherUid : name));
                                    adapter.notifyDataSetChanged();
                                });
                    }
                });
    }

}
