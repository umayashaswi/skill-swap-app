package com.example.skill_swap;

import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvitesActivity extends BaseActivity implements InviteAdapter.OnActionListener {

    private RecyclerView recyclerView;
    private InviteAdapter adapter;
    private final List<Request> inviteRequests = new ArrayList<>();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String myUsername;
    private String myEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invites);
        setTitle("Received Invites");

        recyclerView = findViewById(R.id.your_recycler_view);
        adapter = new InviteAdapter(inviteRequests, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fetchMyUserProfile(); // Fetch name and email before loading requests
    }

    private void fetchMyUserProfile() {
        String myUid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(myUid).get()
                .addOnSuccessListener(doc -> {
                    myUsername = doc.getString("name");
                    myEmail = doc.getString("email");
                    if (myEmail == null) {
                        // fallback to FirebaseAuth email if not in Firestore
                        myEmail = mAuth.getCurrentUser().getEmail();
                    }
                    fetchReceivedRequests();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Unable to get your profile.", Toast.LENGTH_SHORT).show();
                    myUsername = "User";
                    myEmail = mAuth.getCurrentUser().getEmail();
                    fetchReceivedRequests(); // fallback without name and email
                });
    }

    private void fetchReceivedRequests() {
        String myUid = mAuth.getCurrentUser().getUid();

        db.collection("requests")
                .whereEqualTo("toUid", myUid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    inviteRequests.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Request r = Request.fromDoc(doc);

                        // fetch sender details
                        db.collection("users").document(r.getFromUid())
                                .get()
                                .addOnSuccessListener(userSnap -> {
                                    if (userSnap.exists()) {
                                        r.setFromUserName(userSnap.getString("name"));
                                        r.setFromUserSkills(userSnap.getString("skills"));
                                        r.setFromUserBio(userSnap.getString("bio"));
                                        r.setFromUserArea(userSnap.getString("area"));
                                        Object rating = userSnap.get("rating");
                                        if (rating instanceof Number) {
                                            r.setFromUserRating(((Number) rating).floatValue());
                                        }
                                    }

                                    // Also fetch reason from request doc itself
                                    r.setReason(doc.getString("reason"));

                                    inviteRequests.add(r);
                                    adapter.notifyDataSetChanged();
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to fetch invites: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    public void onAccept(Request r, int position) {
        acceptRequest(r, position);
    }

    @Override
    public void onReject(Request r, int position) {
        rejectRequest(r, position);
    }

    private void acceptRequest(Request request, int position) {
        DocumentReference requestRef = db.collection("requests").document(request.getId());

        requestRef.update("status", "Accepted")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Request accepted", Toast.LENGTH_SHORT).show();
                    inviteRequests.get(position).setStatus("Accepted");
                    adapter.notifyItemChanged(position);

                    // Generate personalized acceptance message
                    String message = generateAcceptanceMessage(
                            request.getFromUserName() != null ? request.getFromUserName() : "Friend",
                            myEmail != null ? myEmail : "no-reply@skillswap.com"
                    );

                    // Save acceptance message to inbox
                    saveMessageToInbox(request.getFromUid(), message);

                    // Optionally still send notification (comment if not needed)
                    sendNotificationToUser(request.getFromUid(), "Your swap request has been accepted by " + myUsername);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to accept: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void rejectRequest(Request request, int position) {
        DocumentReference requestRef = db.collection("requests").document(request.getId());

        requestRef.update("status", "Rejected")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Request rejected", Toast.LENGTH_SHORT).show();
                    inviteRequests.get(position).setStatus("Rejected");
                    adapter.notifyItemChanged(position);

                    // Generate polite rejection message
                    String message = generateRejectionMessage(
                            request.getFromUserName() != null ? request.getFromUserName() : "Friend"
                    );

                    // Save rejection message to inbox
                    saveMessageToInbox(request.getFromUid(), message);

                    // Optionally send notification (comment if not needed)
                    sendNotificationToUser(request.getFromUid(), "Your swap request has been rejected by " + myUsername);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to reject: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void saveMessageToInbox(String toUid, String message) {
        Map<String, Object> inboxMessage = new HashMap<>();
        inboxMessage.put("senderUid", mAuth.getCurrentUser().getUid());
        inboxMessage.put("message", message);
        inboxMessage.put("timestamp", System.currentTimeMillis());
        inboxMessage.put("read", false);

        db.collection("inbox")
                .document(toUid)
                .collection("messages")
                .add(inboxMessage)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Message added to inbox", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void sendNotificationToUser(String toUid, String message) {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> notification = new HashMap<>();
        notification.put("toUid", toUid);
        notification.put("fromUid", currentUid);
        notification.put("message", message);
        notification.put("timestamp", System.currentTimeMillis());

        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(docRef ->
                        Toast.makeText(this, "User notified", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to notify: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // Acceptance message generator with greeting based on time
    private String generateAcceptanceMessage(String recipientUsername, String accepterEmail) {
        String greeting = getTimeBasedGreeting();

        return greeting + " " + recipientUsername + ",\n\n"
                + "Your swap request has been accepted! 🎉\n\n"
                + "You can reach out to me directly at: " + accepterEmail + "\n\n"
                + "I'm excited to move forward with our skill swap. Let's make this a great experience together!\n\n"
                + "Thank you for choosing Skill Swap.\n\n"
                + "---\n"
                + "Note: This is a no-reply message. Please use the email above to communicate further.";
    }

    // Polite rejection message generator
    private String generateRejectionMessage(String recipientUsername) {
        String greeting = getTimeBasedGreeting();

        return greeting + " " + recipientUsername + ",\n\n"
                + "Thank you for your interest in Skill Swap. After careful consideration, I am unable to accept your swap request at this time.\n\n"
                + "I appreciate your understanding and wish you all the best in your skill exchange journey.\n\n"
                + "Thank you!\n\n"
                + "---\n"
                + "Note: This is a no-reply message.";
    }

    private String getTimeBasedGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return "Good morning";
        } else if (hour >= 12 && hour < 17) {
            return "Good afternoon";
        } else if (hour >= 17 && hour < 21) {
            return "Good evening";
        } else {
            return "Hello";
        }
    }
}
