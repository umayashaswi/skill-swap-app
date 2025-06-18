package com.example.skill_swap;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Lets the user edit profile data and pick one of 12 bundled avatar icons
 * (ic_1 … ic_12 in res/drawable).  Nothing is uploaded to Firebase Storage;
 * we just save the chosen drawable’s *resource name* (e.g. “ic_4”)
 * in the user document field <b>profileImage</b>.
 */
public class EditProfileActivity extends BaseActivity {

    /* ---------- UI ---------- */
    private ImageView profileImage;
    private EditText  editName, editPhone, editSkills, editArea, editInterests;
    private Spinner   categorySpinner;
    private Button    btnPickAvatar, saveBtn;

    /* ---------- Firebase ---------- */
    private final FirebaseAuth      mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db    = FirebaseFirestore.getInstance();

    /* ---------- bundled avatars ---------- */
    private static final int[] AVATAR_IDS = {
            R.drawable.avatar_1, R.drawable.avatar_2,R.drawable.avatar_3,
            R.drawable.avatar_4,R.drawable.avatar_5,R.drawable.avatar_6,
            R.drawable.avatar_7,R.drawable.avatar_8,R.drawable.avatar_9,
            R.drawable.avatar_10,R.drawable.avatar_11,R.drawable.avatar_12,
            R.drawable.avatar_13, R.drawable.avatar_14, R.drawable.avatar_15,
            R.drawable.avatar_16,
    };


    /** drawable *name* we’ll store in Firestore (e.g. “ic_7”) */
    private String selectedAvatarResName = "ic_profile_placeholder";

    /* =========================================================== */
    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Edit Profile");

        bindViews();
        setupSpinner();
        btnPickAvatar.setOnClickListener(v -> showAvatarPicker());
        saveBtn.setOnClickListener(v   -> saveProfile());

        loadProfile();            // fetch current info & avatar
    }

    /* ---------------- view bindings ---------------- */
    private void bindViews() {
        profileImage    = findViewById(R.id.profileImage);
        btnPickAvatar   = findViewById(R.id.btnPickAvatar);
        saveBtn         = findViewById(R.id.saveButton);

        editName        = findViewById(R.id.editName);
        editPhone       = findViewById(R.id.editPhone);
        editSkills      = findViewById(R.id.editSkills);
        editArea        = findViewById(R.id.editArea);
        editInterests   = findViewById(R.id.editInterests);
        categorySpinner = findViewById(R.id.spinnerCountry);
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adp = ArrayAdapter.createFromResource(
                this, R.array.category_array, android.R.layout.simple_spinner_item);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adp);
    }

    /* ======================  AVATAR PICKER  ==================== */
    private void showAvatarPicker() {
        View sheet = LayoutInflater.from(this)
                .inflate(R.layout.dialog_avatar_picker, null, false);
        RecyclerView rv = sheet.findViewById(R.id.rvAvatars);
        rv.setLayoutManager(new GridLayoutManager(this, 3));

        AlertDialog dlg = new AlertDialog.Builder(this)
                .setView(sheet)
                .create();

        rv.setAdapter(new AvatarAdapter(dlg));
        dlg.show();
    }

    /** adapter for the 12 bundled icons */
    private class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.Holder> {
        private final AlertDialog dialog;
        AvatarAdapter(AlertDialog dlg) { this.dialog = dlg; }

        class Holder extends RecyclerView.ViewHolder {
            final ImageView iv;
            Holder(View v) { super(v); iv = v.findViewById(R.id.ivAvatar); }
        }

        @NonNull @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int vt) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_avatar, parent, false);
            return new Holder(v);
        }

        @Override public int getItemCount() { return AVATAR_IDS.length; }

        @Override public void onBindViewHolder(@NonNull Holder h, int pos) {
            int resId = AVATAR_IDS[pos];
            h.iv.setImageResource(resId);

            h.iv.setOnClickListener(v -> {
                selectedAvatarResName =
                        getResources().getResourceEntryName(resId);   // “ic_4”…..
                profileImage.setImageResource(resId);
                dialog.dismiss();
            });
        }
    }

    /* ======================  FIRESTORE  ======================== */
    private void loadProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    editName      .setText(doc.getString("name"));
                    editPhone     .setText(doc.getString("phone"));
                    editSkills    .setText(doc.getString("skillsOffered"));
                    editArea      .setText(doc.getString("area"));
                    editInterests .setText(doc.getString("interests"));

                    String avatarName = doc.getString("profileImage");
                    if (!TextUtils.isEmpty(avatarName))
                        selectedAvatarResName = avatarName;

                    int resId = getResources()
                            .getIdentifier(selectedAvatarResName, "drawable", getPackageName());
                    profileImage.setImageResource(
                            resId == 0 ? R.drawable.ic_profile_placeholder : resId);
                });
    }

    private void saveProfile() {
        String name = editName.getText().toString().trim();
        if (name.isEmpty()) { editName.setError("Name required"); return; }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        Map<String,Object> map = new HashMap<>();
        map.put("name"         , name);
        map.put("phone"        , editPhone    .getText().toString().trim());
        map.put("skillsOffered", editSkills   .getText().toString().trim());
        map.put("area"         , editArea     .getText().toString().trim());
        map.put("interests"    , editInterests.getText().toString().trim());
        map.put("category"     , categorySpinner.getSelectedItem().toString());
        map.put("profileImage" , selectedAvatarResName);            // 🔑

        db.collection("users").document(user.getUid())
                .update(map)
                .addOnSuccessListener(v ->
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: "+e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}
