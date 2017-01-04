package com.xs.veh.job;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder.Case;

import org.apache.axis2.AxisFault;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.xs.rca.ws.client.TmriJaxRpcOutAccessServiceStub;
import com.xs.rca.ws.client.TmriJaxRpcOutAccessServiceStub.QueryObjectOutResponse;
import com.xs.rca.ws.client.TmriJaxRpcOutAccessServiceStub.WriteObjectOutResponse;
import com.xs.veh.entity.CheckEvents;
import com.xs.veh.entity.CheckLog;
import com.xs.veh.manager.CheckEventManger;
import com.xs.veh.util.BeanXMLUtil;
import com.xs.veh.util.RCAConstant;

@Component("CheckedInfoTaskJob")
public class CheckedInfoTaskJob {

	private static Logger logger = Logger.getLogger(CheckedInfoTaskJob.class);

	private Properties properties = null;

	private TmriJaxRpcOutAccessServiceStub tro = null;
	
	@Value("${jkxlh}")
	private String jkxlh;

	@Value("${jyjgbh}")
	private String jyjgbh;

/*	@Resource(name = "imageDBManger")
	private ImageDBManger imageDBManger;*/

	@Resource(name = "checkEventManger")
	private CheckEventManger eventManger;

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

	private Document write(TmriJaxRpcOutAccessServiceStub.WriteObjectOut woo,
			Object o) throws Exception {
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
			sbrz.setJkbid(Integer.parseInt(xml.getRootElement()
					.element("vehispara").element("id").getText()));
			sbrz.setJylsh(xml.getRootElement().element("vehispara")
					.element("jylsh").getText());
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

	@Scheduled(fixedDelay = 2000)
	public void scanEventJob() throws Exception {
		List<CheckEvents> list = (List<CheckEvents>) eventManger.getEvents();
		for (CheckEvents e : list) {
			try{
				String viewName = "V" + e.getEvent();
				
				String checkItem = e.getJyxm();
				
				/*if (RCAConstant.V18C63.equals(viewName)) {
					uploadImage(e);
					continue;
				}*/
				if ((RCAConstant.V18C53.equals(viewName)
						|| RCAConstant.V18C80.equals(viewName)
						|| RCAConstant.V18C54.equals(viewName)
						|| RCAConstant.V18C81.equals(viewName) || RCAConstant.V18C64
							.equals(viewName))
						&& checkItem != null
						&& !"".equals(checkItem)) {
					viewName += "_" + checkItem;
				}

				List<Map> datas = (List<Map>) eventManger.getViewData(
						viewName, e.getJylsh());

				if (datas == null || datas.isEmpty()) {
					e.setState(2);
					e.setMessage("该事件无法找到视图：" + viewName + " 条件为jylsh ="
							+ e.getJylsh() + "对应的数据。请检查上传的数据是否已经产生！");
					eventManger.update(e);
					continue;
				}

				for (Map data : datas) {
					Document document = this.write(e, data);
					Element root = document.getRootElement();
					Element head = root.element("head");
					Element code = head.element("code");
					Element message = head.element("message");
					if ("1".equals(code.getText())) {
						System.out.println("delete event id" + e.getId());
						eventManger.delete(e);
						logger.info("viewName:"+viewName);
					} else {
						e.setState(2);
						e.setMessage(message.getText());
						logger.error(document.asXML());
						eventManger.update(e);
					}
				}
			}catch(Exception et){
				e.setState(2);
				e.setMessage(et.getMessage());
				et.printStackTrace();
				logger.error("----");
				eventManger.update(e);
			}
		}
	}
	
	

/*	public void uploadImage(Event e) {

		String jyxm = e.getCheckItem();
		String zpzl = e.getZpzl();
		Map zpMap = imageDBManger.getImage(e.getLsh(), zpzl, jyxm);
		if (zpMap != null) {
			zpMap.put("jyxm", jyxm);
			zpMap.put("zpzl", zpzl);
			zpMap.put("jyjgbh", jyjgbh);
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
		}else{
			e.setState(2);
			e.setMessage("无法找到该照片的数据");
			eventManger.update(e);
		}
		
	}
	*/

}
