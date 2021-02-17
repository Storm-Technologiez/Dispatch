package com.example.dispatch.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dispatch.DeliveryInfoMapsActivity;
import com.example.dispatch.R;
import com.example.dispatch.constructors.DeliveryRun;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.DeliveryViewHolder> {
    String userId;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    ArrayList<DeliveryRun> deliveries;
    ChildEventListener mListener;
    DeliveryRun selected_delivery;

    public DeliveryAdapter() {
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();
        deliveries = new ArrayList<>();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                DeliveryRun sd = snapshot.getValue(DeliveryRun.class);
                sd.setOrder_Id(snapshot.getKey());
                deliveries.add(sd);
                notifyItemInserted(deliveries.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        mRef.child("scheduled_deliveries").addChildEventListener(mListener);
    }

    @NonNull
    @Override
    public DeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.rv_delivery, parent, false);
        return new DeliveryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryViewHolder holder, int position) {
        DeliveryRun delivery = deliveries.get(position);
        holder.bind(delivery);
    }

    @Override
    public int getItemCount() {
        return deliveries.size();
    }

    public class DeliveryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView delivery_Id, pick_address;
        ProgressDialog progressBar = new ProgressDialog(itemView.getContext());

        public DeliveryViewHolder(@NonNull View itemView) {
            super(itemView);
            delivery_Id = itemView.findViewById(R.id.tv_delNumber);
            pick_address = itemView.findViewById(R.id.tv_pickAddress);
            itemView.setOnClickListener(this);
        }

        public void bind(DeliveryRun delivery) {
            delivery_Id.setText("Delivery Number: " + delivery.getDelivery_id());
            pick_address.setText(delivery.getPickUpAddress());
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            selected_delivery = deliveries.get(position);
            GetSenderLocation();

            final AlertDialog.Builder alert = new AlertDialog.Builder(itemView.getContext());
            alert.setTitle("Delivery " + selected_delivery.getDelivery_id());
            alert.setMessage("Pick up point: \n" + selected_delivery.getPickUpAddress());
            alert.setPositiveButton("Accept", (dialog, which) -> {

                Loading();
                DeliveryRun pickedDelivery = new DeliveryRun(selected_delivery.getOrder_Id(), selected_delivery.getName(),
                        selected_delivery.getAddress(), selected_delivery.getPhone(), selected_delivery.getDelivery_id(),
                        selected_delivery.pickUpAddress, selected_delivery.getUserId(), selected_delivery.getPickUpTime(),
                        selected_delivery.getDeliveryTime(), selected_delivery.getImageUrl(), selected_delivery.getDate(),
                        selected_delivery.getLatitude(), selected_delivery.getLongitude());

                mRef.child("delivery_in_progress").child(userId).setValue(pickedDelivery)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                mRef.child("scheduled_deliveries").child(selected_delivery.getOrder_Id())
                                        .removeValue().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        progressBar.dismiss();
                                        Intent intent = new Intent(itemView.getContext(), DeliveryInfoMapsActivity.class);
                                        intent.putExtra("delivery", selected_delivery);
                                        itemView.getContext().startActivity(intent);
                                    }
                                });
                            }
                        });
            });
            alert.setNegativeButton("Reject", (dialog, which) ->
                    dialog.cancel());
            alert.show();
        }

        /**
         * The method below is throwing null pointer error
         * but it still gets the item and upload to db
         */
        private void GetSenderLocation() {
            mRef.child("scheduled_deliveries").child(selected_delivery.getOrder_Id()).child("latLng")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            double latitude, longitude;
                            String Lat, Long;
                            Lat = snapshot.child("latitude").getValue().toString();
                            Long = snapshot.child("longitude").getValue().toString();

                            latitude = Double.parseDouble(Lat);
                            longitude = Double.parseDouble(Long);

                            selected_delivery.setLatitude(latitude);
                            selected_delivery.setLongitude(longitude);
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

        private void CheckActiveDelivery() {
            mRef.child("delivery_in_progress").child(selected_delivery.getUserId() // ToDo: this is supposed to be the rider uId.
            ).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    final AlertDialog.Builder alert = new AlertDialog.Builder(itemView.getContext());
                    String test = String.valueOf(snapshot.child("address").getValue());
                    if (test != null) {
                        alert.setTitle("Delivery In Progress");
                        alert.setMessage("You have a delivery in progress. \nPress OK below to complete your delivery.");
                        alert.setNegativeButton("OK", (dialog, which) -> {
                            Intent intent = new Intent(itemView.getContext(), DeliveryInfoMapsActivity.class);
                            intent.putExtra("delivery", selected_delivery);
                            itemView.getContext().startActivity(intent);
                        });
                        alert.show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}
