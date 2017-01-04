package com.xs.veh.manager;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.CheckEvents;
import com.xs.veh.entity.CheckLog;

@Scope("prototype")
@Service("checkEventManger")
public class CheckEventManger  {

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	public List<?> queryEntity(final String entity) {

		List<?> entitys = hibernateTemplate.execute(
				new HibernateCallback<List<?>>() {
					@Override
					public List<?> doInHibernate(Session session)
							throws HibernateException {
						List<?> entitys = session
								.createQuery(
										"From " + entity
												+ " where rccysbzt='0'")
								.setFirstResult(0).setMaxResults(1).list();
						return entitys;
					}
				});
		return entitys;
	}

	public List<?> getEvents() {
		
		return hibernateTemplate.execute(
				new HibernateCallback<List<?>>() {
					@Override
					public List<?> doInHibernate(Session session)
							throws HibernateException {
						List<?> entitys = session
								.createQuery(
										" from CheckEvents where state=0 order by  id asc")
								.setFirstResult(0).setMaxResults(100).list();
						return entitys;
					}
				});
	}

	public Integer saveLog(CheckLog rz) {
		Integer sid = (Integer) this.hibernateTemplate.save(rz);
		return sid;
	}

	public void update(Object entity) {
		hibernateTemplate.update(entity);
	}

	public void delete(Object entity) {
		hibernateTemplate.delete(entity);
	}

	public void save(Object entity) {
		hibernateTemplate.save(entity);
	}


	public String getDetLineSelect(final String code) {
		String line = hibernateTemplate.execute(
				new HibernateCallback<String>() {
					@Override
					public String doInHibernate(Session session)
							throws HibernateException {
						String sql = "SELECT DetLineSelect from registbak where regist_code=:code ";
						Object line = session.createSQLQuery(sql)
								.setString("code", code).uniqueResult();
						return (String) line;
					}
				});
		return line;
	}
		
	public List<?> getViewData(final String viewName,final String jylsh) {
		return hibernateTemplate.execute(
				new HibernateCallback<List<?>>() {
					@Override
					public List<?> doInHibernate(Session session)
							throws HibernateException {
						List<?> entitys = session
								.createSQLQuery("select * from "+viewName + "  where jylsh=:jylsh")
								.setString("jylsh",jylsh)
								.setFirstResult(0)
								.setMaxResults(1)
								.setResultTransformer(
										Transformers.ALIAS_TO_ENTITY_MAP).list();
						return entitys;
					}
				});
	}
	
	public void createEvent(String jylsh,Integer jycs,String event,String jyxm,String hphm,String hpzl,String clsbdh){
		
		CheckEvents e=new CheckEvents();
		e.setJylsh(jylsh);
		e.setJycs(jycs);
		e.setEvent(event);
		e.setJyxm(jyxm);
		e.setHphm(hphm);
		e.setHpzl(hpzl);
		e.setClsbdh(clsbdh);
		e.setState(0);
		e.setCreateDate(new Date());
		this.hibernateTemplate.save(e);
		
	}
	
	

}
