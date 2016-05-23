package com.xs.veh.network.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("weighData")
@Entity
@Table(name = "TM_WeighData")
public class WeighData extends BaseDeviceData {
	
	@Column
	//右轮数据
	private Integer rightData;
	
	@Column
	//左轮数据
	private Integer leftData;
	
	public Integer getRightData() {
		return rightData;
	}

	public Integer getLeftData() {
		return leftData;
	}

	public void setRightData(Integer rightData) {
		this.rightData = rightData;
	}

	public void setLeftData(Integer leftData) {
		this.leftData = leftData;
	}

}
