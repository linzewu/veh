package com.xs.veh.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.common.Message;
import com.xs.veh.entity.CheckPhoto;
import com.xs.veh.entity.ExternalCheck;
import com.xs.veh.entity.ExternalCheckJudge;
import com.xs.veh.entity.User;
import com.xs.veh.entity.VehCheckLogin;

@Service("externalCheckManager")
public class ExternalCheckManager {

	Logger logger = Logger.getLogger(ExternalCheckManager.class);

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	@Resource(name = "vehManager")
	private VehManager vehManager;
	
	@Autowired
	private HttpSession session;
	
	private void createExternalCheckJudge(ExternalCheck externalCheck){
		
		int index=1;
		String item1 =  externalCheck.getItem1()+externalCheck.getItem2()+externalCheck.getItem3()+externalCheck.getItem4()+externalCheck.getItem5();
		if(!item1.equals("00000")){
			ExternalCheckJudge ecj=new ExternalCheckJudge();
			ecj.setHphm(externalCheck.getHphm());
			ecj.setHpzl(externalCheck.getHpzl());
			ecj.setJylsh(externalCheck.getJylsh());
			ecj.setJycs(externalCheck.getJycs());
			ecj.setJyjgbh(externalCheck.getJyjgbh());
			if(item1.indexOf("2")!=-1){
				ecj.setRgjgpd(ExternalCheck.PD_BHG);
			}else{
				ecj.setRgjgpd(ExternalCheck.PD_HG);
			}
			this.hibernateTemplate.save(ecj);
		}
		
		String item2 =  externalCheck.getItem6()+externalCheck.getItem7()+externalCheck.getItem8()+
				externalCheck.getItem9()+externalCheck.getItem10()+externalCheck.getItem11()
				+externalCheck.getItem12()+externalCheck.getItem13()+externalCheck.getItem14()
				+externalCheck.getItem15();
		
		if(!item2.equals("0000000000")){
			ExternalCheckJudge ecj=new ExternalCheckJudge();
			ecj.setHphm(externalCheck.getHphm());
			ecj.setHpzl(externalCheck.getHpzl());
			ecj.setJylsh(externalCheck.getJylsh());
			ecj.setJycs(externalCheck.getJycs());
			ecj.setJyjgbh(externalCheck.getJyjgbh());
			if(item1.indexOf("2")!=-1){
				ecj.setRgjgpd(ExternalCheck.PD_BHG);
			}else{
				ecj.setRgjgpd(ExternalCheck.PD_HG);
			}
			this.hibernateTemplate.save(ecj);
		}
		
		
		String item3 =  externalCheck.getItem16()+externalCheck.getItem17()+externalCheck.getItem18()+
				externalCheck.getItem19()+externalCheck.getItem20()+externalCheck.getItem21();
		
		if(!item3.equals("000000")){
			ExternalCheckJudge ecj=new ExternalCheckJudge();
			ecj.setHphm(externalCheck.getHphm());
			ecj.setHpzl(externalCheck.getHpzl());
			ecj.setJylsh(externalCheck.getJylsh());
			ecj.setJycs(externalCheck.getJycs());
			ecj.setJyjgbh(externalCheck.getJyjgbh());
			if(item1.indexOf("2")!=-1){
				ecj.setRgjgpd(ExternalCheck.PD_BHG);
			}else{
				ecj.setRgjgpd(ExternalCheck.PD_HG);
			}
			this.hibernateTemplate.save(ecj);
		}
		
		
		String item4 =  externalCheck.getItem22()+externalCheck.getItem23()+externalCheck.getItem24()+
				externalCheck.getItem25()+externalCheck.getItem26()+externalCheck.getItem27()+externalCheck.getItem28()
				+externalCheck.getItem29()+externalCheck.getItem30()+externalCheck.getItem31()+externalCheck.getItem32()
				+externalCheck.getItem33()+externalCheck.getItem34()+externalCheck.getItem35()+externalCheck.getItem36()
				+externalCheck.getItem37()+externalCheck.getItem38()+externalCheck.getItem39()+externalCheck.getItem40()
				+externalCheck.getItem41();
		
		if(!item4.equals("00000000000000000000")){
			ExternalCheckJudge ecj=new ExternalCheckJudge();
			ecj.setHphm(externalCheck.getHphm());
			ecj.setHpzl(externalCheck.getHpzl());
			ecj.setJylsh(externalCheck.getJylsh());
			ecj.setJycs(externalCheck.getJycs());
			ecj.setJyjgbh(externalCheck.getJyjgbh());
			if(item1.indexOf("2")!=-1){
				ecj.setRgjgpd(ExternalCheck.PD_BHG);
			}else{
				ecj.setRgjgpd(ExternalCheck.PD_HG);
			}
			this.hibernateTemplate.save(ecj);
		}
		
		String item5 =  externalCheck.getItem80();
		
		if(!item5.equals("0")){
			ExternalCheckJudge ecj=new ExternalCheckJudge();
			ecj.setHphm(externalCheck.getHphm());
			ecj.setHpzl(externalCheck.getHpzl());
			ecj.setJylsh(externalCheck.getJylsh());
			ecj.setJycs(externalCheck.getJycs());
			ecj.setJyjgbh(externalCheck.getJyjgbh());
			if(item1.indexOf("2")!=-1){
				ecj.setRgjgpd(ExternalCheck.PD_BHG);
			}else{
				ecj.setRgjgpd(ExternalCheck.PD_HG);
			}
			this.hibernateTemplate.save(ecj);
		}
		
		String item6 =  externalCheck.getItem42()+externalCheck.getItem43()+externalCheck.getItem44()+
				externalCheck.getItem45();
		
		if(!item6.equals("0000")){
			ExternalCheckJudge ecj=new ExternalCheckJudge();
			ecj.setHphm(externalCheck.getHphm());
			ecj.setHpzl(externalCheck.getHpzl());
			ecj.setJylsh(externalCheck.getJylsh());
			ecj.setJycs(externalCheck.getJycs());
			ecj.setJyjgbh(externalCheck.getJyjgbh());
			if(item1.indexOf("2")!=-1){
				ecj.setRgjgpd(ExternalCheck.PD_BHG);
			}else{
				ecj.setRgjgpd(ExternalCheck.PD_HG);
			}
			this.hibernateTemplate.save(ecj);
		}
		
		String item7 =  externalCheck.getItem46()+externalCheck.getItem47()+externalCheck.getItem48()+
				externalCheck.getItem49()+externalCheck.getItem50();
		
		if(!item7.equals("00000")){
			ExternalCheckJudge ecj=new ExternalCheckJudge();
			ecj.setHphm(externalCheck.getHphm());
			ecj.setHpzl(externalCheck.getHpzl());
			ecj.setJylsh(externalCheck.getJylsh());
			ecj.setJycs(externalCheck.getJycs());
			ecj.setJyjgbh(externalCheck.getJyjgbh());
			if(item1.indexOf("2")!=-1){
				ecj.setRgjgpd(ExternalCheck.PD_BHG);
			}else{
				ecj.setRgjgpd(ExternalCheck.PD_HG);
			}
			this.hibernateTemplate.save(ecj);
		}
		
	}
	
