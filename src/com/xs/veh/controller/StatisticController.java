package com.xs.veh.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.veh.manager.StatisticManager;

@Controller
@RequestMapping(value = "/statistic",produces="application/json")
@Modular(modelCode="statistic",modelName="业务统计")
public class StatisticController {
	
	@Resource(name = "statisticManager")
	private StatisticManager statisticManager;
	
	@UserOperation(code="getCllxheghz",name="车辆类型合格率汇总")
	@RequestMapping(value = "getCllxheghz", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> getCllxheghz(@RequestParam String begin,@RequestParam String end,@RequestParam String type){
		Map<String,Object> data =new HashMap<String,Object>();
		List<Map<String,Object>> dataList = statisticManager.findcllxheghz(begin, end,"findcllxheghz","cllx",type);
		data.put("rows", dataList);
		
		if(dataList.size()>0) {
			data.put("total", dataList.size());
			footCount(data, new String[] {"zsl","hgs","ychgs","fjhgs","bhgs","hgl","bhgl","ychgl","fjhgl"},new String[] {"hgl","bhgl","ychgl","fjhgl"});	
		}
		
		return data;
	}
	
	@UserOperation(code="getJylbhglhz",name="检验类别分类合格率汇总")
	@RequestMapping(value = "getJylbhglhz", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> getJylbhglhz(@RequestParam String begin,@RequestParam String end,@RequestParam String type){
		Map<String,Object> data =new HashMap<String,Object>();
		List<Map<String,Object>> dataList = statisticManager.findcllxheghz(begin, end,"findjylbflhglhz","jylb",type);
		data.put("rows", dataList);
		if(dataList.size()>0) {
			data.put("total", dataList.size());
			footCount(data, new String[] {"zsl","hgs","ychgs","fjhgs","bhgs","hgl","bhgl","ychgl","fjhgl"},new String[] {"hgl","bhgl","ychgl","fjhgl"});
		}
		System.out.println(data);
		return data;
	}
	
	
	@UserOperation(code="findjcxclsfbtj",name="检测线车辆数分布统计表")
	@RequestMapping(value = "findjcxclsfbtj", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> findjcxclsfbtj(@RequestParam String begin,@RequestParam String end,@RequestParam String type){
		Map<String,Object> data =new HashMap<String,Object>();
		List<Map<String,Object>> dataList = statisticManager.findcllxheghz(begin, end,"findjcxclsfbtj","jcxdh",type);
		data.put("rows", dataList);
		data.put("total", dataList.size());
		return data;
	}
	
	
	@UserOperation(code="findjyxmflhgl",name="检验项目分类合格率汇总表")
	@RequestMapping(value = "findjyxmflhgl", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> findjyxmflhgl(@RequestParam String begin,@RequestParam String end,@RequestParam String type){
		Map<String,Object> data =new HashMap<String,Object>();
		List<Map<String,Object>> dataList = statisticManager.findcllxheghz11(begin, end,"findjyxmflhgl","jyxm",type);
		data.put("rows", dataList);
		if(dataList.size()>0) {
			data.put("total", dataList.size());
			footCount(data, new String[] {"zsl","hgs","ychgs","fjhgs","bhgs","hgl","bhgl","ychgl","fjhgl"},new String[] {"hgl","bhgl","ychgl","fjhgl"});
		}
		return data;
	}
	
	
	@UserOperation(code="getRygzltj",name="人员工作量统计")
	@RequestMapping(value = "getRygzltj", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> getRygzltj(@RequestParam String begin,@RequestParam String end,@RequestParam String type){
		Map<String,Object> data =new HashMap<String,Object>();
		List<Map<String,Object>> dataList = statisticManager.findcllxheghz(begin, end,"findrygzltj","cllx",type);
		data.put("rows", dataList);
		data.put("total", dataList.size());
		
		Integer t =0;
		Integer t2 =0;
		
		for(Map<String,Object> map:dataList) {
			t=t+Integer.parseInt(map.get("zsl").toString());
			t2=t2+Integer.parseInt(map.get("hgs").toString());
			
		}
		
		Map footMap=new HashMap();
		
		footMap.put("zsl",t);
		footMap.put("hgs",t2);
		List footList =new ArrayList();
		footList.add(footMap);
		data.put("footer", footList);
				
		return data;
	}
	
	@UserOperation(code="getRygzltj2",name="人员工作量统计2")
	@RequestMapping(value = "getRygzltj2", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> getRygzltj2(@RequestParam String begin,@RequestParam String end){
		Map<String,Object> data =new HashMap<String,Object>();
		List<Map<String,Object>> dataList = statisticManager.findgzl2(begin, end,"findgzl2");
		data.put("rows", dataList);
		data.put("total", dataList.size());
		return data;
	}
	
	
	
	@UserOperation(code="业务量统计",name="业务量统计")
	@RequestMapping(value = "getBusinessArea", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> getBusinessArea(@RequestParam Map param){
		
		Map<String,Object> data = statisticManager.getBusiness(param);
		return data;
	}
	
	@UserOperation(code="业务量统计2",name="业务量统计2")
	@RequestMapping(value = "getBusinessArea2", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> getBusinessArea2(@RequestParam Map param){
		
		Map<String,Object> data = statisticManager.getBusiness2(param);
		return data;
	}
	
	
	
	public void footCount(Map<String,Object> datas,String[] columns,String[] avgColumns) {
		
		List<Map<String,Object>> dataList = (List<Map<String,Object>>) datas.get("rows");
		List<String> arrayColumns=new ArrayList<String>();
		for(String column: columns) {
			arrayColumns.add(column);
		}
		Map<String,Double> footCount =new HashMap<String,Double>();
		List<Map<String,Double>> footList=new ArrayList<Map<String,Double>>();
		
		for(Map<String,Object> data:dataList) {
			for(String key: data.keySet()) {
				
				if(footCount.containsKey(key)) {
					footCount.put(key, footCount.get(key)+Double.parseDouble(data.get(key).toString()));
				}else {
					if(arrayColumns.contains(key)) {
						footCount.put(key, Double.parseDouble(data.get(key).toString()));
					}
				}
				
			}
		}
		if(avgColumns != null) {
			for(String avgColumn:avgColumns) {
				if(footCount.containsKey(avgColumn)) {
					footCount.put(avgColumn, footCount.get(avgColumn)/dataList.size());
				}
			}
		}
		footList.add(footCount);
		datas.put("footer", footList);
	}

}
