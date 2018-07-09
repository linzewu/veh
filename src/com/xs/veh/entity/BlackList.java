package com.xs.veh.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("blackList")
@Entity
@Table(name = "TM_BLACKLIST")
public class BlackList {
	
	
	@Id
	@Column(length=20,unique=true)
	private String ip;
	
	@Column
	private Integer failCount;
	
	@Column
	private Date lastUpdateTime;
	
	@Column
	private String createBy;
	
	@Column(length=2)
	private String enableFlag;
	
	

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public String getIp() {
		return ip;
	}

	public Integer getFailCount() {
		return failCount;
	}

	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setFailCount(Integer failCount) {
		this.failCount = failCount;
	}

	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public String getEnableFlag() {
		return enableFlag;
	}

	public void setEnableFlag(String enableFlag) {
		this.enableFlag = enableFlag;
	}

	
	
}
