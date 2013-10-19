package com.zws.keyrings.jgkm;

import java.util.ArrayList;

public class ItemInfo {
	private int id;
	private int type;
	private String display_name;
	private String secret;
	private long ctime = 0;
	private long mtime = 0;
	public ArrayList<AttributeInfo<?>> attrs = new ArrayList<AttributeInfo<?>>(0);
	public ArrayList<Acl> acl = new ArrayList<Acl>(0);
	
	public ItemInfo(String display_name, String secret) {
		this.display_name = display_name;
		this.secret = secret;
		this.ctime = System.currentTimeMillis() / 1000L;
		this.mtime = this.ctime;
	}

	ItemInfo(int id, int type) {
		this.id = id;
		this.type = type;
	}
	
	public void setPassword(String secret) {
		this.secret = secret;
	}
	public String getPassword() {
		return secret;
	}
	
	public void setDisplayName(String display_name) {
		this.display_name = display_name;
	}
	
	public String getDisplayName() {
		return display_name;
	}
	
	void setCreationTime(long ctime) {
		this.ctime = ctime;
	}
	
	void setModificationTime(long mtime) {
		this.mtime = mtime;
	}
	
	public long getModificationTime() {
		return mtime;
	}
	
	void setAttrNum(int num) {
		attrs = new ArrayList<AttributeInfo<?>>(num);
	}
	
	void addAttr(AttributeInfo<?> attr) {
		this.attrs.add(attr);
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String toString() {
		return display_name;
	}

	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}
	
	public void updateTime() {
		this.mtime = System.currentTimeMillis() / 1000L;
	}
	
	public long getCreationTime() {
		return ctime;
	}
	
	
	public boolean equals(Object o) {
		ItemInfo i;
		if (o instanceof ItemInfo)
			i = (ItemInfo) o;
		else
			return false;
		return this.display_name.equals(i.getDisplayName()) &&
				this.secret.equals(i.getPassword());
	}
}
