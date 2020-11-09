package com.xs.veh.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.xs.common.BaseParamsUtil;
import com.xs.common.MyHibernateTemplate;
import com.xs.veh.entity.BaseParams;
import com.xs.veh.entity.CheckEvents;
import com.xs.veh.entity.CheckLog;
import com.xs.veh.entity.CheckPhoto;
import com.xs.veh.entity.Device;
import com.xs.veh.entity.DeviceCheckJudeg;
import com.xs.veh.entity.ExternalCheck;
import com.xs.veh.entity.ExternalCheckJudge;
import com.xs.veh.entity.Insurance;
import com.xs.veh.entity.PlateApplyTable;
import com.xs.veh.entity.RoadCheck;
import com.xs.veh.entity.TaskPicture;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.DeviceDisplay;
import com.xs.veh.network.SimpleRead;
import com.xs.veh.network.data.BaseDeviceData;
import com.xs.veh.network.data.BrakRollerData;
import com.xs.veh.network.data.CurbWeightData;
import com.xs.veh.network.data.LightData;
import com.xs.veh.network.data.OtherInfoData;
import com.xs.veh.network.data.Outline;
import com.xs.veh.network.data.ParDataOfAnjian;
import com.xs.veh.network.data.SideslipData;
import com.xs.veh.network.data.SpeedData;
import com.xs.veh.util.HKVisionUtil;
import com.xs.veh.util.RCAConstant;

