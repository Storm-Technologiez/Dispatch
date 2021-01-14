package com.example.dispatch;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.dispatch.fragments.AccountFragment;
import com.example.dispatch.fragments.EmergencyFragment;
import com.example.dispatch.fragments.ManifestFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    final String MY_PREFS_NAME = "switchCheckedState";
    DatabaseReference mRef;
    String uId;
    String test;
    Fragment fragment = null;
    RelativeLayout Toolbar;
    TextView HeaderText;
    ImageView moreOption;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // CheckActiveDelivery();
        setContentView(R.layout.activity_main);

        CheckGps();

        mRef = FirebaseDatabase.getInstance().getReference();
        uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        BottomNavigationView bottomNav = (BottomNavigationView) findViewById(R.id.bottom_nav1);
        Toolbar = findViewById(R.id.toolBar_header);
        HeaderText = findViewById(R.id.header_text);
        moreOption = findViewById(R.id.option);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.purple_700));

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new ManifestFragment()).commit();

        moreOption.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MainActivity.this, moreOption);
            popup.getMenuInflater().inflate(R.menu.option_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.logout:

                        // FirebaseAuth.getInstance().signOut();
                        break;
                }
                return true;
            });
            popup.show();
        });

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.manifest:
                    Toolbar.setVisibility(View.GONE);
                    fragment = new ManifestFragment();
                    LoadFragment(fragment);
                    break;

                case R.id.emergency:
                    Toolbar.setVisibility(View.VISIBLE);
                    HeaderText.setText("emergency");
                    moreOption.setVisibility(View.INVISIBLE);
                    fragment = new EmergencyFragment();
                    LoadFragment(fragment);
                    break;

                case R.id.profile:
                    Toolbar.setVisibility(View.VISIBLE);
                    HeaderText.setText("account");
                    moreOption.setVisibility(View.VISIBLE);
                    fragment = new AccountFragment();
                    LoadFragment(fragment);
                    break;
            }
            return true;
        });
    }

    private void CheckGps() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gps) {
            showSettingsAlert();
        }
    }

    private void showSettingsAlert() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("Turn on GPS");
        alert.setMessage("GPS is required for app's location services to work. \nGo to settings menu and enable GPS.");

        alert.setPositiveButton("Settings", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            MainActivity.this.startActivity(intent);
        });

        alert.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            finish();
        });
        alert.setCancelable(false);
        alert.show();
    }

    private void LoadFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment, fragment);
        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean("state", false);
        editor.apply();
    }

    public void GotoHistory(View view) {
        startActivity(new Intent(getApplicationContext(), WayBills.class));
    }

    public void CheckActiveDelivery() {
        mRef.child(getString(R.string.node_delivery)).child(uId).addValueEventListener(new ValueEventListener() {
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