package com.xs.veh.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.DeviceCheckJudeg;
import com.xs.veh.entity.ExternalCheckJudge;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.data.BaseDeviceData;
import com.xs.veh.network.data.BrakRollerData;
import com.xs.veh.network.data.LightData;
import com.xs.veh.network.data.OtherInfoData;
import com.xs.veh.network.data.ParDataOfAnjian;
import com.xs.veh.network.data.SideslipData;
import com.xs.veh.network.data.SpeedData;

@Service("checkDataManager")
public class CheckDataManager {

	Logger logger = Logger.getLogger(CheckDataManager.class);

	// 判定结果 未检
	public static final Integer PDJG_WJ = 0;

	// 合格
	public static final Integer PDJG_HG = 1;

	// 不合格
	public static final Integer PDJG_BHG = 2;

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	@Resource(name = "vehManager")
	private VehManager vehManager;

	@Value("${jyjgmc}")
	private String jyjgmc;

	public void saveData(BaseDeviceData data) {
		this.hibernateTemplate.save(data);
	}

	public Map<String, List> getReport2(String jylsh) {

		List<DeviceCheckJudeg> deviceCheckJudegs = (List<DeviceCheckJudeg>) this.hibernateTemplate
				.find("from DeviceCheckJudeg where jylsh=?", jylsh);

		List<ExternalCheckJudge> externalCheckJudges = (List<ExternalCheckJudge>) this.hibernateTemplate
				.find("from ExternalCheckJudge where jylsh=?", jylsh);

		Map<String, List> data = new HashMap<String, List>();

		data.put("yqsbjyjg", deviceCheckJudegs);
		data.put("rgjyjg", deviceCheckJudegs);

		return data;

	}

	public Map<String, Object> getReport1(String jylsh) {
		Map<String, Object> data = new HashMap<String, Object>();

		List<LightData> lightDatas = (List<LightData>) this.hibernateTemplate
				.find("from LightData where jylsh=? and sjzt=? and jyxm like 'H%' ", jylsh, LightData.SJZT_ZC);

		for (LightData lightData : lightDatas) {
			lightData.setCzpy();
			data.put(lightData.getJyxm() + "_" + lightData.getGx(), lightData);
		}
		data.put("title", jyjgmc);

		List<SpeedData> sds = (List<SpeedData>) this.hibernateTemplate.find("from SpeedData where jylsh=? and sjzt=? ",
				jylsh, SpeedData.SJZT_ZC);
		if (sds != null && !sds.isEmpty()) {
			data.put("S1", sds.get(0));
		}

		List<SideslipData> sids = (List<SideslipData>) this.hibernateTemplate
				.find("from SideslipData where jylsh=? and sjzt=? ", jylsh, SideslipData.SJZT_ZC);
		if (sids != null && !sids.isEmpty()) {
			data.put("A1", sids.get(0));
		}

		List<BrakRollerData> brds = (List<BrakRollerData>) this.hibernateTemplate
				.find("from BrakRollerData where jylsh=? and sjzt=? ", jylsh, BrakRollerData.SJZT_ZC);
		for (BrakRollerData brd : brds) {
			if (!brd.getJyxm().equals("B0")) {
				data.put("ZD_" + brd.getJyxm(), brd);
			} else {
				data.put("ZD_" + brd.getJyxm() + "_" + brd.getZw(), brd);
			}

		}
		List otherInfoArray = this.hibernateTemplate.find("from OtherInfoData where jylsh=? ", jylsh);

		if (otherInfoArray != null && !otherInfoArray.isEmpty()) {
			OtherInfoData otherInfo = (OtherInfoData) otherInfoArray.get(0);
			data.put("other", otherInfo);
		}
		List plist = this.hibernateTemplate.find("from ParDataOfAnjian where jylsh=? ", jylsh);
		if (plist != null && !plist.isEmpty()) {
			ParDataOfAnjian parDataOfAnjian = (ParDataOfAnjian) plist.get(0);
			data.put("par", parDataOfAnjian);
		}

		return data;
	}

	public Integer getDxjccs(VehFlow vehFlow, BaseDeviceData baseDeviceData) {

		String name = baseDeviceData.getClass().getSimpleName();

		List data = this.hibernateTemplate.find("select max(dxcs) from " + name + " where jylsh=?  and jyxm=?",
				vehFlow.getJylsh(), vehFlow.getJyxm());

		if (data.get(0) != null) {
			return ((Integer) data.get(0)) + 1;
		} else {
			return 1;
		}
	}

