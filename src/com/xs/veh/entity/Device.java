package com.xs.veh.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Scope("prototype")
@Component("device")
@Entity
@Table(name = "TM_Device")
@JsonIgnoreProperties(value ={"hibernateLazyInitializer","handler","fieldHandler"})
public class Device extends BaseEntity {
	
	@Column
	private Integer type;
	
	@Column(length=20)
	private String ip;
	
	@Column(length=10)
	private String port;
	
	@Column
	private Integer line;
	
	

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public Integer getType() {
		return type;
	}

	public String getIp() {
		return ip;
	}

	public String getPort() {
		return port;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setPort(String port) {
		this.port = port;
	}
	
}
