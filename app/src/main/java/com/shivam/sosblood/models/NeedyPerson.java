package com.shivam.sosblood.models;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NeedyPerson extends User {
    private String note;
    private String needy_id;
    private Date creation_time;
    private boolean someone_else;

    public boolean isSomeone_else() {
        return someone_else;
    }

    public void setSomeone_else(boolean someone_else) {
        this.someone_else = someone_else;
    }

    public String getNeedy_id() {
        return needy_id;
    }

    public void setNeedy_id(String needy_id) {
        this.needy_id = needy_id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getCreation_time() {
        return creation_time;
    }

    public void setCreation_time(String creation_time_string) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        try
        {
            Date date = format.parse(creation_time_string);
            Calendar calendar=Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.HOUR_OF_DAY,5);
            long t=calendar.getTimeInMillis()+(30*60*1000);
            this.creation_time=new Date(t);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