	public void createOtherDataOfAnjian(String jylsh) {

		OtherInfoData otherInfoData = new OtherInfoData();

		VehCheckLogin vehCheckLogin = (VehCheckLogin) this.hibernateTemplate
				.find("from VehCheckLogin where jylsh=?", jylsh).get(0);
		otherInfoData.setBaseInfo(vehCheckLogin);

		// 获取驻车制动数据
		List<BrakRollerData> brds = (List<BrakRollerData>) this.hibernateTemplate.find(
				"from BrakRollerData where jylsh=? and jyxm='B0' and jycs=? and sjzt=? ", jylsh,
				vehCheckLogin.getJycs(), BrakRollerData.SJZT_ZC);
		// 处理驻车制动数据
		ParDataOfAnjian parDataOfAnjian = null;
		if (brds != null && !brds.isEmpty()) {
			parDataOfAnjian = new ParDataOfAnjian();
			for (BrakRollerData brd : brds) {
				parDataOfAnjian.setJylsh(brd.getJylsh());
				parDataOfAnjian.setHpzl(brd.getHphm());
				parDataOfAnjian.setHpzl(brd.getHpzl());
				parDataOfAnjian.setJycs(brd.getJycs());
				parDataOfAnjian.setDxcs(brd.getDxcs());
				parDataOfAnjian.setSjzt(ParDataOfAnjian.SJZT_ZC);
				int zw = brd.getZw();
				Integer zczczdl = parDataOfAnjian.getZczczdl();
				zczczdl = zczczdl == null ? 0 : zczczdl;
				switch (zw) {
				case 1:
					parDataOfAnjian.setYzzczdl(brd.getZzdl() + brd.getYzdl());
					parDataOfAnjian.setZczczdl(zczczdl + brd.getZzdl() + brd.getYzdl());
					break;
				case 2:
					parDataOfAnjian.setEzzczdl(brd.getZzdl() + brd.getYzdl());
					parDataOfAnjian.setZczczdl(zczczdl + brd.getZzdl() + brd.getYzdl());
					break;
				case 3:
					parDataOfAnjian.setSanzzczdl(brd.getZzdl() + brd.getYzdl());
					parDataOfAnjian.setZczczdl(zczczdl + brd.getZzdl() + brd.getYzdl());
					break;
				case 4:
					parDataOfAnjian.setSizzczdl(brd.getZzdl() + brd.getYzdl());
					parDataOfAnjian.setZczczdl(zczczdl + brd.getZzdl() + brd.getYzdl());
					break;
				case 5:
					parDataOfAnjian.setWzzczdl(brd.getZzdl() + brd.getYzdl());
					parDataOfAnjian.setZczczdl(zczczdl + brd.getZzdl() + brd.getYzdl());
					break;
				}
			}
		}

		// 计算整车制动力
		List<BrakRollerData> list = (List<BrakRollerData>) this.hibernateTemplate
				.find("from BrakRollerData where jylsh=? and jyxm<>'B0' and sjzt=?", jylsh, BrakRollerData.SJZT_ZC);

		// 制动力和
		Integer zdlh = 0;
		// 整车轮荷
		Integer zclh = 0;
		for (BrakRollerData brakRollerData : list) {
			zdlh += brakRollerData.getZzdl() + brakRollerData.getYzdl();
			zclh += brakRollerData.getZlh() + brakRollerData.getYlh();
		}
		otherInfoData.setJczczbzl(zclh);
		otherInfoData.setZdlh(zdlh);
		if (zclh != 0) {
			Float zczdl = (float) ((zdlh * 1.0 / (zclh * 0.98 * 1.0)) * 100);
			otherInfoData.setZczdl(MathRound(zczdl));
		}
		if (parDataOfAnjian != null) {

			Float tczdl = (float) ((parDataOfAnjian.getZczczdl() * 1.0 / (zclh * 0.98 * 1.0)) * 100);
			parDataOfAnjian.setTczclh(zclh);
			parDataOfAnjian.setTczdl(MathRound(tczdl));
			parDataOfAnjian.setTczdxz();
			parDataOfAnjian.setTczdpd();
			this.hibernateTemplate.save(parDataOfAnjian);
		}
		otherInfoData.setZczdlxz();
		otherInfoData.setZczdlpd();
		this.hibernateTemplate.save(otherInfoData);

		createDeviceCheckJudeg(vehCheckLogin, otherInfoData, parDataOfAnjian);
	}

