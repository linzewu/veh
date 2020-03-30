package com.xs.veh.controller;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aspose.words.CellCollection;
import com.aspose.words.Document;
import com.aspose.words.Node;
import com.aspose.words.NodeType;
import com.aspose.words.Table;
import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.common.Constant;
import com.xs.common.ResultHandler;
import com.xs.common.Sql2WordUtil;
import com.xs.veh.entity.BaseParams;
import com.xs.veh.entity.DeviceCheckJudegZJ;
import com.xs.veh.entity.TestResult;
import com.xs.veh.entity.TestVeh;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.manager.VehManager;
import com.xs.veh.manager.ZHCheckDataManager;
import com.xs.veh.network.data.BaseDeviceData;


@Controller
@RequestMapping(value = "/checkReport",produces="application/json")
@Modular(modelCode="checkReport",modelName="车辆性能检验",isEmpowered=false)
public class CheckReportController {
	
	Logger logger = Logger.getLogger(CheckReportController.class);
	
	@Value("${jyjgbh}")
	private String jyjgbh;

	@Value("${sf}")
	private String sf;

	@Value("${cs}")
	private String cs;

	
	//@Value("${stu.cache.dir}")
	private String cacheDir = "D:\\cache";
	
	@Autowired
	private ServletContext servletContext;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private VehManager vehManager;
	
	@Autowired
	private CheckDataManager checkDataManager;
	
	@Autowired
	private ZHCheckDataManager zhCheckDataManager;
	
	@UserOperation(code = "printJyReport", name = "打印道路运输车辆性能检验记录单")
	@RequestMapping(value = "printJyReport", method = RequestMethod.POST)
	public @ResponseBody Map printJyReport(String lsh) throws Exception {
		String basePath = "cache/report/";
		String filePath = request.getSession().getServletContext().getRealPath("/") + basePath;
			String fileName = "";
			VehCheckLogin vehCheckLogin = checkDataManager.getVehCheckLogin(lsh);
			TestVeh testVeh = zhCheckDataManager.getTestVehbyJylsh(lsh);
			JSONObject dataMap =(JSONObject)JSON.toJSON(vehCheckLogin);
			if(testVeh!=null) {
				JSONObject testVehMap = (JSONObject)JSON.toJSON(testVeh);
				dataMap.putAll(testVehMap);
			}
			
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
				
				dataMap.putAll(pfxjo);
			}
			
			dataMap.put("uplinedate", dataMap.get("upLineDate"));
			
			InputStream zdgwzp = zhCheckDataManager.getIamge(lsh, "0348");
			InputStream dggwzp = zhCheckDataManager.getIamge(lsh, "0322");
			InputStream dlxjygwzp = zhCheckDataManager.getIamge(lsh, "0999");
			
			if(zdgwzp!=null) {
				dataMap.put("zdgwzp", zdgwzp);
			}
			if(dggwzp!=null) {
				dataMap.put("dggwzp", dggwzp);
			}
			if(dlxjygwzp!=null) {
				dataMap.put("dlxjygwzp", dlxjygwzp);
			}
			
			
			
			
			Map<String, List<BaseParams>> bpsMap = (Map<String, List<BaseParams>>) servletContext.getAttribute("bpsMap");
			String template = "道路运输车辆性能检验记录单.docx";
			fileName = "template_performance_record"+lsh+".jpg";
			
			logger.info(dataMap);
			
			Document doc = Sql2WordUtil.map2WordUtil(template, dataMap,bpsMap);
			
			doc.save(filePath+"template_performance_record"+lsh+".doc");
			Sql2WordUtil.toCase(doc, filePath, fileName);
			
			
		
