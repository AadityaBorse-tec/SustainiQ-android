package com.sustainiq.app.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
    entities = {
        UserEntity.class,
        ReportEntity.class,
        StatusHistoryEntity.class,
        ChatMessageEntity.class,
        NotificationEntity.class
    },
    version = 2,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final String DB_NAME = "sustainiq.db";

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `notifications` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "`title` TEXT NOT NULL," +
                "`message` TEXT NOT NULL," +
                "`type` TEXT NOT NULL," +
                "`relatedReportId` TEXT," +
                "`isRead` INTEGER NOT NULL DEFAULT 0," +
                "`createdAt` INTEGER NOT NULL DEFAULT 0)"
            );
        }
    };

    public abstract UserDao          userDao();
    public abstract ReportDao        reportDao();
    public abstract StatusHistoryDao statusHistoryDao();
    public abstract ChatDao          chatDao();
    public abstract NotificationDao  notificationDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DB_NAME)
                            .addMigrations(MIGRATION_1_2)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
