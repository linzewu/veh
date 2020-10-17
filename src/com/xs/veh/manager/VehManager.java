package com.xs.veh.manager;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.xs.common.BaseParamsUtil;
import com.xs.common.Message;
import com.xs.rca.ws.client.TmriJaxRpcOutNewAccessServiceStub;
import com.xs.rca.ws.client.TmriJaxRpcOutNewAccessServiceStub.QueryObjectOutResponse;
import com.xs.rca.ws.client.TmriJaxRpcOutNewAccessServiceStub.WriteObjectOutResponse;
import com.xs.veh.entity.BaseParams;
import com.xs.veh.entity.CheckEvents;
import com.xs.veh.entity.CheckLog;
import com.xs.veh.entity.CheckQueue;
import com.xs.veh.entity.Device;
import com.xs.veh.entity.DeviceCheckJudeg;
import com.xs.veh.entity.ExternalCheck;
import com.xs.veh.entity.ExternalCheckJudge;
import com.xs.veh.entity.Flow;
import com.xs.veh.entity.TestVeh;
import com.xs.veh.entity.User;
import com.xs.veh.entity.VehCheckLog;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.entity.VehInfoTemp;
import com.xs.veh.network.data.BaseDeviceData;
import com.xs.veh.network.data.BrakRollerData;
import com.xs.veh.network.data.CurbWeightData;
import com.xs.veh.network.data.LightData;
import com.xs.veh.network.data.OtherInfoData;
import com.xs.veh.network.data.Outline;
import com.xs.veh.network.data.ParDataOfAnjian;
import com.xs.veh.network.data.SideslipData;
import com.xs.veh.network.data.SpeedData;
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

	@Resource(name = "TmriJaxRpcOutNewAccessServiceStub")
	private TmriJaxRpcOutNewAccessServiceStub tro;

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
	private CheckDataManager checkDataManager;

	@Autowired
	private HttpSession session;

	private Document write(String jkid, Map data)
			throws RemoteException, UnsupportedEncodingException, DocumentException {
		TmriJaxRpcOutNewAccessServiceStub.WriteObjectOut woo = new TmriJaxRpcOutNewAccessServiceStub.WriteObjectOut();
		woo.setJkid(jkid);
		woo.setXtlb(RCAConstant.XTLB);
		woo.setJkxlh(jkxlh);

		String bmdm = baseParamsManager.getBaseParam("jkcs", "bmdm").getParamName();
		String ywzdbm = baseParamsManager.getBaseParam("jkcs", "ywzdbm").getParamName();
		String jcip = baseParamsManager.getBaseParam("jkcs", "jcip").getParamName();
		woo.setDwjgdm(bmdm);
		woo.setDwmc(ywzdbm);
		woo.setZdbs(jcip);

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

	public Document queryws(String jkid, Map param)
			throws RemoteException, UnsupportedEncodingException, DocumentException {

		TmriJaxRpcOutNewAccessServiceStub.QueryObjectOut qoo = new TmriJaxRpcOutNewAccessServiceStub.QueryObjectOut();
		if (jkid.equals("18C01") || jkid.equals("18C02")) {
			param.put("jczbh", jyjgbh);
		} else {
			param.put("jyjgbh", jyjgbh);
		}

		qoo.setJkid(jkid);
		qoo.setXtlb(RCAConstant.XTLB);
		qoo.setJkxlh(jkxlh);
		
		logger.info("接口参数：");
		logger.info("bmdm="+ baseParamsManager.getBaseParam("jkcs", "bmdm").getParamName());
		logger.info("ywzdbm="+ baseParamsManager.getBaseParam("jkcs", "ywzdbm").getParamName());
		logger.info("jcip="+baseParamsManager.getBaseParam("jkcs", "jcip").getParamName());
		String bmdm = baseParamsManager.getBaseParam("jkcs", "bmdm").getParamName();
		String ywzdbm = baseParamsManager.getBaseParam("jkcs", "ywzdbm").getParamName();
		String jcip = baseParamsManager.getBaseParam("jkcs", "jcip").getParamName();
		qoo.setDwjgdm(bmdm);
		qoo.setDwmc(ywzdbm);
		qoo.setZdbs(jcip);  

		Document xml = BeanXMLUtil.map2xml(param, "QueryCondition");
		qoo.setUTF8XmlDoc(xml.asXML());
		QueryObjectOutResponse qoor = tro.queryObjectOut(qoo);

		String response = qoor.getQueryObjectOutReturn();
		response = URLDecoder.decode(response, "utf-8");
		Document document = DocumentHelper.parseText(response);
		return document;
	}

	public JSON getVehInfoOfbookNumberz(Map param)
			throws RemoteException, UnsupportedEncodingException, DocumentException {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ServiceClient serviceClient = new ServiceClient();
		// 创建服务地址WebService的URL,注意不是WSDL的URL
		String url = "http://190.204.20.49:8093/pmot/services/TmriOutAccess?wsdl";
		EndpointReference targetEPR = new EndpointReference(url);
		Options options = serviceClient.getOptions();
		options.setTo(targetEPR);
		// 确定调用方法（wsdl 命名空间地址 (wsdl文档中的targetNamespace) 和 方法名称 的组合）
		options.setAction("http://190.204.20.49:8093/pmot/services/TmriOutAccess/query");
		OMFactory fac = OMAbstractFactory.getOMFactory();
		/*
		 * 指定命名空间，参数： uri--即为wsdl文档的targetNamespace，命名空间 perfix--可不填
		 */
		OMNamespace omNs = fac.createOMNamespace("http://190.204.20.49:8093/pmot/services/TmriOutAccess", "");
		// 指定方法
		OMElement method = fac.createOMElement("query", omNs);
		// 指定方法的参数
		OMElement queryXmlDoc = fac.createOMElement("QueryXmlDoc", omNs);
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("root");
		Element head = root.addElement("head");

		head.addElement("xtlb").setText("18");
		head.addElement("jkxlh").setText(jkxlh);
		head.addElement("jkid").setText("JK01");

		Element queryCondition = root.addElement("QueryCondition");

		queryCondition.addElement("bookNumber").setText(param.get("bookNumber").toString());
		queryCondition.addElement("verifyCode").setText(param.get("verifyCode").toString());
		queryXmlDoc.setText(document.asXML());
		method.addChild(queryXmlDoc);
		method.build();
		// 远程调用web服务
		OMElement result = serviceClient.sendReceive(method);

		// logger.info("result.getText()="+
		// result.getFirstOMChild().getNextOMSibling());

		OMElement element = (OMElement) result.getFirstElement();

		Document dd = DocumentHelper.parseText(element.toString());

		Element ed = (Element) dd.getRootElement();

		String decoStr = URLDecoder.decode(ed.getStringValue(), "UTF-8");

		logger.info("decoStr=" + decoStr);

		return new XMLSerializer().read(decoStr);
	}

	public JSON getVehInfoOfws(Map param) throws RemoteException, UnsupportedEncodingException, DocumentException {

		Document document = this.queryws(RCAConstant.V18C49, param);
		
		String xml=document.asXML();
		JSON json = new XMLSerializer().read(xml);
		
		if(json instanceof JSONObject) {
			JSONObject jo =(JSONObject)json;
			JSONObject head= jo.getJSONObject("head");
			
			if(head.getInt("code")==1) {
				JSONArray body= jo.getJSONArray("body");
				JSONObject info = body.getJSONObject(0);
				String clsbdh = info.getString("clsbdh");
				VehInfoTemp vit = getVehInfoTemp(clsbdh);
				if(vit==null) {
					vit=new VehInfoTemp();
				}
				vit.setClsbdh(clsbdh);
				vit.setHphm(param.get("hphm").toString());
				vit.setHpzl(info.getString("hpzl"));
				vit.setVehInfo(xml);
				vit.setStatus(0);
				saveVehInfoTemp(vit);
				
			}
		}
		

		return json;
	}

	public VehInfoTemp getVehInfoTemp(String clsbdh) {
		
		List<VehInfoTemp> datas = (List<VehInfoTemp>) this.hibernateTemplate.find("from VehInfoTemp where clsbdh=?", clsbdh);
		
		if(!CollectionUtils.isEmpty(datas)) {
			return datas.get(0);
		}else {
			return null;
		}
		
	}
	
	public void saveVehInfoTemp(VehInfoTemp vehInfoTemp) {
		this.hibernateTemplate.saveOrUpdate(vehInfoTemp);
	}
	

	public JSON getVehCheckItem(Map param) throws RemoteException, UnsupportedEncodingException, DocumentException {
		Document document = this.queryws(RCAConstant.V18C46, param);

		return new XMLSerializer().read(document.asXML());
	}

	public JSONObject vehLogin(VehCheckLogin vehCheckLogin) {

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
		vehCheckLogin.setVehcsbj(0);
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
			vcp.setVoideSate(0);
			this.hibernateTemplate.save(vcp);
			processArray.add(vcp);
		}
		//addVehFlow(vehCheckLogin, processArray, flow);
		createExternalCheck(vehCheckLogin);

		CheckEvents ce = new CheckEvents();
		ce.setClsbdh(vehCheckLogin.getClsbdh());
		ce.setEvent("18C51");
		ce.setHpzl(vehCheckLogin.getHpzl());
		ce.setJylsh(vehCheckLogin.getJylsh());
		ce.setHphm(vehCheckLogin.getHphm());
		ce.setCreateDate(new Date());
		ce.setState(vehCheckLogin.getVehcsbj());
		ce.setJycs(vehCheckLogin.getJycs());
		this.hibernateTemplate.save(ce);

		CheckEvents ce2 = new CheckEvents();
		ce2.setClsbdh(vehCheckLogin.getClsbdh());
		ce2.setEvent("18C52");
		ce2.setHpzl(vehCheckLogin.getHpzl());
		ce2.setJylsh(vehCheckLogin.getJylsh());
		ce2.setHphm(vehCheckLogin.getHphm());
		ce2.setCreateDate(new Date());
		ce2.setState(vehCheckLogin.getVehcsbj());
		ce2.setJycs(vehCheckLogin.getJycs());
		this.hibernateTemplate.save(ce2);

		head = new JSONObject();
		head.put("code", 1);
		head.put("isNetwork", false);
		messager = new JSONObject();
		messager.put("head", head);
		return messager;
	}

	public CheckLog getLoginCheckLog(String jylsh) {
		List<CheckLog> checkLogs = (List<CheckLog>) this.hibernateTemplate
				.find("from CheckLog where jylsh=? and jkbmc=?", jylsh, "18C51");
		if (checkLogs != null && !checkLogs.isEmpty()) {
			return checkLogs.get(0);
		} else {
			return null;
		}

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
	
	public String getBgdbh() {
		return this.hibernateTemplate.execute(new HibernateCallback<String>() {
			@Override
			public String doInHibernate(Session session) throws HibernateException {
				return session.doReturningWork(new ReturningWork<String>() {
					@Override
					public String execute(Connection connection) throws SQLException {
						CallableStatement cstmt = connection.prepareCall("{CALL Sequences(?,?,?)}");
						cstmt.setString(1, "bgdbh");
						cstmt.setString(2, "1");
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
	
	
	public List<VehCheckProcess> getVehCheckPrcoessByJylsh(String jylsh,Integer jycs) {

		List<VehCheckProcess> vcps = (List<VehCheckProcess>) this.hibernateTemplate
				.find("From VehCheckProcess where jylsh =? and jycs=? order by jylsh desc", jylsh,jycs);
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

		if (vheLogininfo.getCheckType() == 1) {
			List<TestVeh> testVehs = (List<TestVeh>) this.hibernateTemplate.find("from TestVeh where jylsh=?",
					vheLogininfo.getJylsh());
			if (!CollectionUtils.isEmpty(testVehs)) {
				TestVeh testVeh = testVehs.get(0);
				testVeh.setYsjc(2);
				testVeh.setJcwc(2);
				this.hibernateTemplate.update(testVeh);
			}
		}

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
		
		List<BaseParams> baseParams =  baseParamsManager.getBaseParamByType("sdwz");
		boolean sdFlag =false;
		if(!CollectionUtils.isEmpty(baseParams)) {
			sdFlag= baseParams.get(0).getParamValue().equals("true");
		}

		String allJyxm = vcl.getJyxm();

		if (vcl.getJycs() > 1) {
			allJyxm = vcl.getFjjyxm();
		}

		for (int i = 0; i < flowJsons.size(); i++) {

			JSONObject workPoint = flowJsons.getJSONObject(i);

			Integer gwid = workPoint.getInt("id");

			JSONArray sbs = workPoint.getJSONArray("sbs");

			for (int j = 0; j < sbs.size(); j++) {

				JSONObject sb = sbs.getJSONObject(j);

				Integer deviceId = sb.getInt("id");

				Device device = hibernateTemplate.get(Device.class, deviceId);

				// 如果复检，则不需要重新称重
				/*
				 * if(device.getType()== Device.CZJCSB && vcl.getJycs()>1) { continue; }
				 */

				String strJyxm = getDeivceItem(device, process);

				if (deviceId == -1 && allJyxm.indexOf("C1") != -1) {
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

				if (vcl.getReloginWeigth() == null) {
					vcl.setReloginWeigth(0);
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
						} else if (vcl.getJycs() > 1 && device.getType() == Device.CZJCSB
								&& vcl.getReloginWeigth() != 1) {
							continue;
						}else if(sdFlag&&vcl.getZs()>=3&&device.getType() == Device.SDJCSB) {
							continue;
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

	public List<VehCheckLogin> getZ1VehCheckLoginByZt(Integer zt) {

		DetachedCriteria detachedCrit = DetachedCriteria.forClass(VehCheckLogin.class);
		List<VehCheckLogin> vheCheckLogins = (List<VehCheckLogin>) this.hibernateTemplate
				.find("from VehCheckLogin where vehzbzlzt = ? and vehjczt!=?", zt, VehCheckLogin.JCZT_TB);
		return vheCheckLogins;
	}

	public List<VehCheckLogin> getVehCheckLoginOfSXZT(Integer zt) {

		DetachedCriteria detachedCrit = DetachedCriteria.forClass(VehCheckLogin.class);
		List<VehCheckLogin> vheCheckLogins = (List<VehCheckLogin>) this.hibernateTemplate
				.find("from VehCheckLogin where vehsxzt = ? and vehjczt!=?", zt, VehCheckLogin.JCZT_TB);
		return vheCheckLogins;
	}
	
	public List<VehCheckLogin> getVehChecked(String hphm) {
		DetachedCriteria detachedCrit = DetachedCriteria.forClass(VehCheckLogin.class);
		
		String sql ="from VehCheckLogin where vehjczt=?";
		
		List values = new ArrayList();
		values.add(VehCheckLogin.JCZT_JYJS);
		
		if (!StringUtils.isEmpty(hphm)) {
			sql += " and hphm like ?";
			values.add("%"+hphm+"%");
			Calendar c = Calendar.getInstance();
			sql += " and dlsj>=?";
			c.add(Calendar.YEAR, -1);
			values.add(c.getTime());
			
		} else {
			sql += " and dlsj>=? ";
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DAY_OF_MONTH, -3);
			values.add(c.getTime());
		}
		sql+=" order by id desc";
		List<VehCheckLogin> vheCheckLogins = (List<VehCheckLogin>) this.hibernateTemplate
				.find(sql, values.toArray());
		return vheCheckLogins;
	}
	
	
	public Message checkUpLine(Integer jcxdh,VehCheckLogin vehCheckLogin) {
		
		Message message = new Message();
		if (vehCheckLogin.getVehsxzt() == VehCheckLogin.ZT_JCZ) {
			message.setState(Message.STATE_ERROR);
			message.setMessage("引车上线失败，该流水已上线！");
			return message;
		}
		List<VehFlow> vfs = (List<VehFlow>) this.hibernateTemplate
				.find("from VehFlow where jylsh=? and jycs=? and sx=1 order by sx asc", vehCheckLogin.getJylsh(),
						vehCheckLogin.getJycs());
		if(CollectionUtils.isEmpty(vfs)) {
			VehFlow firstVehFlow = vfs.get(0);
			int gwxs = firstVehFlow.getGwsx();
			List<CheckQueue> checkQueues = (List<CheckQueue>) this.hibernateTemplate
					.find("from CheckQueue where gwsx<? and jcxdh=?", gwxs, Integer.parseInt(vehCheckLogin.getJcxdh()));

			if (checkQueues != null && !checkQueues.isEmpty()) {
				message.setState(Message.STATE_ERROR);
				message.setMessage("线上有车，请稍等！");
				return message;
			}
		}	
		return null;
	}
	

	public Message upLine(Integer id,Integer jcxdh) {

		VehCheckLogin vehCheckLogin = this.hibernateTemplate.load(VehCheckLogin.class, id);
		User user = (User) session.getAttribute("user");

		Message message = new Message();

		if (vehCheckLogin.getVehsxzt() == VehCheckLogin.ZT_JYJS || vehCheckLogin.getVehsxzt() == VehCheckLogin.ZT_JCZ) {
			message.setState(Message.STATE_ERROR);
			message.setMessage("引车上线失败，该流水已上线！");
			return message;

		} else {
			
			Flow flow = flowManager.getFlow(jcxdh, vehCheckLogin.getCheckType());
			
			List<VehCheckProcess> process = this.getVehCheckPrcoessByJylsh(vehCheckLogin.getJylsh(),vehCheckLogin.getJycs());
			
			List<VehFlow> oldVehFlows =  (List<VehFlow>) this.hibernateTemplate.find("from VehFlow where jylsh=? and jycs=?", vehCheckLogin.getJylsh(),vehCheckLogin.getJycs());
			
			if(CollectionUtils.isEmpty(oldVehFlows)) {
				addVehFlow(vehCheckLogin, process, flow);
				vehCheckLogin.setJcxdh(jcxdh.toString());
			}
			// 获取第一顺序流程
			VehFlow firstVehFlow = (VehFlow) this.hibernateTemplate
					.find("from VehFlow where jylsh=? and jycs=? and sx=1 order by sx asc", vehCheckLogin.getJylsh(),
							vehCheckLogin.getJycs())
					.get(0);

			int gwxs = firstVehFlow.getGwsx();

			List<CheckQueue> checkQueues = (List<CheckQueue>) this.hibernateTemplate
					.find("from CheckQueue where gwsx<? and jcxdh=?", gwxs, Integer.parseInt(vehCheckLogin.getJcxdh()));

			if (checkQueues != null && !checkQueues.isEmpty()) {
				message.setState(Message.STATE_ERROR);
				message.setMessage("线上有车，请稍等！");
				return message;
			}

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
			// 仪器报告单
			if ((vehCheckLogin.getVehsxzt() == VehCheckLogin.ZT_JYJS
					|| vehCheckLogin.getVehsxzt() == VehCheckLogin.ZT_BJC)
					&& (vehCheckLogin.getVehwkzt() == VehCheckLogin.ZT_JYJS
							|| vehCheckLogin.getVehwkzt() == VehCheckLogin.ZT_BJC)) {
				checkDataManager.createDeviceCheckJudeg(vehCheckLogin);
			}

			// 外检报告
			if ((vehCheckLogin.getVehdpzt() == VehCheckLogin.ZT_BJC
					|| vehCheckLogin.getVehdpzt() == VehCheckLogin.ZT_JYJS)
					&& (vehCheckLogin.getVehdtdpzt() == VehCheckLogin.ZT_BJC
							|| vehCheckLogin.getVehdtdpzt() == VehCheckLogin.ZT_JYJS)
					&& (vehCheckLogin.getVehwjzt() == VehCheckLogin.ZT_BJC
							|| vehCheckLogin.getVehwjzt() == VehCheckLogin.ZT_JYJS)) {
				checkDataManager.createExternalCheckJudge(vehCheckLogin);
			}

			if ((vehCheckLogin.getVehwjzt() == VehCheckLogin.ZT_JYJS
					|| vehCheckLogin.getVehwjzt() == VehCheckLogin.ZT_BJC)
					&& (vehCheckLogin.getVehdpzt() == VehCheckLogin.ZT_JYJS
							|| vehCheckLogin.getVehdpzt() == VehCheckLogin.ZT_BJC)
					&& (vehCheckLogin.getVehdtdpzt() == VehCheckLogin.ZT_JYJS
							|| vehCheckLogin.getVehdtdpzt() == VehCheckLogin.ZT_BJC)
					&& (vehCheckLogin.getVehsxzt() == VehCheckLogin.ZT_JYJS
							|| vehCheckLogin.getVehsxzt() == VehCheckLogin.ZT_BJC)
					&& (vehCheckLogin.getVehlszt() == VehCheckLogin.ZT_JYJS
							|| vehCheckLogin.getVehlszt() == VehCheckLogin.ZT_BJC)
					&& (vehCheckLogin.getVehzbzlzt() == VehCheckLogin.ZT_JYJS
							|| vehCheckLogin.getVehzbzlzt() == VehCheckLogin.ZT_BJC)
					&& (vehCheckLogin.getVehwkzt() == VehCheckLogin.ZT_JYJS
							|| vehCheckLogin.getVehwkzt() == VehCheckLogin.ZT_BJC)
					&& (vehCheckLogin.getVehzbzlzt() == VehCheckLogin.ZT_JYJS
							|| vehCheckLogin.getVehzbzlzt() == VehCheckLogin.ZT_BJC)) {

				List<DeviceCheckJudeg> deviceCheckJudegs = (List<DeviceCheckJudeg>) this.hibernateTemplate
						.find("from DeviceCheckJudeg where jylsh=?", vehCheckLogin.getJylsh());

				List<ExternalCheckJudge> externalCheckJudges = (List<ExternalCheckJudge>) this.hibernateTemplate
						.find("from ExternalCheckJudge where jylsh=?", vehCheckLogin.getJylsh());

				String jyjl = "合格";
				for (DeviceCheckJudeg deviceCheckJudeg : deviceCheckJudegs) {
					if (deviceCheckJudeg.getYqjgpd().equals("2")) {
						jyjl = "不合格";
					}
				}

				for (ExternalCheckJudge externalCheckJudge : externalCheckJudges) {
					if (externalCheckJudge.getRgjgpd().equals("2")) {
						jyjl = "不合格";
					}
				}
				try {
					vehCheckLogin.setJyjl(jyjl);
					checkEventManger.createEvent(2000,vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(), "18C82", null,
							vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(),
							vehCheckLogin.getVehcsbj());
					checkEventManger.createEvent(2300,vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(), "18C59", null,
							vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(),
							vehCheckLogin.getVehcsbj());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				vehCheckLogin.setReloginWeigth(0);
				// 复检
				List<BaseParams> bps = BaseParamsUtil.getBaseParamsByType("zdfj");
				if (vehCheckLogin.getVehcsbj() == 0
						&& (CollectionUtils.isEmpty(bps) || "true".equals(bps.get(0).getParamValue()))) {
					repeatLogin(vehCheckLogin);
				} else {
					vehCheckLogin.setFjjyxm(getFjjyxm(vehCheckLogin));
					vehCheckLogin.setVehjczt(VehCheckLogin.JCZT_JYJS);
					vehCheckLogin.setVehsxzt(VehCheckLogin.ZT_JYJS);
				}

				this.hibernateTemplate.update(vehCheckLogin);

				if (vehCheckLogin.getFjjyxm() == null || vehCheckLogin.getFjjyxm().equals("")) {
					try {
						checkEventManger.createEvent(2600,vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(), "18C62", null,
								vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(),
								vehCheckLogin.getVehcsbj());
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					if (vehCheckLogin.getVehcsbj() == 0
							&& (CollectionUtils.isEmpty(bps) || "true".equals(bps.get(0).getParamValue()))) {
						try {
							checkEventManger.createEvent(2600,vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(), "18C65", null,
									vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(),
									vehCheckLogin.getVehcsbj());
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						try {
							checkEventManger.createEvent(2900,vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(), "18C52", null,
									vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(),
									vehCheckLogin.getVehcsbj());
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
				}

			}
		}
	}

	// 复检项目
	public VehCheckLogin repeatLogin(VehCheckLogin vehCheckLogin) {

		String jylsh = vehCheckLogin.getJylsh();

		String fjjyxm = "";
		List<OtherInfoData> otherInfoData = (List<OtherInfoData>) this.hibernateTemplate
				.find("from OtherInfoData where jylsh=? and zczdpd=?", jylsh, BaseDeviceData.PDJG_BHG.toString());

		// 整车不合格则全部复检
		if (otherInfoData != null && !otherInfoData.isEmpty() && vehCheckLogin.getCllx().indexOf("N") == -1) {
			logger.info("整车不合格");
			if (vehCheckLogin.getJyxm().indexOf("B1") >= 0) {
				fjjyxm += "B1,";
			}
			if (vehCheckLogin.getJyxm().indexOf("B2") >= 0) {
				fjjyxm += "B2,";
			}
			if (vehCheckLogin.getJyxm().indexOf("B3") >= 0) {
				fjjyxm += "B3,";
			}
			if (vehCheckLogin.getJyxm().indexOf("B4") >= 0) {
				fjjyxm += "B4,";
			}
			if (vehCheckLogin.getJyxm().indexOf("B5") >= 0) {
				fjjyxm += "B5,";
			}
			List<BrakRollerData> brakRollerDatas = (List<BrakRollerData>) this.hibernateTemplate.find(
					"from BrakRollerData where jylsh=? and sjzt=? and jyxm in('B1','B2','B3','B4','B5')", jylsh,
					BaseDeviceData.SJZT_ZC);
			for (BrakRollerData brakRollerData : brakRollerDatas) {
				brakRollerData.setSjzt(BaseDeviceData.SJZT_FJ);
				this.hibernateTemplate.save(brakRollerData);
			}

		} else {
			// 制动复检
			List<BrakRollerData> brakRollerDatas = (List<BrakRollerData>) this.hibernateTemplate.find(
					"from BrakRollerData where jylsh=? and sjzt=? and jyxm<>'B0' and zpd=?", jylsh,
					BaseDeviceData.SJZT_ZC, BaseDeviceData.PDJG_BHG);
			for (BrakRollerData brakRollerData : brakRollerDatas) {
				logger.info(brakRollerData.getJyxm() + "不合格");
				fjjyxm += brakRollerData.getJyxm() + ",";
				brakRollerData.setSjzt(BaseDeviceData.SJZT_FJ);
				this.hibernateTemplate.save(brakRollerData);
			}
		}

		// 灯光复检
		List<LightData> lightDatas = (List<LightData>) this.hibernateTemplate.find(
				"from LightData where jylsh=? and sjzt=?  and zpd=?", jylsh, BaseDeviceData.SJZT_ZC,
				BaseDeviceData.PDJG_BHG);
		if (lightDatas != null && !lightDatas.isEmpty()) {

			Set<String> dgjyxmSet = new HashSet<String>();

			for (LightData ld : lightDatas) {
				dgjyxmSet.add(ld.getJyxm());
			}

			for (String s : dgjyxmSet) {
				fjjyxm += s + ",";
			}

		}
		for (LightData lightData : lightDatas) {
			lightData.setSjzt(BaseDeviceData.SJZT_FJ);
			this.hibernateTemplate.save(lightData);
		}

		// 速度复检
		List<SpeedData> speedDatas = (List<SpeedData>) this.hibernateTemplate.find(
				"from SpeedData where jylsh=? and sjzt=?  and zpd=?", jylsh, BaseDeviceData.SJZT_ZC,
				BaseDeviceData.PDJG_BHG);
		if (speedDatas != null && !speedDatas.isEmpty()) {
			fjjyxm += "S1,";
		}

		for (SpeedData speedData : speedDatas) {
			speedData.setSjzt(SpeedData.SJZT_FJ);
			this.hibernateTemplate.save(speedData);
		}

		// 侧滑复检
		List<SideslipData> sideslipDatas = (List<SideslipData>) this.hibernateTemplate.find(
				"from SideslipData where jylsh=? and sjzt=?  and zpd=?", jylsh, BaseDeviceData.SJZT_ZC,
				BaseDeviceData.PDJG_BHG);
		if (sideslipDatas != null && !sideslipDatas.isEmpty()) {
			fjjyxm += "A1,";
			sideslipDatas.get(0).setSjzt(SpeedData.SJZT_FJ);
			this.hibernateTemplate.save(sideslipDatas.get(0));
		}

		for (SideslipData sideslipDat : sideslipDatas) {
			sideslipDat.setSjzt(SpeedData.SJZT_FJ);
			this.hibernateTemplate.save(sideslipDat);
		}

		// 驻车复检
		List<ParDataOfAnjian> parDataOfAnjian = (List<ParDataOfAnjian>) this.hibernateTemplate.find(
				"from ParDataOfAnjian where jylsh=? and sjzt=?  and tczdpd=?", jylsh, BaseDeviceData.SJZT_ZC,
				BaseDeviceData.PDJG_BHG);
		if (parDataOfAnjian != null && !parDataOfAnjian.isEmpty()) {
			fjjyxm += "B0,";
			parDataOfAnjian.get(0).setSjzt(ParDataOfAnjian.SJZT_FJ);
			this.hibernateTemplate.save(parDataOfAnjian.get(0));
		}

		List<Outline> outlines = (List<Outline>) this.hibernateTemplate
				.find("from Outline where jylsh=? order by id desc", jylsh);

		if (outlines != null && !outlines.isEmpty()) {
			Outline outline = outlines.get(0);
			if (outline.getClwkccpd() == BaseDeviceData.PDJG_BHG) {
				fjjyxm += "M1,";
				outline.setSjzt(ParDataOfAnjian.SJZT_FJ);
				this.hibernateTemplate.save(outline);
				vehCheckLogin.setVehwkzt(VehCheckLogin.ZT_JCZ);
			}
		}
		List<CurbWeightData> owds =(List<CurbWeightData>)this.hibernateTemplate.find("from CurbWeightData where jylsh=? order by id desc", jylsh);
		if (owds != null && !owds.isEmpty()) {
			CurbWeightData cwd = owds.get(0);
			if(cwd.getZbzlpd()== BaseDeviceData.PDJG_BHG) {
				fjjyxm += "Z1,";
				cwd.setSjzt(CurbWeightData.SJZT_FJ);
				this.hibernateTemplate.save(cwd);
				vehCheckLogin.setVehzbzlzt(VehCheckLogin.ZT_JCZ);
			}
		}
		
		

		if (!fjjyxm.trim().equals("")) {
			fjjyxm = fjjyxm.substring(0, fjjyxm.length() - 1);
			vehCheckLogin.setVehjczt(VehCheckLogin.JCZT_JYZ);
			
			if(!fjjyxm.equals("Z1")) {
				vehCheckLogin.setVehsxzt(VehCheckLogin.ZT_WKS);
			}

			vehCheckLogin.setJycs(vehCheckLogin.getJycs() + 1);
			vehCheckLogin.setFjjyxm(fjjyxm);

			Flow flow = flowManager.getFlow(Integer.parseInt(vehCheckLogin.getJcxdh()), vehCheckLogin.getCheckType());

			String[] jyxmArray = fjjyxm.split(",");
			List<VehCheckProcess> processArray = new ArrayList<VehCheckProcess>();
			for (String jyxmItem : jyxmArray) {
				VehCheckProcess vcp = new VehCheckProcess();
				vcp.setClsbdh(vehCheckLogin.getClsbdh());
				vcp.setHphm(vehCheckLogin.getHphm());
				vcp.setHpzl(vehCheckLogin.getHpzl());
				vcp.setJylsh(vehCheckLogin.getJylsh());
				vcp.setJyxm(jyxmItem);
				vcp.setJycs(vehCheckLogin.getJycs());
				vcp.setVoideSate(0);
				this.hibernateTemplate.save(vcp);
				processArray.add(vcp);
			}
			//addVehFlow(vehCheckLogin, processArray, flow);

		} else {
			vehCheckLogin.setFjjyxm("");
			vehCheckLogin.setVehjczt(VehCheckLogin.JCZT_JYJS);
			vehCheckLogin.setVehsxzt(VehCheckLogin.ZT_JYJS);
		}
		logger.info("复检项目：" + fjjyxm);
		return vehCheckLogin;

	}
	
	
	public String getFjjyxm(VehCheckLogin vehCheckLogin) {
		
		String jylsh = vehCheckLogin.getJylsh();
		String fjjyxm = "";
		List<OtherInfoData> otherInfoData = (List<OtherInfoData>) this.hibernateTemplate
				.find("from OtherInfoData where jylsh=? and zczdpd=?", jylsh, BaseDeviceData.PDJG_BHG.toString());

		// 整车不合格则全部复检
		if (otherInfoData != null && !otherInfoData.isEmpty() && vehCheckLogin.getCllx().indexOf("N") == -1) {
			logger.info("整车不合格");
			if (vehCheckLogin.getJyxm().indexOf("B1") >= 0) {
				fjjyxm += "B1,";
			}
			if (vehCheckLogin.getJyxm().indexOf("B2") >= 0) {
				fjjyxm += "B2,";
			}
			if (vehCheckLogin.getJyxm().indexOf("B3") >= 0) {
				fjjyxm += "B3,";
			}
			if (vehCheckLogin.getJyxm().indexOf("B4") >= 0) {
				fjjyxm += "B4,";
			}
			if (vehCheckLogin.getJyxm().indexOf("B5") >= 0) {
				fjjyxm += "B5,";
			}
			
			// 加载复检
			List<BrakRollerData> brakRollerDatas = (List<BrakRollerData>) this.hibernateTemplate.find(
					"from BrakRollerData where jylsh=? and sjzt=? and jyxm in ('L1','L2','L3','L4') and zpd=?", jylsh,
					BaseDeviceData.SJZT_ZC, BaseDeviceData.PDJG_BHG);
			for (BrakRollerData brakRollerData : brakRollerDatas) {
				logger.info(brakRollerData.getJyxm() + "不合格");
				fjjyxm += brakRollerData.getJyxm() + ",";
			}

		} else {
			// 制动复检
			List<BrakRollerData> brakRollerDatas = (List<BrakRollerData>) this.hibernateTemplate.find(
					"from BrakRollerData where jylsh=? and sjzt=? and jyxm<>'B0' and zpd=?", jylsh,
					BaseDeviceData.SJZT_ZC, BaseDeviceData.PDJG_BHG);
			for (BrakRollerData brakRollerData : brakRollerDatas) {
				logger.info(brakRollerData.getJyxm() + "不合格");
				fjjyxm += brakRollerData.getJyxm() + ",";
			}
		}

		// 灯光复检
		List<LightData> lightDatas = (List<LightData>) this.hibernateTemplate.find(
				"from LightData where jylsh=? and sjzt=?  and zpd=?", jylsh, BaseDeviceData.SJZT_ZC,
				BaseDeviceData.PDJG_BHG);
		if (lightDatas != null && !lightDatas.isEmpty()) {

			Set<String> dgjyxmSet = new HashSet<String>();

			for (LightData ld : lightDatas) {
				dgjyxmSet.add(ld.getJyxm());
			}

			for (String s : dgjyxmSet) {
				fjjyxm += s + ",";
			}

		}

		// 速度复检
		List<SpeedData> speedDatas = (List<SpeedData>) this.hibernateTemplate.find(
				"from SpeedData where jylsh=? and sjzt=?  and zpd=?", jylsh, BaseDeviceData.SJZT_ZC,
				BaseDeviceData.PDJG_BHG);
		if (speedDatas != null && !speedDatas.isEmpty()) {
			fjjyxm += "S1,";
		}


		// 侧滑复检
		List<SideslipData> sideslipDatas = (List<SideslipData>) this.hibernateTemplate.find(
				"from SideslipData where jylsh=? and sjzt=?  and zpd=?", jylsh, BaseDeviceData.SJZT_ZC,
				BaseDeviceData.PDJG_BHG);
		if (sideslipDatas != null && !sideslipDatas.isEmpty()) {
			fjjyxm += "A1,";
		}


		// 驻车复检
		List<ParDataOfAnjian> parDataOfAnjian = (List<ParDataOfAnjian>) this.hibernateTemplate.find(
				"from ParDataOfAnjian where jylsh=? and sjzt=?  and tczdpd=?", jylsh, BaseDeviceData.SJZT_ZC,
				BaseDeviceData.PDJG_BHG);
		if (parDataOfAnjian != null && !parDataOfAnjian.isEmpty()) {
			fjjyxm += "B0,";
		}

		List<Outline> outlines = (List<Outline>) this.hibernateTemplate
				.find("from Outline where jylsh=? order by id desc", jylsh);

		if (outlines != null && !outlines.isEmpty()) {
			Outline outline = outlines.get(0);
			if (outline.getClwkccpd() == BaseDeviceData.PDJG_BHG) {
				fjjyxm += "M1,";
			}
		}
		
		return fjjyxm;
	}

	public void saveRelogin2(String jylsh, String fjjyxm, Integer reloginWeigth) {

		VehCheckLogin vehCheckLogin = this.checkDataManager.getVehCheckLogin(jylsh);

		vehCheckLogin.setVehjczt(VehCheckLogin.JCZT_JYZ);

		if (fjjyxm.indexOf("B") > -1 || fjjyxm.indexOf("H") > -1 || fjjyxm.indexOf("S") > -1
				|| fjjyxm.indexOf("A") > -1||fjjyxm.indexOf("L") > -1) {
			vehCheckLogin.setVehsxzt(VehCheckLogin.ZT_WKS);
		}

		if (fjjyxm.indexOf("F") > -1) {
			vehCheckLogin.setVehwjzt(VehCheckLogin.ZT_WKS);
		}

		if (fjjyxm.indexOf("DC") > -1) {
			vehCheckLogin.setVehdtdpzt(VehCheckLogin.ZT_WKS);
		}

		if (fjjyxm.indexOf("C1") > -1) {
			vehCheckLogin.setVehdpzt(VehCheckLogin.ZT_WKS);
		}

		if (fjjyxm.indexOf("R") > -1) {
			vehCheckLogin.setVehlszt(VehCheckLogin.ZT_WKS);
		}
		
		if (fjjyxm.indexOf("Z") > -1) {
			vehCheckLogin.setVehzbzlzt(VehCheckLogin.ZT_WKS);
		}
		
		if (fjjyxm.indexOf("M") > -1) {
			vehCheckLogin.setVehwkzt(VehCheckLogin.ZT_WKS);
		}
		

		vehCheckLogin.setJycs(vehCheckLogin.getJycs() + 1);
		vehCheckLogin.setFjjyxm(fjjyxm);

		reloginWeigth = reloginWeigth == null ? 0 : reloginWeigth;

		vehCheckLogin.setReloginWeigth(reloginWeigth);
		vehCheckLogin.setPrintStatus(0);

//	    Flow flow = flowManager.getFlow(Integer.parseInt(vehCheckLogin.getJcxdh()), vehCheckLogin.getCheckType());

		String[] jyxmArray = fjjyxm.split(",");
		List<VehCheckProcess> processArray = new ArrayList<VehCheckProcess>();
		for (String jyxmItem : jyxmArray) {
			VehCheckProcess vcp = new VehCheckProcess();
			vcp.setClsbdh(vehCheckLogin.getClsbdh());
			vcp.setHphm(vehCheckLogin.getHphm());
			vcp.setHpzl(vehCheckLogin.getHpzl());
			vcp.setJylsh(vehCheckLogin.getJylsh());
			vcp.setJyxm(jyxmItem);
			vcp.setJycs(vehCheckLogin.getJycs());
			vcp.setVoideSate(0);
			this.hibernateTemplate.save(vcp);
			processArray.add(vcp);
		}
		//addVehFlow(vehCheckLogin, processArray, flow);

		for (String jyxmItem : jyxmArray) {
			if (jyxmItem.indexOf("B") > -1 && !jyxmItem.equals("B0")) {
				// 制动复检
				List<BrakRollerData> brakRollerDatas = (List<BrakRollerData>) this.hibernateTemplate.find(
						"from BrakRollerData where jylsh=? and sjzt=? and jyxm=?", jylsh, BaseDeviceData.SJZT_ZC,
						jyxmItem);
				for (BrakRollerData brakRollerData : brakRollerDatas) {
					brakRollerData.setSjzt(BaseDeviceData.SJZT_FJ);
					this.hibernateTemplate.save(brakRollerData);
				}
			}
			
			if(jyxmItem.indexOf("L") > -1) {
				// 制动复检
				List<BrakRollerData> brakRollerDatas = (List<BrakRollerData>) this.hibernateTemplate.find(
						"from BrakRollerData where jylsh=? and sjzt=? and jyxm=?", jylsh, BaseDeviceData.SJZT_ZC,
						jyxmItem);
				for (BrakRollerData brakRollerData : brakRollerDatas) {
					brakRollerData.setSjzt(BaseDeviceData.SJZT_FJ);
					this.hibernateTemplate.save(brakRollerData);
				}
			}
			

			if (jyxmItem.equals("B0")) {
				List<ParDataOfAnjian> parDataOfAnjian = (List<ParDataOfAnjian>) this.hibernateTemplate
						.find("from ParDataOfAnjian where jylsh=? and sjzt=? ", jylsh, BaseDeviceData.SJZT_ZC);
				if (parDataOfAnjian != null && !parDataOfAnjian.isEmpty()) {
					parDataOfAnjian.get(0).setSjzt(ParDataOfAnjian.SJZT_FJ);
					this.hibernateTemplate.save(parDataOfAnjian.get(0));
				}
			}

			if (jyxmItem.indexOf("H") > -1) {
				// 灯光复检
				List<LightData> lightDatas = (List<LightData>) this.hibernateTemplate.find(
						"from LightData where jylsh=? and sjzt=? and jyxm=?", jylsh, BaseDeviceData.SJZT_ZC, jyxmItem);
				for (LightData lightData : lightDatas) {
					lightData.setSjzt(BaseDeviceData.SJZT_FJ);
					this.hibernateTemplate.save(lightData);
				}
			}

			if (jyxmItem.equals("S1")) {
				List<SpeedData> speedDatas = (List<SpeedData>) this.hibernateTemplate
						.find("from SpeedData where jylsh=? and sjzt=? ", jylsh, BaseDeviceData.SJZT_ZC);

				for (SpeedData speedData : speedDatas) {
					speedData.setSjzt(SpeedData.SJZT_FJ);
					this.hibernateTemplate.save(speedData);
				}
			}

			if (jyxmItem.equals("A1")) {
				// 侧滑复检
				List<SideslipData> sideslipDatas = (List<SideslipData>) this.hibernateTemplate
						.find("from SideslipData where jylsh=? and sjzt=? ", jylsh, BaseDeviceData.SJZT_ZC);

				for (SideslipData sideslipDat : sideslipDatas) {
					sideslipDat.setSjzt(SpeedData.SJZT_FJ);
					this.hibernateTemplate.save(sideslipDat);
				}
			}
		}
		try {
			checkEventManger.createEvent(0,vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(), "18C65", null,
					vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(),
					vehCheckLogin.getVehcsbj(),vehCheckLogin.getFjjyxm());
			checkEventManger.createEvent(500,vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(), "18C52", null,
					vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(),
					vehCheckLogin.getVehcsbj());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public List<Outline> getOutlineOfReportFlag() {

		return (List<Outline>) this.hibernateTemplate.find("from Outline where reportFlag!=? or reportFlag is null ",
				Outline.FLAG_Y);

	}

	public void updateOutline(Outline outline) {
		this.hibernateTemplate.update(outline);
	}

	public void deleteDeviceCheckJudegOfOutline(String jylsh) {
		List<DeviceCheckJudeg> dcs = (List<DeviceCheckJudeg>) this.hibernateTemplate
				.find("from DeviceCheckJudeg where jylsh=? and yqjyxm=?", jylsh, Outline.STR_JYXM);
		for (DeviceCheckJudeg dc : dcs) {
			this.hibernateTemplate.delete(dc);
		}
	}

	/**
	 * 保存整备质量
	 * 
	 * @param curbWeight
	 * @throws InterruptedException
	 */
	public void saveCurbWeight(CurbWeightData curbWeight) throws InterruptedException {

		VehCheckLogin vehCheckLogin = getVehCheckLoginByJylsh(jyjgbh, curbWeight.getJylsh());
		vehCheckLogin.setVehzbzlzt(VehCheckLogin.ZT_JYJS);

		VehCheckProcess vehCheckProcess = checkDataManager.getVehCheckProces(vehCheckLogin.getJylsh(),
				vehCheckLogin.getJycs(), "Z1");

		vehCheckProcess.setJssj(new Date());

		this.hibernateTemplate.update(vehCheckLogin);
		this.hibernateTemplate.update(vehCheckProcess);
		this.hibernateTemplate.save(curbWeight);

		checkEventManger.createEvent(vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(), "18C81", "Z1",
				vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(),
				vehCheckLogin.getVehcsbj());
		Thread.sleep(1000);
		checkEventManger.createEvent(vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(), "18C58", "Z1",
				vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(),
				vehCheckLogin.getVehcsbj());

		List<OtherInfoData> otherInfoDatas = (List<OtherInfoData>) this.hibernateTemplate
				.find("from OtherInfoData where jylsh=? order by id desc", vehCheckLogin.getJylsh());

		if (!otherInfoDatas.isEmpty()) {
			OtherInfoData other = otherInfoDatas.get(0);
			other.setJczczbzl(curbWeight.getZbzl());
			other.setZbzlpd(curbWeight.getZbzlpd());
			other.setBzzczbzl(vehCheckLogin.getZbzl());
			DecimalFormat df = new DecimalFormat(".#");
			double bfb = (Math.abs(curbWeight.getZbzl() - other.getBzzczbzl()) / (other.getBzzczbzl() * 1.0)) * 100;
			other.setZczbzlbfb(Float.valueOf(df.format(bfb)));
			this.hibernateTemplate.update(other);
		}

		// 判断项目的状态
		updateVehCheckLoginState(vehCheckLogin.getJylsh());
	}

	public static void main(String[] age) {

		VehCheckLogin vcl = new VehCheckLogin();

		System.out.println(vcl.getReloginWeigth() != 1);
	}

	public CurbWeightData getLastCurbWeightDataOfJylsh(String jylsh) {

		List<CurbWeightData> datas = (List<CurbWeightData>) this.hibernateTemplate
				.find("from CurbWeightData where jylsh=? order by id desc", jylsh);

		if (!datas.isEmpty()) {
			return datas.get(0);
		} else {
			return null;
		}

	}

	public void saveTestVeh(TestVeh testVeh) {

		this.hibernateTemplate.saveOrUpdate(testVeh);

	}

	public TestVeh getTestVehWaitBylsh(String jylsh) {

		List<TestVeh> testVehs = (List<TestVeh>) this.hibernateTemplate.find("from TestVeh where jylsh=? and ysjc!=1",
				jylsh);

		if (!CollectionUtils.isEmpty(testVehs)) {
			return testVehs.get(0);
		} else {
			return null;
		}

	}

	public TestVeh getTestVehBylsh(String jylsh) {

		List<TestVeh> testVehs = (List<TestVeh>) this.hibernateTemplate.find("from TestVeh where jylsh=?", jylsh);

		if (!CollectionUtils.isEmpty(testVehs)) {
			return testVehs.get(0);
		} else {
			return null;
		}

	}

	public TestVeh getTestVeh(Integer testVehId) {

		TestVeh testVehs = this.hibernateTemplate.load(TestVeh.class, testVehId);

		return testVehs;

	}

	public List<TestVeh> getTestVehWaitList() {

		List<TestVeh> testVehs = (List<TestVeh>) this.hibernateTemplate.find("from TestVeh where ysjc=0");

		return testVehs;

	}

	public Map<String, List<VehCheckLogin>> getUnOutcheckedInfo() {

		List<VehCheckLogin> wjList = (List<VehCheckLogin>) this.hibernateTemplate
				.find("from VehCheckLogin where vehwjzt=? and vehjczt!=?", VehCheckLogin.ZT_WKS, VehCheckLogin.JCZT_TB);
		List<VehCheckLogin> dpList = (List<VehCheckLogin>) this.hibernateTemplate
				.find("from VehCheckLogin where vehdpzt=? and vehjczt!=?", VehCheckLogin.ZT_WKS, VehCheckLogin.JCZT_TB);
		List<VehCheckLogin> dtpList = (List<VehCheckLogin>) this.hibernateTemplate.find(
				"from VehCheckLogin where vehdtdpzt=? and vehjczt!=?", VehCheckLogin.ZT_WKS, VehCheckLogin.JCZT_TB);

		Map<String, List<VehCheckLogin>> map = new HashMap<String, List<VehCheckLogin>>();

		map.put("wjList", wjList);
		map.put("dpList", dpList);
		map.put("dtpList", dtpList);

		return map;

	}

	public void updatePrintStatus(final String jylsh) {

		this.hibernateTemplate.execute(new HibernateCallback<Integer>() {

			@Override
			public Integer doInHibernate(Session session) throws HibernateException {
				Integer r = session.createQuery("update VehCheckLogin set printStatus=1 where jylsh=?")
						.setParameter(0, jylsh).executeUpdate();
				return r;
			}

		});

	}
	
	/**
	 * 获取复检项目
	 * @param jylsh
	 * @return
	 */
	public String getFjxm(String jylsh) {
		
		VehCheckLogin vehCheckLogin = this.checkDataManager.getVehCheckLogin(jylsh);
		
		String fjjyxm = "";
		List<OtherInfoData> otherInfoData = (List<OtherInfoData>) this.hibernateTemplate
				.find("from OtherInfoData where jylsh=? and zczdpd=?", jylsh, BaseDeviceData.PDJG_BHG.toString());

		// 整车不合格则全部复检
		if (otherInfoData != null && !otherInfoData.isEmpty() && vehCheckLogin.getCllx().indexOf("N") == -1) {
			logger.info("整车不合格");
			if (vehCheckLogin.getJyxm().indexOf("B1") >= 0) {
				fjjyxm += "B1,";
			}
			if (vehCheckLogin.getJyxm().indexOf("B2") >= 0) {
				fjjyxm += "B2,";
			}
			if (vehCheckLogin.getJyxm().indexOf("B3") >= 0) {
				fjjyxm += "B3,";
			}
			if (vehCheckLogin.getJyxm().indexOf("B4") >= 0) {
				fjjyxm += "B4,";
			}
			if (vehCheckLogin.getJyxm().indexOf("B5") >= 0) {
				fjjyxm += "B5,";
			}
			List<BrakRollerData> brakRollerDatas = (List<BrakRollerData>) this.hibernateTemplate.find(
					"from BrakRollerData where jylsh=? and sjzt=? and jyxm in('B1','B2','B3','B4','B5')", jylsh,
					BaseDeviceData.SJZT_ZC);
			for (BrakRollerData brakRollerData : brakRollerDatas) {
				brakRollerData.setSjzt(BaseDeviceData.SJZT_FJ);
				this.hibernateTemplate.save(brakRollerData);
			}

		} else {
			// 制动复检
			List<BrakRollerData> brakRollerDatas = (List<BrakRollerData>) this.hibernateTemplate.find(
					"from BrakRollerData where jylsh=? and sjzt=? and jyxm<>'B0' and zpd=?", jylsh,
					BaseDeviceData.SJZT_ZC, BaseDeviceData.PDJG_BHG);
			for (BrakRollerData brakRollerData : brakRollerDatas) {
				logger.info(brakRollerData.getJyxm() + "不合格");
				fjjyxm += brakRollerData.getJyxm() + ",";
			}
		}

		// 灯光复检
		List<LightData> lightDatas = (List<LightData>) this.hibernateTemplate.find(
				"from LightData where jylsh=? and sjzt=?  and zpd=?", jylsh, BaseDeviceData.SJZT_ZC,
				BaseDeviceData.PDJG_BHG);
		if (lightDatas != null && !lightDatas.isEmpty()) {

			Set<String> dgjyxmSet = new HashSet<String>();

			for (LightData ld : lightDatas) {
				dgjyxmSet.add(ld.getJyxm());
			}

			for (String s : dgjyxmSet) {
				fjjyxm += s + ",";
			}

		}

		// 速度复检
		List<SpeedData> speedDatas = (List<SpeedData>) this.hibernateTemplate.find(
				"from SpeedData where jylsh=? and sjzt=?  and zpd=?", jylsh, BaseDeviceData.SJZT_ZC,
				BaseDeviceData.PDJG_BHG);
		if (speedDatas != null && !speedDatas.isEmpty()) {
			fjjyxm += "S1,";
		}


		// 侧滑复检
		List<SideslipData> sideslipDatas = (List<SideslipData>) this.hibernateTemplate.find(
				"from SideslipData where jylsh=? and sjzt=?  and zpd=?", jylsh, BaseDeviceData.SJZT_ZC,
				BaseDeviceData.PDJG_BHG);
		if (sideslipDatas != null && !sideslipDatas.isEmpty()) {
			fjjyxm += "A1,";
		}


		// 驻车复检
		List<ParDataOfAnjian> parDataOfAnjian = (List<ParDataOfAnjian>) this.hibernateTemplate.find(
				"from ParDataOfAnjian where jylsh=? and sjzt=?  and tczdpd=?", jylsh, BaseDeviceData.SJZT_ZC,
				BaseDeviceData.PDJG_BHG);
		if (parDataOfAnjian != null && !parDataOfAnjian.isEmpty()) {
			fjjyxm += "B0,";
		}

		List<Outline> outlines = (List<Outline>) this.hibernateTemplate
				.find("from Outline where jylsh=? order by id desc", jylsh);

		if (outlines != null && !outlines.isEmpty()) {
			Outline outline = outlines.get(0);
			if (outline.getClwkccpd() == BaseDeviceData.PDJG_BHG) {
				fjjyxm += "M1,";
			}
		}
		
		return fjjyxm;
		
	}

}
