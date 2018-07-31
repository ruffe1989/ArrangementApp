package no.usn.grupp1.arrangementapp;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.android.volley.toolbox.Volley.newRequestQueue;

public class SeatSelectionActivity extends AppCompatActivity {

    private SessionManager session;
    GridView grid;
    int[] seatId;
    int eventID;
    String title;
    private ArrayList<JSONObject> tickets = new ArrayList<>();
    ImageView sete;
    RequestQueue queue;
    boolean run = true;
    Button cancelButton, reserveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        eventID = getIntent().getIntExtra("eventID",0);
        title = getIntent().getStringExtra("title");
        seatId = getIntent().getIntArrayExtra("OpptatteSeter");
        session = new SessionManager(getApplicationContext());
        queue = newRequestQueue(this);
        cancelButton = findViewById(R.id.ticketCancelButton);
        reserveButton = findViewById(R.id.ticketReserverButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (JSONObject j : tickets) {
                    int eventIDbutton = -1;
                    int seatIDbutton = -1;
                    try {
                        eventIDbutton = Integer.parseInt(j.getString("EventID"));
                        seatIDbutton = Integer.parseInt(j.getString("SeatID"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(eventIDbutton > 0){
                        finnSete(eventIDbutton, seatIDbutton);
                    }

                }
                finish();
            }
        });

        reserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                finish();
                startActivity(intent);
            }
        });

        /*Toast toast = Toast.makeText(getApplicationContext(),title.toString(),Toast.LENGTH_LONG);
        toast.show();*/

        SeatAdapter adapter = new SeatAdapter(seatId,SeatSelectionActivity.this);
        grid = findViewById(R.id.gridview);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // ledig sete
                if(session.isLoggedIn()){
                    if(seatId[position] != -1){
                        sete = view.findViewById(R.id.seatImage);
                        Glide.with(getApplicationContext()).load(R.drawable.valgtsete).into(sete);
                        velgSete(eventID, position);
                    }
                    // opptatt sete
                    else{
                        Log.d("SETE", "OPPTATT");
                    }
                }
                else{
                    session.checkAndSendLogin();
                }


            }
        });
        ImageView im = findViewById(R.id.sceneBilde);
        Glide.with(this).load(R.drawable.scene).into(im);

    }


    public void velgSete(int eventID, int seatID) {
        // Får tak i bruker ID fra session manager
        HashMap<String, String> user = session.getUserDetails();
        int id =  Integer.parseInt(user.get(SessionManager.KEY_ID));

        // lager et nytt ticket object
        JSONObject nyTicket = new JSONObject();
        try {
            nyTicket.put("EventID", Integer.toString(eventID));
            nyTicket.put("SeatID", Integer.toString(seatID+1));
            nyTicket.put("UserID", user.get(SessionManager.KEY_ID));


        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // deselect method
        for (JSONObject j : tickets) {
            if(j.toString().equals(nyTicket.toString())){
                finnSete(eventID,seatID+1);
                Glide.with(getApplicationContext()).load(R.drawable.ledigsete).into(sete);
                tickets.remove(j);

                run = false;
            }
        }

        if(run) {
            tickets.add(nyTicket);
            Log.d("TICKETS", tickets.toString());

            String ticket_URL = getString(R.string.endpoint) + "/ticket";

            if (isOnline()) {
                JsonObjectRequest JSONRequest = new JsonObjectRequest(Request.Method.POST, ticket_URL, nyTicket, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
                queue.add(JSONRequest);
            }
        }
        run = true;

    }


    public void finnSete(int eventID, int seatID){
        String seatURL = getString(R.string.endpoint) + "/ticket?filter[]=EventID,eq," + eventID + "&filter[]=SeatID,eq," + seatID+"&satisfy=all";
        if (isOnline()) {
            JsonObjectRequest JSONRequest = new JsonObjectRequest(Request.Method.GET, seatURL, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject ticket = response.getJSONObject("ticket");
                        JSONArray records = ticket.getJSONArray("records");
                        int ticketIDRequest = records.getJSONArray(0).getInt(0);

                        deleteSete(ticketIDRequest);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast toast = Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG);
                        toast.show();
                    }


                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                    Toast toast = Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG);
                    toast.show();
                }
            });
            queue.add(JSONRequest);
        }
    }

    public void deleteSete(int ticket) {

        String deleteURL = getString(R.string.endpoint) + "/ticket/" + ticket;

        StringRequest sr = new StringRequest(Request.Method.DELETE, deleteURL,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        Log.d("DELETE", "WORKS");
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast toast = Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
        );

        queue.add(sr);

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
