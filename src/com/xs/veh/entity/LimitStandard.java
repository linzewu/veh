package com.xs.veh.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("limitStandard")
@Entity
@Table(name = "TM_LimitStandard")
public class LimitStandard extends BaseEntity {
	
	@Column(length=64)
	private String xzlx;
	
	@Column(length=64)
	private String xzqj;
	
	@Column(length=64)
	private String hgz;
	
	@Column(length=8000)
	private String xztj;
	
	@Column(length=2000)
	private String bz;
	
	

	public String getXzlx() {
		return xzlx;
	}

	public String getXzqj() {
		return xzqj;
	}

	public String getHgz() {
		return hgz;
	}

	public String getXztj() {
		return xztj;
	}

	public String getBz() {
		return bz;
	}

	public void setXzlx(String xzlx) {
		this.xzlx = xzlx;
	}

	public void setXzqj(String xzqj) {
		this.xzqj = xzqj;
	}

	public void setHgz(String hgz) {
		this.hgz = hgz;
	}

	public void setXztj(String xztj) {
		this.xztj = xztj;
	}

	public void setBz(String bz) {
		this.bz = bz;
	}
	

}
