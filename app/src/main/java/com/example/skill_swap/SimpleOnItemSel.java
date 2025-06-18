package com.example.skill_swap;

import android.view.View;
import android.widget.AdapterView;

/** Tiny helper to cut boilerplate:  new SimpleOnItemSel(() -> filter()) */
public class SimpleOnItemSel implements AdapterView.OnItemSelectedListener {

    private final Runnable callback;
    public SimpleOnItemSel(Runnable cb){ this.callback = cb; }

    @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id){
        callback.run();
    }
    @Override public void onNothingSelected(AdapterView<?> p){ }
}
