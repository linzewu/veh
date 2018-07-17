package com.xs.veh.manager;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.LimitStandard;

@Service("limitStandardManager")
public class LimitStandardManager {
	
	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	public List<LimitStandard> getAllLimitStandard(){
		return (List<LimitStandard>)this.hibernateTemplate.find(" from LimitStandard", null);
	}

}
