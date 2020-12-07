package com.example.dispatch.constructors;

import java.io.Serializable;

public class Schedule_deliveries implements Serializable {

    public String order_Id;
    public String name;
    public String address;
    public String phone;
    public String delivery_id;
    public String pickUpAddress;
    public String userId;

    public Schedule_deliveries() {
    }

    public Schedule_deliveries(String order_Id, String name, String address, String phone, String delivery_id, String pickUpAddress, String userId) {
        this.order_Id = order_Id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.delivery_id = delivery_id;
        this.pickUpAddress = pickUpAddress;
        this.userId = userId;
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
}
