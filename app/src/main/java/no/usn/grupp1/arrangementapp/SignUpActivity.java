package no.usn.grupp1.arrangementapp;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.android.volley.toolbox.Volley.newRequestQueue;

public class SignUpActivity extends AppCompatActivity {

    private RequestQueue queue;

    private EditText mName;
    private EditText mEmail;
    private EditText mPassword;
    private EditText mPasswordRepeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mName = findViewById(R.id.name_signup);
        mEmail = findViewById(R.id.email_signup);
        mPassword = findViewById(R.id.password_signup);
        mPasswordRepeat = findViewById(R.id.password_repeat_signup);

        Button mSignUpButton = findViewById(R.id.email_sign_up_button);
        mSignUpButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                signup();
            }
        });
    }

    private void signup(){

        if(!isValid()){
            onSignupFailed();
            return;
        }



        JSONObject nyBruker = new JSONObject();
        try {
            nyBruker.put("Username", mName.getText().toString());
            nyBruker.put("Password", mPassword.getText().toString());
            nyBruker.put("Email", mEmail.getText().toString());
            nyBruker.put("IsAdmin", "0");

            checkUsers(nyBruker);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void onSignupFailed() {
        Toast toast = Toast.makeText(getApplicationContext(), R.string.error_signup_failed, Toast.LENGTH_SHORT);
        toast.show();
        recreate();
    }

    public void onSignupSuccess(JSONObject nyBruker) {
        // TODO registrere bruker i databasen
        // TODO logg inn brukeren etter registrering og gå tilbake til hovedside
        // TODO eller tilbake til sign in og brukeren og logge inn selv
        queue = newRequestQueue(getApplicationContext());

        String bruker_URL = getString(R.string.endpoint) + "/user";

        if(isOnline()){
            JsonObjectRequest JSONRequest = new JsonObjectRequest(Request.Method.POST, bruker_URL, nyBruker, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                }
            }, new Response.ErrorListener(){

                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
            queue.add(JSONRequest);
        }


        finish();
    }


    private boolean isValid(){

        boolean valid = true;
        String name = mName.getText().toString();
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();
        String passwordRepeat = mPasswordRepeat.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            mName.setError(getString(R.string.error_invalid_name));
            valid = false;
        } else {
            mName.setError(null);
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            mEmail.setError(getString(R.string.error_invalid_email));
            valid = false;
        } else{
            mEmail.setError(null);
        }

        if(password.length() < 4){
            mPassword.setError(getString(R.string.error_invalid_password));
            valid = false;
        } else{
            mPassword.setError(null);
        }

        if(!password.equals(passwordRepeat)){
            mPasswordRepeat.setError(getString(R.string.error_password_nomatch));
            valid = false;
        } else{
            mPasswordRepeat.setError(null);
        }

        return valid;
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void checkUsers(final JSONObject nyBruker){
        if (isOnline()){
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String user_URL = getString(R.string.endpoint)+"/user?filter=Email,eq," +mEmail.getText().toString()+"&transform=1";
            JsonObjectRequest JSONRequest = new JsonObjectRequest(Request.Method.GET, user_URL , null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray temp = response.getJSONArray("user");

                        if(temp.length() == 0){
                            onSignupSuccess(nyBruker);
                        }
                        else{
                            Toast toast = Toast.makeText(getApplicationContext(),"Bruker finnes",Toast.LENGTH_LONG);
                            toast.show();
                        }

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
}