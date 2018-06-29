package com.xs.veh.manager;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.BaseParams;

@Service("baseParamsManager")
public class BaseParamsManager {

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	@Autowired
	private ServletContext servletContext;

	public List<BaseParams> getBaseParams() {
		DetachedCriteria dc = DetachedCriteria.forClass(BaseParams.class);
		dc.addOrder(Order.asc("type"));
		dc.addOrder(Order.asc("seq"));
		List<BaseParams> bps = (List<BaseParams>) this.hibernateTemplate.findByCriteria(dc);
		return bps;
	}

	public BaseParams getBaseParam(String type, String value) {
		List<BaseParams> bps = (List<BaseParams>) servletContext.getAttribute("bps");
		if(bps == null){
			bps=getBaseParams();
		}
		for (BaseParams param : bps) {
			if (param.getType().equals(type) && param.getParamValue().equals(value)) {
				return param;
			}
		}
		return null;
	}
	
	

	public BaseParams saveBaseParam(BaseParams baseParams) {
		
		BaseParams bp=new BaseParams();
		
		this.hibernateTemplate.saveOrUpdate(baseParams);
		
		return baseParams;
	}

}
