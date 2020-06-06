package com.xs.veh.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("plateApplyTable")
@Entity
@Table(name = "TM_PlateApplyTable")
public class PlateApplyTable extends BaseEntity {
	
	@Column
	private String yzbm;
	
	@Column(length=512)
	private String yjdz;
	
	@Column(length=10)
	private String sqlx;
	
	@Column
	private String jylsh;
	
	@Column
	private String sjhm;
	
	@Column
	private String gddh;
	
	@Column
	private String dlr;
	
	@Column(length=100)
	private String dlrdh;
	
	

	public String getDlr() {
		return dlr;
	}

	public void setDlr(String dlr) {
		this.dlr = dlr;
	}

	public String getDlrdh() {
		return dlrdh;
	}

	public void setDlrdh(String dlrdh) {
		this.dlrdh = dlrdh;
	}

	public String getSjhm() {
		return sjhm;
	}

	public void setSjhm(String sjhm) {
		this.sjhm = sjhm;
	}

	public String getGddh() {
		return gddh;
	}

	public void setGddh(String gddh) {
		this.gddh = gddh;
	}

	public String getJylsh() {
		return jylsh;
	}

	public void setJylsh(String jylsh) {
		this.jylsh = jylsh;
	}

	public String getYzbm() {
		return yzbm;
	}

	public void setYzbm(String yzbm) {
		this.yzbm = yzbm;
	}

	public String getYjdz() {
		return yjdz;
	}

	public void setYjdz(String yjdz) {
		this.yjdz = yjdz;
	}

	public String getSqlx() {
		return sqlx;
	}

	public void setSqlx(String sqlx) {
		this.sqlx = sqlx;
	}
	
	
	

}
