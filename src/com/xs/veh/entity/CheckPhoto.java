package com.xs.veh.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("checkPhoto")
@Entity
@Table(name = "TM_CheckPhoto")
public class CheckPhoto extends BaseEntity {
	
	
	@Column(length = 20)
	private String jyjgbh;
	
	@Column(length = 2)
	private String jcxdh;
	
	@Column(length = 25)
	private String jylsh;

	@Column(length = 20)
	private String hphm;

	@Column(length = 10)
	private String hpzl;
	
	@Column(length = 30)
	private String clsbdh;

	@Column
	private Integer jycs;

	@Lob   
    @Basic(fetch=FetchType.LAZY)   
    @Column(name="zp", nullable=true)  
	private byte[] zp;
	
	@Column
	private Date pssj;
	
	@Column(length = 20)
	private String jyxm;
	
	@Column(length = 20)
	private String zpzl;

	public String getJyjgbh() {
		return jyjgbh;
	}

	public String getJcxdh() {
		return jcxdh;
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

	public Integer getJycs() {
		return jycs;
	}


	public Date getPssj() {
		return pssj;
	}

	public String getJyxm() {
		return jyxm;
	}

	public String getZpzl() {
		return zpzl;
	}

	public void setJyjgbh(String jyjgbh) {
		this.jyjgbh = jyjgbh;
	}

	public void setJcxdh(String jcxdh) {
		this.jcxdh = jcxdh;
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

	public void setJycs(Integer jycs) {
		this.jycs = jycs;
	}


	public void setPssj(Date pssj) {
		this.pssj = pssj;
	}

	public void setJyxm(String jyxm) {
		this.jyxm = jyxm;
	}

	public void setZpzl(String zpzl) {
		this.zpzl = zpzl;
	}

	public byte[] getZp() {
		return zp;
	}

	public void setZp(byte[] zp) {
		this.zp = zp;
	}
	

	

}
