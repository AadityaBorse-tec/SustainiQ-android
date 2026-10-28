package com.sustainiq.app.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String email;
    public String phone;
    public String passwordHash;  // SHA-256 hex, never plain text
    public String role;          // CITIZEN | ADMIN | FIELD_WORKER
    public String savedDistrict;
    public String savedCity;
    public String savedPinCode;
    public String language;      // en | hi
    public long createdAt;
    public long lastLoginAt;
}
