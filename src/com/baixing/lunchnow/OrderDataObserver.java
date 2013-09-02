package com.baixing.lunchnow;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;

public class OrderDataObserver extends ContentObserver {
	
	private Context mContext;
	private Handler mHandler;

	public OrderDataObserver(Context context, Handler handler) {
		super(handler);
		mContext = context;
		mHandler = handler;
	}

	@Override
	public void onChange(boolean selfChange) {
		Cursor cursor = mContext.getContentResolver().query(OrderDataProvider.ORDER_CONTENT_URI, null, null, null, null);
		if (cursor != null) {
			StringBuilder content = new StringBuilder();
			int count = 0;
			while (cursor.moveToNext()) {
				count++;
				content.append(cursor.getString(1) + ", ");
			}
			mHandler.obtainMessage(MainActivity.MSG_CONTENT, count, count, content.toString()).sendToTarget();
		}
		cursor.close();
		
		super.onChange(selfChange);
	}

}
