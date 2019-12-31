package com.xs.veh.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.common.IamgeBase64Cash;
import com.xs.common.ImageChange;
import com.xs.common.Message;
import com.xs.common.ResultHandler;
import com.xs.veh.entity.CheckPhoto;
import com.xs.veh.entity.Insurance;
import com.xs.veh.entity.PlateApplyTable;
import com.xs.veh.entity.RoadCheck;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.manager.RoadCheackManager;
import com.xs.veh.util.HKVisionUtil;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

@Controller
@RequestMapping(value = "/report")
@Modular(modelCode="Report",modelName="检验报告查询")
public class ReportController {

	@Resource(name = "roadCheackManager")
	private RoadCheackManager roadCheackManager;
	
	@Autowired
	private HKVisionUtil h;

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;

	@UserOperation(code="getReport1",name="仪器设备检验报告")
	@RequestMapping(value = "getReport1", method = RequestMethod.POST)
	public @ResponseBody Map getReport1(@RequestParam String jylsh,int jycs) {
		Map data = checkDataManager.getReport1(jylsh,jycs);
		return data;
	}

	@UserOperation(code="getReport4",name="检验报告")
	@RequestMapping(value = "getReport4", method = RequestMethod.POST)
	public @ResponseBody String getReport4(@RequestParam String jylsh) {
		String datas = checkDataManager.getReport4(jylsh);

		return datas;
	}

	@UserOperation(code="getReport2",name="外检报告")
	@RequestMapping(value = "getReport2", method = RequestMethod.POST)
	public @ResponseBody Map getReport2(@RequestParam String jylsh) {
		Map<String, List> data = checkDataManager.getReport2(jylsh);
		return data;
	}

	@RequestMapping(value = "test")
	public void saveTT(@RequestParam String jylsh) {
		
		VehCheckLogin vehCheckLogin = checkDataManager.getVehCheckLogin(jylsh);
		checkDataManager.createDeviceCheckJudeg(vehCheckLogin);
		System.out.println("更新完成");
	}
	
	@RequestMapping(value = "test3",method=RequestMethod.GET)
	public void test3() {
		try {
			String file = h.taskPicture("admin", "123456", "192.168.0.100", 8000,"12345677");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	@RequestMapping(value = "test2")
	public void saveET(@RequestParam String jylsh) {
		VehCheckLogin vehCheckLogin = checkDataManager.getVehCheckLogin(jylsh);
		checkDataManager.createExternalCheckJudge(vehCheckLogin);
		System.out.println("更新完成");
	}

	@UserOperation(code="getCheckPhotos",name="查询检测照片")
	@RequestMapping(value = "getCheckPhotos")
	public @ResponseBody List getCheckPhotos(@RequestParam String jylsh) {
		return this.checkDataManager.getCheckPhotos(jylsh);
	}
	
	

	@UserOperation(code="uploadPhoto",name="上传检测照片")
	@RequestMapping(value = "uploadPhoto")
	public @ResponseBody Map uploadPhoto(CheckPhoto checkPhoto, @RequestParam String imageData) throws IOException {
		BASE64Decoder decoder = new BASE64Decoder();
		byte[] zp = decoder.decodeBuffer(imageData);
		
		if(checkPhoto.getZpzl().indexOf("02")==0){
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zp);
			BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);
			bufferedImage =  ImageChange.Rotate(bufferedImage, 90);
            // 创建字节输入流
			ByteArrayOutputStream byteArrayOutputStream =new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "JPEG", byteArrayOutputStream);
			zp =byteArrayOutputStream.toByteArray();
		}
		
		checkPhoto.setZp(zp);
		checkPhoto.setPssj(new Date());
		this.checkDataManager.saveCheckPhoto(checkPhoto);
		
		BASE64Encoder encoder = new BASE64Encoder();
		imageData = encoder.encode(checkPhoto.getZp());
		IamgeBase64Cash.getInstance().cashBase64Iamge(imageData, checkPhoto.getId().toString());
		Map resulMap = ResultHandler.toSuccessJSON("保存成功！");
		resulMap.put("id", checkPhoto.getId());
		resulMap.put("img", imageData);

