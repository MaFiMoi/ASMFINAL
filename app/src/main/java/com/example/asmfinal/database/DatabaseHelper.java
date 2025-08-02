package com.example.asmfinal.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.asmfinal.adapter.Expense;
import com.example.asmfinal.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "AppManager.db";
    private static final int DATABASE_VERSION = 6;

    // Users table
    public static final String TABLE_USER = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_FULLNAME = "fullname";
    public static final String COLUMN_DATE_OF_BIRTH = "date_of_birth";
    public static final String COLUMN_GENDER = "gender";

    // Expenses table
    public static final String TABLE_EXPENSE = "expenses";
    public static final String COLUMN_EXPENSE_ID = "id";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_CATEGORY = "category";

    private static final String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_EMAIL + " TEXT UNIQUE,"
            + COLUMN_PASSWORD + " TEXT,"
            + COLUMN_FULLNAME + " TEXT,"
            + COLUMN_DATE_OF_BIRTH + " TEXT,"
            + COLUMN_GENDER + " TEXT"
            + ")";

    private static final String CREATE_EXPENSE_TABLE = "CREATE TABLE " + TABLE_EXPENSE + "("
            + COLUMN_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_DESCRIPTION + " TEXT,"
            + COLUMN_AMOUNT + " REAL,"
            + COLUMN_DATE + " TEXT,"
            + COLUMN_TIME + " TEXT,"
            + COLUMN_CATEGORY + " TEXT"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "DatabaseHelper: Khởi tạo database");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_EXPENSE_TABLE);
        Log.d(TAG, "onCreate: Tạo bảng users và expenses thành công");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade: Nâng cấp database từ " + oldVersion + " lên " + newVersion + ". Xóa và tạo lại bảng.");
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE);
            onCreate(db);
        }
    }

    // --- User methods (giữ nguyên) ---
    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_FULLNAME, user.getFullName());
        values.put(COLUMN_DATE_OF_BIRTH, user.getDateOfBirth());
        values.put(COLUMN_GENDER, user.getGender());
        long id = db.insert(TABLE_USER, null, values);
        db.close();
        return id;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] args = {email, password};
        Cursor cursor = db.query(TABLE_USER, columns, selection, args, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_EMAIL + " = ?";
        String[] args = {email};
        Cursor cursor = db.query(TABLE_USER, columns, selection, args, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;
        String[] columns = {COLUMN_USER_ID, COLUMN_EMAIL, COLUMN_PASSWORD, COLUMN_FULLNAME, COLUMN_DATE_OF_BIRTH, COLUMN_GENDER};
        String selection = COLUMN_EMAIL + " = ?";
        String[] args = {email};
        Cursor cursor = db.query(TABLE_USER, columns, selection, args, null, null, null);
        if (cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
            user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULLNAME)));
            user.setDateOfBirth(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_OF_BIRTH)));
            user.setGender(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER)));
        }
        cursor.close();
        db.close();
        return user;
    }

    // --- Expense methods ---
    public long insertExpense(String description, double amount, String date, String time, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_CATEGORY, category);
        long id = db.insert(TABLE_EXPENSE, null, values);
        db.close();
        return id;
    }

    public long updateExpense(int id, String category, String description, double amount, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TIME, time);

        long rowsAffected = db.update(TABLE_EXPENSE, values, COLUMN_EXPENSE_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected;
    }

    public void deleteExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXPENSE, COLUMN_EXPENSE_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Đã sửa lại constructor để khớp với một constructor giả định trong lớp Expense
    public Expense getExpenseById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Expense expense = null;
        String query = "SELECT * FROM " + TABLE_EXPENSE + " WHERE " + COLUMN_EXPENSE_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});

        if (cursor.moveToFirst()) {
            expense = new Expense(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                    null, // categoryName sẽ được xử lý ở fragment
                    (int) cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME))
            );
        }
        cursor.close();
        db.close();
        return expense;
    }

    // Đã sửa lại constructor để khớp với một constructor giả định trong lớp Expense
    public List<Expense> getExpensesForMonth(String startDate, String endDate) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_EXPENSE + " WHERE " + COLUMN_DATE + " BETWEEN ? AND ?";
        Cursor cursor = db.rawQuery(query, new String[]{startDate, endDate});
        if (cursor.moveToFirst()) {
            do {
                Expense expense = new Expense(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                        null, // categoryName sẽ được xử lý ở fragment
                        (int) cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME))
                );
                expenses.add(expense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }

    public Map<String, Float> getSpendingByCategory(String startDate, String endDate) {
        Map<String, Float> spendingMap = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COLUMN_CATEGORY + ", SUM(" + COLUMN_AMOUNT + ") as total_amount " +
                "FROM " + TABLE_EXPENSE +
                " WHERE " + COLUMN_DATE + " BETWEEN ? AND ?" +
                " GROUP BY " + COLUMN_CATEGORY;

        Cursor cursor = db.rawQuery(query, new String[]{startDate, endDate});

        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                float totalAmount = cursor.getFloat(cursor.getColumnIndexOrThrow("total_amount"));
                spendingMap.put(category, totalAmount);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return spendingMap;
    }
}