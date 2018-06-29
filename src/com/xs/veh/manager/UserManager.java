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

import com.xs.common.Constant;
import com.xs.veh.entity.User;
import com.xs.veh.util.PageInfo;

@Service("userManager")
public class UserManager {

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	public User login(String userName) {

		List<User> list = (List<User>) hibernateTemplate.findByNamedQueryAndNamedParam("User.login",
				new String[] { "userName", "userState" }, new Object[] { userName, 2 });

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
				
				
				Query query = session.createQuery(sql.toString()).setParameters(params.toArray(), (Type[]) paramType.toArray(new Type[paramType.size()]));
				return pageInfo.toPage(query).list();
			}
		});

		return list;
	}
	
	public List<User> getUsers(){
		return  (List<User>) this.hibernateTemplate.find(" from User  where userName!='admin'");
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
				Query query = session.createQuery(sql.toString()).setParameters(params.toArray(), (Type[]) paramType.toArray(new Type[paramType.size()]));
				return ((Long)pageInfo.toPage(query).uniqueResult()).intValue();
			}
		});

		return count;
	}
	
	public User saveUser(User user){
		if(user.getId()==null){
			//user.setPassword(user.encodePwd(Constant.initPassword));
			user.setUserState(User.USER_STATE_PASSWORD_INVALID);
			 this.hibernateTemplate.save(user);
			 user.setPassword(user.encodePwd(Constant.initPassword));
		}else{
			User oldUser=this.hibernateTemplate.load(User.class, user.getId());
			user.setPassword(oldUser.getPassword());
			user.setUserName(oldUser.getUserName());
			user.setLastLoginDate(oldUser.getLastLoginDate());
		}
		return this.hibernateTemplate.merge(user);
		
		
	}
	
	public User queryUserByUserName(User user){
		
		StringBuffer sb=new StringBuffer("from User where userName=?");
		
		List<User> users;
		
		if(user.getId()!=null){
			sb.append(" and id!=?");
			users=(List<User> )this.hibernateTemplate.find(sb.toString(), user.getUserName(),user.getId());
		}else{
			users=(List<User> )this.hibernateTemplate.find(sb.toString(), user.getUserName());
		}
		
		return users==null||users.size()==0?null:users.get(0);
	}
	
	
	public void resetPassword(User user){
		
		User oldUser=  this.hibernateTemplate.load(User.class, user.getId());
		oldUser.setPassword(Constant.initPassword);
		oldUser.setUserState(User.USER_STATE_PASSWORD_INVALID);
		this.hibernateTemplate.update(oldUser);
	}
	
	public void deleteUser(User user){
		
		this.hibernateTemplate.delete(user);
		
	}
	
	
	public void updateUser(User user){
		this.hibernateTemplate.update(user);
	}
	
	public User loadUser(Integer id){
		return this.hibernateTemplate.load(User.class, id);
	}

}
