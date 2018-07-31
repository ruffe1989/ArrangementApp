package no.usn.grupp1.arrangementapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

/**
 * Created by finge on 19.02.2018.
 */

public class SessionManager {

    SharedPreferences pref;

    Editor editor;

    Context _context;

    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "ArrangementPref";

    private static final String IS_LOGIN = "IsLoggedIn";

    public static final String KEY_NAME = "name";

    public static final String KEY_EMAIL = "email";

    public static final String KEY_ID = "id";

    public SessionManager(Context _context) {
        this._context = _context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String name, String email, String id){
        // Storing login value as True
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        editor.putString(KEY_EMAIL, email);

        // Storing id in pref
        editor.putString(KEY_ID, id);

        // Storing name in pref
        editor.putString(KEY_NAME, name);

        // apply changes
        editor.apply();
    }

    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<>();

        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));

        // user email
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

        user.put(KEY_ID, pref.getString(KEY_ID, null));

        return user;
    }

    public void checkAndSendLogin(){
        if(!this.isLoggedIn()){
            Intent intent = new Intent(_context, LoginActivity.class);
            // Closing all the activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // start acitivty
            _context.startActivity(intent);
        }
    }

    public void logoutUser(){
        // clearing all data from Shared Pref
        editor.clear();
        editor.commit();

        // Redirect to ArrActivity after logout
        Intent intent = new Intent(_context, MainActivity.class);

        // Closing all the activities
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // start acitivty
        _context.startActivity(intent);
    }


    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }
}
