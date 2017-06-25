package com.rlyth.eventplanner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VenuePage extends AppCompatActivity {
    String id;
    int requestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_page);

        id = getIntent().getStringExtra("id");

        getVenueDetails();

        getVenueEvents();

        setDeleteButton();

        setVenueEditButton();

        setNewEventButton();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Venue was updated
        if(resultCode == 5) {
            setResult(5);
            getVenueDetails();
        }

        // Events were updated
        if(resultCode == 6) {
            getVenueEvents();
        }
    }

    void getVenueDetails() {
        HttpUrl reqUrl = HttpUrl.parse(BuildConfig.API_SERVER_BASE_URL +  "/venue/" + id);

        Request request = new Request.Builder()
                .url(reqUrl)
                .build();

        OkHttpClient mOkHttpClient = new OkHttpClient();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String r = response.body().string();

                try {
                    JSONObject eventObj = new JSONObject(r);

                    final String name = eventObj.getString("name");
                    final String address = eventObj.getString("address");
                    final String description = eventObj.getString("description");
                    final String occupancy = eventObj.getString("max_occupancy");
                    final String phone = eventObj.getString("phone_num");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Set textviews
                            TextView t;

                            t = (TextView)findViewById(R.id.venue_page_name);
                            t.setText(name);

                            t = (TextView)findViewById(R.id.venue_page_address);
                            if("null".equals(address) || "".equals(address)) {
                                t.setText("N/A");
                            }
                            else {
                                t.setText(address);
                            }

                            t = (TextView)findViewById(R.id.venue_page_description);
                            if("null".equals(description) || "".equals(description)) {
                                t.setText("N/A");
                            }
                            else {
                                t.setText(description);
                            }

                            t = (TextView)findViewById(R.id.venue_page_occupancy);
                            if("null".equals(occupancy)) {
                                t.setText("N/A");
                            }
                            else {
                                t.setText(occupancy);
                            }

                            t = (TextView)findViewById(R.id.venue_page_phone);
                            if("null".equals(phone)) {
                                t.setText("N/A");
                            }
                            else {
                                t.setText(phone);
                            }

                        }
                    });

                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }


    // Retrieves information on all events scheduled for this venue
    void getVenueEvents() {
        HttpUrl reqUrl = HttpUrl.parse(BuildConfig.API_SERVER_BASE_URL +  "/venue/" + id + "/event");

        Request request = new Request.Builder()
                .url(reqUrl)
                .build();

        OkHttpClient mOkHttpClient = new OkHttpClient();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String r = response.body().string();

                try {
                    JSONArray jArray = new JSONArray(r);

                    // Retrieve event's id, name, and date
                    List<Map<String,String>> events = new ArrayList<Map<String,String>>();
                    for(int i = 0; i < jArray.length(); i++) {
                        JSONObject j = jArray.getJSONObject(i);

                        HashMap<String,String> m = new HashMap<String,String>();

                        m.put("id", j.getString("id"));
                        m.put("name", j.getString("name"));
                        if("".equals(j.getString("date")) || "null".equals(j.getString("date"))) {
                           m.put("date", "");
                        }
                        else {
                            m.put("date", j.getString("date"));
                        }

                        events.add(m);
                    }

                    // Create an adapter and use it to populate a list of events
                    final SimpleAdapter eventAdapter = new SimpleAdapter(
                            VenuePage.this,
                            events,
                            R.layout.view_event,
                            new String[]{"name", "date", "id"},
                            new int[]{R.id.event_list_name, R.id.event_list_date, R.id.event_list_id});
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ListView eventList = ((ListView)findViewById(R.id.venue_page_event_list));

                            eventList.setAdapter(eventAdapter);
                            eventList.setOnItemClickListener(new eventClickHandler());
                        }
                    });
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }

            }
        });
    }


    void setDeleteButton() {
        ((Button)findViewById(R.id.venue_page_delete)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpUrl reqUrl = HttpUrl.parse(BuildConfig.API_SERVER_BASE_URL +  "/venue/" + id);

                Request request = new Request.Builder()
                        .url(reqUrl)
                        .delete()
                        .build();

                OkHttpClient mOkHttpClient = new OkHttpClient();

                mOkHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        // return to previous view
                        setResult(5);
                        finish();
                    }
                });
            }
        });
    }


    void setVenueEditButton() {
        ((Button)findViewById(R.id.venue_page_edit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VenuePage.this, AddVenue.class);
                intent.putExtra("id", id);

                // Add venue's current details to intent
                TextView t = (TextView)findViewById(R.id.venue_page_name);
                intent.putExtra("name", t.getText().toString());

                t = (TextView)findViewById(R.id.venue_page_address);
                if("N/A".equals(t.getText().toString())) {
                    intent.putExtra("address", "");
                }
                else {
                    intent.putExtra("address", t.getText().toString());
                }

                t = (TextView)findViewById(R.id.venue_page_description);
                if("N/A".equals(t.getText().toString())) {
                    intent.putExtra("description", "");
                }
                else {
                    intent.putExtra("description", t.getText().toString());
                }

                t = (TextView)findViewById(R.id.venue_page_occupancy);
                if("N/A".equals(t.getText().toString())) {
                    intent.putExtra("max_occupancy", "");
                }
                else {
                    intent.putExtra("max_occupancy", t.getText().toString());
                }

                t = (TextView)findViewById(R.id.venue_page_phone);
                if("N/A".equals(t.getText().toString())) {
                    intent.putExtra("phone", "");
                }
                else {
                    intent.putExtra("phone", t.getText().toString());
                }

                intent.putExtra("action", "edit");

                startActivityForResult(intent, requestCode);
            }
        });
    }


    void setNewEventButton() {
        ((Button)findViewById(R.id.venue_page_new_event)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VenuePage.this, AddEvent.class);

                intent.putExtra("parent_venue", id);

                intent.putExtra("action", "add");

                startActivityForResult(intent, requestCode);
            }
        });
    }

    // When an event item is clicked on, this will send the event ID to the event details page
    private class eventClickHandler implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Get the event's ID value
            TextView idBox = (TextView) view.findViewById(R.id.event_list_id);
            String eventID = idBox.getText().toString();

            // Create and launch the intent
            Intent intent = new Intent(VenuePage.this, EventPage.class);
            intent.putExtra("id", eventID);
            startActivityForResult(intent, requestCode);
        }
    }

};