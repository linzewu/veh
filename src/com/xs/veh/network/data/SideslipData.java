package com.xs.veh.network.data;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("sideslipData")
@Entity
@Table(name = "TM_SideslipData")
public class SideslipData extends BaseDeviceData {
	
	@Column
	private Float sideslip;
	
	@Transient
	private List<Float> datas;
	
	@Column(length=4000)
	private String strData;
	
	public String getStrData() {
		return strData;
	}

	public void setStrData(String strData) {
		this.strData = strData;
	}

	public Float getSideslip() {
		return sideslip;
	}

	public List<Float> getDatas() {
		return datas;
	}

	public void setSideslip(Float sideslip) {
		this.sideslip = sideslip;
	}

	public void setDatas(List<Float> datas) {
		this.datas = datas;
	}

	@Override
	public String toString() {
		return "SideslipData [sideslip=" + sideslip + ", datas=" + datas + "]";
	}
	
	

}
