package com.example.skill_swap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView adapter that can work in two modes:
 *  1) Incoming  – shows Accept / Reject buttons
 *  2) Outgoing  – hides buttons and shows request status
 */
public class RequestsAdapter
        extends RecyclerView.Adapter<RequestsAdapter.RequestViewHolder> {

    /* ---------- callback interface ---------- */
    public interface OnActionListener {
        void onAccept(Request request, int position);
        void onReject(Request request, int position);
    }

    /* ---------- fields ---------- */
    private final List<Request> requests;
    private final OnActionListener listener;   // may be null in outgoing‑only mode
    private final boolean isIncomingMode;      // true = show buttons

    /* ---------- ctor ---------- */
    public RequestsAdapter(List<Request> requests,
                           OnActionListener listener,
                           boolean isIncomingMode) {
        this.requests       = requests;
        this.listener       = listener;
        this.isIncomingMode = isIncomingMode;
    }

    /* ---------- view‑holder ---------- */
    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserSkills, tvStatus;
        Button   btnAccept, btnReject;

        public RequestViewHolder(@NonNull View item) {
            super(item);
            tvUserName  = item.findViewById(R.id.tvUserName);
            tvUserSkills= item.findViewById(R.id.tvUserSkills);
            tvStatus    = item.findViewById(R.id.tvStatus);   // ⬅️ new status field
            btnAccept   = item.findViewById(R.id.btnAccept);
            btnReject   = item.findViewById(R.id.btnReject);
        }
    }

    /* ---------- required adapter methods ---------- */
    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder h, int pos) {
        Request r = requests.get(pos);

        if (isIncomingMode) {
            h.tvUserName.setText(r.getFromUserName());
            h.tvUserSkills.setText("Skills: " + r.getFromUserSkills());
        } else {
            h.tvUserName.setText(r.getToUserName());
            h.tvUserSkills.setText("Skills: " + r.getToUserSkills());
        }


        if (isIncomingMode) {                    // 🔵 incoming → show buttons
            h.tvStatus .setVisibility(View.GONE);
            h.btnAccept.setVisibility(View.VISIBLE);
            h.btnReject.setVisibility(View.VISIBLE);

            h.btnAccept.setOnClickListener(v -> {
                if (listener != null) listener.onAccept(r, pos);
            });

            h.btnReject.setOnClickListener(v -> {
                if (listener != null) listener.onReject(r, pos);
            });

        } else {                                 // 🟢 outgoing → show status
            h.tvStatus .setVisibility(View.VISIBLE);
            h.btnAccept.setVisibility(View.GONE);
            h.btnReject.setVisibility(View.GONE);
            h.tvStatus.setText("Status: " + r.getStatus());
        }
    }

    @Override
    public int getItemCount() { return requests.size(); }
}
