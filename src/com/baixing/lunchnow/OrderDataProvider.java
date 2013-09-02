package com.baixing.lunchnow;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class OrderDataProvider extends ContentProvider {
	
	private static final String TAG = OrderDataProvider.class.getSimpleName();
	
	public static final String AUTHORITY = "com.baixing.lunchnow.OrderData";
	public static final Uri ORDER_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + OrderSQLiteHelper.TABLE_ORDERS);
	
	private SQLiteDatabase database;
	private OrderSQLiteHelper dbHelper;
	private String[] allColumns = { OrderSQLiteHelper.COLUMN_ID, OrderSQLiteHelper.COLUMN_PERSON };
	private OrderDataObserver dataObserver;
	/*
	private static final int PEOPLE = 1;
	private static final int PEOPLE_ID = 2;
	private static final UriMatcher uriMather = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		uriMather.addURI(AUTHORITY, OrderSQLiteHelper.TABLE_ORDERS, PEOPLE);
		uriMather.addURI(AUTHORITY, OrderSQLiteHelper.TABLE_ORDERS + "/#", PEOPLE_ID);
	}
	*/

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Cursor cursor = query(ORDER_CONTENT_URI, allColumns, OrderSQLiteHelper.COLUMN_PERSON + " = '" + values.getAsString(OrderSQLiteHelper.COLUMN_PERSON).trim() + "'", null, null);
		if (cursor.moveToFirst()) {
			Log.w(TAG, "already exists person " + cursor.getString(1));
			int rowId = cursor.getInt(0);
			cursor.close();
			return Uri.parse(ORDER_CONTENT_URI + "/" + rowId);
		}
		long inserId = database.insert(OrderSQLiteHelper.TABLE_ORDERS, null, values);
		if (inserId != -1) {
			Uri newUri = ContentUris.withAppendedId(ORDER_CONTENT_URI, inserId);
			getContext().getContentResolver().notifyChange(newUri, dataObserver);
			return newUri;
		}
		return null;
	}

	@Override
	public boolean onCreate() {
		dbHelper = new OrderSQLiteHelper(getContext());
		database = dbHelper.getWritableDatabase();
		if (database == null) {
			return false;
		}
		if (database.isReadOnly()) {
			database.close();
			database = null;
			return false;
		}
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sql = new SQLiteQueryBuilder();
		sql.setTables(OrderSQLiteHelper.TABLE_ORDERS);
		return sql.query(database, projection, selection, selectionArgs, null, null, sortOrder);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
