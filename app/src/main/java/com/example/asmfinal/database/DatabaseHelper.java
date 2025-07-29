package com.example.asmfinal.database;

import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.asmfinal.model.User;

/**
 * Lớp DatabaseHelper quản lý cơ sở dữ liệu SQLite
 * Chịu trách nhiệm tạo, nâng cấp và tương tác với database
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    // Thông tin database
    private static final String DATABASE_NAME = "UserManager.db";
    private static final int DATABASE_VERSION = 1;

    // Tên bảng
    private static final String TABLE_USER = "users";

    // Tên các cột
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_FULLNAME = "fullname";

    // Câu lệnh tạo bảng
    private static final String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USERNAME + " TEXT UNIQUE,"
            + COLUMN_PASSWORD + " TEXT,"
            + COLUMN_FULLNAME + " TEXT"
            + ")";

    /**
     * Constructor của DatabaseHelper
     * @param context Context của ứng dụng
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "DatabaseHelper: Khởi tạo database");
    }

    /**
     * Được gọi khi database được tạo lần đầu
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng users
        db.execSQL(CREATE_USER_TABLE);
        Log.d(TAG, "onCreate: Tạo bảng users thành công");
    }

    /**
     * Được gọi khi cần nâng cấp database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng cũ nếu tồn tại và tạo bảng mới
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
        Log.d(TAG, "onUpgrade: Nâng cấp database từ " + oldVersion + " lên " + newVersion);
    }

    /**
     * Thêm user mới vào database
     * @param user Đối tượng User cần thêm
     * @return ID của user mới hoặc -1 nếu thất bại
     */
    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Đưa thông tin user vào ContentValues
        values.put(COLUMN_USERNAME, user.getUsername());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_FULLNAME, user.getFullName());

        // Thêm hàng mới, trả về ID của hàng hoặc -1 nếu lỗi
        long id = db.insert(TABLE_USER, null, values);
        db.close();

        Log.d(TAG, "addUser: Thêm user " + user.getUsername() + " với ID = " + id);
        return id;
    }

    /**
     * Kiểm tra đăng nhập
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     * @return true nếu thông tin đăng nhập đúng, false nếu sai
     */
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {COLUMN_ID};
        String selection = COLUMN_USERNAME + " = ?" + " AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(
                TABLE_USER,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        int count = cursor.getCount();
        cursor.close();
        db.close();

        Log.d(TAG, "checkUser: Kiểm tra user " + username + " - Kết quả: " + (count > 0));
        return count > 0;
    }

    /**
     * Kiểm tra username đã tồn tại chưa
     * @param username Tên đăng nhập cần kiểm tra
     * @return true nếu username đã tồn tại, false nếu chưa
     */
    public boolean checkUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {COLUMN_ID};
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(
                TABLE_USER,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        int count = cursor.getCount();
        cursor.close();
        db.close();

        Log.d(TAG, "checkUsername: Kiểm tra username " + username + " - Kết quả: " + (count > 0));
        return count > 0;
    }

    /**
     * Lấy tất cả users từ database
     * @return Danh sách các user
     */
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_USER;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)));
                user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
                user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULLNAME)));

                userList.add(user);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        Log.d(TAG, "getAllUsers: Lấy " + userList.size() + " users từ database");
        return userList;
    }

    /**
     * Lấy thông tin user theo username
     * @param username Tên đăng nhập cần tìm
     * @return Đối tượng User hoặc null nếu không tìm thấy
     */
    public User getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        String[] columns = {COLUMN_ID, COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_FULLNAME};
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(
                TABLE_USER,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
            user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULLNAME)));
        }

        cursor.close();
        db.close();

        Log.d(TAG, "getUserByUsername: " + (user != null ? "User found" : "User not found"));
        return user;
    }

}

