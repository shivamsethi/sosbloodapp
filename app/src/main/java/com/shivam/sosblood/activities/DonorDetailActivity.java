package com.shivam.sosblood.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.shivam.sosblood.R;
import com.shivam.sosblood.models.Donor;
import com.shivam.sosblood.others.MyConstants;
import com.shivam.sosblood.others.MySingleton;
import com.shivam.sosblood.utils.CommonTasks;
import com.shivam.sosblood.utils.DateHandler;
import com.shivam.sosblood.utils.JSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DonorDetailActivity extends AppCompatActivity {

    private String access_token,donor_id;
    private Toolbar toolbar;
    private TextView name_textview,phone_textview,date_textview,note_textview,note_head_textview,help_textview;
    private ImageView picture_imageview;
    private Donor donor;
    private ProgressBar image_progress_bar,center_progress_bar;
    private RelativeLayout dial_icon_rel_lay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_detail);

        access_token=getIntent().getBundleExtra("donor").getString("access_token");
        donor_id=getIntent().getBundleExtra("donor").getString("donor_id");

        toolbar=(Toolbar)findViewById(R.id.toolbar_id);
        name_textview=(TextView)findViewById(R.id.name_textview_id);
        phone_textview=(TextView)findViewById(R.id.phone_textview_id);
        date_textview=(TextView)findViewById(R.id.date_textview_id);
        note_textview=(TextView)findViewById(R.id.note_textview_id);
        note_head_textview=(TextView)findViewById(R.id.note_head_textview_id);
        help_textview=(TextView)findViewById(R.id.help_textview_id);
        picture_imageview=(ImageView)findViewById(R.id.imageview_id);
        image_progress_bar=(ProgressBar)findViewById(R.id.image_progress_bar_id);
        center_progress_bar=(ProgressBar)findViewById(R.id.center_progress_bar_id);
        dial_icon_rel_lay=(RelativeLayout)findViewById(R.id.dial_icon_rel_lay_id);

        picture_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    CommonTasks.showImageDialog(DonorDetailActivity.this,((BitmapDrawable)(picture_imageview.getDrawable())).getBitmap());
                }catch(NullPointerException e)
                {
                    e.printStackTrace();
                    Toast.makeText(DonorDetailActivity.this, "Image not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dial_icon_rel_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+donor.getPhone()));
                startActivity(intent);
            }
        });

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Blood Request Response");
        CommonTasks.setToolbarFont(toolbar,this);

        fetchDonor();
    }

    private void fetchDonor(){
        String url= MyConstants.BASE_URL_API+"blood_request_responses/"+donor_id;
        JsonObjectRequest request=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response)
            {
                try
                {
                    donor= JSONParser.fetchDonor(response.getJSONObject("blood_request_response"));
                    populateDonor(donor);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                if(!CommonTasks.isNetworkAvailable(DonorDetailActivity.this))
                    Toast.makeText(DonorDetailActivity.this, "Network connectivity problem", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers=new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization",access_token);
                return headers;
            }
        };
        MySingleton.getInstance(this).addToRequestQueue(request,"needy_detail");
    }

    private void populateDonor(Donor donor) {
        String note=donor.getNote();
        if(note.isEmpty())
            note="---";

        ImageRequest request=new ImageRequest(donor.getPicture_url(), new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                image_progress_bar.setVisibility(View.INVISIBLE);
                picture_imageview.setImageBitmap(response);
            }
        }, 0, 0, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                image_progress_bar.setVisibility(View.INVISIBLE);
                picture_imageview.setImageBitmap(((BitmapDrawable)(getResources().getDrawable(R.drawable.user))).getBitmap());
            }
        });
        MySingleton.getInstance(this).addToRequestQueue(request,"needy_image");

        name_textview.setText(donor.getFirst_name()+" "+donor.getLast_name());
        phone_textview.setText(donor.getPhone());
        date_textview.setText(new DateHandler().getSimplifiedDate(donor.getResponse_creation_time()));
        note_head_textview.setText(donor.getFirst_name()+" says:");
        note_textview.setText(note);
        help_textview.setText(donor.getFirst_name()+" "+getResources().getString(R.string.help_text_three));

        center_progress_bar.setVisibility(View.INVISIBLE);
    }

    public void actButtonImageClick(View view) {

    }
}