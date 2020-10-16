package com.xs.veh.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.common.Message;
import com.xs.veh.entity.CheckPhoto;
import com.xs.veh.entity.DeviceCheckJudegZJ;
import com.xs.veh.entity.TestVeh;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.manager.ExternalCheckManager;
import com.xs.veh.manager.TestVehManager;
import com.xs.veh.manager.VehManager;
import com.xs.veh.manager.ZHCheckDataManager;
import com.xs.veh.network.data.BaseDeviceData;
@Controller
@RequestMapping(value = "/testVeh")
@Modular(modelCode="testVeh",modelName="机动车综检联网")
public class TestVehController {
	
	@Resource(name = "testVehManager")
	private TestVehManager testVehManager;
	
	@Autowired
	private VehManager vehManager;
	
	@Autowired
	private CheckDataManager checkDataManager;
	
	@Autowired
	private ZHCheckDataManager zhCheckDataManager;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Resource(name = "externalCheckManager")
	private ExternalCheckManager externalCheckManager;
	
	Logger logger = Logger.getLogger(TestVehController.class);
	
	
	@UserOperation(code="getTestVeh",name="查询机动车综检联网过程")
	@RequestMapping(value = "getTestVeh", method = RequestMethod.POST)
	public @ResponseBody List<Map> getTestVeh(@RequestParam String jylsh) {
		System.out.println("jylsh:"+jylsh);
		TestVeh testVeh = zhCheckDataManager.getTestVehbyJylsh(jylsh);
		if(testVeh == null) {
			return null;
		}
		VehCheckLogin checkLogin = this.testVehManager.getVehCheckLogin(jylsh);
		JSONObject dataMap =(JSONObject)JSON.toJSON(testVeh);
		JSONObject checkLoginMap =(JSONObject)JSON.toJSON(checkLogin);
		dataMap.putAll(checkLoginMap);
		System.out.println(dataMap);
		List<Map> map = new ArrayList();
		map.add(dataMap);
		return map;
	}
	
	/**
     * 获取指定年月的最后一天
     * @param year
     * @param month
     * @return
     */
     public static String getLastDayOfMonth1(int year, int month) {     
         Calendar cal = Calendar.getInstance();     
         //设置年份  
         cal.set(Calendar.YEAR, year);  
         //设置月份  
         cal.set(Calendar.MONTH, month-1); 
         //获取某月最大天数
         int lastDay = cal.getActualMaximum(Calendar.DATE);
         //设置日历中月份的最大天数  
         cal.set(Calendar.DAY_OF_MONTH, lastDay);  
         //格式化日期
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
         return sdf.format(cal.getTime());
     }
	
