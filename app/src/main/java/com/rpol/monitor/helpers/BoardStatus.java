package com.rpol.monitor.helpers;

import android.util.Log;

import com.rpol.monitor.events.BoardStatusListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardStatus {
    public static final String NOTHING_NEW = "Nothing new.";
    private static BoardStatus instance = null;

    private List<BoardStatusListener> listeners = new ArrayList<>();
    private List<BoardItem> boards;
    private boolean newActivity = false;
    private String message = NOTHING_NEW;

    private BoardStatus() {
        this.boards = new ArrayList<>();
    }

    public static BoardStatus get() {
        if (instance == null)
            instance = new BoardStatus();
        return instance;
    }

    public void addUpdateListener(BoardStatusListener listener) {
        listeners.add(listener);
    }

    public void removeListener(BoardStatusListener listener) {
        listeners.remove(listener);
    }

    public String getMessage() {
        return message;
    }

    public List<BoardItem> getBoards() { return boards; }

    // Checks if there is some new (interesting) activity on RPoL
    // Crafts the notification message that should be sent if there's something new
    public void update_status(List<BoardItem> boards) {
        Log.d("RPoLMonitor", "Updating status board");
        this.boards = boards;
        newActivity = false;
        String newMessage = "";
        for (BoardItem board : boards) {
            // It might not be new since the last notification, but we didn't check it yet
            // so it still needs to appear in the updated notification
            if (board.getStatus() == BoardItem.Status.NewMessage)
                newMessage += "You have new message(s) on " + board.getName() + "\n";
            else if (board.getStatus() == BoardItem.Status.NewPM)
                newMessage += "You have new PM(s) on " + board.getName() + "\n";
            else if (board.getStatus() == BoardItem.Status.NewMessageAndPM)
                newMessage += "You have new Message(s) and PM(s) on " + board.getName() + "\n";
        }

        if ((newMessage != "") && (!newMessage.equals(message)))
            newActivity = true;

        if (newMessage == "") {
            newMessage = NOTHING_NEW;
        }

        message = newMessage;

        // Fires event to warn everything there's an update available
        for (BoardStatusListener listener : listeners) {
            listener.onBoardUpdate(newActivity);
        }
        Log.d("RPoLMonitor", "Detected new activity: " + newActivity);
    }
}
