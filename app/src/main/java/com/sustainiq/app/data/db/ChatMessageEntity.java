package com.sustainiq.app.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatMessageEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String text;
    public boolean isBot;        // true = bot, false = user
    public long timestamp;
    public String actionType;    // NAVIGATE | REPORT | STATUS | INFO | null
    public String actionPayload; // e.g. screen name or report ID
}
