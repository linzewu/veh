package com.xs.veh.manager;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

@Service("checkQueueManager")
@Scope("singleton")
public class CheckQueueManager {
	
	@Resource(name="hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	public synchronized Integer getPdxh(String jcxdh, Integer gwsx) {
		List list =  hibernateTemplate.find("select max(pdxh) from CheckQueue where jcxdh=? and gwsx=? ", Integer.parseInt(jcxdh), gwsx);
		//没有排队则为1
		Object o = list.get(0);
		
		if(o==null){
			return 1;
		}else{
			return ((Integer)o)+1;
		}
	}

}
