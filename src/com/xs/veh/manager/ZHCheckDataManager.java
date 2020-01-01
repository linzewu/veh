package com.xs.veh.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.xs.common.MyHibernateTemplate;
import com.xs.veh.entity.DeviceCheckJudegZJ;
import com.xs.veh.entity.RoadCheck;
import com.xs.veh.entity.TestResult;
import com.xs.veh.entity.TestVeh;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.network.data.BrakRollerData;
import com.xs.veh.network.data.CurbWeightData;
import com.xs.veh.network.data.LightData;
import com.xs.veh.network.data.OtherInfoData;
import com.xs.veh.network.data.Outline;
import com.xs.veh.network.data.ParDataOfAnjian;
import com.xs.veh.network.data.SideslipData;
import com.xs.veh.network.data.SpeedData;
import com.xs.veh.network.data.SuspensionData;
import com.xs.veh.network.data.VolumeData;

@Service
public class ZHCheckDataManager {
	
	Logger logger = Logger.getLogger(CheckDataManager.class);

	// 判定结果 未检
	public static final Integer PDJG_WJ = 0;

	// 合格
	public static final Integer PDJG_HG = 1;

	// 不合格
	public static final Integer PDJG_BHG = 2;

	@Resource(name = "hibernateTemplate")
	private MyHibernateTemplate hibernateTemplate;
	
	@Value("${jyjgmc}")
	private String jyjgmc;
	
	@Value("${jyjgbh}")
	private String jyjgbh;
	
	@Value("${sf}")
	private String sf;
	
	@Value("${cs}")
	private String cs;
	
	@Autowired
	private VehManager vehManager;
	
	@Autowired
	private CheckDataManager checkDataManager;
	
	
	public TestVeh getTestVehbyJylsh(String jylsh) {
		
		List<TestVeh> datas = (List<TestVeh>) this.hibernateTemplate.find("from TestVeh where jylsh=?", jylsh);
		
		
		if(!CollectionUtils.isEmpty(datas)) {
			return datas.get(0);
		}
		return null;
		
	}
	
	@Scheduled(fixedDelay = 2000)
	public void updateTestResultBystateIsNull() {
		
		final String sql ="select a.* from QCPFWQ2018.dbo.TestResult a left join QCPFWQ2018.dbo.TestVeh b on a.jylsh=b.jylsh  where a.status is null and charindex('S1',b.jcxm)>0 and CSB_SCZ is not null";
		
		List<Map> datas = (List<Map>) this.hibernateTemplate.execute(new HibernateCallback<List<Map>>() {
			@Override
			public List<Map> doInHibernate(Session session) throws HibernateException {
				List<Map> datas = (List<Map>) session.createSQLQuery(sql).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
				return datas;
			}
		});
		
		
		for(Map testResultMap:datas) {
			SpeedData speedData = new SpeedData();
			speedData.setSpeed(Float.valueOf(testResultMap.get("csb_scz").toString()));
			//速度限值
			speedData.setSdxz();
			//速度判定
			speedData.setSdpd();
			speedData.setZpd();
			
			VehCheckLogin cvl = vehManager.getVehCheckLoginByJylsh(jyjgbh, testResultMap.get("jylsh").toString());
			if(cvl!=null) {
				speedData.setBaseDeviceData(cvl, 1, "S1");
				this.hibernateTemplate.save(speedData);
			}
			TestResult testResult = this.hibernateTemplate.load(TestResult.class, Integer.parseInt(testResultMap.get("id").toString()));
			testResult.setStatus(1);
			this.hibernateTemplate.saveOrUpdate(testResult);
			
		}
		
		
	}
	
	
	public Map<String, Object> getHData(String jylsh,Integer jycs){
		List<LightData> lightDatas = (List<LightData>) this.hibernateTemplate
				.find("from LightData where jylsh=? and jyxm like 'H%' order by id asc", jylsh);
		
		Map<String, Object> lightMapData = checkDataManager.getLightDatasOfJycs(lightDatas,jycs);
		
		return lightMapData;
		
	}
	
	public  Map<String, Object> getS1Data(String jylsh,Integer jycs){
		
		List<SpeedData> sds = (List<SpeedData>) this.hibernateTemplate
				.find("from SpeedData where jylsh=? order by id desc", jylsh);
		
		Map s1map=new HashMap<String, Object>();
		
		if (sds != null && !sds.isEmpty()) {
			SpeedData speedData = sds.get(0);
			int speedJycs=speedData.getJycs();
			if(speedJycs>jycs){
				speedJycs=jycs;
			}
			for(SpeedData sd:sds){
				
				if(sd.getJycs()==speedJycs){
					s1map.put("s1", sd);
				}
			}
		}
		
		return s1map;
	}
	
