package com.example.skill_swap;

import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class IncomingRequestsActivity
        extends BaseActivity
        implements RequestsAdapter.OnActionListener {

    private RecyclerView recyclerView;
    private RequestsAdapter adapter;
    private final List<Request> incomingRequests = new ArrayList<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth      auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_requests);
        setTitle("Incoming Requests");

        recyclerView = findViewById(R.id.recyclerViewRequests);
        adapter = new RequestsAdapter(incomingRequests, this, /*incomingMode=*/true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fetchIncomingRequests();
    }

    /* ------------------------------------------------------------------ */
    /** Download all PENDING skill-swap requests addressed to me
     *  + enrich each Request with sender profile fields so the adapter
     *    can display them.
     */
    private void fetchIncomingRequests() {
        String myUid = auth.getUid();
        if (myUid == null) return;

        db.collection("requests")
                .whereEqualTo("toUid", myUid)
                .whereIn("status", List.of("pending", "Pending"))
                .get()
                .addOnSuccessListener(snapshot -> {
                    incomingRequests.clear();

                    if (snapshot.isEmpty()) {
                        Toast.makeText(this, "No incoming requests",
                                Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Request r = Request.fromDoc(doc);

                        /* offeredSkill is saved INSIDE the request doc */
                        r.setFromUserSkills(doc.getString("offeredSkill"));

                        /* fetch sender profile for name, bio, rating, area */
                        db.collection("users")
                                .document(r.getFromUid())
                                .get()
                                .addOnSuccessListener(userSnap -> {
                                    if (userSnap.exists()) {
                                        r.setFromUserName (userSnap.getString("name"));
                                        r.setFromUserArea (userSnap.getString("area"));
                                        r.setFromUserBio  (userSnap.getString("bio"));

                                        Double rating = userSnap.getDouble("rating");
                                        r.setFromUserRating(
                                                rating == null ? 0f : rating.floatValue());
                                    } else {
                                        r.setFromUserName ("Unknown");
                                        r.setFromUserSkills("");
                                    }
                                    incomingRequests.add(r);
                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    // fallback if user doc fails
                                    r.setFromUserName ("Unknown");
                                    incomingRequests.add(r);
                                    adapter.notifyDataSetChanged();
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    /* ------------------------------------------------------------------ */
    /** Accept the swap: set status = Accepted */
    @Override
    public void onAccept(Request req, int pos) {
        db.collection("requests").document(req.getId())
                .update("status", "accepted")
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Request accepted", Toast.LENGTH_SHORT).show();
                    incomingRequests.get(pos).setStatus("accepted");
                    adapter.notifyItemChanged(pos);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    /** Reject the swap: set status = Rejected */
    @Override
    public void onReject(Request req, int pos) {
        db.collection("requests").document(req.getId())
                .update("status", "rejected")
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Request rejected", Toast.LENGTH_SHORT).show();
                    incomingRequests.get(pos).setStatus("rejected");
                    adapter.notifyItemChanged(pos);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}
