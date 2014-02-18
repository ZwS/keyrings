package com.zws.keyrings.jgkm;


import java.io.File;
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
	private File file;
	
	public ArrayList<ItemInfo> items;
	
	private KeyringParser parser; 
	
	public KeyringInfo(String name, String password) throws IOException {
		this.name = name;
		this.password = password;
		ctime = System.currentTimeMillis() / 1000L;
		mtime = ctime;
		flags = 0;
		lock_timeout = 0;
		items = new ArrayList<ItemInfo>(0);
		
		String keyring_file = name.toLowerCase().replace("[^a-z0-9]", "_");
		String keyring = Globals.getKeyringDir() + File.separator + keyring_file + ".keyring";
		
		File f = new File(keyring);
		
		if(f.exists() || f.isDirectory())
			throw new IOException("File already exists");
		file = f;
		file.createNewFile();
		
		parser = ParserFactory.createParser(this);
	}
	
	public KeyringInfo(File file) throws IOException {
		this.file = file;
		parser = ParserFactory.getParser(this.file);
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
		if (parser instanceof EncryptedKeyringParser)
			return ((EncryptedKeyringParser) parser).decrypt();
		return true;
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

	public void changePassword(String new_password) throws IOException {
		password = new_password;
		parser = ParserFactory.createParser(this);
		parser.readKeyringInfo(this);
	}

	public File getFile() {
		return file;
	}
}
