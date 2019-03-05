package com.xs.veh.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@Scope("prototype")
@Component("videoInfo")
@Entity
@Table(name = "TT_VMS")
@JsonIgnoreProperties(value ={"hibernateLazyInitializer","handler","fieldHandler"})
public class VideoInfo {
	
	@Id
	@GenericGenerator(name = "idGenerator", strategy = "identity")
	@GeneratedValue(generator = "idGenerator")
	@Column(name="VMSID")
	private Integer id;
	
	@Column
	private String jylsh;
	
	@Column
	private String jyjgmc;
	
	@Column
	private String jyjgbh;
	
	@Column(name="XH")
	private Integer jcxdh;
	
	@Column
	private String hphm;
	
	@Column
	private String hpzl;
	
	@Column
	private String jyxm;
	
	@Column
	private Integer jycs;
	
	@Column
	private String jylb;
	
	@Column
	private Date kssj;
	
	@Column
	private Date jssj;
	
	@Column(name="nvr_ip")
	private String ip;
	
	@Column(name="nvr_port")
	private String port;
	
	@Column(name="nvr_userName")
	private String userName;
	
	@Column(name="nvr_password")
	private String password;
	
	@Column(name="nvr_channelno")
	private String channelno;
	
	@Column
	private String clsbdh;
	
	
	

	public String getClsbdh() {
		return clsbdh;
	}

	public void setClsbdh(String clsbdh) {
		this.clsbdh = clsbdh;
	}

	public String getIp() {
		return ip;
	}

	public String getPort() {
		return port;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getChannelno() {
		return channelno;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setChannelno(String channelno) {
		this.channelno = channelno;
	}

	public Integer getId() {
		return id;
	}

	public String getJylsh() {
		return jylsh;
	}

	public String getJyjgmc() {
		return jyjgmc;
	}

	public String getJyjgbh() {
		return jyjgbh;
	}

	public Integer getJcxdh() {
		return jcxdh;
	}

	public String getHphm() {
		return hphm;
	}

	public String getHpzl() {
		return hpzl;
	}

	public String getJyxm() {
		return jyxm;
	}

	public Integer getJycs() {
		return jycs;
	}

	public String getJylb() {
		return jylb;
	}

	public Date getKssj() {
		return kssj;
	}

	public Date getJssj() {
		return jssj;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setJylsh(String jylsh) {
		this.jylsh = jylsh;
	}

	public void setJyjgmc(String jyjgmc) {
		this.jyjgmc = jyjgmc;
	}

	public void setJyjgbh(String jyjgbh) {
		this.jyjgbh = jyjgbh;
	}

	public void setJcxdh(Integer jcxdh) {
		this.jcxdh = jcxdh;
	}

	public void setHphm(String hphm) {
		this.hphm = hphm;
	}

	public void setHpzl(String hpzl) {
		this.hpzl = hpzl;
	}

	public void setJyxm(String jyxm) {
		this.jyxm = jyxm;
	}

	public void setJycs(Integer jycs) {
		this.jycs = jycs;
	}

	public void setJylb(String jylb) {
		this.jylb = jylb;
	}

	public void setKssj(Date kssj) {
		this.kssj = kssj;
	}

	public void setJssj(Date jssj) {
		this.jssj = jssj;
	}

	
}