	public  Map<String, Object> getAData(String jylsh,Integer jycs){
		
		List<SideslipData> sids = (List<SideslipData>) this.hibernateTemplate
				.find("from SideslipData where jylsh=? and sjzt=? order by id desc", jylsh, SideslipData.SJZT_ZC);
		Map aMap=new HashMap<String, Object>();
		if (sids != null && !sids.isEmpty()) {
			SideslipData sideslipData = sids.get(0);
			int sideslipJycs=sideslipData.getJycs();
			if(sideslipJycs>jycs){
				sideslipJycs=jycs;
			}
			for(SideslipData sid:sids){
				if(sid.getJycs()==sideslipJycs){
					if(sid.getZxzs()==1||sid.getZxzs()==null) {
						aMap.put("a1", sid);
					}
					if(sid.getZxzs()==2) {
						aMap.put("a2", sid);
					}
				}
			}
		}
		return aMap;
	}
	
	public  Map<String, Object> getBData(VehCheckLogin vehCheckLogin,Integer jycs){
		List<BrakRollerData> brds = (List<BrakRollerData>) this.hibernateTemplate
				.find("from BrakRollerData where jylsh=?  order by id desc ", vehCheckLogin.getJylsh());
		
		OtherInfoData otherData=new OtherInfoData();
		
		Map data=new HashMap<String, Object>();
		
		for (BrakRollerData brd : brds) {
			int zdjycs=brd.getJycs();
			if(zdjycs>jycs){
				zdjycs=jycs;
			}
			
			if (!brd.getJyxm().equals("B0")) {
				String key =  brd.getJyxm().toLowerCase();
				if (data.get(key) == null&&zdjycs==brd.getJycs()) {
					data.put(key, brd);
					if(brd.getJyxm().contains("B")){
						int zdlh=otherData.getZdlh()==null?0:otherData.getZdlh();
						int zczbzl=otherData.getJczczbzl()==null?0:otherData.getJczczbzl();
						otherData.setJczczbzl(zczbzl+brd.getZlh()+brd.getYlh());
						otherData.setZdlh(zdlh+brd.getZzdl()+brd.getYzdl());
					}
					
				}
			} else {
				String key =  brd.getJyxm().toLowerCase() + "_" + brd.getZw();
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
		
		
		List<ParDataOfAnjian> plist = (List<ParDataOfAnjian>) this.hibernateTemplate.find("from ParDataOfAnjian where jylsh=? order by id desc ", vehCheckLogin.getJylsh());
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
		
		return data;

	}
	
	
	public  Map<String, Object> getRData(VehCheckLogin vehCheckLogin,Integer jycs){
		List<RoadCheck> lsys = (List<RoadCheck>) this.hibernateTemplate.find("from RoadCheck where jylsh=? order by id desc", vehCheckLogin.getJylsh());
		Map data=new HashMap<String, Object>();
		if (lsys != null && !lsys.isEmpty()) {
			data.put("lsy", lsys.get(0));
		}
		return data;
	}
	
	
	public  Map<String, Object> getDLXData(VehCheckLogin vehCheckLogin,Integer jycs){
		List<TestResult> dlxList = (List<TestResult>) this.hibernateTemplate.find("from TestResult where jylsh=? order by id desc", vehCheckLogin.getJylsh());
		Map data=new HashMap<String, Object>();
		if (dlxList != null && !dlxList.isEmpty()) {
			data.put("dlx", dlxList.get(0));
		}
		return data;
	}
	
	
	public  Map<String,Map<String,Object>> getPFXData(final VehCheckLogin vehCheckLogin){
		
		return hibernateTemplate.execute(new HibernateCallback<Map<String,Map<String,Object>>>() {
			@Override
			public Map<String,Map<String,Object>> doInHibernate(Session session) throws HibernateException {
				
				Calendar c=Calendar.getInstance();
				c.setTime(vehCheckLogin.getDlsj());
				c.add(Calendar.MONTH, 1);
				
				SQLQuery wtQuery = session.createSQLQuery("select * from QCPFWQ2018.dbo.ASMCLSJB where JCRQ>? and JCRQ<? and cphm=? and CPYS=?");
				wtQuery.setParameter(0, vehCheckLogin.getDlsj())
				.setParameter(1, c.getTime()).setParameter(2, vehCheckLogin.getHphm())
				.setParameter(3, getCpysByhpzl(vehCheckLogin.getHpzl()));
				wtQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
				List<Map<String,Object>> wtList  =  wtQuery.list();
				Map<String,Object> wtMap=null;
				if(!CollectionUtils.isEmpty(wtList)) {
					wtMap= wtList.get(wtList.size()-1);
				}
				
				
				SQLQuery sdsQuery = session.createSQLQuery("select * from  QCPFWQ2018.dbo.SDSCLSJB where JCRQ>? and JCRQ<? and cphm=? and CPYS=?");
				sdsQuery.setParameter(0, vehCheckLogin.getDlsj())
				.setParameter(1, c.getTime()).setParameter(2, vehCheckLogin.getHphm())
				.setParameter(3, getCpysByhpzl(vehCheckLogin.getHpzl()));
				sdsQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
				List<Map<String,Object>> sdsList  =  sdsQuery.list();
				Map<String,Object> sdsMap=null;
				if(!CollectionUtils.isEmpty(sdsList)) {
					sdsMap= sdsList.get(sdsList.size()-1);
				}
				
				SQLQuery lgdQuery = session.createSQLQuery("select * from  QCPFWQ2018.dbo.lgdclsjb where JCRQ>? and JCRQ<? and cphm=? and CPYS=?");
				lgdQuery.setParameter(0, vehCheckLogin.getDlsj())
					.setParameter(1, c.getTime()).setParameter(2, vehCheckLogin.getHphm())
					.setParameter(3, getCpysByhpzl(vehCheckLogin.getHpzl()));
				lgdQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
				List<Map<String,Object>> lgdList  =  lgdQuery.list();
				Map<String,Object> lgdMap=null;
				if(!CollectionUtils.isEmpty(lgdList)) {
					lgdMap= lgdList.get(lgdList.size()-1);
				}
				
				SQLQuery ydQuery = session.createSQLQuery("select * from  QCPFWQ2018.dbo.YDCLSJB where JCRQ>? and JCRQ<? and cphm=? and CPYS=?");
				ydQuery.setParameter(0, vehCheckLogin.getDlsj())
					.setParameter(1, c.getTime()).setParameter(2, vehCheckLogin.getHphm())
					.setParameter(3, getCpysByhpzl(vehCheckLogin.getHpzl()));
				ydQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
				List<Map<String,Object>> ydList  =  ydQuery.list();
				Map<String,Object> ydMap=null;
				if(!CollectionUtils.isEmpty(ydList)) {
					ydMap= ydList.get(ydList.size()-1);
				}
				
				return getData(wtMap, sdsMap, lgdMap, ydMap);
			}
			
			private Map<String,Map<String,Object>> getData(Map<String,Object> wtMap ,Map<String,Object> sdsMap,
					Map<String,Object> lgdMap,Map<String,Object> ydMap) {
				
				List<Long> list=new ArrayList<Long>();
				
				Map<String,Map<String,Map<String,Object>>> data =new HashMap<String, Map<String,Map<String,Object>>>();
				
				if(!CollectionUtils.isEmpty(wtMap)) {
					Date jcrq = (Date)wtMap.get("jcrq");
					list.add(jcrq.getTime());
					Map<String,Map<String,Object>> subData =new HashMap<String, Map<String,Object>>();
					subData.put("wt",wtMap);
					data.put(String.valueOf(jcrq.getTime()),subData);
				}
				
				if(!CollectionUtils.isEmpty(sdsMap)) {
					Date jcrq = (Date)sdsMap.get("jcrq");
					list.add(jcrq.getTime());
					
					Map<String,Map<String,Object>> subData =new HashMap<String, Map<String,Object>>();
					subData.put("sds",sdsMap);
					
					data.put(String.valueOf(jcrq.getTime()),subData);
				}
				
				if(!CollectionUtils.isEmpty(lgdMap)) {
					Date jcrq = (Date)lgdMap.get("jcrq");
					list.add(jcrq.getTime());
					
					Map<String,Map<String,Object>> subData =new HashMap<String, Map<String,Object>>();
					subData.put("lgd",lgdMap);
					
					data.put(String.valueOf(jcrq.getTime()),subData);
				}
				
				if(!CollectionUtils.isEmpty(ydMap)) {
					Date jcrq = (Date)ydMap.get("jcrq");
					list.add(jcrq.getTime());
					Map<String,Map<String,Object>> subData =new HashMap<String, Map<String,Object>>();
					subData.put("yd",ydMap);
					data.put(String.valueOf(jcrq.getTime()),subData);
				}
				
				if(!CollectionUtils.isEmpty(list)) {
					Collections.sort(list);
					String key = String.valueOf(list.get(list.size()-1));
					return data.get(key);
				}
				return null;
			}
		
			
		});
	}
	
	
	public  Map<String, Object> getXJData(VehCheckLogin vehCheckLogin,Integer jycs){
		List<SuspensionData> xjList = (List<SuspensionData>) this.hibernateTemplate.find("from SuspensionData where jylsh=? order by id desc", vehCheckLogin.getJylsh());
		
		Map data=new HashMap<String, Object>();
		if (xjList != null && !xjList.isEmpty()) {
			data.put("xj"+xjList.get(0).getZs(), xjList.get(0));
			data.put("xj"+xjList.get(1).getZs(), xjList.get(1));
		}
		return data;
	}
	
	
	public  Map<String, Object> getSJJData(VehCheckLogin vehCheckLogin,Integer jycs){
		List<VolumeData> sjjList = (List<VolumeData>) this.hibernateTemplate.find("from VolumeData where jylsh=? order by id desc", vehCheckLogin.getJylsh());
		
		Map data=new HashMap<String, Object>();
		if (sjjList != null && !sjjList.isEmpty()) {
			data.put("sjj", sjjList.get(0));
		}
		return data;
	}
	
	
	public String getCpysByhpzl(String hpzl) {
		
		if(hpzl.equals("01")||hpzl.equals("15")||hpzl.equals("07")||hpzl.equals("16")||hpzl.equals("17")) {
			return "黄";
		}
		
		if(hpzl.equals("02")||hpzl.equals("08")) {
			return "蓝";
		}
		return "";
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
			DeviceCheckJudegZJ dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
			dcj1.setYqjyxm("驻车制动率(%)");
			dcj1.setYqjyjg(parDataOfAnjian.getTczdl() == null ? "" : parDataOfAnjian.getTczdl().toString());
			dcj1.setYqbzxz(parDataOfAnjian.getTczdxz() == null ? "" : "≥" + parDataOfAnjian.getTczdxz());
			dcj1.setYqjgpd(parDataOfAnjian.getTczdpd() == null ? "" : parDataOfAnjian.getTczdpd().toString());
			dcj1.setXh(xh);
			xh++;
			this.hibernateTemplate.save(dcj1);
		}
		// 整车制动率判定
		if (otherInfoData != null) {
			DeviceCheckJudegZJ dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
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
			DeviceCheckJudegZJ dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
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
			DeviceCheckJudegZJ dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
			dcj1.setYqjyxm("车速表指示误差(km/h)");
			dcj1.setYqjyjg(speedData.getSpeed() == null ? "" : speedData.getSpeed().toString());
			dcj1.setYqbzxz(speedData.getSdxz().replace(",", "~"));
			dcj1.setYqjgpd(speedData.getSdpd() == null ? "" : speedData.getSdpd().toString());
			dcj1.setXh(xh);
			xh++;
			this.hibernateTemplate.save(dcj1);
		}
		
		CurbWeightData curbWeightData = this.vehManager.getLastCurbWeightDataOfJylsh(vehCheckLogin.getJylsh());

		if (vehCheckLogin.getJylb().equals("00") && curbWeightData != null&&vehCheckLogin.getJyxm().indexOf("Z1")>=0) {
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
			
			DeviceCheckJudegZJ dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
			dcj1.setXh(xh);
			dcj1.setYqjyxm("整备质量(KG)");
			dcj1.setYqjyjg(curbWeightData.getZbzl()==null ? "" : curbWeightData.getZbzl().toString());
			dcj1.setYqbzxz(temp1+xzgj+"KG");
			dcj1.setYqjgpd(curbWeightData.getZbzlpd().toString());
			dcj1.setXh(xh);
			xh++;
			this.hibernateTemplate.save(dcj1);
		}
		
		//外廓尺寸测量报告
		if(outline!=null){
			DeviceCheckJudegZJ dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
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
					DeviceCheckJudegZJ dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
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
					//if(lightData.getGx() == LightData.GX_JGD) {
						DeviceCheckJudegZJ dcj2 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
						dcj2.setYqjyxm(
								getLight(jyxm) + (lightData.getGx() == LightData.GX_YGD ? "远光灯" : "近光灯") + "垂直偏移(mm/10m)量");
						
						String czpc = lightData.getCzpc().toString().trim();
						if(isInteger(czpc)) {
							Integer intczpc =Integer.parseInt(czpc);
							if(intczpc>0) {
								czpc="+"+String.valueOf(intczpc);
							}else {
								czpc=String.valueOf(intczpc);
							}
						}
						dcj2.setYqjyjg(lightData.getCzpc() == null ? "" : czpc);
						dcj2.setYqbzxz(lightData.getCzpyxz() == null ? "" : lightData.getCzpyxz().replace(",", "~"));
						dcj2.setYqjgpd(lightData.getCzpypd() == null ? "" : lightData.getCzpypd().toString());
						dcj2.setXh(xh);
						xh++;
						this.hibernateTemplate.save(dcj2);
					//}
				}

			}

			flagMap.put(jyxm + lightData.getGx(), lightData);

		}
		
