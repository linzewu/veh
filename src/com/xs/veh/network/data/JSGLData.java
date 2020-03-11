package com.xs.veh.network.data;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.xs.veh.entity.BaseEntity;

@Scope("prototype")
@Component("jsglData")
@Entity
@Table(name = "TM_JSGLData")
public class JSGLData extends BaseEntity {
	
	//项目名称
	private String xmmc;
	
	private Integer sj;
	
	//项目类型
	private Integer xmlx;
	
	//检测时间
	private Date jcsj;
	
	//是否合格
	private Integer sfhg;
	
	//寄生功率
	private Float jsgl;

	public String getXmmc() {
		return xmmc;
	}

	public void setXmmc(String xmmc) {
		this.xmmc = xmmc;
	}

	public Integer getSj() {
		return sj;
	}

	public void setSj(Integer sj) {
		this.sj = sj;
	}

	
	public Date getJcsj() {
		return jcsj;
	}

	public void setJcsj(Date jcsj) {
		this.jcsj = jcsj;
	}

	public Integer getSfhg() {
		return sfhg;
	}

	public void setSfhg(Integer sfhg) {
		this.sfhg = sfhg;
	}

	public Integer getXmlx() {
		return xmlx;
	}

	public void setXmlx(Integer xmlx) {
		this.xmlx = xmlx;
	}

	public Float getJsgl() {
		return jsgl;
	}

	public void setJsgl(Float jsgl) {
		this.jsgl = jsgl;
	}
	
	
	
	
	

}
