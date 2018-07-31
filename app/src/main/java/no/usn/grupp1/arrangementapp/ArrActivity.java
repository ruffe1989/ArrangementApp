package no.usn.grupp1.arrangementapp;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.os.AsyncTask;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.rbrooks.indefinitepagerindicator.IndefinitePagerIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ArrActivity extends AppCompatActivity {

    // https://github.com/rbro112/Android-Indefinite-Pager-Indicator

    private RecyclerView mRecyclerView;
    private ArrayList<Arrangement> mArrData;

    private ArrAdapter ArrAdapter;

    private SessionManager session;

    private IndefinitePagerIndicator indicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hovedside);

        session = new SessionManager(getApplicationContext());
        //Initialize the ArrayLIst that will contain the data
        mArrData = new ArrayList<>();

        //Initialize the RecyclerView
        mRecyclerView = findViewById(R.id.recyclerView);
        indicator = findViewById(R.id.recyclerview_pager_indicator);

        //Set the Layout Manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        //Initialize the adapter and set it ot the RecyclerView
        ArrAdapter = new ArrAdapter(this, mArrData);
        mRecyclerView.setAdapter(ArrAdapter);

        // Add pager behaviour
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mRecyclerView);
        indicator.attachToRecyclerView(mRecyclerView);

        // intitialize data in background
        initData data = new initData();
        data.execute((Void) null);

    }

    public class initData extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            initializeData();
            return null;
        }
    }

    private void initializeData() {


        String events = getString(R.string.endpoint)+"/event";

        if (isOnline()){
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            JsonObjectRequest JSONRequest = new JsonObjectRequest(Request.Method.GET, events, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject events = response.getJSONObject("event");
                        JSONArray eventsArray = events.getJSONArray("records");

                        //Clear the existing data (to avoid duplication)
                        mArrData.clear();

                        for(int i = 0; i < eventsArray.length(); i++){
                            JSONArray event = eventsArray.getJSONArray(i);
                            int EventID = event.getInt(0);
                            String Name = event.getString(1);
                            String Date = event.getString(2);
                            String Time = event.getString(3);
                            String Comment = event.getString(4);
                            String Description = event.getString(5);
                            String Producer = event.getString(6);
                            String Age = event.getString(7);
                            int Fee = event.getInt(8);
                            int Active = event.getInt(9);
                            mArrData.add(new Arrangement(Name ,Comment, Date, Time, Age, Fee, i,EventID));
                        }

                        ArrAdapter.notifyDataSetChanged();


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast toast = Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG);
                        toast.show();
                    }



                }
            }, new Response.ErrorListener(){

                @Override
                public void onErrorResponse(VolleyError error) {

                    Toast toast = Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_LONG);
                    toast.show();
                }
            });
            queue.add(JSONRequest);
        }

    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    // MENY
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if(session.isLoggedIn()){
            getMenuInflater().inflate(R.menu.logedinmenu, menu);
        }else{
            getMenuInflater().inflate(R.menu.hovedmenu, menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        invalidateOptionsMenu();
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(session.isLoggedIn()){
            getMenuInflater().inflate(R.menu.logedinmenu, menu);
        }else{
            getMenuInflater().inflate(R.menu.hovedmenu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login:
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivity(loginIntent);
                return true;
            case R.id.userInfo:
                Intent userIntent = new Intent(this, UserProfileActivity.class);
                startActivity(userIntent);
                return true;
            case R.id.logout:
                session.logoutUser();
                return true;
            case R.id.homeButton:
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivity(mainIntent);
            default:
        }
        return super.onOptionsItemSelected(item);
    }
}