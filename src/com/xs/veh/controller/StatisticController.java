package com.xs.veh.controller;

import java.util.ArrayList;
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
import com.xs.veh.manager.StatisticManager;

@Controller
@RequestMapping(value = "/statistic",produces="application/json")
@Modular(modelCode="statistic",modelName="业务统计")
public class StatisticController {
	
	@Resource(name = "statisticManager")
	private StatisticManager statisticManager;
	
	@UserOperation(code="getCllxheghz",name="车辆类型合格率汇总")
	@RequestMapping(value = "getCllxheghz", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> getCllxheghz(@RequestParam String begin,@RequestParam String end){
		Map<String,Object> data =new HashMap<String,Object>();
		List<Map<String,Object>> dataList = statisticManager.findcllxheghz(begin, end,"findcllxheghz");
		data.put("rows", dataList);
		data.put("total", dataList.size());
		footCount(data, new String[] {"zsl","hgs","ychgs","fjhgs","bhgs","hgl","bhgl","ychgl","fjhgl"},new String[] {"hgl","bhgl","ychgl","fjhgl"});
		return data;
	}
	
	@UserOperation(code="getJylbhglhz",name="检验类别分类合格率汇总")
	@RequestMapping(value = "getJylbhglhz", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> getJylbhglhz(@RequestParam String begin,@RequestParam String end){
		Map<String,Object> data =new HashMap<String,Object>();
		List<Map<String,Object>> dataList = statisticManager.findcllxheghz(begin, end,"findjylbflhglhz");
		data.put("rows", dataList);
		data.put("total", dataList.size());
		footCount(data, new String[] {"zsl","hgs","ychgs","fjhgs","bhgs","hgl","bhgl","ychgl","fjhgl"},new String[] {"hgl","bhgl","ychgl","fjhgl"});
		return data;
	}
	
	
	@UserOperation(code="findjcxclsfbtj",name="检测线车辆数分布统计表")
	@RequestMapping(value = "findjcxclsfbtj", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> findjcxclsfbtj(@RequestParam String begin,@RequestParam String end){
		Map<String,Object> data =new HashMap<String,Object>();
		List<Map<String,Object>> dataList = statisticManager.findcllxheghz(begin, end,"findjcxclsfbtj");
		data.put("rows", dataList);
		data.put("total", dataList.size());
		return data;
	}
	
	
	@UserOperation(code="findjyxmflhgl",name="检验项目分类合格率汇总表")
	@RequestMapping(value = "findjyxmflhgl", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> findjyxmflhgl(@RequestParam String begin,@RequestParam String end){
		Map<String,Object> data =new HashMap<String,Object>();
		List<Map<String,Object>> dataList = statisticManager.findcllxheghz11(begin, end,"findjyxmflhgl");
		data.put("rows", dataList);
		data.put("total", dataList.size());
		footCount(data, new String[] {"zsl","hgs","ychgs","fjhgs","bhgs","hgl","bhgl","ychgl","fjhgl"},new String[] {"hgl","bhgl","ychgl","fjhgl"});
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
