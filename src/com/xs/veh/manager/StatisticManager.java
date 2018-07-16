package com.xs.veh.manager;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
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
	public List<Map<String,Object>> findcllxheghz(String begin,String end,String sqlId,String condition,String cllx){
		Session session = this.hibernateTemplate.getSessionFactory().getCurrentSession();
		String queryString = session.getNamedQuery(sqlId).getQueryString();
		if(StringUtils.isNotEmpty(cllx)) {
			queryString = queryString + " where t."+condition+" =:cllx";
		}
		System.out.println(queryString);
		SQLQuery query =session.createSQLQuery(queryString);
	
		query.setParameter("begin", begin);
		query.setParameter("end", end);
		if(StringUtils.isNotEmpty(cllx)) {
			query.setParameter("cllx", cllx);
		}
		query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		return query.list();
		
	}
	
	public List<Map<String,Object>> findcllxheghz11(String begin,String end,String sqlId,String condition,String cllx){
		Session session = this.hibernateTemplate.getSessionFactory().getCurrentSession();
		//Query query = session.getNamedQuery(sqlId);
		String queryString = session.getNamedQuery(sqlId).getQueryString();
		if(StringUtils.isNotEmpty(cllx)) {
			queryString = " select a.* from ("+queryString + ")a where a."+condition+" =:cllx";
		}
		System.out.println(queryString);
		SQLQuery query =session.createSQLQuery(queryString);
		if(StringUtils.isNotEmpty(cllx)) {
			query.setParameter("cllx", cllx);
		}
		//query.setParameter("begin", begin);
		//query.setParameter("end", end);
		query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		return query.list();
		
	}
	
	

}
