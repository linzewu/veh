package com.xs.veh.manager;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.jdbc.ReturningWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.common.Message;
import com.xs.rca.ws.client.TmriJaxRpcOutAccessServiceStub;
import com.xs.rca.ws.client.TmriJaxRpcOutAccessServiceStub.QueryObjectOutResponse;
import com.xs.rca.ws.client.TmriJaxRpcOutAccessServiceStub.WriteObjectOutResponse;
import com.xs.veh.entity.BaseParams;
import com.xs.veh.entity.CheckEvents;
import com.xs.veh.entity.CheckQueue;
import com.xs.veh.entity.Device;
import com.xs.veh.entity.ExternalCheck;
import com.xs.veh.entity.Flow;
import com.xs.veh.entity.User;
import com.xs.veh.entity.VehCheckLog;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.util.BeanXMLUtil;
import com.xs.veh.util.RCAConstant;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

@Service("vehManager")
public class VehManager {

	@Resource(name = "checkQueueManager")
	private CheckQueueManager checkQueueManager;

	private static Logger logger = Logger.getLogger(VehManager.class);

	@Resource(name = "tmriJaxRpcOutAccessServiceStub")
	private TmriJaxRpcOutAccessServiceStub tro;

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	@Value("${jkxlh}")
	private String jkxlh;

	@Value("${jyjgbh}")
	private String jyjgbh;

	@Value("${jylshqz}")
	private String jylshqz;

	@Value("${isNetwork}")
	private boolean isNetwork;

	@Resource(name = "baseParamsManager")
	private BaseParamsManager baseParamsManager;

	@Resource(name = "flowManager")
	private FlowManager flowManager;

	@Resource(name = "checkEventManger")
	private CheckEventManger checkEventManger;

	@Autowired
	private HttpSession session;

	private Document write(String jkid, Map data)
			throws RemoteException, UnsupportedEncodingException, DocumentException {
		TmriJaxRpcOutAccessServiceStub.WriteObjectOut woo = new TmriJaxRpcOutAccessServiceStub.WriteObjectOut();
		woo.setJkid(jkid);
		woo.setXtlb(RCAConstant.XTLB);
		woo.setJkxlh(jkxlh);
		Document xml = BeanXMLUtil.map2xml(data, "vehispara");
		String bo = xml.asXML();
		woo.setUTF8XmlDoc(bo);
		WriteObjectOutResponse wor = tro.writeObjectOut(woo);
		String response = wor.getWriteObjectOutReturn();
		response = URLDecoder.decode(response, "utf-8");
		Document document = DocumentHelper.parseText(response);

		Element root = document.getRootElement();
		Element head = root.element("head");
		Element code = head.element("code");
		Element message = head.element("message");

		logger.info(document.asXML());

		String hphm = (String) data.get("hphm");
		String jylsh = (String) data.get("jylsh");
		String hpzl = (String) data.get("hphm");
		String clsbdh = (String) data.get("clsbdh");
		String fhxml = document.asXML();
		VehCheckLog vcl = new VehCheckLog();
		vcl.setHphm(hphm);
		vcl.setJylsh(jylsh);
		vcl.setHpzl(hpzl);
		vcl.setClsbdh(clsbdh);
		vcl.setFhxml(fhxml);
		vcl.setCode(code.getText());
		vcl.setBo(bo);
		vcl.setMessager(message.getText());
		this.hibernateTemplate.save(vcl);
		return document;
	}

	private Document queryws(String jkid, Map param)
			throws RemoteException, UnsupportedEncodingException, DocumentException {

		TmriJaxRpcOutAccessServiceStub.QueryObjectOut qoo = new TmriJaxRpcOutAccessServiceStub.QueryObjectOut();
		param.put("jyjgbh", jyjgbh);

		qoo.setJkid(jkid);
		qoo.setXtlb(RCAConstant.XTLB);
		qoo.setJkxlh(jkxlh);
		Document xml = BeanXMLUtil.map2xml(param, "QueryCondition");
		qoo.setUTF8XmlDoc(xml.asXML());
		QueryObjectOutResponse qoor = tro.queryObjectOut(qoo);

		String response = qoor.getQueryObjectOutReturn();
		response = URLDecoder.decode(response, "utf-8");
		Document document = DocumentHelper.parseText(response);
		return document;
	}

	public JSON getVehInfoOfws(Map param) throws RemoteException, UnsupportedEncodingException, DocumentException {

		Document document = this.queryws(RCAConstant.V18C49, param);

		return new XMLSerializer().read(document.asXML());
	}

