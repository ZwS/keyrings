package com.zws.keyrings;
import java.io.File;
import java.io.FilenameFilter;


class Filter implements FilenameFilter {

	@Override
	public boolean accept(File dir, String name) {
		return name.endsWith(".keyring");
	}

}
