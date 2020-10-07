package com.xs.veh.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.common.ResultHandler;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.manager.CheckEventManger;
import com.xs.veh.manager.ExtendManage;
import com.xs.veh.manager.VehManager;
import com.xs.veh.network.TakePicture;

@Controller
@RequestMapping(value = "/extend")
@Modular(modelCode="Extend",modelName="扩展接口",isEmpowered=false)
public class ExtendController {
	
	@Autowired
	private ExtendManage extendManage;
	
	@Autowired
	private CheckDataManager checkDataManager;
	
	@Autowired
	private CheckEventManger checkEventManger;
	
	@Autowired
	private VehManager vehManager;
	
	
	@UserOperation(code="getQkdOutLineWaitList",name="查询检测列表")
	@RequestMapping(value = "getQkdOutLineWaitList", method = RequestMethod.POST)
	public @ResponseBody List<Map<String, Object>> getQkdOutLineWaitList(@RequestParam String jylsh){
		
		return extendManage.getqkdWaitList(jylsh);
	}
	
	
	@UserOperation(code="upQkdOutLine",name="强科达外廓引车")
	@RequestMapping(value = "upQkdOutLine", method = RequestMethod.POST)
	public @ResponseBody Map upQkdOutLine(@RequestParam String jylsh){
		VehCheckLogin vehCheckLogin = this.checkDataManager.getVehCheckLogin(jylsh);
		Integer jycs =vehCheckLogin.getJycs();
		this.extendManage.upQkdOutLine(vehCheckLogin);
		VehCheckProcess vehCheckProcess = checkDataManager.getVehCheckProces(jylsh, jycs, "M1");
		vehCheckProcess.setKssj(new Date());
		this.checkDataManager.updateProcess(vehCheckProcess);
		checkEventManger.createEvent(jylsh, jycs, "18C55", vehCheckProcess.getJyxm(), vehCheckProcess.getHphm(),
				vehCheckProcess.getHpzl(), vehCheckProcess.getClsbdh(),vehCheckLogin.getVehcsbj()); 
		return ResultHandler.toSuccessJSON("保存成功！");
	}
	
	@UserOperation(code="saveQkdOutLine",name="外廓保存数据")
	@RequestMapping(value = "saveQkdOutLine", method = RequestMethod.POST)
	public @ResponseBody Map saveQkdOutLine(@RequestParam String jylsh) throws InterruptedException{
		VehCheckLogin vehCheckLogin = this.checkDataManager.getVehCheckLogin(jylsh);
		this.extendManage.saveQkdOutLine(vehCheckLogin);
		return ResultHandler.toSuccessJSON("保存成功！");
	}
	
	
	@UserOperation(code="qkdOutlinePicture",name="强科达外廓拍照")
	@RequestMapping(value = "qkdOutlinePicture", method = RequestMethod.POST)
	public @ResponseBody Map qkdOutlinePicture(@RequestParam String jylsh,@RequestParam String zpzl){
		VehCheckLogin vehCheckLogin = this.checkDataManager.getVehCheckLogin(jylsh);
		TakePicture.createNew(vehCheckLogin, "M1", 0,zpzl);
		return ResultHandler.toSuccessJSON("拍照命令已发送！");
	}
	
	

}
