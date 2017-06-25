package com.rlyth.eventplanner;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
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

public class AddEvent extends AppCompatActivity {
    private OkHttpClient mOkHttpClient;
    String action;
    int refreshResult=6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        action = getIntent().getStringExtra("action");

        // If editing existing event, populate input fields with event details
        if("edit".equals(action)) {
            setDefaultValues();
        }

        submitEvent();
    }


    // Handles post/patch requests for events
    void submitEvent() {
        Button submit = (Button)findViewById(R.id.add_event_submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check that name has been filled in
                EditText thisInput = (EditText)findViewById(R.id.add_event_name);

                // Name exists; start building POST
                if(!"".equals(thisInput.getText().toString())) {
                    // Create JSON body
                    JSONObject event = buildEventPost();
                    MediaType JSON = MediaType.parse("application/json");
                    RequestBody reqBody = RequestBody.create(JSON, (event.toString()));

                    HttpUrl reqUrl;
                    Request request;

                    // Update event via PATCH
                    if("edit".equals(action)) {
                        String id = getIntent().getStringExtra("id");
                        reqUrl = HttpUrl.parse(BuildConfig.API_SERVER_BASE_URL + "/event/" + id);

                        request = new Request.Builder()
                                .url(reqUrl)
                                .patch(reqBody)
                                .build();
                    }
                    // Create new event via POST
                    else {
                        reqUrl = HttpUrl.parse(BuildConfig.API_SERVER_BASE_URL + "/event");

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
                            String r = response.body().string();

                            // If new event was created, add event to current venue's EventList
                            if("add".equals(action)) {
                                try {
                                    JSONObject j = new JSONObject(r);

                                    String newEventId = j.getString("id");

                                    putEventInVenue(newEventId);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                            else {
                                // Return to previous view
                                setResult(refreshResult);
                                finish();
                            }
                        }
                    });
                }
            }
        });
    }


    // Make put request with event to parent venue
    void putEventInVenue(String eventID) {
        String venueID = getIntent().getStringExtra("parent_venue");

        JSONObject event = new JSONObject();
        try {
            event.put("event_id", eventID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpUrl reqUrl = HttpUrl.parse(BuildConfig.API_SERVER_BASE_URL + "/venue/" + venueID + "/event");
        MediaType JSON = MediaType.parse("application/json");

        RequestBody reqBody = RequestBody.create(JSON, event.toString());

        Request request = new Request.Builder()
                .url(reqUrl)
                .put(reqBody)
                .build();

        mOkHttpClient = new OkHttpClient();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Return to previous view
                setResult(refreshResult);
                finish();
            }
        });
    }


    // Retrieve input values and add to JSONObject
    JSONObject buildEventPost() {
        String location, host, notes, date, name;

        EditText t = (EditText)findViewById(R.id.add_event_name);
        name = t.getText().toString();

        t = (EditText)findViewById(R.id.add_event_location);
        location = t.getText().toString();

        t = (EditText)findViewById(R.id.add_event_date);
        date = t.getText().toString();

        t = (EditText)findViewById(R.id.add_event_host);
        host = t.getText().toString();

        t = (EditText)findViewById(R.id.add_event_notes);
        notes = t.getText().toString();

        // Build JSON input
        JSONObject event = new JSONObject();
        try {
            event.put("name", name);
            event.put("location", location);
            event.put("date", date);
            event.put("host", host);
            event.put("notes", notes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return event;
    }


    // Populate EditText inputs with existing values of edited event
    void setDefaultValues() {
        // Retrieve event info
        String name = getIntent().getStringExtra("name");
        String host = getIntent().getStringExtra("host");
        String location = getIntent().getStringExtra("location");
        String date = getIntent().getStringExtra("date");
        String notes = getIntent().getStringExtra("notes");

        EditText e = (EditText)findViewById(R.id.add_event_name);
        e.setText(name, TextView.BufferType.EDITABLE);

        e = (EditText)findViewById(R.id.add_event_host);
        e.setText(host);

        e = (EditText)findViewById(R.id.add_event_date);
        e.setText(date);

        e = (EditText)findViewById(R.id.add_event_location);
        e.setText(location);

        e = (EditText)findViewById(R.id.add_event_notes);
        e.setText(notes);
    }

}
