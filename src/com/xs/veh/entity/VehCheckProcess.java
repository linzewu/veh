package com.xs.veh.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Scope("prototype")
@Component("vehCheckeProcess")
@Entity
@Table(name = "TM_VehCheckeProcess")
@JsonIgnoreProperties(value ={"hibernateLazyInitializer"})
public class VehCheckProcess extends BaseEntity {
	
	@Column(length=32)
	private String jylsh;
	
	@Column(length=30)
	private String hphm;
	
	@Column(length=10)
	private String hpzl;
	
	@Column(length=32)
	private String clsbdh;
	
	@Column(length=10)
	private String jyxm;
	
	@Column(length=32)
	private Date kssj;
	
	@Column(length=32)
	private Date jssj;
	
	@Column
	private Integer jycs;
	
	

	public Date getKssj() {
		return kssj;
	}

	public Date getJssj() {
		return jssj;
	}

	public Integer getJycs() {
		return jycs;
	}

	public void setKssj(Date kssj) {
		this.kssj = kssj;
	}

	public void setJssj(Date jssj) {
		this.jssj = jssj;
	}

	public void setJycs(Integer jycs) {
		this.jycs = jycs;
	}

	public String getJylsh() {
		return jylsh;
	}

	public String getHphm() {
		return hphm;
	}

	public String getHpzl() {
		return hpzl;
	}

	public String getClsbdh() {
		return clsbdh;
	}

	public String getJyxm() {
		return jyxm;
	}


	public void setJylsh(String jylsh) {
		this.jylsh = jylsh;
	}

	public void setHphm(String hphm) {
		this.hphm = hphm;
	}

	public void setHpzl(String hpzl) {
		this.hpzl = hpzl;
	}

	public void setClsbdh(String clsbdh) {
		this.clsbdh = clsbdh;
	}

	public void setJyxm(String jyxm) {
		this.jyxm = jyxm;
	}

}
