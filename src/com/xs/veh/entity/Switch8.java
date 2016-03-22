package com.xs.veh.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Scope("prototype")
@Component("switch8")
@Entity
@Table(name = "TM_Switch8")
@JsonIgnoreProperties(value ={"hibernateLazyInitializer","handler","fieldHandler"})
public class Switch8 extends BaseEntity {
	
	@Column(length=20)
	private String ip;
	
	@Column(length=10)
	private String port;
	
	@Column
	private Integer line;
	
	@Column
	private Integer x1;
	
	@Column
	private Integer x2;
	
	@Column
	private Integer x3;
	
	@Column
	private Integer x4;
	
	@Column
	private Integer x5;
	
	@Column
	private Integer x6;
	
	@Column
	private Integer x7;
	
	@Column
	private Integer x8;
	
	
	
	
	

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}



	public String getIp() {
		return ip;
	}

	public String getPort() {
		return port;
	}

	public Integer getX1() {
		return x1;
	}

	public Integer getX2() {
		return x2;
	}

	public Integer getX3() {
		return x3;
	}

	public Integer getX4() {
		return x4;
	}

	public Integer getX5() {
		return x5;
	}

	public Integer getX6() {
		return x6;
	}

	public Integer getX7() {
		return x7;
	}

	public Integer getX8() {
		return x8;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setX1(Integer x1) {
		this.x1 = x1;
	}

	public void setX2(Integer x2) {
		this.x2 = x2;
	}

	public void setX3(Integer x3) {
		this.x3 = x3;
	}

	public void setX4(Integer x4) {
		this.x4 = x4;
	}

	public void setX5(Integer x5) {
		this.x5 = x5;
	}

	public void setX6(Integer x6) {
		this.x6 = x6;
	}

	public void setX7(Integer x7) {
		this.x7 = x7;
	}

	public void setX8(Integer x8) {
		this.x8 = x8;
	}
	
}
