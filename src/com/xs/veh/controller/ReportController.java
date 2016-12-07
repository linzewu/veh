package com.xs.veh.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.common.IamgeBase64Cash;
import com.xs.common.ResultHandler;
import com.xs.veh.entity.CheckPhoto;
import com.xs.veh.manager.CheckDataManager;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

@Controller
@RequestMapping(value = "/report")
public class ReportController {

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;

	@RequestMapping(value = "getReport1", method = RequestMethod.POST)
	public @ResponseBody Map getReport1(@RequestParam String jylsh) {
		Map data = checkDataManager.getReport1(jylsh);
		return data;
	}

	@RequestMapping(value = "getReport4", method = RequestMethod.POST)
	public @ResponseBody String getReport4(@RequestParam String jylsh) {
		String datas = checkDataManager.getReport4(jylsh);
		
		return datas;
	}

	@RequestMapping(value = "getReport2", method = RequestMethod.POST)
	public @ResponseBody Map getReport2(@RequestParam String jylsh) {
		Map<String, List> data = checkDataManager.getReport2(jylsh);
		return data;
	}
	
	@RequestMapping(value = "test")
	public void saveTT(@RequestParam String jylsh){
		checkDataManager.updateBrakRollerDataByJylsh(jylsh);
		System.out.println("更新完成");
	}
	
	@RequestMapping(value = "getCheckPhotos")
	public @ResponseBody List getCheckPhotos(@RequestParam String jylsh){
		return this.checkDataManager.getCheckPhotos(jylsh);
	}
	
	@RequestMapping(value = "uploadPhoto")
	public @ResponseBody Map uploadPhoto(CheckPhoto checkPhoto,@RequestParam String imageData) throws IOException{
		BASE64Decoder decoder=new BASE64Decoder();
		byte[] zp = decoder.decodeBuffer(imageData);
		checkPhoto.setZp(zp);
		checkPhoto.setPssj(new Date());
		this.checkDataManager.saveCheckPhoto(checkPhoto);
		IamgeBase64Cash.getInstance().cashBase64Iamge(imageData, checkPhoto.getId().toString());
		
		Map resulMap=ResultHandler.toSuccessJSON("保存成功！");
		
		resulMap.put("id", checkPhoto.getId());
		
		return resulMap;
	}
	
	@RequestMapping(value = "getCheckPhoto")
	public @ResponseBody String getCheckPhoto(String id) throws Exception{
		
		String img = IamgeBase64Cash.getInstance().getCashBase64Iamge(id);
		
		if(img==null){
			CheckPhoto cp = this.checkDataManager.getCheckPhoto(Integer.parseInt(id));
			BASE64Encoder encoder=new BASE64Encoder();
			img = encoder.encode(cp.getZp());
			IamgeBase64Cash.getInstance().cashBase64Iamge(img, cp.getId().toString());
		}
		
		return img;
		
	}
	
	@RequestMapping(value = "deleteImage")
	public @ResponseBody Map deleteImage(Integer id) throws Exception{
		this.checkDataManager.deleteImage(id);
		return ResultHandler.toSuccessJSON("删除成功");
		
	}

}
