package com.rpol.monitor.helpers;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TableRow;
import android.widget.TextView;

import com.rpol.monitor.ActivityMain;

import java.net.URL;

// Stores pertinent information about a game / board
public class BoardItem {
    private Integer gid;
    private String name;
    private URL url;
    private int posts;
    private Status status;

    public BoardItem(Integer gid, String name, URL url, int posts, String status) {
        this.gid = gid;
        this.name = name;
        this.url = url;
        this.posts = posts;
        switch (status) {
            case "red": this.status = Status.NewMessage; break;
            case "blue": this.status = Status.NewPM; break;
            case "purple": this.status = Status.NewMessageAndPM; break;
            default: this.status = Status.Read;
        }
    }

    public Integer getGid() {
        return gid;
    }

    public String getName() {
        return name;
    }

    public URL getUrl() {
        return url;
    }

    public int getPosts() {
        return posts;
    }

    public Status getStatus() {
        return status;
    }

    public enum Status {Read, NewMessage, NewPM, NewMessageAndPM}

    public TableRow toTableRow(ActivityMain context) {
        TableRow tr = new TableRow(context);
        tr.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        TextView boardName=new TextView(context);
        boardName.setClickable(true);
        boardName.setMovementMethod(LinkMovementMethod.getInstance());
        String text = "<a href='" + url + "'>" + name + "</a>";
        boardName.setText(Html.fromHtml(text));
        boardName.setTextColor(Color.BLACK);
        boardName.setTextSize(15);
        tr.addView(boardName);

        TextView nMessages=new TextView(context);
        nMessages.setText(new String("" + posts));
        nMessages.setTextSize(15);
        if (status != Status.Read) {
            nMessages.setTextColor(getColor());
            nMessages.setTypeface(nMessages.getTypeface(), Typeface.BOLD);
        }
        tr.addView(nMessages);

        return tr;
    }

    public int getColor() {
        int res = 0;
        switch (status) {
            case Read: res = Color.BLACK; break;
            case NewMessage: res = Color.RED; break;
            case NewPM: res = Color.BLUE; break;
            case NewMessageAndPM: res = Color.MAGENTA; break;
        }
        return res;
    }


}
