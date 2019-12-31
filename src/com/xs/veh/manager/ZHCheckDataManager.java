package com.xs.veh.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.xs.common.MyHibernateTemplate;
import com.xs.veh.entity.RoadCheck;
import com.xs.veh.entity.TestResult;
import com.xs.veh.entity.TestVeh;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.network.data.BrakRollerData;
import com.xs.veh.network.data.LightData;
import com.xs.veh.network.data.OtherInfoData;
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
			data.put("xj"+xjList.get(0).getZs(), xjList);
			data.put("xj"+xjList.get(1).getZs(), xjList);
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

}