	/**
	 * 保存外检测信息
	 * @param externalCheck
	 * @return
	 */
	public Message saveExternalCheck(ExternalCheck externalCheck) {
		VehCheckLogin vehCheckLogin = vehManager.getVehCheckLoginByJylsh(externalCheck.getJyjgbh(),
				externalCheck.getJylsh());
		
		User user = (User)session.getAttribute("user");

		Message message = new Message();
		if (vehCheckLogin != null) {
			this.hibernateTemplate.save(externalCheck);
			//创建判定结果
			createExternalCheckJudge(externalCheck);
			
			vehCheckLogin.setVehwjzt(VehCheckLogin.WJZT_JYJS);
			vehCheckLogin.setExternalCheckDate(new Date());
			
			if(user!=null){
				vehCheckLogin.setWjy(user.getRealName());
				vehCheckLogin.setWjysfzh(user.getIdCard());
			}
			this.hibernateTemplate.update(vehCheckLogin);
			message.setMessage("上传成功");
			message.setState(Message.STATE_SUCCESS);
		} else {
			message.setMessage("无法找到该机动车的登陆信息");
			message.setState(Message.STATE_ERROR);
		}
		
		return message;
	}

	public List<VehCheckLogin> getExternalCheckVhe(String hphm) {

		String sql = "from VehCheckLogin where vehwjzt=?";

		List values = new ArrayList();

		values.add(VehCheckLogin.WJZT_WKS);

		if (hphm != null) {
			sql += " and hphm=?";
			values.add(hphm);
		} else {
			sql += " and dlsj>=?";
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MONTH, -1);
			values.add(c.getTime());
		}
		return (List<VehCheckLogin>) this.hibernateTemplate.find(sql, values.toArray());

	}
	
	public Message savePhoto(CheckPhoto checkPhoto){
		
		
		
		this.hibernateTemplate.save(checkPhoto);
		
		Message message = new Message();
		message.setMessage("上传成功");
		message.setState(Message.STATE_SUCCESS);
		
		return message;
		
	}

}
