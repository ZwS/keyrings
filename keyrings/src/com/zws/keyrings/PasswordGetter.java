package com.zws.keyrings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.zws.keyrings.R;
import com.zws.keyrings.jgkm.KeyringInfo;


public class PasswordGetter extends Activity implements OnClickListener {
	public final static String KEYRING_POSITION = "keyring position";
	public final static String KEYRING_PASSWORD = "keyring password";
	public final static String CANCELED = "canceled";
	
	private EditText mPassword;

	private int position;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPassword = new EditText(this);
		mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		
		Intent intent = getIntent();
		position = intent.getIntExtra(KEYRING_POSITION, -1);
		String keyring = MainActivity.mKeyringAdapter.getItem(position).getName();
		
		new Builder(this)
		    .setTitle(R.string.enter_pass_title)
		    .setMessage(String.format(getResources().getString(R.string.enter_pass), keyring))
		    .setView(mPassword)
		    .setPositiveButton(getResources().getString(R.string.ok), this)
		    .setNegativeButton(getResources().getString(R.string.cancel), this)
		    .setCancelable(false).show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		Intent intent = new Intent();
		switch(which) {
		case AlertDialog.BUTTON_NEGATIVE:
			finish();
			break;
		case AlertDialog.BUTTON_POSITIVE:
			if (check(mPassword.getText().toString())) {
				intent.putExtra(CANCELED, 0);
				intent.putExtra(KEYRING_POSITION, position);
				intent.putExtra(KEYRING_PASSWORD, mPassword.getText().toString());
			} else {
				Toast.makeText(this, R.string.wrong_password, Toast.LENGTH_SHORT).show();
			}
			setResult(RESULT_OK, intent);
			finish();
			break;
		}
	}

	private boolean check(String password) {
		KeyringInfo k = MainActivity.mKeyringAdapter.getItem(position);
		k.setPassword(password);
		return k.check();
	}
}
