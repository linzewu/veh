package com.xs.veh.manager;

import javax.annotation.Resource;

import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.VehCheckProcess;

@Service("sqProcessManager")
public class SQProcessManager {

	@Resource(name="hibernateTemplate")
	  private HibernateTemplate hibernateTemplate;
	  
	  public void save(VehCheckProcess vehCheckProcess)
	  {
	    this.hibernateTemplate.save(vehCheckProcess);
	  }
}
