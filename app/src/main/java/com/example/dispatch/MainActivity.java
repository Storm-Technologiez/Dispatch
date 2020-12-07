package com.example.dispatch;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.dispatch.fragments.AccountFragment;
import com.example.dispatch.fragments.EmergencyFragment;
import com.example.dispatch.fragments.ManifestFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    DatabaseReference mRef;
    String uId;
    String test;
    Fragment fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRef = FirebaseDatabase.getInstance().getReference();
        uId = "qE9xsFcEMlTXSMGLUKi1siaUZPA2";
        // CheckActiveDelivery();

        /*
            I don't know what is wrong with this bottom navigation
            Just run it and check what the problem is
            Thank you
         */

        BottomNavigationView bottomNav = (BottomNavigationView) findViewById(R.id.bottom_nav1);

        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new ManifestFragment()).commit();

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.manifest:
                    fragment = new ManifestFragment();
                    LoadFragment(fragment);
                    break;

                case R.id.emergency:
                    fragment = new EmergencyFragment();
                    LoadFragment(fragment);
                    break;

                case R.id.profile:
                    fragment = new AccountFragment();
                    LoadFragment(fragment);
                    break;
            }
            return true;
        });

//        if (test.isEmpty()){
//            Schedule_deliveries selected_delivery = snapshot.getValue(Schedule_deliveries.class);
//            Intent intent = new Intent(MainActivity.this, DeliveryInfo.class);
//            intent.putExtra("delivery", selected_delivery);
//            startActivity(intent);
//
//            Log.i("Query", "db is empty");
//        }

    }

    private void LoadFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment, fragment);
        transaction.commit();
    }

    public void CheckActiveDelivery() {
        mRef.child("delivery_in_progress").child(uId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                test = String.valueOf(snapshot.child("delivery_id").getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}