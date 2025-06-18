package com.example.skill_swap;

import com.google.firebase.firestore.QueryDocumentSnapshot;

public class User {

    private String uid;
    private String name;
    private String skillsOffered;
    private String location;
    private float rating;

    // ✅ Newly added fields
    private String photoUrl;
    private int totalSwaps;
    private int yearsOfExperience;

    // Required empty constructor for Firebase
    public User() {}

    public User(String uid, String name) {
        this.uid  = uid;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    // --- Factory method from Firestore document ---
    public static User fromDoc(QueryDocumentSnapshot doc) {
        User u = new User();
        u.uid = doc.getId();
        u.name = doc.getString("name");

        u.skillsOffered = doc.getString("skillsOffered") == null ? "" : doc.getString("skillsOffered");
        u.location = doc.getString("location") == null ? "" : doc.getString("location");

        Double r = doc.getDouble("rating");
        u.rating = r == null ? 0f : r.floatValue();

        // ✅ Added fields
        u.photoUrl = doc.getString("photoUrl") == null ? "" : doc.getString("photoUrl");

        Long swaps = doc.getLong("totalSwaps");
        u.totalSwaps = swaps == null ? 0 : swaps.intValue();

        Long exp = doc.getLong("yearsOfExperience");
        u.yearsOfExperience = exp == null ? 0 : exp.intValue();

        return u;
    }

    // --- Getters ---
    public String getUid()               { return uid; }
    public String getName()              { return name == null ? "" : name; }
    public String getSkillsOffered()     { return skillsOffered; }
    public String getLocation()          { return location; }
    public float getRating()             { return rating; }

    // ✅ New Getters
    public String getPhotoUrl()          { return photoUrl; }
    public int getTotalSwaps()           { return totalSwaps; }
    public int getYearsOfExperience()    { return yearsOfExperience; }

    // Optional: Add setters if Firestore needs them
}