	/**
	 * 生成报告单
	 */
	private void createDeviceCheckJudeg(final VehCheckLogin vehCheckLogin, OtherInfoData otherInfoData,
			ParDataOfAnjian parDataOfAnjian) {

		Map<String, Object> flagMap = new HashMap<String, Object>();

		int xh = 1;

		// 清空报告
		this.hibernateTemplate.execute(new HibernateCallback<Integer>() {
			@Override
			public Integer doInHibernate(Session session) throws HibernateException {
				int res = session.createQuery("delete DeviceCheckJudeg where jylsh=? and jyjgbh=? ")
						.setString(0, vehCheckLogin.getJylsh()).setString(1, vehCheckLogin.getJyjgbh()).executeUpdate();
				return res;
			}
		});

		// 制动数据判定
		createBrakRollerDateJudeg(vehCheckLogin, flagMap, xh);

		// 驻车制动率判定
		if (parDataOfAnjian != null) {
			DeviceCheckJudeg dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
			dcj1.setXh(xh);
			dcj1.setYqjyxm("整车手刹制动率");
			dcj1.setYqjyjg(parDataOfAnjian.getTczdl() == null ? "" : parDataOfAnjian.getTczdl().toString());
			dcj1.setYqbzxz(parDataOfAnjian.getTczdxz() == null ? "" : ">=" + parDataOfAnjian.getTczdxz());
			dcj1.setYqjgpd(parDataOfAnjian.getTczdpd() == null ? "" : parDataOfAnjian.getTczdpd().toString());
			dcj1.setXh(xh);
			xh++;
			this.hibernateTemplate.save(dcj1);
		}
		// 整车制动率判定
		if (otherInfoData != null) {
			DeviceCheckJudeg dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
			dcj1.setXh(xh);
			dcj1.setYqjyxm("整车制动率(%)");
			dcj1.setYqjyjg(otherInfoData.getZczdl() == null ? "" : otherInfoData.getZczdl().toString());
			dcj1.setYqbzxz(otherInfoData.getZczdlxz() == null ? "" : ">=" + otherInfoData.getZczdlxz());
			dcj1.setYqjgpd(otherInfoData.getZcpd() == null ? "" : otherInfoData.getZcpd().toString());
			dcj1.setXh(xh);
			xh++;
			this.hibernateTemplate.save(dcj1);
		}
		// 灯光数据判定
		createLightDataJudeg(vehCheckLogin, flagMap, xh);

		// 侧滑报告判定
		List<SideslipData> sideslipDatas = (List<SideslipData>) this.hibernateTemplate.find(
				"from SideslipData where jylsh=? and sjzt=? order by jycs desc", vehCheckLogin.getJylsh(),
				SideslipData.SJZT_ZC);

		if (sideslipDatas != null && !sideslipDatas.isEmpty()) {
			SideslipData sideslipData = sideslipDatas.get(0);
			DeviceCheckJudeg dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
			dcj1.setXh(xh);
			dcj1.setYqjyxm("侧滑检测值(m/km)");
			dcj1.setYqjyjg(sideslipData.getSideslip() == null ? "" : sideslipData.getSideslip().toString());
			dcj1.setYqbzxz(sideslipData.getChxz().replace(",", "~"));
			dcj1.setYqjgpd(sideslipData.getChpd() == null ? "" : sideslipData.getChpd().toString());
			dcj1.setXh(xh);
			xh++;
			this.hibernateTemplate.save(dcj1);
		}

		// 速度报告判定
		List<SpeedData> speedDatas = (List<SpeedData>) this.hibernateTemplate.find(
				"from SpeedData where   jylsh=? and sjzt=? order by jycs desc", vehCheckLogin.getJylsh(),
				SideslipData.SJZT_ZC);

		if (speedDatas != null && !speedDatas.isEmpty()) {
			SpeedData speedData = speedDatas.get(0);
			DeviceCheckJudeg dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
			dcj1.setXh(xh);
			dcj1.setYqjyxm("速度检测值(km/h)");
			dcj1.setYqjyjg(speedData.getSpeed() == null ? "" : speedData.getSpeed().toString());
			dcj1.setYqbzxz(speedData.getSdxz().replace(",", "~"));
			dcj1.setYqjgpd(speedData.getSdpd() == null ? "" : speedData.getSdpd().toString());
			dcj1.setXh(xh);
			xh++;
			this.hibernateTemplate.save(dcj1);
		}

		// 修改上线状态
		VehCheckLogin vehInfo = this.hibernateTemplate.load(VehCheckLogin.class, vehCheckLogin.getId());
		vehInfo.setVehsxzt(VehCheckLogin.JCZT_JYJS);
		this.hibernateTemplate.update(vehInfo);
		vehManager.updateVehCheckLoginState(vehCheckLogin.getJylsh());
	}

