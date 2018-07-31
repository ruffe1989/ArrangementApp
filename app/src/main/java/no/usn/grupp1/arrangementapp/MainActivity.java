package no.usn.grupp1.arrangementapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ImageView mainImage = findViewById(R.id.mainImage);
        TextView title = findViewById(R.id.mainTitle);
        TextView desc = findViewById(R.id.mainDesc);
        title.setText(R.string.title);
        desc.setText(R.string.desc);
        Button arrInfo = findViewById(R.id.viewArr);
        arrInfo.setText("Vis arrangement");
        Glide.with(this).load(R.drawable.hovedbilde).into(mainImage);

        arrInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ArrActivity.class);
                startActivity(i);
            }
        });

        session = new SessionManager(getApplicationContext());
    }

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
