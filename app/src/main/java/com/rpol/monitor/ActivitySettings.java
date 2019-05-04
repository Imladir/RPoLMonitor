package com.rpol.monitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.rpol.monitor.R;
import com.rpol.monitor.helpers.Settings;
import com.rpol.monitor.network.Authenticator;
import com.rpol.monitor.network.UpdateScheduler;

import java.util.Arrays;
import java.util.List;

public class ActivitySettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Loads the last user's preferences
        SharedPreferences sp = getSharedPreferences(Settings.PREFS, MODE_PRIVATE);

        String nick = sp.getString(Settings.PREFS_NICK, "");
        String pwd = sp.getString((Settings.PREFS_PWD), "");
        Boolean save_pwd = sp.getBoolean(Settings.PREFS_SAVE_PWD, false);

        ((EditText) findViewById(R.id.etNickname)).setText(nick);
        ((EditText) findViewById(R.id.etPassword)).setText(pwd);
        ((CheckBox) findViewById(R.id.cbRememberPwd)).setChecked(save_pwd);

        // Shows the right log in / log out display depending on user's status
        if (!Settings.isLoggedIn()) {
            ((ConstraintLayout)findViewById(R.id.clLogin)).setVisibility(View.VISIBLE);
            ((ConstraintLayout)findViewById(R.id.clLoggedin)).setVisibility(View.GONE);
        } else {
            ((TextView)findViewById(R.id.tvLoggedin)).setText("You are logged in as " + Settings.getNickname());
            ((ConstraintLayout) findViewById(R.id.clLogin)).setVisibility(View.GONE);
            ((ConstraintLayout) findViewById(R.id.clLoggedin)).setVisibility(View.VISIBLE);
        }

        String[] intervalsArray = getResources().getStringArray(R.array.update_intervals);
        List<String> intervals = Arrays.asList(intervalsArray);
        ((Spinner)findViewById(R.id.ddlIntervals)).setSelection(intervals.indexOf("" + sp.getInt(Settings.PREFS_UPDATE_INTERVAL, 1)));

        ((Spinner)findViewById(R.id.ddlIntervals)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String[] intervals = getResources().getStringArray(R.array.update_intervals);
                int new_interval = Integer.parseInt(intervals[position]);
                UpdateScheduler.get(null).update_interval(new_interval);
                SharedPreferences sp = getSharedPreferences(Settings.PREFS, MODE_PRIVATE);
                sp.edit().putInt(Settings.PREFS_UPDATE_INTERVAL, new_interval).apply();
                Log.d("XXX", "New saved interval = " +  sp.getInt(Settings.PREFS_UPDATE_INTERVAL, 1));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // nothing to do here, can't (shouldn't?) happen
            }

        });
    }

    /** Called when the user taps the login button */
    public void authenticate(View view) {
        ((Button)findViewById(R.id.bLogin)).setClickable(false);

        SharedPreferences sp = getSharedPreferences(Settings.PREFS, MODE_PRIVATE);
        // Retrieve nickname and save it in preferences
        String nick = ((EditText) findViewById(R.id.etNickname)).getText().toString();
        sp.edit().putString(Settings.PREFS_NICK, nick).apply();

        // Retrieve password and saves it if asked to (saves the checkbox state)
        String pwd = ((EditText) findViewById(R.id.etPassword)).getText().toString();
        final CheckBox cbPwd = (CheckBox) findViewById(R.id.cbRememberPwd);
        sp.edit().putBoolean(Settings.PREFS_SAVE_PWD, cbPwd.isChecked()).apply();
        if (cbPwd.isChecked())
            sp.edit().putString(Settings.PREFS_PWD, pwd).apply();
        else
            sp.edit().putString(Settings.PREFS_PWD, "").apply();

        // Proceeds to (async) authentication
        Authenticator auth = new Authenticator(this);
        auth.execute(nick, pwd);
    }

    // Logs out, clears cookies
    public void log_out(View view) {
        Settings.logOut();
        Settings.setNickname("");
        Settings.getCookieManager().getCookieStore().removeAll();
        UpdateScheduler.get(null).stop();
        ((ConstraintLayout)findViewById(R.id.clLogin)).setVisibility(View.VISIBLE);
        ((ConstraintLayout)findViewById(R.id.clLoggedin)).setVisibility(View.GONE);
    }

    // Invoked at the end of the (async) authentication process
    // If successfully logged in, swicthes to the main screen
    // Otherwise just displays a small (unhelpful) message
    public void post_authenticate(Boolean success) {
        ((Button) findViewById(R.id.bLogin)).setClickable(true);
        if (success) {
            ((TextView)findViewById(R.id.tvLoggedin)).setText("You are logged in as " + Settings.getNickname());
            ((ConstraintLayout)findViewById(R.id.clLogin)).setVisibility(View.GONE);
            ((ConstraintLayout) findViewById(R.id.clLoggedin)).setVisibility(View.VISIBLE);
            Log.d("XXX", "Successfully logged in");
            Intent myIntent = new Intent(this, ActivityMain.class);
            startActivity(myIntent);
        } else {
            ((TextView)findViewById(R.id.tvLoginError)).setText("Unable to login. Nickname / password error?");
        }
    }
}
