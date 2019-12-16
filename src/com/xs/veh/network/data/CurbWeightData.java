package com.xs.veh.network.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("curbWeightData")
@Entity
@Table(name = "TM_CurbWeightData")
public class CurbWeightData  extends BaseDeviceData {
	
	@Column
	private Integer zbzl;
	
	@Column
	private Integer zbzlpd;
	
	@Column
	private Integer qzzl;
	
	@Column
	private Integer hzzl;
	
	
	
	

	public Integer getQzzl() {
		return qzzl;
	}



	public void setQzzl(Integer qzzl) {
		this.qzzl = qzzl;
	}



	public Integer getHzzl() {
		return hzzl;
	}



	public void setHzzl(Integer hzzl) {
		this.hzzl = hzzl;
	}



	@Override
	public void setZpd() {
		this.setZpd(this.getZbzlpd());
	}

	

	public Integer getZbzl() {
		return zbzl;
	}



	public Integer getZbzlpd() {
		return zbzlpd;
	}



	public void setZbzl(Integer zbzl) {
		this.zbzl = zbzl;
		setZpd();
	}



	public void setZbzlpd(Integer zbzlpd) {
		this.zbzlpd = zbzlpd;
	}
	
	

}
