package com.xs.veh.manager;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.xs.veh.entity.HBDeviceConfig;
import com.xs.veh.entity.HBRoutineCheck;

@Service("hbManager")
public class HBManager {
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	
	public List<HBDeviceConfig> getHBDeviceConfigList(HBDeviceConfig queryParam){
		
		String sql ="from HBDeviceConfig where 1=1 ";
		
		List<Object> params =new ArrayList<Object>();
		
		if(!StringUtils.isEmpty(queryParam.getIp())) {
			sql+=" and ip=?";
			params.add(queryParam.getIp());
		}
		List<HBDeviceConfig> datas = (List<HBDeviceConfig>) this.hibernateTemplate.find(sql, params.toArray());
		return datas;
	}
	
	
	public List<HBRoutineCheck> getRoutineCheck(){
		String sql ="from HBRoutineCheck ";
		List<HBRoutineCheck> datas = (List<HBRoutineCheck>) this.hibernateTemplate.find(sql);
		return datas;
	}
	
	
	public HBDeviceConfig save(HBDeviceConfig hbDeviceConfig) {
		 this.hibernateTemplate.saveOrUpdate(hbDeviceConfig);
		 return hbDeviceConfig;
	}
	
	
	

}
