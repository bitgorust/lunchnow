package com.baixing.lunchnow;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
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
    
    private static BluetoothAdapter bluetoothAdapter;

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
        
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        Log.d(TAG, "registerReceiver(bluetooth)");
		registerReceiver(bluetoothReceiver, filter);
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
	protected void onDestroy() {
    	Log.d(TAG, "unregisterReceiver(bluetooth)");
    	unregisterReceiver(bluetoothReceiver);
		super.onDestroy();
	}

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
	
	private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "action: " + action);
			if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				Log.d(TAG, "device.getName(): " + device.getName());
				Log.d(TAG, "device.getAddress(): " + device.getAddress());
				if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
					Log.d(TAG, "BluetoothDevice.BOND_BONDED");
					/*ContentValues values = new ContentValues();
					values.put(BluetoothShare.URI, Uri.fromFile(new File("file:///android_asset/baixing_V3.4.4.apk")).toString()); 
					values.put(BluetoothShare.DESTINATION, device.getAddress()); 
					values.put(BluetoothShare.DIRECTION, BluetoothShare.DIRECTION_OUTBOUND);
					values.put(BluetoothShare.TIMESTAMP, System.currentTimeMillis()); 
					getContentResolver().insert(BluetoothShare.CONTENT_URI, values);*/
				}
			}
		}
		
	};
	
	private class AcceptThread extends Thread {
		private final BluetoothServerSocket mServerSocket;
		
		public AcceptThread() {
			BluetoothServerSocket tmp = null;
			try {
				Method m = bluetoothAdapter.getClass().getMethod("listenUsingRfcommOn", new Class[] { int.class });
				tmp = (BluetoothServerSocket) m.invoke(bluetoothAdapter, 1);
			} catch (NoSuchMethodException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}
			mServerSocket = tmp;
		}
		
		public void run() {
			BluetoothSocket socket = null;
			while (true) {
				try {
					socket = mServerSocket.accept();
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
					e.printStackTrace();
					break;
				}
				
				if (socket != null) {
					// to do
					try {
						mServerSocket.close();
					} catch (IOException e) {
						Log.e(TAG, e.getMessage());
						e.printStackTrace();
					}
					break;
				}
			}
		}
		
		public void cancel() {
			try {
				mServerSocket.close();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}
		}
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
