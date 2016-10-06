package com.xs.veh.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.manager.ExternalCheckManager;
import com.xs.veh.manager.VehManager;

@Controller
@RequestMapping(value = "/pda")
public class PDAServiceController {

	@Resource(name = "vehManager")
	private VehManager vehManager;

	@Resource(name = "externalCheckManager")
	private ExternalCheckManager externalCheckManager;

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
	public @ResponseBody Map externalUpload(@Valid ExternalCheck externalCheck, BindingResult result) {
		if (!result.hasErrors()) {
			Message message = externalCheckManager.saveExternalCheck(externalCheck);
			return ResultHandler.toMessage(message);
		} else {
			return ResultHandler.resultHandle(result, null, "校验出错");
		}
	}

	@RequestMapping(value = "externalDC", method = RequestMethod.POST)
	public @ResponseBody Map externalDCUpload(@Valid ExternalCheck externalCheck, BindingResult result) {
		if (!result.hasErrors()) {
			Message message = externalCheckManager.saveExternalCheckDC(externalCheck);
			return ResultHandler.toMessage(message);
		} else {
			return ResultHandler.resultHandle(result, null, "校验出错");
		}
	}

	@RequestMapping(value = "externalC1", method = RequestMethod.POST)
	public @ResponseBody Map externalC1Upload(@Valid ExternalCheck externalCheck, BindingResult result) {
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

}
