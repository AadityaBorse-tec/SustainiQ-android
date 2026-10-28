package com.sustainiq.app.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sustainiq.app.R;
import com.sustainiq.app.util.SessionManager;

import java.util.Calendar;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager session = new SessionManager(requireContext());

        // Dynamic greeting based on hour
        TextView tvGreeting = view.findViewById(R.id.tv_greeting);
        TextView tvName     = view.findViewById(R.id.tv_user_name);

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12) {
            greeting = getString(R.string.good_morning);
        } else if (hour < 17) {
            greeting = getString(R.string.good_afternoon);
        } else {
            greeting = getString(R.string.good_evening);
        }

        tvGreeting.setText(greeting);
        tvName.setText(session.getUserName());

        // Category click listeners (stub — Stage 2 will navigate)
        view.findViewById(R.id.cat_waste).setOnClickListener(v ->
                showToast("Waste reports — coming in Stage 2"));
        view.findViewById(R.id.cat_water).setOnClickListener(v ->
                showToast("Water reports — coming in Stage 2"));
        view.findViewById(R.id.cat_air).setOnClickListener(v ->
                showToast("Air reports — coming in Stage 2"));
        view.findViewById(R.id.cat_green_spaces).setOnClickListener(v ->
                showToast("Green spaces reports — coming in Stage 2"));

        // Report CTA
        view.findViewById(R.id.card_report_cta).setOnClickListener(v ->
                showToast("Report an Issue — coming in Stage 2"));

        // Recent report card
        view.findViewById(R.id.card_recent_report).setOnClickListener(v ->
                showToast("Report detail — coming in Stage 2"));
    }

    private void showToast(String msg) {
        android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_SHORT).show();
    }
}
