package com.baixing.lunchnow;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import android.R.menu;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class MainActivity extends Activity {
	
	private static final String TAG = MainActivity.class.getSimpleName();

    Button mLunchToday;
    static TextView mWhoIsIn;
    LunchNowApp app;
    
    QRCodeMenu qrCodeMenu;
    String promoter;
    boolean isClicked;
    
    private OrderDataObserver dataObserver;
    
    //public static final int MSG_INFO = 1;
    public static final int MSG_CONTENT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWhoIsIn = (TextView) findViewById(R.id.text_whoIsIn);
        mLunchToday = (Button) findViewById(R.id.button_lunchToday);
        
        //mLunchToday.setEnabled(false);
        app = (LunchNowApp) getApplication();
        app.setActivity(this);
        app.setHandler(mHandler);
        
        qrCodeMenu = new QRCodeMenu(this, SlidingMenu.SLIDING_CONTENT);
        isClicked = false;
        dataObserver = new OrderDataObserver(this, mHandler);
        
        app.promoteInfo("info");

        mLunchToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	isClicked = true;
            	update(true);
            }
        });

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mWhoIsIn.setText(tm.getDeviceId() + " " + tm.getLine1Number() + app.getAccount());

        update(false);
    }
    
    private static Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_CONTENT:
				mWhoIsIn.setText((String)msg.obj);
				break;
			default:
				break;
			}
		}
		
    };
    
    @Override
	protected void onPause() {
		getContentResolver().unregisterContentObserver(dataObserver);
		super.onPause();
	}

	@Override
	protected void onResume() {
		getContentResolver().registerContentObserver(OrderDataProvider.ORDER_CONTENT_URI, true, dataObserver);
		super.onResume();
	}

	/*
    private void createShortcut() {
		Intent shortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		//shortcutIntent.putExtra("duplicate", true);
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "funny");
		try {
			shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, QRCodeMenu.encodeAsBitmap("test", BarcodeFormat.QR_CODE, 250, 250));
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendBroadcast(shortcutIntent);
	}
    */
    public QRCodeMenu getMenu() {
    	return qrCodeMenu;
    }

    public void update(boolean joining) {
        mWhoIsIn.setText("....");
        FetchURLTask task = new FetchURLTask();
        String url = LunchNowApp.SERVER_URL;
        String actionStr;
        if(joining)
            actionStr = "join";
        else
            actionStr = "update";
        try {
            url = LunchNowApp.SERVER_URL + "?" + actionStr + "=" + URLEncoder.encode(app.getAccount(), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        task.execute(url);
    }

    private class FetchURLTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(20000);
                conn.setRequestMethod("GET");
                conn.setReadTimeout(10000);
                conn.connect();

                InputStream is = conn.getInputStream();
                Reader reader = new InputStreamReader(is);
                char[] buffer = new char[1000];
                reader.read(buffer);
                return new String(buffer);

            } catch(IOException e){
                return getResources().getString(R.string.msgNetworkError);
            } catch(Exception e) {
                e.printStackTrace();
                Log.e("LunchNow", e.toString());
                return "Network Error." + e.toString();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            toggleOrderStatus(s.charAt(0) == '*');
            
            mWhoIsIn.setText(s);
            
            String[] parts = s.trim().split("\n");
            if (parts.length < 2) {
            	return;
            }
            String[] people = parts[1].split(",");
            if (people.length < 1) {
            	return;
            }
            ContentResolver contentResolver = getContentResolver();
            for (String person : people) {
            	ContentValues values = new ContentValues();
            	values.put(OrderSQLiteHelper.COLUMN_PERSON, person.trim());
            	contentResolver.insert(OrderDataProvider.ORDER_CONTENT_URI, values);
            }
        }

    }

    private void toggleOrderStatus(boolean confirmed) {
        if(confirmed) {
            mLunchToday.setBackgroundResource(R.drawable.ic_dingcan_finished);
            if (isClicked) {
            	app.promoteInfo("order");
            	isClicked = false;
            }
            //mLunchToday.setEnabled(false);
        } else {
            mLunchToday.setBackgroundResource(R.drawable.ic_dingcan_normal);
            //mLunchToday.setEnabled(true);
        }
    }
        
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
