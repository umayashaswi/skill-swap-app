package com.example.skill_swap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;          //  ★ new (for Arrays.asList)
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private final List<User> users;
    private final Context    ctx;
    private final FirebaseFirestore db   = FirebaseFirestore.getInstance();
    private final FirebaseAuth      auth = FirebaseAuth.getInstance();

    public UsersAdapter(List<User> users, Context context){
        this.users = users;
        this.ctx   = context;
    }

    /* ------------ basic adapter stuff ------------ */
    @NonNull
    @Override public UserViewHolder onCreateViewHolder(@NonNull ViewGroup p,int vt){
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_user,p,false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder h,int pos){
        User u = users.get(pos);

        h.tvName    .setText(u.getName());
        h.tvSkills  .setText("Skills: "    + u.getSkillsOffered());
        h.tvLocation.setText("Location: "  + u.getLocation());

        /* let the helper decide the correct label / listener */
        refreshButton(h.btnConnect, u.getUid());     // ★ changed helper name
    }

    @Override public int getItemCount(){ return users.size(); }

    /* ---------- helper that sets up ONE button ---------- */
    private void refreshButton(Button btn, String otherUid){          // ★ rewritten
        final String me = auth.getUid();

        /* cannot connect to myself */
        if (me.equals(otherUid)){
            btn.setText("Connect");
            btn.setEnabled(false);
            return;
        }

        btn.setEnabled(false);
        btn.setText("Checking…");

        /* look ONLY at *my* outgoing doc (me → other) */
        db.collection("connection_requests")
                .whereEqualTo("fromUid", me)
                .whereEqualTo("toUid",   otherUid)
                .limit(1)
                .get()
                .addOnSuccessListener(q -> {
                    String status = q.isEmpty() ? null
                            : q.getDocuments().get(0).getString("status");

                    if ("Accepted".equals(status)){
                        btn.setText("Connected");
                        // disabled

                    } else if ("Pending".equals(status)){
                        btn.setText("Request Sent");
                        // disabled

                    } else {                       // no doc yet, or Rejected/Cancelled
                        btn.setText("Connect");
                        btn.setEnabled(true);
                        btn.setOnClickListener(v ->
                                sendConnectionRequest(otherUid, btn));
                    }
                })
                .addOnFailureListener(e -> {
                    btn.setText("Connect");
                    btn.setEnabled(true);
                    Toast.makeText(ctx,"Load failed: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                });
    }

    /* ---------- create a new outgoing request ---------- */
    private void sendConnectionRequest(String toUid, Button btn){
        final String fromUid = auth.getUid();

        if (fromUid.equals(toUid)){
            Toast.makeText(ctx,"Cannot connect with yourself",Toast.LENGTH_SHORT).show();
            return;
        }

        btn.setEnabled(false);
        btn.setText("Sending…");

        /* only send if I don’t already have Pending/Accepted */
        db.collection("connection_requests")
                .whereEqualTo("fromUid", fromUid)
                .whereEqualTo("toUid",   toUid)
                .whereIn("status", Arrays.asList("Pending","Accepted"))
                .limit(1)
                .get()
                .addOnSuccessListener(q -> {
                    if (!q.isEmpty()){               // something already exists
                        btn.setText("Request Sent");
                        return;
                    }

                    ConnectionRequest cr =
                            new ConnectionRequest(fromUid, toUid, "Pending");

                    db.collection("connection_requests")
                            .add(cr.toMap())
                            .addOnSuccessListener(d -> {
                                btn.setText("Request Sent");
                            })
                            .addOnFailureListener(e -> {
                                btn.setText("Connect");
                                btn.setEnabled(true);
                                Toast.makeText(ctx,"Send failed: "+e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btn.setText("Connect");
                    btn.setEnabled(true);
                    Toast.makeText(ctx,"Check failed: "+e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    /* ---------- View-holder ---------- */
    static class UserViewHolder extends RecyclerView.ViewHolder{
        TextView tvName, tvSkills, tvLocation;
        Button   btnConnect;

        UserViewHolder(View v){
            super(v);
            tvName     = v.findViewById(R.id.tvUserName);
            tvSkills   = v.findViewById(R.id.tvSkills);
            tvLocation = v.findViewById(R.id.tvLocation);
            btnConnect = v.findViewById(R.id.btnConnect);
        }
    }
}
