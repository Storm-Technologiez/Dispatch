package com.example.dispatch.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dispatch.R;
import com.example.dispatch.constructors.DeliveryRun;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class FirestoreAdapter extends FirestoreRecyclerAdapter<DeliveryRun, FirestoreAdapter.DeliveryHolder> {

    onItemClickListener listener;

    public FirestoreAdapter(@NonNull FirestoreRecyclerOptions<DeliveryRun> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull DeliveryHolder deliveryHolder, int i, @NonNull DeliveryRun deliveryRun) {
        deliveryHolder.txtDeliveryId.setText("Delivery Number: " + deliveryRun.getOrder_Id());
        deliveryHolder.txtPickLocation.setText(deliveryRun.getPickUpAddress());
    }

    @NonNull
    @Override
    public DeliveryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.rv_delivery, parent, false);
        return new DeliveryHolder(itemView);
    }

    public void setOnItemClickListener(onItemClickListener listener) {
        this.listener = listener;
    }

    public interface onItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    class DeliveryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txtDeliveryId, txtPickLocation;

        public DeliveryHolder(@NonNull View itemView) {
            super(itemView);
            txtDeliveryId = itemView.findViewById(R.id.tv_delNumber);
            txtPickLocation = itemView.findViewById(R.id.tv_pickAddress);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            if (position != RecyclerView.NO_POSITION && listener != null) {
                listener.onItemClick(getSnapshots().getSnapshot(position), position);
            }
        }
    }

}
