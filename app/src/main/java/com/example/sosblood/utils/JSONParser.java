package com.example.sosblood.utils;

import com.example.sosblood.models.User;

import org.json.JSONException;
import org.json.JSONObject;

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
            user.setAge(21);

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
}