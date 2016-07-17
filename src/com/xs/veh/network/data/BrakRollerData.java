package com.xs.veh.network.data;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.manager.CheckDataManager;

@Scope("prototype")
@Component("brakRollerData")
@Entity
@Table(name = "TM_BrakRollerData")
public class BrakRollerData extends BaseDeviceData {
	
	public static final Integer JSZT_ZCJS=0;
	public static final Integer JSZT_ZLBS=1;
	public static final Integer JSZT_YLBS=2;
	public static final Integer JSZT_ZYLBS=3;
	
	/**
	 * 是加载轴
	 */
	public static final Integer SFJZZ_YES=1;
	
	/**
	 * 不是加载轴
	 */
	public static final Integer SFJZZ_NO=0;

	public BrakRollerData() {
		leftData = new ArrayList<Integer>();
		rigthData = new ArrayList<Integer>();
	}

	// 左阻滞力
	@Column
	private Integer zzzl;

	// 右阻滞力
	@Column
	private Integer yzzl;

	// 左制动力
	@Column
	private Integer zzdl;

	// 右制动力
	@Column
	private Integer yzdl;

	// 左最大力差点
	@Column
	private Integer zzdlcd;

	// 右最大力差点
	@Column
	private Integer yzdlcd;

	// 过程差
	@Column
	private Float gcc;

	// 左边过程数据
	@Transient
	private List<Integer> leftData;

	// 右边过程数据
	@Transient
	private List<Integer> rigthData;

	//检测结束的状态 0 正常结束，1左轮抱死 2右轮抱死 3左右轮抱死
	@Column
	private Integer jszt;

	@Column
	private Integer zw;

	// 空载行车制动率
	@Column
	private Float kzxczdl;

	// 空载不平衡率
	@Column
	private Float kzbphl;

	// 加载制动率
	@Column
	private Integer jzzzdl;

	// 加载轴荷
	@Column
	private Integer jzzh;

	// 加载不平衡率
	@Column
	private Integer jzbphl;

	// 空载制动率限值
	@Column
	private Integer kzzdlxz;

	// 空载制动率判定
	@Column
	private Integer kzzdlpd;

	// 加载制动率限值
	@Column
	private Integer jzzdlxz;

	// 加载制动率判定
	@Column
	private Integer jzzdlpd;

	// 不平衡率限值
	@Column
	private Float bphlxz;

	// 空载不平衡率判定
	@Column
	private Integer kzbphlpd;

	// 加载不平衡率判定
	@Column
	private Integer jzbphlpd;
	
	//是否加载轴
	@Column
	private Integer sfjzz;
	

	public Integer getSfjzz() {
		return sfjzz;
	}

	public void setSfjzz(Integer sfjzz) {
		this.sfjzz = sfjzz;
	}

	public Integer getKzbphlpd() {
		return kzbphlpd;
	}

	public Integer getJzbphlpd() {
		return jzbphlpd;
	}

	public void setKzbphlpd(Integer kzbphlpd) {
		this.kzbphlpd = kzbphlpd;
	}

	public void setJzbphlpd(Integer jzbphlpd) {
		this.jzbphlpd = jzbphlpd;
	}

	public Integer getJzzdlxz() {
		return jzzdlxz;
	}

	public Integer getJzzdlpd() {
		return jzzdlpd;
	}

	public Float getBphlxz() {
		return bphlxz;
	}

	public void setJzzdlxz(Integer jzzdlxz) {
		this.jzzdlxz = jzzdlxz;
	}

	public void setJzzdlpd(Integer jzzdlpd) {
		this.jzzdlpd = jzzdlpd;
	}

	public void setBphlxz(Float bphlxz) {
		this.bphlxz = bphlxz;
	}

	public Integer getKzzdlxz() {
		return kzzdlxz;
	}

	public Integer getKzzdlpd() {
		return kzzdlpd;
	}

	public void setKzzdlxz(Integer kzzdlxz) {
		this.kzzdlxz = kzzdlxz;
	}

	public void setKzzdlpd(Integer kzzdlpd) {
		this.kzzdlpd = kzzdlpd;
	}

