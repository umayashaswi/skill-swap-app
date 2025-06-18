package com.example.skill_swap;

import android.os.Bundle;

public class InboxActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        // Optional: If your layout has a toolbar
        // setupToolbar(R.id.toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new InboxFragment())
                    .commit();
        }
    }
}
