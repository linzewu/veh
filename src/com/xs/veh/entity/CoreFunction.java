package com.xs.veh.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
@Scope("prototype")
@Component("coreFunction")
@Entity
@Table(name = "TB_CoreFunction")
public class CoreFunction extends BaseEntity {
	
	//功能点
	@Column(length=100)
	private String functionPoint;

	public String getFunctionPoint() {
		return functionPoint;
	}

	public void setFunctionPoint(String functionPoint) {
		this.functionPoint = functionPoint;
	}
	
	

}
