package com.xs.veh.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.enums.CommonUserOperationEnum;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.entity.VideoConfig;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.manager.VideoManager;
import com.xs.veh.util.ConvertVideo;
import com.xs.veh.util.FileUtil;
import com.xs.veh.util.HKVisionUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


@Controller
@RequestMapping(value = "/video")
@Modular(modelCode="video",modelName="视频配置",isEmpowered=false)
public class VideoController {
	
	private static Logger logger = Logger.getLogger(VideoController.class);
	
	@Resource(name="videoManager")
	private VideoManager videoManager;
	@Value("${jyjgbh}")
	private String jyjgbh_sys;
	
	@Value("${jyjgmc}")
	private String jyjgmc_sys;
	
	@Autowired
	private CheckDataManager checkDataManager;
	
	@RequestMapping(value = "play")
	@UserOperation(code="play",name="播放")
	public @ResponseBody Map getPlayInfo(HttpServletRequest request ,String jylsh) {
		Map map = new HashMap();
		List<Map> list = videoManager.getProcessDataByLsh(jylsh);
		
		if(list==null||list.isEmpty()){
			return map;
		}
		//处理路试
		isRoadTest(list);
		
		String jyjgbh=list.get(0).get("JYJGBH").toString();
		String jcxdh =list.get(0).get("JCXDH").toString();
		
		List<VideoConfig> conifgs = videoManager.getConfig(jyjgbh);
		
		JSONArray ja=new JSONArray();
		
		for(Map item:list){
			String jyxm = (String)item.get("JYXM");
			if(jyxm!=null){
				for(VideoConfig vc:conifgs){
					if(vc.getJyxm().indexOf(jyxm)!=-1&&vc.getJcxdh().equals(jcxdh)){
						JSONObject jo = JSONObject.fromObject(vc);
						Date kssj=(Date)item.get("KSSJ");
						Date jssj=(Date)item.get("JSSJ");
						Integer jycs=(Integer)item.get("JYCS");
						String fzjg=(String)item.get("FZJG");
						String hphm=(String)item.get("HPHM");
						if(fzjg!=null){
							hphm=fzjg.substring(0, 1)+(String)item.get("HPHM");
						}
						SimpleDateFormat sd=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						
						jo.put("kssj",sd.format(kssj));
						jo.put("jssj",sd.format(jssj));
						jo.put("hphm",hphm);
						jo.put("jyxm", jyxm);
						jo.put("jycs", jycs.intValue());
						ja.add(jo);
						break;
					}
				}
			}
		}
		
		map.put("playInfo", ja.toString());
		map.put("jyjgmc", jyjgmc_sys);
		return map;
	}
	
	
	@RequestMapping(value = "play2",method= {RequestMethod.POST,RequestMethod.GET})
	@UserOperation(code="play2",name="视频地址",userOperationEnum=CommonUserOperationEnum.NoLogin)
	public String getPlayInfo2(HttpServletRequest request ,String jylsh) {
		
		List<Map> list = videoManager.getProcessDataByLsh(jylsh);
		
		if(list==null||list.isEmpty()){
			return "video2";
		}
		//处理路试
		isRoadTest(list);
		
		String jyjgbh=list.get(0).get("JYJGBH").toString();
		//String jcxdh =list.get(0).get("JCXDH").toString();
		
		List<VideoConfig> conifgs = videoManager.getConfig(jyjgbh);
		
		JSONArray ja=new JSONArray();
		
		for(Map item:list){
			String jyxm = (String)item.get("JYXM");
			if(jyxm!=null){
				
				if(jyxm.equals("F2")||jyxm.equals("F3")||jyxm.equals("F4")) {
					JSONObject jo =new JSONObject();
					Date kssj=(Date)item.get("KSSJ");
					Date jssj=(Date)item.get("JSSJ");
					
					Integer jycs=(Integer)item.get("JYCS");
					
					String fzjg=(String)item.get("FZJG");
					String hphm=(String)item.get("HPHM");
					if(fzjg!=null){
						hphm=fzjg.substring(0, 1)+(String)item.get("HPHM");
					}
					SimpleDateFormat sd=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					
					jo.put("kssj",sd.format(kssj));
					jo.put("jssj",sd.format(jssj));
					jo.put("hphm",hphm);
					jo.put("jyxm", jyxm);
					jo.put("jycs", jycs.intValue());
					ja.add(jo);
				}else {
					for(VideoConfig vc:conifgs){
						if(vc.getJyxm().indexOf(jyxm)!=-1&&vc.getJcxdh().equals(item.get("JCXDH").toString())){
							JSONObject jo = JSONObject.fromObject(vc);
							Date kssj=(Date)item.get("KSSJ");
							Date jssj=(Date)item.get("JSSJ");
							
							Integer jycs=(Integer)item.get("JYCS");
							
							String fzjg=(String)item.get("FZJG");
							String hphm=(String)item.get("HPHM");
							if(fzjg!=null){
								hphm=fzjg.substring(0, 1)+(String)item.get("HPHM");
							}
							SimpleDateFormat sd=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							
							jo.put("kssj",sd.format(kssj));
							jo.put("jssj",sd.format(jssj));
							jo.put("hphm",hphm);
							jo.put("jyxm", jyxm);
							jo.put("jycs", jycs.intValue());
							ja.add(jo);
							//break;
						}
					}
				}
				
				
			}
		}
		request.setAttribute("playInfo", ja.toString());
		System.out.println("video2");
		return "video2";
	}
	
	
	@RequestMapping(value = "getConfig", method = RequestMethod.POST)
	@UserOperation(code="getConfig",name="查询视频配置")
	public @ResponseBody Map  getConfig(VideoConfig vc) {
		List list = videoManager.getConfig(vc.getJyjgbh());
		Map map =new HashMap();
		map.put("total", list.size());
		map.put("rows", list);
		return map;
	}
	
