package com.xs.veh.manager;

import java.util.List;

import javax.annotation.Resource;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.CoreFunction;

@Service("coreFunctionManager")
public class CoreFunctionManager {
	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	public List<CoreFunction> getAllCoreFunction(){
		return (List<CoreFunction>)this.hibernateTemplate.find(" from CoreFunction", null);
	}
	
	public void deleteAllCoreFunction() {
		this.hibernateTemplate.execute(new HibernateCallback<CoreFunction>() {
			@Override
			public CoreFunction doInHibernate(Session session) throws HibernateException {
				Query query = session.createQuery("delete from CoreFunction");
                query.executeUpdate();
                return null;
			}
			
		});
	}
	
	public void save(List <CoreFunction> funs) {
		for(CoreFunction fun:funs) {
			this.hibernateTemplate.save(fun);
		}
	}

}
