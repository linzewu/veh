package com.xs.veh.network.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("volumeData")
@Entity
@Table(name = "TM_VolumeData")
public class VolumeData  extends BaseDeviceData {
	
	@Column
	private String fb;
	
	
	public String getFb() {
		return fb;
	}


	public void setFb(String fb) {
		this.fb = fb;
	}


	@Override
	public void setZpd() {
		
	}
	
	
	
	
	
	
	


}
