package com.xs.veh.manager;

import java.util.List;

import javax.annotation.Resource;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.RecordInfoOfCheckStaff;
import com.xs.veh.entity.SecurityAuditPolicySetting;
import com.xs.veh.entity.User;

@Service("securityAuditPolicySettingManager")
public class SecurityAuditPolicySettingManager {
	
	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	public List<SecurityAuditPolicySetting> getList(Integer page, Integer rows, SecurityAuditPolicySetting securityAuditPolicySetting) {

		DetachedCriteria query = DetachedCriteria.forClass(SecurityAuditPolicySetting.class);

		Integer firstResult = (page - 1) * rows;
		if(securityAuditPolicySetting.getAqsjcllxmc()!=null&&!"".equals(securityAuditPolicySetting.getAqsjcllxmc().trim())){
			query.add(Restrictions.eq("aqsjcllxmc", securityAuditPolicySetting.getAqsjcllxmc()));
		}
		
		List<SecurityAuditPolicySetting> vcps = (List<SecurityAuditPolicySetting>) this.hibernateTemplate.findByCriteria(query, firstResult,
				rows);

		return vcps;
	}
	
	public Integer getListCount(Integer page, Integer rows, SecurityAuditPolicySetting securityAuditPolicySetting) {

		DetachedCriteria query = DetachedCriteria.forClass(SecurityAuditPolicySetting.class);

		query.setProjection(Projections.rowCount());
		if(securityAuditPolicySetting.getAqsjcllxmc()!=null&&!"".equals(securityAuditPolicySetting.getAqsjcllxmc().trim())){
			query.add(Restrictions.eq("aqsjcllxmc", securityAuditPolicySetting.getAqsjcllxmc()));
		}
		List<Long> count = (List<Long>) hibernateTemplate.findByCriteria(query);

		return count.get(0).intValue();
	}
	
	public void updateSecurityAuditPolicySetting(List<SecurityAuditPolicySetting> list){
		for(SecurityAuditPolicySetting vo:list) {
			this.hibernateTemplate.update(vo);
		}
	}

}
