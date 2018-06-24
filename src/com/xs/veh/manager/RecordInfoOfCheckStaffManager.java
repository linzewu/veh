package com.xs.veh.manager;

import java.util.List;

import javax.annotation.Resource;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.OperationLog;
import com.xs.veh.entity.RecordInfoOfCheckStaff;

@Service("recordInfoOfCheckStaffManager")
public class RecordInfoOfCheckStaffManager {
	
	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	public List<RecordInfoOfCheckStaff> getRecordInfoOfCheckStaff(Integer page, Integer rows, RecordInfoOfCheckStaff recordInfoOfCheckStaff) {

		DetachedCriteria query = DetachedCriteria.forClass(RecordInfoOfCheckStaff.class);

		Integer firstResult = (page - 1) * rows;
		
		List<RecordInfoOfCheckStaff> vcps = (List<RecordInfoOfCheckStaff>) this.hibernateTemplate.findByCriteria(query, firstResult,
				rows);

		return vcps;
	}
	
	public Integer getRecordInfoOfCheckStaffCount(Integer page, Integer rows, RecordInfoOfCheckStaff recordInfoOfCheckStaff) {

		DetachedCriteria query = DetachedCriteria.forClass(RecordInfoOfCheckStaff.class);

		query.setProjection(Projections.rowCount());
		List<Long> count = (List<Long>) hibernateTemplate.findByCriteria(query);

		return count.get(0).intValue();
	}

}
