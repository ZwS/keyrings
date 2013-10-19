package com.zws.keyrings.jgkm;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

class KeyringOutputStream extends DataOutputStream {

	public KeyringOutputStream(OutputStream out) {
		super(out);
	}
	
	public void writeString(String s) throws IOException {
		byte[] tmp = s.getBytes(Charset.defaultCharset());
		writeInt(tmp.length);
		write(tmp);
	}
}
