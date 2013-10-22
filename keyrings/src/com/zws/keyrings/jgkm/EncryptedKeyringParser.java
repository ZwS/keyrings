package com.zws.keyrings.jgkm;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class EncryptedKeyringParser implements KeyringParser {
	public final static String HEADER = "GnomeKeyring\n\r\0\n";
	
	private final File file;
	
	private ByteBuffer data;
	private String password;
	private byte[] version = {0, 0};
	private byte[] salt = new byte[8];
	private int hash_iterations;
	private byte cryto = 0;	//0 == AES
	private byte hash = 0;	//0 == MD5
	private byte[] decoded;
	private ArrayList<ItemInfo> items;
	private ByteBuffer decrypted;
	
	public EncryptedKeyringParser (String keyring) throws IOException {
		file = new File(keyring);
		byte [] data = new byte[(int)file.length()];
		DataInputStream dis = new DataInputStream((new FileInputStream(file)));
		dis.readFully(data);
		dis.close();
		
		this.data = ByteBuffer.wrap(data);
	}
	
	@Override
	public void readKeyringInfo(KeyringInfo k) {
		data.position(16); //skip header
		//header info
		data.get(version);
		cryto = data.get();
		hash = data.get();
		
		//keyring info
		k.setName(getString(data));
		k.setCreateTime(data.getLong());
		k.setModificationTime(data.getLong());
		k.setFlags(data.getInt());
		k.setLockTimeout(data.getInt());
		hash_iterations = data.getInt();

		data.get(salt);
		data.get(new byte[16]); // reserved, int * 4
		
		int num_items = data.getInt();
		items = new ArrayList<ItemInfo>(num_items);
		
		for (int i = 0; i < num_items; i++) {
			int id = data.getInt();
			int type = data.getInt();
			ItemInfo item = new ItemInfo(id, type);
			int num_attributes = data.getInt();
			item.setAttrNum(num_attributes);
			for (int n = 0; n < num_attributes; n++) {
				getString(data); //attr_name, useless
				int attr_type = data.getInt();
				switch (attr_type) {
					case 0: getString(data); break;
					case 1: data.getInt(); break;
					default: break;
				}
			}
			items.add(item);
		}
		
		int num_decoded = data.getInt();
		decoded = new byte[num_decoded];
		data.get(decoded);
	}
	
	public boolean decrypt() {
		byte[] k = new byte[password.getBytes().length + 8];
		System.arraycopy(password.getBytes(), 0, k, 0, password.getBytes().length);
		System.arraycopy(salt, 0, k, password.getBytes().length, 8);
		
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			int hash_iterations = this.hash_iterations;
			while (hash_iterations-- > 0) {
				digest.update(k);
				k = digest.digest();
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		byte[] key = Arrays.copyOfRange(k, 0, 16);
		byte[] iv = Arrays.copyOfRange(k, 16, 32);
		byte[] t = new byte[0];
		byte[] encryted_hash = new byte[16];
		byte[] md5 = new byte[16];
		
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			SecretKeySpec keyspec = new SecretKeySpec(key, "AES");
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
			
			t = cipher.doFinal(decoded);
			encryted_hash = Arrays.copyOfRange(t, 0, 16);
			
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(t, 16, t.length - 16);
			md5 = md.digest();

		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if (!Arrays.equals(md5, encryted_hash))
			return false;
		decrypted = ByteBuffer.wrap(t);
		return true;
	}
	
	@Override
	public ArrayList<ItemInfo> readKeyringItems() {
		try {
			decrypted.position(16);
			for (ItemInfo i : items) {
				i.setDisplayName(getString(decrypted));
				i.setPassword(getString(decrypted));
				i.setCreationTime(decrypted.getLong());
				i.setModificationTime(decrypted.getLong());
				
				getString(decrypted);
				decrypted.get(new byte[16]); //reserved 4 int
				
				int num_attributes = decrypted.getInt();
				for (int n = 0; n < num_attributes; n++) {
					AttributeInfo<?> attr = null;
					String name = getString(decrypted);
					int type = decrypted.getInt();
					switch (type) {
						case 0: 
							attr = new AttributeInfo<String>(name, getString(decrypted));
							break;
						case 1: 
							attr = new AttributeInfo<Integer>(name, decrypted.getInt());
							break;
						default: break;
					}
					i.addAttr(attr);
				}
				
				int acl_len = decrypted.getInt();
				for (int n = 0; n < acl_len; n++) {
					int allowed = decrypted.getInt();
					String d_name = getString(decrypted);
					String pathname = getString(decrypted);
					getString(decrypted); //reserved
					decrypted.getInt(); //reserved
					
					i.acl.add(new Acl(allowed, d_name, pathname));
				}
			}
		} catch (Exception e) {
			System.out.print(e.toString());
			e.printStackTrace();
		}
		
		return items;
	}
	
	private String getString(ByteBuffer data) {
		int length = data.getInt();
		if (length < 0)
			return "";
		byte[] c = new byte[length];
		data.get(c);
		return new String(c);
	}

	@Override
	public void write(KeyringInfo k) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		KeyringOutputStream w = new KeyringOutputStream(baos);
		
		setPassword(k.getPassword());
		writeHeader(w, k);
		writeItemList(w, k);
		
		w.flush();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(baos.toByteArray());
		fos.close();
		
	}

	@Override
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public Type getType() {
		return Type.Encrypted;
	}

	private void writeHeader(KeyringOutputStream w, KeyringInfo k) throws IOException {
		w.write(HEADER.getBytes());
		w.write(version);
		w.write(cryto);
		w.write(hash);
		
		//keyring info
		w.writeString(k.getName());
		w.writeLong(k.getCreateTime());
		w.writeLong(k.getModificationTime());
		w.writeInt(k.getFlags());
		w.writeInt(k.getLockTimeout());
		
		w.writeInt(hash_iterations);
		w.write(salt);
		w.write(new byte[16]); // reserved, int * 4
	}

	private void writeItemList(KeyringOutputStream w, KeyringInfo k) throws IOException {
		w.writeInt(k.items.size());
		for (ItemInfo i : k.items) {
			w.writeInt(i.getId());
			w.writeInt(i.getType());
			w.writeInt(i.attrs.size());
			for(AttributeInfo<?> attr : i.attrs) {
				w.writeString(attr.getName());
				Object value = attr.getValue();
				if (value instanceof Integer) {
					w.writeInt(1);
				}
				if (value instanceof String) {
					w.writeInt(0);
				}
			}
		}
		
		ByteArrayOutputStream decrypted = new ByteArrayOutputStream();
		KeyringOutputStream wdecrypted = new KeyringOutputStream(decrypted);

		wdecrypted.write(new byte[16]); //for md5
		
		for (ItemInfo i : k.items) {
			wdecrypted.writeString(i.getDisplayName());
			wdecrypted.writeString(i.getPassword());
			wdecrypted.writeLong(i.getCreationTime());
			wdecrypted.writeLong(i.getModificationTime());
			wdecrypted.writeString(""); // reserved string
			wdecrypted.write(new byte[16]); //reserved 4 int
			
			wdecrypted.writeInt(i.attrs.size());
			
			for(AttributeInfo<?> attr : i.attrs) {
				wdecrypted.writeString(attr.getName());
				Object value = attr.getValue();
				if (value instanceof Integer)
					wdecrypted.writeInt((Integer) value);
				if (value instanceof String)
					wdecrypted.writeString((String) value);
			}
			wdecrypted.writeInt(i.acl.size());
			for (Acl a : i.acl) {
				wdecrypted.writeInt(a.getAllowed());
				wdecrypted.writeString(a.getDname());
				wdecrypted.writeString(a.getPathname());
				wdecrypted.writeString(""); //reserved string
				wdecrypted.write(new byte[4]); //reserved int
			}
		}
		
		wdecrypted.flush();
		wdecrypted.close();
		
		byte[] encrypted = decrypted.toByteArray();
		byte[] md5 = new byte[0];
		
		if (encrypted.length % 16 != 0) {
			byte[] tmp = new byte[encrypted.length + (16 - encrypted.length % 16)];
			System.arraycopy(encrypted, 0, tmp, 0, encrypted.length);
			encrypted = tmp;
		}
		
		this.decrypted = ByteBuffer.wrap(encrypted); //update data
		
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(encrypted, 16, encrypted.length - 16);
			md5 = md.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		System.arraycopy(md5, 0, encrypted, 0, md5.length);
		
		if (encrypted.length % 16 != 0) {
			byte[] tmp = new byte[encrypted.length + (16 - encrypted.length % 16)];
			System.arraycopy(encrypted, 0, tmp, 0, encrypted.length);
			encrypted = tmp;
		}
		
		byte[] k1 = new byte[password.getBytes().length + 8];
		System.arraycopy(password.getBytes(), 0, k1, 0, password.getBytes().length);
		System.arraycopy(salt, 0, k1, password.getBytes().length, 8);
		
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			int hash_iterations = this.hash_iterations;
			while (hash_iterations-- > 0) {
				digest.update(k1);
				k1 = digest.digest();
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		byte[] key = Arrays.copyOfRange(k1, 0, 16);
		byte[] iv = Arrays.copyOfRange(k1, 16, 32);
		byte[] t = new byte[0];
		
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			SecretKeySpec keyspec = new SecretKeySpec(key, "AES");
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			
			t = cipher.doFinal(encrypted);
		} catch(Exception e) {
			e.printStackTrace();
		}
				
		w.writeInt(t.length);
		w.write(t);
	}
	
	@Override
	public void create(KeyringInfo k) throws IOException {
		Random generator = new Random(); 
		hash_iterations = generator.nextInt(1000) + 1;
		generator.nextBytes(salt);
		setPassword(k.getPassword());
		
		write(k);
	}

	@Override
	public void delete() {
		data.clear();
		file.delete();
	}
}
