package com.example.skill_swap;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.skill_swap.ui.FeedActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;
    protected ActionBarDrawerToggle toggle;

    protected FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        // Inflate the base layout which contains DrawerLayout and Toolbar
        DrawerLayout fullLayout = (DrawerLayout) LayoutInflater.from(this).inflate(R.layout.activity_base, null);

        // Find FrameLayout in the base layout where child layout will be inserted
        FrameLayout contentFrame = fullLayout.findViewById(R.id.content_frame);

        // Inflate the child layout into the FrameLayout
        LayoutInflater.from(this).inflate(layoutResID, contentFrame, true);

        // Set the whole layout as content view
        super.setContentView(fullLayout);

        // Initialize drawer, navigation view and toolbar
        drawerLayout = fullLayout.findViewById(R.id.drawerLayout);
        navigationView = fullLayout.findViewById(R.id.navigationView);
        toolbar = fullLayout.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup drawer toggle button
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        setupNavHeader();
        setupNavMenuActions();
    }

    private void setupNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.navUsername);
        ImageView navProfileImage = headerView.findViewById(R.id.navProfileImage);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        String name = doc.getString("name");
                        String avatar = doc.getString("profileImage");

                        navUsername.setText(name != null ? name : "User");

                        if (avatar != null) {
                            int resId = getResources().getIdentifier(avatar, "drawable", getPackageName());
                            if (resId != 0) {
                                navProfileImage.setImageResource(resId);
                            } else {
                                navProfileImage.setImageResource(R.drawable.ic_profile); // fallback image
                            }
                        } else {
                            navProfileImage.setImageResource(R.drawable.ic_profile); // fallback image
                        }
                    });
        }
    }

    private void setupNavMenuActions() {
        navigationView.setNavigationItemSelectedListener(item -> {
            // Close drawer immediately
            drawerLayout.closeDrawer(GravityCompat.START);

            // Handle after drawer closes smoothly with delay
            drawerLayout.postDelayed(() -> {
                int id = item.getItemId();

                if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                } else if (id == R.id.nav_edit_profile) {
                    startActivity(new Intent(this, EditProfileActivity.class));
                } else if (id == R.id.nav_sent_requests) {
                    startActivity(new Intent(this, SentRequestsActivity.class));
                } else if (id == R.id.nav_invites) {
                    startActivity(new Intent(this, InvitesActivity.class));
                } else if (id == R.id.nav_logout) {
                    mAuth.signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                } else if (id == R.id.home) {
                    startActivity(new Intent(this, MainActivity.class));
                } else if (id == R.id.nav_discussions) {
                    startActivity(new Intent(this, FeedActivity.class));
                }
                else if(id == R.id.nav_inbox){
                    startActivity(new Intent(this, InboxActivity.class));
                }

            }, 250); // 250ms delay for drawer animation

            return true;
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
