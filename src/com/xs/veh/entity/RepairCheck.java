package com.xs.veh.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
//二级维护竣工检验记录单
@Scope("prototype")
@Component("repairCheck")
@Entity
@Table(name = "TM_RepairCheck")
public class RepairCheck extends BaseEntity{
	@Column(length = 30)
	private String jylsh;
	
	//托修方
	@Column(length = 90)
	private String txf;
	
	//合同编号
	@Column(length = 30)
	private String htbh;
	
	//号牌号码
	@Column(length = 15)
	private String hphm;
	//车辆型号
	@Column(length = 32)
	private String clxh;
	
	/** 清洁 */
	@Column(length = 5)
	private String wg1;
	
	/** 紧固 */
	@Column(length = 5)
	private String wg2;
	
	/** 润滑 */
	@Column(length = 5)
	private String wg3;
	
	/** 密封 */
	@Column(length = 5)
	private String wg4;
	
	/** 附属设施 */
	@Column(length = 5)
	private String wg5;
	
	/** 发动机工作状况 */
	@Column(length = 5)
	private String wg6;
	
	/** 发动机装备 */
	@Column(length = 5)
	private String wg7;
	
	/** 转向机构 */
	@Column(length = 5)
	private String wg8;
	
	/** 轮胎 */
	@Column(length = 5)
	private String wg9;
	
	/** 悬架 */
	@Column(length = 5)
	private String wg10;
	
	/** 减震器 */
	@Column(length = 5)
	private String wg11;
	
	/** 车桥 */
	@Column(length = 5)
	private String wg12;
	
	/** 离合器 */
	@Column(length = 5)
	private String wg13;
	
	/** 变速器，传动轴，主减速器 */
	@Column(length = 5)
	private String wg14;
	
	/** 牵引连接装置和锁止机构 */
	@Column(length = 5)
	private String wg15;
	
	/** 前照灯 */
	@Column(length = 5)
	private String wg16;
	
	/** 信号指示装置 */
	@Column(length = 5)
	private String wg17;
	
	/** 仪表 */
	@Column(length = 5)
	private String wg18;
	
	/** 是否有故障 */
	@Column(length = 5)
	private String gz1;
	
	/** 故障描述 */
	@Column(length = 600)
	private String gz2;
	
	/** 故障评价 */
	@Column(length = 5)
	private String gz3;
	
	/** 排气污染 怠速CO/% */
	@Column(length = 10)
	private String wr1;
	
	/** 怠速 HC/*10 */
	@Column(length = 10)
	private String wr2;
	
	/** 怠速 评价*/
	@Column(length = 5)
	private String wr3;
	
	/** 排气污染 高怠速CO/% */
	@Column(length = 10)
	private String wr4;
	
	/** 高怠速 HC/*10 */
	@Column(length = 10)
	private String wr5;
	
	/** 高怠速 评价*/
	@Column(length = 5)
	private String wr6;
	
	/** 光吸收系数1 */
	@Column(length = 10)
	private String wr7;
	
	/** 光吸收系数2 */
	@Column(length = 10)
	private String wr8;
	
	/** 光吸收系数3 */
	@Column(length = 10)
	private String wr9;
	
	/** 平均/m */
	@Column(length = 10)
	private String wr10;
	
	/** 光吸收系数 评价 */
	@Column(length = 5)
	private String wr11;
	
	/** 烟度值/BSU 1 */
	@Column(length = 10)
	private String wr12;
	
	/** 烟度值/BSU 2 */
	@Column(length = 10)
	private String wr13;
	
	/** 烟度值/BSU 3 */
	@Column(length = 10)
	private String wr14;
	
	/** 平均/BSU  */
	@Column(length = 10)
	private String wr15;
	
	/** 烟度值/BSU 评价 */
	@Column(length = 5)
	private String wr16;
	
	//检验结论
	@Column(length = 60)
	private String jyjl;
	
	//检验人
	@Column(length = 15)
	private String jyr;

	public String getJylsh() {
		return jylsh;
	}

	public String getTxf() {
		return txf;
	}

	public String getHtbh() {
		return htbh;
	}

	public String getHphm() {
		return hphm;
	}

	public String getClxh() {
		return clxh;
	}

	public String getWg1() {
		return wg1;
	}

	public String getWg2() {
		return wg2;
	}

	public String getWg3() {
		return wg3;
	}

	public String getWg4() {
		return wg4;
	}

	public String getWg5() {
		return wg5;
	}

	public String getWg6() {
		return wg6;
	}

	public String getWg7() {
		return wg7;
	}

	public String getWg8() {
		return wg8;
	}

	public String getWg9() {
		return wg9;
	}

	public String getWg10() {
		return wg10;
	}

	public String getWg11() {
		return wg11;
	}

	public String getWg12() {
		return wg12;
	}

