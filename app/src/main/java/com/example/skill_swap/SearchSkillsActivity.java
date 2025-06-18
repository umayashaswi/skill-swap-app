package com.example.skill_swap;

import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Browse & filter users by skill, location, rating */
public class SearchSkillsActivity extends BaseActivity {

    private SearchView searchView;
    private Spinner spLocation, spRating;
    private RecyclerView recycler;
    private SkillUserAdapter adapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<User>  masterList   = new ArrayList<>();
    private final List<User>  filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_skills);
        setTitle("Search Skills");

        searchView  = findViewById(R.id.searchView);
        spLocation  = findViewById(R.id.spinnerLocation);
        spRating    = findViewById(R.id.spinnerRating);
        recycler    = findViewById(R.id.recyclerViewUsers);

        adapter = new SkillUserAdapter(
                filteredList,
                user -> ProfileActivity.start(this, user.getUid()),   // open profile
                this::showSwapForm                                      // open form
        );

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        setupRatingSpinner();
        fetchUsersFromFirestore();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public boolean onQueryTextSubmit(String q) { filter(); return true; }
            public boolean onQueryTextChange(String q){ filter(); return true; }
        });
        spLocation.setOnItemSelectedListener(new SimpleOnItemSel(this::filter));
        spRating  .setOnItemSelectedListener(new SimpleOnItemSel(this::filter));
    }

    /* ---------------- firestore ---------------- */
    private void fetchUsersFromFirestore() {
        String myUid = mAuth.getCurrentUser().getUid();

        db.collection("users").get().addOnSuccessListener(snap -> {
            masterList.clear();
            Set<String> locations = new HashSet<>();

            for (QueryDocumentSnapshot doc : snap) {
                if (doc.getId().equals(myUid)) continue;      // skip myself
                User u = User.fromDoc(doc);
                masterList.add(u);
                if (!u.getLocation().isEmpty()) locations.add(u.getLocation());
            }
            populateLocationSpinner(locations);
            filter();
        }).addOnFailureListener(e ->
                Toast.makeText(this,"Failed: "+e.getMessage(),Toast.LENGTH_LONG).show());
    }

    /* ---------------- spinners ---------------- */
    private void setupRatingSpinner() {
        ArrayAdapter<String> a = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"All","1+","2+","3+","4+","4.5+"});
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRating.setAdapter(a);
    }
    private void populateLocationSpinner(Set<String> loc) {
        List<String> items = new ArrayList<>();
        items.add("All"); items.addAll(loc);
        ArrayAdapter<String> a = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLocation.setAdapter(a);
    }

    /* ---------------- filtering ---------------- */
    private void filter() {
        if (masterList.isEmpty()) return;

        String q = searchView.getQuery()==null ? "" :
                searchView.getQuery().toString().trim().toLowerCase();

        String locSel = spLocation.getSelectedItem()==null ? "All"
                : spLocation.getSelectedItem().toString();
        String ratSel = spRating.getSelectedItem()==null ? "All"
                : spRating.getSelectedItem().toString();

        float minRating = ratSel.equals("All") ? 0f
                : Float.parseFloat(ratSel.replace("+",""));

        filteredList.clear();
        for (User u : masterList) {
            boolean okSkill = u.getSkillsOffered().toLowerCase().contains(q);
            boolean okLoc   = locSel.equals("All") || u.getLocation().equals(locSel);
            boolean okRate  = u.getRating() >= minRating;
            if (okSkill && okLoc && okRate) filteredList.add(u);
        }
        adapter.notifyDataSetChanged();
    }

    /* ---------------- form & firestore write ---------------- */
    private void showSwapForm(User target) {
        new SwapRequestSheet(payload -> sendRequest(target, payload))
                .show(getSupportFragmentManager(), "swap_form");
    }

    private void sendRequest(User target, SwapFormPayload p) {
        String myUid = mAuth.getCurrentUser().getUid();

        Map<String,Object> m = new HashMap<>();
        m.put("fromUid",        myUid);
        m.put("toUid",          target.getUid());
        m.put("status",         "pending");
        m.put("sentAt",         FieldValue.serverTimestamp());

        /* ---- attach everything the sender filled in ---- */
        m.put("fullName",       p.fullName);
        m.put("bio",            p.bio);
        m.put("offeredSkill",   p.offeredSkill);
        m.put("offeredLevel",   p.offeredLevel);
        m.put("yearsExp",       p.yearsExp);
        m.put("expDesc",        p.expDesc);
        m.put("portfolioUrl",   p.portfolioUrl);
        m.put("sampleFileUrl",  p.sampleFileUrl);
        m.put("seekSkill",      p.seekSkill);
        m.put("seekLevel",      p.seekLevel);
        m.put("seekWhy",        p.seekWhy);
        m.put("sessionDuration",p.sessionDuration);
        m.put("sessionCount",   p.sessionCount);
        m.put("preferredDate",  p.proposedDate);
        m.put("preferredTime",  p.timeSlot);
        m.put("commMode",       p.commMode);
        m.put("commModeOther",  p.commModeOther);
        m.put("personalMsg",    p.personalMsg);

        db.collection("requests").add(m)
                .addOnSuccessListener(r ->
                        Toast.makeText(this,"Request sent!",Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this,"Error: "+e.getMessage(),Toast.LENGTH_LONG).show());
    }
}
