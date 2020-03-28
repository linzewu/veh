package com.xs.veh.manager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
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
import com.xs.veh.entity.CheckPhoto;
import com.xs.veh.entity.DeviceCheckJudegZJ;
import com.xs.veh.entity.RoadCheck;
import com.xs.veh.entity.TestResult;
import com.xs.veh.entity.TestVeh;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.network.data.BaseDeviceData;
import com.xs.veh.network.data.BrakRollerData;
import com.xs.veh.network.data.LightData;
import com.xs.veh.network.data.OtherInfoData;
import com.xs.veh.network.data.ParDataOfAnjian;
import com.xs.veh.network.data.SideslipData;
import com.xs.veh.network.data.SpeedData;
import com.xs.veh.network.data.SuspensionData;
import com.xs.veh.network.data.VolumeData;

import net.sf.json.JSONObject;

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
				.find("from BrakRollerData where jylsh=? and jyxm!='L1' and jyxm!='L2' and jyxm!='L3' and jyxm!='L4' order by id desc ", vehCheckLogin.getJylsh());
		
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
				
				
				Calendar c1 = Calendar.getInstance();
				c1.setTime(vehCheckLogin.getDlsj());
				c1.set(Calendar.HOUR_OF_DAY, 0);
				c1.set(Calendar.MINUTE, 0);
				c1.set(Calendar.SECOND, 0);
				c1.set(Calendar.MILLISECOND, 0);
				
				SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				logger.info("日期："+sdf.format(c1.getTime()));
				
				Calendar c=Calendar.getInstance();
				c.setTime(vehCheckLogin.getDlsj());
				c.add(Calendar.MONTH, 1);
				
				SQLQuery wtQuery = session.createSQLQuery("select * from QCPFWQ2018.dbo.ASMCLSJB where JCRQ>=? and JCRQ<? and cphm=? and CPYS=?");
				wtQuery.setParameter(0, c1)
				.setParameter(1, c.getTime()).setParameter(2, vehCheckLogin.getHphm())
				.setParameter(3, getCpysByhpzl(vehCheckLogin.getHpzl()));
				wtQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
				List<Map<String,Object>> wtList  =  wtQuery.list();
				Map<String,Object> wtMap=null;
				if(!CollectionUtils.isEmpty(wtList)) {
					wtMap= wtList.get(wtList.size()-1);
				}
				
				
				SQLQuery sdsQuery = session.createSQLQuery("select * from  QCPFWQ2018.dbo.SDSCLSJB where JCRQ>=? and JCRQ<? and cphm=? and CPYS=?");
				sdsQuery.setParameter(0, c1)
				.setParameter(1, c.getTime()).setParameter(2, vehCheckLogin.getHphm())
				.setParameter(3, getCpysByhpzl(vehCheckLogin.getHpzl()));
				sdsQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
				List<Map<String,Object>> sdsList  =  sdsQuery.list();
				Map<String,Object> sdsMap=null;
				if(!CollectionUtils.isEmpty(sdsList)) {
					sdsMap= sdsList.get(sdsList.size()-1); 
				}
				
				SQLQuery lgdQuery = session.createSQLQuery("select CPHM, CLLX, XDSD, HJWD, DQYL, CPYS, EDZS, SJZS, SJGL, XZGL, YDZ1, YDZ2, YDZ3, YDXZ1, YDXZ2, YDXZ3, NOCLZ, NOXZ, SFHG, JCRY, KSSJ, JSSJ, CSSBBH, CZRY, JSYMC, ID, JCRQ, KH, DF, GLXZXS, CO, CO2, BZ, BGJCZMC, BGJCZDZ, BGJCZCMC, BGWQFXYCJ, BGWQFXYXH, BGWQFXYRQ, BGOBDCJ, BGOBDXH, BGDPCJ, BGDPXH, BGYDJCJ, BGYDJXH, BGYDJRQ  from  QCPFWQ2018.dbo.lgdclsjb where JCRQ>=? and JCRQ<? and cphm=? and CPYS=?");
				lgdQuery.setParameter(0, c1)
					.setParameter(1, c.getTime()).setParameter(2, vehCheckLogin.getHphm())
					.setParameter(3, getCpysByhpzl(vehCheckLogin.getHpzl()));
				lgdQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
				List<Map<String,Object>> lgdList  =  lgdQuery.list();
				Map<String,Object> lgdMap=null;
				if(!CollectionUtils.isEmpty(lgdList)) {
					lgdMap= lgdList.get(lgdList.size()-1);
				}
				
				SQLQuery ydQuery = session.createSQLQuery("select * from  QCPFWQ2018.dbo.YDCLSJB where JCRQ>=? and JCRQ<? and cphm=? and CPYS=?");
				ydQuery.setParameter(0, c1)
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
					Date jcrq = (Date)wtMap.get("JCRQ");
					Date jssj = (Date)wtMap.get("JSSJ");
					
					Calendar jssjCalendar = Calendar.getInstance();
					jssjCalendar.setTime(jssj);
					Calendar jcrqCalendar = Calendar.getInstance();
					jcrqCalendar.setTime(jcrq);
					
					jcrqCalendar.set(Calendar.HOUR_OF_DAY, jssjCalendar.get(Calendar.HOUR_OF_DAY));
					jcrqCalendar.set(Calendar.MINUTE, jssjCalendar.get(Calendar.MINUTE));
					jcrqCalendar.set(Calendar.SECOND, jssjCalendar.get(Calendar.SECOND));
					
					Long timeKey =jcrqCalendar.getTimeInMillis();
					
					list.add(timeKey);
					Map<String,Map<String,Object>> subData =new HashMap<String, Map<String,Object>>();
					subData.put("wt",wtMap);
					data.put(String.valueOf(timeKey),subData);
				}
				
				if(!CollectionUtils.isEmpty(sdsMap)) {
					Date jcrq = (Date)sdsMap.get("JCRQ");
					Date jssj = (Date)sdsMap.get("JSSJ");
					
					Calendar jssjCalendar = Calendar.getInstance();
					jssjCalendar.setTime(jssj);
					Calendar jcrqCalendar = Calendar.getInstance();
					jcrqCalendar.setTime(jcrq);
					
					jcrqCalendar.set(Calendar.HOUR_OF_DAY, jssjCalendar.get(Calendar.HOUR_OF_DAY));
					jcrqCalendar.set(Calendar.MINUTE, jssjCalendar.get(Calendar.MINUTE));
					jcrqCalendar.set(Calendar.SECOND, jssjCalendar.get(Calendar.SECOND));
					
					Long timeKey =jcrqCalendar.getTimeInMillis();
					
					
					list.add(timeKey);
					
					Map<String,Map<String,Object>> subData =new HashMap<String, Map<String,Object>>();
					subData.put("sds",sdsMap);
					
					data.put(String.valueOf(timeKey),subData);
				}
				
				if(!CollectionUtils.isEmpty(lgdMap)) {
					
					logger.info("lgdMap:"+JSONObject.fromObject(lgdMap).toString());
					Date jcrq = (Date)lgdMap.get("JCRQ");
					Date jssj = (Date)lgdMap.get("JSSJ");
					
					Calendar jssjCalendar = Calendar.getInstance();
					jssjCalendar.setTime(jssj);
					Calendar jcrqCalendar = Calendar.getInstance();
					jcrqCalendar.setTime(jcrq);
					
					jcrqCalendar.set(Calendar.HOUR_OF_DAY, jssjCalendar.get(Calendar.HOUR_OF_DAY));
					jcrqCalendar.set(Calendar.MINUTE, jssjCalendar.get(Calendar.MINUTE));
					jcrqCalendar.set(Calendar.SECOND, jssjCalendar.get(Calendar.SECOND));
					
					Long timeKey =jcrqCalendar.getTimeInMillis();
					
					
					list.add(timeKey);
					
					Map<String,Map<String,Object>> subData =new HashMap<String, Map<String,Object>>();
					subData.put("lgd",lgdMap);
					
					data.put(String.valueOf(timeKey),subData);
				}
				
				if(!CollectionUtils.isEmpty(ydMap)) {
					Date jcrq = (Date)ydMap.get("JCRQ");
					Date jssj = (Date)ydMap.get("JSSJ");
					
					Calendar jssjCalendar = Calendar.getInstance();
					jssjCalendar.setTime(jssj);
					Calendar jcrqCalendar = Calendar.getInstance();
					jcrqCalendar.setTime(jcrq);
					
					jcrqCalendar.set(Calendar.HOUR_OF_DAY, jssjCalendar.get(Calendar.HOUR_OF_DAY));
					jcrqCalendar.set(Calendar.MINUTE, jssjCalendar.get(Calendar.MINUTE));
					jcrqCalendar.set(Calendar.SECOND, jssjCalendar.get(Calendar.SECOND));
					
					Long timeKey =jcrqCalendar.getTimeInMillis();
					
					
					list.add(timeKey);
					Map<String,Map<String,Object>> subData =new HashMap<String, Map<String,Object>>();
					subData.put("yd",ydMap);
					data.put(String.valueOf(timeKey),subData);
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
	
	public TestResult getTestResultBylsh(String jylsh) {
		List<TestResult> testResults =  (List<TestResult>) this.hibernateTemplate.find("from TestResult where jylsh=? order by id desc ",jylsh);
		if(!CollectionUtils.isEmpty(testResults)) {
			return testResults.get(0);
		}else {
			return null;
		}
	
	}
	
	
	/**
	 * 生成报告单
	 */
	public void createDeviceCheckJudeg(final VehCheckLogin vehCheckLogin) {

		Map<String, Object> flagMap = new HashMap<String, Object>();
		
		List otherInfoDatas =this.hibernateTemplate.find("from OtherInfoData where jylsh=? order by id desc", vehCheckLogin.getJylsh());
		
		List parDataOfAnjians =this.hibernateTemplate.find("from ParDataOfAnjian where jylsh=? order by id desc", vehCheckLogin.getJylsh());
		
	//	List outlines =this.hibernateTemplate.find("from Outline where jylsh=? order by id desc ", vehCheckLogin.getJylsh());
		
		List<TestResult> testResults =  (List<TestResult>) this.hibernateTemplate.find("from TestResult where jylsh=? order by id desc ",vehCheckLogin.getJylsh());
		
		TestVeh testVeh =getTestVehbyJylsh(vehCheckLogin.getJylsh());
		
		OtherInfoData otherInfoData = null;
		ParDataOfAnjian parDataOfAnjian=null;
		
		TestResult testResult=null;
		
		if(otherInfoDatas!=null&&!otherInfoDatas.isEmpty()){
			otherInfoData=(OtherInfoData) otherInfoDatas.get(0);
		}
		
		
		if(parDataOfAnjians!=null&&!parDataOfAnjians.isEmpty()){
			parDataOfAnjian=(ParDataOfAnjian) parDataOfAnjians.get(0);
		}
		
		if(!CollectionUtils.isEmpty(testResults)) {
			testResult=testResults.get(0);
		}
		
		
		Integer xh = 1;

		// 清空报告
		this.hibernateTemplate.execute(new HibernateCallback<Integer>() {
			@Override
			public Integer doInHibernate(Session session) throws HibernateException {
				int res = session.createQuery("delete DeviceCheckJudegZJ where jylsh=? and jyjgbh=? ")
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
//		List<SideslipData> sideslipDatas = (List<SideslipData>) this.hibernateTemplate.find(
//				"from SideslipData where jylsh=? and sjzt=? order by jycs desc", vehCheckLogin.getJylsh(),
//				SideslipData.SJZT_ZC);
		
		Map<String, Object>  adatas = getAData(vehCheckLogin.getJylsh(),vehCheckLogin.getJycs());

		if (!CollectionUtils.isEmpty(adatas)) {
			
			SideslipData sideslipData = (SideslipData)adatas.get("a1");
			
			if(sideslipData!=null) {
				DeviceCheckJudegZJ dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
				dcj1.setYqjyxm("第一转向轮横向侧滑值(m/km)");
				dcj1.setYqjyjg(sideslipData.getSideslip() == null ? "" : sideslipData.getSideslip().toString());
				dcj1.setYqbzxz(sideslipData.getChxz().replace(",", "~"));
				dcj1.setYqjgpd(sideslipData.getChpd() == null ? "" : sideslipData.getChpd().toString());
				dcj1.setXh(xh);
				xh++;
				this.hibernateTemplate.save(dcj1);
			}
			
			SideslipData sideslipData2 = (SideslipData)adatas.get("a2");
			
			if(sideslipData2!=null) {
				DeviceCheckJudegZJ dcj2 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
				dcj2.setYqjyxm("第二转向轮横向侧滑值(m/km)");
				dcj2.setYqjyjg(sideslipData2.getSideslip() == null ? "" : sideslipData2.getSideslip().toString());
				dcj2.setYqbzxz(sideslipData2.getChxz().replace(",", "~"));
				dcj2.setYqjgpd(sideslipData2.getChpd() == null ? "" : sideslipData2.getChpd().toString());
				dcj2.setXh(xh);
				xh++;
				this.hibernateTemplate.save(dcj2);
			}
			
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
		
//		CurbWeightData curbWeightData = this.vehManager.getLastCurbWeightDataOfJylsh(vehCheckLogin.getJylsh());
//
//		if (vehCheckLogin.getJylb().equals("00") && curbWeightData != null&&vehCheckLogin.getJyxm().indexOf("Z1")>=0) {
//			String cllx=vehCheckLogin.getCllx();
//			int xzgj=100;
//			String temp1="±3%或±";
//			if(cllx.indexOf("H1")==0||cllx.indexOf("H2")==0||cllx.indexOf("Z1")==0||cllx.indexOf("Z2")==0||cllx.indexOf("Z5")==0||cllx.indexOf("G")==0||cllx.indexOf("B")==0){
//				xzgj=500;
//			}else if(cllx.indexOf("H3")==0||cllx.indexOf("H4")==0||cllx.indexOf("Z3")==0||cllx.indexOf("Z4")==0){
//				xzgj=100;
//			}else if(cllx.indexOf("N")==0){
//				xzgj=100;
//				temp1="±5%或±";
//			}else if(cllx.indexOf("M")==0){
//				xzgj=10;
//			}
//			
//			DeviceCheckJudegZJ dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
//			dcj1.setXh(xh);
//			dcj1.setYqjyxm("整备质量(KG)");
//			dcj1.setYqjyjg(curbWeightData.getZbzl()==null ? "" : curbWeightData.getZbzl().toString());
//			dcj1.setYqbzxz(temp1+xzgj+"KG");
//			dcj1.setYqjgpd(curbWeightData.getZbzlpd().toString());
//			dcj1.setXh(xh);
//			xh++;
//			this.hibernateTemplate.save(dcj1);
//		}
//		
		if(testResult!=null) {
			
			if(testResult.getDlx_pd()!=null) {
				DeviceCheckJudegZJ dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
				dcj1.setXh(xh);
				dcj1.setYqjyxm("动力性（km/h）");
				dcj1.setYqjyjg(testResult.getDlx_wdcs()==null ? "" : testResult.getDlx_wdcs().toString());
				dcj1.setYqbzxz("≥"+testResult.getDlx_edcs());
				
				String pd=BaseDeviceData.PDJG_WJ.toString();
				
				if("不合格".equals(testResult.getDlx_pd())) {
					pd=BaseDeviceData.PDJG_BHG.toString();
				}else if("合格".equals(testResult.getDlx_pd())) {
					if("等级评定".equals(testVeh.getJcxz())) {
						if("0.82".equals(testResult.getDlx_dbgl())) {
							pd="一级";
						}else if("0.75".equals(testResult.getDlx_dbgl())){
							pd="二级";
						}
					}else {
						pd=BaseDeviceData.PDJG_HG.toString();
					}
				}
				dcj1.setYqjgpd(pd);
				dcj1.setXh(xh);
				xh++;
				this.hibernateTemplate.save(dcj1);
			}
			logger.info(!testResult.getYH_PD().equals("0"));
			if(testResult.getYH_PD()!=null&& !testResult.getYH_PD().equals("0")) {
				DeviceCheckJudegZJ dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
				dcj1.setXh(xh);
				dcj1.setYqjyxm("经济性（L/100 km）");
				dcj1.setYqjyjg(testResult.getYh_scz()==null ? "" : testResult.getYh_scz().toString());
				dcj1.setYqbzxz("≤"+testResult.getYh_bzxz());
				dcj1.setYqjgpd(testResult.getYH_PD().toString());
				dcj1.setXh(xh);
				xh++;
				this.hibernateTemplate.save(dcj1);
			}
			
			Map<String, Map<String, Object>>  pfxData =  getPFXData(vehCheckLogin);
			
			if(pfxData!=null) {
				
				if(pfxData.get("sds")!=null) {
					
					Map<String, Object> sds = pfxData.get("sds");
					DeviceCheckJudegZJ dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
					dcj1.setXh(xh);
					dcj1.setYqjyxm("高怠速HC(10¯6)");
					Double hcgclz =(Double)sds.get("HCGCLZ");
					Double hcgxz = (Double)sds.get("HCGXZ");
					
					dcj1.setYqjyjg(hcgclz==null ? "" : hcgclz.toString());
					dcj1.setYqbzxz("≤"+hcgxz);
					dcj1.setYqjgpd(hcgxz<=hcgxz?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
					dcj1.setXh(xh);
					xh++;
					this.hibernateTemplate.save(dcj1);
					
					DeviceCheckJudegZJ dcj2 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
					Double cogclz =(Double)sds.get("COGCLZ");
					Double cogxz = (Double)sds.get("COGXZ");
					
					dcj2.setXh(xh);
					dcj2.setYqjyxm("高怠速CO(%)");
					dcj2.setYqjyjg(cogclz==null ? "" : cogclz.toString());
					dcj2.setYqbzxz("≤"+cogxz);
					dcj2.setYqjgpd(cogclz<=cogxz?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
					dcj2.setXh(xh);
					xh++;
					this.hibernateTemplate.save(dcj2);
					
					DeviceCheckJudegZJ dcj3 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
					Double kqxs =(Double)sds.get("KQXS");
					Double kqxsxz = (Double)sds.get("KQXSXZ");
					dcj3.setXh(xh);
					dcj3.setYqjyxm("高怠速λ");
					dcj3.setYqjyjg(kqxs==null ? "" : kqxs.toString());
					dcj3.setYqbzxz("≤"+kqxsxz);
					dcj3.setYqjgpd(kqxs<=kqxsxz?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
					dcj3.setXh(xh);
					xh++;
					this.hibernateTemplate.save(dcj3);
					
					DeviceCheckJudegZJ dcj4 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
					Double hcdclz =(Double)sds.get("HCDCLZ");
					Double hcdxz = (Double)sds.get("HCDXZ");					
					
					dcj4.setXh(xh);
					dcj4.setYqjyxm("怠速HC(10¯6)");
					dcj4.setYqjyjg(hcdclz==null ? "" : hcdclz.toString());
					dcj4.setYqbzxz("≤"+hcdxz);
					dcj4.setYqjgpd(hcdclz<=hcdxz?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
					dcj4.setXh(xh);
					xh++;
					this.hibernateTemplate.save(dcj4);
					
					DeviceCheckJudegZJ dcj5 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
					
					Double codclz =(Double)sds.get("CODCLZ");
					Double codxz = (Double)sds.get("CODXZ");		
					
					dcj5.setXh(xh);
					dcj5.setYqjyxm("怠速CO(10¯6)");
					dcj5.setYqjyjg(codclz==null ? "" : codclz.toString());
					dcj5.setYqbzxz("≤"+codxz);
					dcj5.setYqjgpd(codclz<=codxz?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
					dcj5.setXh(xh);
					xh++;
					this.hibernateTemplate.save(dcj5);
					
				}
				
				if(pfxData.get("wt")!=null) {
					Map<String, Object> wt = pfxData.get("wt");
					DeviceCheckJudegZJ dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
					
				
					
					Double clzco40 =(Double)wt.get("CLZCO40");
					Double xzco40 = (Double)wt.get("XZCO40");
					
					Double clzhc40 =(Double)wt.get("CLZHC40");
					Double xzhc40 = (Double)wt.get("XZHC40");	
					
					Double clzno40 =(Double)wt.get("CLZNO40");
					Double xzno40 = (Double)wt.get("XZNO40");	
					
					if(clzco40!=0||clzhc40!=0||clzno40!=0) {
						DeviceCheckJudegZJ dcj4 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
						dcj4.setXh(xh);
						dcj4.setYqjyxm("稳态2540工况CO(%)");
						dcj4.setYqjyjg(clzco40==null ? "" : clzco40.toString());
						dcj4.setYqbzxz("≤"+xzco40);
						dcj4.setYqjgpd(clzco40<=xzco40?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
						dcj4.setXh(xh);
						xh++;
						this.hibernateTemplate.save(dcj4);
						
						
						DeviceCheckJudegZJ dcj5 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
						dcj5.setXh(xh);
						dcj5.setYqjyxm("稳态2540工况HC(10¯6)");
						dcj5.setYqjyjg(clzhc40==null ? "" : clzhc40.toString());
						dcj5.setYqbzxz("≤"+xzhc40);
						dcj5.setYqjgpd(clzhc40<=xzhc40?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
						dcj5.setXh(xh);
						xh++;
						this.hibernateTemplate.save(dcj5);
						
						DeviceCheckJudegZJ dcj6 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
						
						dcj6.setXh(xh);
						dcj6.setYqjyxm("稳态2540工况NO(10¯6)");
						dcj6.setYqjyjg(clzno40==null ? "" : clzno40.toString());
						dcj6.setYqbzxz("≤"+xzno40);
						dcj6.setYqjgpd(clzno40<=xzno40?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
						dcj6.setXh(xh);
						xh++;
						this.hibernateTemplate.save(dcj6);
					}else {
						Double clzhc25 =(Double)wt.get("CLZHC25");
						Double xzhc25 = (Double)wt.get("XZHC25");		
						
						dcj1.setXh(xh);
						dcj1.setYqjyxm("稳态5025工况HC(10¯6)");
						dcj1.setYqjyjg(clzhc25==null ? "" : clzhc25.toString());
						dcj1.setYqbzxz("≤"+xzhc25);
						dcj1.setYqjgpd(clzhc25<=xzhc25?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
						dcj1.setXh(xh);
						xh++;
						this.hibernateTemplate.save(dcj1);
						
						DeviceCheckJudegZJ dcj2 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
						
						Double clzco25 =(Double)wt.get("CLZCO25");
						Double xzco25 = (Double)wt.get("XZCO25");		
						
						dcj2.setXh(xh);
						dcj2.setYqjyxm("稳态5025工况CO(%)");
						dcj2.setYqjyjg(clzco25==null ? "" : clzco25.toString());
						dcj2.setYqbzxz("≤"+xzco25);
						dcj2.setYqjgpd(clzco25<=xzco25?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
						dcj2.setXh(xh);
						xh++;
						this.hibernateTemplate.save(dcj2);
						
						
						DeviceCheckJudegZJ dcj3 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
						
						Double clzno25 =(Double)wt.get("CLZNO25");
						Double xzno25 = (Double)wt.get("XZNO25");		
						
						dcj3.setXh(xh);
						dcj3.setYqjyxm("稳态5025工况NO(10¯6)");
						dcj3.setYqjyjg(clzno25==null ? "" : clzno25.toString());
						dcj3.setYqbzxz("≤"+xzno25);
						dcj3.setYqjgpd(clzno25<=xzno25?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
						dcj3.setXh(xh);
						xh++;
						this.hibernateTemplate.save(dcj3);
					}
					
					
				}
				
				if(pfxData.get("yd")!=null) {
					
					Map<String, Object> yd = pfxData.get("yd");
					
					DeviceCheckJudegZJ dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
					
					Double ydpjz =(Double)yd.get("YDPJZ");
					Double ydxz = (Double)yd.get("YDXZ");		
					
					dcj1.setXh(xh);
					dcj1.setYqjyxm("光吸收系数  m¯1");
					dcj1.setYqjyjg(ydpjz==null ? "" : ydpjz.toString());
					dcj1.setYqbzxz("≤"+ydxz);
					dcj1.setYqjgpd(ydpjz<=ydxz?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
					dcj1.setXh(xh);
					xh++;
					this.hibernateTemplate.save(dcj1);
					
				}
				
				if(pfxData.get("lgd")!=null) {
					Map<String, Object> lgd = pfxData.get("lgd");
					
					Object ydz1 =lgd.get("YDZ1");
					Object ydxz1 = lgd.get("YDXZ1");		
					
					
					DeviceCheckJudegZJ dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
					dcj1.setXh(xh);
					dcj1.setYqjyxm("加载减速工况100%（m¯1）");
					dcj1.setYqjyjg(ydz1==null ? "" : ydz1.toString());
					dcj1.setYqbzxz("≤"+ydxz1);
					boolean ydz1Flag  = Double.parseDouble(ydz1.toString())<=Double.parseDouble(ydxz1.toString());
					dcj1.setYqjgpd(ydz1Flag?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
					dcj1.setXh(xh);
					xh++;
					this.hibernateTemplate.save(dcj1);
					
					DeviceCheckJudegZJ dcj2 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
					
					Object ydz2 =lgd.get("YDZ2");
					Object ydxz2 = lgd.get("YDXZ2");	
					
					dcj2.setXh(xh);
					dcj2.setYqjyxm("加载减速工况80%（m¯1）");
					dcj2.setYqjyjg(ydz2==null ? "" : ydz2.toString());
					dcj2.setYqbzxz("≤"+ydxz2);
					boolean ydz2Flag  = Double.parseDouble(ydz2.toString())<=Double.parseDouble(ydxz2.toString());
					dcj2.setYqjgpd(ydz2Flag?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
					dcj2.setXh(xh);
					xh++;
					this.hibernateTemplate.save(dcj2);
					
					DeviceCheckJudegZJ dcj3 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
					
					Object sjgl =lgd.get("SJGL");
					Object xzgl = lgd.get("XZGL");	
					
					dcj3.setXh(xh);
					dcj3.setYqjyxm("实测最大轮边功率 (kw)");
					dcj3.setYqjyjg(sjgl==null ? "" : sjgl.toString());
					dcj3.setYqbzxz("≤"+xzgl);
					boolean yqjgpdFlag  = Double.parseDouble(sjgl.toString())<=Double.parseDouble(xzgl.toString());
					dcj3.setYqjgpd(yqjgpdFlag?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
					dcj3.setXh(xh);
					xh++;
					this.hibernateTemplate.save(dcj3);
					
				}
				
			}
			
		}
		
		//外廓尺寸测量报告
//		if(outline!=null){
//			DeviceCheckJudegZJ dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
//			dcj1.setXh(xh);
//			dcj1.setYqjyxm("外廓尺寸(mmxmmxmm)");
//			dcj1.setYqjyjg(outline.getCwkc()+"x"+outline.getCwkk()+"x"+outline.getCwkg());
//			
//			if(vehCheckLogin.getJylb().equals("00")){
//				dcj1.setYqbzxz("±1%或50mm");
//			}else{
//				dcj1.setYqbzxz("±2%或100mm");
//			}
//			dcj1.setYqjgpd(outline.getClwkccpd().toString());
//			dcj1.setXh(xh);
//			xh++;
//			this.hibernateTemplate.save(dcj1);
//		}
		
		Map<String,Object> xjMap =  getXJData(vehCheckLogin, vehCheckLogin.getJycs());
		SuspensionData xj1 = (SuspensionData)xjMap.get("xj1");
		SuspensionData xj2 = (SuspensionData)xjMap.get("xj2");
		
		if(xj1!=null) {
			
			Float zxslFloat=Float.parseFloat(xj1.getZxsl());
			Float yxslFloat=Float.parseFloat(xj1.getYxsl());
			Float zycFloat = Float.parseFloat(xj1.getZyc());
			
			DeviceCheckJudegZJ dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
			dcj1.setXh(xh);
			dcj1.setYqjyxm("悬架前轴左吸收率");
			dcj1.setYqjyjg(zxslFloat+"%");
			dcj1.setYqbzxz("≥40");
			dcj1.setYqjgpd(zxslFloat>=40?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
			dcj1.setXh(xh);
			xh++;
			this.hibernateTemplate.save(dcj1);
			
			
			
			DeviceCheckJudegZJ dcj2 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
			dcj2.setXh(xh);
			dcj2.setYqjyxm("悬架前轴右吸收率");
			dcj2.setYqjyjg(yxslFloat+"%");
			dcj2.setYqbzxz("≥40");
			dcj2.setYqjgpd(yxslFloat>=40?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
			dcj2.setXh(xh);
			xh++;
			this.hibernateTemplate.save(dcj2);
			
			DeviceCheckJudegZJ dcj3 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
			dcj3.setXh(xh);
			dcj3.setYqjyxm("悬架前轴左右差");
			dcj3.setYqjyjg(zycFloat+"%");
			dcj3.setYqbzxz("≤15");
			dcj3.setYqjgpd(zycFloat<=15?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
			dcj3.setXh(xh);
			xh++;
			this.hibernateTemplate.save(dcj3);
			
			
		}
		
		if(xj2!=null) {
			
			Float zxslFloat=Float.parseFloat(xj2.getZxsl());
			Float yxslFloat=Float.parseFloat(xj2.getYxsl());
			Float zycFloat = Float.parseFloat(xj2.getZyc());
			
			DeviceCheckJudegZJ dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
			dcj1.setXh(xh);
			dcj1.setYqjyxm("悬架后轴左吸收率");
			dcj1.setYqjyjg(zxslFloat+"%");
			dcj1.setYqbzxz("≥40");
			dcj1.setYqjgpd(zxslFloat>=40?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
			dcj1.setXh(xh);
			xh++;
			this.hibernateTemplate.save(dcj1);
			
			
			
			DeviceCheckJudegZJ dcj2 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
			dcj2.setXh(xh);
			dcj2.setYqjyxm("悬架后轴右吸收率");
			dcj2.setYqjyjg(yxslFloat+"%");
			dcj2.setYqbzxz("≥40");
			dcj2.setYqjgpd(yxslFloat>=40?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
			dcj2.setXh(xh);
			xh++;
			this.hibernateTemplate.save(dcj2);
			
			DeviceCheckJudegZJ dcj3 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
			dcj3.setXh(xh);
			dcj3.setYqjyxm("悬架后轴左右差");
			dcj3.setYqjyjg(zycFloat+"%");
			dcj3.setYqbzxz("≤15");
			dcj3.setYqjgpd(zycFloat<=15?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
			dcj3.setXh(xh);
			xh++;
			this.hibernateTemplate.save(dcj3);
			
			
		}
		
		Map<String,Object> sjjData = getSJJData(vehCheckLogin, vehCheckLogin.getJycs());
		
		VolumeData volumeData =(VolumeData) sjjData.get("sjj");
		
		if(volumeData!=null) {
			DeviceCheckJudegZJ dcj1 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
			dcj1.setXh(xh);
			dcj1.setYqjyxm("喇叭声压级(dB(A))");
			dcj1.setYqjyjg(volumeData.getFb());
			dcj1.setYqbzxz("90dB(A)~115bD(A)");
			boolean pd =Float.parseFloat(volumeData.getFb().trim())>=90&&Float.parseFloat(volumeData.getFb().trim())<=115;
			dcj1.setYqjgpd(pd?BaseDeviceData.PDJG_HG.toString():BaseDeviceData.PDJG_BHG.toString());
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
				"from BrakRollerData where jylsh=? and jyxm !='B0' and jyxm!='L1' and jyxm!='L2' and jyxm!='L3' and jyxm!='L4' order by jycs desc",
				vehCheckLogin.getJylsh());
		
		
		TestVeh testVeh =getTestVehbyJylsh(vehCheckLogin.getJylsh());

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
				
				Float zhzjbphlxz = brd.getZhzjbphlxz(vehCheckLogin,testVeh);
				
				Integer djpd = brd.getDjpd(vehCheckLogin);
				
				String yqjgpd=null;
				
				
				if(testVeh!=null&&"等级评定".equals(testVeh.getJcxz())) {
					if(djpd==1) {
						yqjgpd="一级";
					}else if(djpd==2) {
						yqjgpd="二级";
					}else {
						yqjgpd ="不合格";
					}
				}else {
					if(djpd==1) {
						yqjgpd="合格";
					}else if(djpd==2) {
						yqjgpd="合格";
					}else {
						yqjgpd ="不合格";
					}
				}
				
				

				DeviceCheckJudegZJ dcj2 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
				dcj2.setXh(xh);
				dcj2.setYqjyxm(getZW(brd.getZw()) +temp+ "不平衡率(%)");
				dcj2.setYqjyjg(brd.getKzbphl() == null ? "" : brd.getKzbphl().toString());
				dcj2.setYqbzxz(zhzjbphlxz == null ? "" : "≤" + zhzjbphlxz.toString());
				dcj2.setYqjgpd(yqjgpd);
				dcj2.setXh(xh);
				xh++;
				this.hibernateTemplate.save(dcj2);
				
				if(brd.getJyxm().indexOf("L")==-1&&!"B0".equals(brd.getJyxm())) {
					DeviceCheckJudegZJ dcj3 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
					dcj3.setXh(xh);
					dcj3.setYqjyxm(getZW(brd.getZw()) + "左轮阻滞力");
					dcj3.setYqjyjg(brd.getZzzl() == null ? "" : brd.getZzzl().toString());
					dcj3.setYqbzxz(brd.getZzlxz() == null ? "" : "≤" + brd.getZzlxz().toString());
					brd.setZlzzlPd();
					
					dcj3.setYqjgpd(brd.getZlzzlpd() == null ? "" : brd.getZlzzlpd().toString());
					dcj3.setXh(xh);
					xh++;
					this.hibernateTemplate.save(dcj3);
					
					DeviceCheckJudegZJ dcj4 = createDeviceCheckJudegBaseInfo(vehCheckLogin);
					dcj4.setXh(xh);
					dcj4.setYqjyxm(getZW(brd.getZw()) + "右轮阻滞力");
					dcj4.setYqjyjg(brd.getYzzl() == null ? "" : brd.getZzzl().toString());
					dcj4.setYqbzxz(brd.getZzlxz() == null ? "" : "≤" + brd.getZzlxz().toString());
					brd.setYlzzlPd();
					dcj4.setYqjgpd(brd.getYlzzlpd() == null ? "" : brd.getYlzzlpd().toString());
					dcj4.setXh(xh);
					xh++;
					this.hibernateTemplate.save(dcj4);
				}
				flagMap.put(brd.getJyxm(), brd);
			}
			
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
	
	
	public List<DeviceCheckJudegZJ> getDeviceCheckJudegZJ(String jylsh) {
		List<DeviceCheckJudegZJ> datas = (List<DeviceCheckJudegZJ>) this.hibernateTemplate.find("from DeviceCheckJudegZJ where jylsh=?", jylsh);
		return datas;
	}
	
	public InputStream getIamge(String lsh,String zpzl) {
		
		String sql="from CheckPhoto where jylsh=? and zpzl=?";
		List<CheckPhoto> list = (List<CheckPhoto>) hibernateTemplate.find(sql, lsh,zpzl);
		
		if(!CollectionUtils.isEmpty(list)) {
			InputStream sbs = new ByteArrayInputStream(list.get(0).getZp());
			return sbs;
		}
		return null;
		
	}

}
