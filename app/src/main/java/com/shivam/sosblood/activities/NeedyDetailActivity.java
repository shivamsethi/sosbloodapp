package com.shivam.sosblood.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.shivam.sosblood.R;
import com.shivam.sosblood.models.NeedyPerson;
import com.shivam.sosblood.others.MyApplication;
import com.shivam.sosblood.others.MyConstants;
import com.shivam.sosblood.others.MySingleton;
import com.shivam.sosblood.utils.CommonTasks;
import com.shivam.sosblood.utils.DateHandler;
import com.shivam.sosblood.utils.JSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class NeedyDetailActivity extends AppCompatActivity {

    private NeedyPerson needy_person;
    private String access_token;
    private Toolbar toolbar;
    private ImageView pic_image_view;
    private TextView name_textview,blood_group_textview,someone_else_textview,creation_time_textview,age_textview,address_textview,note_head_textview,note_textview,help_textview;
    private ProgressBar image_progress_bar,center_progress_bar;
    private Bitmap screen_shot_bmp=null;
    private Menu menu;
    private static final int STORAGE_PERMISSION_REQUEST_CODE=1;
    private String request_id;
    private Button accept_button;
    private Integer user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_needy_detail);

        toolbar=(Toolbar)findViewById(R.id.toolbar_id);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Blood Request");
        CommonTasks.setToolbarFont(toolbar,this);

        pic_image_view=(ImageView)findViewById(R.id.image_view_id);
        name_textview=(TextView)findViewById(R.id.name_textview_id);
        blood_group_textview=(TextView)findViewById(R.id.blood_group_textview_id);
        address_textview=(TextView)findViewById(R.id.address_textview_id);
        note_head_textview=(TextView)findViewById(R.id.note_head_textview_id);
        note_textview=(TextView)findViewById(R.id.note_textview_id);
        image_progress_bar =(ProgressBar)findViewById(R.id.progress_bar_id);
        help_textview=(TextView)findViewById(R.id.help_textview_id);
        creation_time_textview=(TextView)findViewById(R.id.creation_time_textview_id);
        accept_button=(Button)findViewById(R.id.accept_button_id);
        center_progress_bar=(ProgressBar)findViewById(R.id.center_progress_bar_id);
        someone_else_textview=(TextView)findViewById(R.id.someone_else_textview_id);

        accept_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actButtonAccept();
            }
        });

        pic_image_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    CommonTasks.showImageDialog(NeedyDetailActivity.this,((BitmapDrawable)(pic_image_view.getDrawable())).getBitmap());
                }catch(NullPointerException e)
                {
                    e.printStackTrace();
                    Toast.makeText(NeedyDetailActivity.this, "Image not available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        image_progress_bar.setVisibility(View.VISIBLE);

        SharedPreferences shared_prefs=getApplicationContext().getSharedPreferences(MyConstants.SHARED_PREFS_USER_KEY, Context.MODE_PRIVATE);
        user_id=shared_prefs.getInt("id",0);

        access_token=getIntent().getBundleExtra("needy_person").getString("access_token");

        request_id=getIntent().getBundleExtra("needy_person").getString("request_id");
        fetchNeedyPerson();
    }

    private void fetchNeedyPerson() {

        String url=MyConstants.BASE_URL_API+"blood_requests/"+request_id;
        JsonObjectRequest request=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    needy_person= JSONParser.fetchNeedyPerson(response.getJSONObject("blood_request"));
                    populateNeedyPerson();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                if(!CommonTasks.isNetworkAvailable(NeedyDetailActivity.this))
                    Toast.makeText(NeedyDetailActivity.this, "Network connectivity problem", Toast.LENGTH_SHORT).show();

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

    private void takeScreenShotBitmap() {

        View screenView = getWindow().getDecorView().findViewById(android.R.id.content).getRootView();

        screenView.setDrawingCacheEnabled(true);

        screenView.measure(View.MeasureSpec.makeMeasureSpec(getMetrics().widthPixels, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(getMetrics().heightPixels, View.MeasureSpec.EXACTLY));
        screenView.layout(0,0, screenView.getMeasuredWidth(), screenView.getMeasuredHeight());
        screenView.buildDrawingCache(true);

        screen_shot_bmp = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);

        handleStoragePermissions();
    }

    private void handleStoragePermissions() {
        int permission= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permission== PackageManager.PERMISSION_DENIED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE))
            {
                AlertDialog.Builder builder=new AlertDialog.Builder(NeedyDetailActivity.this);
                builder.setTitle("Alert");
                builder.setMessage("SOS Blood offers you to share a request to your friends as a picture over messengers like WhatsApp. For this,storage permission is required. Kindly click on Allow in the next step.");
                builder.setNeutralButton("OKAY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(NeedyDetailActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_REQUEST_CODE);
                    }
                });
                AlertDialog dialog1=builder.create();
                dialog1.show();
            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_REQUEST_CODE);
            }
        }
        else if(permission==PackageManager.PERMISSION_GRANTED)
            setupShareRequestMenuItem(menu);
    }

    private DisplayMetrics getMetrics()
    {
        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics;
    }

    private void populateNeedyPerson() {

        String note=needy_person.getNote();
        if(note.isEmpty())
            note="---";

        name_textview.setText(needy_person.getFirst_name()+" "+needy_person.getLast_name());
        blood_group_textview.setText("needs "+((MyApplication)this.getApplication()).getBloodGroups().get(needy_person.getBlood_group())+" blood");
        if(needy_person.isSomeone_else())
            someone_else_textview.setText("for someone else");
        address_textview.setText(needy_person.getCity());
        note_head_textview.setText(needy_person.getFirst_name()+" says:");
        note_textview.setText(note);
        help_textview.setText(getResources().getString(R.string.help_text_one)+" "+needy_person.getFirst_name()+" "+getResources().getString(R.string.help_text_two));
        creation_time_textview.setText(new DateHandler().getSimplifiedDate(needy_person.getCreation_time()));

        center_progress_bar.setVisibility(View.INVISIBLE);

        ImageRequest request=new ImageRequest(needy_person.getPicture_url(), new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                image_progress_bar.setVisibility(View.INVISIBLE);
                pic_image_view.setImageBitmap(response);
                takeScreenShotBitmap();
            }
        }, 0, 0, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                image_progress_bar.setVisibility(View.INVISIBLE);
                Toast.makeText(NeedyDetailActivity.this, "Error loading image", Toast.LENGTH_SHORT).show();
                pic_image_view.setImageBitmap(((BitmapDrawable)getResources().getDrawable(R.drawable.user)).getBitmap());
                takeScreenShotBitmap();

                if(!CommonTasks.isNetworkAvailable(NeedyDetailActivity.this))
                    Toast.makeText(NeedyDetailActivity.this, "Network connectivity problem", Toast.LENGTH_SHORT).show();

                error.printStackTrace();
            }
        });
        MySingleton.getInstance(this).addToRequestQueue(request,"needy_detail");

        SharedPreferences shared_prefs=getSharedPreferences(MyConstants.SHARED_PREFS_EXTRA_KEY,MODE_PRIVATE);
        int responses_length=shared_prefs.getInt("my_responses_array_length",0);
        for(int i=0;i<responses_length;i++)
        {
            if(shared_prefs.getString("my_response_"+i,"").equals(needy_person.getNeedy_id()))
            {
                accept_button.setText("Already accepted");
                accept_button.setClickable(false);
                break;
            }
        }

        if(needy_person.getNeedy_id().equals(CommonTasks.getMyRequestId(NeedyDetailActivity.this)))
        {
            accept_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(NeedyDetailActivity.this, "You cannot accept your own request", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void actButtonAccept() {

        View view=getLayoutInflater().inflate(R.layout.request_accept_dialog,null,false);
        final EditText phone,note;
        Button cancel,submit;
        TextInputLayout phone_input_lay;
        phone=(EditText)view.findViewById(R.id.phone_edittext_id);
        note=(EditText)view.findViewById(R.id.note_edittext_id);
        cancel=(Button)view.findViewById(R.id.cancel_button_id);
        submit=(Button)view.findViewById(R.id.submit_button_id);
        phone_input_lay=(TextInputLayout)view.findViewById(R.id.phone_input_lay_id);

        phone_input_lay.setHint("Phone(to share with "+needy_person.getFirst_name()+")");

        AlertDialog.Builder builder=new AlertDialog.Builder(this)
                .setTitle("Accept")
                .setView(view);
        final AlertDialog dialog=builder.create();
        dialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(phone.getText().toString().length()==10)
                {
                    hitAcceptApi(needy_person.getNeedy_id(),phone.getText().toString(),note.getText().toString());
                }else
                {
                    Toast.makeText(NeedyDetailActivity.this, "Phone number should be of 10 digits", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void hitAcceptApi(final String needy_id, String phone, String note) {

        String url= MyConstants.BASE_URL_API+"blood_request_responses/";
        JSONObject blood_request_json,json;
        blood_request_json=new JSONObject();
        json=new JSONObject();
        try
        {
            blood_request_json.put("blood_request_id",needy_id);
            blood_request_json.put("phone",phone);
            blood_request_json.put("note",note);
            json.put("blood_request_response",blood_request_json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                AlertDialog.Builder builder=new AlertDialog.Builder(NeedyDetailActivity.this)
                        .setNeutralButton("OKAY", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setMessage("That was very kind of you! You may get a call from "+needy_person.getFirst_name()+" asking for your help. Thanks again and keep helping blood requesters by donating or sharing for ones in need. ")
                        .setCancelable(true);
                AlertDialog dialog=builder.create();
                dialog.show();
                addToMyRespondedRequests(needy_id);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                if(!CommonTasks.isNetworkAvailable(NeedyDetailActivity.this))
                    Toast.makeText(NeedyDetailActivity.this, "Network connectivity problem", Toast.LENGTH_SHORT).show();

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
        MySingleton.getInstance(this).addToRequestQueue(request,"needy_response");
    }

    private void addToMyRespondedRequests(String needy_id)
    {
        SharedPreferences shared_prefs=getSharedPreferences(MyConstants.SHARED_PREFS_EXTRA_KEY,MODE_PRIVATE);
        SharedPreferences.Editor editor=shared_prefs.edit();
        int responses_length=shared_prefs.getInt("my_responses_array_length",0);
        editor.putInt("my_responses_array_length",responses_length+1);
        editor.putString("my_response_"+responses_length,needy_id);
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.needy_detail_menu,menu);
        this.menu=menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode)
        {
            case STORAGE_PERMISSION_REQUEST_CODE:

                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    setupShareRequestMenuItem(menu);
                }
                else
                {
                    menu.findItem(R.id.share_item_id).setVisible(false);
                    Toast.makeText(this, "You denied the storage permission. Now you can't share this request to your friends.", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    private void setupShareRequestMenuItem(final Menu menu)
    {
        menu.findItem(R.id.share_item_id).setVisible(true);
        MenuItem share_item=menu.findItem(R.id.share_item_id);
        share_item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                try{
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_STREAM,getImageUri(NeedyDetailActivity.this,screen_shot_bmp));
                    intent.putExtra(Intent.EXTRA_TEXT,"SOSBlood Request. Please help! http://sosbloodapp.com/request?r="+needy_person.getNeedy_id()+"&u="+user_id);
                    intent.setType("*/*");
                    startActivity(Intent.createChooser(intent,"Select app..."));
                }catch(NullPointerException e)
                {
                    if(ContextCompat.checkSelfPermission(NeedyDetailActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED)
                        Toast.makeText(NeedyDetailActivity.this, "Storage permission problem. You can change permissions in phone settings.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.share_item_id:
                Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}