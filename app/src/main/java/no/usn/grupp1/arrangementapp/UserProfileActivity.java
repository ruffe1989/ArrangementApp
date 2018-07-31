package no.usn.grupp1.arrangementapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaCas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

// TODO vis navn, email, billettoversikt(link). Kan redigeres

public class UserProfileActivity extends AppCompatActivity {

    private SessionManager session;
    private TextView nameText, emailText;

    private RecyclerView mRecyclerView;
    private ArrayList<Billett> mBillettData;
    private BillettAdapter BillettAdapter;

    // Hashmap containing all events with its id as key.
    private HashMap<Integer, Arrangement> eventMap;

    // Queue for http requests.
    private RequestQueue queue;

    // Endpoint strings.
    private String tickets;
    private String events;
    private String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        session = new SessionManager(getApplicationContext());
        nameText = findViewById(R.id.getName);
        emailText = findViewById(R.id.getUsername);

        HashMap<String, String> user = session.getUserDetails();

        String name = user.get(SessionManager.KEY_NAME);
        String email = user.get(SessionManager.KEY_EMAIL);

        nameText.setText(name);
        emailText.setText(email);

        //Initialize the RecyclerView
        mRecyclerView = findViewById(R.id.billettRecyclerView);

        //Set the Layout Manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mBillettData = new ArrayList<>();

        initializeData();

        BillettAdapter = new BillettAdapter(this, mBillettData);
        mRecyclerView.setAdapter(BillettAdapter);
        int dragdir = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT |
                ItemTouchHelper.DOWN |ItemTouchHelper.UP;
        int movedir = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        ItemTouchHelper deleteSeat = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(dragdir,movedir) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
                builder.setMessage("Ønsker du å slette billetten?");
                builder.setNegativeButton("Nei", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        builder.setCancelable(true);
                        BillettAdapter.notifyDataSetChanged();
                    }
                });
                builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int ticket = mBillettData.get(viewHolder.getAdapterPosition()).getTicket();
                        mBillettData.remove(viewHolder.getAdapterPosition());

                        BillettAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());

                        String deleteRequestURL = getResources().getString(R.string.endpoint)+"/ticket/"+ticket;
                        if(isOnline()){
                            queue = Volley.newRequestQueue(getApplicationContext());

                            StringRequest stringRequest = new StringRequest(Request.Method.DELETE, deleteRequestURL, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.i("LoggVolley", response);
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("LoggVolleyError",error.toString());
                                }
                            });
                            queue.add(stringRequest);
                        }

                        Toast toast = Toast.makeText(getApplicationContext(),"Delete funker kanskje",Toast.LENGTH_LONG);
                        toast.show();
                    }
                });

                builder.create();
                builder.show();

                /**/
            }
        });
        deleteSeat.attachToRecyclerView(mRecyclerView);

    }

    private void initializeData() {

        events = getString(R.string.endpoint)+"/event";


        // Initialize Hashmap for events.
        eventMap = new HashMap<>();

        // Retrieving users email for userid request.
        SessionManager session = new SessionManager(getApplicationContext());

        HashMap<String, String> user = session.getUserDetails();
        String email = user.get(SessionManager.KEY_EMAIL);

        userid = getString(R.string.endpoint) + "/user?filter=Email,eq," + email;

        if (isOnline()){
            queue = Volley.newRequestQueue(getApplicationContext());

            // Request for all events, EVER.
            JsonObjectRequest JSONRequestEvents = new JsonObjectRequest(Request.Method.GET, events, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject events = response.getJSONObject("event");
                        JSONArray eventsArray = events.getJSONArray("records");

                        for(int i = 0; i < eventsArray.length(); i++){
                            JSONArray ticket = eventsArray.getJSONArray(i);
                            int EventID = ticket.getInt(0);
                            String tittel = ticket.getString(1);
                            String description = ticket.getString(5);
                            String date = ticket.getString(2);
                            String time = ticket.getString(3);
                            String age = ticket.getString(7);
                            int fee = ticket.getInt(8);
                            int pos = -1;
                            Arrangement currentArr = new Arrangement(tittel, description, date, time, age, fee, pos, EventID);
                            eventMap.put(new Integer(EventID), currentArr);
                        }

                        // New request for user's tickets after getting all events.
                        JsonObjectRequest JSONRequestUserID = null;
                        requestUserID(userid, JSONRequestUserID);

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

            queue.add(JSONRequestEvents);
        }


    }

    // Gets user's id from email.
    // On response calls method for getting user's ticket data.
    private void requestUserID(String UserID, JsonObjectRequest JSONRequestUserID){
        JSONRequestUserID = new JsonObjectRequest(Request.Method.GET, userid, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject JSONObjectUserID = response.getJSONObject("user");
                    JSONArray UserIDArray = JSONObjectUserID.getJSONArray("records");

                    JSONArray UserIDSingle = UserIDArray.getJSONArray(0);
                    int UserIDValue = UserIDSingle.getInt(0);

                    JsonObjectRequest JSONRequestTickets = null;
                    requestTicket(UserIDValue, JSONRequestTickets);

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
        queue.add(JSONRequestUserID);

    }

    // Gets data from the user's tickets and updates RecyclerView with the data.
    private void requestTicket(final int UserID, JsonObjectRequest JSONRequestTickets){

        tickets = getString(R.string.endpoint)+"/ticket?filter=UserID,eq," + UserID;

        JSONRequestTickets = new JsonObjectRequest(Request.Method.GET,tickets, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    JSONObject tickets = response.getJSONObject("ticket");
                    JSONArray ticketsArray = tickets.getJSONArray("records");

                    //Clear the existing data (to avoid duplication)
                    mBillettData.clear();

                    for(int i = 0; i < ticketsArray.length(); i++){
                        JSONArray ticket = ticketsArray.getJSONArray(i);
                        int TicketID = ticket.getInt(0);
                        int EventID = ticket.getInt(1);
                        Arrangement currentArr = eventMap.get(new Integer(EventID));
                        int Seat = ticket.getInt(2);
                        Billett bil = new Billett(currentArr.getTittel(), Seat, currentArr.getFeeInt(), TicketID);
                        //createDeleteButton(TicketID);

                        mBillettData.add(bil);
                    }

                    //Notify the adapter of the change
                    BillettAdapter.notifyDataSetChanged();

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
        queue.add(JSONRequestTickets);
    }


    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    // Meny
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
