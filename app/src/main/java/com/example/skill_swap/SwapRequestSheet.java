package com.example.skill_swap;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.skill_swap.databinding.FragmentSwapRequestBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SwapRequestSheet extends BottomSheetDialogFragment {

    private FragmentSwapRequestBinding b;
    private final OnReadyListener cb;

    /* ------------------------------------------------------------------ */
    public interface OnReadyListener {
        void onFormReady(SwapFormPayload payload);
    }

    public SwapRequestSheet(OnReadyListener cb) { this.cb = cb; }

    /* ------------------------------------------------------------------ */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        b = FragmentSwapRequestBinding.inflate(inflater, container, false);

        /* ── populate all spinners from arrays.xml ─────────────────── */
        setSpinnerAdapter(R.array.skill_levels,      b.spSkillLevel);
        setSpinnerAdapter(R.array.skill_levels,      b.spExpectedLevel);
        setSpinnerAdapter(R.array.session_durations, b.spDuration);
        setSpinnerAdapter(R.array.comm_modes,        b.spCommMode);

        b.btnSubmit.setOnClickListener(v -> collectAndSend());
        return b.getRoot();
    }

    /* ------------------------------------------------------------------ */
    /** Utility: bind a <string-array> to a Spinner. */
    private void setSpinnerAdapter(int arrayResId, Spinner spinner) {
        ArrayAdapter<CharSequence> a = ArrayAdapter.createFromResource(
                requireContext(), arrayResId, android.R.layout.simple_spinner_item);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(a);
    }

    /* ------------------------------------------------------------------ */
    private void collectAndSend() {

        if (!b.chkGuidelines.isChecked()) {
            Toast.makeText(getContext(),
                    "Please accept the guidelines.", Toast.LENGTH_SHORT).show();
            return;
        }

        String yearsExpStr   = b.etYearsExp.getText().toString().trim();
        String numSessionsStr= b.etNumSessions.getText().toString().trim();

        if (TextUtils.isEmpty(yearsExpStr) || TextUtils.isEmpty(numSessionsStr)) {
            Toast.makeText(getContext(),
                    "Experience and sessions fields must be filled.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int yearsExp, numSessions;
        try {
            yearsExp    = Integer.parseInt(yearsExpStr);
            numSessions = Integer.parseInt(numSessionsStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(),
                    "Invalid number format.", Toast.LENGTH_SHORT).show();
            return;
        }

        /* format date & time */
        String formattedDate  = String.format("%04d-%02d-%02d",
                b.dpDate.getYear(),
                b.dpDate.getMonth() + 1,
                b.dpDate.getDayOfMonth());

        String formattedTime  = String.format("%02d:%02d",
                b.tpTime.getHour(), b.tpTime.getMinute());

        /* build payload */
        SwapFormPayload p = new SwapFormPayload(
                b.etFullName     .getText().toString().trim(),
                b.etBio          .getText().toString().trim(),
                b.etSkillOffer   .getText().toString().trim(),
                b.spSkillLevel   .getSelectedItem().toString(),
                yearsExp,
                b.etExpDesc      .getText().toString().trim(),
                b.etPortfolio    .getText().toString().trim(),
                "",                       // sampleFileUrl placeholder
                b.etSkillWant    .getText().toString().trim(),
                b.spExpectedLevel.getSelectedItem().toString(),
                b.etWhyLearn     .getText().toString().trim(),
                b.spDuration     .getSelectedItem().toString(),
                numSessions,
                formattedDate,
                formattedTime,
                b.spCommMode     .getSelectedItem().toString(),
                b.etCommOther    .getText().toString().trim(),
                b.etMessage      .getText().toString().trim()
        );

        cb.onFormReady(p);   // send it back to SearchSkillsActivity
        dismiss();
    }
}
