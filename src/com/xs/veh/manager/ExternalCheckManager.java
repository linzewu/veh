package com.xs.veh.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.common.Message;
import com.xs.veh.entity.CheckPhoto;
import com.xs.veh.entity.ExternalCheck;
import com.xs.veh.entity.ExternalCheckJudge;
import com.xs.veh.entity.User;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;

@Service("externalCheckManager")
public class ExternalCheckManager {

	Logger logger = Logger.getLogger(ExternalCheckManager.class);

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	@Resource(name = "vehManager")
	private VehManager vehManager;

	@Autowired
	private HttpSession session;

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;

	@Resource(name = "checkEventManger")
	private CheckEventManger checkEventManger;

	private void createExternalCheckJudge(ExternalCheck externalCheck) {

		int index = 1;
		String item1 = externalCheck.getItem1() + externalCheck.getItem2() + externalCheck.getItem3()
				+ externalCheck.getItem4() + externalCheck.getItem5();
		if (!item1.equals("00000")) {
			ExternalCheckJudge ecj = new ExternalCheckJudge();
			ecj.setHphm(externalCheck.getHphm());
			ecj.setHpzl(externalCheck.getHpzl());
			ecj.setJylsh(externalCheck.getJylsh());
			ecj.setJycs(externalCheck.getJycs());
			ecj.setJyjgbh(externalCheck.getJyjgbh());
			if (item1.indexOf("2") != -1) {
				ecj.setRgjgpd(ExternalCheck.PD_BHG);
			} else {
				ecj.setRgjgpd(ExternalCheck.PD_HG);
			}
			this.hibernateTemplate.save(ecj);
		}

		String item2 = externalCheck.getItem6() + externalCheck.getItem7() + externalCheck.getItem8()
				+ externalCheck.getItem9() + externalCheck.getItem10() + externalCheck.getItem11()
				+ externalCheck.getItem12() + externalCheck.getItem13() + externalCheck.getItem14()
				+ externalCheck.getItem15();

		if (!item2.equals("0000000000")) {
			ExternalCheckJudge ecj = new ExternalCheckJudge();
			ecj.setHphm(externalCheck.getHphm());
			ecj.setHpzl(externalCheck.getHpzl());
			ecj.setJylsh(externalCheck.getJylsh());
			ecj.setJycs(externalCheck.getJycs());
			ecj.setJyjgbh(externalCheck.getJyjgbh());
			if (item1.indexOf("2") != -1) {
				ecj.setRgjgpd(ExternalCheck.PD_BHG);
			} else {
				ecj.setRgjgpd(ExternalCheck.PD_HG);
			}
			this.hibernateTemplate.save(ecj);
		}

		String item3 = externalCheck.getItem16() + externalCheck.getItem17() + externalCheck.getItem18()
				+ externalCheck.getItem19() + externalCheck.getItem20() + externalCheck.getItem21();

		if (!item3.equals("000000")) {
			ExternalCheckJudge ecj = new ExternalCheckJudge();
			ecj.setHphm(externalCheck.getHphm());
			ecj.setHpzl(externalCheck.getHpzl());
			ecj.setJylsh(externalCheck.getJylsh());
			ecj.setJycs(externalCheck.getJycs());
			ecj.setJyjgbh(externalCheck.getJyjgbh());
			if (item1.indexOf("2") != -1) {
				ecj.setRgjgpd(ExternalCheck.PD_BHG);
			} else {
				ecj.setRgjgpd(ExternalCheck.PD_HG);
			}
			this.hibernateTemplate.save(ecj);
		}

		String item4 = externalCheck.getItem22() + externalCheck.getItem23() + externalCheck.getItem24()
				+ externalCheck.getItem25() + externalCheck.getItem26() + externalCheck.getItem27()
				+ externalCheck.getItem28() + externalCheck.getItem29() + externalCheck.getItem30()
				+ externalCheck.getItem31() + externalCheck.getItem32() + externalCheck.getItem33()
				+ externalCheck.getItem34() + externalCheck.getItem35() + externalCheck.getItem36()
				+ externalCheck.getItem37() + externalCheck.getItem38() + externalCheck.getItem39()
				+ externalCheck.getItem40() + externalCheck.getItem41();

		if (!item4.equals("00000000000000000000")) {
			ExternalCheckJudge ecj = new ExternalCheckJudge();
			ecj.setHphm(externalCheck.getHphm());
			ecj.setHpzl(externalCheck.getHpzl());
			ecj.setJylsh(externalCheck.getJylsh());
			ecj.setJycs(externalCheck.getJycs());
			ecj.setJyjgbh(externalCheck.getJyjgbh());
			if (item1.indexOf("2") != -1) {
				ecj.setRgjgpd(ExternalCheck.PD_BHG);
			} else {
				ecj.setRgjgpd(ExternalCheck.PD_HG);
			}
			this.hibernateTemplate.save(ecj);
		}

		String item5 = externalCheck.getItem80();

		if (!item5.equals("0")) {
			ExternalCheckJudge ecj = new ExternalCheckJudge();
			ecj.setHphm(externalCheck.getHphm());
			ecj.setHpzl(externalCheck.getHpzl());
			ecj.setJylsh(externalCheck.getJylsh());
			ecj.setJycs(externalCheck.getJycs());
			ecj.setJyjgbh(externalCheck.getJyjgbh());
			if (item1.indexOf("2") != -1) {
				ecj.setRgjgpd(ExternalCheck.PD_BHG);
			} else {
				ecj.setRgjgpd(ExternalCheck.PD_HG);
			}
			this.hibernateTemplate.save(ecj);
		}

		String item6 = externalCheck.getItem42() + externalCheck.getItem43() + externalCheck.getItem44()
				+ externalCheck.getItem45();

		if (!item6.equals("0000")) {
			ExternalCheckJudge ecj = new ExternalCheckJudge();
			ecj.setHphm(externalCheck.getHphm());
			ecj.setHpzl(externalCheck.getHpzl());
			ecj.setJylsh(externalCheck.getJylsh());
			ecj.setJycs(externalCheck.getJycs());
			ecj.setJyjgbh(externalCheck.getJyjgbh());
			if (item1.indexOf("2") != -1) {
				ecj.setRgjgpd(ExternalCheck.PD_BHG);
			} else {
				ecj.setRgjgpd(ExternalCheck.PD_HG);
			}
			this.hibernateTemplate.save(ecj);
		}

		String item7 = externalCheck.getItem46() + externalCheck.getItem47() + externalCheck.getItem48()
				+ externalCheck.getItem49() + externalCheck.getItem50();

		if (!item7.equals("00000")) {
			ExternalCheckJudge ecj = new ExternalCheckJudge();
			ecj.setHphm(externalCheck.getHphm());
			ecj.setHpzl(externalCheck.getHpzl());
			ecj.setJylsh(externalCheck.getJylsh());
			ecj.setJycs(externalCheck.getJycs());
			ecj.setJyjgbh(externalCheck.getJyjgbh());
			if (item1.indexOf("2") != -1) {
				ecj.setRgjgpd(ExternalCheck.PD_BHG);
			} else {
				ecj.setRgjgpd(ExternalCheck.PD_HG);
			}
			this.hibernateTemplate.save(ecj);
		}

	}

