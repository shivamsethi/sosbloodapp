package com.shivam.sosblood.utils;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.shivam.sosblood.R;
import com.shivam.sosblood.others.MyConstants;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class FetchAddressIntentService extends IntentService {

    private ResultReceiver receiver;

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder=new Geocoder(this, Locale.getDefault());
        Location location=intent.getParcelableExtra(MyConstants.LOCATION_DATA_EXTRA);
        receiver=intent.getParcelableExtra(MyConstants.RECEIVER);
        String error_msg="";

        List<Address> addresses=null;
        try
        {
            addresses=geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
        } catch (IOException e) {
            e.printStackTrace();
            error_msg=getString(R.string.service_unavailable);
        } catch (IllegalArgumentException e)
        {
            e.printStackTrace();
            error_msg=getString(R.string.invalid_lat_lng);
        }

        if(addresses==null || addresses.size()==0)
        {
            if(error_msg.isEmpty())
            {
                error_msg=getString(R.string.no_address_found);
            }
            deliverResultToReceiver(MyConstants.FAILURE_RESULT,error_msg,"");
        }
        else
        {
            Address address=addresses.get(0);
            StringBuilder builder=new StringBuilder();

            if(address.getLocality()==null)
            {
                if(address.getSubAdminArea()!=null)
                    builder.append(address.getSubAdminArea());
            }else
            {
                builder.append(address.getLocality());
            }
            if(address.getCountryName()!=null)
            {
                builder.append(", ");
                builder.append(address.getCountryName());
            }

            deliverResultToReceiver(MyConstants.SUCCESS_RESULT,builder.toString(),address.getAddressLine(0));
        }
    }

    private void deliverResultToReceiver(int result_code, String result_data,String address) {
        Bundle bundle = new Bundle();
        bundle.putString(MyConstants.RESULT_DATA_KEY, result_data);
        bundle.putString(MyConstants.ADDRESS_DATA_KEY, address);
        receiver.send(result_code, bundle);
    }
}