package com.xs.veh.job;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.xs.rca.ws.client.TmriJaxRpcOutAccessServiceStub;
import com.xs.rca.ws.client.TmriJaxRpcOutAccessServiceStub.QueryObjectOutResponse;
import com.xs.rca.ws.client.TmriJaxRpcOutAccessServiceStub.WriteObjectOutResponse;
import com.xs.veh.entity.BaseParams;
import com.xs.veh.entity.CheckEvents;
import com.xs.veh.entity.CheckLog;
import com.xs.veh.entity.CheckPhoto;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.manager.BaseParamsManager;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.manager.CheckEventManger;
import com.xs.veh.manager.VehManager;
import com.xs.veh.network.data.Outline;
import com.xs.veh.util.BeanXMLUtil;
import com.xs.veh.util.RCAConstant;

import sun.misc.BASE64Encoder;

@Component("CheckedInfoTaskJob")
public class CheckedInfoTaskJob {

	private static Logger logger = Logger.getLogger(CheckedInfoTaskJob.class);

	private TmriJaxRpcOutAccessServiceStub tro = null;
	
	@Autowired
	private SessionFactory sessionFactory;

	@Value("${jkxlh}")
	private String jkxlh;
	
	@Value("${lic.startData}")
	private String startDataStr;
	
	@Value("${lic.endData}")
	private String endDataStr;

	/*
	 * @Resource(name = "imageDBManger") private ImageDBManger imageDBManger;
	 */

	@Resource(name = "checkEventManger")
	private CheckEventManger eventManger;

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;

	@Resource(name = "vehManager")
	private VehManager vehManager;

	@Value("${isNetwork}")
	private boolean isNetwork;

	@Value("${sqrqz}")
	private String pzrxm;

	@Value("${jyjgbh}")
	private String jyjgbh;
	
	@Autowired
	private ServletContext servletContext;

	@Resource(name = "baseParamsManager")
	private BaseParamsManager baseParamsManager;

	public CheckedInfoTaskJob() {
		try {
			tro = new TmriJaxRpcOutAccessServiceStub();
		} catch (AxisFault e) {
			logger.error("链接专网查验平台失败", e);
		} catch (IOException e) {
			logger.error("加载rca配置文件出错", e);
		}
	}
	
	

