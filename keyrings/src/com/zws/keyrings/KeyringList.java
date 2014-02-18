package com.zws.keyrings;

import java.io.File;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.zws.keyrings.jgkm.Globals;
import com.zws.keyrings.jgkm.KeyringInfo;

public class KeyringList {
	private ArrayAdapter<KeyringInfo> mKeyringAdapter;
	
	public KeyringList(ArrayAdapter<KeyringInfo> keyringAdapter) {
		 mKeyringAdapter = keyringAdapter;
	}
	
	public void createKeyringList() {
		File keyring_dir = new File(Globals.getKeyringDir());
		
		if (!keyring_dir.exists())
			keyring_dir.mkdirs();
		
		String[] filelist = keyring_dir.list(new Filter());
		
		mKeyringAdapter.clear();
		
		if (filelist != null) {
			new ReadFilesTask().execute(filelist);
		} else {
			Log.d(Globals.getTAG(), "No files found.");
		}
	}
	
	private class ReadFilesTask extends AsyncTask<String, KeyringInfo, Integer> {
		
		@Override
		protected Integer doInBackground(String... params) {
			for (String keyring : params) {
				KeyringInfo k;
				try {
					k = new KeyringInfo(new File(Globals.getKeyringDir() + File.separator + keyring));
					publishProgress(k);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (isCancelled()) break;
			}
			return params.length;
		}
		
		protected void onProgressUpdate(KeyringInfo... progress) {
			mKeyringAdapter.add(progress[0]);
			mKeyringAdapter.notifyDataSetChanged();
	     }

		@Override
		protected void onPostExecute(Integer result) {
	         mKeyringAdapter.notifyDataSetChanged();
		}
	}
}
