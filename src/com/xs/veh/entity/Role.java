package com.xs.veh.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("role")
@Entity
@Table(name = "TB_Role")
public class Role extends BaseEntity {
	
	@Column(length=32)
	private String roleName;
	
	//功能点
	@Column(length=1000)
	private String functionPoint;
	
	@Column(length=32)
	private String roleType;

	public String getRoleName() {
		return roleName;
	}

	public String getFunctionPoint() {
		return functionPoint;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public void setFunctionPoint(String functionPoint) {
		this.functionPoint = functionPoint;
	}

	public String getRoleType() {
		return roleType;
	}

	public void setRoleType(String roleType) {
		this.roleType = roleType;
	}

}
