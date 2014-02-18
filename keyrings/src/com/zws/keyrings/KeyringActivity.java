package com.zws.keyrings;

import java.io.IOException;

import com.zws.keyrings.R;
import com.zws.keyrings.jgkm.Globals;
import com.zws.keyrings.jgkm.KeyringInfo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;

public class KeyringActivity extends Activity implements OnClickListener {
	private EditText mKeyringName;
	private EditText mPassword;
	private EditText mPasswordConfirm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_keyring_add);
		
		mKeyringName = (EditText) findViewById(R.id.KeyringName);
		mPassword = (EditText) findViewById(R.id.KeyringPassword);
		mPasswordConfirm = (EditText) findViewById(R.id.KeyringPasswordConfirm);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.BtnKeyringAdd : 
			if (validate())
				createKeyring();
			break;
		case R.id.BtnKeyringCancel : 
			onBackPressed();
			break;
		}
	}

	private void createKeyring() {
		try {
			String name = mKeyringName.getText().toString();
			String password = mPassword.getText().toString();
			
			new KeyringInfo(name, password);
		} catch (IOException e) {
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			b.setTitle(getResources().getString(R.string.dialog_title_error))
			.setMessage(getResources().getString(R.string.dialog_message_error))
            .setCancelable(true)
            .setNeutralButton(getResources().getString(android.R.string.ok), null)
            .show();
			Log.d(Globals.getTAG(), e.toString());
			e.printStackTrace();
			return;
		}

		MainActivity.createKeyringList();
		
		onBackPressed();
	}

	private boolean validate() {
		String keyringName = mKeyringName.getText().toString();
		String p = mPassword.getText().toString();
		String pc = mPasswordConfirm.getText().toString();
		if (keyringName.isEmpty()) {
			Toast.makeText(this, R.string.message_no_name, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if (!p.equals(pc)) {
			Toast.makeText(this, R.string.message_password_different, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
	}

}
