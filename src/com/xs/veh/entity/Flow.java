package com.xs.veh.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("flow")
@Entity
@Table(name = "TM_Flow")
public class Flow extends BaseEntity {
	
	/**
	 * 安检
	 */
	public final static Integer JCLX_AJ=0;
	
	/**
	 * 综检
	 */
	public final static Integer JCLX_ZJ=1;
	
	/**
	 * 环检
	 */
	public final static Integer JCLX_HJ=2;
	
	
	
	@Column
	private Integer jcxdh;
	
	@Column
	private Integer jclx;

	@Column
	private Integer gw1;
	
	@Column
	private Integer gw2;
	
	@Column
	private Integer gw3;
	
	@Column
	private Integer gw4;
	
	@Column
	private Integer gw5;
	
	@Column
	private Integer gw6;
	
	@Column
	private Integer gw7;
	
	@Column
	private Integer gw8;
	
	@Column
	private Integer gw9;
	
	@Column
	private Integer gw10;

	public Integer getJcxdh() {
		return jcxdh;
	}

	public Integer getJclx() {
		return jclx;
	}

	public Integer getGw1() {
		return gw1;
	}

	public Integer getGw2() {
		return gw2;
	}

	public Integer getGw3() {
		return gw3;
	}

	public Integer getGw4() {
		return gw4;
	}

	public Integer getGw5() {
		return gw5;
	}

	public Integer getGw6() {
		return gw6;
	}

	public Integer getGw7() {
		return gw7;
	}

	public Integer getGw8() {
		return gw8;
	}

	public Integer getGw9() {
		return gw9;
	}

	public Integer getGw10() {
		return gw10;
	}

	public void setJcxdh(Integer jcxdh) {
		this.jcxdh = jcxdh;
	}

	public void setJclx(Integer jclx) {
		this.jclx = jclx;
	}

	public void setGw1(Integer gw1) {
		this.gw1 = gw1;
	}

	public void setGw2(Integer gw2) {
		this.gw2 = gw2;
	}

	public void setGw3(Integer gw3) {
		this.gw3 = gw3;
	}

	public void setGw4(Integer gw4) {
		this.gw4 = gw4;
	}

	public void setGw5(Integer gw5) {
		this.gw5 = gw5;
	}

	public void setGw6(Integer gw6) {
		this.gw6 = gw6;
	}

	public void setGw7(Integer gw7) {
		this.gw7 = gw7;
	}

	public void setGw8(Integer gw8) {
		this.gw8 = gw8;
	}

	public void setGw9(Integer gw9) {
		this.gw9 = gw9;
	}

	public void setGw10(Integer gw10) {
		this.gw10 = gw10;
	}
	
	

}