	public JSON getVehCheckItem(Map param) throws RemoteException, UnsupportedEncodingException, DocumentException {
		Document document = this.queryws(RCAConstant.V18C46, param);

		return new XMLSerializer().read(document.asXML());
	}

	public JSONObject vehLogin(VehCheckLogin vehCheckLogin)
			throws RemoteException, UnsupportedEncodingException, DocumentException {
		Flow flow = flowManager.getFlow(Integer.parseInt(vehCheckLogin.getJcxdh()), vehCheckLogin.getCheckType());
		JSONObject head = new JSONObject();
		JSONObject messager = null;

		if (flow == null) {
			head.put("code", "-1");
			head.put("message", "无法获取检测流程");
			messager = new JSONObject();
			messager.put("head", head);
			return messager;
		}

		// 默认情况下为成功
		head.put("code", "1");

		/*
		 * if (isNetwork) { JSONObject param =
		 * JSONObject.fromObject(vehCheckLogin); Document document =
		 * this.write(RCAConstant.V18C51, param); JSON json = new
		 * XMLSerializer().read(document.asXML()); String josnStr =
		 * json.toString(); if (json.isArray()) { JSONArray ja =
		 * JSONArray.fromObject(josnStr); head = ja.getJSONObject(0); messager =
		 * head; } else { JSONObject jo = JSONObject.fromObject(josnStr); head =
		 * jo.getJSONObject("head"); messager = jo; } }
		 */

		if (head.get("code").equals("1")) {
			this.hibernateTemplate.save(vehCheckLogin);
			String jyxm = vehCheckLogin.getJyxm();
			String[] jyxmArray = jyxm.split(",");
			List<VehCheckProcess> processArray = new ArrayList<VehCheckProcess>();
			for (String jyxmItem : jyxmArray) {
				VehCheckProcess vcp = new VehCheckProcess();
				vcp.setClsbdh(vehCheckLogin.getClsbdh());
				vcp.setHphm(vehCheckLogin.getHphm());
				vcp.setHpzl(vehCheckLogin.getHpzl());
				vcp.setJylsh(vehCheckLogin.getJylsh());
				vcp.setJyxm(jyxmItem);
				vcp.setJycs(vehCheckLogin.getJycs());
				this.hibernateTemplate.save(vcp);
				processArray.add(vcp);
			}
			addVehFlow(vehCheckLogin, processArray, flow);
			createExternalCheck(vehCheckLogin);

			CheckEvents ce = new CheckEvents();
			ce.setClsbdh(vehCheckLogin.getClsbdh());
			ce.setEvent("18C51");
			ce.setHpzl(vehCheckLogin.getHpzl());
			ce.setJylsh(vehCheckLogin.getJylsh());
			ce.setHphm(vehCheckLogin.getHphm());
			ce.setCreateDate(new Date());
			ce.setState(0);
			ce.setJycs(vehCheckLogin.getJycs());
			this.hibernateTemplate.save(ce);

			CheckEvents ce2 = new CheckEvents();
			ce2.setClsbdh(vehCheckLogin.getClsbdh());
			ce2.setEvent("18C52");
			ce2.setHpzl(vehCheckLogin.getHpzl());
			ce2.setJylsh(vehCheckLogin.getJylsh());
			ce2.setHphm(vehCheckLogin.getHphm());
			ce2.setCreateDate(new Date());
			ce2.setState(0);
			ce2.setJycs(vehCheckLogin.getJycs());
			this.hibernateTemplate.save(ce2);

		}

		if (!isNetwork) {
			head = new JSONObject();
			head.put("code", 1);
			head.put("isNetwork", false);
			messager = new JSONObject();
			messager.put("head", head);
		}

		return messager;
	}

	private void createExternalCheck(VehCheckLogin vehCheckLogin) {

		ExternalCheck ec = new ExternalCheck();

		ec.setHphm(vehCheckLogin.getHphm());
		ec.setHpzl(vehCheckLogin.getHpzl());
		ec.setJylsh(vehCheckLogin.getJylsh());
		ec.setJyjgbh(vehCheckLogin.getJyjgbh());
		ec.setJycs(vehCheckLogin.getJycs());
		this.hibernateTemplate.save(ec);

	}

	public boolean isLoged(VehCheckLogin vehCheckLogin) {

		String hphm = vehCheckLogin.getHphm();
		String hpzl = vehCheckLogin.getHpzl();

		List data = hibernateTemplate.find("from VehCheckLogin where hphm=? and hpzl=? and ( vehjczt=? or vehjczt=? )",
				hphm, hpzl, VehCheckLogin.JCZT_DL, VehCheckLogin.JCZT_JYZ);

		if (data != null && !data.isEmpty()) {
			return true;
		} else {
			return false;
		}

	}

