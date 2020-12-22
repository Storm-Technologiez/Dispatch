package com.example.dispatch.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dispatch.R;
import com.example.dispatch.adapters.DeliveryAdapter;

public class ManifestFragment extends Fragment {
    View view;

    public ManifestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_manifest, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LoadFeeds();
    }

    private void LoadFeeds() {
        RecyclerView rvDelivery = view.findViewById(R.id.rv_schedules);
        final DeliveryAdapter adapter = new DeliveryAdapter();
        rvDelivery.setAdapter(adapter);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(view.getContext(), RecyclerView.VERTICAL, false);
        rvDelivery.setLayoutManager(layoutManager);
    }
}