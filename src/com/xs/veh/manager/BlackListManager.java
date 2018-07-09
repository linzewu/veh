package com.xs.veh.manager;

import java.util.List;

import javax.annotation.Resource;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.BlackList;
import com.xs.veh.entity.SecurityAuditPolicySetting;
import com.xs.veh.entity.User;

@Service("blackListManager")
public class BlackListManager {
	
	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	public void saveBlackList(BlackList blackList) {
		 this.hibernateTemplate.saveOrUpdate(blackList);
	}
	
	public void deleteBlackList(BlackList blackList) {
		 this.hibernateTemplate.delete(blackList);
	}
	
	public void deleteSystemBlackList() {
		List<BlackList> blackList = (List<com.xs.veh.entity.BlackList>) hibernateTemplate.find("from BlackList where createBy=?",User.SYSTEM_USER);
		hibernateTemplate.deleteAll(blackList);
	}
	
	public BlackList getBlackListByIp(String ip) {
		BlackList blackList = this.hibernateTemplate.get(BlackList.class, ip);
		return blackList;
	}
	
	public boolean checkIpIsBan(String ip) {
		
		BlackList blackList = this.hibernateTemplate.get(BlackList.class, ip);
		if(blackList!=null) {
			//阈值取阈值表
			if("Y".equals(blackList.getEnableFlag())||!User.SYSTEM_USER.equals(blackList.getCreateBy())) {
				return true;
			}
		}
		return false;
	}
	
	public List<BlackList> getList(Integer page, Integer rows, BlackList blackList) {

		DetachedCriteria query = DetachedCriteria.forClass(BlackList.class);

		Integer firstResult = (page - 1) * rows;
		query.add(Restrictions.eq("enableFlag", "Y"));
		
		List<BlackList> vcps = (List<BlackList>) this.hibernateTemplate.findByCriteria(query, firstResult,
				rows);

		return vcps;
	}
	
	public Integer getListCount(Integer page, Integer rows, BlackList blackList) {

		DetachedCriteria query = DetachedCriteria.forClass(BlackList.class);

		query.setProjection(Projections.rowCount());
		
		List<Long> count = (List<Long>) hibernateTemplate.findByCriteria(query);

		return count.get(0).intValue();
	}
	
	public List<BlackList> getEnableList(){
		return (List<BlackList>)this.hibernateTemplate.find(" from BlackList where enableFlag = 'Y'", null);
	}

}
