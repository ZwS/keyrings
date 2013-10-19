package com.zws.keyrings.jgkm;

import java.io.IOException;
import java.util.ArrayList;

public interface KeyringParser {
	enum Type {Encrypted, Nonencrypted}
	
	public void readKeyringInfo (KeyringInfo k);
	public ArrayList<ItemInfo> readKeyringItems ();
	public void create(KeyringInfo k) throws IOException;
	public void write(KeyringInfo k) throws IOException;
	public void delete();
	public void setPassword(String password);
	public Type getType();
}
