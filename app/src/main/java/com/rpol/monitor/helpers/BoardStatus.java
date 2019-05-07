package com.rpol.monitor.helpers;

import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardStatus {
    private static BoardStatus instance = null;
    private Map<Integer, BoardItem.Status> active_boards;
    private boolean new_activity = false;
    private String message = "";

    private BoardStatus() {
        this.active_boards = new HashMap<>();
    }

    public static BoardStatus get() {
        if (instance == null)
            instance = new BoardStatus();
        return instance;
    }

    public boolean hasNew_activity() {
        return new_activity;
    }

    public String getMessage() {
        return message;
    }

    // Checks if there is some new (interesting) activity on RPoL
    // Crafts the notification message that should be sent if there's something new
    public void update_status(List<BoardItem> boards) {
        new_activity = false;
        message = "";
        for (BoardItem board : boards) {
            // We read the messages here, the board is not new anymore
            if (board.getStatus() == BoardItem.Status.Read && active_boards.containsKey(board.getGid())) {
                active_boards.remove(board.getGid());
            }
            // There's some new acitivity here
            else if (board.getStatus() != BoardItem.Status.Read) {
                if (!active_boards.containsKey(board.getGid())
                        || (active_boards.get(board.getGid()) != board.getStatus())) {
                    new_activity = true;
                    Log.d("RPoLMonitor", "Board " + board.getName() + "is " + board.getStatus());
                    active_boards.put(board.getGid(), board.getStatus());
                }
            }
            // It might not be new since the last notification, but we didn't check it yet
            // so it still needs to appear in the updated notification
            if (board.getStatus() == BoardItem.Status.NewMessage)
                message += "You have new message(s) on " + board.getName() + "\n";
            else if (board.getStatus() == BoardItem.Status.NewPM)
                message += "You have new PM(s) on " + board.getName() + "\n";
            else if (board.getStatus() == BoardItem.Status.NewMessageAndPM)
                message += "You have new Message(s) and PM(s) on " + board.getName() + "\n";
        }
        Log.d("RPoLMonitor", "Detected new activity: " + new_activity);
    }
}