import net.sf.json.JSONObject;

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
	private MyHibernateTemplate hibernateTemplate;
	
	@Autowired
	private RoadCheackManager roadCheackManager;

	@Resource(name = "vehManager")
	private VehManager vehManager;
	
	@Autowired
	private VideoManager videoManager;

	@Value("${jyjgmc}")
	private String jyjgmc;
	
	@Value("${jyjgbh}")
	private String jyjgbh;
	
	@Value("${sf}")
	private String sf;
	
	@Value("${cs}")
	private String cs;

	@Resource(name = "checkEventManger")
	private CheckEventManger checkEventManger;
	
	@Autowired
	private ServletContext servletContext;
	
	@Resource(name = "deviceManager")
	private DeviceManager deviceManager;
	
	@Autowired
	private HKVisionUtil hkUtil;

	public <T> void saveData(BaseDeviceData data) {
		this.hibernateTemplate.saveOrUpdate(data);
	}
	

	public Map<String, List> getReport2(String jylsh) {

		List<DeviceCheckJudeg> deviceCheckJudegs = (List<DeviceCheckJudeg>) this.hibernateTemplate
				.find("from DeviceCheckJudeg where jylsh=?", jylsh);

		List<ExternalCheckJudge> externalCheckJudges = (List<ExternalCheckJudge>) this.hibernateTemplate
				.find("from ExternalCheckJudge where jylsh=?", jylsh);

		Map<String, List> data = new HashMap<String, List>();

		data.put("yqsbjyjg", deviceCheckJudegs);
		data.put("rgjyjg", externalCheckJudges);

		return data;

	}
	
	public ExternalCheck getReport3(String jylsh) {

		List<ExternalCheck> datas = (List<ExternalCheck>) this.hibernateTemplate.find("from ExternalCheck where jylsh=? order by id desc", jylsh);
		
		if(!CollectionUtils.isEmpty(datas)) {
			return datas.get(0);
		}
		
		return null;

	}

	public Map<String, Object> getReport1(String jylsh,int jycs) {
		
		if(jycs==0){
			jycs=1;
		}
		
		VehCheckLogin  vehCheckLogin=this.getVehCheckLogin(jylsh);
		
		Map<String, Object> data = new HashMap<String, Object>();

		List<LightData> lightDatas = (List<LightData>) this.hibernateTemplate
				.find("from LightData where jylsh=? and jyxm like 'H%' order by id asc", jylsh);
		
		Map<String, Object> lightMapData = getLightDatasOfJycs(lightDatas,jycs);
		if(!lightMapData.isEmpty()){
			data.putAll(lightMapData);
		}

		/*for (LightData lightData : lightDatas) {
			lightData.setCzpy();
			data.put(lightData.getJyxm() + "_" + lightData.getGx(), lightData);
		}*/
		data.put("title", jyjgmc);

		List<SpeedData> sds = (List<SpeedData>) this.hibernateTemplate
				.find("from SpeedData where jylsh=? order by id desc", jylsh);
		
		if (sds != null && !sds.isEmpty()) {
			SpeedData speedData = sds.get(0);
			int speedJycs=speedData.getJycs();
			if(speedJycs>jycs){
				speedJycs=jycs;
			}
			for(SpeedData sd:sds){
				
				if(sd.getJycs()==speedJycs){
					data.put("S1", sd);
				}
			}
		}

		List<SideslipData> sids = (List<SideslipData>) this.hibernateTemplate
				.find("from SideslipData where jylsh=?  order by id desc", jylsh);
		if (sids != null && !sids.isEmpty()) {
			SideslipData sideslipData = sids.get(0);
			int sideslipJycs=sideslipData.getJycs();
			if(sideslipJycs>jycs){
				sideslipJycs=jycs;
			}
			for(SideslipData sid:sids){
				if(sid.getJycs()==sideslipJycs){
					data.put("A1", sid);
				}
			}
		}

		List<BrakRollerData> brds = (List<BrakRollerData>) this.hibernateTemplate
				.find("from BrakRollerData where jylsh=?  order by id desc ", jylsh);
		
		OtherInfoData otherData=new OtherInfoData();
		
		for (BrakRollerData brd : brds) {
			int zdjycs=brd.getJycs();
			if(zdjycs>jycs){
				zdjycs=jycs;
			}
			
			if (!brd.getJyxm().equals("B0")) {
				String key = "ZD_" + brd.getJyxm();
				if (data.get(key) == null&&zdjycs==brd.getJycs()) {
					data.put(key, brd);
					if(brd.getJyxm().contains("B")){
						int zdlh=otherData.getZdlh()==null?0:otherData.getZdlh();
						int zczbzl=otherData.getJczczbzl()==null?0:otherData.getJczczbzl();
						
//						if(vehCheckLogin.getZs()>=3&&brd.getJzzlh()!=null&&brd.getJzylh()!=null) {
//							logger.info("负荷台称重！");
//							otherData.setJczczbzl(zczbzl+brd.getJzzlh()+brd.getJzylh());
//						}else {
//							otherData.setJczczbzl(zczbzl+brd.getZlh()+brd.getYlh());
//						}
						otherData.setJczczbzl(zczbzl+brd.getZlh()+brd.getYlh());
						Integer zzdl = brd.getZzdl()==null?0:brd.getZzdl();
						Integer yzdl= brd.getYzdl()==null?0:brd.getYzdl();
						
						otherData.setZdlh(zdlh+zzdl+yzdl);
					}
					
				}
			} else {
				String key = "ZD_" + brd.getJyxm() + "_" + brd.getZw();
				if (data.get(key) == null&&zdjycs==brd.getJycs()) {
					data.put(key, brd);
				}
			}
		}
		
		if(otherData.getZdlh()!=null){
			otherData.setBaseInfo(vehCheckLogin);
			otherData.setZczdlxz();
			otherData.setZczdl();
			otherData.setZczdlpd();
			data.put("other", otherData);
		}
		
		
		/*List otherInfoArray = this.hibernateTemplate.find("from OtherInfoData where jylsh=? order by id desc ", jylsh);

		if (otherInfoArray != null && !otherInfoArray.isEmpty()) {
			OtherInfoData otherInfo = (OtherInfoData) otherInfoArray.get(0);
			data.put("other", otherInfo);
		}*/
		
		
		List<ParDataOfAnjian> plist = (List<ParDataOfAnjian>) this.hibernateTemplate.find("from ParDataOfAnjian where jylsh=? order by id desc ", jylsh);
		if (plist != null && !plist.isEmpty()) {
			ParDataOfAnjian parDataOfAnjian = plist.get(0);
			int pjycs=parDataOfAnjian.getJycs();
			if(pjycs>jycs){
				pjycs=jycs;
			}
			
			for(ParDataOfAnjian pdata:plist){
				if(pdata.getJycs()==pjycs){
					data.put("par", pdata);
				}
			}
		}

		List<DeviceCheckJudeg> roadChecks = (List<DeviceCheckJudeg>) this.hibernateTemplate
				.find("from DeviceCheckJudeg where jylsh=? and bz1='R' ", jylsh);
		
		if (roadChecks != null && !roadChecks.isEmpty()) {
			RoadCheck roadCheckInfo = roadCheackManager.getRoadCheck(jylsh);
			data.put("roadChecks", roadChecks);
			data.put("roadCheckInfo", roadCheckInfo);
		}

		List<RoadCheck> lsys = (List<RoadCheck>) this.hibernateTemplate.find("from RoadCheck where jylsh=?", jylsh);
		if (lsys != null && !lsys.isEmpty()) {
			data.put("lsy", lsys.get(0).getLsy());
		}
		
		List<Outline> outlines=(List<Outline>) this.hibernateTemplate.find("from Outline where jylsh=? order by id desc", jylsh);
		
		if(outlines!=null&&!outlines.isEmpty()){
			
			Outline outline = outlines.get(0);
			int ojycs=outline.getJycs();
			
			if(ojycs>jycs){
				ojycs=jycs;
			}
			
			for(Outline odata:outlines){
				if(odata.getJycs()==ojycs){
					data.put("wkcc", odata);
				}
			}
		}
		
		List<CurbWeightData> datas = (List<CurbWeightData>) this.hibernateTemplate.find("from CurbWeightData where jylsh=? order by id desc", jylsh);
		
		if(!datas.isEmpty()) {
			data.put("Z1", datas.get(0));
		}

		return data;
	}

	/**
	 * 根据建议次数获取灯光数据
	 * @param lightDatas
	 * @param jycs
	 * @return
	 */
	public Map<String,Object> getLightDatasOfJycs(List<LightData> lightDatas, int jycs) {
		
		//List<LightData> datas =new ArrayList<LightData>();
		Map<String, Object> datas = new HashMap<String, Object>();
		if(lightDatas.isEmpty()){
			return datas;
		}
		
		LightData lightData = lightDatas.get(lightDatas.size()-1);
		
		
		if(lightData.getJycs()<=jycs){
			jycs=lightData.getJycs();
		}
		
		for(LightData data:lightDatas){
			
			if(data.getJycs()>jycs) {
				continue;
			}
			String key=data.getJyxm() + "_" + data.getGx();
			datas.put(key, data);
		}
		
		
		
		/*for(int i=jycs;i>=1;i--){
			for(LightData data:lightDatas){
				String key=data.getJyxm() + "_" + data.getGx();
				
				if(data.getJycs()==i&&datas.get(key)==null){
					datas.put(key, data);
				}
			}
			
		}*/
		
		
		return datas;
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

		List<OtherInfoData> otherInfoDatas = (List<OtherInfoData>) this.hibernateTemplate
				.find("from OtherInfoData where jylsh=?", jylsh);
		
		OtherInfoData otherInfoData = new OtherInfoData();

		if (otherInfoDatas != null && !otherInfoDatas.isEmpty()) {
			
			OtherInfoData other = otherInfoDatas.get(0);
			otherInfoData.setJczczbzl(other.getJczczbzl());
			otherInfoData.setZbzlpd(other.getZbzlpd());
			otherInfoData.setBzzczbzl(other.getBzzczbzl());
			otherInfoData.setZczbzlbfb(other.getZczbzlbfb());
			
			this.hibernateTemplate.delete(other);
		}

		

		VehCheckLogin vehCheckLogin = (VehCheckLogin) this.hibernateTemplate
				.find("from VehCheckLogin where jylsh=?", jylsh).get(0);
		otherInfoData.setBaseInfo(vehCheckLogin);
		logger.info("current Session2="+this.hibernateTemplate.getSessionFactory().getCurrentSession());
		
		final String tjylsh=jylsh;

		// 计算整车制动力
//		List<BrakRollerData> list = (List<BrakRollerData>) this.hibernateTemplate
//				.find("from BrakRollerData where jylsh=? and jyxm<>'B0' and sjzt=? and jyxm<>'L1' and jyxm<>'L2' and jyxm<>'L3' and jyxm<>'L4'", jylsh, BrakRollerData.SJZT_ZC);

		List<BrakRollerData> list=  this.hibernateTemplate.executeWithNativeSession(new HibernateCallback<List<BrakRollerData>>() {
			@Override
			public List<BrakRollerData> doInHibernate(Session session) throws HibernateException {
				List<BrakRollerData> data = session.createQuery("from BrakRollerData where jylsh=? and jyxm<>'B0' and sjzt=? and jyxm<>'L1' and jyxm<>'L2' and jyxm<>'L3' and jyxm<>'L4'").setParameter(0, tjylsh).setParameter(1, BrakRollerData.SJZT_ZC).list();
				return data;
			}
		});
		
		// 制动力和
		Integer zdlh = 0;
		// 整车轮荷
		Integer zclh = 0;
		
		//判断是否平板设备
		boolean isRoller=true;
		
		for (BrakRollerData brakRollerData : list) {
			zclh += brakRollerData.getZlh() + brakRollerData.getYlh();
			Integer zzdl=brakRollerData.getZzdl()==null?0:brakRollerData.getZzdl();
			Integer yzdl=brakRollerData.getYzdl()==null?0:brakRollerData.getYzdl();
			zdlh += zzdl + yzdl;

			
			if(brakRollerData.getZdtlh()!=null) {
				isRoller=false;
			}
			
		}
		otherInfoData.setZdlh(zdlh);
		
		otherInfoData.setJczczbzl(zclh);
		if (zclh != 0) {
			Float zczdl = (float) ((zdlh * 1.0 / (zclh * 0.98 * 1.0)) * 100);
			otherInfoData.setZczdl(MathRound1(zczdl));
		}
		
		otherInfoData.setZczdlxz();
		otherInfoData.setZczdlpd();

		otherInfoData.setZjccs(vehCheckLogin.getJycs());
		this.hibernateTemplate.save(otherInfoData);
		
		// 修改上线状态
		VehCheckLogin vehInfo = this.hibernateTemplate.load(VehCheckLogin.class, vehCheckLogin.getId());
		vehInfo.setVehsxzt(VehCheckLogin.JCZT_JYJS);
		this.hibernateTemplate.update(vehInfo);
	}
	
	
	public void createParDataOfAnjian(String jylsh) {
		VehCheckLogin vehCheckLogin = (VehCheckLogin) this.hibernateTemplate
				.find("from VehCheckLogin where jylsh=?", jylsh).get(0);
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
		
		List<BrakRollerData> list= (List<BrakRollerData>) this.hibernateTemplate.
				find("from BrakRollerData where jylsh=? and jyxm<>'B0' and sjzt=? and jyxm<>'L1' and jyxm<>'L2' and jyxm<>'L3' and jyxm<>'L4'", 
						jylsh, BrakRollerData.SJZT_ZC);
		// 制动力和
		Integer zdlh = 0;
		// 整车轮荷
		Integer zclh = 0;
		
		//判断是否平板设备
		boolean isRoller=true;
		
		for (BrakRollerData brakRollerData : list) {
			zclh += brakRollerData.getZlh() + brakRollerData.getYlh();
			Integer zzdl=brakRollerData.getZzdl()==null?0:brakRollerData.getZzdl();
			Integer yzdl=brakRollerData.getYzdl()==null?0:brakRollerData.getYzdl();
			zdlh += zzdl + yzdl;
			if(brakRollerData.getZdtlh()!=null) {
				isRoller=false;
			}
			
		}
		
		if (parDataOfAnjian != null) {
			Float tczdl = (float) ((parDataOfAnjian.getZczczdl() * 1.0 / (zclh * 0.98 * 1.0)) * 100);
			parDataOfAnjian.setTczclh(zclh);
			parDataOfAnjian.setTczdl(MathRound1(tczdl));
			parDataOfAnjian.setTczdxz(vehCheckLogin,isRoller);
			parDataOfAnjian.setTczdpd();
			this.hibernateTemplate.save(parDataOfAnjian);
			try {
				checkEventManger.createEvent(jylsh, vehCheckLogin.getJycs(), "18C55", "B0", vehCheckLogin.getHphm(), vehCheckLogin.getHpzl()
						,vehCheckLogin.getClsbdh(),vehCheckLogin.getVehcsbj());
				Thread.sleep(100);
				checkEventManger.createEvent(jylsh, vehCheckLogin.getJycs(), "18C81", "B0", vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(),
						vehCheckLogin.getClsbdh(),vehCheckLogin.getVehcsbj());
				Thread.sleep(100);
				checkEventManger.createEvent(jylsh, vehCheckLogin.getJycs(), "18C58", "B0", vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(),
						vehCheckLogin.getClsbdh(),vehCheckLogin.getVehcsbj());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	/**
	 * 生成报告单
	 */
	public void createDeviceCheckJudeg(final VehCheckLogin vehCheckLogin) {

		Map<String, Object> flagMap = new HashMap<String, Object>();
		
		List otherInfoDatas =this.hibernateTemplate.find("from OtherInfoData where jylsh=? order by id desc", vehCheckLogin.getJylsh());
		
		List parDataOfAnjians =this.hibernateTemplate.find("from ParDataOfAnjian where jylsh=? order by id desc", vehCheckLogin.getJylsh());
		
		List outlines =this.hibernateTemplate.find("from Outline where jylsh=? order by id desc ", vehCheckLogin.getJylsh());
		
		OtherInfoData otherInfoData = null;
		ParDataOfAnjian parDataOfAnjian=null;
		
		Outline outline=null;
		
		if(otherInfoDatas!=null&&!otherInfoDatas.isEmpty()){
			otherInfoData=(OtherInfoData) otherInfoDatas.get(0);
		}
		
		if(parDataOfAnjians!=null&&!parDataOfAnjians.isEmpty()){
			parDataOfAnjian=(ParDataOfAnjian) parDataOfAnjians.get(0);
		}
		
		if(outlines!=null&&!outlines.isEmpty()){
			 outline =(Outline) outlines.get(0);
		}

		Integer xh = 1;

		// 清空报告
		this.hibernateTemplate.execute(new HibernateCallback<Integer>() {
			@Override
			public Integer doInHibernate(Session session) throws HibernateException {
				int res = session.createQuery("delete DeviceCheckJudeg where jylsh=? and jyjgbh=? ")
						.setString(0, vehCheckLogin.getJylsh()).setString(1, vehCheckLogin.getJyjgbh()).executeUpdate();
				return res;
			}
		});

		// 路试数据
		xh = createRoadCheckJudeg(vehCheckLogin, xh);

		// 制动数据判定
		xh = createBrakRollerDateJudeg(vehCheckLogin, flagMap, xh);

		// 驻车制动率判定
		if (parDataOfAnjian != null) {
			DeviceCheckJudeg dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
			dcj1.setYqjyxm("驻车制动率(%)");
			dcj1.setYqjyjg(parDataOfAnjian.getTczdl() == null ? "" : parDataOfAnjian.getTczdl().toString());
			dcj1.setYqbzxz(parDataOfAnjian.getTczdxz() == null ? "" : "≥" + parDataOfAnjian.getTczdxz());
			dcj1.setYqjgpd(parDataOfAnjian.getTczdpd() == null ? "" : parDataOfAnjian.getTczdpd().toString());
			dcj1.setXh(xh);
			xh++;
			this.hibernateTemplate.save(dcj1);
		}
		// 整车制动率判定
		if (otherInfoData != null&&vehCheckLogin.getCllx().indexOf("N")==-1) {
			DeviceCheckJudeg dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
			dcj1.setYqjyxm("整车制动率(%)");
			dcj1.setYqjyjg(otherInfoData.getZczdl() == null ? "" : otherInfoData.getZczdl().toString());
			dcj1.setYqbzxz(otherInfoData.getZczdlxz() == null ? "" : "≥" + otherInfoData.getZczdlxz());
			dcj1.setYqjgpd(otherInfoData.getZcpd() == null ? "" : otherInfoData.getZcpd().toString());
			dcj1.setXh(xh);
			xh++;
			this.hibernateTemplate.save(dcj1);
		}
		// 灯光数据判定
		xh = createLightDataJudeg(vehCheckLogin, flagMap, xh);

		// 侧滑报告判定
		List<SideslipData> sideslipDatas = (List<SideslipData>) this.hibernateTemplate.find(
				"from SideslipData where jylsh=? and sjzt=? order by jycs desc", vehCheckLogin.getJylsh(),
				SideslipData.SJZT_ZC);

		if (sideslipDatas != null && !sideslipDatas.isEmpty()) {
			SideslipData sideslipData = sideslipDatas.get(0);
			DeviceCheckJudeg dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
			dcj1.setYqjyxm("转向轮横向侧滑值(m/km)");
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
			dcj1.setYqjyxm("车速表指示误差(km/h)");
			dcj1.setYqjyjg(speedData.getSpeed() == null ? "" : speedData.getSpeed().toString());
			dcj1.setYqbzxz(speedData.getSdxz().replace(",", "~"));
			dcj1.setYqjgpd(speedData.getSdpd() == null ? "" : speedData.getSdpd().toString());
			dcj1.setXh(xh);
			xh++;
			this.hibernateTemplate.save(dcj1);
		}
		
		CurbWeightData curbWeightData = this.vehManager.getLastCurbWeightDataOfJylsh(vehCheckLogin.getJylsh());

		if (curbWeightData != null&&vehCheckLogin.getJyxm().indexOf("Z1")>=0) {
			String cllx=vehCheckLogin.getCllx();
			int xzgj=100;
			String temp1="±3%或±";
			if(cllx.indexOf("H1")==0||cllx.indexOf("H2")==0||cllx.indexOf("Z1")==0||cllx.indexOf("Z2")==0||cllx.indexOf("Z5")==0||cllx.indexOf("G")==0||cllx.indexOf("B")==0){
				xzgj=500;
			}else if(cllx.indexOf("H3")==0||cllx.indexOf("H4")==0||cllx.indexOf("Z3")==0||cllx.indexOf("Z4")==0){
				xzgj=100;
			}else if(cllx.indexOf("N")==0){
				xzgj=100;
				temp1="±5%或±";
			}else if(cllx.indexOf("M")==0){
				xzgj=10;
			}
			
			DeviceCheckJudeg dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
			dcj1.setXh(xh);
			dcj1.setYqjyxm("整备质量");
			dcj1.setYqjyjg(curbWeightData.getZbzl()==null ? "" : curbWeightData.getZbzl().toString());
			dcj1.setYqbzxz(temp1+xzgj+"kg");
			
			dcj1.setYqjgpd(curbWeightData.getZbzlpd().toString());
			dcj1.setXh(xh);
			xh++;
			this.hibernateTemplate.save(dcj1);
		}
		
		//外廓尺寸测量报告
		if(outline!=null){
			DeviceCheckJudeg dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
			dcj1.setXh(xh);
			dcj1.setYqjyxm("外廓尺寸(mmxmmxmm)");
			dcj1.setYqjyjg(outline.getCwkc()+"x"+outline.getCwkk()+"x"+outline.getCwkg());
			
			if(vehCheckLogin.getJylb().equals("00")){
				dcj1.setYqbzxz("±1%或50mm");
			}else{
				dcj1.setYqbzxz("±2%或100mm");
			}
			dcj1.setYqjgpd(outline.getClwkccpd().toString());
			dcj1.setXh(xh);
			xh++;
			this.hibernateTemplate.save(dcj1);
		}
	}

	public void createExternalCheckJudge(VehCheckLogin vehCheckLogin) {
		ExternalCheck ec = (ExternalCheck) this.hibernateTemplate
				.find("from ExternalCheck where jylsh=? ", vehCheckLogin.getJylsh()).get(0);
		JSONObject jb = JSONObject.fromObject(ec);
		final String jylsh = vehCheckLogin.getJylsh();
		final String jyjgbh = vehCheckLogin.getJyjgbh();
		this.hibernateTemplate.execute(new HibernateCallback<Integer>() {
			@Override
			public Integer doInHibernate(Session session) throws HibernateException {
				return  session.createQuery("delete ExternalCheckJudge where jylsh=? and jyjgbh=?").setString(0, jylsh)
						.setString(1, jyjgbh).executeUpdate();
			}
		});
		
	//	List<ExternalCheck> externalChecks = (List<ExternalCheck>) this.hibernateTemplate.find("from ExternalCheck where jylsh=? ", jylsh);
		
		
		int i = 1;
		if (vehCheckLogin.getJyxm().indexOf("F1") >= 0) {
			for (; i <= 5; i++) {
				ExternalCheckJudge ecj = new ExternalCheckJudge();
				ecj.setJylsh(vehCheckLogin.getJylsh());
				ecj.setJycs(vehCheckLogin.getJycs());
				ecj.setJyjgbh(vehCheckLogin.getJyjgbh());
				ecj.setHphm(vehCheckLogin.getHphm());
				ecj.setHpzl(vehCheckLogin.getHpzl());
				ecj.setXh(i);
				int[] item =null;
				if (i == 1) {
					ecj.setRgjyxm("车辆唯一性检测");
					item = new int[] {1,2,3,4,5};
				} else if (i == 2) {
					ecj.setRgjyxm("车辆特征参数检查");
					/*if(externalChecks!=null&&!externalChecks.isEmpty()){
						ExternalCheck externalCheck =externalChecks.get(0);
						String item6 = externalCheck.getItem6();
						if(cs.equals("J")&&sf.equals("苏")){
							if(item6.equals("1")||item6.equals("2")){
								ecj.setRgjybz(externalCheck.getCwkc()+"*"+externalCheck.getCwkk()+"*"+externalCheck.getCwkg());
							}
						}
					}*/
					
					item = new int[] {6,7,8,9,10,11,12,13,14,15};
				} else if (i == 3) {
					ecj.setRgjyxm("车辆外观检查");
					item = new int[] {16,17,18,19,20,21};
				} else if (i == 4) {
					ecj.setRgjyxm("安全装置检查");
					item = new int[] {22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41};
				} else if (i == 5) {
					ecj.setRgjyxm("联网查询");
					item = new int[] {80}; 
				}
				List bhgList = getxm(item,jb,"2");
				List hgList = getxm(item,jb,"1");
				setPd(ecj, bhgList,hgList);
				this.hibernateTemplate.save(ecj);
			}
		}

		if (vehCheckLogin.getJyxm().indexOf("DC") >= 0) {
			ExternalCheckJudge ecj = new ExternalCheckJudge();
			ecj.setJylsh(vehCheckLogin.getJylsh());
			ecj.setJycs(vehCheckLogin.getJycs());
			ecj.setJyjgbh(vehCheckLogin.getJyjgbh());
			ecj.setHphm(vehCheckLogin.getHphm());
			ecj.setHpzl(vehCheckLogin.getHpzl());
			ecj.setXh(i);
			ecj.setRgjyxm("底盘动态检验");
			int[] item=new int[] {42,43,44,45};
			List bhgList = getxm(item,jb,"2");
			List hgList = getxm(item,jb,"1");
			setPd(ecj, bhgList,hgList);
			//ecj.setRgjgpd("1");
			this.hibernateTemplate.save(ecj);
			i++;
		}

		if (vehCheckLogin.getJyxm().indexOf("C1") >= 0) {

			ExternalCheckJudge ecj = new ExternalCheckJudge();
			ecj.setJylsh(vehCheckLogin.getJylsh());
			ecj.setJycs(vehCheckLogin.getJycs());
			ecj.setJyjgbh(vehCheckLogin.getJyjgbh());
			ecj.setHphm(vehCheckLogin.getHphm());
			ecj.setHpzl(vehCheckLogin.getHpzl());
			ecj.setXh(i);
			ecj.setRgjyxm("车辆底盘部件检查");
			int[] item=new int[] {46,47,48,49,50};
			List bhgList = getxm(item,jb,"2");
			List hgList = getxm(item,jb,"1");
			setPd(ecj, bhgList,hgList);
			//ecj.setRgjgpd("1");
			this.hibernateTemplate.save(ecj);
			i++;
		}
		
		if(!StringUtils.isEmpty(ec.getCwkc())&&!StringUtils.isEmpty(ec.getCwkk())&&!StringUtils.isEmpty(ec.getCwkg())) {
			ExternalCheckJudge ecj = new ExternalCheckJudge();
			ecj.setJylsh(vehCheckLogin.getJylsh());
			ecj.setJycs(vehCheckLogin.getJycs());
			ecj.setJyjgbh(vehCheckLogin.getJyjgbh());
			ecj.setHphm(vehCheckLogin.getHphm());
			ecj.setHpzl(vehCheckLogin.getHpzl());
			ecj.setXh(i);
			ecj.setRgjyxm("外廓尺寸人工测量(长x宽x高)");
			//setPd(ecj, bhgList,hgList);
			ecj.setRgjgpd("1");
			ecj.setRgjybz(ec.getCwkc()+"x"+ec.getCwkk()+"x"+ec.getCwkg());
			this.hibernateTemplate.save(ecj);
			i++;
			
		}
	}

	private void setPd(ExternalCheckJudge ecj, List bhgList,List hgList) {
		if(bhgList.size() > 0) {
			ecj.setRgjgpd("2");
		}else if(CollectionUtils.isEmpty(hgList)&&CollectionUtils.isEmpty(bhgList)) {
			ecj.setRgjgpd("0");
		}else {
			ecj.setRgjgpd("1");
		}
		StringBuffer sm = new  StringBuffer("");
		for (int a=0;a<bhgList.size();a++) {
			if(a != bhgList.size()-1) {
				sm.append(bhgList.get(a)).append(",");
			}else {
				sm.append(bhgList.get(a));
			}
		}
		ecj.setRgjysm(sm.toString());
	}
	
	public List getxm(int[] bh,JSONObject jo,String type) {
		List list = new ArrayList();
		for(int i=0;i<bh.length;i++) {
			Object o = jo.get("item"+bh[i]);
			if(o != null) {
				if(type.equals(o.toString())) {
					list.add(bh[i]);
				}
			}
		}
		return list;
	}

	public Integer createRoadCheckJudeg(final VehCheckLogin vehCheckLogin, Integer xh) {

		List<RoadCheck> roadChecks = (List<RoadCheck>) this.hibernateTemplate.find("from RoadCheck where jylsh=?",
				vehCheckLogin.getJylsh());

		if (roadChecks == null || roadChecks.isEmpty()) {
			return xh;
		} else {
			RoadCheck roadCheck = roadChecks.get(0);

			String cllx = vehCheckLogin.getCllx();
			Integer zzl = vehCheckLogin.getZzl();
			String zzly = vehCheckLogin.getZzly();

			if (vehCheckLogin.getJyxm().indexOf("R1") >= 0) {
				// 路试初速度
				DeviceCheckJudeg deviceCheckJudegLscsd = new DeviceCheckJudeg();
				setDeviceCheckJudeg(deviceCheckJudegLscsd, vehCheckLogin);
				deviceCheckJudegLscsd.setYqjyxm("制动初速度（km/h）");
				deviceCheckJudegLscsd.setYqjyjg(roadCheck.getZdcsd().toString());
				deviceCheckJudegLscsd.setYqjgpd(roadCheck.getLscsdpd().toString());
				deviceCheckJudegLscsd.setYqbzxz("≥" + roadCheck.getLscsdxz().toString());
				deviceCheckJudegLscsd.setXh(xh.intValue());
				deviceCheckJudegLscsd.setBz1("R");
				xh++;
				this.hibernateTemplate.save(deviceCheckJudegLscsd);

				// 协调时间
				DeviceCheckJudeg deviceCheckJudegXtsj = new DeviceCheckJudeg();
				setDeviceCheckJudeg(deviceCheckJudegXtsj, vehCheckLogin);
				deviceCheckJudegXtsj.setYqjyxm("制动协调时间(s)");
				deviceCheckJudegXtsj.setYqjyjg(roadCheck.getZdxtsj().toString());
				deviceCheckJudegXtsj.setYqjgpd(roadCheck.getLsxtsjpd().toString());
				deviceCheckJudegXtsj.setYqbzxz("≤" + roadCheck.getLsxtsjxz());
				deviceCheckJudegXtsj.setXh(xh.intValue());
				deviceCheckJudegXtsj.setBz1("R");
				xh++;
				this.hibernateTemplate.save(deviceCheckJudegXtsj);

				// 制动稳定性
				String zdwdx = "未跑偏";
				if (roadCheck.getZdwdx().equals("1")) {
					zdwdx = "未跑偏";
				} else if (roadCheck.getZdwdx().equals("2")) {
					zdwdx = "左跑偏";
				} else if (roadCheck.getZdwdx().equals("3")) {
					zdwdx = "右跑偏";
				}
				DeviceCheckJudeg deviceCheckJudegZdwdx = new DeviceCheckJudeg();
				setDeviceCheckJudeg(deviceCheckJudegZdwdx, vehCheckLogin);
				deviceCheckJudegZdwdx.setYqjyxm("制动稳定性");
				deviceCheckJudegZdwdx.setYqjyjg(zdwdx);
				deviceCheckJudegZdwdx.setYqjgpd(roadCheck.getZdwdx().equals("1") ? "1" : "2");
				deviceCheckJudegZdwdx.setYqbzxz("2.5m");
				deviceCheckJudegZdwdx.setXh(xh.intValue());
				deviceCheckJudegZdwdx.setBz1("R");
				xh++;
				this.hibernateTemplate.save(deviceCheckJudegZdwdx);

//				// 行车空载制动距离
//				DeviceCheckJudeg deviceCheckJudegKzzdjl = new DeviceCheckJudeg();
//				setDeviceCheckJudeg(deviceCheckJudegKzzdjl, vehCheckLogin);
//				deviceCheckJudegKzzdjl.setYqjyxm("空载制动距离(m)");
//				deviceCheckJudegKzzdjl.setYqjyjg(roadCheck.getXckzzdjl().toString());
//				deviceCheckJudegKzzdjl.setYqjgpd(roadCheck.getLskzzdjlpd().toString());
//				deviceCheckJudegKzzdjl.setYqbzxz("≤" + roadCheck.getLskzzdjlxz());
//				deviceCheckJudegKzzdjl.setXh(xh.intValue());
//				deviceCheckJudegKzzdjl.setBz1("R");
//				xh++;
//				this.hibernateTemplate.save(deviceCheckJudegKzzdjl);
				
				if(!StringUtils.isEmpty(roadCheck.getXckzmfdd())) {
					// 行车空载制动距离
					DeviceCheckJudeg deviceCheckJudegKzmfdd = new DeviceCheckJudeg();
					setDeviceCheckJudeg(deviceCheckJudegKzmfdd, vehCheckLogin);
					deviceCheckJudegKzmfdd.setYqjyxm("空载MFFDD(m/s<sup>2</sup>)");
					deviceCheckJudegKzmfdd.setYqjyjg(roadCheck.getXckzmfdd().toString());
					deviceCheckJudegKzmfdd.setYqjgpd(roadCheck.getLskzmfddpd().toString());
					deviceCheckJudegKzmfdd.setYqbzxz("≥" + roadCheck.getLskzmfddxz());
					deviceCheckJudegKzmfdd.setXh(xh.intValue());
					deviceCheckJudegKzmfdd.setBz1("R");
					xh++;
					this.hibernateTemplate.save(deviceCheckJudegKzmfdd);
				}

			}

			if (vehCheckLogin.getJyxm().indexOf("R2") >= 0) {
				// 驻车制动
				DeviceCheckJudeg deviceCheckJudegZcpd = new DeviceCheckJudeg();
				setDeviceCheckJudeg(deviceCheckJudegZcpd, vehCheckLogin);
				deviceCheckJudegZcpd.setYqjyxm("路试驻车" + (roadCheck.getZcpd() == 0 ? "20%" : "15%") + "坡道路试");
				deviceCheckJudegZcpd.setYqjyjg((roadCheck.getLszczdpd()==1 )? "2min未溜" : "2min内溜车");
				deviceCheckJudegZcpd.setYqjgpd(roadCheck.getLszczdpd().toString());
				deviceCheckJudegZcpd.setYqbzxz("正反≥2min");
				deviceCheckJudegZcpd.setXh(xh.intValue());
				deviceCheckJudegZcpd.setBz1("R");
				xh++;
				this.hibernateTemplate.save(deviceCheckJudegZcpd);
			}

			if (vehCheckLogin.getJyxm().indexOf("R3") >= 0) {
				DeviceCheckJudeg deviceCheckJudegcsb = new DeviceCheckJudeg();
				setDeviceCheckJudeg(deviceCheckJudegcsb, vehCheckLogin);
				deviceCheckJudegcsb.setYqjyxm("路试车速表");
				deviceCheckJudegcsb.setYqjyjg(roadCheck.getCsdscz().toString());
				deviceCheckJudegcsb.setYqjgpd(roadCheck.getCsbpd().toString());
				deviceCheckJudegcsb.setYqbzxz("32.8km/h ~ 40km/h");
				deviceCheckJudegcsb.setXh(xh.intValue());
				deviceCheckJudegcsb.setBz1("R");
				xh++;
				this.hibernateTemplate.save(deviceCheckJudegcsb);
			}

			return xh;
		}
	}

	private void setDeviceCheckJudeg(DeviceCheckJudeg deviceCheckJudeg, VehCheckLogin vehCheckLogin) {
		deviceCheckJudeg.setJylsh(vehCheckLogin.getJylsh());
		deviceCheckJudeg.setHphm(vehCheckLogin.getHphm());
		deviceCheckJudeg.setHpzl(vehCheckLogin.getHpzl());
		deviceCheckJudeg.setJycs(vehCheckLogin.getJycs());
		deviceCheckJudeg.setJyjgbh(vehCheckLogin.getJyjgbh());
	}
	
	public static boolean isInteger(String str) {  
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");  
        return pattern.matcher(str).matches();  
	}
	
	public static void main(String[] age) {
		System.out.println(MathRound1((float)(223*0.1/(1006*0.1*0.98)*100)));
	}

	private Integer createLightDataJudeg(final VehCheckLogin vehCheckLogin, Map<String, Object> flagMap, Integer xh) {
		List<LightData> lightDatas = (List<LightData>) this.hibernateTemplate.find(
				"from LightData where  jylsh=? and sjzt=? order by jycs desc", vehCheckLogin.getJylsh(),
				LightData.SJZT_ZC);

		String cllx = vehCheckLogin.getCllx();

		String syxz = vehCheckLogin.getSyxz();
		//光强度总和  项目
		String zgqxm = "左右外灯远光发光强度总和(cd)";
		//光强度总和  结果  
		String zgqjg = "";
		//光强度总和  标准限值
		String zgqxz = "430000";
		//光强度总和  判定
		String zgqpd = "";

		for (LightData lightData : lightDatas) {
			String jyxm = lightData.getJyxm();
			if (flagMap.get(jyxm + lightData.getGx()) == null) {
				if (lightData.getGx() == LightData.GX_YGD) {
					DeviceCheckJudeg dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
					dcj1.setYqjyxm(getLightGQ(jyxm) + "光强度(cd)");
					dcj1.setYqjyjg(lightData.getGq() == null ? "" : lightData.getGq().toString());
					dcj1.setYqbzxz(lightData.getGqxz() == null ? "" : "≥" + lightData.getGqxz().toString());
					dcj1.setYqjgpd(lightData.getGqpd() == null ? "" : lightData.getGqpd().toString());
					dcj1.setXh(xh);
					xh++;
					this.hibernateTemplate.save(dcj1);
					if(StringUtils.isEmpty(zgqjg)) {
						zgqjg = dcj1.getYqjyjg();
					}else {
						zgqjg = String.valueOf(Integer.parseInt(zgqjg) + Integer.parseInt(dcj1.getYqjyjg()));
					}
				}

				if (!((cllx.indexOf("K3") == 0 || cllx.indexOf("K4") == 0 || cllx.indexOf("N") == 0)
						&& syxz.equals("A"))) {
					if(lightData.getGx() == LightData.GX_JGD) {
						DeviceCheckJudeg dcj2 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
						dcj2.setYqjyxm(
								getLight(jyxm) + (lightData.getGx() == LightData.GX_YGD ? "远光灯" : "近光灯") + "垂直偏移量(mm/10m)");
						
						String czpc = lightData.getCzpc().toString().trim();
						if(isInteger(czpc)) {
							Integer intczpc =Integer.parseInt(czpc);
							if(intczpc>0) {
								czpc="+"+String.valueOf(intczpc);
							}else {
								czpc=String.valueOf(intczpc);
							}
						}
						
						
						String czpd = lightData.getCzpypd().toString();
						
						if("0".equals(czpd)) {
							czpd="3";
						}
						
						dcj2.setYqjyjg(lightData.getCzpc() == null ? "" : czpc);
						dcj2.setYqbzxz(lightData.getCzpyxz() == null ? "" : lightData.getCzpyxz().replace(",", "~"));
						dcj2.setYqjgpd(lightData.getCzpypd() == null ? "" : czpd);
						dcj2.setXh(xh);
						xh++;
						this.hibernateTemplate.save(dcj2);
					}
				}

			}

			flagMap.put(jyxm + lightData.getGx(), lightData);

		}
		
		List<BaseParams> gqzhpd =  BaseParamsUtil.getBaseParamsByType("gqzhpd");
		
		if(!CollectionUtils.isEmpty(gqzhpd)) {
			if("true".equals(gqzhpd.get(0).getParamValue())) {
				// 光强度总和
				if(!StringUtils.isEmpty(zgqjg)) {
					DeviceCheckJudeg zgq = createDeviceCheckJudegBaseInfo(vehCheckLogin);
					zgq.setYqjyxm(zgqxm);
					zgq.setYqjyjg(zgqjg);
					zgq.setYqbzxz("≤" + zgqxz);
					zgq.setYqjgpd(Integer.parseInt(zgqjg) > Integer.parseInt(zgqxz) ? "2":"1");
					zgq.setXh(xh);
					xh++;
					this.hibernateTemplate.save(zgq);
				}
			}
		}
		
	

		return xh;
	}

	private Integer createBrakRollerDateJudeg(final VehCheckLogin vehCheckLogin, Map<String, Object> flagMap,
			Integer xh) {
		List<BrakRollerData> brds = (List<BrakRollerData>) this.hibernateTemplate.find(
				"from BrakRollerData where jylsh=? and jyxm !='B0' and sjzt=? order by jycs desc",
				vehCheckLogin.getJylsh(), BrakRollerData.SJZT_ZC);

		for (BrakRollerData brd : brds) {
			if (flagMap.get(brd.getJyxm()) == null) {
				if(vehCheckLogin.getCllx().indexOf("N")!=-1&&brd.getJyxm().equals("B1")) {
					continue;
				}
				
				
				String temp = brd.getJyxm().indexOf("L")==0?"加载":"";
				
//				DeviceCheckJudeg dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
//				dcj1.setXh(xh);
//				dcj1.setYqjyxm(getZW(brd.getZw()) +temp+":"+ "制动率(%)/不平衡率(%)");
//				dcj1.setYqjyjg((brd.getKzxczdl() == null ? "" : brd.getKzxczdl().toString())+"/"+(brd.getKzbphl() == null ? "" : brd.getKzbphl().toString()));
//				dcj1.setYqbzxz((brd.getKzzdlxz() == null ? "" : "≥" + brd.getKzzdlxz().toString()+".0")+"/"+(brd.getBphlxz() == null ? "" : "≤" + brd.getBphlxz().toString()));
//				dcj1.setYqjgpd((brd.getKzzdlpd() == null ? "" : brd.getKzzdlpd().toString())+"/"+(brd.getKzbphlpd() == null ? "" : brd.getKzbphlpd().toString()));
//				dcj1.setXh(xh);
//				xh++;
//				
//				this.hibernateTemplate.save(dcj1);
				
				DeviceCheckJudeg dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
				dcj1.setXh(xh);
				dcj1.setYqjyxm(getZW(brd.getZw()) +temp+":"+ "制动率(%)");
				dcj1.setYqjyjg((brd.getKzxczdl() == null ? "" : brd.getKzxczdl().toString()));
				dcj1.setYqbzxz((brd.getKzzdlxz() == null ? "" : "≥" + brd.getKzzdlxz().toString()+".0"));
				dcj1.setYqjgpd((brd.getKzzdlpd() == null ? "" : brd.getKzzdlpd().toString()));
				dcj1.setXh(xh);
				xh++;
				
				this.hibernateTemplate.save(dcj1);
				

				DeviceCheckJudeg dcj2 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
				dcj2.setXh(xh);
				dcj2.setYqjyxm(getZW(brd.getZw()) +temp+ "不平衡率(%)");
				dcj2.setYqjyjg(brd.getKzbphl() == null ? "" : brd.getKzbphl().toString());
				dcj2.setYqbzxz(brd.getBphlxz() == null ? "" : "≤" + brd.getBphlxz().toString());
				dcj2.setYqjgpd(brd.getKzbphlpd() == null ? "" : brd.getKzbphlpd().toString());
				dcj2.setXh(xh);
				xh++;
				this.hibernateTemplate.save(dcj2);

			/*	if (brd.getJzzzdl() != null) {
					DeviceCheckJudeg dcj3 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
					dcj3.setXh(xh);
					dcj3.setYqjyxm(getZW(brd.getZw()) + "加载制动率(%)");
					dcj3.setYqjyjg(brd.getJzzzdl() == null ? "" : brd.getJzzzdl().toString());
					dcj3.setYqbzxz(brd.getJzzdlxz() == null ? "" : "≥" + brd.getJzzdlxz().toString());
					dcj3.setYqjgpd(brd.getJzzdlpd() == null ? "" : brd.getJzzdlpd().toString());
					dcj3.setXh(xh);
					xh++;
					this.hibernateTemplate.save(dcj3);

					DeviceCheckJudeg dcj4 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
					dcj4.setXh(xh);
					dcj4.setYqjyxm(getZW(brd.getZw()) + "加载不平衡率(%)");
					dcj4.setYqjyjg(brd.getJzbphl() == null ? "" : brd.getJzbphl().toString());
					dcj4.setYqbzxz(brd.getBphlxz() == null ? "" : "≤" + brd.getBphlxz().toString());
					dcj4.setYqjgpd(brd.getJzbphlpd() == null ? "" : brd.getJzbphlpd().toString());
					dcj4.setXh(xh);
					xh++;
					this.hibernateTemplate.save(dcj4);
				}*/
			}
			flagMap.put(brd.getJyxm(), brd);
		}
		return xh;
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
	
	public String getLightGQ(String jyxm) {

		if (jyxm.equals("H1")) {
			return "左外灯远光发";
		}
		if (jyxm.equals("H2")) {
			return "左辅灯";
		}
		if (jyxm.equals("H3")) {
			return "右辅灯";
		}
		if (jyxm.equals("H4")) {
			return "右外灯远光发";
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

	public String getReport4(String jylsh) {

		List<BrakRollerData> report4 = (List<BrakRollerData>) this.hibernateTemplate
				.find("from BrakRollerData where jylsh=?  and jyxm!=? order by id desc", jylsh, "B0");
		
		if(report4!=null&&!report4.isEmpty()){
			//Integer jycs = report4.get(0).getJycs();
			Map<String,Object> tempMap=new HashMap<String,Object>();
			for(BrakRollerData brakRollerData:report4){
				Integer jycs = brakRollerData.getJycs();
				String jyxm= brakRollerData.getJyxm();
				String key=jycs+"_"+jyxm;
				
				if(tempMap.get(key)==null){
					tempMap.put(key, brakRollerData);
				}
			}
		}

		List<SideslipData> sides = (List<SideslipData>) this.hibernateTemplate
				.find("from SideslipData  where jylsh=? order by id desc", jylsh);
		
//		if(sides!=null&&!sides.isEmpty()){
//			Integer jycs = sides.get(0).getJycs();
//			for(SideslipData side:sides){
//				if(side.getJycs()!=jycs){
//					sides.remove(side);
//				}
//			}
//		}

		JSONObject jo = new JSONObject();

		jo.put("zd", report4);

		jo.put("ch", sides);

		return jo.toString();
	}

	public static Float MathRound(Float f) {
		return (float) (Math.round(f * 100)) / 100;
	}

	public static Float MathRound1(Float f) {
		return (float) (Math.round(f * 10)) / 10;
	}

	public BrakRollerData getBrakRollerDataOfVehLoginInfo(VehCheckLogin vehCheckLogin, String jyxm) {

		List<BrakRollerData> datas = (List<BrakRollerData>) this.hibernateTemplate.find(
				"from BrakRollerData where jylsh=? and jycs=? and jyxm=?", vehCheckLogin.getJylsh(),
				vehCheckLogin.getJycs(), jyxm);

		if (datas == null || datas.isEmpty() || jyxm.equals("B0")) {
			return null;
		} else {
			return datas.get(0);
		}
	}
	
	public BrakRollerData getLastBrakRollerDataOfVehLoginInfo(VehCheckLogin vehCheckLogin, String jyxm) {

		List<BrakRollerData> datas = (List<BrakRollerData>) this.hibernateTemplate.find(
				"from BrakRollerData where jylsh=? and jyxm=? order by id desc", vehCheckLogin.getJylsh(), jyxm);

		if (datas == null || datas.isEmpty() || jyxm.equals("B0")) {
			return null;
		} else {
			return datas.get(0);
		}
	}

	public List<BrakRollerData> getBrakRollerDataB0(VehCheckLogin vehCheckLogin) {
		List<BrakRollerData> datas = (List<BrakRollerData>) this.hibernateTemplate.find(
				"from BrakRollerData where jylsh=? and jyxm='B0' and jycs=?", vehCheckLogin.getJylsh(),
				vehCheckLogin.getJycs());
		return datas;
	}
	
	
	public BrakRollerData getLastBrakRollerDataB0(VehCheckLogin vehCheckLogin) {
		List<BrakRollerData> datas = (List<BrakRollerData>) this.hibernateTemplate.find(
				"from BrakRollerData where jylsh=? and jyxm='B0' order by desc", vehCheckLogin.getJylsh(),
				vehCheckLogin.getJycs());
		if(CollectionUtils.isEmpty(datas)) {
			return null;
		}else {
			return datas.get(0);
		}
	}
	

	// 获取整车轴荷
	public Integer getZCZH(VehCheckLogin vehCheckLogin) {
		List<BrakRollerData> list = (List<BrakRollerData>) this.hibernateTemplate.find(
				"from BrakRollerData where jylsh=? and jyxm<>'B0' and jyxm<>'L1' and jyxm<>'L2' and jyxm<>'L3' and jyxm<>'L4'  and sjzt=? ", vehCheckLogin.getJylsh(),
				BrakRollerData.SJZT_ZC);
		// 整车轮荷
		Integer zclh = 0;
		for (BrakRollerData brakRollerData : list) {
			
			if(vehCheckLogin.getZs()>=3&&brakRollerData.getJzzlh()!=null&&brakRollerData.getJzylh()!=null) {
				zclh+=brakRollerData.getJzzlh()+brakRollerData.getJzylh();
			}else {
				zclh += brakRollerData.getZlh() + brakRollerData.getYlh();
			}
			
			
		}
		return zclh;
	}

	public void updateBrakRollerDataByJylsh(String jylsh) {

		VehCheckLogin vehCheckLogin = (VehCheckLogin) this.hibernateTemplate
				.find("from VehCheckLogin where jylsh =?", jylsh).get(0);

		List<BrakRollerData> datas = (List<BrakRollerData>) this.hibernateTemplate
				.find("from  BrakRollerData where jylsh = ?", jylsh);

		for (BrakRollerData brakRollerData : datas) {
			// 非驻车制动则计算检测结果
			if (!brakRollerData.getJyxm().equals("B0")) {
				// 空载行车制动率
				brakRollerData.setKzxczdl(vehCheckLogin);
				// 空载制动率限制及判定
				brakRollerData.setKzzdlxz(vehCheckLogin);
				brakRollerData.setKzzdlpd(vehCheckLogin);

				// 设置空载不平衡率
				brakRollerData.setKzbphl(vehCheckLogin);
				// 设置不平衡率限值
				brakRollerData.setBphlxz(vehCheckLogin);
				// 空载不平衡率判定
				brakRollerData.setKzbphlpd();

				brakRollerData.setJzzdl();
				// 加载制动率限制及判定
				brakRollerData.setJzzdlxz(vehCheckLogin);
				brakRollerData.setJzzdlpd();

				// 设置加载不平衡率
				brakRollerData.setJzbphl(vehCheckLogin);
				// 加载不平衡率判定
				brakRollerData.setJzbphlpd();
			}
			brakRollerData.setZpd();
		}

		List<LightData> lightDatas = (List<LightData>) this.hibernateTemplate.find("from  LightData where jylsh = ?",
				jylsh);

		for (LightData data : lightDatas) {
			data.setBaseDeviceData(vehCheckLogin, vehCheckLogin.getJycs(), data.getJyxm());
			data.setCzpy();
			// 设置灯光光强的判定限制
			data.setDgpdxz(vehCheckLogin);
			// 设置光强判定
			data.setGqpd();
			// 设置垂直偏移限值
			data.setCzpyxz(vehCheckLogin);
			data.setCzpypd();
			data.setZpd();
			this.saveData(data);
		}

		this.createOtherDataOfAnjian(vehCheckLogin.getJylsh());

	}

	public List<CheckPhoto> getCheckPhotos(String jylsh) {
		List<CheckPhoto> datas = (List<CheckPhoto>) this.hibernateTemplate.find(
				"select new CheckPhoto(id,jyjgbh,jcxdh,jylsh,hphm,hpzl,clsbdh, jycs,pssj,jyxm,zpzl) from CheckPhoto where jylsh=?",
				jylsh);

		return datas;
	}

	public CheckPhoto getCheckPhoto(String jylsh, String zpzl, Integer jycs) {
		List<CheckPhoto> datas = (List<CheckPhoto>) this.hibernateTemplate
				.find("from CheckPhoto where jylsh=? and zpzl=? and jycs=?", jylsh, zpzl, jycs);

		if (datas == null || datas.isEmpty()) {
			return null;
		}

		return datas.get(0);
	}

	public CheckPhoto getCheckPhoto(Integer id) {

		return this.hibernateTemplate.load(CheckPhoto.class, id);
	}

	public void saveCheckPhoto(final CheckPhoto checkPhoto) {

		this.hibernateTemplate.execute(new HibernateCallback<Integer>() {

			@Override
			public Integer doInHibernate(Session session) throws HibernateException {
				return session.createQuery("delete CheckPhoto where jylsh=? and zpzl=? ")
						.setParameter(0, checkPhoto.getJylsh()).setParameter(1, checkPhoto.getZpzl()).executeUpdate();
			}
		});

		this.hibernateTemplate.save(checkPhoto);

		logger.info("检验流水号：" + checkPhoto.getJylsh() + ",检验次数：" + checkPhoto.getJycs() + "，检验项目：" + checkPhoto.getJyxm()
				+ ",照片种类：" + checkPhoto.getZpzl());

		checkEventManger.createEvent(checkPhoto.getJylsh(), checkPhoto.getJycs(), "18C63", checkPhoto.getJyxm(),
				checkPhoto.getHphm(), checkPhoto.getHpzl(), checkPhoto.getClsbdh(), checkPhoto.getZpzl(),0);

	}

	public void deleteImage(Integer id) {

		CheckPhoto cp = new CheckPhoto();
		cp.setId(id);
		this.hibernateTemplate.delete(cp);

	}
	
	public void saveOrUpdateProcess(VehCheckProcess vehCheckProcess) {
		this.hibernateTemplate.saveOrUpdate(vehCheckProcess); 
	}

	public VehCheckProcess getVehCheckProces(String jylsh, Integer jycs, String jyxm) {
		List<VehCheckProcess> data = (List<VehCheckProcess>) this.hibernateTemplate
				.find("from VehCheckProcess where jylsh=? and jycs=?  and jyxm=?", jylsh, jycs, jyxm);

		if (data != null && !data.isEmpty()) {
			return data.get(0);
		}
		return null;

	}

	public List<CheckLog> getCheckLogs(String jylsh) {
		List<CheckLog> data = (List<CheckLog>) this.hibernateTemplate.find("from CheckLog where jylsh=?", jylsh);
		return data;
	}

	public List<CheckEvents> getCheckEvents(String jylsh) {
		List<CheckEvents> data = (List<CheckEvents>) this.hibernateTemplate.find("from CheckEvents where jylsh=?",
				jylsh);
		return data;
	}

	public List<VehCheckProcess> getVehCheckProcess(String jylsh) {
		List<VehCheckProcess> data = (List<VehCheckProcess>) this.hibernateTemplate
				.find("from VehCheckProcess where jylsh=?", jylsh);
		return data;
	}

	public void updateProcess(VehCheckProcess vehCheckProcess) {
		this.hibernateTemplate.update(vehCheckProcess);
	}
	
	public void saveProcess(VehCheckProcess vehCheckProcess) {
		this.hibernateTemplate.save(vehCheckProcess);
	}

	public void createCheckEventOnLine(String jylsh, Integer jycs) {

		List<VehCheckProcess> data = (List<VehCheckProcess>) this.hibernateTemplate.find(
				"from VehCheckProcess where jylsh=? and jycs=? and jyxm in('H1','H2','H3','H4','B0','B1','B2','B3','B4','B5','A1','S1','L1','L2','L3','L4')",
				jylsh, jycs);
		
		VehCheckLogin vehCheckLogin = this.getVehCheckLogin(jylsh);

		for (VehCheckProcess vp : data) {
			checkEventManger.createEvent(vp.getJylsh(), vp.getJycs(), "18C55", vp.getJyxm(), vp.getHphm(), vp.getHpzl(),
					vp.getClsbdh(),vehCheckLogin.getVehcsbj());
			checkEventManger.createEvent(vp.getJylsh(), vp.getJycs(), "18C81", vp.getJyxm(), vp.getHphm(), vp.getHpzl(),
					vp.getClsbdh(),vehCheckLogin.getVehcsbj());
			checkEventManger.createEvent(vp.getJylsh(), vp.getJycs(), "18C58", vp.getJyxm(), vp.getHphm(), vp.getHpzl(),
					vp.getClsbdh(),vehCheckLogin.getVehcsbj());
		}
	}

	public List getExternalCheckJudge(final String jylsh) {
		List<Map> list = this.hibernateTemplate.execute(new HibernateCallback<List>() {
			@Override
			public List<Map> doInHibernate(Session session) throws HibernateException {

				return session
						.createSQLQuery(
								"select xh,rgjyxm,rgjgpd,rgjysm,rgjybz from TM_ExternalCheckJudge where jylsh=?")
						.setString(0, jylsh).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();

			}
		});
		return list;
	}

	public List getDeviceCheckJudeg(final String jylsh) {
		List<Map> list = this.hibernateTemplate.execute(new HibernateCallback<List>() {
			@Override
			public List<Map> doInHibernate(Session session) throws HibernateException {
				return session
						.createSQLQuery(
								"select xh,yqjyxm,yqjyjg,yqbzxz,yqjgpd,yqjybz from TM_DeviceCheckJudeg where jylsh=?")
						.setString(0, jylsh).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
			}
		});
		return list;
	}

	public void saveInsurance(Insurance insurance) {

		this.hibernateTemplate.saveOrUpdate(insurance);
		
		VehCheckLogin vehCheckLogin =  this.getVehCheckLogin(insurance.getJylsh());
		
		checkEventManger.createEvent(insurance.getJylsh(), null, "18C61", null, insurance.getHphm(),
				insurance.getHpzl(), insurance.getClsbdh(),vehCheckLogin.getVehcsbj());
		checkEventManger.createEvent(insurance.getJylsh(), null, "18C64", null, insurance.getHphm(),
				insurance.getHpzl(), insurance.getClsbdh(),vehCheckLogin.getVehcsbj());
	}
	
	public void saveOrUpdateInsurance(Insurance insurance) {

		this.hibernateTemplate.saveOrUpdate(insurance);
	}

	public Insurance getInsurance(String jylsh) {

		List<Insurance> insurances = (List<Insurance>) this.hibernateTemplate.find("from Insurance where jylsh=?",
				jylsh);

		if (insurances != null && !insurances.isEmpty()) {
			return insurances.get(0);
		}

		return null;
	}

	public VehCheckLogin getVehCheckLogin(String jylsh) {

		List<VehCheckLogin> list = (List<VehCheckLogin>) this.hibernateTemplate.find("from VehCheckLogin where jylsh=?",
				jylsh);
		if (list != null && !list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}
	
	public void resetEventState(final String jylsh){
		 
		List<CheckEvents> events =  (List<CheckEvents>) this.hibernateTemplate.find("from CheckEvents where jylsh=? and event in ('18C81','18C55','18C80','18C58','18C59')", jylsh);
		 
		for(CheckEvents event: events) {
			List<CheckLog> logs =null;
			if(event.getEvent().equals(RCAConstant.V18C59)) {
				logs  = (List<CheckLog>) this.hibernateTemplate.find("from CheckLog where code='1' and jylsh=? and jycs=? and jkbmc=?", jylsh,event.getJycs(),event.getEvent());
			}else {
				logs  = (List<CheckLog>) this.hibernateTemplate.find("from CheckLog where code='1' and jylsh=? and jycs=? and jkbmc=?", jylsh,event.getJycs(),event.getEvent()+"_"+event.getJyxm());
			}
			if(!CollectionUtils.isEmpty(logs)) {
				this.hibernateTemplate.delete(event);
			}
		}
		
		this.hibernateTemplate.execute(new HibernateCallback<Integer>() {

			@Override
			public Integer doInHibernate(Session session) throws HibernateException {
				
				return session.createQuery("update CheckEvents set state=0 where jylsh=? ").setParameter(0, jylsh).executeUpdate();
				
			}
		});
		
	}
	
	/**
	 * 检测过程开始发送消息
	 * @param hphm
	 * @param jyxm
	 * @throws IOException
	 */
	public void displaySendMsg(String hphm,String jyxm,Integer jcxdh) throws IOException {
		String zh_jyxm = getJyxm_zh(jyxm);
		// 获取显示屏
		String deviceIds = getDeviceId(jyxm);
		
		if(StringUtils.isEmpty(deviceIds)) {
			return ;
		}
		
		String[] deviceIdArray = deviceIds.split(",");
		String deviceId  = deviceIdArray[jcxdh-1];

		if (!"".equals(deviceId)) {
			Device device = new Device();
			device.setId(Integer.parseInt(deviceId));
			SimpleRead sr = (SimpleRead) servletContext.getAttribute(device.getThredKey());
			// 根据设备id判断是否是显示屏
			if (sr instanceof DeviceDisplay) {
				DeviceDisplay display = (DeviceDisplay) sr;
				display.sendMessage(zh_jyxm + " 检测中", DeviceDisplay.XP);
				display.sendMessage(hphm, DeviceDisplay.SP);
			}
		}
		
	}
	
	//@Async
	public void processEndSendMsg(String hphm,String jyxm,Integer jcxdh) throws IOException, InterruptedException {
		
		String zh_jyxm = getJyxm_zh(jyxm);
		// 获取显示屏
		String deviceIds = getDeviceId(jyxm);
		
		if(StringUtils.isEmpty(deviceIds)) {
			return ;
		}
		
		String[] deviceIdArray = deviceIds.split(",");
		String deviceId  = deviceIdArray[jcxdh-1];

		if (!"".equals(deviceId)) {
			Device device = new Device();
			device.setId(Integer.parseInt(deviceId));
			SimpleRead sr = (SimpleRead) servletContext.getAttribute(device.getThredKey());
			// 根据设备id判断是否是显示屏
			if (sr instanceof DeviceDisplay) {
				DeviceDisplay display = (DeviceDisplay) sr;
				display.sendMessage(zh_jyxm + " 检测结束", DeviceDisplay.XP);
				display.sendMessage(hphm, DeviceDisplay.SP);
				
				this.deviceManager.asySetDefault(display);
				
			}
		}
	}
	
	public void savePlateApplyTable(PlateApplyTable plateApplyTable) {
		
		this.hibernateTemplate.saveOrUpdate(plateApplyTable);
		
	}
	
	public PlateApplyTable getPlateApplyTable(String jylsh) {
		
		List<PlateApplyTable> datas = (List<PlateApplyTable>) this.hibernateTemplate.find("from PlateApplyTable where jylsh=?", jylsh);
		
		if(!CollectionUtils.isEmpty(datas)) {
			return datas.get(0);
		}
		
		return null;
		
	}
	

	private String getDeviceId(String jyxm) {
		List<BaseParams> bps = (List<BaseParams>) servletContext.getAttribute("bps");
		String deviceId = "";
		for (BaseParams param : bps) {
			if (param.getType().equals("wjdisplay") && param.getParamName().equals(jyxm)) {
				deviceId = param.getParamValue();
			}
		}
		return deviceId;
	}

	private String getJyxm_zh(String jyxm) {
		String zh_jyxm = "";
		if ("DC".equals(jyxm)) {
			zh_jyxm = "动态底盘";
		} else if ("F1".equals(jyxm)) {
			zh_jyxm = "外检";
		} else if ("C1".equals(jyxm)) {
			zh_jyxm = "底盘";
		}
		return zh_jyxm;
	}
	
	
	public void saveReloadVideo(String strIds) {
		
		String[] ids=strIds.split(",");
		
		for(String id:ids) {
			VehCheckProcess vehCheckProcess = this.hibernateTemplate.load(VehCheckProcess.class, Integer.parseInt(id));
			vehCheckProcess.setVoideSate(0);
			this.hibernateTemplate.save(vehCheckProcess);
		}
	}
	
	public void updateTaskPaice(TaskPicture taskPicture) {
		
		this.hibernateTemplate.update(taskPicture);
		
	}
	
	public void deleteTaskPaice(TaskPicture taskPicture) {
		this.hibernateTemplate.delete(taskPicture);
	}
	
	public void deleteIamge(final String jylsh,final String zpzl) {
		
		this.hibernateTemplate.execute(new HibernateCallback<Integer>() {
			@Override
			public Integer doInHibernate(Session session) throws HibernateException {
				return session.createQuery("delete CheckPhoto  where jylsh=? and zpzl=? ").setParameter(0, jylsh)
						.setParameter(1, zpzl).executeUpdate();
				
			}
		});
		
	}
	
	public Outline getOutLine(String jylsh){
		List<Outline> outlines=(List<Outline>) this.hibernateTemplate.find("from Outline where jylsh=? order by id desc", jylsh);
		if(!CollectionUtils.isEmpty(outlines)){
			
			return outlines.get(0);
		}else {
			return null;
		}
	}
	
	
	
	
	
	
	
	
}
