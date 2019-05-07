package com.rpol.monitor.network;


import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.rpol.monitor.ActivitySettings;
import com.rpol.monitor.helpers.Settings;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.URL;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class Authenticator extends AsyncTask<String, Integer, Boolean> {

    private ActivitySettings parent;

    public Authenticator(ActivitySettings parent) {
        this.parent = parent;
    }

    // Checks Nickname / Password and logs into RPoL
    @Override
    protected Boolean doInBackground(String... args) {

        // Retrieve login information
        String nick = args[0];
        String pwd = args[1];

        Boolean res = false;
        // Set up HTTP Request
        HttpsURLConnection conn = null;
        try {
            URL url = new URL("https://rpol.net/login.cgi");
            conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            List<AbstractMap.SimpleEntry> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry("username", nick));
            params.add(new AbstractMap.SimpleEntry("password", pwd));
            params.add(new AbstractMap.SimpleEntry("specialaction", "Login"));

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(params));
            writer.flush();
            writer.close();
            os.close();

            // Sends request to the server
            conn.connect();

            // Gets the connection results and set up cookies
            String COOKIES_HEADER = "Set-Cookie";
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    Settings.getCookieManager().getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                    // If there's a cookie with UID, then the login was successful
                    if (cookie.startsWith("uid=")) {
                        SharedPreferences sp = parent.getSharedPreferences(Settings.PREFS, parent.MODE_PRIVATE);
                        sp.edit().putString(Settings.UID_COOKIE, cookie).apply();
                        res = true;
                        Settings.logIn();
                        Settings.setNickname(nick);
                    }
                }
            }
            Log.d("RPoLMonitor", "Finished authenticating");
        } catch (Exception e) {
            Log.e("RPoLMonitor", e.toString());
        } finally {
            if (conn != null)
                conn.disconnect();
        }

        return res;
    }

    // Helper function to format POST HTTPS request
    private String getQuery(List<AbstractMap.SimpleEntry> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (AbstractMap.SimpleEntry pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode((String)pair.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode((String)pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    // At the end of the authentication, redirects to the settings screen
    @Override
    protected void onPostExecute(Boolean res) {
        this.parent.post_authenticate(res);
    }

}