	private ExternalCheck setExternalCheck(VehCheckLogin vehCheckLogin, ExternalCheck externalCheck) {

		ExternalCheck ec = (ExternalCheck) this.hibernateTemplate
				.find("from ExternalCheck where jylsh=? ", vehCheckLogin.getJylsh()).get(0);

		ec.setItem1(externalCheck.getItem1());
		ec.setItem2(externalCheck.getItem2());
		ec.setItem3(externalCheck.getItem3());
		ec.setItem4(externalCheck.getItem4());
		ec.setItem5(externalCheck.getItem5());
		ec.setItem6(externalCheck.getItem6());
		ec.setItem7(externalCheck.getItem7());
		ec.setItem8(externalCheck.getItem8());
		ec.setItem9(externalCheck.getItem9());
		ec.setItem10(externalCheck.getItem10());
		ec.setItem11(externalCheck.getItem11());
		ec.setItem12(externalCheck.getItem12());
		ec.setItem13(externalCheck.getItem13());
		ec.setItem14(externalCheck.getItem14());
		ec.setItem15(externalCheck.getItem15());
		ec.setItem16(externalCheck.getItem16());
		ec.setItem17(externalCheck.getItem17());
		ec.setItem18(externalCheck.getItem18());
		ec.setItem19(externalCheck.getItem19());
		ec.setItem20(externalCheck.getItem20());
		ec.setItem21(externalCheck.getItem21());
		ec.setItem22(externalCheck.getItem22());
		ec.setItem23(externalCheck.getItem23());
		ec.setItem24(externalCheck.getItem24());
		ec.setItem25(externalCheck.getItem25());
		ec.setItem26(externalCheck.getItem26());
		ec.setItem27(externalCheck.getItem27());
		ec.setItem28(externalCheck.getItem28());
		ec.setItem29(externalCheck.getItem29());
		ec.setItem30(externalCheck.getItem30());
		ec.setItem31(externalCheck.getItem31());
		ec.setItem32(externalCheck.getItem32());
		ec.setItem33(externalCheck.getItem33());
		ec.setItem34(externalCheck.getItem34());
		ec.setItem35(externalCheck.getItem35());
		ec.setItem36(externalCheck.getItem36());
		ec.setItem37(externalCheck.getItem37());
		ec.setItem38(externalCheck.getItem38());
		ec.setItem39(externalCheck.getItem39());
		ec.setItem40(externalCheck.getItem40());
		ec.setItem41(externalCheck.getItem41());
		ec.setItem80(externalCheck.getItem80());

		User user = (User) session.getAttribute("user");
		ec.setWgjcjyy(user.getRealName());
		ec.setWgjcjyysfzh(user.getIdCard());

		return ec;
	}

