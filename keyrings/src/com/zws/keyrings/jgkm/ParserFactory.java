package com.zws.keyrings.jgkm;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.util.Log;

class ParserFactory {
	static KeyringParser getParser(File keyring) throws IOException {
		KeyringParser parser;
		
		byte[] header = new byte[16];
		
		try {
			RandomAccessFile f = new RandomAccessFile(keyring, "r");
			f.read(header);
			f.close();
		} catch (IOException e) {
			Log.e("jgkm", "Error while reading file " + keyring);
			Log.e("jgkm", e.toString());
		}
		
		

		if (new String(header).equals(EncryptedKeyringParser.HEADER))
			parser = new EncryptedKeyringParser(keyring);
		else if (new String(header).startsWith(NonencryptedKeyringParser.HEADER))
			parser = new NonencryptedKeyringParser(keyring);
		else 
			throw new IllegalArgumentException("Unknown file format");
		
		return parser;
	}
	
	static KeyringParser createParser(KeyringInfo k) throws IOException {
		KeyringParser parser;
		if (k.getPassword().isEmpty())
			parser = new NonencryptedKeyringParser(k.getFile());
		else
			parser = new EncryptedKeyringParser(k.getFile());
		parser.create(k);
		return parser;
	}
}
