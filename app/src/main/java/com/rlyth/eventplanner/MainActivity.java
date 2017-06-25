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

public class MainActivity extends AppCompatActivity {
    int requestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getVenuesFromServer();

        addVenueButton();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == 5) {
            getVenuesFromServer();
        }
    }


    void getVenuesFromServer() {
        OkHttpClient mOkHttpClient;

        HttpUrl reqUrl = HttpUrl.parse(BuildConfig.API_SERVER_BASE_URL + "/venue");

        Request request = new Request.Builder()
                .url(reqUrl)
                .build();

        mOkHttpClient = new OkHttpClient();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String r = response.body().string();

                try {
                    JSONArray jArray = new JSONArray(r);

                    List<Map<String,String>> venues = new ArrayList<Map<String,String>>();
                    for(int i = 0; i < jArray.length(); i++) {
                        JSONObject j = jArray.getJSONObject(i);

                        HashMap<String,String> m = new HashMap<String,String>();

                        m.put("id", j.getString("id"));
                        m.put("name", j.getString("name"));
                        m.put("address", j.getString("address"));

                        venues.add(m);
                    }

                    // Create an adapter and use it to populate a list of events
                    final SimpleAdapter venueAdapter = new SimpleAdapter(
                            MainActivity.this,
                            venues,
                            R.layout.view_venue,
                            new String[]{"name", "address", "id"},
                            new int[]{R.id.view_venue_name, R.id.view_venue_address,
                                    R.id.view_venue_id});
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ListView venueList = ((ListView)findViewById(R.id.venue_list));

                            venueList.setAdapter(venueAdapter);
                            venueList.setOnItemClickListener(new venueClickHandler());
                        }
                    });
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }


    void addVenueButton() {
        ((Button)findViewById(R.id.add_venue)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddVenue.class);

                intent.putExtra("action", "add");

                startActivityForResult(intent, requestCode);
            }
        });
    }


    // When an event item is clicked on, this will send the event ID to the event details page
    private class venueClickHandler implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Get the id value
            TextView idBox = (TextView) view.findViewById(R.id.view_venue_id);
            String venId = idBox.getText().toString();

            // Create and launch the intent
            Intent intent = new Intent(MainActivity.this, VenuePage.class);
            intent.putExtra("id", venId);
            startActivityForResult(intent, requestCode);
        }
    }
};