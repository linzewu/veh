package com.xs.veh.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Scope("prototype")
@Component("TestResult")
@Entity
@Table(name = "TestResult",schema="QCPFWQ2018.dbo")
@JsonIgnoreProperties(value ={"hibernateLazyInitializer","handler","fieldHandler","password"})
public class TestResult extends BaseEntity {
	
	//JYLSH	检验流水号	字符串
	@Column
	private String jylsh;
	
	//HPZL	号牌种类	格式同公安部规定,如“01”
	@Column
	private String hpzl;
	
	//HPHM	号牌号码
	@Column
	private String hphm;
	
	//KSSJ	开始测试时间	yyyy-MM-dd HH:mm:ss
	@Column
	private String  kssj;
	
	//KSSJ	开始测试时间	yyyy-MM-dd HH:mm:ss
	@Column
	private String jssj;
	
	//HJWD	环境温度（℃）
	@Column
	private String hjwd;
	
	//HJSD	环境湿度（%）
	@Column
	private String hjsd;
	
	//DQY	大气压（kPa）
	@Column
	private String dqy;
	
	//DLX_DBGL	达标功率	(一级车：0.82\其他：0.75)倍额定功率或额定扭矩功率
	@Column
	private String dlx_dbgl;
	
	//DLX_EDCS	额定车速
	@Column
	private String dlx_edcs;
	
	//DLX_JZL	加载力
	@Column
	private String dlx_jzl;
	
	//DLX_WDCS	稳定车速
	@Column
	private String dlx_wdcs;
	
	//DLX_PD	动力性判定	合格\不合格
	@Column
	private String dlx_pd;
	
	//CSB_SCZ	速度表实测值
	@Column
	private String csb_scz;
	
	//CSB_PD	车速表判定	合格\不合格
	@Column
	private String csb_pd;
	
	//YH_BZXZ	油耗标准限值
	@Column
	private String yh_bzxz;
	
	//YH_SCZ	油耗实测值
	@Column
	private String yh_scz;
	
	//YH_PD	油耗判定
	@Column
	private String YH_PD;
	
	//功率比值系数	一级：0.82\其他：0.75
	@Column
	private String dlx_bzxs;
	
	//Fe或Fm
	@Column
	private String dlx_fem;
	
	//Ftc		整数
	@Column
	private String dlx_ftc;
	
	//Fc		整数
	@Column
	private String dlx_fc;
	
	//Ff		整数
	@Column
	private String dlx_ff;
	
	//DLX_XZXS	功率修正系数ad或者aa		整数
	@Column
	private String dlx_xzxs;
	
	//DLX_SDQX	动力性速度曲线
	@Column(length=10000)
	private String dlx_sdqx;
	
	//DLX_JZLQX	动力性加载力曲线
	@Column(length=10000)
	private String dlx_jzlqx;
	
	//YH_SDQX	油耗速度曲线
	@Column(length=10000)
	private String yh_sdqx;
	
	//YH_JZLQX	油耗加载力曲线
	@Column(length=10000)
	private String yh_jzlqx;
	
	//YH_YHQX	油耗实时曲线
	@Column(length=10000)
	private String yh_yhqx;

	public String getJylsh() {
		return jylsh;
	}

	public void setJylsh(String jylsh) {
		this.jylsh = jylsh;
	}

	public String getHpzl() {
		return hpzl;
	}

	public void setHpzl(String hpzl) {
		this.hpzl = hpzl;
	}

	public String getHphm() {
		return hphm;
	}

	public void setHphm(String hphm) {
		this.hphm = hphm;
	}

	public String getKssj() {
		return kssj;
	}

	public void setKssj(String kssj) {
		this.kssj = kssj;
	}

	public String getJssj() {
		return jssj;
	}

	public void setJssj(String jssj) {
		this.jssj = jssj;
	}

	public String getHjwd() {
		return hjwd;
	}

	public void setHjwd(String hjwd) {
		this.hjwd = hjwd;
	}

	public String getHjsd() {
		return hjsd;
	}

