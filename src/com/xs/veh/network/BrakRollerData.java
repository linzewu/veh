package com.xs.veh.network;

import java.util.ArrayList;
import java.util.List;

public class BrakRollerData {
	
	public BrakRollerData(){
		
		leftData=new ArrayList<Integer>();
		rigthData = new ArrayList<Integer>();
	}
	
	
	//左阻滞力
	private Integer zzzl;
	
	//右阻滞力
	private Integer yzzl;
	
	//左制动力
	private Integer zzdl;
	
	//右制动力
	private Integer yzdl;
	
	//左最大力差点
	private Integer zzdlcd;
	
	//右最大力差点
	private Integer yzdlcd;
	
	//过程差
	private Float gcc;
	
	//左边过程数据
	private List<Integer> leftData;
	
	//右边过程数据
	private List<Integer> rigthData;
	
	//检测结束的状态 0 正常结束，1左轮抱死 2右轮抱死 3左右轮抱死
	private Integer jszt;
	
	
	public Integer getJszt() {
		return jszt;
	}

	public void setJszt(Integer jszt) {
		this.jszt = jszt;
	}

	/**
	 * 左阻滞力
	 * @return
	 */
	public Integer getZzzl() {
		return zzzl;
	}

	/**
	 * 右阻滞力
	 * @return
	 */
	public Integer getYzzl() {
		return yzzl;
	}

	/**
	 * 左制动力
	 * @return
	 */
	public Integer getZzdl() {
		return zzdl;
	}

	/**
	 * 右制动力
	 * @return
	 */
	public Integer getYzdl() {
		return yzdl;
	}

	/**
	 * 左最大力差点
	 * @return
	 */
	public Integer getZzdlcd() {
		return zzdlcd;
	}

	/**
	 * 右最大力差点
	 * @return
	 */
	public Integer getYzdlcd() {
		return yzdlcd;
	}

	/**
	 * 过程差
	 * @return
	 */
	public Float getGcc() {
		return gcc;
	}



	/**
	 * 左阻滞力
	 * @return
	 */
	public void setZzzl(Integer zzzl) {
		this.zzzl = zzzl;
	}

	/**
	 * 右阻滞力
	 * @param yzzl
	 */
	public void setYzzl(Integer yzzl) {
		this.yzzl = yzzl;
	}

	/**
	 * 左制动力
	 * @param zzdl
	 */
	public void setZzdl(Integer zzdl) {
		this.zzdl = zzdl;
	}

	/**
	 * 右制动力
	 * @param yzdl
	 */
	public void setYzdl(Integer yzdl) {
		this.yzdl = yzdl;
	}

	/**
	 * 左制动力差点
	 * @param zzdlcd
	 */
	public void setZzdlcd(Integer zzdlcd) {
		this.zzdlcd = zzdlcd;
	}

	/**
	 * 右制动力差点
	 * @param yzdlcd
	 */
	public void setYzdlcd(Integer yzdlcd) {
		this.yzdlcd = yzdlcd;
	}

	/**
	 * 过程差
	 * @param gcc
	 */
	public void setGcc(Float gcc) {
		this.gcc = gcc;
	}

	/**
	 * 左轮实时数据
	 * @return
	 */
	public List<Integer> getLeftData() {
		return leftData;
	}

	/**
	 * 右轮实时数据
	 * @return
	 */
	public List<Integer> getRigthData() {
		return rigthData;
	}
	
	/**
	 * 左轮实时数据
	 * @return
	 */
	public void setLeftData(List<Integer> leftData) {
		this.leftData = leftData;
	}

	/**
	 * 左轮实时数据
	 * @return
	 */
	public void setRigthData(List<Integer> rigthData) {
		this.rigthData = rigthData;
	}

	@Override
	public String toString() {
		return "BrakRollerData [zzzl=" + zzzl + ", yzzl=" + yzzl + ", zzdl=" + zzdl + ", yzdl=" + yzdl + ", zzdlcd="
				+ zzdlcd + ", yzdlcd=" + yzdlcd + ", gcc=" + gcc + ", leftData=" + leftData + ", rigthData=" + rigthData
				+ ", jszt=" + jszt + "]";
	}

	
	
	

}