	public String getWg13() {
		return wg13;
	}

	public String getWg14() {
		return wg14;
	}

	public String getWg15() {
		return wg15;
	}

	public String getWg16() {
		return wg16;
	}

	public String getWg17() {
		return wg17;
	}

	public String getWg18() {
		return wg18;
	}

	public String getGz1() {
		return gz1;
	}

	public String getGz2() {
		return gz2;
	}

	public String getGz3() {
		return gz3;
	}

	public String getWr1() {
		return wr1;
	}

	public String getWr2() {
		return wr2;
	}

	public String getWr3() {
		return wr3;
	}

	public String getWr4() {
		return wr4;
	}

	public String getWr5() {
		return wr5;
	}

	public String getWr6() {
		return wr6;
	}

	public String getWr7() {
		return wr7;
	}

	public String getWr8() {
		return wr8;
	}

	public String getWr9() {
		return wr9;
	}

	public String getWr10() {
		return wr10;
	}

	public String getWr11() {
		return wr11;
	}

	public String getWr12() {
		return wr12;
	}

	public String getWr13() {
		return wr13;
	}

	public String getWr14() {
		return wr14;
	}

	public String getWr15() {
		return wr15;
	}

	public String getWr16() {
		return wr16;
	}

	public String getJyjl() {
		return jyjl;
	}

	public String getJyr() {
		return jyr;
	}

	public void setJylsh(String jylsh) {
		this.jylsh = jylsh;
	}

	public void setTxf(String txf) {
		this.txf = txf;
	}

	public void setHtbh(String htbh) {
		this.htbh = htbh;
	}

	public void setHphm(String hphm) {
		this.hphm = hphm;
	}

	public void setClxh(String clxh) {
		this.clxh = clxh;
	}

	public void setWg1(String wg1) {
		this.wg1 = wg1;
	}

	public void setWg2(String wg2) {
		this.wg2 = wg2;
	}

	public void setWg3(String wg3) {
		this.wg3 = wg3;
	}

	public void setWg4(String wg4) {
		this.wg4 = wg4;
	}

	public void setWg5(String wg5) {
		this.wg5 = wg5;
	}

	public void setWg6(String wg6) {
		this.wg6 = wg6;
	}

	public void setWg7(String wg7) {
		this.wg7 = wg7;
	}

	public void setWg8(String wg8) {
		this.wg8 = wg8;
	}

	public void setWg9(String wg9) {
		this.wg9 = wg9;
	}

	public void setWg10(String wg10) {
		this.wg10 = wg10;
	}

	public void setWg11(String wg11) {
		this.wg11 = wg11;
	}

	public void setWg12(String wg12) {
		this.wg12 = wg12;
	}

	public void setWg13(String wg13) {
		this.wg13 = wg13;
	}

	public void setWg14(String wg14) {
		this.wg14 = wg14;
	}

	public void setWg15(String wg15) {
		this.wg15 = wg15;
	}

	public void setWg16(String wg16) {
		this.wg16 = wg16;
	}

	public void setWg17(String wg17) {
		this.wg17 = wg17;
	}

	public void setWg18(String wg18) {
		this.wg18 = wg18;
	}

	public void setGz1(String gz1) {
		this.gz1 = gz1;
	}

	public void setGz2(String gz2) {
		this.gz2 = gz2;
	}

	public void setGz3(String gz3) {
		this.gz3 = gz3;
	}

	public void setWr1(String wr1) {
		this.wr1 = wr1;
	}

	public void setWr2(String wr2) {
		this.wr2 = wr2;
	}

	public void setWr3(String wr3) {
		this.wr3 = wr3;
	}

	public void setWr4(String wr4) {
		this.wr4 = wr4;
	}

	public void setWr5(String wr5) {
		this.wr5 = wr5;
	}

	public void setWr6(String wr6) {
		this.wr6 = wr6;
	}

	public void setWr7(String wr7) {
		this.wr7 = wr7;
	}

	public void setWr8(String wr8) {
		this.wr8 = wr8;
	}

	public void setWr9(String wr9) {
		this.wr9 = wr9;
	}

	public void setWr10(String wr10) {
		this.wr10 = wr10;
	}

	public void setWr11(String wr11) {
		this.wr11 = wr11;
	}

	public void setWr12(String wr12) {
		this.wr12 = wr12;
	}

	public void setWr13(String wr13) {
		this.wr13 = wr13;
	}

	public void setWr14(String wr14) {
		this.wr14 = wr14;
	}

	public void setWr15(String wr15) {
		this.wr15 = wr15;
	}

	public void setWr16(String wr16) {
		this.wr16 = wr16;
	}

	public void setJyjl(String jyjl) {
		this.jyjl = jyjl;
	}

	public void setJyr(String jyr) {
		this.jyr = jyr;
	}

}