	public String getJylsh() {
		return this.hibernateTemplate.execute(new HibernateCallback<String>() {
			@Override
			public String doInHibernate(Session session) throws HibernateException {
				return session.doReturningWork(new ReturningWork<String>() {
					@Override
					public String execute(Connection connection) throws SQLException {
						CallableStatement cstmt = connection.prepareCall("{CALL getJYLSH(?,?,?)}");
						cstmt.setString(1, "jylsh");
						cstmt.setString(2, jylshqz);
						cstmt.registerOutParameter(3, Types.VARCHAR);
						cstmt.executeUpdate();
						return cstmt.getString(3);
					}
				});
			}
		});
	}

	/**
	 * 通过检验流水查询检验项目
	 * 
	 * @param jylsh
	 * @return
	 */
	public List<VehCheckProcess> getVehCheckPrcoessByJylsh(String jylsh) {

		List<VehCheckProcess> vcps = (List<VehCheckProcess>) this.hibernateTemplate
				.find("From VehCheckProcess where jylsh =? order by jylsh desc", jylsh);
		return vcps;
	}

	public List<VehCheckLogin> getVehChecking(Integer page, Integer rows, VehCheckLogin vehCheckLogin, Integer[] jczt) {

		DetachedCriteria query = DetachedCriteria.forClass(VehCheckLogin.class);

		if (vehCheckLogin.getHphm() != null && !"".equals(vehCheckLogin.getHphm())) {
			query.add(Restrictions.like("hphm", "%" + vehCheckLogin.getHphm() + "%"));
		}

		if (vehCheckLogin.getClxh() != null && !"".equals(vehCheckLogin.getClxh())) {
			query.add(Restrictions.like("clxh", "%" + vehCheckLogin.getClxh() + "%"));
		}

		if (vehCheckLogin.getHpzl() != null && !"".equals(vehCheckLogin.getHpzl())) {
			query.add(Restrictions.eq("hpzl", vehCheckLogin.getHpzl()));
		}
		if (jczt != null) {
			query.add(Restrictions.in("vehjczt", jczt));
		}

		query.addOrder(Order.desc("jylsh"));

		Integer firstResult = (page - 1) * rows;

		List<VehCheckLogin> vcps = (List<VehCheckLogin>) this.hibernateTemplate.findByCriteria(query, firstResult,
				rows);

		return vcps;
	}

	public Integer getVehCheckingCount(Integer page, Integer rows, VehCheckLogin vehCheckLogin, Integer[] jczt) {

		DetachedCriteria query = DetachedCriteria.forClass(VehCheckLogin.class);

		if (vehCheckLogin.getHphm() != null && !"".equals(vehCheckLogin.getHphm())) {
			query.add(Restrictions.like("hphm", "%" + vehCheckLogin.getHphm() + "%"));
		}

		if (vehCheckLogin.getClxh() != null && !"".equals(vehCheckLogin.getClxh())) {
			query.add(Restrictions.like("clxh", "%" + vehCheckLogin.getClxh() + "%"));
		}

		if (vehCheckLogin.getHpzl() != null && !"".equals(vehCheckLogin.getHpzl())) {
			query.add(Restrictions.eq("hpzl", vehCheckLogin.getHpzl()));
		}
		if (jczt != null) {
			query.add(Restrictions.in("vehjczt", jczt));
		}

		query.setProjection(Projections.rowCount());

		List<Long> count = (List<Long>) hibernateTemplate.findByCriteria(query);

		return count.get(0).intValue();
	}

	/**
	 * 退办逻辑
	 * 
	 * @param id
	 * @return
	 * @throws RemoteException
	 * @throws UnsupportedEncodingException
	 * @throws DocumentException
	 */
	public JSONObject unLoginVeh(Integer id) throws RemoteException, UnsupportedEncodingException, DocumentException {

		VehCheckLogin vheLogininfo = this.hibernateTemplate.load(VehCheckLogin.class, id);
		final String jylsh = vheLogininfo.getJylsh();
		JSONObject jo = new JSONObject();
		JSONObject jsonHead = new JSONObject();
		jsonHead.put("code", "1");
		jsonHead.put("isNetwork", isNetwork);
		jo.put("head", jsonHead);

		vheLogininfo.setVehjczt(VehCheckLogin.JCZT_TB);
		this.hibernateTemplate.update(vheLogininfo);
		// 同时修改 上线表 队列表 状态 为退办
		this.hibernateTemplate.execute(new HibernateCallback<Integer>() {
			@Override
			public Integer doInHibernate(Session session) throws HibernateException {
				return session.createQuery("delete CheckQueue where jylsh=?").setParameter(0, jylsh).executeUpdate();
			}

		});

		return jo;
	}

