package com.example.asmfinal.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.Room;
import android.content.Context;

import com.example.asmfinal.adapter.Expense;
import com.example.asmfinal.dao.ExpenseDao;

@Database(entities = {Expense.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ExpenseDao expenseDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "expense_database")
                            .allowMainThreadQueries() // Không khuyến khích cho production
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
