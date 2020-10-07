package com.xs.veh.network.driver;


public class LED192 {
	
	public native static void led192(Integer intCom,String strFontSizeName,String strLedInfoUp, String strLedInfoDown);
	
	static 
	{
		System.load("D:\\extend\\led\\led192_64.dll");  
	}
}
