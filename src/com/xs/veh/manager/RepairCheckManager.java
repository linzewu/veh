package com.xs.veh.manager;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.RepairCheck;

@Service("repairCheckManager")
public class RepairCheckManager {

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	public RepairCheck saveRepairCheck(RepairCheck repairCheck) {

		return this.hibernateTemplate.merge(repairCheck);
	}
	
	public RepairCheck getRepairCheck(String jylsh){
		List<RepairCheck> repairCheck = (List<RepairCheck>)this.hibernateTemplate.find("from RepairCheck where jylsh=?", jylsh);
		
		if(repairCheck!=null&&!repairCheck.isEmpty()){
			return repairCheck.get(0);
		}
		return null;
	}

}
