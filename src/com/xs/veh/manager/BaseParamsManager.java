package com.xs.veh.manager;

import java.util.List;

import javax.annotation.Resource;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.BaseParams;

@Service("baseParamsManager")
public class BaseParamsManager {

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	public List<BaseParams> getBaseParams() {
		DetachedCriteria dc = DetachedCriteria.forClass(BaseParams.class);
		dc.addOrder(Order.asc("type"));
		dc.addOrder(Order.asc("seq"));
		List<BaseParams> bps = (List<BaseParams>) this.hibernateTemplate
				.findByCriteria(dc);
		return bps;
	}

}
