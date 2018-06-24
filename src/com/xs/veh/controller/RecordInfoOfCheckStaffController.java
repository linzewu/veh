package com.xs.veh.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.veh.entity.RecordInfoOfCheckStaff;
import com.xs.veh.manager.RecordInfoOfCheckStaffManager;

@Controller
@RequestMapping(value = "/recordInfoOfCheckStaff")
@Modular(modelCode="RecordInfoOfCheck",modelName="检验机构信息")
public class RecordInfoOfCheckStaffController {
	
	@Resource(name = "recordInfoOfCheckStaffManager")
	private RecordInfoOfCheckStaffManager recordInfoOfCheckStaffManager;
	
	@UserOperation(code="getRecordInfoOfCheckStaffList",name="检验机构人员信息查询")
	@RequestMapping(value = "getRecordInfoOfCheckStaffList", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> getRecordInfoOfCheckStaff(Integer page, Integer rows, RecordInfoOfCheckStaff recordInfoOfCheckStaff) {		
		List<RecordInfoOfCheckStaff> vcps = recordInfoOfCheckStaffManager.getRecordInfoOfCheckStaff(page, rows, recordInfoOfCheckStaff);
		
		Integer total = recordInfoOfCheckStaffManager.getRecordInfoOfCheckStaffCount(page, rows, recordInfoOfCheckStaff);
		
		Map<String,Object> data =new HashMap<String,Object>();
		
		data.put("rows", vcps);
		data.put("total", total);		
		
		return data;
	}

}
