package com.xs.veh.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("vehInfoTemp")
@Entity
@Table(name = "TM_VehInfoTemp")
public class VehInfoTemp {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(length=10000)
	private String vehInfo;
	
	@Column
	private Integer status;
	
	@Column(length=100)
	private String hphm;
	
	@Column(length=20)
	private String hpzl;
	
	@Column(length=20)
	private String clsbdh;
	
	

	public String getVehInfo() {
		return vehInfo;
	}

	public void setVehInfo(String vehInfo) {
		this.vehInfo = vehInfo;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getHphm() {
		return hphm;
	}

	public void setHphm(String hphm) {
		this.hphm = hphm;
	}

	public String getHpzl() {
		return hpzl;
	}

	public void setHpzl(String hpzl) {
		this.hpzl = hpzl;
	}

	public String getClsbdh() {
		return clsbdh;
	}

	public void setClsbdh(String clsbdh) {
		this.clsbdh = clsbdh;
	}
	

}