	private void createLightDataJudeg(final VehCheckLogin vehCheckLogin, Map<String, Object> flagMap, int xh) {
		List<LightData> lightDatas = (List<LightData>) this.hibernateTemplate.find(
				"from LightData where  jylsh=? and sjzt=? order by jycs desc", vehCheckLogin.getJylsh(),
				LightData.SJZT_ZC);

		String cllx = vehCheckLogin.getCllx();

		String syxz = vehCheckLogin.getSyxz();

		for (LightData lightData : lightDatas) {
			String jyxm = lightData.getJyxm();
			if (flagMap.get(jyxm + lightData.getGx()) == null) {
				if (lightData.getGx() == LightData.GX_YGD) {
					DeviceCheckJudeg dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
					dcj1.setXh(xh);
					dcj1.setYqjyxm(getLight(jyxm) + "光强(cd)");
					dcj1.setYqjyjg(lightData.getGq() == null ? "" : lightData.getGq().toString());
					dcj1.setYqbzxz(lightData.getGqxz() == null ? "" : ">=" + lightData.getGqxz().toString());
					dcj1.setYqjgpd(lightData.getGqpd() == null ? "" : lightData.getGqpd().toString());
					dcj1.setXh(xh);
					xh++;
					this.hibernateTemplate.save(dcj1);
				}

				if (!((cllx.indexOf("K3") == 0 || cllx.indexOf("K4") == 0 || cllx.indexOf("N") == 0)
						&& syxz.equals("A"))) {
					DeviceCheckJudeg dcj2 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
					dcj2.setXh(xh);
					dcj2.setYqjyxm(getLight(jyxm) + (lightData.getGx() == LightData.GX_YGD ? "远光灯" : "近光灯") + "垂直偏(H)");
					dcj2.setYqjyjg(lightData.getCzpy() == null ? "" : lightData.getCzpy().toString());
					dcj2.setYqbzxz(lightData.getCzpyxz() == null ? "" : lightData.getCzpyxz().replace(",", "~"));
					dcj2.setYqjgpd(lightData.getCzpypd() == null ? "" : lightData.getCzpypd().toString());
					dcj2.setXh(xh);
					xh++;
					this.hibernateTemplate.save(dcj2);
				}

			}

			flagMap.put(jyxm + lightData.getGx(), lightData);

		}
	}

