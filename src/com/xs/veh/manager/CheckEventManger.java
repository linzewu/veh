package com.xs.veh.manager;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.CheckEvents;
import com.xs.veh.entity.CheckLog;
import com.xs.veh.job.CheckedInfoTaskJob;
import com.xs.veh.network.data.BaseDeviceData;

@Scope("prototype")
@Service("checkEventManger")
public class CheckEventManger {
	
	private static Logger logger = Logger.getLogger(CheckedInfoTaskJob.class);

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	public List<?> queryEntity(final String entity) {

		List<?> entitys = hibernateTemplate.execute(new HibernateCallback<List<?>>() {
			@Override
			public List<?> doInHibernate(Session session) throws HibernateException {
				List<?> entitys = session.createQuery("From " + entity + " where rccysbzt='0'").setFirstResult(0)
						.setMaxResults(1).list();
				return entitys;
			}
		});
		return entitys;
	}

	public List<?> getEvents() {

		return hibernateTemplate.execute(new HibernateCallback<List<?>>() {
			@Override
			public List<?> doInHibernate(Session session) throws HibernateException {
				List<?> entitys = session.createQuery(" from CheckEvents where state=0 order by  id asc")
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
		String line = hibernateTemplate.execute(new HibernateCallback<String>() {
			@Override
			public String doInHibernate(Session session) throws HibernateException {
				String sql = "SELECT DetLineSelect from registbak where regist_code=:code ";
				Object line = session.createSQLQuery(sql).setString("code", code).uniqueResult();
				return (String) line;
			}
		});
		return line;
	}

	public List<?> getViewData(final String viewName, final String jylsh,final String jyxm) {
		
		
		return hibernateTemplate.executeWithNativeSession(new HibernateCallback<List<?>>() {
			@Override
			public List<?> doInHibernate(Session session) throws HibernateException {
				
				String jyxmSql="";
				
				boolean jyxmFlag=viewName.equals("V18C55")||viewName.equals("V18C58");
				
				if(jyxmFlag){
					jyxmSql=" and jyxm=:jyxm";
				}
				
				SQLQuery sq=session.createSQLQuery("select * from " + viewName + "  where jylsh=:jylsh "+jyxmSql);
				
				sq.setString("jylsh", jylsh);
						
				if(jyxmFlag){
					sq.setString("jyxm", jyxm);
					logger.info("sql:"+"select * from " + viewName + "  where jylsh=:jylsh "+jyxmSql);
				}
				logger.info("ViewName " + viewName ); 
				List<?> entitys =sq.setFirstResult(0).setMaxResults(1)
						.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list(); 
				return entitys;
			}
		});
	}

	@Async
	public void createEvent(String jylsh, Integer jycs, String event, String jyxm, String hphm, String hpzl,
			String clsbdh,Integer csbj) {

		CheckEvents e = new CheckEvents();
		e.setJylsh(jylsh);
		e.setJycs(jycs);
		e.setEvent(event);
		e.setJyxm(jyxm);
		e.setHphm(hphm);
		e.setHpzl(hpzl);
		e.setClsbdh(clsbdh);
		e.setState(csbj);
		e.setCreateDate(new Date());
		this.hibernateTemplate.save(e);
		this.hibernateTemplate.flush();
		this.hibernateTemplate.clear();

	}
	
	@Async
	public void createEvent(Integer later,String jylsh, Integer jycs, String event, String jyxm, String hphm, String hpzl,
			String clsbdh,Integer csbj) throws InterruptedException {
		
		Thread.sleep(later);

		createEvent(jylsh, jycs, event, jyxm, hphm, hpzl, clsbdh,csbj);

	}
	
	@Async
	public void createEvent(String jylsh, Integer jycs, String event, String jyxm, String hphm, String hpzl,
			String clsbdh) {

		CheckEvents e = new CheckEvents();
		e.setJylsh(jylsh);
		e.setJycs(jycs);
		e.setEvent(event);
		e.setJyxm(jyxm);
		e.setHphm(hphm);
		e.setHpzl(hpzl);
		e.setClsbdh(clsbdh);
		e.setCreateDate(new Date());
		this.hibernateTemplate.save(e);
		this.hibernateTemplate.flush();
		this.hibernateTemplate.clear();

	}
	
	@Async
	public <T> void saveDataAsync(BaseDeviceData data) {
		this.hibernateTemplate.saveOrUpdate(data);
	}

	public void createEvent(String jylsh, Integer jycs, String event, String jyxm, String hphm, String hpzl,
			String clsbdh, String zpzl,Integer csbj) {

		CheckEvents e = new CheckEvents();
		e.setJylsh(jylsh);
		e.setJycs(jycs);
		e.setEvent(event);
		e.setJyxm(jyxm);
		e.setHphm(hphm);
		e.setHpzl(hpzl);
		e.setClsbdh(clsbdh);
		e.setState(0);
		e.setCreateDate(new Date());
		e.setZpzl(zpzl);
		this.hibernateTemplate.save(e);

	}
	
	
	public String getCheckItem(String jylsh,String type) throws DocumentException{
		
		List<CheckLog> checkLogs =(List<CheckLog>) this.hibernateTemplate.find("from CheckLog where jylsh=? and jkbmc=?", jylsh,"18C51");
		
		if(checkLogs!=null&&!checkLogs.isEmpty()){
			
			CheckLog checkLog = checkLogs.get(0);
			
			Document doc = DocumentHelper.parseText(checkLog.getXml());
			
			String item = doc.getRootElement().element("head").element(type).getText();
			
			return item;
			
		}
		return "";
		
	}

}
