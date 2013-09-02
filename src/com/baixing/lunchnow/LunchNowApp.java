package com.baixing.lunchnow;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushClient.MiPushClientCallback;

public class LunchNowApp extends Application {
	
	private static final String TAG = LunchNowApp.class.getSimpleName();
	
	private static final String APP_ID = "1006122";
	private static final String APP_KEY = "340100662122";
	private static final String PROMOTER_ID = "PROMOTER_ID";
	
	public static String SERVER_URL = "http://home.wangjianshuo.com/scripts/lunchnow/update.php";
	public static String PROMOTE_URL = "http://192.168.5.109/baixing/lunchnow/promote.php";
	
    String ownUDID = null;
    String mPromoterID = null;
    String mAccountName = null;
    
    private MainActivity activity;
    private Handler mHandler;
	
	public static NotificationCallback callback;
	
	@Override
	public void onCreate() {
		super.onCreate();
		ownUDID = Installation.id(getApplicationContext());
		Log.d("LunchNowApp", "ownUDID: " + ownUDID);
		mAccountName = getUserAccount();
		mPromoterID = getPromoter();
		Log.d("LunchNowApp", "promoter: " + mPromoterID);
		
		init();
	}
	
	public boolean init() {
		callback = new NotificationCallback();
		MiPushClient.initialize(this, APP_ID, APP_KEY, callback);
		return true;
	}
	
	private String getPromoter() {
		try {
			InputStream is = getApplicationContext().getAssets().open(PROMOTER_ID);
			int length = is.available();
			byte[] buffer = new byte[length];
			length = is.read(buffer, 0, length);
			return new String(buffer, 0, length);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getAccount() {
		return mAccountName;
	}
	
	public void setActivity(MainActivity activity) {
		this.activity = activity;
	}
	
	public void setHandler(Handler handler) {
		this.mHandler = handler;
	}
	
	public void promoteInfo(String action) {
		String url = LunchNowApp.PROMOTE_URL + "?action=" + action;
		if (action.equals("join") || action.equals("order")) {
			if (!TextUtils.isEmpty(mPromoterID)) {
				Log.d("LunchNowApp", action + ": " + mPromoterID);
				url += "&promoter=" + mPromoterID + "&receiver=" + mAccountName;
				new NotifyTask().execute(url);
			}
		} else if (action.equals("info")) {
			Log.d("LunchNowApp", "ownUDID: " + Installation.id(getApplicationContext()));
			url += "&udid=" + Installation.id(getApplicationContext());
			new NotifyTask().execute(url);
		}
	}
	
	private class NotifyTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			try {
                HttpURLConnection conn = (HttpURLConnection) new URL(params[0]).openConnection();
                conn.setConnectTimeout(20000);
                conn.setRequestMethod("GET");
                conn.connect();
                
                InputStream is = conn.getInputStream();
                Reader reader = new InputStreamReader(is);
                char[] buffer = new char[1000];
                reader.read(buffer);
                return new String(buffer).trim();
                
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!TextUtils.isEmpty(s)) {
            	Log.d(TAG, s);
            	activity.getMenu().updateInfo(s);
            }
        }
    }
	
	private String getUserAccount() {

        AccountManager am = AccountManager.get(this);
        Account[] accounts;
        accounts = am.getAccounts();

        String accountName = accounts[0].name;

        for(Account ac : accounts) {
            Log.i("LUNCH", ac.name + "/" + ac.type);
            if(ac.type.equals("com.tencent.mm.account")) {
                accountName = ac.name;
                Log.i("LUNCH", ac.name + " is recorded");
            }
        }
        Log.i("LUNCH", accountName);
        return accountName;

    }
	
	public class NotificationCallback extends MiPushClientCallback {

		@Override
		public void onCommandResult(String command, long resultCode, String reason,
				List<String> params) {
			if (command.equals(MiPushClient.COMMAND_SET_ALIAS) && resultCode == ErrorCode.SUCCESS) {
				promoteInfo("join");
			}
		}

		@Override
		public void onInitializeResult(long resultCode, String reason, String regID) {
			if (resultCode == ErrorCode.SUCCESS) {
				Log.d("LunchNowApp", ownUDID);
				MiPushClient.setAlias(getApplicationContext(), ownUDID, null);
			} else {
				Log.d(TAG, reason);
			}
		}

		@Override
		public void onReceiveMessage(String content, String topic, String alias) {
			final String displayInfo = content;
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), displayInfo, Toast.LENGTH_SHORT).show();
					promoteInfo("info");
					activity.update(false);
				}		
			});
		}

		@Override
		public void onSubscribeResult(long resultCode, String reason, String topic) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onUnsubscribeResult(long resultCode, String reason, String topic) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