		// 光强度总和
//		if(!StringUtils.isEmpty(zgqjg)) {
//			DeviceCheckJudeg zgq = createDeviceCheckJudegBaseInfo(vehCheckLogin);
//			zgq.setYqjyxm(zgqxm);
//			zgq.setYqjyjg(zgqjg);
//			zgq.setYqbzxz("≤" + zgqxz);
//			zgq.setYqjgpd(Integer.parseInt(zgqjg) > Integer.parseInt(zgqxz) ? "2":"1");
//			zgq.setXh(xh);
//			xh++;
//			this.hibernateTemplate.save(zgq);
//		}

		return xh;
	}
	
	
	private DeviceCheckJudegZJ createDeviceCheckJudegBaseInfo(VehCheckLogin vehCheckLogin) {
		DeviceCheckJudegZJ dcj = new DeviceCheckJudegZJ();
		dcj.setHphm(vehCheckLogin.getHphm());
		dcj.setHpzl(vehCheckLogin.getHpzl());
		dcj.setJycs(vehCheckLogin.getJycs());
		dcj.setJyjgbh(vehCheckLogin.getJyjgbh());
		dcj.setJylsh(vehCheckLogin.getJylsh());
		return dcj;
	}
	
	public static boolean isInteger(String str) {  
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");  
        return pattern.matcher(str).matches();  
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
				DeviceCheckJudegZJ deviceCheckJudegLscsd = new DeviceCheckJudegZJ();
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
				DeviceCheckJudegZJ deviceCheckJudegXtsj = new DeviceCheckJudegZJ();
				setDeviceCheckJudeg(deviceCheckJudegXtsj, vehCheckLogin);
				deviceCheckJudegXtsj.setYqjyxm("制动协调时间(S)");
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
				DeviceCheckJudegZJ deviceCheckJudegZdwdx = new DeviceCheckJudegZJ();
				setDeviceCheckJudeg(deviceCheckJudegZdwdx, vehCheckLogin);
				deviceCheckJudegZdwdx.setYqjyxm("制动稳定性");
				deviceCheckJudegZdwdx.setYqjyjg(zdwdx);
				deviceCheckJudegZdwdx.setYqjgpd(roadCheck.getZdwdx().equals("1") ? "1" : "2");
				deviceCheckJudegZdwdx.setYqbzxz("-");
				deviceCheckJudegZdwdx.setXh(xh.intValue());
				deviceCheckJudegZdwdx.setBz1("R");
				xh++;
				this.hibernateTemplate.save(deviceCheckJudegZdwdx);

				// 行车空载制动距离
				DeviceCheckJudegZJ deviceCheckJudegKzzdjl = new DeviceCheckJudegZJ();
				setDeviceCheckJudeg(deviceCheckJudegKzzdjl, vehCheckLogin);
				deviceCheckJudegKzzdjl.setYqjyxm("空载制动距离(m)");
				deviceCheckJudegKzzdjl.setYqjyjg(roadCheck.getXckzzdjl().toString());
				deviceCheckJudegKzzdjl.setYqjgpd(roadCheck.getLskzzdjlpd().toString());
				deviceCheckJudegKzzdjl.setYqbzxz("≤" + roadCheck.getLskzzdjlxz());
				deviceCheckJudegKzzdjl.setXh(xh.intValue());
				deviceCheckJudegKzzdjl.setBz1("R");
				xh++;
				this.hibernateTemplate.save(deviceCheckJudegKzzdjl);
				
				if(!StringUtils.isEmpty(roadCheck.getXckzmfdd())) {
					// 行车空载制动距离
					DeviceCheckJudegZJ deviceCheckJudegKzmfdd = new DeviceCheckJudegZJ();
					setDeviceCheckJudeg(deviceCheckJudegKzmfdd, vehCheckLogin);
					deviceCheckJudegKzmfdd.setYqjyxm("空载MFFDD(m)");
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
				DeviceCheckJudegZJ deviceCheckJudegZcpd = new DeviceCheckJudegZJ();
				setDeviceCheckJudeg(deviceCheckJudegZcpd, vehCheckLogin);
				deviceCheckJudegZcpd.setYqjyxm("路试驻车" + (roadCheck.getZcpd() == 0 ? "20%" : "15%") + "坡道路试");
				deviceCheckJudegZcpd.setYqjyjg((roadCheck.getLszczdpd()==1 )? "2min未溜" : "2min内溜车");
				deviceCheckJudegZcpd.setYqjgpd(roadCheck.getLszczdpd().toString());
				deviceCheckJudegZcpd.setYqbzxz("正反2min");
				deviceCheckJudegZcpd.setXh(xh.intValue());
				deviceCheckJudegZcpd.setBz1("R");
				xh++;
				this.hibernateTemplate.save(deviceCheckJudegZcpd);
			}

			if (vehCheckLogin.getJyxm().indexOf("R3") >= 0) {
				DeviceCheckJudegZJ deviceCheckJudegcsb = new DeviceCheckJudegZJ();
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
	
	
	private Integer createBrakRollerDateJudeg(final VehCheckLogin vehCheckLogin, Map<String, Object> flagMap,
			Integer xh) {
		List<BrakRollerData> brds = (List<BrakRollerData>) this.hibernateTemplate.find(
				"from BrakRollerData where jylsh=? and jyxm !='B0' and sjzt=? order by jycs desc",
				vehCheckLogin.getJylsh(), BrakRollerData.SJZT_ZC);

		for (BrakRollerData brd : brds) {
			if (flagMap.get(brd.getJyxm()) == null) {
				
				String temp = brd.getJyxm().indexOf("L")==0?"加载":"";
				
				DeviceCheckJudegZJ dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
				dcj1.setXh(xh);
				dcj1.setYqjyxm(getZW(brd.getZw()) +temp+ "制动率(%)");
				dcj1.setYqjyjg(brd.getKzxczdl() == null ? "" : brd.getKzxczdl().toString());
				dcj1.setYqbzxz(brd.getKzzdlxz() == null ? "" : "≥" + brd.getKzzdlxz().toString()+".0");
				dcj1.setYqjgpd(brd.getKzzdlpd() == null ? "" : brd.getKzzdlpd().toString());
				dcj1.setXh(xh);
				xh++;

				this.hibernateTemplate.save(dcj1);

				DeviceCheckJudegZJ dcj2 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
				dcj2.setXh(xh);
				dcj2.setYqjyxm(getZW(brd.getZw()) +temp+ "不平衡率(%)");
				dcj2.setYqjyjg(brd.getKzbphl() == null ? "" : brd.getKzbphl().toString());
				dcj2.setYqbzxz(brd.getBphlxz() == null ? "" : "≤" + brd.getBphlxz().toString());
				dcj2.setYqjgpd(brd.getKzbphlpd() == null ? "" : brd.getKzbphlpd().toString());
				dcj2.setXh(xh);
				xh++;
				this.hibernateTemplate.save(dcj2);

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

	private void setDeviceCheckJudeg(DeviceCheckJudegZJ deviceCheckJudeg, VehCheckLogin vehCheckLogin) {
		deviceCheckJudeg.setJylsh(vehCheckLogin.getJylsh());
		deviceCheckJudeg.setHphm(vehCheckLogin.getHphm());
		deviceCheckJudeg.setHpzl(vehCheckLogin.getHpzl());
		deviceCheckJudeg.setJycs(vehCheckLogin.getJycs());
		deviceCheckJudeg.setJyjgbh(vehCheckLogin.getJyjgbh());
	}

}
