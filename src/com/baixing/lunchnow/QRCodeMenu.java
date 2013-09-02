package com.baixing.lunchnow;

import java.util.Hashtable;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.Display;
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
