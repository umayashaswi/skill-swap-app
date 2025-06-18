package com.example.skill_swap;

import android.app.AlertDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class InviteAdapter extends RecyclerView.Adapter<InviteAdapter.InviteVH> {

    private final List<Request> data;
    private final OnActionListener listener;
    private final Set<Integer> detailsViewedPositions = new HashSet<>();

    public interface OnActionListener {
        void onAccept(Request r, int position);
        void onReject(Request r, int position);
    }

    public InviteAdapter(List<Request> data, OnActionListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InviteVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invite, parent, false);
        return new InviteVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InviteVH h, int position) {
        Request r = data.get(position);

        h.tvFromUser.setText("From: " + (r.getFromUserName() != null ? r.getFromUserName() : r.getFromUid()));
        h.tvStatus.setText("Status: " + safe(r.getStatus()));

        String pref = "-";
        if (r.getPreferredDate() != null) {
            String d = formatDate(h.itemView.getContext(), r.getPreferredDate());
            pref = d + (r.getPreferredTime() != null ? "  " + r.getPreferredTime() : "");
        } else if (r.getPreferredTime() != null) {
            pref = r.getPreferredTime();
        }
        h.tvPreferred.setText("Preferred: " + pref);

        boolean detailsViewed = detailsViewedPositions.contains(position);
        h.btnAccept.setEnabled(detailsViewed);
        h.btnReject.setEnabled(detailsViewed);

        h.btnViewDetails.setOnClickListener(v -> {
            showRequestDetailsDialog(v.getContext(), r);
            detailsViewedPositions.add(position);
            notifyItemChanged(position);
        });

        h.btnAccept.setOnClickListener(v -> {
            listener.onAccept(r, position);

            // START: Inbox message creation (UPDATED to match Firestore rules)
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null && r.getFromUid() != null) {
                String currentUserName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "A Skill Swap User";
                String currentUserEmail = currentUser.getEmail() != null ? currentUser.getEmail() : "no-email@example.com";

                Map<String, Object> message = new HashMap<>();
                message.put("toUserId", r.getFromUid());
                message.put("fromUserId", currentUser.getUid());
                String longText = "Hello " + currentUserName + ", Your swap request has been accepted! 🎉 " +
                        "You can reach out to me directly at: " + currentUserEmail + ".\n\n" +
                        "I'm excited to move forward with our skill swap. Let's make this a great experience together! " +
                        "Thank you for choosing Skill Swap.\n\n" +
                        "--- Note: Please use the email above to communicate further.";

                message.put("text", longText);
                message.put("timestamp", new com.google.firebase.Timestamp(new java.util.Date()));


                // Write message under inbox/{toUserId}/messages collection
                db.collection("inbox")
                        .document(r.getFromUid()) // recipient's userId document
                        .collection("messages")
                        .add(message)
                        .addOnSuccessListener(docRef -> Log.d("Inbox", "Inbox message created"))
                        .addOnFailureListener(e -> Log.e("Inbox", "Failed to send inbox message", e));
            } else {
                Log.e("Inbox", "Missing currentUser or requester UID");
            }
            // END: Inbox message creation
        });

        h.btnReject.setOnClickListener(v -> listener.onReject(r, position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private void showRequestDetailsDialog(Context context, Request r) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_invite_details, null);

        ((TextView) view.findViewById(R.id.tvFullName)).setText("Full Name: " + safe(r.getFromUserName()));
        ((TextView) view.findViewById(R.id.tvStatus)).setText("Status: " + safe(r.getStatus()));
        ((TextView) view.findViewById(R.id.tvPreferredDate)).setText("Preferred Date: " + formatDate(context, r.getPreferredDate()));
        ((TextView) view.findViewById(R.id.tvPreferredTime)).setText("Preferred Time: " + safe(r.getPreferredTime()));
        ((TextView) view.findViewById(R.id.tvReason)).setText("Reason: " + safe(r.getReason()));
        ((TextView) view.findViewById(R.id.tvSkillOffered)).setText("Skill Offered: " + safe(r.getSkillOffered()));
        ((TextView) view.findViewById(R.id.tvSkillWanted)).setText("Skill Wanted: " + safe(r.getSkillWanted()));
        ((TextView) view.findViewById(R.id.tvSkillLevel)).setText("Skill Level: " + safe(r.getSkillLevel()));
        ((TextView) view.findViewById(R.id.tvYearsExp)).setText("Years of Experience: " + safe(r.getYearsOfExperience()));
        ((TextView) view.findViewById(R.id.tvExperienceDescription)).setText("Experience Description: " + safe(r.getExperienceDescription()));
        ((TextView) view.findViewById(R.id.tvPortfolioLink)).setText("Portfolio: " + safe(r.getPortfolioLink()));
        ((TextView) view.findViewById(R.id.tvWhyLearn)).setText("Why Learn: " + safe(r.getWhyLearn()));
        ((TextView) view.findViewById(R.id.tvCommunicationMode)).setText("Communication Mode: " + safe(r.getCommunicationMode()));
        ((TextView) view.findViewById(R.id.tvNumberOfSessions)).setText("Sessions: " + safe(r.getNumberOfSessions()));
        ((TextView) view.findViewById(R.id.tvSessionDuration)).setText("Session Duration: " + safe(r.getSessionDuration()));
        ((TextView) view.findViewById(R.id.tvExpectedProficiency)).setText("Expected Proficiency: " + safe(r.getExpectedProficiency()));
        ((TextView) view.findViewById(R.id.tvOtherCommunication)).setText("Other Communication Info: " + safe(r.getOtherCommunication()));
        ((TextView) view.findViewById(R.id.tvMessage)).setText("Message: " + safe(r.getMessage()));

        new AlertDialog.Builder(context)
                .setTitle("Request Details")
                .setView(view)
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private String safe(String s) {
        return s != null && !s.isEmpty() ? s : "-";
    }

    private String formatDate(Context context, String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "-";
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(dateStr);
            if (date != null) {
                return DateFormat.getDateFormat(context).format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    static class InviteVH extends RecyclerView.ViewHolder {
        TextView tvFromUser, tvStatus, tvPreferred;
        Button btnAccept, btnReject, btnViewDetails;

        public InviteVH(@NonNull View v) {
            super(v);
            tvFromUser = v.findViewById(R.id.tvFromUser);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvPreferred = v.findViewById(R.id.tvPreferred);

            btnAccept = v.findViewById(R.id.btnAccept);
            btnReject = v.findViewById(R.id.btnReject);
            btnViewDetails = v.findViewById(R.id.btnViewDetails);
        }
    }
}