	@UserOperation(code="zjRecordAdd",name="动车综检联网")
	@RequestMapping(value = "zjRecordAdd", method = RequestMethod.POST)
	public @ResponseBody String zjRecordAdd(String lsh,String djsj) {
		String[] dateArr = djsj.split("-");
		int year = Integer.parseInt(dateArr[0]);
		int month = 0;
		if(dateArr[1].startsWith("0")) {
			month = Integer.parseInt(dateArr[1].substring(1));
		}else {
			month = Integer.parseInt(dateArr[1]);
		}
		String techLevelEndDate = this.getLastDayOfMonth1(year, month)+" 23:59:59";
		VehCheckLogin vehCheckLogin = checkDataManager.getVehCheckLogin(lsh);
		TestVeh testVeh = zhCheckDataManager.getTestVehbyJylsh(lsh);
		JSONObject dataMap =(JSONObject)JSON.toJSON(vehCheckLogin);
		dataMap.put("techLevelEndDate", techLevelEndDate);
		if(testVeh!=null) {
			JSONObject testVehMap = (JSONObject)JSON.toJSON(testVeh);
			dataMap.putAll(testVehMap);
			
			if(testVeh.getBzzs()==null||testVeh.getBzzs()==0) {
				dataMap.put("bzzxs", "无");
			}else {
				if(vehCheckLogin.getCllx().indexOf("G")==0||vehCheckLogin.getCllx().indexOf("B")==0) {
					dataMap.put("bzzxs", "挂+"+testVeh.getBzzs());
				}
			}
			
			Integer qdzkzzl = testVeh.getQdzkzzl();
			if(qdzkzzl==null||qdzkzzl==0) {
				dataMap.remove("qdzkzzl");
			}
			
		}
		
//		if(vehCheckLogin.getCllx().indexOf("K")==-1) {
//			dataMap.remove("cwkc");
//		}
		
		String zczw = vehCheckLogin.getZczw();
		if(!StringUtils.isEmpty(zczw)) {
			String newZczw ="";
			for(char c:zczw.toCharArray()) {
				newZczw=c+",";
			}
			dataMap.put("zczw",newZczw.substring(0,newZczw.length()-1));
		}
		
		
		Date date = vehCheckLogin.getUpLineDate();
		SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String uplinedate = null;
		if(date==null) {
			uplinedate=vehCheckLogin.getCreateTime();
		}else {
			uplinedate = sdf.format(date);
		}
		
		dataMap.put("uplinedate", uplinedate);
		
		dataMap.put("qdzs", vehCheckLogin.getQdxs());
		
		//灯光
		Map<String, Object>  dgData = zhCheckDataManager.getHData(lsh, vehCheckLogin.getJycs());
		Map<String, Object>  zjcheckeData = new HashMap<String, Object>();
		for(String key:dgData.keySet()) {
			zjcheckeData.put(key.toLowerCase(), dgData.get(key));
		}
		JSONObject dgjo =(JSONObject) JSON.toJSON(zjcheckeData);
		dataMap.putAll(dgjo);
		
		//速度
		Map<String, Object>  s1Data = zhCheckDataManager.getS1Data(lsh, vehCheckLogin.getJycs());
		JSONObject s1jo =(JSONObject) JSON.toJSON(s1Data);
		dataMap.putAll(s1jo);
		
		//侧滑
		Map<String, Object>  aData = zhCheckDataManager.getAData(lsh, vehCheckLogin.getJycs());
		JSONObject ajo =(JSONObject) JSON.toJSON(aData);
		dataMap.putAll(ajo);
		
		//制动
		Map<String, Object>  bData = zhCheckDataManager.getBData(vehCheckLogin, vehCheckLogin.getJycs());
		
		
		
		JSONObject bjo =(JSONObject) JSON.toJSON(bData);
		dataMap.putAll(bjo);
		
		//路试
		Map<String, Object>  rData = zhCheckDataManager.getRData(vehCheckLogin, vehCheckLogin.getJycs());
		JSONObject rjo =(JSONObject) JSON.toJSON(rData);
		dataMap.putAll(rjo);
		
		//动力性性
		Map<String, Object>  dlxData =  zhCheckDataManager.getDLXData(vehCheckLogin, vehCheckLogin.getJycs());
		JSONObject dlxjo =(JSONObject) JSON.toJSON(dlxData);
		if(dlxjo!=null) {
			dataMap.putAll(dlxjo);
		}
		
		
		//悬架
		Map<String, Object>  xjData =  zhCheckDataManager.getXJData(vehCheckLogin, vehCheckLogin.getJycs());
		JSONObject xjjo =(JSONObject) JSON.toJSON(xjData);
		if(xjjo!=null) {
			dataMap.putAll(xjjo);
		}
		logger.info(xjjo);
		
		
		//声级计
		Map<String, Object>  sjjData =  zhCheckDataManager.getSJJData(vehCheckLogin, vehCheckLogin.getJycs());
		JSONObject sjjjo =(JSONObject) JSON.toJSON(sjjData);
		if(sjjjo!=null) {
			dataMap.putAll(sjjjo);
		}
		
		
		//排放性
		Map<String, Map<String, Object>>  pfxData =  zhCheckDataManager.getPFXData(vehCheckLogin);
		JSONObject pfxjo =(JSONObject) JSON.toJSON(pfxData);
		
		logger.info("pfxjo:"+pfxjo);
		
		if(pfxjo!=null) {
			
			if(pfxjo.containsKey("sds")) {
				dataMap.put("pfx1pd", pfxjo.getJSONObject("sds").getString("SFHG").equals("true")?"○":"X");
			}
			if(pfxjo.containsKey("wt")) {
				dataMap.put("pfx1pd", pfxjo.getJSONObject("wt").getString("SFHG").equals("true")?"○":"X");
			}
			
			if(pfxjo.containsKey("lgd")) {
				dataMap.put("pfx2pd", pfxjo.getJSONObject("lgd").getString("SFHG").equals("true")?"○":"X");
			}
			
			if(pfxjo.containsKey("yd")) {
				dataMap.put("pfx2pd", pfxjo.getJSONObject("yd").getString("SFHG").equals("true")?"○":"X");
			}
			
			if(pfxjo.containsKey("vm")) {
				dataMap.put("pfx1pd", pfxjo.getJSONObject("vm").getString("SFHG").equals("1")?"○":"X");
			}
			
			dataMap.putAll(pfxjo);
		}
		
		SimpleDateFormat sdfC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		List<VehCheckProcess> bprocess = zhCheckDataManager.getVehProcessByLsh(lsh, "B");		
		if(!CollectionUtils.isEmpty(bprocess)) {
			dataMap.put("zdjckssj", sdfC.format(bprocess.get(0).getKssj()));
			dataMap.put("zdjcjssj", sdfC.format(bprocess.get(bprocess.size()-1).getKssj()));
		}
		
		List<VehCheckProcess> bprocessD = zhCheckDataManager.getVehProcessByLsh(lsh, "H");		
		if(!CollectionUtils.isEmpty(bprocessD)) {
			dataMap.put("dgjckssj", sdfC.format(bprocessD.get(0).getKssj()));
			dataMap.put("dgjcjssj", sdfC.format(bprocessD.get(bprocessD.size()-1).getKssj()));
		}
		
		List<DeviceCheckJudegZJ> reports = zhCheckDataManager.getDeviceCheckJudegZJ(lsh);
		
		String jl="壹级车";
		
		for(DeviceCheckJudegZJ dzj:reports) {
			if(BaseDeviceData.PDJG_BHG.toString().equals(dzj.getYqjgpd())) {
				jl="不合格";
				break;
			} 
		}
		if(jl.equals("壹级车")) {
			for(DeviceCheckJudegZJ dzj:reports) {
				if(dzj.getYqjgpd().equals("2级")||dzj.getYqjgpd().equals("二级")) {
					jl="贰级车";
					break;
				}
			}
		}
		dataMap.put("zjjl",jl); 
		
		//dataMap.put("uplinedate", dataMap.get("upLineDate"));
		
//		InputStream zdgwzp = zhCheckDataManager.getIamge(lsh, "0322");
//		InputStream dggwzp = zhCheckDataManager.getIamge(lsh, "0321");
//		InputStream dlxjygwzp = zhCheckDataManager.getIamge(lsh, "0999");
		
		
//		logger.info("**************zdgwzp:"+zdgwzp);
//		if(zdgwzp!=null) {
//			dataMap.put("zdgwzp", zdgwzp);
//		}
//		if(dggwzp!=null) {
//			dataMap.put("dggwzp", dggwzp);
//		}
//		if(dlxjygwzp!=null) {
//			dataMap.put("dlxjygwzp", dlxjygwzp);
//		}
		try {
			Properties prop = new Properties();
	        prop.load(this.getClass().getClassLoader().getResourceAsStream("veh.properties"));
	        String jczUrl = prop.getProperty("jczjk.url");
	        String imageUrl = prop.getProperty("image.save.url");
	        logger.info("jczUrl:"+jczUrl);
	        logger.info("dataMap:"+dataMap);
			String result = restTemplate.postForEntity(jczUrl+"recordadd", dataMap, String.class).getBody();
			logger.info("result:"+result);
			JSONObject jobj = JSONObject.parseObject(result);
			//保存二维码图片
			if("1".equals(jobj.getString("code"))) {
				File ewmFile = new File(imageUrl+dataMap.getString("bgdbh")+".jpg");
				if(ewmFile.exists()) {
					CheckPhoto checkPhoto = new CheckPhoto();
					checkPhoto.setClsbdh(dataMap.getString("clsbdh"));
					checkPhoto.setHphm(dataMap.getString("hphm"));
					checkPhoto.setHpzl(dataMap.getString("hpzl"));
					checkPhoto.setJylsh(dataMap.getString("jylsh"));
					checkPhoto.setJcxdh(dataMap.getString("jcxdh"));
					checkPhoto.setJyjgbh(dataMap.getString("jyjgbh"));
					checkPhoto.setPssj(new Date());
					checkPhoto.setJycs(1);
					checkPhoto.setStatus(0);
					checkPhoto.setZpzl("9998");
					checkPhoto.setZp(this.File2byte(ewmFile));
					Message message = externalCheckManager.savePhoto(checkPhoto);
				}
			}
			testVeh.setErrorCode(jobj.getString("code"));
			testVeh.setErrorMsg(jobj.getString("status"));
			logger.info(result);
		}catch(Exception e) {
			testVeh.setErrorCode("-99");
			testVeh.setErrorMsg("系统发生异常"+e.getMessage());
			logger.info("系统发生异常", e);
		}
		
		this.testVehManager.updateTestVeh(testVeh);		
		return "success";
	}
	
