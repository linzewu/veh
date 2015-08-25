package com.xs.veh.manager;

import java.util.List;

import javax.annotation.Resource;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.User;

@Service("userManager")
public class UserManager {

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	public User login(String userName, String password) {

		// DetachedCriteria dc = DetachedCriteria.forClass(User.class);
		//
		// dc.add(Restrictions.eq("userName", userName));
		// dc.add(Restrictions.eq("password", password));
		//
		// List<User> list = (List<User>) hibernateTemplate.findByCriteria(dc);
		//
		
		List<User> list = (List<User>) hibernateTemplate.findByNamedQueryAndNamedParam("User.login",
				new String[] { "userName", "password" }, new String[] { userName, password });

		if (list != null && list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}

	}

}
