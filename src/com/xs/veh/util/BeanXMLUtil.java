package com.xs.veh.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class BeanXMLUtil {
	
	 private static Logger logger = Logger.getLogger(BeanXMLUtil.class);

	public static Document bean2xml(Object bean, String element)
			throws 	NoSuchMethodException{

		Field[] fields = bean.getClass().getDeclaredFields();
		
		Document document=DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		Element subelement = root.addElement(element);
		logger.debug("fields size"+fields.length);
		for (Field field : fields) {
			String fname = field.getName();
			String getMehod = "get" + fname.substring(0, 1).toUpperCase()
					+ fname.substring(1, fname.length());
			Method method = bean.getClass().getMethod(getMehod);
			Object value;
			try {
				value = method.invoke(bean);
				Element felement = subelement.addElement(fname);
				if(value!=null&&!"".equals(value)){
					felement.setText( URLEncoder.encode(value.toString(),"UTF-8"));
				}
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | UnsupportedEncodingException e) {
				logger.error("bean2xml执行异常",e);
			}
		}
		logger.debug("subelement"+subelement.asXML());
		logger.debug("document"+document.asXML());
		return document;
	}
	
	
	public static Document map2xml(Map map ,String element){
		logger.debug("map:+"+map);
		Document document=DocumentHelper.createDocument();
		document.setXMLEncoding("GBK");
		Element root = document.addElement("root");
		Element subelement = root.addElement(element);
		Set<String> set = map.keySet();
		for(String key:set){
			Element felement = subelement.addElement(key);
			try {
				if(map.get(key)!=null){
						felement.setText( URLEncoder.encode(map.get(key).toString().trim(),"UTF-8"));
				}
			} catch (Exception e) {
				logger.error("map2xml执行异常",e);
			}
		}
		logger.debug("document:"+document.asXML());
		return document;
	} 
	
	

	public static void main(String[] arg) {
//		JYDLXX jydxx = new JYDLXX();
//		jydxx.setId(111);
//		try {
//		System.out.println(	BeanXMLUtil.bean2xml(jydxx, "vehispara"));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

}
