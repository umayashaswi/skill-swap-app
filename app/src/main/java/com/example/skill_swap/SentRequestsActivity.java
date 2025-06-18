package com.example.skill_swap;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class SentRequestsActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private SentRequestAdapter adapter;
    private final List<Request> sentRequests = new ArrayList<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sent_requests);
        setTitle("Sent Requests");

        recyclerView = findViewById(R.id.recyclerViewRequests);
        adapter = new SentRequestAdapter(sentRequests, this::deleteRequest);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fetchSentRequests();
    }

    private void fetchSentRequests() {
        String myUid = mAuth.getCurrentUser().getUid();

        db.collection("requests")
                .whereEqualTo("fromUid", myUid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    sentRequests.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Request r = Request.fromDoc(doc);

                        // Fetch recipient's name from 'users' collection
                        db.collection("users")
                                .document(r.getToUid())
                                .get()
                                .addOnSuccessListener(userSnap -> {
                                    if (userSnap.exists()) {
                                        r.setToUserName(userSnap.getString("name"));
                                        sentRequests.add(r);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
    private void deleteRequest(Request request) {
        db.collection("requests")
                .document(request.getId()) // Assuming Request has a getId() method
                .delete()
                .addOnSuccessListener(unused -> {
                    sentRequests.remove(request);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Request deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

}
