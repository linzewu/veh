package com.xs.veh.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Scope("prototype")
@Component("HBCalibrationWeight")
@Entity
@Table(name = "TM_HBCalibrationWeight")
@JsonIgnoreProperties(value ={"hibernateLazyInitializer","handler","fieldHandler","password"})
public class HBCalibrationWeight extends BaseEntity {
	
	@Column
	private Integer lllz;
	
	@Column
	private Integer sjlz;
	
	@Column
	private Integer jdwc;
	
	@Column
	private Float xdwc;
	
	@Column
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss", timezone = "GMT+8")
	private Date checkDate;
	
	@Column
	private Long group;
	
	
	
	

	public Long getGroup() {
		return group;
	}

	public void setGroup(Long group) {
		this.group = group;
	}

	public Date getCheckDate() {
		return checkDate;
	}

	public void setCheckDate(Date checkDate) {
		this.checkDate = checkDate;
	}

	public Integer getLllz() {
		return lllz;
	}

	public void setLllz(Integer lllz) {
		this.lllz = lllz;
	}

	public Integer getSjlz() {
		return sjlz;
	}

	public void setSjlz(Integer sjlz) {
		this.sjlz = sjlz;
	}

	public Integer getJdwc() {
		return jdwc;
	}

	public void setJdwc(Integer jdwc) {
		this.jdwc = jdwc;
	}

	public Float getXdwc() {
		return xdwc;
	}

	public void setXdwc(Float xdwc) {
		this.xdwc = xdwc;
	}
	
	

}