	@PostConstruct
	public void intJob() {
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

	private Document write(TmriJaxRpcOutAccessServiceStub.WriteObjectOut woo, Object o) throws Exception {
		try {
			Document xml = BeanXMLUtil.bean2xml(o, "vehispara");
			logger.debug("bo:" + xml.asXML());
			woo.setUTF8XmlDoc(xml.asXML());
			WriteObjectOutResponse wor = tro.writeObjectOut(woo);
			String response = wor.getWriteObjectOutReturn();
			response = URLDecoder.decode(response, "utf-8");
			Document document = DocumentHelper.parseText(response);
			Element root = document.getRootElement();
			Element head = root.element("head");
			Element code = head.element("code");
			Element message = head.element("message");

			CheckLog sbrz = new CheckLog();
			sbrz.setSbsj(new Date());
			sbrz.setJkbid(Integer.parseInt(xml.getRootElement().element("vehispara").element("id").getText()));
			sbrz.setJylsh(xml.getRootElement().element("vehispara").element("jylsh").getText());
			sbrz.setJkbmc("JYDLXX");
			sbrz.setMessage(message.getText());
			sbrz.setCode(code.getText());
			sbrz.setXml(document.asXML());
			eventManger.saveLog(sbrz);
			logger.debug("response BO:" + document.asXML());

			return document;
		} catch (NoSuchMethodException e) {
			logger.error("bean2xml转换异常", e);
			throw e;
		} catch (UnsupportedEncodingException e) {
			logger.error("xmlencoding异常", e);
			throw e;
		} catch (RemoteException e) {
			logger.error("远程连接异常", e);
			throw e;
		} catch (DocumentException e) {
			logger.error("response parseText异常", e);
			throw e;
		}
	}

	private Document write(CheckEvents event, Map data) throws Exception {
		try {
			TmriJaxRpcOutAccessServiceStub.WriteObjectOut woo = new TmriJaxRpcOutAccessServiceStub.WriteObjectOut();
			String jkid = event.getEvent();
			if (jkid.equals("18C55_R")) {
				jkid = "18C55";
			}
			if (jkid.equals("18C58_R")) {
				jkid = "18C58";
			}
			woo.setJkid(jkid);
			woo.setXtlb(RCAConstant.XTLB);
			woo.setJkxlh(jkxlh);
			Document xml = BeanXMLUtil.map2xml(data, "vehispara");
			woo.setUTF8XmlDoc(xml.asXML());
			WriteObjectOutResponse wor = tro.writeObjectOut(woo);
			String response = wor.getWriteObjectOutReturn();
			response = URLDecoder.decode(response, "utf-8");
			Document document = DocumentHelper.parseText(response);
			Element root = document.getRootElement();
			Element head = root.element("head");
			Element code = head.element("code");
			Element message = head.element("message");
			logger.info(document.asXML());
			CheckLog sbrz = new CheckLog();
			sbrz.setSbsj(new Date());

			logger.debug("xml:" + xml.asXML());
			sbrz.setJylsh((String) data.get("jylsh"));
			if (event.getJyxm() == null) {
				sbrz.setJkbmc(jkid);
			} else {
				sbrz.setJkbmc(jkid + "_" + event.getJyxm());
			}
			sbrz.setHmph(event.getHphm());
			sbrz.setHpzl(event.getHpzl());
			sbrz.setMessage(message.getText());
			sbrz.setCode(code.getText());
			sbrz.setXml(document.asXML());
			if (!"18C63".equals(jkid)) {
				if (xml.asXML().length() < 8000) {
					sbrz.setBo(xml.asXML());
				}
			}
			eventManger.saveLog(sbrz);
			return document;
		} catch (UnsupportedEncodingException e) {
			logger.error("xmlencoding异常", e);
			throw e;
		} catch (RemoteException e) {
			logger.error("远程连接异常", e);
			throw e;
		} catch (DocumentException e) {
			logger.error("response parseText异常", e);
			throw e;
		}
	}

	public boolean getUploadSwitch() {

		String uploadSwitch = (String) servletContext.getAttribute("uploadSwitch");
		if (uploadSwitch != null && uploadSwitch.equals("1")) {
			return false;
		}
		return true;
	}

	@Scheduled(fixedDelay = 5000)
	private void vehOutlineReport() {
		List<Outline> outlines = this.vehManager.getOutlineOfReportFlag();
		if(outlines!=null&&!outlines.isEmpty()){
			for(Outline outline : outlines){
				outline.setReportFlag(Outline.FLAG_Y);
				vehManager.updateOutline(outline);
				vehManager.updateVehCheckLoginState(outline.getJylsh());
			}
		}
	}

	@Scheduled(fixedDelay = 1000 * 3600 * 12)
	public void synTime() throws RemoteException, UnsupportedEncodingException, DocumentException {

		logger.info("开始同步监管系统时间！");
		Map<String, String> data = new HashMap<String, String>();
		Document doc = this.queryws("18C50", data);
		Element body = doc.getRootElement().element("body");
		if (body != null) {
			Element para = body.element("vehispara");
			if (para != null) {
				String sj = para.element("sj").getText();
				logger.info("监管平台时间" + sj);
				String[] sjs = sj.split(" ");
				String osName = System.getProperty("os.name");
				String cmd = "";
				try {
					if (osName.matches("^(?i)Windows.*$")) {// Window 系统
						// 格式 HH:mm:ss
						cmd = "  cmd /c time " + sjs[1].substring(0, 8);

						logger.info("设置时间：" + cmd);

						Runtime.getRuntime().exec(cmd);
						// 格式：yyyy-MM-dd
						cmd = " cmd /c date " + sjs[0];

						logger.info("设置日期：" + cmd);
						Runtime.getRuntime().exec(cmd);

					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}

	@Scheduled(fixedDelay = 1000)
	public void scanEventJob() throws Exception {
//		if (!isNetwork || !getUploadSwitch()) {
//			return;
//		}
		List<CheckEvents> list = (List<CheckEvents>) eventManger.getEvents();
		for (CheckEvents e : list) {
			Thread.sleep(1000);
			try {
				String viewName = "V" + e.getEvent();

				String checkItem = e.getJyxm();

				if (RCAConstant.V18C63.equals(e.getEvent())) {
					uploadImage(e);
					continue;
				}
				if ((RCAConstant.V18C53.equals(e.getEvent()) || RCAConstant.V18C80.equals(e.getEvent())
						|| RCAConstant.V18C81.equals(e.getEvent()) || RCAConstant.V18C64.equals(e.getEvent()))
						&& checkItem != null && !"".equals(checkItem)) {

					viewName += "_" + checkItem;
				}

				List<Map> datas = (List<Map>) eventManger.getViewData(viewName, e.getJylsh(),e.getJyxm());

				if (datas == null || datas.isEmpty()) {
					e.setState(2);
					e.setMessage("该事件无法找到视图：" + viewName + " 条件为jylsh =" + e.getJylsh() + "对应的数据。请检查上传的数据是否已经产生！");
					eventManger.update(e);
					continue;
				}

				for (Map data : datas) {

					if (e.getEvent().equals(RCAConstant.V18C62)) {
						List yqjg = checkDataManager.getDeviceCheckJudeg(e.getJylsh());
						Document docYqjg = BeanXMLUtil.list2xml(yqjg, "yqsbjyjg");
						if (docYqjg != null) {
							data.put("yqsbjyjgs", docYqjg.asXML());
						}

						// 人工检验项目结果
						List rgjg = checkDataManager.getExternalCheckJudge(e.getJylsh());
						if (rgjg != null) {
							Document docRgjg = BeanXMLUtil.list2xml(rgjg, "rgjyjg");
							data.put("rgjyjgs", docRgjg.asXML());
						}
						data.put("pzrxm", pzrxm);
						VehCheckLogin info = vehManager.getVehCheckLoginByJylsh(jyjgbh, e.getJylsh());
						data.put("jyjl", info.getJyjl());

					}

					Document document = this.write(e, data);
					Element root = document.getRootElement();
					Element head = root.element("head");
					Element code = head.element("code");
					Element message = head.element("message");
					if ("1".equals(code.getText())) {
						System.out.println("delete event id" + e.getId());
						eventManger.delete(e);
						logger.info("viewName:" + viewName);
					} else {
						e.setState(2);
						e.setMessage(message.getText());
						logger.error(document.asXML());
						eventManger.update(e);
					}
				}
			} catch (Exception et) {
				e.setState(2);
				e.setMessage(et.getMessage());
				et.printStackTrace();
				logger.error("----");
				eventManger.update(e);
			}
		}
	}

	public void uploadImage(CheckEvents e) {

		String zpzl = e.getZpzl();

		CheckPhoto photo = checkDataManager.getCheckPhoto(e.getJylsh(), zpzl, e.getJycs());

		if (photo != null) {

			Map zpMap = new HashMap();
			zpMap.put("jylsh", photo.getJylsh());
			zpMap.put("jyjgbh", jyjgbh);
			zpMap.put("zpMap", photo.getJcxdh());
			zpMap.put("jycs", photo.getJycs());
			zpMap.put("hphm", photo.getHphm());
			zpMap.put("hpzl", photo.getHpzl());
			zpMap.put("clsbdh", photo.getClsbdh());

			SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			zpMap.put("pssj", sd.format(photo.getPssj()));
			zpMap.put("jyxm", photo.getJyxm());
			zpMap.put("zpzl", photo.getZpzl());

			if (photo.getZp() != null) {
				BASE64Encoder encode = new BASE64Encoder();
				String imageCode = encode.encode(photo.getZp());
				zpMap.put("zp", imageCode);
			}

			try {
				Document document = this.write(e, zpMap);
				Element root = document.getRootElement();
				Element head = root.element("head");
				Element code = head.element("code");
				Element message = head.element("message");
				if ("1".equals(code.getText())) {
					eventManger.delete(e);
				} else {
					e.setState(2);
					e.setMessage(message.getText());
					eventManger.update(e);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} else {
			e.setState(2);
			e.setMessage("无法找到该照片的数据");
			eventManger.update(e);
		}

	}
	
	@Scheduled(fixedDelay = 1000*10)
	public void timeoutPocess() throws Exception{
		try {
			validate();
		} catch (Exception e) {
			if(!sessionFactory.isClosed()){
				sessionFactory.close();
			}
			throw e;
		}
	}
	
//	@Scheduled(fixedDelay = 1000*10)
	private void validate() throws Exception{
		SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd");
		Date startData  = sdf.parse(startDataStr);
		Date endData  = sdf.parse(endDataStr);
		Date now = new Date();
		if(now.getTime()<startData.getTime()||now.getTime()>endData.getTime()){
			throw new Exception("系统有效日期为："+startDataStr+"至"+endDataStr);
		}
	}



}
