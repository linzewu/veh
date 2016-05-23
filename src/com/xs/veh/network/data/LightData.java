package com.xs.veh.network.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("lightData")
@Entity
@Table(name = "TM_LightData")
public class LightData extends BaseDeviceData {
	
	/**
	 * 光型 远
	 */
	public static final char GX_YGD='Y';
	
	/**
	 * 光型 近
	 */
	public static final char GX_JGD='J';
	
	/**
	 * 灯型 主灯
	 */
	public static final Integer DX_ZD=0;
	
	/**
	 * 灯型 副灯
	 */
	public static final Integer DX_FD=1;
	
	/**
	 * 位置 左
	 */
	public static final char WZ_Z='L';
	
	/**
	 * 位置 右
	 */
	public static final char WZ_Y='R';
	

	/**
	 * 水平偏差
	 */
	@Column(length=32)
	private String sppc;

	/**
	 * 垂直偏差
	 */
	@Column(length=32)
	private String czpc;

	/**
	 * 光强
	 */
	@Column(length=32)
	private String gq;

	/**
	 * 登高
	 */
	@Column(length=32)
	private String dg;
	
	@Column
	private char wz;

	@Column
	private char gx;

	@Column
	private Integer dx;
	
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