	private ExternalCheck setExternalCheckDC(VehCheckLogin vehCheckLogin, ExternalCheck externalCheck) {

		ExternalCheck ec = (ExternalCheck) this.hibernateTemplate
				.find("from ExternalCheck where jylsh=? ", vehCheckLogin.getJylsh()).get(0);

		ec.setItem42(externalCheck.getItem42());
		ec.setItem43(externalCheck.getItem43());
		ec.setItem44(externalCheck.getItem44());
		ec.setItem45(externalCheck.getItem45());
		User user = (User) session.getAttribute("user");
		ec.setDpdtjyy(user.getRealName());
		ec.setDpdtjyysfzh(user.getIdCard());

		return ec;
	}

	private ExternalCheck setExternalCheckC1(VehCheckLogin vehCheckLogin, ExternalCheck externalCheck) {

		ExternalCheck ec = (ExternalCheck) this.hibernateTemplate
				.find("from ExternalCheck where jylsh=? ", vehCheckLogin.getJylsh()).get(0);
		ec.setItem46(externalCheck.getItem46());
		ec.setItem47(externalCheck.getItem47());
		ec.setItem48(externalCheck.getItem48());
		ec.setItem49(externalCheck.getItem49());
		ec.setItem50(externalCheck.getItem50());

		User user = (User) session.getAttribute("user");
		ec.setDpjcjyy(user.getRealName());
		ec.setDpjyysfzh(user.getIdCard());
		return ec;
	}

