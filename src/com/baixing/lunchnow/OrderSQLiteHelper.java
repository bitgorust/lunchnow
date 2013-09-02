package com.baixing.lunchnow;
import java.util.Date;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.DateFormat;
import android.util.Log;


public class OrderSQLiteHelper extends SQLiteOpenHelper {
	
	private static final String TAG = OrderSQLiteHelper.class.getSimpleName();
	
	public static final String TABLE_ORDERS = "orders";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_PERSON = "person";
	
	private static final String DATABASE_NAME = "order.db";
	private static int DATABASE_VERSION = getVersion();
	
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_ORDERS + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_PERSON
			+ " text not null);";

	public OrderSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + " will destroy all old data");
		database.execSQL("drop table if exists " + TABLE_ORDERS);
		onCreate(database);
	}
	
	private static int getVersion() {
		return Integer.parseInt(DateFormat.format("yyyyMMdd", new Date()).toString());
	}

}
