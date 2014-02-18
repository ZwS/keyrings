package com.zws.keyrings.jgkm;

import java.io.File;

import android.content.Context;

public class Globals {
	private static String TAG = "Keyrings";
	private static String KeyringDir;
	
	public static void init(Context c) {
		KeyringDir = c.getFilesDir().getAbsolutePath() + File.separator + "keyrings";
	}

	public static String getTAG() {
		return TAG;
	}
	
	public static String getKeyringDir() {
		if (KeyringDir == null) {
			throw new RuntimeException("KeyringDir not initializated.");
		}
		return KeyringDir;
	}
}