	/**
	 * 保存外检测信息
	 * 
	 * @param externalCheck
	 * @return
	 * @throws InterruptedException
	 */
	public Message saveExternalCheck(ExternalCheck externalCheck) throws InterruptedException {
		VehCheckLogin vehCheckLogin = vehManager.getVehCheckLoginByJylsh(externalCheck.getJyjgbh(),
				externalCheck.getJylsh());

		ExternalCheck ec = setExternalCheck(vehCheckLogin, externalCheck);

		Message message = new Message();
		if (vehCheckLogin != null) {
			this.hibernateTemplate.update(ec);

			vehCheckLogin.setVehwjzt(VehCheckLogin.ZT_JYJS);
			vehCheckLogin.setExternalCheckDate(new Date());
			User user = (User) session.getAttribute("user");
			if (user != null) {
				vehCheckLogin.setWjy(user.getRealName());
				vehCheckLogin.setWjysfzh(user.getIdCard());
			}
			this.hibernateTemplate.update(vehCheckLogin);

			VehCheckProcess vehCheckProcess = checkDataManager.getVehCheckProces(vehCheckLogin.getJylsh(),
					vehCheckLogin.getJycs(), "F1");
			vehCheckProcess.setJssj(new Date());
			this.checkDataManager.updateProcess(vehCheckProcess);
			checkEventManger.createEvent(vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(), "18C80", "F1",
					vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(),vehCheckLogin.getVehcsbj());
			Thread.sleep(1000);
			checkEventManger.createEvent(vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(), "18C58", "F1",
					vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(),vehCheckLogin.getVehcsbj());

			// 判断项目的状态
			vehManager.updateVehCheckLoginState(vehCheckLogin.getJylsh());
			message.setMessage("上传成功");
			message.setState(Message.STATE_SUCCESS);
		} else {
			message.setMessage("无法找到该机动车的登陆信息");
			message.setState(Message.STATE_ERROR);
		}
		return message;
	}

	/**
	 * 动态底盘
	 * 
	 * @param externalCheck
	 * @return
	 * @throws InterruptedException
	 */
	public Message saveExternalCheckDC(ExternalCheck externalCheck) throws InterruptedException {
		VehCheckLogin vehCheckLogin = vehManager.getVehCheckLoginByJylsh(

				externalCheck.getJyjgbh(), externalCheck.getJylsh());

		ExternalCheck ec = setExternalCheckDC(vehCheckLogin, externalCheck);

		Message message = new Message();
		if (vehCheckLogin != null) {
			this.hibernateTemplate.update(ec);
			vehCheckLogin.setVehdtdpzt(VehCheckLogin.ZT_JYJS);
			this.hibernateTemplate.update(vehCheckLogin);
			// 判断项目的状态

			VehCheckProcess vehCheckProcess = checkDataManager.getVehCheckProces(vehCheckLogin.getJylsh(),
					vehCheckLogin.getJycs(), "DC");
			vehCheckProcess.setJssj(new Date());
			this.checkDataManager.updateProcess(vehCheckProcess);
			checkEventManger.createEvent(vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(), "18C80", "DC",
					vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(),vehCheckLogin.getVehcsbj());
			Thread.sleep(1000);
			checkEventManger.createEvent(vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(), "18C58", "DC",
					vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(),vehCheckLogin.getVehcsbj());

			vehManager.updateVehCheckLoginState(vehCheckLogin.getJylsh());

			message.setMessage("上传成功");
			message.setState(Message.STATE_SUCCESS);
		} else {
			message.setMessage("无法找到该机动车的登陆信息");
			message.setState(Message.STATE_ERROR);
		}
		return message;
	}

	/**
	 * 动态底盘
	 * 
	 * @param externalCheck
	 * @return
	 * @throws InterruptedException
	 */
	public Message saveExternalCheckC1(ExternalCheck externalCheck) throws InterruptedException {
		VehCheckLogin vehCheckLogin = vehManager.getVehCheckLoginByJylsh(externalCheck.getJyjgbh(),
				externalCheck.getJylsh());

		ExternalCheck ec = setExternalCheckC1(vehCheckLogin, externalCheck);

		Message message = new Message();
		if (vehCheckLogin != null) {
			this.hibernateTemplate.update(ec);
			vehCheckLogin.setVehdpzt(VehCheckLogin.ZT_JYJS);
			this.hibernateTemplate.update(vehCheckLogin);

			VehCheckProcess vehCheckProcess = checkDataManager.getVehCheckProces(vehCheckLogin.getJylsh(),
					vehCheckLogin.getJycs(), "C1");
			vehCheckProcess.setJssj(new Date());
			this.checkDataManager.updateProcess(vehCheckProcess);
			checkEventManger.createEvent(vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(), "18C80", "C1",
					vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(),vehCheckLogin.getVehcsbj());
			Thread.sleep(1000);
			checkEventManger.createEvent(vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(), "18C58", "C1",
					vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(),vehCheckLogin.getVehcsbj());

			// 判断项目的状态
			vehManager.updateVehCheckLoginState(vehCheckLogin.getJylsh());
			message.setMessage("上传成功");
			message.setState(Message.STATE_SUCCESS);
		} else {
			message.setMessage("无法找到该机动车的登陆信息");
			message.setState(Message.STATE_ERROR);
		}
		return message;
	}

