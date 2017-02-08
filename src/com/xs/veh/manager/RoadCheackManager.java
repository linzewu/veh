package com.xs.veh.manager;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.common.Message;
import com.xs.veh.entity.DeviceCheckJudeg;
import com.xs.veh.entity.RoadCheck;
import com.xs.veh.entity.VehCheckLogin;


@Service("roadCheackManager")
public class RoadCheackManager {
	
	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;
	@Resource(name = "checkEventManger")
	private CheckEventManger checkEventManger;
	
	@Resource(name = "vehManager")
	private VehManager vehManager;
	
	public Message saveRoadCheck(RoadCheck roadCheck) throws InterruptedException{
		
		String jylsh = roadCheck.getJylsh();
		Message message =new Message();
		List<VehCheckLogin> datas = (List<VehCheckLogin>) this.hibernateTemplate.find("from VehCheckLogin where jylsh =?", jylsh);
		if(datas==null||datas.isEmpty()){
			message.setState("2");
			message.setMessage("路试车辆不存在！");
			return message;
		}
		
		boolean createJudegFlag=false;
		
		if(roadCheck.getId()==null){
			createJudegFlag=true;
		}
		
		Long xh = (Long) this.hibernateTemplate.find("select count(*) from DeviceCheckJudeg where jylsh=?", roadCheck.getJylsh()).get(0);
		xh++;
		VehCheckLogin vehCheckLogin =datas.get(0);
		String cllx=vehCheckLogin.getCllx();
		Integer zzl=vehCheckLogin.getZzl();
		String zzly=vehCheckLogin.getZzly();
		
		//路试初速度
		roadCheck.setLscsdxz(cllx, zzl);
		roadCheck.setLscsdpd();
		//协调时间
		roadCheck.setLsxtsjxz(zzly);;
		roadCheck.setLsxtsjpd();
		
		
		//行车空载制动距离
		roadCheck.setLskzzdjlxz(cllx, zzl);
		roadCheck.setLskzzdjlpd();
		
		//行车空载制动距离
		roadCheck.setLskzmfddxz(cllx, zzl);
		roadCheck.setLskzmfddpd();
		
		roadCheck.setCsbpd();
		//驻车制动
		DeviceCheckJudeg deviceCheckJudegZcpd=new DeviceCheckJudeg();
		setDeviceCheckJudeg(deviceCheckJudegZcpd,vehCheckLogin);
		
		//行车路试制动判定
		if(roadCheck.getLscsdpd()==new Integer(2)||roadCheck.getLskzzdjlpd()==new Integer(2)||roadCheck.getLskzmfddpd()==new Integer(2)){
			roadCheck.setLszdpd(2);
		}else{
			roadCheck.setLszdpd(1);
		}
		
		if(roadCheck.getLszdpd()==new Integer(2)||roadCheck.getLszczdpd()!=new Integer(1)||roadCheck.getCsbpd()==new Integer(2)){
			roadCheck.setLsjg(2);
		}else{
			roadCheck.setLsjg(1);
		}
		this.hibernateTemplate.saveOrUpdate(roadCheck);
		vehCheckLogin.setVehlszt(VehCheckLogin.ZT_JYJS);
		if(createJudegFlag){
			checkDataManager.createRoadCheckJudeg(vehCheckLogin,xh.intValue());
		}
		
		String jyxm="";
		
		if(vehCheckLogin.getJyxm().indexOf("R1")>=0){
			jyxm+=",R1";
		}
		
		if(vehCheckLogin.getJyxm().indexOf("R2")>=0){
			jyxm+=",R2";
		}
		
		if(vehCheckLogin.getJyxm().indexOf("R3")>=0){
			jyxm+=",R3";
		}
		
		jyxm=jyxm.substring(1,jyxm.length());
		checkEventManger.createEvent(jylsh, vehCheckLogin.getJycs(), "18C55_R", "R", vehCheckLogin.getHphm(),
				vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh());
		
		checkEventManger.createEvent(jylsh, vehCheckLogin.getJycs(), "18C54", jyxm, vehCheckLogin.getHphm(),
				vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh());
		
		if(vehCheckLogin.getJyxm().indexOf("R1")>=0){
			checkEventManger.createEvent(jylsh, vehCheckLogin.getJycs(), "18C58", "R1", vehCheckLogin.getHphm(),
					vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh());
		}
		
		if(vehCheckLogin.getJyxm().indexOf("R2")>=0){
			checkEventManger.createEvent(jylsh, vehCheckLogin.getJycs(), "18C58", "R2", vehCheckLogin.getHphm(),
					vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh());
		}
		
		if(vehCheckLogin.getJyxm().indexOf("R3")>=0){
			checkEventManger.createEvent(jylsh, vehCheckLogin.getJycs(), "18C58", "R3", vehCheckLogin.getHphm(),
					vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh());
		}
		
		checkEventManger.createEvent(jylsh, vehCheckLogin.getJycs(), "18C58_R","R", vehCheckLogin.getHphm(),
				vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh());
		
		vehManager.updateVehCheckLoginState(jylsh);
		message.setState(Message.STATE_SUCCESS);
		message.setMessage("保存成功！");
		return message;
	}
	
	private void setDeviceCheckJudeg(DeviceCheckJudeg deviceCheckJudeg,VehCheckLogin vehCheckLogin){
		deviceCheckJudeg.setJylsh(vehCheckLogin.getJylsh());
		deviceCheckJudeg.setHphm(vehCheckLogin.getHphm());
		deviceCheckJudeg.setHpzl(vehCheckLogin.getHpzl());
		deviceCheckJudeg.setJycs(vehCheckLogin.getJycs());
		deviceCheckJudeg.setJyjgbh(vehCheckLogin.getJyjgbh());
	}
	
	public RoadCheck getRoadCheck(String jylsh){
		List<RoadCheck> roadChecks = (List<RoadCheck>)this.hibernateTemplate.find("from RoadCheck where jylsh=?", jylsh);
		
		if(roadChecks!=null&&!roadChecks.isEmpty()){
			return roadChecks.get(0);
		}
		return null;
	}
	
	public static void main(String[] age){
		System.out.println();
	}

}
