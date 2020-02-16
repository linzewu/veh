package com.xs.veh.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Scope("prototype")
@Component("HBDeviceConfig")
@Entity
@Table(name = "TM_HBDeviceConfig")
@JsonIgnoreProperties(value ={"hibernateLazyInitializer","handler","fieldHandler","password"})
public class HBDeviceConfig extends BaseEntity {
	
	@Column(length=100)
	private String ip;
	
	@Column
	private Integer deviceId;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Integer deviceId) {
		this.deviceId = deviceId;
	}

	
}
