package com.example.oikonomia;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBConnector {
    static final String DATABASE_NAME = "OikonomiaDB2";
    static final String TRANSACTIONS_TABLE = "transactions";
    static final int DATABASE_VERSION = 2;

    static final String KEY_ID = "_id";
    static final String KEY_IS_INCOME = "isincome";
    static final String KEY_DATE = "date";
    static final String KEY_DESC = "description";
    static final String KEY_AMOUNT = "amount";

    static final String DATABASE_CREATE_COMMAND =
            "create table " + TRANSACTIONS_TABLE + " ("
                    + KEY_ID + " integer primary key autoincrement, "
                    + KEY_IS_INCOME + " integer, "
                    + KEY_DATE + " TEXT, "
                    + KEY_DESC + " TEXT, "
                    + KEY_AMOUNT + " FLOAT);";
    static String[] transaction_table_keys = new String[]{
            KEY_ID, KEY_IS_INCOME, KEY_DATE, KEY_DESC, KEY_AMOUNT
    };

    final Context context;

    DatabaseHelper DBHelper;
    SQLiteDatabase db;

    public DBConnector(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    public DBConnector open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        DBHelper.close();
    }

    public long insertRecordIntoTransactions(OikonomiaRecord orec) {
        ContentValues v = new ContentValues();
        v.put(KEY_IS_INCOME, orec.isIncomeInt);
        v.put(KEY_DATE, orec.getIsoDate());
        v.put(KEY_DESC, orec.description);
        v.put(KEY_AMOUNT, orec.amount);
        return db.insert(TRANSACTIONS_TABLE, null, v);
    }

    public Cursor query(String queryString) {
        return db.rawQuery(queryString, null);
    }

    public int updateRecordInTransactions(OikonomiaRecord orec) {
        ContentValues v = new ContentValues();
        v.put(KEY_IS_INCOME, orec.isIncomeInt);
        v.put(KEY_DATE, orec.getIsoDate());
        v.put(KEY_DESC, orec.description);
        v.put(KEY_AMOUNT, orec.amount);
        return db.update(TRANSACTIONS_TABLE, v, "_id=?", new String[]{orec.rowId});
    }

    public Cursor getAllRowsFromTransactions() {
        String orderBy = KEY_DATE + " DESC";
        return db.query(TRANSACTIONS_TABLE, transaction_table_keys, null, null, null, null, orderBy);
    }

    public Cursor getRowFromTransactions(String rowId) {
        Cursor cursor = db.query(
                TRANSACTIONS_TABLE, new String[]{KEY_ID, KEY_IS_INCOME, KEY_DATE, KEY_DESC, KEY_AMOUNT}, KEY_ID + "=?",
                new String[]{String.valueOf(rowId)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        return cursor;
    }

    public void eraseTransactions() {
        db.execSQL("DROP TABLE IF EXISTS transactions");
        try {
            db.execSQL(DATABASE_CREATE_COMMAND);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteRow(String rowId) {
        return db.delete(TRANSACTIONS_TABLE, KEY_ID + "=" + rowId, null) > 0;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(DATABASE_CREATE_COMMAND);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
//                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS transactions");
            onCreate(db);
        }
    }
}
