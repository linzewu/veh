package com.xs.veh.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.dom4j.DocumentException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xs.common.Message;
import com.xs.common.ResultHandler;
import com.xs.veh.entity.CheckPhoto;
import com.xs.veh.entity.ExternalCheck;
import com.xs.veh.entity.RoadCheck;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.manager.CheckEventManger;
import com.xs.veh.manager.ExternalCheckManager;
import com.xs.veh.manager.VehManager;
import com.xs.veh.network.TakePicture;

@Controller
@RequestMapping(value = "/pda")
public class PDAServiceController {

	@Resource(name = "vehManager")
	private VehManager vehManager;

	@Resource(name = "externalCheckManager")
	private ExternalCheckManager externalCheckManager;

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;

	@Resource(name = "checkEventManger")
	private CheckEventManger checkEventManger;

	@RequestMapping(value = "getCheckList")
	public @ResponseBody String getCheckList(HttpServletRequest request, @RequestParam Integer status)
			throws JsonProcessingException {
		List<VehCheckLogin> data = vehManager.getVehCheckLoginOfSXZT(status);
		ObjectMapper om = new ObjectMapper();
		String jsonp = ResultHandler.parserJSONP(request, om.writeValueAsString(data));
		return jsonp;
	}

	@RequestMapping(value = "pushVehOnLine")
	public @ResponseBody Map pushVehOnLine(@RequestParam Integer id) {
		Message message = this.vehManager.upLine(id);
		return ResultHandler.toMessage(message);
	}

	@RequestMapping(value = "external", method = RequestMethod.POST)
	public @ResponseBody Map externalUpload(@Valid ExternalCheck externalCheck, BindingResult result)
			throws InterruptedException {
		if (!result.hasErrors()) {
			Message message = externalCheckManager.saveExternalCheck(externalCheck);
			return ResultHandler.toMessage(message);
		} else {
			return ResultHandler.resultHandle(result, null, "校验出错");
		}
	}

	@RequestMapping(value = "externalDC", method = RequestMethod.POST)
	public @ResponseBody Map externalDCUpload(@Valid ExternalCheck externalCheck, BindingResult result)
			throws InterruptedException {
		if (!result.hasErrors()) {
			Message message = externalCheckManager.saveExternalCheckDC(externalCheck);
			return ResultHandler.toMessage(message);
		} else {
			return ResultHandler.resultHandle(result, null, "校验出错");
		}
	}

	@RequestMapping(value = "externalC1", method = RequestMethod.POST)
	public @ResponseBody Map externalC1Upload(@Valid ExternalCheck externalCheck, BindingResult result)
			throws InterruptedException {
		if (!result.hasErrors()) {
			Message message = externalCheckManager.saveExternalCheckC1(externalCheck);
			return ResultHandler.toMessage(message);
		} else {
			return ResultHandler.resultHandle(result, null, "校验出错");
		}
	}

	@RequestMapping(value = "getExternal")
	public @ResponseBody List getExternal(String hphm) {
		List<VehCheckLogin> data = externalCheckManager.getExternalCheckVhe(hphm);
		return data;
	}

	@RequestMapping(value = "getExternalDC")
	public @ResponseBody List getExternalDC(String hphm) {
		List<VehCheckLogin> data = externalCheckManager.getExternalDC(hphm);
		return data;
	}

	@RequestMapping(value = "getExternalC1")
	public @ResponseBody List getExternalC1(String hphm) {
		List<VehCheckLogin> data = externalCheckManager.getExternalC1(hphm);
		return data;
	}
	
	@RequestMapping(value = "getExternalR")
	public @ResponseBody List getExternalR(String hphm) {
		List<VehCheckLogin> data = externalCheckManager.getExternalR(hphm);
		return data;
	}
	
	@RequestMapping(value = "getRoadProcess")
	public @ResponseBody List getRoadProcess(String jylsh) {
		List<VehCheckProcess> data = externalCheckManager.getRoadProcess(jylsh);
		return data;
	}
	
