package com.rlyth.eventplanner;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddVenue extends AppCompatActivity {
    String action;
    private OkHttpClient mOkHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_venue);

        action = getIntent().getStringExtra("action");

        // If editing existing venue, populate input fields with current values
        if("edit".equals(action)) {
            setDefaultValues();
        }

        submitVenue();
    }


    void submitVenue() {
        Button submit = (Button)findViewById(R.id.add_venue_submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText thisInput = (EditText)findViewById(R.id.add_venue_name);

                // If name exists, start building POST
                if(!"".equals(thisInput.getText().toString())) {
                    // Create JSON body
                    JSONObject venue = buildVenuePost();
                    MediaType JSON = MediaType.parse("application/json");
                    RequestBody reqBody = RequestBody.create(JSON, (venue.toString()));

                    HttpUrl reqUrl;
                    Request request;

                    // Update values of existing venue via PATCH
                    if("edit".equals(action)) {
                        String id = getIntent().getStringExtra("id");
                        reqUrl = HttpUrl.parse(BuildConfig.API_SERVER_BASE_URL + "/venue/" + id);

                        request = new Request.Builder()
                                .url(reqUrl)
                                .patch(reqBody)
                                .build();
                    }
                    // Create new venue via POST
                    else {
                        reqUrl = HttpUrl.parse(BuildConfig.API_SERVER_BASE_URL + "/venue");

                        request = new Request.Builder()
                                .url(reqUrl)
                                .post(reqBody)
                                .build();
                    }

                    mOkHttpClient = new OkHttpClient();

                    mOkHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            // return to previous view
                            setResult(5);
                            finish();
                        }
                    });
                }
            }
        });
    }


    // Build a JSON object using values entered into input fields
    JSONObject buildVenuePost() {
        String name, address, description;
        int occupancy=-1;
        long phone=-1;

        EditText t = (EditText)findViewById(R.id.add_venue_name);
        name = t.getText().toString();

        t = (EditText)findViewById(R.id.add_venue_address);
        address = t.getText().toString();

        t = (EditText)findViewById(R.id.add_venue_description);
        description = t.getText().toString();

        t = (EditText)findViewById(R.id.add_venue_occupancy);
        try {
            occupancy = Integer.parseInt(t.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        t = (EditText)findViewById(R.id.add_venue_phone);
        try {
            phone = Long.parseLong(t.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject venue = new JSONObject();
        try {
            venue.put("name", name);
            venue.put("address", address);
            venue.put("description", description);

            if(occupancy >= 0) {
                venue.put("max_occupancy", occupancy);
            }

            if(phone >= 0) {
                venue.put("phone_num", phone);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return venue;
    }


    void setDefaultValues() {
        // Retrieve event info
        String name = getIntent().getStringExtra("name");
        String description = getIntent().getStringExtra("description");
        String address = getIntent().getStringExtra("address");
        String max_occupancy = getIntent().getStringExtra("max_occupancy");
        String phone = getIntent().getStringExtra("phone");

        EditText e = (EditText)findViewById(R.id.add_venue_name);
        e.setText(name, TextView.BufferType.EDITABLE);

        e = (EditText)findViewById(R.id.add_venue_address);
        e.setText(address);

        e = (EditText)findViewById(R.id.add_venue_description);
        e.setText(description);

        e = (EditText)findViewById(R.id.add_venue_phone);
        e.setText(phone);

        e = (EditText)findViewById(R.id.add_venue_occupancy);
        e.setText(max_occupancy);
    }
}
