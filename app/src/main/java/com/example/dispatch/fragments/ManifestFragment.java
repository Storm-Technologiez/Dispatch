package com.example.dispatch.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dispatch.R;
import com.example.dispatch.adapters.DeliveryAdapter;
import com.google.android.material.snackbar.Snackbar;

import static android.content.Context.MODE_PRIVATE;

public class ManifestFragment extends Fragment {
    final String MY_PREFS_NAME = "switchCheckedState";
    View view;
    SwitchCompat aSwitch;
    RecyclerView rvDelivery;
    TextView dailyEarning, dailyStat;

    public ManifestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_manifest, container, false);

        rvDelivery = view.findViewById(R.id.rv_schedules);
        aSwitch = view.findViewById(R.id.switch1);
        dailyEarning = view.findViewById(R.id.tv_dailyEarn_number);
        dailyStat = view.findViewById(R.id.tv_compStats_number);

        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                riderOnline();
                Snackbar.make(view.getRootView(), "Your are Active and working.", Snackbar.LENGTH_SHORT).show();
            } else {
                aSwitch.setChecked(false);
                riderOffline();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isRiderActive()) {
            aSwitch.setChecked(true);
        }
    }

    private void riderOnline() {
        aSwitch.setText("Active");
        rvDelivery.setVisibility(View.VISIBLE);
        LoadFeeds();
        saveCheckedState(true);
    }

    private void riderOffline() {
        aSwitch.setText("Offline");
        saveCheckedState(false);
        rvDelivery.setVisibility(View.INVISIBLE);
        Snackbar.make(view.getRootView(), "Your are currently off-duty.", Snackbar.LENGTH_SHORT).show();
    }

    private void saveCheckedState(boolean b) {
        SharedPreferences.Editor editor = view.getContext().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean("state", b);
        editor.apply();
    }

    private boolean isRiderActive() {
        SharedPreferences prefs = view.getContext().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        Boolean isActive = prefs.getBoolean("state", false);
        return isActive;
    }

    private void LoadFeeds() {
        final DeliveryAdapter adapter = new DeliveryAdapter();
        rvDelivery.setAdapter(adapter);
        rvDelivery.setHasFixedSize(true);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(view.getContext(), RecyclerView.VERTICAL, false);
        rvDelivery.setLayoutManager(layoutManager);
    }
}