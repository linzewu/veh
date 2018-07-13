package com.xs.veh.manager;

import java.util.List;

import javax.annotation.Resource;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.OperationLog;
import com.xs.veh.entity.SecurityLog;

@Service("securityLogManager")
public class SecurityLogManager {
	
	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	public List<SecurityLog> getSecurityLog(Integer page, Integer rows, SecurityLog securityLog) {

		DetachedCriteria query = DetachedCriteria.forClass(SecurityLog.class);

		Integer firstResult = (page - 1) * rows;
		
		List<SecurityLog> vcps = (List<SecurityLog>) this.hibernateTemplate.findByCriteria(query, firstResult,
				rows);

		return vcps;
	}
	
	public Integer getSecurityLogCount(Integer page, Integer rows, SecurityLog securityLog) {

		DetachedCriteria query = DetachedCriteria.forClass(SecurityLog.class);

		query.setProjection(Projections.rowCount());
		
		List<Long> count = (List<Long>) hibernateTemplate.findByCriteria(query);

		return count.get(0).intValue();
	}

	/**
	 * 插入
	 * @param operationLog
	 */
	public void saveSecurityLog(SecurityLog securityLog) {

		this.hibernateTemplate.save(securityLog);

	}

}
