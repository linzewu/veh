package com.xs.veh.manager;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.xs.veh.entity.CheckQueue;

@Service("checkQueueManager")
@Scope("singleton")
public class CheckQueueManager {
	
	@Resource(name="hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	public synchronized Integer getPdxh(String jcxdh, Integer gwsx) {
		List list =  hibernateTemplate.find("select max(pdxh) from CheckQueue where jcxdh=? and gwsx=? ", Integer.parseInt(jcxdh), gwsx);
		//没有排队则为1
		Object o = list.get(0);
		
		if(o==null){
			return 1;
		}else{
			return ((Integer)o)+1;
		}
	}
	
	
	public List<Map<String,Object>> getCheckQueueVeh(Integer status) {
		Session session = this.hibernateTemplate.getSessionFactory().getCurrentSession();
		SQLQuery sqlQuery = session.createSQLQuery("select MAX(jcxdh) as jcxdh,MAX(hphm) as hphm,MAX(hpzl) as hpzl,jylsh from TM_CheckQueue where status=? group by jylsh");
		sqlQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		sqlQuery.setParameter(0, status);
		return sqlQuery.list();
	}
	
	public void downLine(String jylsh) {
		Session session = this.hibernateTemplate.getSessionFactory().getCurrentSession();
		SQLQuery sqlQuery = session.createSQLQuery("update TM_CheckQueue set status=1 where jylsh=?");
		sqlQuery.setParameter(0, jylsh);
		sqlQuery.executeUpdate();
	}
	
	
	public synchronized void reUpLine(String jylsh) {
		List<CheckQueue> list = (List<CheckQueue>) this.hibernateTemplate.find("from CheckQueue where status=1 and jylsh=? order by lcsx asc",  jylsh);
		
		if(!CollectionUtils.isEmpty(list)) {
			for(CheckQueue checkQueue : list) {
				checkQueue.setPdxh(getPdxh(checkQueue.getJcxdh().toString(), checkQueue.getGwsx()));
				checkQueue.setStatus(0);
				this.hibernateTemplate.update(checkQueue);
			}
			
		}
		
	}

}
