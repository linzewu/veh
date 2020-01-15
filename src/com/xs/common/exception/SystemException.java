package com.xs.common.exception;

public class SystemException extends Exception {
	
	public SystemException(String msg){
		super(msg);
	}
	
	
	public SystemException(String msg,Exception e){
		super(msg,e);
	}
}
