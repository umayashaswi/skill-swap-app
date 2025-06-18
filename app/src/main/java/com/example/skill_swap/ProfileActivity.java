package com.example.skill_swap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Shows a public profile.  If an explicit UID is passed via Intent extras it
 * shows that user; otherwise it shows the currently-signed-in user.
 */
public class ProfileActivity extends BaseActivity {

    /* ---------- UI ---------- */
    private TextView  tvName, tvBio, tvSkillsOffered, tvSkillsWanted;
    private RatingBar ratingBar;
    private Button    btnEdit;
    private ImageView profileImage;

    /* ---------- Firebase ---------- */
    private final FirebaseAuth      mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db    = FirebaseFirestore.getInstance();

    /* ============================================================ */
    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setTitle("My Profile");

        bindViews();

        /* Which user to show? */
        String targetUid = getIntent().getStringExtra("uid");
        if (targetUid == null) {
            FirebaseUser me = mAuth.getCurrentUser();
            if (me != null) targetUid = me.getUid();
        }
        if (targetUid == null) {
            Toast.makeText(this, "No user to show", Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        loadProfile(targetUid);

        /* Only you can edit your own profile */
        boolean isOwnProfile = targetUid.equals(mAuth.getCurrentUser().getUid());
        btnEdit.setEnabled(isOwnProfile);
        btnEdit.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));
    }

    /* ---------------- view bindings ---------------- */
    private void bindViews() {
        tvName          = findViewById(R.id.tvName);
        tvBio           = findViewById(R.id.tvBio);
        tvSkillsOffered = findViewById(R.id.tvSkillsOffered);
        tvSkillsWanted  = findViewById(R.id.tvSkillsWanted);
        ratingBar       = findViewById(R.id.ratingBar);
        btnEdit         = findViewById(R.id.btnEditProfile);
        profileImage    = findViewById(R.id.profileImage);
    }

    /* ---------------- static launcher ---------------- */
    public static void start(Context ctx, String uid) {
        Intent i = new Intent(ctx, ProfileActivity.class);
        i.putExtra("uid", uid);
        ctx.startActivity(i);
    }

    /* ---------------- read from Firestore ------------ */
    private void loadProfile(String uid) {
        db.collection("users").document(uid)
                .addSnapshotListener((doc, err) -> {
                    if (err != null || doc == null || !doc.exists()) {
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    tvName .setText(doc.getString("name"));
                    tvBio  .setText(doc.getString("interests"));

                    tvSkillsOffered.setText(
                            getString(R.string.skills_fmt, doc.getString("skillsOffered")));
                    tvSkillsWanted .setText(
                            getString(R.string.skills_fmt, doc.getString("skillsWanted")));

                    Double rating = doc.getDouble("rating");
                    ratingBar.setRating(rating == null ? 0f : rating.floatValue());

                    /* ---------- profile picture ---------- */
                    String imgValue = doc.getString("profileImage");   // same key used in EditProfile
                    if (TextUtils.isEmpty(imgValue)) {
                        profileImage.setImageResource(R.drawable.ic_default_avatar);
                        return;
                    }

                    if (imgValue.startsWith("http")) {       // old flow: stored download URL
                        Glide.with(this)
                                .load(imgValue)
                                .placeholder(R.drawable.ic_default_avatar)
                                .into(profileImage);
                    } else {                                 // new flow: bundled drawable name
                        int resId = getResources()
                                .getIdentifier(imgValue, "drawable", getPackageName());
                        profileImage.setImageResource(
                                resId == 0 ? R.drawable.ic_default_avatar : resId);
                    }
                });
    }
}
