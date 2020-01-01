package com.xs.common;

import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Blob;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.engine.jdbc.SerializableBlobProxy;
import org.springframework.jdbc.core.JdbcTemplate;

import com.aspose.words.Document;
import com.aspose.words.ImageSaveOptions;
import com.aspose.words.Node;
import com.aspose.words.NodeCollection;
import com.aspose.words.NodeType;
import com.aspose.words.SaveFormat;
import com.aspose.words.Shape;
import com.aspose.words.Table;
import com.xs.veh.entity.BaseParams;

import net.sf.json.JSONArray;

public class Sql2WordUtil {
	
	static Log logger = LogFactory.getLog(Sql2WordUtil.class);
	
	public static Document sql2WordUtil(final String template,final String sql, JdbcTemplate jdbcTemplate,Map<String,List<BaseParams>> bpsMap) throws Exception{
		Map<String,Object> data = jdbcTemplate.queryForMap(sql);
		Document doc=null;
		if(data!=null) {
			 doc = createTemplate(template,data,bpsMap);
		}
		return doc;
	}
	
	public static Map<String,Object> sql2MapUtil(final String sql,JdbcTemplate jdbcTemplate) throws Exception{
		Map<String,Object> data = jdbcTemplate.queryForMap(sql);
		return data;
	}
	
	public static Document map2WordUtil(final String template,Map<String,Object> data,Map<String,List<BaseParams>> bpsMap) throws Exception{
		
		Document doc=null;
		if(data!=null) {
			 doc = createTemplate(template,data,bpsMap);
		}
		return doc;
	}
	
	public static String toCase(Document doc,String paht,String fileName) throws Exception{
		
		if(doc!=null) {
			ImageSaveOptions iso = new ImageSaveOptions(SaveFormat.JPEG);
			iso.setPrettyFormat(true);
			iso.setUseAntiAliasing(true);
			iso.setJpegQuality(80);
			doc.save(paht+fileName,iso);
			return fileName;
		}else {
			return null;
		}
	}
	
	public static Object getData(String key,Map data) {
		
		if(key==null||data==null) {
			return null;
		}
		
		String[] keys =  key.split("\\.");
		String firstKey = keys[0];
		int index1=firstKey.indexOf("[");
		int index2=firstKey.indexOf("]");
		
		Object obj = null;
		
		if(index1!=-1&&index2!=-1) {
			String key1=firstKey.substring(0,index1);
			int index =Integer.parseInt(firstKey.substring(index1+1,index2));
			List array =(List) data.get(key1);
			obj = array.get(index);
		}else {
			obj = data.get(firstKey);
		}
		
		if(keys.length==1) {
			return obj;
			
		}else {
			String newKey="";
			for(int i=1;i<keys.length;i++) {
				newKey = newKey+keys[i]+".";
			}
			newKey=newKey.substring(0,newKey.length()-1);
			return getData(newKey, (Map) obj);
		}
		
	}
	
	
	
	
	
