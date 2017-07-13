package com.example.sosblood.utils;

import android.content.Context;

import com.example.sosblood.models.Donor;
import com.example.sosblood.models.NeedyPerson;
import com.example.sosblood.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class JSONParser {

    public static User parseFbLogin(JSONObject object)
    {
        User user=new User();
        try
        {
            user.setEmail(object.getString("email"));
            user.setFirst_name(object.getString("first_name"));
            user.setLast_name(object.getString("last_name"));
            user.setPicture_url(object.getString("picture"));
            user.setId(object.getInt("id"));
            user.setAccess_token(object.getString("access_token"));

            String bday=object.getString("birthday");
            try
            {
                int age=getAge(Integer.parseInt(bday.substring(6,10)),Integer.parseInt(bday.substring(0,2)),Integer.parseInt(bday.substring(3,5)));
                user.setAge(age);
            }catch(IndexOutOfBoundsException e)
            {
                user.setAge(null);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try
        {
            user.setAddress(object.getString("address"));
        }catch(JSONException e)
        {
            e.printStackTrace();
        }

        try
        {
            user.setBlood_group(object.getInt("bgroup"));
        }catch(JSONException e)
        {
            e.printStackTrace();
        }

        try
        {
            user.setLatitude(object.getDouble("latitude"));
        }catch(JSONException e)
        {
            e.printStackTrace();
        }

        try
        {
            user.setLongitude(object.getDouble("longitude"));
        }catch(JSONException e)
        {
            e.printStackTrace();
        }

        return user;
    }

    private static int getAge(int DOByear, int DOBmonth, int DOBday)
    {
        int age;
        final Calendar calenderToday = Calendar.getInstance();
        int currentYear = calenderToday.get(Calendar.YEAR);
        int currentMonth = 1 + calenderToday.get(Calendar.MONTH);
        int todayDay = calenderToday.get(Calendar.DAY_OF_MONTH);

        age = currentYear - DOByear;

        if(DOBmonth > currentMonth){
            --age;
        }
        else if(DOBmonth == currentMonth){
            if(DOBday > todayDay){
                --age;
            }
        }
        return age;
    }

    public static List<NeedyPerson> fetchNeedyPersons(JSONArray array, Context context) {
        List<NeedyPerson> needy_persons=new ArrayList<>();

        for(int i=0;i<array.length();i++)
        {
            try
            {
                NeedyPerson needy_person=new NeedyPerson();
                JSONObject obj=array.getJSONObject(i);
                needy_person.setFirst_name(obj.getString("first_name"));
                needy_person.setNeedy_id(obj.getString("id"));
                needy_person.setNote(obj.getString("note"));
                needy_person.setBlood_group(obj.getInt("bgroup"));
                needy_person.setAddress(obj.getString("address"));
                needy_person.setAge(obj.getInt("age"));
                needy_person.setLast_name(obj.getString("last_name"));
                needy_person.setPicture_url(obj.getString("picture"));
                needy_person.setCreation_time(obj.getString("created_at"));

                needy_persons.add(needy_person);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return needy_persons;
    }

    public static List<Donor> fetchDonors(JSONArray array) {
        List<Donor> donors=new ArrayList<>();

        for(int i=0;i<array.length();i++)
        {
            try
            {
                JSONObject obj=array.getJSONObject(i);
                Donor donor=new Donor();

                donor.setNote(obj.getString("note"));
                donor.setLast_name(obj.getString("last_name"));
                donor.setPicture_url(obj.getString("picture"));
                donor.setFirst_name(obj.getString("first_name"));
                donor.setPhone(obj.getString("phone"));
                donor.setId(obj.getString("id"));

                donors.add(donor);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return donors;
    }
}