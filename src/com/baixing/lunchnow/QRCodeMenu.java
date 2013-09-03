package com.baixing.lunchnow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class QRCodeMenu extends SlidingMenu {
	
	private static final String TAG = QRCodeMenu.class.getSimpleName();

	int qrCodeSize;
	int menuWidth;
	ImageView qrCode;
	TextView textInvited;
	LunchNowApp app;
	
	Button btnBluetooth;
	
	public QRCodeMenu(Activity activity, int slideStyle) {
		super(activity, slideStyle);
		app = (LunchNowApp) activity.getApplication();
		Display display = activity.getWindowManager().getDefaultDisplay();
		qrCodeSize = display.getWidth() < display.getHeight() ? display.getWidth() / 2 : display.getHeight() / 2;
		menuWidth = display.getWidth() * 2 / 3;
		
		setMode(SlidingMenu.LEFT);
		setBehindWidth(menuWidth);
		setFadeDegree(0.35f);
		setMenu(R.layout.menu);
		
		textInvited = (TextView) findViewById(R.id.text_invited);
		
		qrCode = (ImageView) findViewById(R.id.menu_qrcode);
		try {
			qrCode.setImageBitmap(encodeAsBitmap(LunchNowApp.PROMOTE_URL + "?action=download&udid=" + Installation.id(app), BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize));
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		btnBluetooth = (Button) findViewById(R.id.btn_bluetooth);
		btnBluetooth.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				String apkName = Environment.getExternalStorageDirectory().getPath() + "/LunchNow-" + Installation.id(getContext()) + ".apk";
				
				//if (!new File(apkName).exists()) {
					PackageManager pm = getContext().getPackageManager();
					List<PackageInfo> pkginfo_list = pm
							.getInstalledPackages(PackageManager.GET_ACTIVITIES);
					List<ApplicationInfo> appinfo_list = pm
							.getInstalledApplications(0);
					String originPath = null;
					for (int x = 0; x < pkginfo_list.size(); x++) {
						if (appinfo_list.get(x).publicSourceDir
								.contains("com.baixing.lunchnow")) {
							originPath = appinfo_list.get(x).publicSourceDir; 
						}
					}
					
					if (originPath != null) {
						try {
							InputStream is = new FileInputStream(new File(originPath));
							/*InputStream is = getContext().getAssets().open(
									"LunchNow.apk");*/
							int length = is.available();
							Log.d(TAG, "is.available: " + length);
							if (length > 0) {
								FileOutputStream fos = new FileOutputStream(
										new File(apkName));
								byte[] buffer = new byte[length];
								while (true) {
									length = is.read(buffer, 0, length);
									if (length == -1) {
										break;
									}
									fos.write(buffer, 0, length);
								}
								fos.close();
							}
							is.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						Log.e(TAG, "No apk found");
					}
				//}
				
				
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				intent.setType("*/*");
				intent.setClassName("com.android.bluetooth" , "com.android.bluetooth.opp.BluetoothOppLauncherActivity");
				intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(apkName)));
				getContext().startActivity(intent);
				
				/*BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				if (mBluetoothAdapter != null) {
					Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
					discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
					getContext().startActivity(discoverableIntent);
				}*/
			}
		});
		
	}
	
	public void updateInfo(String invited) {
		textInvited.setText("ÄúÒÑ¾­ÑûÇëÁË£º\n" + invited);
	}
	
	static Bitmap encodeAsBitmap(String contents, BarcodeFormat format,
			int desiredWidth, int desiredHeight) throws WriterException {
		Hashtable<EncodeHintType, String> hints = null;
		String encoding = guessAppropriateEncoding(contents);
		if (encoding != null) {
			hints = new Hashtable<EncodeHintType, String>(2);
			hints.put(EncodeHintType.CHARACTER_SET, encoding);
		}
		MultiFormatWriter writer = new MultiFormatWriter();
		BitMatrix result = writer.encode(contents, format, desiredWidth,
				desiredHeight, hints);
		int width = result.getWidth();
		int height = result.getHeight();
		int[] pixels = new int[width * height];
		// All are 0, or black, by default
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				pixels[offset + x] = result.get(x, y) ? Color.BLACK : Color.WHITE;
			}
		}
		
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}
	
	private static String guessAppropriateEncoding(CharSequence contents) {
		// Very crude at the moment
		for (int i = 0; i < contents.length(); i++) {
			if (contents.charAt(i) > 0xFF) {
				return "UTF-8";
			}
		}
		return null;
	}
}
