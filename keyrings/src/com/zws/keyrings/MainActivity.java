package com.zws.keyrings;

import java.io.File;

import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.zws.keyrings.R;
import com.zws.keyrings.jgkm.Globals;
import com.zws.keyrings.jgkm.ItemInfo;
import com.zws.keyrings.jgkm.KeyringInfo;
import com.zws.keyrings.jgkm.KeyringParser.Type;


public class MainActivity extends Activity implements ListView.OnItemClickListener{
	private static final int PASSWORD_GETTER = 0;
	private static final int PASSWORD_ACTIVITY = 1;

	private PasswordsFragment mPasswordfragment;
	
	private int position;
	private boolean drawerstate = true;
	private ListView mDrawerList;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	public static ArrayAdapter<KeyringInfo> mKeyringAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		MainActivity.mKeyringAdapter = new ArrayAdapter<KeyringInfo>(this, R.layout.drawer_list_item);
		MainActivity.createKeyringList();

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setAdapter(MainActivity.mKeyringAdapter);
		mDrawerList.setOnItemClickListener(this);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				getActionBar().setTitle(R.string.passwords);
				drawerstate = false;
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(R.string.app_name);
				drawerstate = true;
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (drawerstate)
			mDrawerLayout.openDrawer(mDrawerList);
		else 
			mDrawerLayout.closeDrawer(mDrawerList);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (drawerstate) {
			menu.setGroupVisible(R.id.keyrings, true);
			menu.setGroupVisible(R.id.passwords, false);
		} else {
			menu.setGroupVisible(R.id.keyrings, false);
			menu.setGroupVisible(R.id.passwords, true);
		}
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		selectItem(position);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case PASSWORD_GETTER :
			if (data == null || data.getIntExtra(PasswordGetter.CANCELED, 1) == 1)
				return;
			String password = data.getStringExtra(PasswordGetter.KEYRING_PASSWORD);
			MainActivity.mKeyringAdapter.getItem(position).setPassword(password);
	   		showPasswordFragment();
	   		break;
		case PASSWORD_ACTIVITY :
			showPasswordFragment(); //update fragment
			break;
		}
	}
	
	private void selectItem(int pos) {
		this.position = pos;
		if (MainActivity.mKeyringAdapter.getItem(position).getType() == Type.Encrypted) {
			Intent i = new Intent(this, PasswordGetter.class);
			i.putExtra(PasswordGetter.KEYRING_POSITION, position);
			startActivityForResult(i, PASSWORD_GETTER);
		} else {
			showPasswordFragment();
		}
	}
	
	private void showPasswordFragment() {
		mPasswordfragment = new PasswordsFragment();
		Bundle args = new Bundle();
		args.putInt(PasswordsFragment.KEYRING_POSITION, position);
		mPasswordfragment.setArguments(args);

		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, mPasswordfragment).commit();
		
		// update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	public static class PasswordsFragment extends Fragment implements OnItemClickListener {
		public static final String KEYRING_POSITION = "keyring position";
		private int position;
		private ArrayAdapter<ItemInfo> items;

		public PasswordsFragment() {
			// Empty constructor required for fragment subclasses
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			if (savedInstanceState != null)
				position = savedInstanceState.getInt("position");
			else
				position = getArguments().getInt(KEYRING_POSITION);
			
			KeyringInfo keyring = MainActivity.mKeyringAdapter.getItem(position);
			keyring.getKeyringItems();
			items = new ArrayAdapter<ItemInfo>(getActivity(), R.layout.password_list_item, keyring.items);
			
			View rootView;
			if (!items.isEmpty()) {
				rootView = inflater.inflate(R.layout.fragment_password, container, false);
				ListView mPasswords = (ListView) rootView.findViewById(R.id.passwords);
				mPasswords.setAdapter(items);
				mPasswords.setOnItemClickListener(this);
			} else {
				rootView = inflater.inflate(R.layout.fragment_no_items, container, false);
			}
			getActivity().setTitle(MainActivity.mKeyringAdapter.getItem(position).getName());
			return rootView;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Intent i = new Intent(getActivity(), PasswordActivity.class);
			i.putExtra(PasswordActivity.KEYRING_POSITION, this.position);
			i.putExtra(PasswordActivity.SECRET_POSITION, position);
			getActivity().startActivityForResult(i, PASSWORD_ACTIVITY);
		}
		
		@Override
		public void onSaveInstanceState(Bundle savedInstanceState) {
			super.onSaveInstanceState(savedInstanceState);
			savedInstanceState.putInt("position", this.position);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		 // The action bar home/up action should open or close the drawer.
		 // ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		
		Intent intent;
		switch (item.getItemId()) {
		case R.id.add_keyring: 
			intent = new Intent(this, KeyringActivity.class);
			this.startActivity(intent);
			break;
		case R.id.remove_keyring:
			delete();
			break;
		case R.id.change_keyring_password: 
			
			break;
		case R.id.action_settings:
			intent = new Intent(this, SettingsActivity.class);
			this.startActivity(intent);
			break;
		case R.id.add_secret:
			intent = new Intent(this, PasswordActivity.class);
			intent.putExtra(PasswordActivity.NEW_SECRET, true);
			intent.putExtra(PasswordActivity.KEYRING_POSITION, position);
			startActivityForResult(intent, PASSWORD_ACTIVITY);
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void delete() {
		KeyringInfo k = MainActivity.mKeyringAdapter.getItem(position);
		k.delete();
		MainActivity.mKeyringAdapter.remove(k);
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().remove(mPasswordfragment).commit();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggle
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putBoolean("drawerstate", drawerstate);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		drawerstate = savedInstanceState.getBoolean("drawerstate");
	}

	public static void createKeyringList() {
		mKeyringAdapter.clear();
		
		File keyring_dir = new File(Globals.keyring_dir);
		
		if (!keyring_dir.exists())
			keyring_dir.mkdirs();
		
		String[] filelist = keyring_dir.list(new Filter());
		if (filelist != null) {
			for (String keyring : filelist) {
				KeyringInfo k;
				try {
					k = new KeyringInfo(Globals.keyring_dir + keyring);
					mKeyringAdapter.add(k);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			Log.d(Globals.TAG, "No files found.");
		}
	}
}
