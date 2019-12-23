package com.xs.veh.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Scope("prototype")
@Component("TestVeh")
@Entity
@Table(name = "TestVeh",schema="QCPFWQ2018.dbo")
@JsonIgnoreProperties(value ={"hibernateLazyInitializer","handler","fieldHandler","password"})
public class TestVeh extends BaseEntity {
	
	//检验流水号
	private String jylsh;
	//检验次数
	private Integer jycs;
	
	//号牌种类
	private String hpzl;
	//号牌号码
	private String hphm;
	//检测性质	“二级维护/等级评定”等
	private String jcxz;
	
	/**检测项目	车速表：S1
	动力性测试：EP
	油耗测试：OC
	检测多项时，以逗号分隔检测项目**/
	private String jcxm;
	
	//1:是挂车；0:非挂车
	private Integer sfgc;
	
	//1:是客车；0:非客车
	private Integer sfkc;
	
	//RLZL	燃料种类	A:汽油B柴油
	private String rlzl;
	//JQFS	进气方式	自然吸气，涡轮增压...
	private String jqfs;
	
	//CLLX	车辆类型	按公安部规定，如“K33”等
	private String cllx;
	//SYXZ	使用性质	按国标，如“A”（非营运）等
	private String syxz;
	
	//ZZL	总质量		整数
	private Integer zzl;
	
	//ZBZL	整备质量		整数
	private Integer zbzl;
	
	//JGL	净功率(kw)		整数
	private Integer jgl;
	//EDGL	额定功率(kw)		整数
	private Integer edgl;
	
	//EDZS	额定转速(r/min)		整数
	private Integer edzs;
	
	//EDNJ	额定扭矩(NM)	Mm	整数
	private Integer ednj;
	
	//EDNJZS	额定扭矩转速(r/min)		整数
	private Integer ednjzs;
	
	//EDNJGL	额定扭矩功率(kw)		整数
	private Integer ednjgl;
	
	//EDYH	额定油耗		1位小数
	private String edyh;
	
	//PL	排量(ml)		整数
	private String pl;
	
	//LTLX	轮胎类型	0:子午胎；1:斜交胎；9：其他
	private Integer ltlx;
	//LTDMKD	轮胎断面宽度(mm)		整数
	private Integer ltdmkd;
	
	//QCGD	汽车高度(mm)		整数
	private Integer qcgd;
	
	//QLJ	前轮距(mm)		整数
	private Integer qlj;
	
	//QCCD	汽车长度(mm)		整数
	private Integer qccd;
	
	//KCDJ	客车等级	高级、中高级、中级、普通、高一、高二、高三	以文本表示
	private String kcdj;
	
	//HCCSXS	货车车身型式	1：拦板；2：自卸；3：牵引；4：仓栅；5：厢式；6：罐式
	private Integer hccsxs;
	
	//QDZKZZL	驱动轴空载质量(kg)		整数
	private Integer qdzkzzl;
	
	//QYCMZZL	牵引车满载质量(kg)		整数
	private Integer qycmzzl;
	
	//DLXPJBZ	动力性评级标准	1:一级	2:二级
	private String dlxpjbz;
	
	//CSBXX	车速下限
	private String csbxx;
	//CSBSX	车速上限
	private String csbsx;
	//YHXZ	油耗限值
	private String yhxz;
	
	//YHCS	油耗车速
	private String yhcs;
	
	//允许检测
	private Integer ysjc;
	
	//检测完成
	private Integer jcwc;
	
	private String zjywlx;
	
	private String zjwtr;
	
	private String dlyxzh;
	
	
	

	public String getZjwtr() {
		return zjwtr;
	}

	public void setZjwtr(String zjwtr) {
		this.zjwtr = zjwtr;
	}

	public String getDlyxzh() {
		return dlyxzh;
	}

	public void setDlyxzh(String dlyxzh) {
		this.dlyxzh = dlyxzh;
	}

	public String getZjywlx() {
		return zjywlx;
	}

	public void setZjywlx(String zjywlx) {
		this.zjywlx = zjywlx;
	}

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

	public String getJcxz() {
		return jcxz;
	}

	public void setJcxz(String jcxz) {
		this.jcxz = jcxz;
	}

	public String getJcxm() {
		return jcxm;
	}

	public void setJcxm(String jcxm) {
		this.jcxm = jcxm;
	}

	public Integer getSfgc() {
		return sfgc;
	}

