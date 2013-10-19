package com.zws.keyrings;

import java.io.IOException;

import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;

import com.zws.keyrings.R;
import com.zws.keyrings.jgkm.KeyringInfo;
import com.zws.keyrings.jgkm.ItemInfo;

public class PasswordActivity extends Activity {
	public final static String NEW_SECRET = "create new secret";
	public final static String KEYRING_POSITION = "keyring position";
	public final static String SECRET_POSITION = "secret position";
	
	private EditText mItemName;
	private EditText mSecret;
	
	private int keyring_position;
	private int secret_position;
	private Action action;
	private ItemInfo item;
	
	enum Action {Update, New, Delete}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item_info);
		
		Intent intent = getIntent();
		keyring_position = intent.getIntExtra(KEYRING_POSITION, -1);
		secret_position = intent.getIntExtra(SECRET_POSITION, -1);
		
		KeyringInfo k = MainActivity.mKeyringAdapter.getItem(keyring_position);
		
		boolean create_new = intent.getBooleanExtra(NEW_SECRET, false);
		
		if (create_new) {
			action = Action.New;
			item = new ItemInfo("", ""); //empty secret
		} else {
			action = Action.Update;
			item = k.items.get(secret_position); 
		}
		
		mItemName = (EditText) findViewById(R.id.password_description);
		mItemName.setText(item.getDisplayName());
		
		mSecret = (EditText) findViewById(R.id.secret);
		mSecret.setText(item.getPassword());
		
		CheckBox mShowSecret = (CheckBox) findViewById(R.id.show_secret);
		mShowSecret.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		    {
		        if ( isChecked )
		        	mSecret.setInputType(InputType.TYPE_CLASS_TEXT);
		        else
		        	mSecret.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		    }
		});
		
		ImageButton mCopy = (ImageButton) findViewById(R.id.copy);
		mCopy.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) {
				ClipboardManager clipboard = (ClipboardManager)
				        getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("simple text", mSecret.getText().toString());
				try {
					clipboard.setPrimaryClip(clip);
				} catch (NullPointerException e) {
					return; // Samsung make me cry
				}
				Toast.makeText(v.getContext(), getResources().getString(R.string.secret_copied), Toast.LENGTH_SHORT).show();
			}
		});
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.password_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    switch (item.getItemId()) 
	    {
	        case android.R.id.home:
	            finish();
	            return true;
	        case R.id.delete:
	        	action = Action.Delete;
	        	finish();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public void finish() {
		super.finish();
		
		item.setPassword(mSecret.getText().toString());
		item.setDisplayName(mItemName.getText().toString());
		
		KeyringInfo k = MainActivity.mKeyringAdapter.getItem(keyring_position);
		
		switch(action) {
		case New : 
			item.setId(k.getMaxId());
			item.updateTime();
			k.items.add(item);
			break;
		case Update :
			item.updateTime();
			k.items.set(secret_position, item);
			break;
		case Delete :
			k.items.remove(secret_position);
			break;
		}
		
		try {
			k.write();
		} catch (IOException e) {
			Toast.makeText(this, R.string.password_save_error, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
}