	public static byte[] File2byte(File tradeFile){
	    byte[] buffer = null;
	    try
	    {
	        FileInputStream fis = new FileInputStream(tradeFile);
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        byte[] b = new byte[1024];
	        int n;
	        while ((n = fis.read(b)) != -1)
	        {
	            bos.write(b, 0, n);
	        }
	        fis.close();
	        bos.close();
	        buffer = bos.toByteArray();
	    }catch (FileNotFoundException e){
	        e.printStackTrace();
	    }catch (IOException e){
	        e.printStackTrace();
	    }
	    return buffer;
	}
	
	
	@UserOperation(code="photoUppload",name="动车综检联网图片上传")
	@RequestMapping(value = "photoUppload", method = RequestMethod.POST)
	public @ResponseBody String photoUppload(String lsh) throws IOException {
		Map imageMap = new HashMap();
		
		TestVeh testVeh = zhCheckDataManager.getTestVehbyJylsh(lsh);
		imageMap.put("bgdbh", testVeh.getBgdbh());
//		TestVeh testVeh = new TestVeh();
//		testVeh.setBgdbh("321303200122801");
		//车辆左前方斜视45度照片
		InputStream zq45 = zhCheckDataManager.getIamge(lsh, "0111");
		String fileName = "";
		if(zq45 != null) {
			Date pasj = zhCheckDataManager.getImageTime(lsh, "0111");
			fileName = getFileName("wj1",testVeh.getBgdbh(),pasj);
			saveImg(zq45,fileName);
			imageMap.put("wj1", fileName);
			//FtpUtils.uploadFile("", fileName, zq45, ftpClient);
		}
		
		//车辆右后方斜视45度照片
		InputStream yh45 = zhCheckDataManager.getIamge(lsh, "0112");
		if(yh45 != null) {
			Date pasj = zhCheckDataManager.getImageTime(lsh, "0112");
			fileName = getFileName("wj2",testVeh.getBgdbh(),pasj);
			saveImg(yh45,fileName);
			imageMap.put("wj2", fileName);
			//FtpUtils.uploadFile("", fileName, yh45, ftpClient);
		}
		
		//车架号0113
		InputStream cjh = zhCheckDataManager.getIamge(lsh, "0113");
		if(cjh != null) {
			Date pasj = zhCheckDataManager.getImageTime(lsh, "0113");
			fileName = getFileName("wj3",testVeh.getBgdbh(),pasj);
			saveImg(cjh,fileName);
			imageMap.put("wj3", fileName);
			//FtpUtils.uploadFile("", fileName, cjh, ftpClient);
		}
		
		//制动检测
		InputStream zdgwzp = zhCheckDataManager.getIamge(lsh, "0322");
		if(zdgwzp != null) {
			Date pasj = zhCheckDataManager.getImageTime(lsh, "0322");
			fileName = getFileName("zd",testVeh.getBgdbh(),pasj);
			saveImg(zdgwzp,fileName);
			imageMap.put("zd", fileName);
			//FtpUtils.uploadFile("", fileName, zdgwzp, ftpClient);
		}
		////灯光检测
		InputStream dggwzp = zhCheckDataManager.getIamge(lsh, "0321");
		if(dggwzp != null) {
			Date pasj = zhCheckDataManager.getImageTime(lsh, "0321");
			fileName = getFileName("dg",testVeh.getBgdbh(),pasj);
			saveImg(dggwzp,fileName);
			imageMap.put("dg", fileName);
			//FtpUtils.uploadFile("", fileName, dggwzp, ftpClient);
		}
		//动力性
		InputStream dlxjygwzp = zhCheckDataManager.getIamge(lsh, "0999");
		if(dlxjygwzp != null) {
			Date pasj = zhCheckDataManager.getImageTime(lsh, "0999");
			fileName = getFileName("dl",testVeh.getBgdbh(),pasj);
			saveImg(dlxjygwzp,fileName);
			imageMap.put("dl", fileName);
			//FtpUtils.uploadFile("", fileName, dlxjygwzp, ftpClient);
		}
		
		//底盘		
		InputStream dp = zhCheckDataManager.getIamge(lsh, "0323");
		if(dp != null) {
			Date pasj = zhCheckDataManager.getImageTime(lsh, "0323");
			fileName = getFileName("dp",testVeh.getBgdbh(),pasj);
			saveImg(dp,fileName);
			imageMap.put("dp", fileName);
			//FtpUtils.uploadFile("", fileName, dp, ftpClient);
		}
		
		//行驶证
		InputStream xsz = zhCheckDataManager.getIamge(lsh, "0201");
		if(xsz != null) {
			Date pasj = zhCheckDataManager.getImageTime(lsh, "0201");
			fileName = getFileName("xsz",testVeh.getBgdbh(),pasj);
			saveImg(xsz,fileName);
			imageMap.put("xsz", fileName);
			//FtpUtils.uploadFile("", fileName, xsz, ftpClient);
		}
		Properties prop = new Properties();
        prop.load(this.getClass().getClassLoader().getResourceAsStream("veh.properties"));
        String jczUrl = prop.getProperty("jczjk.url");
        logger.info("jczUrl:"+jczUrl);
        logger.info("imageMap:"+imageMap);
		this.restTemplate.postForEntity(jczUrl+"uploadPhoto", imageMap, String.class).getBody();
		return "success";
	}
	