	public void setSfgc(Integer sfgc) {
		this.sfgc = sfgc;
	}

	public Integer getSfkc() {
		return sfkc;
	}

	public void setSfkc(Integer sfkc) {
		this.sfkc = sfkc;
	}

	public String getRlzl() {
		return rlzl;
	}

	public void setRlzl(String rlzl) {
		this.rlzl = rlzl;
	}

	public String getJqfs() {
		return jqfs;
	}

	public void setJqfs(String jqfs) {
		this.jqfs = jqfs;
	}

	public String getCllx() {
		return cllx;
	}

	public void setCllx(String cllx) {
		this.cllx = cllx;
	}

	public String getSyxz() {
		return syxz;
	}

	public void setSyxz(String syxz) {
		this.syxz = syxz;
	}

	public Integer getZzl() {
		return zzl;
	}

	public void setZzl(Integer zzl) {
		this.zzl = zzl;
	}

	public Integer getZbzl() {
		return zbzl;
	}

	public void setZbzl(Integer zbzl) {
		this.zbzl = zbzl;
	}

	public Integer getJgl() {
		return jgl;
	}

	public void setJgl(Integer jgl) {
		this.jgl = jgl;
	}

	public Integer getEdgl() {
		return edgl;
	}

	public void setEdgl(Integer edgl) {
		this.edgl = edgl;
	}

	public Integer getEdzs() {
		return edzs;
	}

	public void setEdzs(Integer edzs) {
		this.edzs = edzs;
	}

	public Integer getEdnj() {
		return ednj;
	}

	public void setEdnj(Integer ednj) {
		this.ednj = ednj;
	}

	public Integer getEdnjzs() {
		return ednjzs;
	}

	public void setEdnjzs(Integer ednjzs) {
		this.ednjzs = ednjzs;
	}

	public Integer getEdnjgl() {
		return ednjgl;
	}

	public void setEdnjgl(Integer ednjgl) {
		this.ednjgl = ednjgl;
	}

	public String getEdyh() {
		return edyh;
	}

	public void setEdyh(String edyh) {
		this.edyh = edyh;
	}

	public String getPl() {
		return pl;
	}

	

	public void setPl(String pl) {
		this.pl = pl;
	}

	public Integer getLtlx() {
		return ltlx;
	}

	public void setLtlx(Integer ltlx) {
		this.ltlx = ltlx;
	}

	public Integer getLtdmkd() {
		return ltdmkd;
	}

	public void setLtdmkd(Integer ltdmkd) {
		this.ltdmkd = ltdmkd;
	}

	public Integer getQcgd() {
		return qcgd;
	}

	public void setQcgd(Integer qcgd) {
		this.qcgd = qcgd;
	}

	public Integer getQlj() {
		return qlj;
	}

	public void setQlj(Integer qlj) {
		this.qlj = qlj;
	}

	public Integer getQccd() {
		return qccd;
	}

	public void setQccd(Integer qccd) {
		this.qccd = qccd;
	}

	public String getKcdj() {
		return kcdj;
	}

	public void setKcdj(String kcdj) {
		this.kcdj = kcdj;
	}

	public Integer getHccsxs() {
		return hccsxs;
	}

	public void setHccsxs(Integer hccsxs) {
		this.hccsxs = hccsxs;
	}

	public Integer getQdzkzzl() {
		return qdzkzzl;
	}

	public void setQdzkzzl(Integer qdzkzzl) {
		this.qdzkzzl = qdzkzzl;
	}

	public Integer getQycmzzl() {
		return qycmzzl;
	}

	public void setQycmzzl(Integer qycmzzl) {
		this.qycmzzl = qycmzzl;
	}

	public String getDlxpjbz() {
		return dlxpjbz;
	}

	public void setDlxpjbz(String dlxpjbz) {
		this.dlxpjbz = dlxpjbz;
	}

	public String getCsbxx() {
		return csbxx;
	}

	public void setCsbxx(String csbxx) {
		this.csbxx = csbxx;
	}

	public String getCsbsx() {
		return csbsx;
	}

	public void setCsbsx(String csbsx) {
		this.csbsx = csbsx;
	}

	public String getYhxz() {
		return yhxz;
	}

	public void setYhxz(String yhxz) {
		this.yhxz = yhxz;
	}

	public String getYhcs() {
		return yhcs;
	}

	public void setYhcs(String yhcs) {
		this.yhcs = yhcs;
	}

