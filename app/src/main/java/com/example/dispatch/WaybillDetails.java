package com.example.dispatch;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dispatch.constructors.DeliveryRun;

public class WaybillDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waybill_details);

        Intent intent = getIntent();
        DeliveryRun deliveryRun = (DeliveryRun) intent.getSerializableExtra("waybill");

        Toast.makeText(this, "Delivery for " + deliveryRun.getName(), Toast.LENGTH_SHORT).show();
    }
}