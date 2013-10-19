package com.zws.keyrings.jgkm;

public class Acl {
	private int allowed;
	private String d_name;
	private String pathname;
	
	Acl(int allowed, String d_name, String pathname){
		this.allowed = allowed;
		this.d_name = d_name;
		this.pathname = pathname;
	}
	
	int getAllowed() {
		return allowed;
	}
	
	String getDname() {
		return d_name;
	}
	
	String getPathname() {
		return pathname;
	}
}
