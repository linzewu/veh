package com.xs.veh.manager;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.jdbc.ReturningWork;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.rca.ws.client.TmriJaxRpcOutAccessServiceStub;
import com.xs.rca.ws.client.TmriJaxRpcOutAccessServiceStub.QueryObjectOutResponse;
import com.xs.rca.ws.client.TmriJaxRpcOutAccessServiceStub.WriteObjectOutResponse;
import com.xs.veh.entity.VehCheckLog;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckeProcess;
import com.xs.veh.util.BeanXMLUtil;
import com.xs.veh.util.RCAConstant;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

@Service("vehManager")
public class VehManager {

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

		JSONObject head = null;
		JSONObject messager = null;

		if (isNetwork) {
			JSONObject param = JSONObject.fromObject(vehCheckLogin);
			Document document = this.write(RCAConstant.V18C51, param);
			JSON json = new XMLSerializer().read(document.asXML());
			String josnStr = json.toString();
			if (json.isArray()) {
				JSONArray ja = JSONArray.fromObject(josnStr);
				head = ja.getJSONObject(0);
				messager = head;
			} else {
				JSONObject jo = JSONObject.fromObject(josnStr);
				head = jo.getJSONObject("head");
				messager = jo;
			}
		}

		if ((!isNetwork) || head.get("code").equals("1")) {
			this.hibernateTemplate.save(vehCheckLogin);
			String jyxm = vehCheckLogin.getJyxm();
			String[] jyxmArray = jyxm.split(",");

			for (String jyxmItem : jyxmArray) {
				VehCheckeProcess vcp = new VehCheckeProcess();
				vcp.setClsbdh(vehCheckLogin.getClsbdh());
				vcp.setHphm(vehCheckLogin.getHphm());
				vcp.setHpzl(vehCheckLogin.getHpzl());
				vcp.setJylsh(vehCheckLogin.getJylsh());
				vcp.setJyxm(jyxmItem);
				vcp.setStatus(RCAConstant.UNCHECK);
				this.hibernateTemplate.save(vcp);
			}
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
	public List<VehCheckeProcess> getVehCheckPrcoessByJylsh(String jylsh) {

		List<VehCheckeProcess> vcps = (List<VehCheckeProcess>) this.hibernateTemplate
				.find("From VehCheckeProcess where jylsh =? order by jylsh desc", jylsh);

		return vcps;
	}

	public List<VehCheckLogin> getVehChecking(Integer page, Integer rows, VehCheckLogin vehCheckLogin,
			Integer[] status) {

		DetachedCriteria query = DetachedCriteria.forClass(VehCheckLogin.class);

		if (vehCheckLogin.getHphm() != null && !"".equals(vehCheckLogin.getHphm())) {
			query.add(Restrictions.like("hphm", vehCheckLogin.getHphm()));
		}

		if (vehCheckLogin.getHpzl() != null && !"".equals(vehCheckLogin.getHpzl())) {
			query.add(Restrictions.eq("hpzl", vehCheckLogin.getHpzl()));
		}
		if (status != null) {
			query.add(Restrictions.in("status", status));
		}
		query.addOrder(Order.desc("jylsh"));

		Integer firstResult = (page - 1) * rows;

		List<VehCheckLogin> vcps = (List<VehCheckLogin>) this.hibernateTemplate.findByCriteria(query, firstResult,
				rows);

		return vcps;
	}

	public JSONObject deleteVeh(Integer id) throws RemoteException, UnsupportedEncodingException, DocumentException {

		VehCheckLogin vheLogininfo = this.hibernateTemplate.load(VehCheckLogin.class, id);
		final String jylsh = vheLogininfo.getJylsh();
		if (isNetwork) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("jylsh", jylsh);
			map.put("jyjgbh", jyjgbh);
			map.put("hpzl", vheLogininfo.getHpzl());
			map.put("hpzl", vheLogininfo.getHphm());
			Document document = this.write(RCAConstant.V18C72, map);

			Element head = document.getRootElement().element("head");

			if (head.element("code").getText().equals("1")) {
				this.hibernateTemplate.delete(vheLogininfo);
				this.hibernateTemplate.execute(new HibernateCallback<Integer>() {
					@Override
					public Integer doInHibernate(Session session) throws HibernateException {
						return session.createQuery("delete VehCheckeProcess where jylsh=:jylsh").setParameter("jylsh", jylsh)
								.executeUpdate();
					}
				});
			}
			JSON json = new XMLSerializer().read(document.asXML());
			JSONObject jo = JSONObject.fromObject(json.toString());
			return jo;

		} else {
			JSONObject jo = new JSONObject();
			JSONObject jsonHead = new JSONObject();
			jsonHead.put("code", "1");
			jsonHead.put("isNetwork", isNetwork);
			jo.put("head", jsonHead);
			this.hibernateTemplate.delete(vheLogininfo);
			this.hibernateTemplate.execute(new HibernateCallback<Integer>() {
				@Override
				public Integer doInHibernate(Session session) throws HibernateException {
					return session.createQuery("delete VehCheckeProcess where jylsh=:jylsh").setParameter("jylsh", jylsh)
							.executeUpdate();
				}
			});
			return jo;
		}
	}

}
