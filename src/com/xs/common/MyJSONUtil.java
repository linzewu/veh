package com.xs.common;

import java.util.HashMap;
import java.util.Map;


public class MyJSONUtil {

	public static Map toMyJSON(Integer state, String message,
			Object object) {
		Map myJson=new HashMap();
		myJson.put(Constant.DATA, object);
		myJson.put(Constant.MESSAGE, message);
		myJson.put(Constant.STATE, state);
		return myJson;
	}
	
	public static Map toMyJSON(Integer state, String message) {
		
		Map myJson=new HashMap();
		myJson.put(Constant.MESSAGE, message);
		myJson.put(Constant.STATE, state);
		return myJson;
	}

}
