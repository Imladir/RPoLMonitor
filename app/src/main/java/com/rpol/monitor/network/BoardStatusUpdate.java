package com.rpol.monitor.network;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import com.rpol.monitor.ActivityMain;
import com.rpol.monitor.helpers.BoardItem;
import com.rpol.monitor.helpers.Settings;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class BoardStatusUpdate extends AsyncTask<Void, Integer, List<BoardItem>> {

    private ActivityMain parent;

    public BoardStatusUpdate(ActivityMain parent) {
        this.parent = parent;
    }

    // Reads the homepage, parses the data, and retrieves the current boards states
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected List<BoardItem> doInBackground(Void... args) {
        Log.d("XXX", "Beginning retrieving boards data");
        List<BoardItem> res = new ArrayList<BoardItem>();

        // Set up HTTP Request
        HttpsURLConnection conn = null;
        try {
            Log.d("XXX", "Building connection");
            URL url = new URL("https://rpol.net/");
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestProperty("Cookie",
                    TextUtils.join(";", Settings.getCookieManager().getCookieStore().getCookies()));
            conn.connect();
            Log.d("XXX", "Reading homepage");
            String page_data = readStream(conn.getInputStream());

            // Parsing data for board names & status
            Log.d("XXX", "Parsing homepage");
            List<String> rows = new ArrayList<String>();

            // Keep only the data regarding the games / boards
            Pattern pattern = Pattern.compile("<tr class='highlight'.*?<\\/tr>");
            Matcher matcher = pattern.matcher(page_data);

            while (matcher.find()) {
                rows.add(matcher.group(0));
            }

            // Get the board / game information
            pattern = Pattern.compile(".*?href=\\\"([^\\\\s]*?)\\\" title='.*?'>(.*?)<\\/a>.*?nowrap.*?>([a-zA-Z0-9]+).*?(?:(red|blue|purple)'><b>)?(\\d+)[^0-9]*$");
            for (String row : rows) {
                matcher = pattern.matcher(row);
                while (matcher.find()) {
                    Pattern gid_pattern = Pattern.compile("\\d+");
                    Matcher gid_matcher = gid_pattern.matcher(matcher.group(1));
                    gid_matcher.find();
                    int gid = Integer.parseInt(gid_matcher.group(0));
                    String name = matcher.group(2);
                    URL game_url = new URL("https://www.rpol.net" + matcher.group(1));
                    int posts = Integer.parseInt(matcher.group(5));
                    String status = matcher.group(4);
                    if (status == null)
                        status = "black";
                    BoardItem item = new BoardItem(gid, name, game_url, posts, status);
                    res.add(item);
                }
            }

        } catch (Exception e) {
            Log.e("XXX", e.toString());
        } finally {
            if (conn != null)
                conn.disconnect();
        }

        return res;
    }

    // Transforms the source stream into a string
    private String readStream(InputStream stream) {
        StringBuffer response = new StringBuffer();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            Log.e("Parse error:", e.toString());
        }
        return response.toString();
    }

    @Override
    protected void onPostExecute(List<BoardItem> boards) {
        parent.update_boards(boards);
    }
}
