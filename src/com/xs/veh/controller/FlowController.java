package com.xs.veh.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.common.Message;
import com.xs.common.ResultHandler;
import com.xs.enums.CommonUserOperationEnum;
import com.xs.veh.entity.Device;
import com.xs.veh.entity.Flow;
import com.xs.veh.entity.WorkPoint;
import com.xs.veh.manager.DeviceManager;
import com.xs.veh.manager.FlowManager;
import com.xs.veh.manager.WorkPointManager;
import com.xs.veh.util.PageInfo;

import net.sf.json.JSONObject;

@Controller
@RequestMapping(value = "/flow")
@Modular(modelCode="Flow",modelName="检测流程")
public class FlowController {

	@Resource(name = "flowManager")
	private FlowManager flowManager;

	@Resource(name = "workPointManager")
	private WorkPointManager workPointManager;
	
	@Resource(name = "deviceManager")
	private DeviceManager deviceManager;

	@UserOperation(code="getFlows",name="查询检测流程")
	@RequestMapping(value = "getFlows", method = RequestMethod.POST)
	public @ResponseBody Map getDevices(PageInfo pageInfo) {
		Map json = ResultHandler.toMyJSON(flowManager.getFlows(), 0);
		return json;
	}

	@UserOperation(code="addFlow",name="新增修改检测流程")
	@RequestMapping(value = "addFlow", method = RequestMethod.POST)
	public @ResponseBody Map addFlow(Flow flow) {
		this.flowManager.save(flow);
		return ResultHandler.toSuccessJSON("新增流程成功。");
	}

	@UserOperation(code="delete",name="删除流程")
	@RequestMapping(value = "delete", method = RequestMethod.POST)
	public @ResponseBody Map delete(Flow flow) {
		this.flowManager.delete(flow);
		return ResultHandler.toSuccessJSON("删除成功。");
	}

	@UserOperation(code="addFlow",name="新增修改检测流程")
	@RequestMapping(value = "updateFlow", method = RequestMethod.POST)
	public @ResponseBody Map updateFlow(Flow flow) {
		
		System.out.println(flow.getFlow());
		Message message = this.flowManager.update(flow);
		return ResultHandler.toMessage(message);
	}

	@UserOperation(code="getFlows",name="查询检测流程")
	@RequestMapping(value = "getWorkPointAndDeviceByJcxxh", method = RequestMethod.POST)
	public @ResponseBody Map getWorkPointAndDeviceByJcxxh(@RequestParam Integer jcxdh) {
		List<WorkPoint> wps = this.workPointManager.getWorkPointsByJcxdh(jcxdh);
		List<Device> devices = deviceManager.getDevices(jcxdh);
		Map data =new HashMap();
		data.put("gw", wps);
		data.put("sb", devices);
		return data;
	}

}