	@RequestMapping(value = "saveConfig", method = RequestMethod.POST)
	@UserOperation(code="saveConfig",name="保存视频配置")
	public @ResponseBody String  saveConfig(VideoConfig vc) {
		 videoManager.saveConfig(vc);
		Map<String,Object> map =new HashMap<String,Object>();
		map.put("status", 1);
		map.put("message", "保存成功");
		return JSONObject.fromObject(map).toString();
	}
	
	@RequestMapping(value = "deleteConfig", method = RequestMethod.POST)
	@UserOperation(code="deleteConfig",name="删除视频配置")
	public @ResponseBody String  deleteConfig(VideoConfig vc) {
		 videoManager.deleteConfig(vc);
		Map<String,Object> map =new HashMap<String,Object>();
		map.put("status", 1);
		map.put("message", "删除成功");
		return JSONObject.fromObject(map).toString();
	}
	
	/**
	 * 
	 * @param list
	 */
	private void isRoadTest(List<Map> list){
		
		for(Map map : list){
			String  jcxdh =(String)map.get("JCXDH");
			if(jcxdh==null || "".equals(jcxdh)){
				map.put("JCXDH", "0");
			}
		}
		
	}
	
	@RequestMapping(value = "getAllVideoConfig", method = RequestMethod.POST)
	@UserOperation(code="getAllVideoConfig",name="查询所有视频配置")
	public @ResponseBody Map  getAllVideoConfig() {
		List<VideoConfig> list = videoManager.getConfig("");
		Map map = new HashMap();
		map.put("vcList", list);
		map.put("jyjgmc", jyjgmc_sys);
		return map;
	}
	
	@RequestMapping(value = "getJyjgmc", method = RequestMethod.POST)
	@UserOperation(code="getJyjgmc",name="获取检测站名称")
	public @ResponseBody String  getJyjgmc() {
		return jyjgmc_sys;
	}
	
	
	@RequestMapping(value = "uploadVideo", method = RequestMethod.POST)
	@UserOperation(code="uploadVideo",name="视频上传")
	public @ResponseBody String  uploadVideo(@RequestParam("videoFile") MultipartFile videoFile,VehCheckProcess vcp) throws Exception {
		
		try {
			logger.info("uploadVideo:开始");
			
			VehCheckProcess old = checkDataManager.getVehCheckProces(vcp.getJylsh(), vcp.getJycs(), vcp.getJyxm());
			if(old!=null) {
				old.setKssj(vcp.getKssj());
				old.setJssj(vcp.getJssj());
				vcp=old;
			}
			FileUtil.createDirectory(HKVisionUtil.getConfigPath()+"\\video\\");
			
			String filePath = vcp.getJylsh()+"_"+vcp.getJycs()+"_"+vcp.getJyxm()+"_0";
			
			// 创建文件实例
			File file = new File(HKVisionUtil.getConfigPath()+"\\video\\", filePath+".mp4");
			// 写入文件
			videoFile.transferTo(file);
			boolean vFlag = ConvertVideo.processMp4(HKVisionUtil.getConfigPath()+"\\video\\"+filePath+".mp4", filePath+".mp4");
			if(vFlag) {
				ConvertVideo.copy(ConvertVideo.outputPath+filePath+".mp4", HKVisionUtil.getConfigPath()+"\\video\\");
				File tempFile = new File(ConvertVideo.outputPath, filePath+".mp4");
				tempFile.delete();
			}
			checkDataManager.saveOrUpdateProcess(vcp);
			Map<String,Object> map =new HashMap<String,Object>();
			map.put("status", 1);
			map.put("message", "上传成功");
			logger.info("uploadVideo:结束");
			return JSONObject.fromObject(map).toString();
		}catch (Exception e) {
			logger.error("上传视频异常",e);
			throw e;
		}
		
	}
	

}