	/**
	 * 加载轴制动率
	 * 
	 * @return
	 */
	public Integer getJzzzdl() {
		return jzzzdl;
	}

	/**
	 * 加载轴制动率
	 * 
	 * @param jzzzdl
	 */
	public void setJzzzdl(Integer jzzzdl) {
		this.jzzzdl = jzzzdl;
	}

	/**
	 * 空载行车制动率
	 * 
	 * @return
	 */
	public Float getKzxczdl() {
		return kzxczdl;
	}

	/**
	 * 空载不平衡率
	 * 
	 * @return
	 */
	public Float getKzbphl() {
		return kzbphl;
	}

	/**
	 * 加载轴荷
	 * 
	 * @return
	 */
	public Integer getJzzh() {
		return jzzh;
	}

	/**
	 * 加载不平衡率
	 * 
	 * @return
	 */
	public Integer getJzbphl() {
		return jzbphl;
	}

	/**
	 * 空载行车制动力
	 * 
	 * @param kzxczdl
	 */
	public void setKzxczdl(Float kzxczdl) {
		this.kzxczdl = kzxczdl;
	}

	/**
	 * 空载不平衡率
	 * 
	 * @param kzbphl
	 */
	public void setKzbphl(Float kzbphl) {
		this.kzbphl = kzbphl;
	}

	/**
	 * 加载轴荷
	 * 
	 * @param jzzh
	 */
	public void setJzzh(Integer jzzh) {
		this.jzzh = jzzh;
	}

	/**
	 * 加载不平衡率
	 * 
	 * @param jzbphl
	 */
	public void setJzbphl(Integer jzbphl) {
		this.jzbphl = jzbphl;
	}

	/**
	 * 轴位
	 * 
	 * @return
	 */
	public Integer getZw() {
		return zw;
	}

	public void setZw(Integer zw) {
		this.zw = zw;
	}

	@Column(length = 4000)
	private String leftDataStr;

	@Column(length = 4000)
	private String rigthDataStr;

	public Integer getJszt() {
		return jszt;
	}

	public void setJszt(Integer jszt) {
		this.jszt = jszt;
	}

	/**
	 * 左阻滞力
	 * 
	 * @return
	 */
	public Integer getZzzl() {
		return zzzl;
	}

	/**
	 * 右阻滞力
	 * 
	 * @return
	 */
	public Integer getYzzl() {
		return yzzl;
	}

	/**
	 * 左制动力
	 * 
	 * @return
	 */
	public Integer getZzdl() {
		return zzdl;
	}

	/**
	 * 右制动力
	 * 
	 * @return
	 */
	public Integer getYzdl() {
		return yzdl;
	}

	/**
	 * 左最大力差点
	 * 
	 * @return
	 */
	public Integer getZzdlcd() {
		return zzdlcd;
	}

	/**
	 * 右最大力差点
	 * 
	 * @return
	 */
	public Integer getYzdlcd() {
		return yzdlcd;
	}

	/**
	 * 过程差
	 * 
	 * @return
	 */
	public Float getGcc() {
		return gcc;
	}

	/**
	 * 左阻滞力
	 * 
	 * @return
	 */
	public void setZzzl(Integer zzzl) {
		this.zzzl = zzzl;
	}

	/**
	 * 右阻滞力
	 * 
	 * @param yzzl
	 */
	public void setYzzl(Integer yzzl) {
		this.yzzl = yzzl;
	}

	/**
	 * 左制动力
	 * 
	 * @param zzdl
	 */
	public void setZzdl(Integer zzdl) {
		this.zzdl = zzdl;
	}

	/**
	 * 右制动力
	 * 
	 * @param yzdl
	 */
	public void setYzdl(Integer yzdl) {
		this.yzdl = yzdl;
	}

	/**
	 * 左制动力差点
	 * 
	 * @param zzdlcd
	 */
	public void setZzdlcd(Integer zzdlcd) {
		this.zzdlcd = zzdlcd;
	}

	/**
	 * 右制动力差点
	 * 
	 * @param yzdlcd
	 */
	public void setYzdlcd(Integer yzdlcd) {
		this.yzdlcd = yzdlcd;
	}

