package com.xs.veh.manager;

import java.util.List;

import javax.annotation.Resource;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.OperationLog;
@Service("operationLogManager")
public class OperationLogManager {
	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	/**
	 * 查询
	 * @return
	 */
	public List<OperationLog> getOperationLog() {
		return (List<OperationLog>) this.hibernateTemplate.find("from OperationLog ");
	}
	
	public List<OperationLog> getOperationLog(Integer page, Integer rows, OperationLog operationLog) {

		DetachedCriteria query = DetachedCriteria.forClass(OperationLog.class);

		Integer firstResult = (page - 1) * rows;
		if(operationLog.getOperationUser()!=null&&!"".equals(operationLog.getOperationUser().trim())){
			query.add(Restrictions.like("operationUser", "%"+operationLog.getOperationUser()+"%"));
		}
		if(operationLog.getModule()!=null&&!"".equals(operationLog.getModule().trim())){
			query.add(Restrictions.like("module", "%"+operationLog.getModule()+"%"));
		}
		if(operationLog.getOperationDate() != null) {
			query.add(Restrictions.ge("operationDate", operationLog.getOperationDate()));
		}
		if(operationLog.getOperationDateEnd() != null) {
			query.add(Restrictions.le("operationDate", operationLog.getOperationDateEnd()));
		}
		List<OperationLog> vcps = (List<OperationLog>) this.hibernateTemplate.findByCriteria(query, firstResult,
				rows);

		return vcps;
	}
	
	public Integer getOperationLogCount(Integer page, Integer rows, OperationLog operationLog) {

		DetachedCriteria query = DetachedCriteria.forClass(OperationLog.class);

		query.setProjection(Projections.rowCount());
		if(operationLog.getOperationUser()!=null&&!"".equals(operationLog.getOperationUser().trim())){
			query.add(Restrictions.like("operationUser", "%"+operationLog.getOperationUser()+"%"));
		}
		if(operationLog.getModule()!=null&&!"".equals(operationLog.getModule().trim())){
			query.add(Restrictions.like("module", "%"+operationLog.getModule()+"%"));
		}
		if(operationLog.getOperationDate() != null) {
			query.add(Restrictions.ge("operationDate", operationLog.getOperationDate()));
		}
		if(operationLog.getOperationDateEnd() != null) {
			query.add(Restrictions.le("operationDate", operationLog.getOperationDateEnd()));
		}
		List<Long> count = (List<Long>) hibernateTemplate.findByCriteria(query);

		return count.get(0).intValue();
	}

	/**
	 * 插入
	 * @param operationLog
	 */
	public void saveOperationLog(OperationLog operationLog) {

		this.hibernateTemplate.save(operationLog);

	}
}