		return resulMap;
	}
	
	@UserOperation(code="uploadPhoto",name="上传检测照片")
	@RequestMapping(value = "uploadFileImag")
	public @ResponseBody String uploadFileImag(CheckPhoto checkPhoto, MultipartFile imgFile) throws IOException {
		byte[] zp = imgFile.getBytes();
		if(checkPhoto.getZpzl().indexOf("02")==0){
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zp);
			BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);
			bufferedImage =  ImageChange.Rotate(bufferedImage, 90);
            // 创建字节输入流
			ByteArrayOutputStream byteArrayOutputStream =new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "JPEG", byteArrayOutputStream);
			zp =byteArrayOutputStream.toByteArray();
		}
		
		checkPhoto.setZp(zp);
		checkPhoto.setPssj(new Date());
		this.checkDataManager.saveCheckPhoto(checkPhoto);
		
		BASE64Encoder encoder = new BASE64Encoder();
		String imageData = encoder.encode(checkPhoto.getZp());
		IamgeBase64Cash.getInstance().cashBase64Iamge(imageData, checkPhoto.getId().toString());
		Map resulMap = ResultHandler.toSuccessJSON("保存成功！");
		resulMap.put("id", checkPhoto.getId());
		resulMap.put("img", imageData);
		
		ObjectMapper om=new ObjectMapper();

		return om.writeValueAsString(resulMap);
	}

	@UserOperation(code="getCheckPhotos",name="查询检测照片")
	@RequestMapping(value = "getCheckPhoto")
	public @ResponseBody String getCheckPhoto(String id) throws Exception {

		String img = IamgeBase64Cash.getInstance().getCashBase64Iamge(id);

		if (img == null) {
			CheckPhoto cp = this.checkDataManager.getCheckPhoto(Integer.parseInt(id));
			BASE64Encoder encoder = new BASE64Encoder();
			img = encoder.encode(cp.getZp());
			IamgeBase64Cash.getInstance().cashBase64Iamge(img, cp.getId().toString());
		}

		return img;

	}

	@UserOperation(code="deleteImage",name="删除检测照片")
	@RequestMapping(value = "deleteImage")
	public @ResponseBody Map deleteImage(Integer id) throws Exception {
		this.checkDataManager.deleteImage(id);
		return ResultHandler.toSuccessJSON("删除成功");

	}

	@UserOperation(code="getProcess",name="查询检测过程")
	@RequestMapping(value = "getProcess")
	public @ResponseBody List getProcess(@RequestParam String jylsh) {
		return this.checkDataManager.getVehCheckProcess(jylsh);
	}

	@UserOperation(code="getCheckLogs",name="查询检测日志")
	@RequestMapping(value = "getCheckLogs")
	public @ResponseBody List getCheckLogs(@RequestParam String jylsh) {
		return this.checkDataManager.getCheckLogs(jylsh);
	}

	@UserOperation(code="getCheckEvents",name="查询检测事件")
	@RequestMapping(value = "getCheckEvents")
	public @ResponseBody List getCheckEvents(@RequestParam String jylsh) {
		return this.checkDataManager.getCheckEvents(jylsh);
	}

	@UserOperation(code="getInsurance",name="查询保险信息")
	@RequestMapping(value = "getInsurance")
	public @ResponseBody Insurance getInsurance(@RequestParam String jylsh) {

		return checkDataManager.getInsurance(jylsh);
	}

	@UserOperation(code="getRoadCheck",name="查询路试信息")
	@RequestMapping(value = "getRoadCheck")
	public @ResponseBody RoadCheck getRoadCheck(@RequestParam String jylsh) {

		return roadCheackManager.getRoadCheck(jylsh);
	}

	@UserOperation(code="saveInsurance",name="上传保险信息")
	@RequestMapping(value = "saveInsurance")
	public @ResponseBody Map saveInsurance(Insurance insurance) {
		checkDataManager.saveInsurance(insurance);
		Map resulMap = ResultHandler.toSuccessJSON("保存成功！");
		resulMap.put("id", insurance.getId());
		return resulMap;
	}

	@UserOperation(code="saveRoadCheck",name="上传路试信息")
	@RequestMapping(value = "saveRoadCheck")
	public @ResponseBody String saveRoadCheck(RoadCheck roadCheck) throws JsonProcessingException, InterruptedException {
		Message message = roadCheackManager.saveRoadCheck(roadCheck);
		Map map = ResultHandler.toMessage(message);
		map.put("data", roadCheck);
		
		 ObjectMapper objectMapper = new ObjectMapper();
	     String jsonString=objectMapper.writeValueAsString(map);
		
		return jsonString;
	}
	
	@UserOperation(code="updateEventState",name="重置检测事件")
	@RequestMapping(value = "updateEventState")
	public @ResponseBody Map updateEventState(@RequestParam String jylsh) throws JsonProcessingException, InterruptedException {
		
		this.checkDataManager.resetEventState(jylsh);
		
		return ResultHandler.toSuccessJSON("更新成功！");
		
	}
	
	
	@UserOperation(code="savePlateApplyTable",name="保存牌证申请表")
	@RequestMapping(value = "savePlateApplyTable")
	public @ResponseBody PlateApplyTable  savePlateApplyTable(PlateApplyTable plateApplyTable) throws JsonProcessingException, InterruptedException {
		this.checkDataManager.savePlateApplyTable(plateApplyTable);
		return plateApplyTable;
	}
	
	@UserOperation(code="getPlateApplyTable",name="保存牌证申请表")
	@RequestMapping(value = "getPlateApplyTable")
	public @ResponseBody PlateApplyTable  getPlateApplyTable(String jylsh) throws JsonProcessingException, InterruptedException {
		return this.checkDataManager.getPlateApplyTable(jylsh);
	}
	
	
	@UserOperation(code="reloadVideo",name="重置视频下载")
	@RequestMapping(value = "reloadVideo", method = RequestMethod.POST)
	public @ResponseBody Map reloadVideo(@RequestParam("ids") String ids){
		this.checkDataManager.saveReloadVideo(ids);
		Map info=new HashMap();
		info.put("state","1");
		info.put("message", "视频状态重置成功！");
		return info;
	}

}
