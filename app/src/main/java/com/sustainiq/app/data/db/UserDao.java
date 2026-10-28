package com.sustainiq.app.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertUser(UserEntity user);

    @Update
    void updateUser(UserEntity user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity findByEmail(String email);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    UserEntity findById(int userId);

    @Query("SELECT * FROM users WHERE role = 'FIELD_WORKER'")
    List<UserEntity> getFieldWorkers();

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int emailExists(String email);

    @Query("UPDATE users SET lastLoginAt = :ts WHERE id = :userId")
    void updateLastLogin(int userId, long ts);

    @Query("UPDATE users SET language = :lang WHERE id = :userId")
    void updateLanguage(int userId, String lang);
}