	public void setHjsd(String hjsd) {
		this.hjsd = hjsd;
	}

	public String getDqy() {
		return dqy;
	}

	public void setDqy(String dqy) {
		this.dqy = dqy;
	}

	public String getDlx_dbgl() {
		return dlx_dbgl;
	}

	public void setDlx_dbgl(String dlx_dbgl) {
		this.dlx_dbgl = dlx_dbgl;
	}

	public String getDlx_edcs() {
		return dlx_edcs;
	}

	public void setDlx_edcs(String dlx_edcs) {
		this.dlx_edcs = dlx_edcs;
	}

	public String getDlx_jzl() {
		return dlx_jzl;
	}

	public void setDlx_jzl(String dlx_jzl) {
		this.dlx_jzl = dlx_jzl;
	}

	public String getDlx_wdcs() {
		return dlx_wdcs;
	}

	public void setDlx_wdcs(String dlx_wdcs) {
		this.dlx_wdcs = dlx_wdcs;
	}

	public String getDlx_pd() {
		return dlx_pd;
	}

	public void setDlx_pd(String dlx_pd) {
		this.dlx_pd = dlx_pd;
	}

	public String getCsb_scz() {
		return csb_scz;
	}

	public void setCsb_scz(String csb_scz) {
		this.csb_scz = csb_scz;
	}

	public String getCsb_pd() {
		return csb_pd;
	}

	public void setCsb_pd(String csb_pd) {
		this.csb_pd = csb_pd;
	}

	public String getYh_bzxz() {
		return yh_bzxz;
	}

	public void setYh_bzxz(String yh_bzxz) {
		this.yh_bzxz = yh_bzxz;
	}

	public String getYh_scz() {
		return yh_scz;
	}

	public void setYh_scz(String yh_scz) {
		this.yh_scz = yh_scz;
	}

	public String getYH_PD() {
		return YH_PD;
	}

	public void setYH_PD(String yH_PD) {
		YH_PD = yH_PD;
	}

	public String getDlx_bzxs() {
		return dlx_bzxs;
	}

	public void setDlx_bzxs(String dlx_bzxs) {
		this.dlx_bzxs = dlx_bzxs;
	}

	public String getDlx_fem() {
		return dlx_fem;
	}

	public void setDlx_fem(String dlx_fem) {
		this.dlx_fem = dlx_fem;
	}

	public String getDlx_ftc() {
		return dlx_ftc;
	}

	public void setDlx_ftc(String dlx_ftc) {
		this.dlx_ftc = dlx_ftc;
	}

	public String getDlx_fc() {
		return dlx_fc;
	}

	public void setDlx_fc(String dlx_fc) {
		this.dlx_fc = dlx_fc;
	}

	public String getDlx_ff() {
		return dlx_ff;
	}

	public void setDlx_ff(String dlx_ff) {
		this.dlx_ff = dlx_ff;
	}

	public String getDlx_xzxs() {
		return dlx_xzxs;
	}

	public void setDlx_xzxs(String dlx_xzxs) {
		this.dlx_xzxs = dlx_xzxs;
	}

	public String getDlx_sdqx() {
		return dlx_sdqx;
	}

	public void setDlx_sdqx(String dlx_sdqx) {
		this.dlx_sdqx = dlx_sdqx;
	}

	public String getDlx_jzlqx() {
		return dlx_jzlqx;
	}

	public void setDlx_jzlqx(String dlx_jzlqx) {
		this.dlx_jzlqx = dlx_jzlqx;
	}

	public String getYh_sdqx() {
		return yh_sdqx;
	}

	public void setYh_sdqx(String yh_sdqx) {
		this.yh_sdqx = yh_sdqx;
	}

	public String getYh_jzlqx() {
		return yh_jzlqx;
	}

	public void setYh_jzlqx(String yh_jzlqx) {
		this.yh_jzlqx = yh_jzlqx;
	}

	public String getYh_yhqx() {
		return yh_yhqx;
	}

	public void setYh_yhqx(String yh_yhqx) {
		this.yh_yhqx = yh_yhqx;
	}

	

}
