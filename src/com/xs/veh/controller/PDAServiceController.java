package com.xs.veh.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
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
import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.common.Message;
import com.xs.common.ResultHandler;
import com.xs.enums.CommonUserOperationEnum;
import com.xs.veh.entity.CheckPhoto;
import com.xs.veh.entity.ExternalCheck;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.manager.CheckEventManger;
import com.xs.veh.manager.ExternalCheckManager;
import com.xs.veh.manager.VehManager;
import com.xs.veh.network.TakePicture;
import com.xs.veh.network.data.CurbWeightData;

@Controller
@RequestMapping(value = "/pda")
@Modular(modelCode="PDAService",modelName="PDA")
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
	@UserOperation(code="getCheckList",name="查询待检列表")
	public @ResponseBody String getCheckList(HttpServletRequest request, @RequestParam Integer status)
			throws JsonProcessingException {
		List<VehCheckLogin> data = vehManager.getVehCheckLoginOfSXZT(status);
		ObjectMapper om = new ObjectMapper();
		String jsonp = ResultHandler.parserJSONP(request, om.writeValueAsString(data));
		return jsonp;
	}

	@UserOperation(code="pushVehOnLine",name="引车上线")
	@RequestMapping(value = "pushVehOnLine")
	public @ResponseBody Map pushVehOnLine(@RequestParam Integer id) {
		Message message = this.vehManager.upLine(id);
		return ResultHandler.toMessage(message);
	}

	@UserOperation(code="external",name="车辆外检")
	@RequestMapping(value = "external", method = RequestMethod.POST)
	public @ResponseBody Map externalUpload(@Valid ExternalCheck externalCheck, BindingResult result)
			throws InterruptedException, IOException {
		if (!result.hasErrors()) {
			Message message = externalCheckManager.saveExternalCheck(externalCheck);
			VehCheckLogin vehCheckLogin = this.checkDataManager.getVehCheckLogin(externalCheck.getJylsh());
			//检测结束，显示屏显示 XX 检测结束
			this.checkDataManager.processEndSendMsg(externalCheck.getHphm(), "F1",Integer.parseInt(vehCheckLogin.getJcxdh()));
			return ResultHandler.toMessage(message);
		} else {
			return ResultHandler.resultHandle(result, null, "校验出错");
		}
	}

	@UserOperation(code="externalDC",name="动态底盘检测")
	@RequestMapping(value = "externalDC", method = RequestMethod.POST)
	public @ResponseBody Map externalDCUpload(@Valid ExternalCheck externalCheck, BindingResult result)
			throws InterruptedException, IOException {
		if (!result.hasErrors()) {
			Message message = externalCheckManager.saveExternalCheckDC(externalCheck);
			
			VehCheckLogin vehCheckLogin = this.checkDataManager.getVehCheckLogin(externalCheck.getJylsh());
			
			TakePicture.createNew(vehCheckLogin, "DC", 1,"0342");
			
			//检测结束，显示屏显示 XX 检测结束
			this.checkDataManager.processEndSendMsg(externalCheck.getHphm(), "DC",Integer.parseInt(vehCheckLogin.getJcxdh()));
			return ResultHandler.toMessage(message);
		} else {
			return ResultHandler.resultHandle(result, null, "校验出错");
		}
	}

	@UserOperation(code="externalC1",name="底盘检测")
	@RequestMapping(value = "externalC1", method = RequestMethod.POST)
	public @ResponseBody Map externalC1Upload(@Valid ExternalCheck externalCheck, BindingResult result)
			throws InterruptedException, IOException {
		if (!result.hasErrors()) {
			Message message = externalCheckManager.saveExternalCheckC1(externalCheck);
			VehCheckLogin vehCheckLogin = this.checkDataManager.getVehCheckLogin(externalCheck.getJylsh());
			//检测结束，显示屏显示 XX 检测结束
			this.checkDataManager.processEndSendMsg(externalCheck.getHphm(), "C1",Integer.parseInt(vehCheckLogin.getJcxdh()));
			return ResultHandler.toMessage(message);
		} else {
			return ResultHandler.resultHandle(result, null, "校验出错");
		}
	}

	@UserOperation(code="getExternal",name="车辆外观待检列表")
	@RequestMapping(value = "getExternal")
	public @ResponseBody List getExternal(String hphm) {
		List<VehCheckLogin> data = externalCheckManager.getExternalCheckVhe(hphm);
		return data;
	}

	@UserOperation(code="getExternalDC",name="车辆动态底盘待检列表")
	@RequestMapping(value = "getExternalDC")
	public @ResponseBody List getExternalDC(String hphm) {
		List<VehCheckLogin> data = externalCheckManager.getExternalDC(hphm);
		return data;
	}

	@UserOperation(code="getExternalC1",name="车辆底盘待检列表")
	@RequestMapping(value = "getExternalC1")
	public @ResponseBody List getExternalC1(String hphm) {
		List<VehCheckLogin> data = externalCheckManager.getExternalC1(hphm);
		return data;
	}
	
	@UserOperation(code="getExternalR",name="路试待检列表")
	@RequestMapping(value = "getExternalR")
	public @ResponseBody List getExternalR(String hphm) {
		List<VehCheckLogin> data = externalCheckManager.getExternalR(hphm);
		return data;
	}
	
	@UserOperation(code="getRoadProcess",name="查询路试过程",isMain=false)
	@RequestMapping(value = "getRoadProcess")
	public @ResponseBody List getRoadProcess(String jylsh) {
		List<VehCheckProcess> data = externalCheckManager.getRoadProcess(jylsh);
		return data;
	}
	
	@UserOperation(code="roadProcess",name="车辆路试")
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
						vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(),vehCheckLogin.getVehcsbj());
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

	@UserOperation(code="uploadPhoto",name="图片上传")
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

	@UserOperation(code="processStart",name="检测过程开始",userOperationEnum=CommonUserOperationEnum.AllLoginUser)
	@RequestMapping(value = "processStart")
	public @ResponseBody Map processStart(@RequestParam("jyxm") String jyxm, @RequestParam("jylsh") String jylsh,
			@RequestParam("jycs") Integer jycs) throws IOException {

		VehCheckProcess vehCheckProcess = checkDataManager.getVehCheckProces(jylsh, jycs, jyxm);
		VehCheckLogin vehCheckLogin = this.checkDataManager.getVehCheckLogin(jylsh);

		//显示屏现在 XX项目 检测中
		checkDataManager.displaySendMsg(vehCheckProcess.getHphm(), jyxm,Integer.parseInt(vehCheckLogin.getJcxdh()));
		
		vehCheckProcess.setKssj(new Date());
		this.checkDataManager.updateProcess(vehCheckProcess);

		
		if (vehCheckProcess.getJyxm().equals("C1")) {
			TakePicture.createNew(vehCheckLogin, "C1", 5000);
		}
		
		if (vehCheckProcess.getJyxm().equals("DC")) {
			TakePicture.createNew(vehCheckLogin, "DC", 5000,"0344");
		}

		checkEventManger.createEvent(jylsh, jycs, "18C55", vehCheckProcess.getJyxm(), vehCheckProcess.getHphm(),
				vehCheckProcess.getHpzl(), vehCheckProcess.getClsbdh(),vehCheckLogin.getVehcsbj());

		this.checkDataManager.updateProcess(vehCheckProcess);
		return ResultHandler.toSuccessJSON("过程开始成功");
	}
	
	
	
	@UserOperation(code="processStart",name="获取检测项目",userOperationEnum=CommonUserOperationEnum.AllLoginUser)
	@RequestMapping(value = "getChekcItem")
	public @ResponseBody String getChekcItem(@RequestParam("jylsh") String jylsh, @RequestParam("type") String type)
			throws DocumentException {
		String item = checkEventManger.getCheckItem(jylsh, type);
		return item;
	}
	
	@UserOperation(code="getVehInOfHphm",name="查询机动车详细信息")
	@RequestMapping(value = "getVehInOfHphm")
	public @ResponseBody List getVehInOfHphm(@RequestParam("hphm") String hphm) {
		List<VehCheckLogin> data = externalCheckManager.getVheInfoOfHphm(hphm);
		return data;
	}
	
	@UserOperation(code="zbzlUpload",name="整备质量上传")
	@RequestMapping(value = "zbzlUpload", method = RequestMethod.POST)
	public @ResponseBody Map zbzlUpload(@Valid CurbWeightData curbWeight, BindingResult result)
			throws InterruptedException {
		if (!result.hasErrors()) {
			curbWeight.setJyxm("Z1");
			vehManager.saveCurbWeight(curbWeight);
			
			
			Message message = new Message();
			message.setMessage("上传成功");
			message.setState(Message.STATE_SUCCESS);
			return ResultHandler.toMessage(message);
		} else {
			return ResultHandler.resultHandle(result, null, "校验出错");
		}
	}
	
	
	@UserOperation(code="zbzlUpload",name="整备质量查询",isMain=false)
	@RequestMapping(value = "getCurbWeight", method = RequestMethod.POST)
	public @ResponseBody CurbWeightData getCurbWeight(String jylsh){
		
		return this.vehManager.getLastCurbWeightDataOfJylsh(jylsh);
		
	}
	
	

}