	/**
	 * 过程差
	 * 
	 * @param gcc
	 */
	public void setGcc(Float gcc) {
		this.gcc = gcc;
	}

	/**
	 * 左轮实时数据
	 * 
	 * @return
	 */
	public List<Integer> getLeftData() {
		return leftData;
	}

	/**
	 * 右轮实时数据
	 * 
	 * @return
	 */
	public List<Integer> getRigthData() {
		return rigthData;
	}

	/**
	 * 左轮实时数据
	 * 
	 * @return
	 */
	public void setLeftData(List<Integer> leftData) {
		this.leftData = leftData;
	}

	/**
	 * 左轮实时数据
	 * 
	 * @return
	 */
	public void setRigthData(List<Integer> rigthData) {
		this.rigthData = rigthData;
	}

	public String getLeftDataStr() {
		return leftDataStr;
	}

	public String getRigthDataStr() {
		return rigthDataStr;
	}

	public void setLeftDataStr(String leftDataStr) {
		this.leftDataStr = leftDataStr;
	}

	public void setRigthDataStr(String rigthDataStr) {
		this.rigthDataStr = rigthDataStr;
	}

	@Override
	public String toString() {
		return "BrakRollerData [zzzl=" + zzzl + ", yzzl=" + yzzl + ", zzdl=" + zzdl + ", yzdl=" + yzdl + ", zzdlcd="
				+ zzdlcd + ", yzdlcd=" + yzdlcd + ", gcc=" + gcc + ", leftData=" + leftData + ", rigthData=" + rigthData
				+ ", jszt=" + jszt + ", leftDataStr=" + leftDataStr + ", rigthDataStr=" + rigthDataStr + "]";
	}

	/**
	 * 空载限制标准
	 * 
	 * @param vehCheckLogin
	 */
	public void setKzzdlxz(VehCheckLogin vehCheckLogin) {

		String cllx = vehCheckLogin.getCllx();
		Integer zbzl = vehCheckLogin.getZbzl();

		if (cllx.indexOf("N") == 0 && zw > 1) {
			this.kzzdlxz = 60;
			return;
		}

		if (zw == 1 && (cllx.indexOf("K") == 0 || zbzl < 3500)) {
			this.kzzdlxz = 60;
			return;
		}

		if (zw > 1 && (cllx.indexOf("K") == 0 || zbzl < 3500)) {
			this.kzzdlxz = 20;
			return;
		}

		// 普通摩托车
		if (zw == 1 && (cllx.indexOf("M11") == 0 || cllx.indexOf("M21") == 0)) {
			this.kzzdlxz = 60;
			return;
		}
		// 普通摩托车
		if (zw > 1 && (cllx.indexOf("M11") == 0 || cllx.indexOf("M21") == 0)) {
			this.kzzdlxz = 55;
			return;
		}

		// 轻便摩托车
		if (zw == 1 && (cllx.indexOf("M12") == 0 || cllx.indexOf("M22") == 0)) {
			this.kzzdlxz = 60;
			return;
		}

		// 轻便摩托车
		if (zw > 1 && (cllx.indexOf("M12") == 0 || cllx.indexOf("M22") == 0)) {
			this.kzzdlxz = 50;
			return;
		}

		// 其他汽车
		if (zw == 1 && zbzl >= 3500) {
			this.kzzdlxz = 60;
			return;
		}

		// 其他汽车
		if (zw == 1 && zbzl >= 3500) {
			this.kzzdlxz = 50;
			return;
		}
	}

	/**
	 * 加载限制标准
	 * 
	 * @param vehCheckLogin
	 */
	public void setJzzdlxz(VehCheckLogin vehCheckLogin) {

		String cllx = vehCheckLogin.getCllx();
		Integer zbzl = vehCheckLogin.getZbzl();
		Integer zs = vehCheckLogin.getZs();

		// 三轴以上车辆需要加载
		if (zs < 3) {
			return;
		}

		// 客车及3.5T以下车辆 前轴
		if (zw == 1 && (cllx.indexOf("K") == 0 || zbzl < 3500)) {
			this.jzzdlxz = 60;
			return;
		}
		// 客车及3.5T以下车辆 后轴
		if (zw > 1 && (cllx.indexOf("K") == 0 || zbzl < 3500)) {
			this.jzzdlxz = 20;
			return;
		}

		// 其他汽车
		if (zw == 1 && zbzl >= 3500) {
			this.jzzdlxz = 60;
			return;
		}

		// 其他汽车
		if (zw == 1 && zbzl >= 3500) {
			this.kzzdlxz = 50;
			return;
		}
	}

