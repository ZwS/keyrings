package com.zws.keyrings.jgkm;


public class AttributeInfo<T> {
	private String name;
	private T value;
	
	public AttributeInfo(String name, T value) {
		this.name = name;
		this.value = value;
	}
	
	public T getValue() {
		return value;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return name + " : " + value;
		
	}
}
