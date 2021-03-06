package com.example.dispatch.constructors;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

@IgnoreExtraProperties
public class DeliveryRun implements Serializable {
    public String order_Id;
    public String name;
    public String address;
    public String phone;
    public String delivery_id;
    public String pickUpAddress;
    public String userId;
    public String pickUpTime;
    public String deliveryTime;
    public String imageUrl;
    public @ServerTimestamp
    Date date;
    public Double latitude;
    public Double longitude;

    public DeliveryRun(String order_Id, String name, String address, String phone, String delivery_id, String pickUpAddress, String userId,
                       String pickUpTime, String deliveryTime, String imageUrl, Date date, Double latitude, Double longitude) {
        this.order_Id = order_Id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.delivery_id = delivery_id;
        this.pickUpAddress = pickUpAddress;
        this.userId = userId;
        this.pickUpTime = pickUpTime;
        this.deliveryTime = deliveryTime;
        this.imageUrl = imageUrl;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public DeliveryRun() {
    }

    public String getOrder_Id() {
        return order_Id;
    }

    public void setOrder_Id(String order_Id) {
        this.order_Id = order_Id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDelivery_id() {
        return delivery_id;
    }

    public void setDelivery_id(String delivery_id) {
        this.delivery_id = delivery_id;
    }

    public String getPickUpAddress() {
        return pickUpAddress;
    }

    public void setPickUpAddress(String pickUpAddress) {
        this.pickUpAddress = pickUpAddress;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPickUpTime() {
        return pickUpTime;
    }

    public void setPickUpTime(String pickUpTime) {
        this.pickUpTime = pickUpTime;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
