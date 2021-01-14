package com.example.dispatch;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dispatch.adapters.FirestoreAdapter;
import com.example.dispatch.constructors.DeliveryRun;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class WayBills extends AppCompatActivity {

    FirebaseFirestore db;
    CollectionReference waybillRef;
    RecyclerView recyclerView;
    FirestoreAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_way_bills);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        recyclerView = findViewById(R.id.rv_waybill);
        db = FirebaseFirestore.getInstance();
        waybillRef = db.collection(getString(R.string.collection_completed_delivery)).document(userId).collection("deliveries");

        LoadData();
    }

    private void LoadData() {
        Query query = waybillRef;

        FirestoreRecyclerOptions<DeliveryRun> options = new FirestoreRecyclerOptions.Builder<DeliveryRun>()
                .setQuery(query, DeliveryRun.class)
                .build();

        adapter = new FirestoreAdapter(options);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter.setOnItemClickListener((documentSnapshot, position) -> {
            DeliveryRun run = documentSnapshot.toObject(DeliveryRun.class);
            Intent intent = new Intent(WayBills.this, WaybillDetails.class);
            intent.putExtra("waybill", run);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}