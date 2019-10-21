package com.xs.veh.entity;

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
@Component("videoConfig")
@Entity
@Table(name = "VideoConfig2")
@JsonIgnoreProperties(value ={"hibernateLazyInitializer","handler","fieldHandler"})
public class VideoConfig {
	
	@Id
	@Column
	@GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
	private String id;
	
	@Column
	private String jyjgbh;
	
	@Column
	private String jcxdh;
	
	@Column
	private String ip;
	
	@Column
	private String port;
	
	@Column
	private String userName;
	
	@Column
	private String password;
	
	@Column
	private int channel;
	
	@Column
	private String jyxm;
	
	@Column
	private String deviceName;
	
	

	public String getId() {
		return id;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getJyjgbh() {
		return jyjgbh;
	}

	public String getJcxdh() {
		return jcxdh;
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

	public int getChannel() {
		return channel;
	}

	public String getJyxm() {
		return jyxm;
	}


	public void setJyjgbh(String jyjgbh) {
		this.jyjgbh = jyjgbh;
	}

	public void setJcxdh(String jcxdh) {
		this.jcxdh = jcxdh;
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

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public void setJyxm(String jyxm) {
		this.jyxm = jyxm;
	}
	
	
}
