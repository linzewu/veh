package com.xs.veh.network.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
/**
 * 悬仪架
 * @author linzewu
 *
 */
@Scope("prototype")
@Component("suspensionData")
@Entity
@Table(name = "TM_SuspensionData")
public class SuspensionData extends BaseDeviceData {

	@Override
	public void setZpd() {
		// TODO Auto-generated method stub
	}
	//左静态
	@Column
	private String zjtlh;
    //右静态
	@Column
	private String yjtlh;
	//左吸收率
	@Column
	private String zxsl;
	//右吸收率
	@Column
	private String yxsl;
	//左右差
	@Column
	private String zyc;
	
	public String getZxsl() {
		return zxsl;
	}
	public String getYxsl() {
		return yxsl;
	}
	public void setZxsl(String zxsl) {
		this.zxsl = zxsl;
	}
	public void setYxsl(String yxsl) {
		this.yxsl = yxsl;
	}
	public String getZyc() {
		return zyc;
	}
	public void setZyc(String zyc) {
		this.zyc = zyc;
	}
	public String getZjtlh() {
		return zjtlh;
	}
	public void setZjtlh(String zjtlh) {
		this.zjtlh = zjtlh;
	}
	public String getYjtlh() {
		return yjtlh;
	}
	public void setYjtlh(String yjtlh) {
		this.yjtlh = yjtlh;
	}

}