	private List<VehFlow> addVehFlow(VehCheckLogin vcl, List<VehCheckProcess> process, Flow flow) {

		JSONArray flowJsons = JSONArray.fromObject(flow.getFlow());

		List<VehFlow> vehFlows = new ArrayList<VehFlow>();

		for (int i = 0; i < flowJsons.size(); i++) {

			JSONObject workPoint = flowJsons.getJSONObject(i);

			Integer gwid = workPoint.getInt("id");

			JSONArray sbs = workPoint.getJSONArray("sbs");

			for (int j = 0; j < sbs.size(); j++) {

				JSONObject sb = sbs.getJSONObject(j);

				Integer deviceId = sb.getInt("id");

				Device device = hibernateTemplate.get(Device.class, deviceId);

				String strJyxm = getDeivceItem(device, process);

				if (deviceId == -1 && vcl.getJyxm().indexOf("C1") != -1) {
					VehFlow v = new VehFlow();
					v.setGw(gwid);
					v.setHphm(vcl.getHphm());
					v.setHpzl(vcl.getHpzl());
					v.setJylsh(vcl.getJylsh());
					v.setJycs(vcl.getJycs());
					v.setJyxm("C1");
					v.setJysb(-1);
					v.setGwsx(i + 1);
					v.setSbsx(j + 1);
					v.setSbid(deviceId);
					vehFlows.add(v);
				}

				if (strJyxm != null && !strJyxm.equals("")) {
					String[] jyxmArray = strJyxm.split(",");
					for (String jyxm : jyxmArray) {
						// 如果是驻车制动 需要根据驻车轴为来生成
						if (jyxm.equals("B0")) {
							if (device.getType() == Device.ZDJCSB || device.getType() == Device.ZDPBSB) {
								String zczw = vcl.getZczw();
								for (int k = 0; k < zczw.length(); k++) {
									VehFlow v = new VehFlow();
									v.setGw(gwid);
									v.setHphm(vcl.getHphm());
									v.setHpzl(vcl.getHpzl());
									v.setJylsh(vcl.getJylsh());
									v.setJycs(vcl.getJycs());
									v.setJyxm(jyxm);
									v.setJysb(device.getId());
									v.setGwsx(i + 1);
									v.setSbsx(j + 1);
									v.setMemo(String.valueOf(zczw.charAt(k)));
									v.setSbid(deviceId);
									vehFlows.add(v);
								}
							}
						} else {
							VehFlow v = new VehFlow();
							v.setGw(gwid);
							v.setHphm(vcl.getHphm());
							v.setHpzl(vcl.getHpzl());
							v.setJylsh(vcl.getJylsh());
							v.setJycs(vcl.getJycs());
							v.setJyxm(jyxm);
							v.setJysb(device.getId());
							v.setGwsx(i + 1);
							v.setSbsx(j + 1);
							v.setSbid(deviceId);
							vehFlows.add(v);
						}
					}
				}
			}
		}

		VheFlowComparator comparator = new VheFlowComparator(flow);

		Collections.sort(vehFlows, comparator);

		int i = 1;

		for (VehFlow vehFlow : vehFlows) {
			vehFlow.setSx(i);
			this.hibernateTemplate.save(vehFlow);
			i++;
		}
		return vehFlows;
	}

	private String getDeivceItem(Device device, List<VehCheckProcess> vehCheckProcessArray) {

		if (device == null || vehCheckProcessArray == null) {
			return null;
		}

		BaseParams param = baseParamsManager.getBaseParam("deviceType", device.getType().toString());

		String strConfig = param.getMemo();

		String[] config = strConfig.split(",");

		String jyxm = "";

		for (VehCheckProcess process : vehCheckProcessArray) {
			for (String item : config) {
				if (item.equals(process.getJyxm())) {
					jyxm += process.getJyxm() + ",";
				}
			}
		}

		if (jyxm.length() > 0) {
			jyxm = jyxm.substring(0, jyxm.length() - 1);
		}

		return jyxm;
	}

	public List<VehCheckLogin> getVehCheckLoginOfSXZT(Integer zt) {

		DetachedCriteria detachedCrit = DetachedCriteria.forClass(VehCheckLogin.class);
		List<VehCheckLogin> vheCheckLogins = (List<VehCheckLogin>) this.hibernateTemplate
				.find("from VehCheckLogin where vehsxzt = ?", zt);
		return vheCheckLogins;
	}

