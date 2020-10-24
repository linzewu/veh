package com.xs.veh.manager;

import java.util.HashMap;
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
	
	
	/**
	 * 工作量统计
	 * @param begin
	 * @param end
	 * @return
	 */
	public List<Map<String,Object>> findgzl2(String begin,String end,String sqlId){
		Session session = this.hibernateTemplate.getSessionFactory().getCurrentSession();
		String queryString = session.getNamedQuery(sqlId).getQueryString();
		SQLQuery query =session.createSQLQuery(queryString);
	
		query.setParameter("beginDate", begin);
		query.setParameter("endDate", end);
		query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		return query.list();
		
	}
	
	
	/**
	 * 业务统计
	 * @param begin
	 * @param end
	 * @return
	 */
	public Map<String,Object> getBusiness(Map<String,Object> param){
		
		String begin=(String)param.get("begin")+" 00:00:00";
		String end=(String)param.get("end")+" 23:59:59";
		Session session = this.hibernateTemplate.getSessionFactory().getCurrentSession();
		
		Map<String,Object> data =new HashMap<String,Object>();
		
		String sql ="SELECT MAX(hphm) as hphm,MAX(hpzl) as hpzl,clsbdh as clsbdh, MAX(jylsh) as jylsh,MAX(jycs) as jycs FROM TM_VehCheckLogin where "
				+ "dlsj>:begin and dlsj<=:end ";
		
		String jclb = (String)param.get("jclb");
		if(!StringUtils.isEmpty(jclb)) {
			if("1".equals(jclb)) {
				sql+=" and checkType=0";
			}
			if("2".equals(jclb)) {
				sql+=" and checkType=1 and zjlb=0";
			}
			
			if("3".equals(jclb)) {
				sql+=" and checkType=1 and zjlb=1";
			}
		}
		
		String sfhg =  (String)param.get("sfhg");
		
		if(!StringUtils.isEmpty(sfhg)) {
			
			if("1".equals(sfhg)) {
				sql+=" and jyjl='合格'";
			}
			if("2".equals(sfhg)) {
				sql+=" and jyjl='不合格' ";
			}
		}
		
		sql+="  group by clsbdh";
		
		String countSql="select count(*) from ("+sql+") as t";
		
		SQLQuery queryCount = session.createSQLQuery(countSql);
		SQLQuery query =session.createSQLQuery(sql);
		
		queryCount.setParameter("begin", begin);
		queryCount.setParameter("end", end);
		query.setParameter("begin", begin);
		query.setParameter("end", end);
		
		
		
		
		Integer rows=Integer.parseInt((String)param.get("rows"));
		Integer page=Integer.parseInt((String)param.get("page"));
		query.setMaxResults(rows);
		query.setFirstResult((page-1)*rows);
		query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		
		Integer count = (Integer) queryCount.uniqueResult();
		
		data.put("rows", query.list());
		data.put("total", count);
		
		return data;
		
	}
	
	
	
	/**
	 * 业务统计
	 * @param begin
	 * @param end
	 * @return
	 */
	public Map<String,Object> getBusiness2(Map<String,Object> param){
		
		String begin=(String)param.get("begin")+" 00:00:00";
		String end=(String)param.get("end")+" 23:59:59";
		Session session = this.hibernateTemplate.getSessionFactory().getCurrentSession();
		
		Map<String,Object> data =new HashMap<String,Object>();
		
		String sql ="SELECT MAX(hphm) as hphm,MAX(hpzl) as hpzl,clsbdh as clsbdh, MAX(jylsh) as jylsh,MAX(jycs) as jycs FROM TM_VehCheckLogin where "
				+ "upLineDate>:begin and upLineDate<=:end ";
		
		String jclb = (String)param.get("jclb");
		if(!StringUtils.isEmpty(jclb)) {
			if("1".equals(jclb)) {
				sql+=" and checkType=0";
			}
			if("2".equals(jclb)) {
				sql+=" and checkType=1 and zjlb=0";
			}
			
			if("3".equals(jclb)) {
				sql+=" and checkType=1 and zjlb=1";
			}
		}
		
		String sfhg =  (String)param.get("sfhg");
		
		if(!StringUtils.isEmpty(sfhg)) {
			
			if("1".equals(sfhg)) {
				sql+=" and jyjl='合格'";
			}
			if("2".equals(sfhg)) {
				sql+=" and jyjl='不合格' ";
			}
		}
		
		sql+="  group by clsbdh";
		
		String countSql="select count(*) from ("+sql+") as t";
		
		SQLQuery queryCount = session.createSQLQuery(countSql);
		SQLQuery query =session.createSQLQuery(sql);
		
		queryCount.setParameter("begin", begin);
		queryCount.setParameter("end", end);
		query.setParameter("begin", begin);
		query.setParameter("end", end);
		
		
		
		
		Integer rows=Integer.parseInt((String)param.get("rows"));
		Integer page=Integer.parseInt((String)param.get("page"));
		query.setMaxResults(rows);
		query.setFirstResult((page-1)*rows);
		query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		
		Integer count = (Integer) queryCount.uniqueResult();
		
		data.put("rows", query.list());
		data.put("total", count);
		
		return data;
		
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
