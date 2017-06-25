package com.rlyth.eventplanner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EventPage extends AppCompatActivity {
    String id;
    int requestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_page);

        id = getIntent().getStringExtra("id");

        getEventDetails();

        setDeleteButton();

        setEventEditButton();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Event information was edited; update page with new info
        getEventDetails();
        setResult(6);
    }


    // GETs information about this event from server
    void getEventDetails() {
        HttpUrl reqUrl = HttpUrl.parse(BuildConfig.API_SERVER_BASE_URL + "/event/" + id);

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
                    final String location = eventObj.getString("location");
                    final String date = eventObj.getString("date");
                    final String host = eventObj.getString("host");
                    final String notes = eventObj.getString("notes");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Set textviews
                            TextView t;

                            t = (TextView)findViewById(R.id.event_page_name);
                            t.setText(name);

                            t = (TextView)findViewById(R.id.event_page_location);
                            if(!"null".equals(location) && !"".equals(location)) {
                                t.setText(location);
                            }

                            t = (TextView)findViewById(R.id.event_page_date);
                            if(!"null".equals(date) && !"".equals(date)) {
                                t.setText(date);
                            }

                            t = (TextView)findViewById(R.id.event_page_host);
                            if(!"null".equals(host) && !"".equals(host)) {
                                t.setText(host);
                            }

                            t = (TextView)findViewById(R.id.event_page_notes);
                            if(!"null".equals(notes) && !"".equals(notes)) {
                                t.setText(notes);
                            }

                        }
                    });

                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        });


    }


    void setEventEditButton() {
        ((Button)findViewById(R.id.event_page_edit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventPage.this, AddEvent.class);
                intent.putExtra("id", id);

                // Add current event details to intent
                TextView t = (TextView)findViewById(R.id.event_page_name);
                intent.putExtra("name", t.getText().toString());

                t = (TextView)findViewById(R.id.event_page_host);
                if("N/A".equals(t.getText().toString())) {
                    intent.putExtra("host", "");
                }
                else {
                    intent.putExtra("host", t.getText().toString());
                }

                t = (TextView)findViewById(R.id.event_page_location);
                if("N/A".equals(t.getText().toString())) {
                    intent.putExtra("location", "");
                }
                else {
                    intent.putExtra("location", t.getText().toString());
                }

                t = (TextView)findViewById(R.id.event_page_date);
                if("N/A".equals(t.getText().toString())) {
                    intent.putExtra("date", "");
                }
                else {
                    intent.putExtra("date", t.getText().toString());
                }

                t = (TextView)findViewById(R.id.event_page_notes);
                if("N/A".equals(t.getText().toString())) {
                    intent.putExtra("notes", "");
                }
                else {
                    intent.putExtra("notes", t.getText().toString());
                }

                intent.putExtra("action", "edit");

                startActivityForResult(intent, requestCode);
            }
        });
    }


    void setDeleteButton() {
        ((Button)findViewById(R.id.event_page_delete)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpUrl reqUrl = HttpUrl.parse(BuildConfig.API_SERVER_BASE_URL + "/event/" + id);

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
                        setResult(6);
                        finish();
                    }
                });
            }
        });
    }


};