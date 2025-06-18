package com.example.skill_swap;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ConnectionsAdapter extends RecyclerView.Adapter<ConnectionsAdapter.VH> {

    private final Context  ctx;
    private final List<User> list;

    public ConnectionsAdapter(Context ctx, List<User> list) {
        this.ctx  = ctx;
        this.list = list;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView   tvName;
        ImageButton btnChat;
        VH(@NonNull View v) {
            super(v);
            tvName  = v.findViewById(R.id.usernameTextView);
            btnChat = v.findViewById(R.id.chatButton);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p,int t){
        View v = LayoutInflater.from(ctx)
                .inflate(R.layout.item_connection, p, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h,int pos){
        User u = list.get(pos);
        h.tvName.setText(u.getName());
        h.btnChat.setOnClickListener(v -> {
            Intent i = new Intent(ctx, ChatActivity.class);
            i.putExtra("uid",   u.getUid());
            i.putExtra("username", u.getName());
            ctx.startActivity(i);
        });
    }

    @Override public int getItemCount(){ return list.size(); }
}
