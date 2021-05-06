package com.example.dispatch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.dispatch.fragments.AccountFragment;
import com.example.dispatch.fragments.EmergencyFragment;
import com.example.dispatch.fragments.ManifestFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    final String MY_PREFS_NAME = "switchCheckedState";
    private static final int GPS_RESULT = 42;
    DatabaseReference mRef;
    String uId;
    String test;
    Fragment fragment = null;
    RelativeLayout Toolbar;
    TextView HeaderText;
    ImageView moreOption;
    static MainActivity instance;
    BottomSheetBehavior bottomSheetBehavior;

    public static MainActivity getInstance() {
        return instance;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // CheckActiveDelivery();
        setContentView(R.layout.activity_main);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);


        if (!CheckGps()) {
            showSettingsAlert();
        }

        mRef = FirebaseDatabase.getInstance().getReference();
        uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav1);
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

    public void GpsSettings(View view) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        MainActivity.this.startActivityForResult(intent, GPS_RESULT);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public Boolean CheckGps() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void showSettingsAlert() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setDraggable(false);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS_RESULT && resultCode == RESULT_OK) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            Log.i("Intent Result", "OK");
        }
    }
}