package com.xs.veh.manager;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

@Service("statisticManager")
public class StatisticManager {
	
	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	/**
	 * 车辆类型合格率汇总
	 * @param begin
	 * @param end
	 * @return
	 */
	public List<Map<String,Object>> findcllxheghz(String begin,String end,String sqlId){
		Session session = this.hibernateTemplate.getSessionFactory().getCurrentSession();
		Query query = session.getNamedQuery(sqlId);
		query.setParameter("begin", begin);
		query.setParameter("end", end);
		query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		return query.list();
		
	}
	
	public List<Map<String,Object>> findcllxheghz11(String begin,String end,String sqlId){
		Session session = this.hibernateTemplate.getSessionFactory().getCurrentSession();
		Query query = session.getNamedQuery(sqlId);
		//query.setParameter("begin", begin);
		//query.setParameter("end", end);
		query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		return query.list();
		
	}
	
	

}
