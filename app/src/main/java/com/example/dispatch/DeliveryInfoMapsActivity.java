package com.example.dispatch;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.dispatch.constructors.DeliveryRun;
import com.example.dispatch.constructors.Schedule_deliveries;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DeliveryInfoMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    ProgressDialog progressBar;
    DatabaseReference mRef;
    DeliveryRun delivery;
    String senderPhoneNumber, receiverPhoneNumber, uId, senderName;
    TextView deliveryNumber, deliveryAddress, receiverName, receiverPhone, S_name, S_address, S_phone;
    TextView textProgress, textShowMap, pickUpTime, deliveryTime, senderDetail;
    Button ConfirmPickDelivery, callSender, cancelDelivery, callReceiver;
    RoundedImageView profileImage;
    RelativeLayout detailsLayout;
    GridLayout buttonGrid;
    FrameLayout mapLayout;
    private GoogleMap mMap;

    public void CancelDelivery(View view) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(DeliveryInfoMapsActivity.this);
        alert.setTitle("Cancel Delivery");
        alert.setMessage("Confirm you want to cancel this delivery run.");
        alert.setPositiveButton("Continue", (dialog, which) -> {

            Schedule_deliveries RtDelivery = new Schedule_deliveries(delivery.getOrder_Id(), delivery.getName(), delivery.getAddress(), delivery.getPhone(),
                    delivery.getDelivery_id(), delivery.getPickUpAddress(), delivery.getUserId());

            Loading();
            mRef.child("scheduled_deliveries").child(delivery.getOrder_Id()).setValue(RtDelivery)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mRef.child("delivery_in_progress").child(uId).removeValue().addOnCompleteListener(task1 -> {
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
        if (ConfirmPickDelivery.getText().toString().toLowerCase().equals("confirm pick")) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(DeliveryInfoMapsActivity.this);
            alert.setTitle("Confirm Pick Up");
            alert.setMessage("Confirm you have picked the package.");
            alert.setPositiveButton("Confirm", (dialog, which) ->
                    ConfirmPickUp());
            alert.setNegativeButton("Cancel", (dialog, which) ->
                    dialog.cancel());
            alert.show();
        } else {
            final AlertDialog.Builder alert = new AlertDialog.Builder(DeliveryInfoMapsActivity.this);
            alert.setTitle("Confirm package delivered");
            alert.setMessage("Confirm you have delivered the package.");
            alert.setPositiveButton("Confirm", (dialog, which) -> {
                DateFormat format = new SimpleDateFormat("hh:mm aa");
                String date = format.format(new Date());
                Loading();
                mRef.child("delivery_in_progress").child(uId).child("deliveryTime").setValue(date).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        deliveryTime.setText(date);
                        FinishDelivery();
                    }
                });
            });
            alert.setNegativeButton("Cancel", (dialog, which) ->
                    dialog.cancel());
            alert.show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_info_maps);

        CheckPhonePermission();

        profileImage = findViewById(R.id.image_sender);
        progressBar = new ProgressDialog(this);
        mRef = FirebaseDatabase.getInstance().getReference();

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

        /*
        if (delivery.getPickUpTime() != null){
            pickUpTime.setText(delivery.getPickUpTime());
        }

        if (delivery.getDeliveryTime() != null){
            deliveryTime.setText(delivery.getDeliveryTime());
        }
         */

        /**
         * Fetching Sender's details by Id
         * this is not same with the rider uId everywhere else in this code
         */
        uId = delivery.getUserId();
        ;
        if (uId != null) {
            mRef.child("users").child(uId).addValueEventListener(new ValueEventListener() {

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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.isMyLocationEnabled();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    public void ConfirmPickUp() {
        DateFormat format = new SimpleDateFormat("hh:mm aa");
        String date = format.format(new Date());
        Loading();
        mRef.child("delivery_in_progress").child(uId).child("pickUpTime").setValue(date).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                pickUpTime.setText(date);
                progressBar.dismiss();
                SwitchDetails();
            }
        });
    }

    private void FinishDelivery() {
        mRef.child("delivery_in_progress").child(uId).addValueEventListener(new ValueEventListener() {
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
        mRef.child("completed_deliveries").child(uId).push().setValue(complete).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                mRef.child("delivery_in_progress").child(uId).removeValue().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        progressBar.dismiss();
                        Toast.makeText(DeliveryInfoMapsActivity.this, "Delivery Completed", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
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
}