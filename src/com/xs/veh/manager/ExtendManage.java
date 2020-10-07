package com.xs.veh.manager;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.network.data.Outline;

@Service("extendManage")
public class ExtendManage {
	
	
	@Resource(name="qkdJdbcTemplate")
	private JdbcTemplate qkdJdbcTemplate;
	
	@Autowired
	private CheckDataManager checkDataManager;
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	private CheckEventManger checkEventManger;
	
	public void vehLoginAfter(VehCheckLogin vehCheckLogin) {
		
	}
	
	public void upQkdOutLine(VehCheckLogin vehCheckLogin) {
		String jyxm = vehCheckLogin.getJyxm();
		// 外廓尺寸测量
		if (jyxm.indexOf("M") != -1) {
			String sql="insert into CarRemoteExchange(CarNumber,PlateCode,VehicleSN,TestTimes,VinCode,CarOwner,"
					+ "ProduceDate,RegDate,RatedSpeed,TotalMass,NudeMass,RollerNumber,DetailTypeCode,FuelTypeCode,"
					+ "BusinessCode,TestKindCode,ImporterName,LengthRatedValue,WidthRatedValue,HeightRatedValue,"
					+ "WheelBaseRatedValue,NeedTestFence,TestModeCode,TestItemCode)"
					+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,0,1,'C')";
			int i = qkdJdbcTemplate.update(sql, vehCheckLogin.getHphm(),vehCheckLogin.getHpzl(),vehCheckLogin.getJylsh(),
					vehCheckLogin.getJycs(),vehCheckLogin.getClsbdh(),vehCheckLogin.getSyr(),vehCheckLogin.getCcrq(),
					vehCheckLogin.getCcdjrq(),150,vehCheckLogin.getZzl(),vehCheckLogin.getZbzl(),vehCheckLogin.getZs(),
					vehCheckLogin.getCllx(),vehCheckLogin.getRlzl(),vehCheckLogin.getSyxz(),vehCheckLogin.getJylb(),
					vehCheckLogin.getDly(),vehCheckLogin.getCwkc(),vehCheckLogin.getCwkk(),vehCheckLogin.getCwkg(),
					vehCheckLogin.getZj());
		}
		vehCheckLogin.setVehwkzt(VehCheckLogin.ZT_JCZ);
		hibernateTemplate.update(vehCheckLogin);
	}
	
	
	public void saveQkdOutLine(VehCheckLogin vehCheckLogin) throws InterruptedException {
		
		List<Map<String, Object>> list = qkdJdbcTemplate.queryForList("select * from CarRemoteExchange where VehicleSN=? and StateCode=255 ",new Object[]{vehCheckLogin.getJylsh()});
		
		if(!CollectionUtils.isEmpty(list)) {
			Map<String, Object> data = list.get(0);
			Outline out=new Outline();
			out.setBaseDeviceData(vehCheckLogin, vehCheckLogin.getJycs(), "M1");
			Integer clwkccpd = data.get("ContourTotalResult").equals("Y")?Outline.PDJG_HG:Outline.PDJG_BHG;
			out.setClwkccpd(clwkccpd);
			Integer cwkc = (Integer)data.get("LengthRealValue");
			out.setCwkc(cwkc);
			Integer cwkg = (Integer)data.get("HeightRealValue");
			out.setCwkg(cwkg);
			Integer cwkk = (Integer)data.get("WidthRealValue");
			out.setCwkk(cwkk);
			out.setDxcs(vehCheckLogin.getJycs());
			out.setJcxdh("1");
			out.setJycs(vehCheckLogin.getJycs());
			out.setSjzt(Outline.SJZT_ZC);
			out.setStatus(0);
			out.setZpd(clwkccpd);
			out.setJyjgbh(vehCheckLogin.getJyjgbh());
			this.hibernateTemplate.save(out);
		}
		vehCheckLogin.setVehwkzt(VehCheckLogin.JCZT_JYJS);
		hibernateTemplate.update(vehCheckLogin);
		VehCheckProcess vp = checkDataManager.getVehCheckProces(vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(), "M1");
		vp.setJssj(new Date());
		this.checkDataManager.updateProcess(vp);
		Thread.sleep(200);
		checkEventManger.createEvent(vp.getJylsh(), vp.getJycs(), "18C81", vp.getJyxm(), vp.getHphm(), vp.getHpzl(),
				vp.getClsbdh(),vehCheckLogin.getVehcsbj());
		
		Thread.sleep(200);
		checkEventManger.createEvent(vp.getJylsh(), vp.getJycs(), "18C58", vp.getJyxm(), vp.getHphm(), vp.getHpzl(),
				vp.getClsbdh(),vehCheckLogin.getVehcsbj());
		
	}
	
	public  List<Map<String, Object>> getqkdWaitList( String jylsh) {
		return qkdJdbcTemplate.queryForList("select * from CarRemoteExchange where VehicleSN=? ",new Object[]{jylsh});
	}

}
