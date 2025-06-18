package com.example.skill_swap;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;


import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity {

    private EditText nameEt, emailEt, pwEt, confirmPwEt,
            skillsOfferedEt, skillsWantedEt, locationEt, phoneEt;

    private Button   registerBtn;

    private FirebaseAuth     mAuth;
    private FirebaseFirestore db;
    private AlertDialog       loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        /* ---------- find views ---------- */
        nameEt          = findViewById(R.id.nameEditText);
        emailEt         = findViewById(R.id.emailEditText);
        pwEt            = findViewById(R.id.passwordEditText);
        confirmPwEt     = findViewById(R.id.confirmPasswordEditText);
        skillsOfferedEt = findViewById(R.id.skillsOfferedEditText);
        skillsWantedEt  = findViewById(R.id.skillsWantedEditText);
        locationEt      = findViewById(R.id.locationEditText);
        phoneEt         = findViewById(R.id.phoneEditText);   // add this EditText in XML if desired
        registerBtn     = findViewById(R.id.registerButton);

        registerBtn.setOnClickListener(v -> registerUser());
        LinearLayout goToLoginContainer = findViewById(R.id.goToLoginContainer);
        goToLoginContainer.setOnClickListener(v -> {
            startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            finish();
        });

    }

    /* ---------- Main register method ---------- */
    private void registerUser() {

        /* -- collect input -- */
        String name          = nameEt.getText().toString().trim();
        String email         = emailEt.getText().toString().trim();
        String pw            = pwEt.getText().toString().trim();
        String confirmPw     = confirmPwEt.getText().toString().trim();
        String skillsOffer   = skillsOfferedEt.getText().toString().trim();
        String skillsWant    = skillsWantedEt.getText().toString().trim();
        String location      = locationEt.getText().toString().trim();

        String phone         = phoneEt != null ? phoneEt.getText().toString().trim() : "";

        /* -- validate -- */
        if (TextUtils.isEmpty(name))         { nameEt.setError("Name required"); return; }
        if (TextUtils.isEmpty(email))        { emailEt.setError("Email required"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEt.setError("Invalid email"); return;
        }
        if (TextUtils.isEmpty(pw) || pw.length() < 6) {
            pwEt.setError("Password ≥ 6 chars"); return;
        }
        if (!pw.equals(confirmPw)) {
            confirmPwEt.setError("Passwords do not match"); return;
        }

        showLoadingDialog("Creating account…");

        /* -- Firebase Auth sign‑up -- */
        mAuth.createUserWithEmailAndPassword(email, pw)
                .addOnCompleteListener(task -> {
                    dismissLoadingDialog();
                    if (task.isSuccessful()) {
                        FirebaseUser fUser = mAuth.getCurrentUser();
                        if (fUser != null) {
                            /* 1️⃣  build profile map */
                            Map<String,Object> profile = new HashMap<>();
                            profile.put("name",  name);
                            profile.put("email", email);
                            profile.put("phone", phone);
                            profile.put("skillsOffered", skillsOffer);
                            profile.put("skillsWanted", skillsWant);
                            profile.put("location", location);
                            profile.put("rating", 0);   // placeholder

                            /* 2️⃣  save in Firestore */
                            showLoadingDialog("Saving profile…");
                            db.collection("users").document(fUser.getUid())
                                    .set(profile, SetOptions.merge())   // merge for safety
                                    .addOnSuccessListener(v -> {
                                        dismissLoadingDialog();
                                        sendEmailVerification(fUser);
                                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, LoginActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        dismissLoadingDialog();
                                        Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        }
                    } else {
                        String msg = Objects.requireNonNull(task.getException()).getMessage();
                        Toast.makeText(this, "Auth failed: " + msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /* ---------- send email verification ---------- */
    private void sendEmailVerification(@NonNull FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) {
                        Toast.makeText(this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    /* ---------- simple loading dialog helpers ---------- */
    private void showLoadingDialog(String msg) {
        if (loadingDialog == null) {
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            b.setView(inflater.inflate(R.layout.dialog_loading, null));
            b.setCancelable(false);
            loadingDialog = b.create();
        }
        loadingDialog.setMessage(msg);
        loadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();
    }
}
