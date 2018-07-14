package com.xs.veh.manager;

import java.util.List;

import javax.annotation.Resource;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.RecordInfoOfCheck;
import com.xs.veh.entity.RecordInfoOfJcx;

@Service("RecordInfoOfJcxManager")
public class RecordInfoOfJcxManager {
	
	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	public RecordInfoOfJcx getRecordInfoOfJcx() {
		List<RecordInfoOfJcx> list = (List<RecordInfoOfJcx>)hibernateTemplate.find(" from RecordInfoOfJcx", null);
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return new RecordInfoOfJcx();
	}
	
	public void deleteRecordInfo() {
		hibernateTemplate.execute(new HibernateCallback<Integer>() {
			@Override
			public Integer doInHibernate(Session session) throws HibernateException {
				return session.createSQLQuery("delete TM_RecordInfoOfJcx").executeUpdate();
			}
		});
	}
	
	public void saveRecordInfoOfJcx(RecordInfoOfJcx recordInfoOfJcx) {
		hibernateTemplate.save(recordInfoOfJcx);
	}

}
