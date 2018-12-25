package com.xs.veh.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
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
		if(bps==null) {
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
	
	public BaseParams save(BaseParams baseParams) {
		baseParams.setCreateTime(new Date());
		baseParams = this.hibernateTemplate.merge(baseParams);
		return baseParams;
	}

	
	public void delete(Integer id) {
		BaseParams baseParams = this.hibernateTemplate.get(BaseParams.class, id);
		if(baseParams!=null){
			this.hibernateTemplate.delete(baseParams);
		}
	}


	public Map<String,Object> getBaseParams(Integer page, Integer row,BaseParams param) {
		
		DetachedCriteria dc = DetachedCriteria.forClass(BaseParams.class);
		DetachedCriteria dcCou = DetachedCriteria.forClass(BaseParams.class);
		Integer firstResult = (page - 1) * row;
		if(param.getParamName()!=null&&!"".equals(param.getParamName().trim())){
			dc.add(Restrictions.like("paramName", "%"+param.getParamName()+"%"));
			dcCou.add(Restrictions.like("paramName", "%"+param.getParamName()+"%"));
		}
		
		if(param.getType()!=null&&!"".equals(param.getType())){
			dc.add(Restrictions.eq("type", param.getType()));
			dcCou.add(Restrictions.eq("type", param.getType()));
		}
		
		dc.addOrder(Order.asc("type"));
		dc.addOrder(Order.asc("seq"));
		List<BaseParams> rows = (List<BaseParams>) this.hibernateTemplate.findByCriteria(dc, firstResult, row);
		
		
		dcCou.setProjection(Projections.rowCount());
		Long count =  (Long) this.hibernateTemplate.findByCriteria(dcCou).get(0);
		
		Map<String,Object> data=new HashMap<String,Object>();
		data.put("rows", rows);
		data.put("total", count);
		
		return data;
	}
	
	public List<BaseParams> getBaseParamByType(String type) {
		List<BaseParams> bpList = new ArrayList<BaseParams>();
		List<BaseParams> bps = (List<BaseParams>) servletContext.getAttribute("bps");
		if(bps==null) {
			bps=getBaseParams();
		}
		for (BaseParams param : bps) {
			if (param.getType().equals(type)) {
				bpList.add(param);
			}
		}
		return bpList;
	}

}
