package com.baixing.lunchnow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

import android.content.Context;

public class Installation {
	private static String sID = null;
	private static String sInvited = null;
    private static final String INSTALLATION = "INSTALLATION";
    private static final String RECEIVER = "RECEIVER";

    public synchronized static String id(Context context) {
        if (sID == null) {
            File installation = new File(context.getFilesDir(), INSTALLATION);
            try {
                if (!installation.exists())
                    writeFile(installation, UUID.randomUUID().toString());
                sID = readFile(installation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sID;
    }
    
    public synchronized static String addInvited(Context context, String justInvited) {
        if (sInvited == null) {
            File invitedFile = new File(context.getFilesDir(), RECEIVER);
            try {
                if (!invitedFile.exists()) {
                    writeFile(invitedFile, justInvited);
                } else {
					writeFile(invitedFile, readFile(invitedFile) + "," + justInvited);
				}
                sInvited = readFile(invitedFile);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sInvited;
    }

    private static String readFile(File file) throws IOException {
        RandomAccessFile f = new RandomAccessFile(file, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeFile(File file, String content) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        out.write(content.getBytes());
        out.close();
    }
}
