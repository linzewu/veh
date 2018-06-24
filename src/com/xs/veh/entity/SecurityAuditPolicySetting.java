package com.xs.veh.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("securityAuditPolicySetting")
@Entity
@Table(name = "TM_SecurityAuditPolicySetting")
public class SecurityAuditPolicySetting {
	
	@Id
	@GenericGenerator(name = "idGenerator", strategy = "identity")
	@GeneratedValue(generator = "idGenerator")
	@Column(name="id")
	private Integer id;
	
	@Column(length = 120)
	private String aqsjcllxmc;
	
	@Column(length = 120)
	private String aqsjclzlxmc;
	
	@Column
	private Integer clz;
	
	@Column(length = 300)
	private String clzsm;
	
	@Column(length = 1)
	private String sfkq;

	public Integer getId() {
		return id;
	}

	public String getAqsjcllxmc() {
		return aqsjcllxmc;
	}

	public String getAqsjclzlxmc() {
		return aqsjclzlxmc;
	}

	public Integer getClz() {
		return clz;
	}

	public String getClzsm() {
		return clzsm;
	}

	public String getSfkq() {
		return sfkq;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setAqsjcllxmc(String aqsjcllxmc) {
		this.aqsjcllxmc = aqsjcllxmc;
	}

	public void setAqsjclzlxmc(String aqsjclzlxmc) {
		this.aqsjclzlxmc = aqsjclzlxmc;
	}

	public void setClz(Integer clz) {
		this.clz = clz;
	}

	public void setClzsm(String clzsm) {
		this.clzsm = clzsm;
	}

	public void setSfkq(String sfkq) {
		this.sfkq = sfkq;
	}
	

}