	@RequestMapping(value = "roadProcess")
	public @ResponseBody Map roadProcess(@RequestParam String jylsh,@RequestParam String jyxm,@RequestParam Integer type) {
		List<VehCheckProcess> datas = externalCheckManager.getRoadProcess(jylsh);
		VehCheckLogin vehCheckLogin = this.checkDataManager.getVehCheckLogin(jylsh);
		VehCheckProcess vehCheckProcess=null;
		for(VehCheckProcess process:datas){
			if(jyxm.equals(process.getJyxm())){
				vehCheckProcess=process;
				break;
			}
		}
		if(vehCheckProcess==null){
			return ResultHandler.toMyJSON(2, "未找到检验项目");
		}else{
			if(type==0){
				
				if(jyxm.equals("R1")){
					TakePicture.createNew(vehCheckLogin, "R1", 1000,"0341");
				}
				if(jyxm.equals("R2")){
					TakePicture.createNew(vehCheckLogin, "R2", 1000,"0345");
				}
				vehCheckProcess.setKssj(new Date());
				this.checkDataManager.updateProcess(vehCheckProcess);
				checkEventManger.createEvent(jylsh, vehCheckLogin.getJycs(), "18C55", jyxm, vehCheckLogin.getHphm(),
						vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh());
			}else if(type==1){
				if(jyxm.equals("R1")){
					TakePicture.createNew(vehCheckLogin, "R1", 1000,"0343");
				}
				if(jyxm.equals("R2")){
					TakePicture.createNew(vehCheckLogin, "R2", 1000,"0351");
				}
				vehCheckProcess.setJssj(new Date());
				this.checkDataManager.updateProcess(vehCheckProcess);
			}
		}
		
		
		
		return ResultHandler.toSuccessJSON("时间更新成功");
	}

	@RequestMapping(value = "uploadPhoto")
	public @ResponseBody Map uploadPhoto(@RequestParam("photo") CommonsMultipartFile[] photo,
			@Valid CheckPhoto checkPhoto, BindingResult result) {

		if (!result.hasErrors()) {
			checkPhoto.setZp(photo[0].getBytes());
			Message message = externalCheckManager.savePhoto(checkPhoto);
			return ResultHandler.toMessage(message);
		} else {
			Map msg = ResultHandler.resultHandle(result, null, "校验出错");
			return msg;
		}
	}

	@RequestMapping(value = "processStart")
	public @ResponseBody Map processStart(@RequestParam("jyxm") String jyxm, @RequestParam("jylsh") String jylsh,
			@RequestParam("jycs") Integer jycs) {

		VehCheckProcess vehCheckProcess = checkDataManager.getVehCheckProces(jylsh, jycs, jyxm);
		vehCheckProcess.setKssj(new Date());
		this.checkDataManager.updateProcess(vehCheckProcess);

		VehCheckLogin vehCheckLogin = this.checkDataManager.getVehCheckLogin(jylsh);

		if (vehCheckProcess.getJyxm().equals("C1")) {
			TakePicture.createNew(vehCheckLogin, "C1", 5000);
		}

		checkEventManger.createEvent(jylsh, jycs, "18C55", vehCheckProcess.getJyxm(), vehCheckProcess.getHphm(),
				vehCheckProcess.getHpzl(), vehCheckProcess.getClsbdh());

		this.checkDataManager.updateProcess(vehCheckProcess);
		return ResultHandler.toSuccessJSON("过程开始成功");
	}

	@RequestMapping(value = "getChekcItem")
	public @ResponseBody String getChekcItem(@RequestParam("jylsh") String jylsh, @RequestParam("type") String type)
			throws DocumentException {
		String item = checkEventManger.getCheckItem(jylsh, type);
		return item;
	}
	
	@RequestMapping(value = "getVehInOfHphm")
	public @ResponseBody List getVehInOfHphm(@RequestParam("hphm") String hphm) {
		List<VehCheckLogin> data = externalCheckManager.getVheInfoOfHphm(hphm);
		return data;
	}

}
