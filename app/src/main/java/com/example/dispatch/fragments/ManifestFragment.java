package com.example.dispatch.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dispatch.DeliveryInfoMapsActivity;
import com.example.dispatch.MainActivity;
import com.example.dispatch.R;
import com.example.dispatch.adapters.DeliveryAdapter;
import com.example.dispatch.constructors.DeliveryRun;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.MODE_PRIVATE;

public class ManifestFragment extends Fragment {
    final String MY_PREFS_NAME = "switchCheckedState";
    View view;
    DatabaseReference mRef;
    SwitchCompat aSwitch;
    RecyclerView rvDelivery;
    TextView dailyEarning, dailyStat;
    String uId, test;
    ProgressDialog progressBar;
    MainActivity mainActivity;

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
        uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mRef = FirebaseDatabase.getInstance().getReference();
        progressBar = new ProgressDialog(view.getContext());
        mainActivity = new MainActivity();

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
        //  CheckActiveDelivery();
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

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(view.getContext(), RecyclerView.VERTICAL, false);
        rvDelivery.setLayoutManager(layoutManager);
    }

    public void CheckActiveDelivery() {
        mRef.child(getString(R.string.node_delivery)).child(uId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DeliveryRun selected_delivery = snapshot.getValue(DeliveryRun.class);
                if (selected_delivery != null) {
                    test = selected_delivery.getDelivery_id();
                    if (!test.isEmpty()) {
                        Loading();
                        aSwitch.setChecked(true);
                        Intent intent = new Intent(view.getContext(), DeliveryInfoMapsActivity.class);
                        intent.putExtra("delivery", selected_delivery);
                        startActivity(intent);
                        progressBar.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void Loading() {
        progressBar.setTitle("Please Wait...");
        progressBar.show();
        progressBar.setCanceledOnTouchOutside(false);
        progressBar.setCancelable(false);
    }
}