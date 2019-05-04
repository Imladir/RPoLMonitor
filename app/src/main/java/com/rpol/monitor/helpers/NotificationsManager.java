package com.rpol.monitor.helpers;

import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Handles notifications
public class NotificationsManager {
    private static NotificationsManager instance = null;

    private Map<Integer, BoardItem.Status> active_boards;
    public static final int ACTIVE_BOARD_CHANNEL = 0;
    private String message = "";

    private NotificationsManager() {
        this.active_boards = new HashMap<>();
        Log.d("XXX", "New notification manager");
    }

    public static NotificationsManager get() {
        if (instance == null)
            instance = new NotificationsManager();
        return instance;
    }

    // Checks if there is some new (interesting) activity on RPoL
    // Crafts the notification message that should be sent if there's something new
    public Boolean update_notification(List<BoardItem> boards) {
        boolean new_activity = false;
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
                    Log.d("XXX", "Board " + board.getName() + "is " + board.getStatus());
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
        Log.d("XXX", "Detected new activity: " + new_activity);
        return new_activity;
    }

    public String get_message() {
        return message;
    }

}
