package com.xs.veh.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.VehCheckProcess;

@Service
public class VehProcessManager {
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	public List<VehCheckProcess> getDowloadsData(){
		
		List<VehCheckProcess> datas = (List<VehCheckProcess>) this.hibernateTemplate.find("from VehCheckProcess where voideSate=0 and jssj<DATEADD(mi,-2,GETDATE())");
		
		return datas;
	}
	
	
	@Async
	public void saveVehProcessSync(VehCheckProcess vehCheckProcess) {
		
		this.hibernateTemplate.update(vehCheckProcess);
		
	}
	
	
	public void updateDowloadState(Integer id) {
		
	}

}
