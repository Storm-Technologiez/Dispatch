package com.example.dispatch;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.dispatch.constructors.DeliveryRun;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.dispatch.R.string.node_scheduled_deliveries;

public class DeliveryInfoMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int PERMISSION_REQUEST_CODE = 20;
    private static final int PLAY_REQUEST_RES_REQUEST = 21;
    private static final int UPDATE_INTERVAL = 2000;
    private static final int FASTEST_INTERVAL = 300;

    ProgressDialog progressBar;
    DeliveryRun delivery;
    private static final int DISPLACEMENT = 5;
    TextView deliveryNumber, deliveryAddress, receiverName, receiverPhone, S_name, S_address, S_phone;
    TextView textProgress, textShowMap, pickUpTime, deliveryTime, senderDetail;
    Button ConfirmPickDelivery, callSender, cancelDelivery, callReceiver;
    RoundedImageView profileImage;
    RelativeLayout detailsLayout;
    GridLayout buttonGrid;
    FrameLayout mapLayout;


    private GoogleMap mMap;
    FirebaseFirestore db;
    DocumentReference deliveryRef;
    DatabaseReference mRef, drivers;
    String userId;
    String senderPhoneNumber, receiverPhoneNumber, senderId, senderName;
    GeoFire geoFire;
    Marker mCurrent;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    public void CancelDelivery(View view) {
        cancelDeliveryRun();
    }

    private void cancelDeliveryRun() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(DeliveryInfoMapsActivity.this);
        alert.setTitle("Cancel Delivery");
        alert.setMessage("Confirm you want to cancel this delivery run.");
        alert.setPositiveButton("Continue", (dialog, which) -> {

            DeliveryRun RtDelivery = new DeliveryRun(delivery.getOrder_Id(), delivery.getName(), delivery.getAddress(), delivery.getPhone(),
                    delivery.getDelivery_id(), delivery.getPickUpAddress(), delivery.getUserId(), "", "", "");

            Loading();
            mRef.child(getString(node_scheduled_deliveries)).child(delivery.getOrder_Id()).setValue(RtDelivery)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mRef.child(getString(R.string.node_delivery)).child(userId).removeValue().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    progressBar.dismiss();
                                    Toast.makeText(this, "Delivery Cancelled", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                        }
                    });
        });
        alert.setNegativeButton("Cancel", (dialog, which) ->
                dialog.cancel());
        alert.show();
    }

    public void ConfirmPickDelivery(View view) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(DeliveryInfoMapsActivity.this);
        if (ConfirmPickDelivery.getText().toString().toLowerCase().equals("confirm pick")) {
            alert.setTitle("Confirm Pick Up");
            alert.setMessage("Confirm you have picked the package.");
            alert.setPositiveButton("Confirm", (dialog, which) ->
                    ConfirmPickUp());
            alert.setNegativeButton("Cancel", (dialog, which) ->
                    dialog.cancel());
        } else {
            alert.setTitle("Confirm package delivered");
            alert.setMessage("Confirm you have delivered the package.");
            alert.setPositiveButton("Confirm", (dialog, which) -> {
                DateFormat format = new SimpleDateFormat("hh:mm aa");
                String date = format.format(new Date());
                Loading();
                mRef.child(getString(R.string.node_delivery)).child(userId).child("deliveryTime").setValue(date).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        deliveryTime.setText(date);
                        FinishDelivery();
                    }
                });
            });
            alert.setNegativeButton("Cancel", (dialog, which) ->
                    dialog.cancel());
        }
        alert.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_info_maps);

        CheckPhonePermission();
        //startLocationUpdate();

        profileImage = findViewById(R.id.image_sender);
        progressBar = new ProgressDialog(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        db = FirebaseFirestore.getInstance();
        deliveryRef = db.collection(getString(R.string.collection_completed_delivery)).document(userId).collection("deliveries").document();
        mRef = FirebaseDatabase.getInstance().getReference();
        drivers = FirebaseDatabase.getInstance().getReference("Drivers");
        geoFire = new GeoFire(drivers);

        setUpLocation();

        /*
            ----- TextViews -----
         */
        S_name = findViewById(R.id.tv_S_name);
        S_address = findViewById(R.id.tv_S_address);
        S_phone = findViewById(R.id.tv_S_phone);
        textProgress = findViewById(R.id.tv_progress);
        textShowMap = findViewById(R.id.tv_show_map);
        pickUpTime = findViewById(R.id.tv_pickUpTime);
        deliveryTime = findViewById(R.id.tv_deliveryTime);
        senderDetail = findViewById(R.id.tv_sender);
        deliveryNumber = findViewById(R.id.tv_delivery_number);
        deliveryAddress = findViewById(R.id.tv_delivery_address);
        receiverName = findViewById(R.id.tv_delivery_receiver);
        receiverPhone = findViewById(R.id.tv_delivery_TelNumber);

        /*
            ----- Buttons -----
         */
        ConfirmPickDelivery = findViewById(R.id.btn_confirmPickDelivery);
        callSender = findViewById(R.id.btn_callSender);
        cancelDelivery = findViewById(R.id.btn_cancelDeliver);
        callReceiver = findViewById(R.id.btn_call_receiver);

        /*
            ----- Layouts -----
         */
        detailsLayout = findViewById(R.id.receiver);
        buttonGrid = findViewById(R.id.grid_button);
        mapLayout = findViewById(R.id.map_layout);

        Intent intent = getIntent();
        delivery = (DeliveryRun) intent.getSerializableExtra("delivery");

        receiverPhoneNumber = delivery.getPhone();

        deliveryNumber.setText(delivery.getDelivery_id());
        deliveryAddress.setText(delivery.getAddress());
        receiverName.setText(delivery.getName());
        receiverPhone.setText(receiverPhoneNumber);

        /**
         * Fetching Sender's details by Id
         * this is not same with the rider uId everywhere else in this code
         */
        senderId = delivery.getUserId();
        if (senderId != null) {
            mRef.child("users").child(senderId).addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    senderPhoneNumber = String.valueOf(snapshot.child("phone").getValue());
                    senderName = String.valueOf(snapshot.child("name").getValue());

                    S_name.setText(senderName);
                    S_address.setText(delivery.getPickUpAddress());
                    S_phone.setText(senderPhoneNumber);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        /*
        mRef.child("delivery_in_progress").child(uId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String test = snapshot.child("isPicked").getValue().toString();
                if (test.equals("true")) {
                    SwitchDetails();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

         */

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        displayLocation();
    }

    public void ConfirmPickUp() {
        DateFormat format = new SimpleDateFormat("hh:mm aa");
        String date = format.format(new Date());
        Loading();
        mRef.child(getString(R.string.node_delivery))
                .child(userId)
                .child("pickUpTime").setValue(date)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        pickUpTime.setText(date);
                        progressBar.dismiss();
                        SwitchDetails();
                    }
                });
    }

    private void FinishDelivery() {
        mRef.child(getString(R.string.node_delivery))
                .child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        DeliveryRun complete = snapshot.getValue(DeliveryRun.class);
                        Push(complete);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void Push(DeliveryRun complete) {
        deliveryRef.set(complete).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                RemoveDelivery();
                stopLocationUpdate();
                Toast.makeText(this, "Delivery Completed", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DeliveryInfoMapsActivity.this, UploadEvidenceActivity.class);
                intent.putExtra("RefId", deliveryRef.getId());
                startActivity(intent);
                finish();

            } else {
                progressBar.dismiss();
                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void RemoveDelivery() {
        mRef.child(getString(R.string.node_delivery)).child(userId).removeValue().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                progressBar.dismiss();
            }
        });
    }

    public void ShowMap(View view) {
        if (textShowMap.getText().toString().toLowerCase().equals("show map")) {
            SwitchToMapView();
        } else {
            senderDetail.setText("Sender Details:");
            textShowMap.setText("Show Map");
            detailsLayout.setVisibility(View.VISIBLE);
            mapLayout.setVisibility(View.GONE);
            S_name.setText(senderName);
            S_address.setText(delivery.getPickUpAddress());
            S_phone.setText(senderPhoneNumber);
            profileImage.setVisibility(View.VISIBLE);
        }
    }

    public void SwitchDetails() {
        buttonGrid.setVisibility(View.GONE);
        callReceiver.setVisibility(View.VISIBLE);
        textProgress.setText("Delivery in Progress");
        ConfirmPickDelivery.setText("Confirm Delivery");
        ConfirmPickDelivery.setTextSize(13);
    }

    public void SwitchToMapView() {
        senderDetail.setText("Receiver Details:");
        textShowMap.setText("Hide Map");
        detailsLayout.setVisibility(View.GONE);
        mapLayout.setVisibility(View.VISIBLE);
        S_name.setText("Name: " + delivery.getName());
        S_address.setText("Address: " + delivery.getAddress());
        S_phone.setText("Phone number: " + receiverPhoneNumber);
        profileImage.setVisibility(View.GONE);
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            }, PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_REQUEST_RES_REQUEST).show();
            } else {
                Toast.makeText(this, "Google play services is not supported on this device", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this::onLocationChanged);
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            final double latitude = mLastLocation.getLatitude();
            final double longitude = mLastLocation.getLongitude();

            geoFire.setLocation(userId, new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (mCurrent != null)
                        mCurrent.remove();

                    mCurrent = mMap.addMarker(new MarkerOptions()
                            .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_bike))
                            .position(new LatLng(latitude, longitude))
                            .title("Your Location"));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16.5f));
                }
            });
        } else {
            Log.i("ERROR", "Cannot get your location");
        }
    }

    private void stopLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this::onLocationChanged);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void CallSender(View view) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + senderPhoneNumber));
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 10);
            return;
        }
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void CallReceiver(View view) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + receiverPhoneNumber));
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 10);
            return;
        }
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void CheckPhonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 10);
        }
    }

    private void Loading() {
        progressBar.setTitle("Please Wait...");
        progressBar.show();
        progressBar.setCanceledOnTouchOutside(false);
        progressBar.setCancelable(false);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void CheckGps() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gps) {
            showSettingsAlert();
        }
    }

    private void showSettingsAlert() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(DeliveryInfoMapsActivity.this);
        alert.setTitle("Turn on GPS");
        alert.setMessage("GPS is required for app's location services to work. \nGo to settings menu and enable GPS.");

        alert.setPositiveButton("Settings", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            DeliveryInfoMapsActivity.this.startActivity(intent);
        });

        alert.setNegativeButton("Cancel", (dialog, which) -> {
            cancelDeliveryRun();
            dialog.cancel();
            finish();
        });
        alert.setCancelable(false);
        alert.show();
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLastLocation = location;
        displayLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}