	public List<VehCheckLogin> getExternalCheckVhe(String hphm) {

		String sql = "from VehCheckLogin where vehjczt!=? and vehwjzt=?";

		List values = new ArrayList();

		values.add(VehCheckLogin.JCZT_TB);
		values.add(VehCheckLogin.ZT_WKS);

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

	public List<VehCheckLogin> getVheInfoOfHphm(String hphm) {
		String sql = "from VehCheckLogin where hphm like ? and dlsj>=?";
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, -3);
		return (List<VehCheckLogin>) this.hibernateTemplate.find(sql, "%" + hphm + "%", c.getTime());
	}

	public List<VehCheckLogin> getExternalC1(String hphm) {

		String sql = "from VehCheckLogin where vehjczt!=?  and vehdpzt=?";

		List values = new ArrayList();

		values.add(VehCheckLogin.JCZT_TB);
		values.add(VehCheckLogin.ZT_WKS);

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

	public List<VehCheckLogin> getExternalDC(String hphm) {

		String sql = "from VehCheckLogin where  vehjczt!=? and vehdtdpzt=? ";

		List values = new ArrayList();
		values.add(VehCheckLogin.JCZT_TB);
		values.add(VehCheckLogin.ZT_WKS);

		if (hphm != null) {
			sql += " and hphm=?";
			values.add(hphm);
		} else {
			sql += " and dlsj>=? ";
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MONTH, -1);
			values.add(c.getTime());
		}
		return (List<VehCheckLogin>) this.hibernateTemplate.find(sql, values.toArray());

	}

	public Message savePhoto(final CheckPhoto checkPhoto) {

		this.hibernateTemplate.execute(new HibernateCallback<Integer>() {
			@Override
			public Integer doInHibernate(Session session) throws HibernateException {
				return session.createQuery("delete CheckPhoto where jylsh=:jylsh and zpzl=:zpzl")
						.setParameter("jylsh", checkPhoto.getJylsh()).setParameter("zpzl", checkPhoto.getZpzl())
						.executeUpdate();
			}
		});

		this.hibernateTemplate.save(checkPhoto);
		checkEventManger.createEvent(checkPhoto.getJylsh(), checkPhoto.getJycs(), "18C63", checkPhoto.getJyxm(),
				checkPhoto.getHphm(), checkPhoto.getHpzl(), checkPhoto.getClsbdh(), checkPhoto.getZpzl(),0);

		Message message = new Message();
		message.setMessage("上传成功");
		message.setState(Message.STATE_SUCCESS);

		return message;

	}

	public List<VehCheckLogin> getExternalR(String hphm) {

		String sql = "from VehCheckLogin where  vehjczt!=? and (vehlszt=? or vehlszt=?) ";
		List values = new ArrayList();
		values.add(VehCheckLogin.JCZT_TB);
		values.add(VehCheckLogin.ZT_JCZ);
		values.add(VehCheckLogin.ZT_WKS);

		if (hphm != null) {
			sql += " and hphm=?";
			values.add(hphm);
		} else {
			sql += " and dlsj>=? ";
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MONTH, -1);
			values.add(c.getTime());
		}
		return (List<VehCheckLogin>) this.hibernateTemplate.find(sql, values.toArray());

	}

	public List<VehCheckProcess> getRoadProcess(String jylsh) {

		List<VehCheckProcess> datas = (List<VehCheckProcess>) this.hibernateTemplate
				.find("from VehCheckProcess where jylsh=? and jyxm in('R1','R2','R3')", jylsh);

		return datas;

	}

}