	public void setBphlxz(VehCheckLogin vehCheckLogin, WeighData weighData) {

		String jylb = vehCheckLogin.getJylb();
		// 轴荷
		Integer zh = weighData.getRightData() + weighData.getLeftData();

		Integer zdl = this.zzdl + this.yzdl;

		Float temp = (float) (zh * 0.98 * 0.6);

		// 注册登记
		if (jylb.equals("00")) {
			if (zw == 1) {
				this.bphlxz = 20f;
			} else {
				if (zdl >= temp) {
					this.bphlxz = 24f;
				} else {
					this.bphlxz = 8f;
				}
			}
		} else {
			if (zw == 1) {
				this.bphlxz = 24f;
			} else {
				if (zdl >= temp) {
					this.bphlxz = 30f;
				} else {
					this.bphlxz = 10f;
				}
			}
		}
	}

	public void setKzzdlpd() {
		if (this.kzzdlxz == null || this.kzxczdl == null) {
			return;
		}
		if (kzxczdl >= this.kzzdlxz) {
			this.kzzdlpd = CheckDataManager.PDJG_HG;
		} else {
			this.kzzdlpd = CheckDataManager.PDJG_BHG;
		}
	}

	/**
	 * 加载制动率判定
	 */
	public void setJzzdlpd() {
		if (this.jzzdlxz == null || this.jzzzdl == null) {
			return;
		}
		if (jzzzdl >= this.jzzdlxz) {
			this.jzzdlpd = CheckDataManager.PDJG_HG;
		} else {
			this.jzzdlpd = CheckDataManager.PDJG_BHG;
		}
	}

	/**
	 * 设置加载不平衡率判定
	 */
	public void setJzbphlpd() {
		if (this.jzbphl == null || this.bphlxz == null) {
			return;
		}
		if (this.jzbphl <= this.bphlxz) {
			this.jzbphlpd = CheckDataManager.PDJG_HG;
		} else {
			this.jzbphlpd = CheckDataManager.PDJG_BHG;
		}
	}

	/**
	 * 设置空载不平衡率判定
	 */
	public void setKzbphlpd() {

		if (this.kzbphl == null || this.bphlxz == null) {
			return;
		}

		if (this.kzbphl <= this.bphlxz) {
			this.kzbphlpd = CheckDataManager.PDJG_HG;
		} else {
			this.kzbphlpd = CheckDataManager.PDJG_BHG;
		}
	}

	/**
	 * 空载 设置不平衡率
	 * 
	 * @return
	 */
	public void setKzbphl() {

		if (yzdl == null || zzdl == null || gcc == null) {
			return;
		}
		Integer zdzdl = zzdl > yzdl ? zzdl : yzdl;
		Float bphl = (float) (Math.abs(gcc) * 1.0 / zdzdl * 1.0) * 100;

		this.kzbphl = CheckDataManager.MathRound(bphl);

	}

	/**
	 * 设置空载行车制动率
	 */
	public void setKzxczdl(WeighData weighData) {

		if (weighData == null) {
			return;
		}
		Integer zh = weighData.getRightData() + weighData.getLeftData();
		Integer zdl = this.getZzdl() + this.getYzdl();
		Float xczdl = (float) (zdl * 1.0 / zh * 0.98) * 100;
		this.kzxczdl = CheckDataManager.MathRound(xczdl);
	}

	@Override
	public void setZpd() {
		if (this.kzbphlpd == PDJG_BHG || this.kzzdlpd == PDJG_BHG || this.jzbphlpd == PDJG_BHG
				|| this.jzzdlpd == PDJG_BHG) {
			
			this.setZpd(PDJG_BHG);
		}else{
			this.setZpd(PDJG_HG);
		}
	}
	
	

}
