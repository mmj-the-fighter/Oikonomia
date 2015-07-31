/*
    Description: An easy to use personal expense tracker
	Author: mmj-the-fighter 
    Copyright (C) 2015 mmj-the-fighter

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see http://www.gnu.org/licenses/.
*/

package com.xyzsoftware.oikonomia;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBConnector {
	//Debug tag
	static final String TAG = "XYZSOFTWARE::DBConnector";
	
	//Database
    static final String DATABASE_NAME = "OikonomiaDB";
    static final String EXPENSES_TABLE = "expenses";
    static final int DATABASE_VERSION = 2;
    
	//Expenses Table Columns
    static final String KEY_ID = "_id";
    static final String KEY_DATE = "date";
    static final String KEY_ITEMNAME = "itemname";
    static final String KEY_QUANTITY = "quantity";
    static final String KEY_AMOUNT = "amount";
    
    //Create Table command string
    static final String DATABASE_CREATE_COMMAND =
            "create table "+EXPENSES_TABLE+" ("
            + KEY_ID + " integer primary key autoincrement, "
            + KEY_DATE + " TEXT, "
            + KEY_ITEMNAME + " TEXT, "
            + KEY_QUANTITY + " FLOAT, "
            + KEY_AMOUNT + " FLOAT);";
    
    final Context context;

    DatabaseHelper DBHelper;
    SQLiteDatabase db;

    public DBConnector(Context ctx)
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }
    
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            try {
                db.execSQL(DATABASE_CREATE_COMMAND);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS expenses");
            onCreate(db);
        }
    }    

    //Opens the database
    public DBConnector open() throws SQLException 
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //Closes the database
    public void close() 
    {
        DBHelper.close();
    }
    
    //insert an expense into the database
    public long insertRecordIntoExpenses(
    		String date, 
    		String itemName, 
    		float quantity, 
    		float amount) 
    {
        ContentValues v = new ContentValues();
        v.put(KEY_DATE, date);
        v.put(KEY_ITEMNAME, itemName);
        v.put(KEY_QUANTITY, quantity);
        v.put(KEY_AMOUNT, amount);
        return db.insert(EXPENSES_TABLE, null, v);
    }    
    
    public Cursor getAllRowsFromExpenses()
    {
        return db.query(EXPENSES_TABLE, new String[] {KEY_ID, KEY_DATE,
        		KEY_ITEMNAME,KEY_QUANTITY,KEY_AMOUNT}, null, null, null, null, null);
    }
    
    public void eraseExpenses()
    {
    	db.execSQL("DROP TABLE IF EXISTS expenses");
        try {
            db.execSQL(DATABASE_CREATE_COMMAND);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	
    }
}
