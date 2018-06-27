package com.xs.veh.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

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
	
	@Column(length = 120)
	private String clz;
	
	@Column(length = 300)
	private String clzsm;
	
	@Column(length = 1)
	private String sfkq;
	@Transient
	List<SecurityAuditPolicySetting> updateList = new ArrayList<SecurityAuditPolicySetting>();

	public Integer getId() {
		return id;
	}

	public String getAqsjcllxmc() {
		return aqsjcllxmc;
	}

	public String getAqsjclzlxmc() {
		return aqsjclzlxmc;
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

	public void setClzsm(String clzsm) {
		this.clzsm = clzsm;
	}

	public void setSfkq(String sfkq) {
		this.sfkq = sfkq;
	}

	public List<SecurityAuditPolicySetting> getUpdateList() {
		return updateList;
	}

	public void setUpdateList(List<SecurityAuditPolicySetting> updateList) {
		this.updateList = updateList;
	}

	public String getClz() {
		return clz;
	}

	public void setClz(String clz) {
		this.clz = clz;
	}
	

}
