package com.xs.veh.entity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("systemInfo")
public class SystemInfo {
	
	@Value("${jkxlh}")
	private String jkxlh;
	
	@Value("${jyjgbh}")
	private String jyjgbh;
	
	@Value("${ip}")
	private String jgxtip;
	
	@Value("${port}")
	private String jgxtdk;
	
	@Value("${db.url}")
	private String dbInfo;
	
	

	public String getDbInfo() {
		return dbInfo;
	}

	public void setDbInfo(String dbInfo) {
		this.dbInfo = dbInfo;
	}

	public String getJkxlh() {
		return jkxlh;
	}

	public String getJyjgbh() {
		return jyjgbh;
	}

	public String getJgxtip() {
		return jgxtip;
	}

	public String getJgxtdk() {
		return jgxtdk;
	}


	public void setJkxlh(String jkxlh) {
		this.jkxlh = jkxlh;
	}

	public void setJyjgbh(String jyjgbh) {
		this.jyjgbh = jyjgbh;
	}

	public void setJgxtip(String jgxtip) {
		this.jgxtip = jgxtip;
	}

	public void setJgxtdk(String jgxtdk) {
		this.jgxtdk = jgxtdk;
	}

}
