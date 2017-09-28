package com.shivam.sosblood.models;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Donor implements Serializable{

    private String id;
    private String first_name;
    private String last_name;
    private String phone;
    private String note;
    private String picture_url;
    private Date response_creation_time;

    public String getPicture_url() {
        return picture_url;
    }

    public void setPicture_url(String picture_url) {
        this.picture_url = picture_url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getResponse_creation_time() {
        return response_creation_time;
    }

    public void setResponse_creation_time(String response_creation_time) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        try
        {
            Date date = format.parse(response_creation_time);
            Calendar calendar=Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.HOUR_OF_DAY,5);
            long t=calendar.getTimeInMillis()+(30*60*1000);
            this.response_creation_time=new Date(t);
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }
}