	private void createBrakRollerDateJudeg(final VehCheckLogin vehCheckLogin, Map<String, Object> flagMap, int xh) {
		List<BrakRollerData> brds = (List<BrakRollerData>) this.hibernateTemplate.find(
				"from BrakRollerData where jylsh=? and jyxm !='B0' and sjzt=? order by jycs desc",
				vehCheckLogin.getJylsh(), BrakRollerData.SJZT_ZC);

		for (BrakRollerData brd : brds) {
			if (flagMap.get(brd.getJyxm()) == null) {
				DeviceCheckJudeg dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
				dcj1.setXh(xh);
				dcj1.setYqjyxm(getZW(brd.getZw()) + "制动率(%)");
				dcj1.setYqjyjg(brd.getKzxczdl() == null ? "" : brd.getKzxczdl().toString());
				dcj1.setYqbzxz(brd.getKzzdlxz() == null ? "" : ">=" + brd.getKzzdlxz().toString());
				dcj1.setYqjgpd(brd.getKzzdlpd() == null ? "" : brd.getKzzdlpd().toString());
				dcj1.setXh(xh);
				xh++;

				this.hibernateTemplate.save(dcj1);

				DeviceCheckJudeg dcj2 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
				dcj2.setXh(xh);
				dcj2.setYqjyxm(getZW(brd.getZw()) + "不平衡率(%)");
				dcj2.setYqjyjg(brd.getKzbphl() == null ? "" : brd.getKzbphl().toString());
				dcj2.setYqbzxz(brd.getBphlxz() == null ? "" : "<=" + brd.getBphlxz().toString());
				dcj2.setYqjgpd(brd.getKzbphlpd() == null ? "" : brd.getKzbphlpd().toString());
				dcj2.setXh(xh);
				xh++;
				this.hibernateTemplate.save(dcj2);

				if (brd.getJzzzdl() != null) {
					DeviceCheckJudeg dcj3 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
					dcj3.setXh(xh);
					dcj3.setYqjyxm(getZW(brd.getZw()) + "加载制动率(%)");
					dcj3.setYqjyjg(brd.getJzzzdl() == null ? "" : brd.getJzzzdl().toString());
					dcj3.setYqbzxz(brd.getJzzdlxz() == null ? "" : ">=" + brd.getJzzdlxz().toString());
					dcj3.setYqjgpd(brd.getJzzdlpd() == null ? "" : brd.getJzzdlpd().toString());
					dcj3.setXh(xh);
					xh++;
					this.hibernateTemplate.save(dcj3);

					DeviceCheckJudeg dcj4 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
					dcj4.setXh(xh);
					dcj4.setYqjyxm(getZW(brd.getZw()) + "加载不平衡率(%)");
					dcj4.setYqjyjg(brd.getJzbphl() == null ? "" : brd.getJzbphl().toString());
					dcj4.setYqbzxz(brd.getBphlxz() == null ? "" : "<=" + brd.getBphlxz().toString());
					dcj4.setYqjgpd(brd.getJzbphlpd() == null ? "" : brd.getJzbphlpd().toString());
					dcj4.setXh(xh);
					xh++;
					this.hibernateTemplate.save(dcj4);
				}
			}
			flagMap.put(brd.getJyxm(), brd);
		}
	}

	public String getLight(String jyxm) {

		if (jyxm.equals("H1")) {
			return "左主灯";
		}
		if (jyxm.equals("H2")) {
			return "左辅灯";
		}
		if (jyxm.equals("H3")) {
			return "右辅灯";
		}
		if (jyxm.equals("H4")) {
			return "右主灯";
		}

		return null;
	}

	public String getZW(Integer zw) {

		String str = "";

		switch (zw) {
		case 1:
			str = "一轴";
			break;
		case 2:
			str = "二轴";
			break;
		case 3:
			str = "三轴";
			break;
		case 4:
			str = "四轴";
			break;
		case 5:
			str = "五轴";
			break;
		case 6:
			str = "六轴";
			break;
		case 0:
			str = "驻车";
			break;
		default:
			str = zw.toString();
			break;
		}

		return str;

	}

	private DeviceCheckJudeg createDeviceCheckJudegBaseInfo(VehCheckLogin vehCheckLogin) {
		DeviceCheckJudeg dcj = new DeviceCheckJudeg();
		dcj.setHphm(vehCheckLogin.getHphm());
		dcj.setHpzl(vehCheckLogin.getHpzl());
		dcj.setJycs(vehCheckLogin.getJycs());
		dcj.setJyjgbh(vehCheckLogin.getJyjgbh());
		dcj.setJylsh(vehCheckLogin.getJylsh());
		return dcj;
	}

	public List<BrakRollerData> getReport4(String jylsh) {
		List<BrakRollerData> report4 = (List<BrakRollerData>) this.hibernateTemplate
				.find("from BrakRollerData where jylsh=? and sjzt=? and jyxm!=?", jylsh, BrakRollerData.SJZT_ZC, "B0");
		return report4;
	}

	public static Float MathRound(Float f) {
		return (float) (Math.round(f * 100)) / 100;
	}

	public BrakRollerData getBrakRollerDataOfVehLoginInfo(VehCheckLogin vehCheckLogin, String jyxm) {

		List<BrakRollerData> datas = (List<BrakRollerData>) this.hibernateTemplate.find(
				"from BrakRollerData where jylsh=? and jycs=? and jyxm=?", vehCheckLogin.getJylsh(),
				vehCheckLogin.getJycs(), jyxm);

		if (datas == null || datas.isEmpty()||jyxm.equals("B0")) {
			return null;
		} else {
			return datas.get(0);
		}

	}

}
