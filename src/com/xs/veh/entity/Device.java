package com.xs.veh.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Scope("prototype")
@Component("device")
@Entity
@Table(name = "TM_Device")
@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler", "fieldHandler", "decode" })
public class Device extends BaseEntity {

	/**
	 * 制动设备
	 */
	public final static int ZDJCSB = 1;

	/**
	 * 灯光设备
	 */
	public final static int DGJCSB = 2;

	/**
	 * 速度设备
	 */
	public final static int SDJCSB = 3;

	/**
	 * 侧滑设备
	 */
	public final static int CHJCSB = 4;

	/**
	 * 称重设备
	 */
	public final static int CZJCSB = 5;
	
	/**
	 * 平板设备
	 */
	public final static int ZDPBSB = 6;
	
	/**
	 * 悬架设备
	 */
	public final static int XJSB = 7;
	

	/**
	 * 测功机
	 */
	public final static int CGJ = 8;
	
	
	/**
	 * 多轴称重台
	 */
	public final static int DZCZT = 9;
	
	/**
	 * 声级计
	 */
	public final static int SJJ = 10;
	
	/**
	 * 转速计
	 */
	public final static int ZSJ = 11;
	
	/**
	 * 烟度计
	 */
	public final static int YDJ = 12;
	
	/**
	 * 尾气分析仪
	 */
	public final static int WQFXY = 13;
	
	/**
	 * 柴油NO分析仪
	 */
	public final static int CYFXY = 14;
	
	public final static int GDKG = 90;

	public final static int XSP = 91;

	public static final String KEY = "device";

	@Column
	private Integer type;

	@Column(length = 20)
	private String com;

	@Column
	private Integer rate;

	@Column
	private Integer databits;

	@Column
	private Integer stopbits;

	@Column
	private Integer parity;

	@Column
	private Integer jcxxh;

	@Column
	private Integer gw;

	@Column
	private String sbcs;

	@Column
	private String sbxh;

	@Column(length = 8000)
	private String qtxx;
	
	@Column
	private String name;

	@Column
	private String deviceDecode;
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDeviceDecode() {
		return deviceDecode;
	}

	public void setDeviceDecode(String deviceDecode) {
		this.deviceDecode = deviceDecode;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getCom() {
		return com;
	}

	public Integer getRate() {
		return rate;
	}

	public Integer getJcxxh() {
		return jcxxh;
	}

	public Integer getGw() {
		return gw;
	}

	public String getSbcs() {
		return sbcs;
	}

	public String getSbxh() {
		return sbxh;
	}

	public String getQtxx() {
		return qtxx;
	}

	public void setCom(String com) {
		this.com = com;
	}

	public void setRate(Integer rate) {
		this.rate = rate;
	}

	public void setJcxxh(Integer jcxxh) {
		this.jcxxh = jcxxh;
	}

	public void setGw(Integer gw) {
		this.gw = gw;
	}

	public void setSbcs(String sbcs) {
		this.sbcs = sbcs;
	}

	public void setSbxh(String sbxh) {
		this.sbxh = sbxh;
	}

	public void setQtxx(String qtxx) {
		this.qtxx = qtxx;
	}

	public Integer getDatabits() {
		return databits;
	}

	public Integer getStopbits() {
		return stopbits;
	}

	public Integer getParity() {
		return parity;
	}

	public void setDatabits(Integer databits) {
		this.databits = databits;
	}

	public void setStopbits(Integer stopbits) {
		this.stopbits = stopbits;
	}

	public void setParity(Integer parity) {
		this.parity = parity;
	}

	public String getThredKey() {
		return this.getId() + "_" + KEY;
	}
	
	
	public String getDeviceSpringName() {
		String deviceName=null;
		switch (type) {
		case Device.CGJ:
			deviceName="deviceDyno";
			break;
		case Device.CHJCSB:
			deviceName="deviceSideslip";
			break;
		case Device.CYFXY:
			deviceName="deviceEGANO";
			break;
		case Device.CZJCSB:
			deviceName="deviceWeigh";
			break;
		case Device.DGJCSB:
			deviceName="deviceLight";
			break;
		case Device.DZCZT:
			deviceName="deviceManyWeigh";
			break;
		case Device.GDKG:
			deviceName="deviceSignal";
			break;
		case Device.SDJCSB:
			deviceName="deviceSpeed";
			break;
		case Device.SJJ:
			deviceName="deviceVolume";
			break;
		case Device.WQFXY:
			deviceName="deviceEGA";
			break;
		case Device.XJSB:
			deviceName="deviceSuspension";
			break;
		case Device.XSP:
			deviceName="deviceSuspension";
			break;
		case Device.YDJ:
			deviceName="deviceSmokemeter";
			break;
		case Device.ZDJCSB:
			deviceName="deviceBrakRoller";
			break;
		case Device.ZDPBSB:
			deviceName="deviceBrakePad";
			break;
		case Device.ZSJ:
			deviceName="deviceTachometer";
			break;
		default:
			break;
		}
		return deviceName;
	}

}