	public Integer getYsjc() {
		return ysjc;
	}

	public void setYsjc(Integer ysjc) {
		this.ysjc = ysjc;
	}

	public Integer getJcwc() {
		return jcwc;
	}

	public void setJcwc(Integer jcwc) {
		this.jcwc = jcwc;
	}

	public Integer getJycs() {
		return jycs;
	}

	public void setJycs(Integer jycs) {
		this.jycs = jycs;
	}
	
	
	public String setYHxz() {
		
		String yhxz="-";
		
		if(sfkc==1) {
			if(qccd<=6000) {
				yhxz=kcdj.equals("高级")?"11.3":"9.5";
			}else if(qccd>6000&&qccd<=7000) {
				yhxz=kcdj.equals("高级")?"13.1":"11.5";
			}else if(qccd>7000&&qccd<=8000) {
				yhxz=kcdj.equals("高级")?"15.3":"14.1";
			}else if(qccd>8000&&qccd<=9000) {
				yhxz=kcdj.equals("高级")?"16.4":"15.5";
			}else if(qccd>9000&&qccd<=10000) {
				yhxz=kcdj.equals("高级")?"17.8":"16.7";
			}else if(qccd>10000&&qccd<=11000) {
				yhxz=kcdj.equals("高级")?"19.4":"17.6";
			}else if(qccd>11000&&qccd<=12000) {
				yhxz=kcdj.equals("高级")?"20.1":"18.3";
			}else {
				yhxz=kcdj.equals("高级")?"22.3":"20.3";
			}
		}else if(sfgc==1) {
			if(zzl<=27000) {
				yhxz="42.9";
			}else if(zzl>27000&&zzl<=35000) {
				yhxz="43.9";
			}else if(zzl>35000&&zzl<=43000) {
				yhxz="46.2";
			}else if(zzl>43000&&zzl<=49000) {
				yhxz="47.3";
			}
		}else {
			if(zzl>3500&&zzl<=4000) {
				yhxz="10.6";
			}else if(zzl>4000&&zzl<=5000) {
				yhxz="11.3";
			}else if(zzl>5000&&zzl<=6000) {
				yhxz="12.6";
			}else if(zzl>6000&&zzl<=7000) {
				yhxz="13.9";
			}else if(zzl>7000&&zzl<=8000) {
				yhxz="14.9";
			}else if(zzl>8000&&zzl<=9000) {
				yhxz="16.1";
			}else if(zzl>9000&&zzl<=10000) {
				yhxz="16.9";
			}else if(zzl>10000&&zzl<=11000) {
				yhxz="18.0";
			}else if(zzl>11000&&zzl<=12000) {
				yhxz="19.1";
			}else if(zzl>12000&&zzl<=13000) {
				yhxz="20.0";
			}else if(zzl>13000&&zzl<=14000) {
				yhxz="20.9";
			}else if(zzl>14000&&zzl<=15000) {
				yhxz="21.6";
			}else if(zzl>15000&&zzl<=16000) {
				yhxz="22.7";
			}else if(zzl>16000&&zzl<=17000) {
				yhxz="23.6";
			}else if(zzl>17000&&zzl<=18000) {
				yhxz="24.4";
			}else if(zzl>18000&&zzl<=19000) {
				yhxz="25.4";
			}else if(zzl>19000&&zzl<=20000) {
				yhxz="26.1";
			}else if(zzl>20000&&zzl<=21000) {
				yhxz="27.0";
			}else if(zzl>21000&&zzl<=22000) {
				yhxz="27.7";
			}else if(zzl>22000&&zzl<=23000) {
				yhxz="28.2";
			}else if(zzl>23000&&zzl<=24000) {
				yhxz="28.8";
			}else if(zzl>24000&&zzl<=25000) {
				yhxz="29.5";
			}else if(zzl>25000&&zzl<=26000) {
				yhxz="30.1";
			}else if(zzl>26000&&zzl<=27000) {
				yhxz="30.8";
			}else if(zzl>27000&&zzl<=28000) {
				yhxz="31.7";
			}else if(zzl>28000&&zzl<=29000) {
				yhxz="32.6";
			}else if(zzl>29000&&zzl<=30000) {
				yhxz="33.7";
			}else if(zzl>30000&&zzl<=31000) {
				yhxz="34.4";
			}
		}
		this.yhxz=yhxz;
		return yhxz;
	}


	
	
}
