package com.xs.veh.manager;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.RecordInfoOfCheck;

@Service("recordInfoOfCheckManager")
public class RecordInfoOfCheckManager {
	
	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	public RecordInfoOfCheck getRecordInfoOfCheckInfo() {
		List<RecordInfoOfCheck> list = (List<RecordInfoOfCheck>)hibernateTemplate.find(" from RecordInfoOfCheck", null);
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return new RecordInfoOfCheck();
	}

}
