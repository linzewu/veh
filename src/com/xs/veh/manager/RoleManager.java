package com.xs.veh.manager;

import java.util.List;

import javax.annotation.Resource;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.Role;
@Service("roleManager")
public class RoleManager {
	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	public List<Role> getAllRole(){
		return (List<Role>)this.hibernateTemplate.find(" from Role", null);
	}
	
	public List<Role> getAllRoleNoAdmin(){
		return (List<Role>)this.hibernateTemplate.find(" from Role where roleName != '超级管理员'", null);
	}

	
	public List<Role> getRole(Integer page, Integer rows, Role role) {

		DetachedCriteria query = DetachedCriteria.forClass(Role.class);

		Integer firstResult = (page - 1) * rows;
		query.add(Restrictions.ne("roleName", "超级管理员"));

		List<Role> vcps = (List<Role>) this.hibernateTemplate.findByCriteria(query, firstResult,
				rows);

		return vcps;
	}
	
	public Integer getRoleCount(Integer page, Integer rows, Role role) {

		DetachedCriteria query = DetachedCriteria.forClass(Role.class);

		query.setProjection(Projections.rowCount());
		query.add(Restrictions.ne("roleName", "超级管理员"));

		List<Long> count = (List<Long>) hibernateTemplate.findByCriteria(query);

		return count.get(0).intValue();
	}

	/**
	 * 插入
	 * @param operationLog
	 */
	public Role saveRole(Role role) {
		if (role.getId() != null) {
			Role oldRole=this.hibernateTemplate.load(Role.class, role.getId());
			role.setRoleName(oldRole.getRoleName());
		}
		return this.hibernateTemplate.merge(role);

	}
	
	public void delete(Integer id) throws Exception {
		Role role= this.hibernateTemplate.load(Role.class, id);
		
		if("0".equals(role.getRoleType())){
			throw new Exception("系统角色无法删除");
		}else{
			hibernateTemplate.delete(role);
		}
		
	}
	
	public Role queryRoleByRoleName(Role role){
		
		StringBuffer sb=new StringBuffer("from Role where roleName=?");
		
		List<Role> roles;
		
		if(role.getId()!=null){
			sb.append(" and id!=?");
			roles=(List<Role> )this.hibernateTemplate.find(sb.toString(), role.getRoleName(),role.getId());
		}else{
			roles=(List<Role> )this.hibernateTemplate.find(sb.toString(), role.getRoleName());
		}
		
		return roles==null||roles.size()==0?null:roles.get(0);
	}
	
	public Role queryRoleById(Integer id){
		
		StringBuffer sb=new StringBuffer("from Role where id=?");
		
		List<Role> roles=(List<Role> )this.hibernateTemplate.find(sb.toString(), id);
		
		return roles==null||roles.size()==0?null:roles.get(0);
	}
}
