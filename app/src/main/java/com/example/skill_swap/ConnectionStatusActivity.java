package com.example.skill_swap;

import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/** One screen that shows both incoming (with Accept/Reject) and outgoing (view‑only). */
public class ConnectionStatusActivity extends BaseActivity
        implements RequestsAdapter.OnActionListener {

    /* UI */
    private RecyclerView rvIncoming, rvOutgoing;
    private RequestsAdapter inAdapter, outAdapter;

    /* Data */
    private final List<Request> incoming = new ArrayList<>();
    private final List<Request> outgoing = new ArrayList<>();

    /* Firebase */
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_connection_status);
        setTitle("Connection Status");

        rvIncoming = findViewById(R.id.rvIncoming);
        rvOutgoing = findViewById(R.id.rvOutgoing);

        inAdapter = new RequestsAdapter(incoming, this, true);   // incoming mode
        outAdapter = new RequestsAdapter(outgoing, null, false); // outgoing view-only

        rvIncoming.setLayoutManager(new LinearLayoutManager(this));
        rvOutgoing.setLayoutManager(new LinearLayoutManager(this));
        rvIncoming.setAdapter(inAdapter);
        rvOutgoing.setAdapter(outAdapter);

        fetchIncoming();  // requests TO me
        fetchOutgoing();  // requests I SENT
    }

    /* ---------------- Fetch INCOMING (toUid == me, status == Pending) --------------- */
    private void fetchIncoming() {
        String myUid = auth.getCurrentUser().getUid();

        db.collection("connection_requests")
                .whereEqualTo("toUid", myUid)
                .whereEqualTo("status", "Pending")
                .get()
                .addOnSuccessListener(snap -> {
                    incoming.clear();
                    if (snap.isEmpty()) inAdapter.notifyDataSetChanged();

                    for (QueryDocumentSnapshot doc : snap) {
                        Request r = Request.fromDoc(doc);

                        db.collection("users").document(r.getFromUid()).get()
                                .addOnSuccessListener(userDoc -> {
                                    r.setFromUserName(userDoc.getString("name"));
                                    r.setFromUserSkills(userDoc.getString("skillsOffered"));
                                    incoming.add(r);
                                    inAdapter.notifyDataSetChanged();
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /* ---------------- Fetch OUTGOING (fromUid == me) ---------------- */
    private void fetchOutgoing() {
        String myUid = auth.getCurrentUser().getUid();

        db.collection("connection_requests")
                .whereEqualTo("fromUid", myUid)
                .get()
                .addOnSuccessListener(snap -> {
                    outgoing.clear();
                    if (snap.isEmpty()) outAdapter.notifyDataSetChanged();

                    for (QueryDocumentSnapshot doc : snap) {
                        Request r = Request.fromDoc(doc);

                        db.collection("users").document(r.getToUid()).get()
                                .addOnSuccessListener(userDoc -> {
                                    r.setFromUserName("You");
                                    r.setToUserName(userDoc.getString("name"));
                                    r.setToUserSkills(userDoc.getString("skillsOffered"));
                                    outgoing.add(r);
                                    outAdapter.notifyDataSetChanged();
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /* ---------------- Accept / Reject callbacks ---------------- */
    @Override
    public void onAccept(Request req, int pos) {
        db.collection("connection_requests")
                .document(req.getId())
                .update("status", "Accepted")
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Accepted!", Toast.LENGTH_SHORT).show();
                    incoming.remove(pos);
                    inAdapter.notifyItemRemoved(pos);
                    fetchOutgoing(); // outgoing list might be affected
                });
    }

    @Override
    public void onReject(Request req, int pos) {
        db.collection("connection_requests")
                .document(req.getId())
                .update("status", "Rejected")
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Rejected!", Toast.LENGTH_SHORT).show();
                    incoming.remove(pos);
                    inAdapter.notifyItemRemoved(pos);
                });
    }
}
