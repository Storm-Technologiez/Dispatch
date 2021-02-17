package com.example.dispatch.constructors;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class RtDelivery implements Serializable {
    public String name;
    public String address;
    public String phone;
    public String delivery_id;
    public String pickUpAddress;
    public String userId;
    public String pickUpTime;
    public String deliveryTime;
    public String imageUrl;
    public LatLng latLng;

    public RtDelivery(String name, String address, String phone, String delivery_id, String pickUpAddress,
                      String userId, String pickUpTime, String deliveryTime, String imageUrl, LatLng latLng) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.delivery_id = delivery_id;
        this.pickUpAddress = pickUpAddress;
        this.userId = userId;
        this.pickUpTime = pickUpTime;
        this.deliveryTime = deliveryTime;
        this.imageUrl = imageUrl;
        this.latLng = latLng;
    }

    public RtDelivery() {
    }
}
