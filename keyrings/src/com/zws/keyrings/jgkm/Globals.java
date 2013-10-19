package com.zws.keyrings.jgkm;

import android.os.Environment;

public class Globals {
	public final static String TAG = "Keyrings";
	public final static String keyring_dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/keyrings/";
}
