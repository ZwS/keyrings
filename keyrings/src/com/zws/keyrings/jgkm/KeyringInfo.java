package com.zws.keyrings.jgkm;


import java.io.IOException;
import java.util.ArrayList;

import com.zws.keyrings.jgkm.KeyringParser.Type;

public class KeyringInfo {
	private String name;
	private long ctime;
	private long mtime;
	private int flags;
	private int lock_timeout;
	private String password;
	
	public ArrayList<ItemInfo> items;
	
	private KeyringParser parser; 
	
	//manual creation
	public KeyringInfo(String name, String password) throws IOException {
		this.name = name;
		this.password = password;
		ctime = System.currentTimeMillis() / 1000L;
		mtime = ctime;
		flags = 0;
		lock_timeout = 0;
		items = new ArrayList<ItemInfo>(0);
		parser = ParserFactory.createParser(this);
	}
	
	public KeyringInfo(String keyring) throws IOException {
		parser = ParserFactory.getParser(keyring);
		parser.readKeyringInfo(this);
	}
	
	public void setPassword(String password) {
		this.password = password;
		parser.setPassword(password);
	}
	
	public void getKeyringItems() {
		items = parser.readKeyringItems();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
		this.setModificationTime(System.currentTimeMillis() / 1000L);
	}

	public long getCreateTime() {
		return ctime;
	}

	void setCreateTime(long ctime) {
		this.ctime = ctime;
	}

	public long getModificationTime() {
		return mtime;
	}

	void setModificationTime(long mtime) {
		this.mtime = mtime;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public int getLockTimeout() {
		return lock_timeout;
	}

	public void setLockTimeout(int lock_timeout) {
		this.lock_timeout = lock_timeout;
	}

	public Type getType() {
		return parser.getType();
	}

	public String getPassword() {
		return password;
	}

	public String toString() {
		return name;
	}
	
	public boolean check() {
		return ((EncryptedKeyringParser) parser).decrypt();
	}
	
	public void write() throws IOException {
		parser.write(this);
	}
	
	public void delete() {
		parser.delete();
	}
	
	public int getMaxId() {
		int id = 0;
		for (ItemInfo i : items) {
			if (i.getId() >= id)
				id = i.getId() + 1;
		}
		return id;
	}
}
