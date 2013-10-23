package com.zws.keyrings.jgkm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.ini4j.Wini;

public class NonencryptedKeyringParser implements KeyringParser {
	public final static String HEADER = "[keyring]";
	
	private final File file;
	private Wini data;
	
	public NonencryptedKeyringParser (File keyring) throws IOException {
		file = keyring;
		data = new Wini(file);
	}
	
	@Override
	public void readKeyringInfo (KeyringInfo k) {
		String display_name = data.get("keyring", "display-name", String.class);
		int ctime = data.get("keyring", "ctime", int.class);
		int mtime = data.get("keyring", "mtime", int.class);
		String lock_on_idle = data.get("keyring", "lock-on-idle", String.class);
		String lock_after = data.get("keyring", "lock-after", String.class);
		
		k.setName(display_name);
		k.setFlags(lock_on_idle.equals("true") ? 1 : 0);
		k.setCreateTime(ctime);
		k.setModificationTime(mtime);
		k.setLockTimeout(lock_after.equals("false") ? 0 : Integer.parseInt(lock_after));
		
	}
	
	@Override
	public ArrayList<ItemInfo> readKeyringItems () {
		ArrayList<ItemInfo> items = new ArrayList<ItemInfo>();
		
		for (String field : data.keySet()) {
			if (field.equals("keyring"))
				continue;
			if (field.matches("\\d*")) {
				ItemInfo i;
				int id = Integer.parseInt(field);
				i = new ItemInfo(id, data.get(field, "item-type", int.class));
				i.setDisplayName(data.get(field, "display-name"));
				i.setPassword(data.get(field, "secret"));
				i.setCreationTime(data.get(field, "ctime", int.class));
				i.setModificationTime(data.get(field, "mtime", int.class));
				i.setAttrNum(0);
				items.add(i);
			} else if (field.matches("\\d:attribute\\d*")) {
				int id = Integer.parseInt(field.replaceAll("^(\\d*):attribute\\d*$", "$1"));
				for (ItemInfo i : items) {
					if (i.getId() == id) {
						AttributeInfo<?> attr = null;
						if (data.get(field, "type").equals("string"))
							attr = new AttributeInfo<String>(data.get(field, "name"), data.get(field, "value"));
						if (data.get(field, "type").equals("uint32"))
								attr = new AttributeInfo<Integer>(data.get(field, "name"), Integer.parseInt(data.get(field, "value")));
						i.addAttr(attr);
					}
				}
			}
		}
		return items;
	}

	@Override
	public void write(KeyringInfo k) throws IOException {
		data.clear();
		writeHeader(k);
		
		for (ItemInfo i : k.items) {
			String key = ((Integer) i.getId()).toString();
			data.add(key, "item-type", i.getType());
			data.add(key, "display-name", i.getDisplayName());
			data.add(key, "secret", i.getPassword());
			data.add(key, "ctime", i.getCreationTime());
			data.add(key, "mtime", i.getModificationTime());
			if (i.attrs.size() > 0) {
				for (int n = 0; n < i.attrs.size(); n++) {
					String attr_key = key + ":attribute" + n;
					data.add(attr_key, "name", i.attrs.get(n).getName());
					Object value = i.attrs.get(n).getValue();
					if (value instanceof Integer)
						data.add(attr_key, "type", "uint32");
					if (value instanceof String)
						data.add(attr_key, "type", "string");
					data.add(attr_key, "value", value);
				}
			}
		}
		
		data.store(file);
	}

	@Override
	public void setPassword(String password) { }

	@Override
	public Type getType() {
		return Type.Nonencrypted;
	}

	private void writeHeader(KeyringInfo k) {
		data.add("keyring", "display-name", k.getName());
		data.add("keyring", "ctime", k.getCreateTime());
		data.add("keyring", "mtime", k.getModificationTime());
		data.add("keyring", "lock-on-idle", k.getFlags() == 1);
		if (k.getLockTimeout() == 0)
			data.add("keyring", "lock-after", "false");
		else
			data.add("keyring", "lock-after", k.getLockTimeout());
	}
	
	@Override
	public void create(KeyringInfo k) throws IOException {
		write(k);
	}

	@Override
	public void delete() {
		data.clear();
		file.delete();
	}
}