	public String getFileName(String fix,String bgdbh,Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String dateStr = sdf.format(date);
		return fix+"_"+dateStr+"_"+bgdbh+".jpg";
		//return fix+"_19000101000000_"+bgdbh+".jpg";
	}
	
//	public FTPClient initFtpClient() throws IOException {
//		Properties prop = new Properties();
//        prop.load(this.getClass().getClassLoader().getResourceAsStream("veh.properties"));
//        String hostname = prop.getProperty("ftp.photo.hostname");
//        String username = prop.getProperty("ftp.photo.username");
//        String password = prop.getProperty("ftp.photo.password");
//        FTPClient ftpClient = new FTPClient();
//        ftpClient.setControlEncoding("utf-8");
//        try {
//            System.out.println("connecting...ftp服务器:"+hostname+":"+21); 
//            ftpClient.connect(hostname, 21); //连接ftp服务器
//            ftpClient.login(username, password); //登录ftp服务器
//            int replyCode = ftpClient.getReplyCode(); //是否成功登录服务器
//            if(!FTPReply.isPositiveCompletion(replyCode)){
//                System.out.println("connect failed...ftp服务器:"+hostname+":"+21); 
//            }
//            System.out.println("connect successfu...ftp服务器:"+hostname+":"+21); 
//        }catch (MalformedURLException e) { 
//           e.printStackTrace(); 
//        }catch (IOException e) { 
//           e.printStackTrace(); 
//        } 
//        return ftpClient;
//    }
	
	public void saveImg(InputStream inputStream,String filename) throws IOException {
		Properties prop = new Properties();
        prop.load(this.getClass().getClassLoader().getResourceAsStream("veh.properties"));
        String imageUrl = prop.getProperty("image.save.url");
		// 定义一个文件名字进行接收获取文件
		FileOutputStream fileOut = new FileOutputStream(new File(imageUrl + filename));
		byte[] buf = new byte[1024 * 8];
		while (true) {
			int read = 0;
			if (inputStream != null) {
				read = inputStream.read(buf);
			}
			if (read == -1) {
				break;
			}
			fileOut.write(buf, 0, read);
		}
		fileOut.flush();
	}

}
