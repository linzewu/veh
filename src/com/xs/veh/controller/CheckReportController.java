package com.xs.veh.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aspose.words.CellCollection;
import com.aspose.words.Document;
import com.aspose.words.Node;
import com.aspose.words.NodeCollection;
import com.aspose.words.NodeType;
import com.aspose.words.Range;
import com.aspose.words.Table;
import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.common.Constant;
import com.xs.common.ResultHandler;
import com.xs.common.Sql2WordUtil;
import com.xs.veh.entity.BaseParams;
import com.xs.veh.entity.TestVeh;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.manager.VehManager;
import com.xs.veh.manager.ZHCheckDataManager;

import net.sf.json.JSONArray;

@Controller
@RequestMapping(value = "/checkReport",produces="application/json")
@Modular(modelCode="checkReport",modelName="车辆性能检验",isEmpowered=false)
public class CheckReportController {
	
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
			
			
			//声级计
			Map<String, Object>  sjjData =  zhCheckDataManager.getSJJData(vehCheckLogin, vehCheckLogin.getJycs());
			JSONObject sjjjo =(JSONObject) JSON.toJSON(sjjData);
			if(sjjjo!=null) {
				dataMap.putAll(sjjjo);
			}
			
			
			//排放性
			Map<String, Map<String, Object>>  pfxData =  zhCheckDataManager.getPFXData(vehCheckLogin);
			JSONObject pfxjo =(JSONObject) JSON.toJSON(pfxData);
			if(pfxjo!=null) {
				dataMap.putAll(pfxjo);
			}
			
			
			
			
			Map<String, List<BaseParams>> bpsMap = (Map<String, List<BaseParams>>) servletContext.getAttribute("bpsMap");
			String template = "道路运输车辆性能检验记录单.docx";
			fileName = "template_performance_record"+lsh+".jpg";
			Document doc = Sql2WordUtil.map2WordUtil(template, dataMap,bpsMap);
			
			NodeCollection tableCollection = doc.getChildNodes(NodeType.TABLE, true);
			for(Node node:tableCollection.toArray()) {
				Table table = (Table) node;
				table.getText();
			}
			
			doc.save(filePath+"template_performance_record"+lsh+".doc");
			Sql2WordUtil.toCase(doc, filePath, fileName);
			
			
		
		return ResultHandler.toMyJSON(Constant.ConstantState.STATE_SUCCESS, "打印道路运输车辆性能检验记录单成功", fileName);
	}
	
	
	public  Document prcessTable(Table table,JSONArray jo) throws Exception {
		
		for(int i=0;i<jo.size();i++) {
			
			jo.getJSONObject(i);
			
			 Node deepClone = table.getLastRow().deepClone(true);
			 CellCollection cells = table.getLastRow().getCells();
	         
			// cells.get(0).getRange().replace("", jo.getJSONObject(i), true, true);
			
		}
		
		
		return null;
	}
	
	
	@UserOperation(code = "printJyBgReport", name = "打印道路运输车辆性能检验报告单")
	@RequestMapping(value = "printJyBgReport", method = RequestMethod.POST)
	public @ResponseBody Map printJyBgReport(String lsh) throws Exception {
		String basePath = "cache/report/";
		String filePath = request.getSession().getServletContext().getRealPath("/") + basePath;
				String fileName = "";
			

				//PreCarRegister bcr = this.preCarRegisterManager.findPreCarRegisterByLsh(lsh);
				Map<String,Object> data =new HashMap<String,Object>();
//				data.put("lshCode", BarcodeUtil.generateInputStream(bcr.getLsh()));
//				if(StringUtils.isEmpty(bcr.getHphm())) {
//					data.put("hphm", bcr.getClsbdh());
//				}
				
				VehCheckLogin vehCheckLogin = vehManager.getVehCheckLoginByJylsh(jyjgbh, lsh);
				JSONObject map1 = (JSONObject)JSON.toJSON(vehCheckLogin);
				
				
				Map<String, List<BaseParams>> bpsMap = (Map<String, List<BaseParams>>) servletContext.getAttribute("bpsMap");
				String template = "道路运输车辆综合性能检验报告单.docx";
				fileName = "template_performance_record"+new Date().getTime()+".jpg";
				com.aspose.words.Document doc = Sql2WordUtil.map2WordUtil(template, data,bpsMap);
				Sql2WordUtil.toCase(doc, filePath, fileName);
				
		
		return ResultHandler.toMyJSON(Constant.ConstantState.STATE_SUCCESS, "打印道路运输车辆性能检验报告单成功", fileName);
	}

}
