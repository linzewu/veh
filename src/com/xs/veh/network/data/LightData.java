package com.xs.veh.network.data;

public class LightData {
	
	public static final char GX_Y='Y';
	
	public static final char GX_J='J';
	
	public static final Integer DX_ZD=0;
	
	public static final Integer DX_FD=1;
	

	/**
	 * 水平偏差
	 */
	private String sppc;

	/**
	 * 垂直偏差
	 */
	private String czpc;

	/**
	 * 光强
	 */
	private String gq;

	/**
	 * 登高
	 */
	private String dg;
	

	private char wz;

	private char gx;

	private Integer dx;
	
	private Integer state;

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "LightData [sppc=" + sppc + ", czpc=" + czpc + ", gq=" + gq + ", dg=" + dg + ", wz=" + wz + ", gx=" + gx
				+ ", dx=" + dx + "]";
	}

	public Integer getDx() {
		return dx;
	}

	public void setDx(Integer dx) {
		this.dx = dx;
	}

	public char getWz() {
		return wz;
	}

	public char getGx() {
		return gx;
	}

	public String getSppc() {
		return sppc;
	}

	public String getCzpc() {
		return czpc;
	}

	public String getGq() {
		return gq;
	}

	public String getDg() {
		return dg;
	}

	public void setWz(char wz) {
		this.wz = wz;
	}

	public void setGx(char gx) {
		this.gx = gx;
	}

	public void setSppc(String sppc) {
		this.sppc = sppc;
	}

	public void setCzpc(String czpc) {
		this.czpc = czpc;
	}

	public void setGq(String gq) {
		this.gq = gq;
	}

	public void setDg(String dg) {
		this.dg = dg;
	}

}
