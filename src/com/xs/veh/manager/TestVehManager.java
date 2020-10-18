package com.xs.veh.manager;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.xs.veh.entity.TestResult;
import com.xs.veh.entity.TestVeh;
import com.xs.veh.entity.VehCheckLogin;
@Service("testVehManager")
public class TestVehManager {
	
	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	
	public List<TestVeh> getTestVehList() {

		List<TestVeh> testVehs = (List<TestVeh>) this.hibernateTemplate.find("from TestVeh");

		return testVehs;
	}
	
	public List<Map> getTestVehInfo(final String lsh){
		final String sql ="select b.* from TM_VehCheckLogin a, QCPFWQ2018.dbo.TestVeh b where a.jylsh=b.jylsh  and a.jylsh = '"+lsh+"'";
		
		List<Map> datas = (List<Map>) this.hibernateTemplate.execute(new HibernateCallback<List<Map>>() {
			@Override
			public List<Map> doInHibernate(Session session) throws HibernateException {
				List<Map> datas = (List<Map>) session.createSQLQuery(sql).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
				System.out.println("datas:"+datas);
				return datas;
			}
		});
		return null;
	}
	
	public void updateTestVeh(TestVeh testVeh) {
		this.hibernateTemplate.update(testVeh);
	}
	
	public VehCheckLogin getVehCheckLogin(String jylsh){
		List<VehCheckLogin> array = (List<VehCheckLogin>) this.hibernateTemplate
				.find("from VehCheckLogin where jylsh=?", jylsh);
		if(!CollectionUtils.isEmpty(array)) {
			return array.get(0);
		}
		return null;
	}
	
	
	public List<TestResult> getTestResultOfNullStatus() {
		List<TestResult> testResults = (List<TestResult>) this.hibernateTemplate.find("from TestResult where status is null");
		return testResults;
	}
	
	public void updateTestResult(TestResult testResult) {
		
		this.hibernateTemplate.saveOrUpdate(testResult);
		
	}
	
	public void updateVehCheckLogin(VehCheckLogin vehCheckLogin) {
		
		this.hibernateTemplate.saveOrUpdate(vehCheckLogin);
		
	}

}
