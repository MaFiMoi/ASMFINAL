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

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "AppManager.db";
    private static final int DATABASE_VERSION = 4;

    // Users table
    private static final String TABLE_USER = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_FULLNAME = "fullname";
    private static final String COLUMN_DATE_OF_BIRTH = "date_of_birth";
    private static final String COLUMN_GENDER = "gender";

    // Expenses table
    public static final String TABLE_EXPENSE = "expenses";
    public static final String COLUMN_EXPENSE_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_CATEGORY = "category";

    private static final String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_EMAIL + " TEXT UNIQUE,"
            + COLUMN_PASSWORD + " TEXT,"
            + COLUMN_FULLNAME + " TEXT,"
            + COLUMN_DATE_OF_BIRTH + " TEXT,"
            + COLUMN_GENDER + " TEXT"
            + ")";

    private static final String CREATE_EXPENSE_TABLE = "CREATE TABLE " + TABLE_EXPENSE + "("
            + COLUMN_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_TITLE + " TEXT,"
            + COLUMN_AMOUNT + " INTEGER,"
            + COLUMN_DATE + " TEXT,"
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE);
        onCreate(db);
    }

    // User methods
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
        String[] columns = {COLUMN_ID};
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
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_EMAIL + " = ?";
        String[] args = {email};
        Cursor cursor = db.query(TABLE_USER, columns, selection, args, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER, null);
        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
                user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
                user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULLNAME)));
                user.setDateOfBirth(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_OF_BIRTH)));
                user.setGender(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER)));
                userList.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return userList;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;
        String[] columns = {COLUMN_ID, COLUMN_EMAIL, COLUMN_PASSWORD, COLUMN_FULLNAME, COLUMN_DATE_OF_BIRTH, COLUMN_GENDER};
        String selection = COLUMN_EMAIL + " = ?";
        String[] args = {email};
        Cursor cursor = db.query(TABLE_USER, columns, selection, args, null, null, null);
        if (cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
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

    // Expense methods... (giữ nguyên)
    public long insertExpense(String title, int amount, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DATE, date);
        long id = db.insert(TABLE_EXPENSE, null, values);
        db.close();
        return id;
    }

    public long insertExpenseWithCategory(String title, int amount, String date, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_CATEGORY, category);
        long id = db.insert(TABLE_EXPENSE, null, values);
        db.close();
        return id;
    }

    public List<Expense> getExpensesForMonth(String startDate, String endDate) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_EXPENSE + " WHERE " + COLUMN_DATE + " BETWEEN ? AND ?";
        Cursor cursor = db.rawQuery(query, new String[]{startDate, endDate});
        if (cursor.moveToFirst()) {
            do {
                Expense expense = new Expense(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
                );
                expenses.add(expense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }

    public List<Expense> getExpensesByCategory(String category, String startDate, String endDate) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_EXPENSE + " WHERE " + COLUMN_CATEGORY + " = ? AND " + COLUMN_DATE + " BETWEEN ? AND ?";
        Cursor cursor = db.rawQuery(query, new String[]{category, startDate, endDate});
        if (cursor.moveToFirst()) {
            do {
                Expense expense = new Expense(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
                );
                expenses.add(expense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }

    public int getTotalExpensesByCategory(String category, String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_EXPENSE + " WHERE " + COLUMN_CATEGORY + " = ? AND " + COLUMN_DATE + " BETWEEN ? AND ?";
        Cursor cursor = db.rawQuery(query, new String[]{category, startDate, endDate});
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    public void deleteExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXPENSE, COLUMN_EXPENSE_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
    /**
     * Lấy tổng chi tiêu theo danh mục trong một khoảng thời gian cụ thể.
     *
     * @param startDate Ngày bắt đầu (định dạng YYYY-MM-DD).
     * @param endDate   Ngày kết thúc (định dạng YYYY-MM-DD).
     * @return Map chứa tên danh mục và tổng chi tiêu.
     */
    public java.util.Map<String, Float> getSpendingByCategory(String startDate, String endDate) {
        java.util.Map<String, Float> spendingMap = new java.util.HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COLUMN_CATEGORY + ", SUM(" + COLUMN_AMOUNT + ") " +
                "FROM " + TABLE_EXPENSE +
                " WHERE " + COLUMN_DATE + " BETWEEN ? AND ?" +
                " GROUP BY " + COLUMN_CATEGORY;

        Cursor cursor = db.rawQuery(query, new String[]{startDate, endDate});

        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                float totalAmount = cursor.getFloat(cursor.getColumnIndexOrThrow("SUM(" + COLUMN_AMOUNT + ")"));
                spendingMap.put(category, totalAmount);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return spendingMap;
    }

}