	public Message upLine(Integer id) {

		VehCheckLogin vehCheckLogin = this.hibernateTemplate.load(VehCheckLogin.class, id);
		User user = (User) session.getAttribute("user");

		Message message = new Message();

		if (vehCheckLogin.getVehsxzt() == VehCheckLogin.ZT_JYJS || vehCheckLogin.getVehsxzt() == VehCheckLogin.ZT_JCZ) {
			message.setState(Message.STATE_ERROR);
			message.setMessage("引车上线失败，该流水已上线！");
			return message;

		} else {
			// 获取第一顺序流程
			VehFlow firstVehFlow = (VehFlow) this.hibernateTemplate
					.find("from VehFlow where jylsh=? and jycs=? and sx=1 order by sx asc", vehCheckLogin.getJylsh(),
							vehCheckLogin.getJycs())
					.get(0);

			// 获取同一工位的流程
			List<VehFlow> vehFlows = (List<VehFlow>) this.hibernateTemplate.find(
					"from VehFlow where jylsh=? and jycs=? and gw=? order by sx asc", vehCheckLogin.getJylsh(),
					vehCheckLogin.getJycs(), firstVehFlow.getGw());

			for (VehFlow vehFlow : vehFlows) {
				CheckQueue queue = new CheckQueue();
				queue.setGwsx(vehFlow.getGwsx());
				queue.setHphm(vehFlow.getHphm());
				queue.setHpzl(vehFlow.getHpzl());
				queue.setJcxdh(Integer.parseInt(vehCheckLogin.getJcxdh()));
				queue.setJycs(vehFlow.getJycs());
				queue.setJylsh(vehFlow.getJylsh());
				queue.setPdxh(checkQueueManager.getPdxh(vehCheckLogin.getJcxdh(), vehFlow.getGwsx()));
				queue.setLcsx(vehFlow.getSx());
				this.hibernateTemplate.save(queue);
			}

			vehCheckLogin.setVehsxzt(VehCheckLogin.ZT_JCZ);
			vehCheckLogin.setUpLineDate(new Date());

			if (user != null) {
				vehCheckLogin.setYcy(user.getRealName());
				vehCheckLogin.setYcysfzh(user.getIdCard());
			}

			this.hibernateTemplate.update(vehCheckLogin);

			message.setState(Message.STATE_SUCCESS);
			message.setMessage("上线成功！");

			return message;

		}
	}

	public VehCheckLogin getVehCheckLoginByJylsh(String jyjgbh, String jylsh) {
		List<VehCheckLogin> vehCheckLogins = (List<VehCheckLogin>) this.hibernateTemplate
				.find("from VehCheckLogin where jyjgbh=? and jylsh=?", jyjgbh, jylsh);

		if (vehCheckLogins != null && vehCheckLogins.size() > 0) {
			return vehCheckLogins.get(0);
		} else {
			return null;
		}
	}

	public void updateVehCheckLoginState(String jylsh) {

		List<VehCheckLogin> array = (List<VehCheckLogin>) this.hibernateTemplate
				.find("from VehCheckLogin where jylsh=?", jylsh);

		if (array != null && !array.isEmpty()) {
			VehCheckLogin vehCheckLogin = array.get(0);
			if ((vehCheckLogin.getVehwjzt() == VehCheckLogin.ZT_JYJS
					|| vehCheckLogin.getVehwjzt() == VehCheckLogin.ZT_BJC)
					&& (vehCheckLogin.getVehdpzt() == VehCheckLogin.ZT_JYJS
							|| vehCheckLogin.getVehdpzt() == VehCheckLogin.ZT_BJC)
					&& (vehCheckLogin.getVehdtdpzt() == VehCheckLogin.ZT_JYJS
							|| vehCheckLogin.getVehdtdpzt() == VehCheckLogin.ZT_BJC)
					&& (vehCheckLogin.getVehsxzt() == VehCheckLogin.ZT_JYJS
							|| vehCheckLogin.getVehsxzt() == VehCheckLogin.ZT_BJC)
					&& (vehCheckLogin.getVehlszt() == VehCheckLogin.ZT_JYJS
							|| vehCheckLogin.getVehlszt() == VehCheckLogin.ZT_BJC)) {

				checkEventManger.createEvent(vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(), "18C59", null,
						vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh());
				
				checkEventManger.createEvent(vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(), "18C82", null,
						vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh());

				vehCheckLogin.setVehjczt(VehCheckLogin.JCZT_JYJS);
			}

		}

	}

}
