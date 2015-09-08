package com.xs.veh.manager;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.User;
import com.xs.veh.util.PageInfo;

@Service("userManager")
public class UserManager {

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	public User login(String userName, String password) {

		List<User> list = (List<User>) hibernateTemplate.findByNamedQueryAndNamedParam("User.login",
				new String[] { "userName", "password" }, new String[] { userName, password });

		if (list != null && list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}

	}

	public List<User> getUsers(final User user,final PageInfo pageInfo) {

		List<User> list = (List<User>) hibernateTemplate.executeWithNativeSession(new HibernateCallback<List<User>>() {

			@Override
			public List<User> doInHibernate(Session session) throws HibernateException {

				StringBuilder sql = new StringBuilder("from User where userName!='admin' ");

				List params = new ArrayList();
				List paramType = new ArrayList();

				if (user.getUserName() != null && !"".equals(user.getUserName().trim())) {
					sql.append(" and userName like ?");
					params.add("%" + user.getUserName() + "%");
					paramType.add(StandardBasicTypes.STRING);

				}
				Query query = session.createQuery(sql.toString()).setParameters(params.toArray(), (Type[]) paramType.toArray());
				return pageInfo.toPage(query).list();
			}
		});

		return list;
	}
	
	public Integer getUserCount(final User user,final PageInfo pageInfo) {

		Integer count = (Integer)hibernateTemplate.executeWithNativeSession(new HibernateCallback<Integer>() {

			@Override
			public Integer doInHibernate(Session session) throws HibernateException {

				StringBuilder sql = new StringBuilder("select count(*) from User where userName!='admin' ");

				List params = new ArrayList();
				List paramType = new ArrayList();

				if (user.getUserName() != null && !"".equals(user.getUserName().trim())) {
					sql.append(" and userName like ?");
					params.add("%" + user.getUserName() + "%");
					paramType.add(StandardBasicTypes.STRING);

				}
				Query query = session.createQuery(sql.toString()).setParameters(params.toArray(), (Type[]) paramType.toArray());
				return (Integer)pageInfo.toPage(query).uniqueResult();
			}
		});

		return count;
	}
	
	public void saveUser(User user){
		
		this.hibernateTemplate.merge(user);
		
	}

}