	public static Document createTemplate(String template,Map<String, Object> data,Map<String,List<BaseParams>> bpsMap) throws Exception {
	
		InputStream wordTemplate = Sql2WordUtil.class.getClassLoader().getResourceAsStream(template);
		Document doc = new Document(wordTemplate);
		NodeCollection shapeCollection = doc.getChildNodes(NodeType.SHAPE, true);// 查询文档中所有wmf图片
		Node[] shapes = shapeCollection.toArray();// 序列化
		
		
		
		
		
		for(Node node:shapes) {
			Shape shape = (Shape) node;
			com.aspose.words.ImageData i = shape.getImageData();// 获得图片数据
			Object imgObj=getData(shape.getAlternativeText(),data); 
			if(imgObj==null) {
				continue;
			}
			if(imgObj instanceof Proxy) {
				SerializableBlobProxy proxy = (SerializableBlobProxy) Proxy.getInvocationHandler(imgObj);
				Blob blob =proxy.getWrappedBlob();
				if (blob!=null) {// 如果shape类型是ole类型
					InputStream inStream = blob.getBinaryStream();
					i.setImage(inStream);
				}
			}
			
			if(imgObj instanceof InputStream) {
				InputStream inStream =(InputStream)imgObj;
				i.setImage(inStream);
			}
			
		}
		
		// 填充文字
		if (data != null) {
			
			String[] fieldNames =  doc.getMailMerge().getFieldNames();
			Object[] fieldValues = new Object[fieldNames.length];
			int i=0;
			for(String fieldName:fieldNames) {
				if(fieldName.indexOf("CK##")==0) {
					String[] temp = fieldName.split("##");
					String value=temp[1];
					String key =temp[2].toLowerCase();
					fieldValues[i]=(String)getData(key,data);
					if(value.equals(fieldValues[i])) {
						fieldValues[i]="☑";
					}else {
						fieldValues[i]="□";
					}
				}else if(bpsMap!=null&&bpsMap.containsKey(fieldName.toLowerCase())){
					
					fieldValues[i] = translateParamVlaue(getData(fieldName,data),bpsMap.get(getLastKsy(fieldName.toLowerCase())));
					
				}else {
					fieldValues[i] = translateMapValue(data, fieldName.toLowerCase());
				}
				i++;
			}
			
			// 合并模版，相当于页面的渲染
			doc.getMailMerge().execute(fieldNames, fieldValues);
			
			
		}
		return doc;
		
	}
	
	private static String getLastKsy(String key) {
		
	
		String[] keys = key.split("\\.");
		
		String lastKey = keys[keys.length-1];
		
		int index1=lastKey.indexOf("[");
		int index2=lastKey.indexOf("]");
		
		if(index1!=-1&&index2!=-1) {
			return lastKey.substring(0,index1);
		}else {
			return lastKey;
		}
		
	}
	
	
	private static Object translateParamVlaue(Object object, List<BaseParams> list) {
		
		if(object!=null) {
			for(BaseParams param : list) {
				if(param.getParamValue().equals(object.toString())) {
					return param.getParamName();
				}
			}
		}
		
		return object;
	}

	public static String sql2WordUtilCase(String template, String sql, JdbcTemplate jdbcTemplate,String fileName) throws Exception {
		Document doc = sql2WordUtil(template, sql, jdbcTemplate,null);
		
		if(doc!=null) {
			ImageSaveOptions iso = new ImageSaveOptions(SaveFormat.JPEG);
			iso.setPrettyFormat(true);
			iso.setUseAntiAliasing(true);
			iso.setJpegQuality(80);
			doc.save(getCacheDir()+fileName,iso);
			return fileName;
		}else {
			return null;
		}
		
		
	}
	
	public static String getCacheDir() {
		String path = Sql2WordUtil.class.getClassLoader().getResource("").getPath().toString();
		String temp = path.split("WEB-INF")[0];
		
		return temp+"images/cache/";
		
	}

	public static String getStringDate(Date date,int type){
		return type==0?new SimpleDateFormat("yyyy年MM月dd日").format(date):new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
	}
	
	public static String translateMapValue(Map<String,Object> map,String key){
		
		Object obj = getData(key, map);
		
		if(obj instanceof BigDecimal ){
			BigDecimal bg = (BigDecimal) obj;
			return bg.toString();
		}
		if(obj instanceof Date ){
			if(key.indexOf("PSSJ")==0){
				return getStringDate((Date) obj,1);
			}
			return getStringDate((Date) obj,0);
		}
		if(obj instanceof Character){
			return ((Character)obj).toString();
		}
		
		if(obj instanceof Integer){
			return ((Integer)obj).toString();
		}
		
		if(obj instanceof Float){
			return ((Float)obj).toString();
		}
		
		if(obj==null) {
			return "—";
		}
		
		return (String) obj;
	}
}