		return ResultHandler.toMyJSON(Constant.ConstantState.STATE_SUCCESS, "打印道路运输车辆性能检验记录单成功", fileName);
	}
	
	
	public  void prcessTable(Table table,JSONArray jo) throws Exception {
		
		for(int i=0;i<jo.size();i++) {
			
			 String xh =  jo.getJSONObject(i).get("xh").toString();
			 String yqjyxm = (String) jo.getJSONObject(i).get("yqjyxm");
			 String yqjyjg = (String) jo.getJSONObject(i).get("yqjyjg");
			 String yqbzxz = (String) jo.getJSONObject(i).get("yqbzxz");
			 String yqjgpd = (String) jo.getJSONObject(i).get("yqjgpd");
			
			 Node deepClone = table.getLastRow().deepClone(true);
			 CellCollection cells = table.getLastRow().getCells();
			 cells.get(0).getRange().replace("xh", xh, true, true);
			 cells.get(1).getRange().replace("yqjyxm", yqjyxm, true, true);
			 cells.get(2).getRange().replace("yqjyjg", yqjyjg, true, true);
			 cells.get(3).getRange().replace("yqbzxz", yqbzxz, true, true);
			 cells.get(4).getRange().replace("yqjgpd", getPD(yqjgpd), true, true);
			 i++;
			 
			 if(i<jo.size()) {
				  xh =  jo.getJSONObject(i).get("xh").toString();
				  yqjyxm = (String) jo.getJSONObject(i).get("yqjyxm");
				  yqjyjg = (String) jo.getJSONObject(i).get("yqjyjg");
				  yqbzxz = (String) jo.getJSONObject(i).get("yqbzxz");
				  yqjgpd = (String) jo.getJSONObject(i).get("yqjgpd");
				 cells.get(5).getRange().replace("xh", xh, true, true);
				 cells.get(6).getRange().replace("yqjyxm", yqjyxm, true, true);
				 cells.get(7).getRange().replace("yqjyjg", yqjyjg, true, true);
				 cells.get(8).getRange().replace("yqbzxz", yqbzxz, true, true);
				 cells.get(9).getRange().replace("yqjgpd", getPD(yqjgpd), true, true);
			 }else {
				 cells.get(5).getRange().replace("xh", "—", true, true);
				 cells.get(6).getRange().replace("yqjyxm", "—", true, true);
				 cells.get(7).getRange().replace("yqjyjg", "—", true, true);
				 cells.get(8).getRange().replace("yqbzxz", "—", true, true);
				 cells.get(9).getRange().replace("yqjgpd", "—", true, true);
			 }
			 
			 table.getRows().add(deepClone);
		}
		table.getLastRow().remove();
	}
	
	private String getPD(String pd) {
		if(pd.equals(BaseDeviceData.PDJG_HG.toString())) {
			return "合格";
		}else if(pd.equals(BaseDeviceData.PDJG_BHG.toString())) {
			return "不合格";
		}else if(pd.equals(BaseDeviceData.PDJG_WJ.toString())) {
			return "—";
		}else {
			return pd;
		}
		
	}
	
	
	@UserOperation(code = "printJyBgReport", name = "打印道路运输车辆性能检验报告单")
	@RequestMapping(value = "printJyBgReport", method = RequestMethod.POST)
	public @ResponseBody Map printJyBgReport(String lsh) throws Exception {
		String basePath = "cache/report/";
		String filePath = request.getSession().getServletContext().getRealPath("/") + basePath;
		String fileName = "";
		VehCheckLogin vehCheckLogin = checkDataManager.getVehCheckLogin(lsh);
		TestVeh testVeh = zhCheckDataManager.getTestVehbyJylsh(lsh);
		JSONObject dataMap =(JSONObject)JSON.toJSON(vehCheckLogin);
		
		if(testVeh!=null) {
			JSONObject testVehMap = (JSONObject)JSON.toJSON(testVeh);
			dataMap.putAll(testVehMap);
		}
		zhCheckDataManager.createDeviceCheckJudeg(vehCheckLogin);
		
		List<DeviceCheckJudegZJ> reports = zhCheckDataManager.getDeviceCheckJudegZJ(lsh);
		
		TestResult testResult = zhCheckDataManager.getTestResultBylsh(lsh);
		
		if(testResult!=null) {
			dataMap.put("hjwd",testResult.getHjwd());
			dataMap.put("hjsd",testResult.getHjsd());
			dataMap.put("dqy",testResult.getDqy());
		}
		
		
		Map<String, List<BaseParams>> bpsMap = (Map<String, List<BaseParams>>) servletContext.getAttribute("bpsMap");
		
		List<BaseParams> params = bpsMap.get("csys");
		
		if(!CollectionUtils.isEmpty(params)) {
			String csys =(String) dataMap.get("csys");
			
			String newCsys = "";
			
			if(!StringUtils.isEmpty(csys)) {
				for(char c:csys.toCharArray()) {
					for(BaseParams p: params) {
						if(p.getParamValue().equals(String.valueOf(c))) {
							newCsys+=p.getParamName();
						}
					}
					
				}
			}
			
			if(!StringUtils.isEmpty(newCsys)) {
				dataMap.put("csys",newCsys);
			}
		}
		
		String template = "道路运输车辆综合性能检验报告单.docx";
		fileName = "template_performance_record_bg_"+lsh+".jpg";
		Document doc = Sql2WordUtil.map2WordUtil(template, dataMap,bpsMap);
		
		if(!CollectionUtils.isEmpty(reports)) {
			JSONArray jsonArray =(JSONArray) JSON.toJSON(reports);
			Table table = (Table) doc.getChild(NodeType.TABLE, 1, true);
			prcessTable(table, jsonArray);
		}
		doc.save(filePath+"template_performance_record_bg_"+lsh+".doc");
		Sql2WordUtil.toCase(doc, filePath, fileName);
		
		
		return ResultHandler.toMyJSON(Constant.ConstantState.STATE_SUCCESS, "打印道路运输车辆性能检验报告单成功", fileName);
	}

}
