package com.xs.veh.network.data;

import java.util.List;

public class SideslipData {
	
	private Float sideslip;
	
	private List<Float> datas;

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
