package com.example.skill_swap;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Simple adapter that lists requests you have SENT */
public class SentRequestAdapter extends RecyclerView.Adapter<SentRequestAdapter.ReqVH> {

    private final List<Request> data;
    private final OnRequestDeleteListener deleteListener;

    public SentRequestAdapter(List<Request> data, OnRequestDeleteListener deleteListener) {
        this.data = data;
        this.deleteListener = deleteListener;
    }

    public interface OnRequestDeleteListener {
        void onDelete(Request request);
    }

    @NonNull
    @Override
    public ReqVH onCreateViewHolder(@NonNull ViewGroup p, int vType) {
        View v = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_sent_request, p, false);
        return new ReqVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReqVH h, int pos) {
        Request r = data.get(pos);

        h.tvUser.setText("To: " + (r.getToUserName() == null ? r.getToUid() : r.getToUserName()));
        h.tvStatus.setText("Status: " + r.getStatus());

        // Preferred date/time string
        String pref = "-";
        if (r.getPreferredDate() != null) {
            try {
                Log.d("SentRequestAdapter", "Preferred Date: " + r.getPreferredDate());
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date parsedDate = inputFormat.parse(r.getPreferredDate());

                String formattedDate = android.text.format.DateFormat.format("dd MMM yyyy", parsedDate).toString();
                pref = formattedDate + (r.getPreferredTime() == null ? "" : "  " + r.getPreferredTime());

            } catch (Exception e) {
                e.printStackTrace();
                pref = r.getPreferredDate() + (r.getPreferredTime() == null ? "" : "  " + r.getPreferredTime());
            }
        } else if (r.getPreferredTime() != null) {
            pref = r.getPreferredTime();
        }

        h.tvPreferred.setText("Preferred: " + pref);
        h.btnDelete.setOnClickListener(v -> deleteListener.onDelete(r));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    /* ---------- view‑holder ---------- */
    static class ReqVH extends RecyclerView.ViewHolder {
        TextView tvUser, tvStatus, tvPreferred;
        View btnDelete;

        ReqVH(View v) {
            super(v);
            tvUser = v.findViewById(R.id.tvToUser);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvPreferred = v.findViewById(R.id.tvPreferred);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
