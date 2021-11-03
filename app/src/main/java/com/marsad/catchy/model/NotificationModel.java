package com.marsad.catchy.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class NotificationModel {

    String id, notification;

    @ServerTimestamp
    Date time;

    public NotificationModel() {
    }


    public NotificationModel(String id, String notification, Date time) {
        this.id = id;
        this.notification = notification;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
