package com.xs.veh.network.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("speedData")
@Entity
@Table(name = "TM_SpeedData")
public class SpeedData extends BaseDeviceData {
	
	@Column
	private Float speed;
	
	@Column
	public Float getSpeed() {
		return speed;
	}

	public void setSpeed(Float speed) {
		this.speed = speed;
	}
	

}
