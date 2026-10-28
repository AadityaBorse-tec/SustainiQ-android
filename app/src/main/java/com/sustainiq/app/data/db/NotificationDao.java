package com.sustainiq.app.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NotificationEntity notification);

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    LiveData<List<NotificationEntity>> getAll();

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    LiveData<Integer> getUnreadCount();

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    void markRead(int id);

    @Query("UPDATE notifications SET isRead = 1")
    void markAllRead();

    @Query("SELECT COUNT(*) FROM notifications")
    int count();
}
