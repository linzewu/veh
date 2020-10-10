package com.xs.veh.job;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.xs.common.BaseParamsUtil;
import com.xs.rca.ws.client.TmriJaxRpcOutNewAccessServiceStub;
import com.xs.rca.ws.client.TmriJaxRpcOutNewAccessServiceStub.QueryObjectOutResponse;
import com.xs.rca.ws.client.TmriJaxRpcOutNewAccessServiceStub.WriteObjectOutResponse;
import com.xs.veh.entity.BaseParams;
import com.xs.veh.entity.CheckEvents;
import com.xs.veh.entity.CheckLog;
import com.xs.veh.entity.CheckPhoto;
import com.xs.veh.entity.ExternalCheck;
import com.xs.veh.entity.TaskPicture;
import com.xs.veh.entity.User;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.entity.VideoConfig;
import com.xs.veh.manager.BaseParamsManager;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.manager.CheckEventManger;
import com.xs.veh.manager.ExternalCheckManager;
import com.xs.veh.manager.VehManager;
import com.xs.veh.manager.VehProcessManager;
import com.xs.veh.manager.VideoManager;
import com.xs.veh.network.TakePicture;
import com.xs.veh.network.data.Outline;
import com.xs.veh.sz.IaspecTmriOutAccessStub;
import com.xs.veh.util.BeanXMLUtil;
import com.xs.veh.util.HKVisionUtil;
import com.xs.veh.util.RCAConstant;

import sun.misc.BASE64Encoder;

@Component("CheckedInfoTaskJob")
public class CheckedInfoTaskJob {

	private static Logger logger = Logger.getLogger(CheckedInfoTaskJob.class);

	private TmriJaxRpcOutNewAccessServiceStub tro = null;
	
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
	private HKVisionUtil hkUtil;
	
	@Autowired
	private VideoManager videoManager;
	
	@Autowired
	private ServletContext servletContext;
	
	@Autowired
	private VehProcessManager vehProcessManager;

	@Resource(name = "baseParamsManager")
	private BaseParamsManager baseParamsManager;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Resource(name = "externalCheckManager")
	private ExternalCheckManager externalCheckManager;

	public CheckedInfoTaskJob() {
		try {
			tro = new TmriJaxRpcOutNewAccessServiceStub();
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

		TmriJaxRpcOutNewAccessServiceStub.QueryObjectOut qoo = new TmriJaxRpcOutNewAccessServiceStub.QueryObjectOut();
		param.put("jyjgbh", jyjgbh);
		qoo.setJkid(jkid);
		qoo.setXtlb(RCAConstant.XTLB);
		qoo.setJkxlh(jkxlh);
		
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

	private Document write(TmriJaxRpcOutNewAccessServiceStub.WriteObjectOut woo, Object o) throws Exception {
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
			TmriJaxRpcOutNewAccessServiceStub.WriteObjectOut woo = new TmriJaxRpcOutNewAccessServiceStub.WriteObjectOut();
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
			
			   String bmdm = baseParamsManager.getBaseParam("jkcs", "bmdm").getParamName();
			   String ywzdbm = baseParamsManager.getBaseParam("jkcs", "ywzdbm").getParamName();
			   String jcip = baseParamsManager.getBaseParam("jkcs", "jcip").getParamName();
			   woo.setDwjgdm(bmdm);
			   woo.setDwmc(ywzdbm);
			   woo.setZdbs(jcip);
			
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
	
	@Scheduled(fixedDelay = 5000)
	private void vehSZPTOutCheck() throws UnsupportedEncodingException, DocumentException, InterruptedException {
		List<BaseParams> paams = BaseParamsUtil.getBaseParamsByType("szdsfpt");
		if(!CollectionUtils.isEmpty(paams)) {
			 String szdsfpt = paams.get(0).getParamValue();
			 if("true".equals(szdsfpt)) {
				 
				 Map<String,List<VehCheckLogin>>  datas = this.vehManager.getUnOutcheckedInfo();
				 
				 List<VehCheckLogin> wjList = datas.get("wjList");
				 List<VehCheckLogin> dpList = datas.get("dpList");
				 List<VehCheckLogin> dtpList = datas.get("dtpList");
				 
				 if(!CollectionUtils.isEmpty(wjList)) {
					 for(VehCheckLogin wj: wjList) {
						String wjData =  getSZPTOutCheck(wj,"F1");
						if(!StringUtils.isEmpty(wjData)) {
							ExternalCheck externalCheck=new ExternalCheck();
							crateExternalCheck(externalCheck, wjData);
							externalCheckManager.saveExternalCheck(externalCheck);
						}
					 }
				 }
				 
				 if(!CollectionUtils.isEmpty(dpList)) {
					 for(VehCheckLogin dp: dpList) {
						String dpData =  getSZPTOutCheck(dp,"C1");
						if(!StringUtils.isEmpty(dpData)) {
							ExternalCheck externalCheck=new ExternalCheck();
							crateExternalCheckC1(externalCheck, dpData);;
							externalCheckManager.saveExternalCheckC1(externalCheck);
						}
					 }
				 }
				 
				 if(!CollectionUtils.isEmpty(dpList)) {
					 for(VehCheckLogin dc: dtpList) {
						String dtdpData =  getSZPTOutCheck(dc,"DC");
						if(!StringUtils.isEmpty(dtdpData)) {
							ExternalCheck externalCheck=new ExternalCheck();
							crateExternalCheckDC(externalCheck, dtdpData);;
							externalCheckManager.saveExternalCheckDC(externalCheck);
						}
					 }
				 }
				 
			 }
		}
	}
	
	private void crateExternalCheck(ExternalCheck ec,String xml) throws UnsupportedEncodingException, DocumentException {
		
		xml =URLDecoder.decode(xml,"UTF-8");
		
		Document doc =  DocumentHelper.parseText(xml);
		Element root = doc.getRootElement();
		Element vehispara = root.element("vehispara");
		
		ec.setItem1(vehispara.elementText("rhplx"));
		ec.setItem2(vehispara.elementText("rppxh"));
		ec.setItem3(vehispara.elementText("rvin"));
		ec.setItem4(vehispara.elementText("rfdjh"));
		ec.setItem5(vehispara.elementText("rcsys"));
		ec.setItem6(vehispara.elementText("rwkcc"));
		ec.setItem7(vehispara.elementText("rzj"));
		ec.setItem8(vehispara.elementText("rzbzl"));
		ec.setItem9(vehispara.elementText("rhdzrs"));
		ec.setItem10(vehispara.elementText("rhdzll"));
		ec.setItem11(vehispara.elementText("rlbgd"));
		ec.setItem12(vehispara.elementText("rhzgbthps"));
		ec.setItem13(vehispara.elementText("rkcyjck"));
		ec.setItem14(vehispara.elementText("rkccktd"));
		ec.setItem15(vehispara.elementText("rhx"));
		ec.setItem16(vehispara.elementText("rcswg"));
		ec.setItem17(vehispara.elementText("rwgbs"));
		ec.setItem18(vehispara.elementText("rwbzm"));
		ec.setItem19(vehispara.elementText("rlt"));
		ec.setItem20(vehispara.elementText("rhpaz"));
		ec.setItem21(vehispara.elementText("rjzgj"));
		ec.setItem22(vehispara.elementText("rqcaqd"));
		ec.setItem23(vehispara.elementText("rsjp"));
		ec.setItem24(vehispara.elementText("rmhq"));
		ec.setItem25(vehispara.elementText("rxsjly"));
		ec.setItem26(vehispara.elementText("rcsfgbs"));
		ec.setItem27(vehispara.elementText("rclwbzb"));
		ec.setItem28(vehispara.elementText("rchfh"));
		ec.setItem29(vehispara.elementText("ryjc"));
		ec.setItem30(vehispara.elementText("rjjx"));
		ec.setItem31(vehispara.elementText("rxsgn"));
		ec.setItem32(vehispara.elementText("rfbs"));
		ec.setItem33(vehispara.elementText("rfzzd"));
		ec.setItem34(vehispara.elementText("rpszdq"));
		ec.setItem35(vehispara.elementText("rjjqd"));
		ec.setItem36(vehispara.elementText("rfdjcmh"));
		ec.setItem37(vehispara.elementText("rsddd"));
		ec.setItem38(vehispara.elementText("rfzdtb"));
		ec.setItem39(vehispara.elementText("rxcbz"));
		ec.setItem40(vehispara.elementText("rwxhwbz"));
		ec.setItem41(vehispara.elementText("ztcjrfzzz"));
		ec.setItem80(vehispara.elementText("rlwcx"));
		
		
		if(!StringUtils.isEmpty(vehispara.elementText("cwkc"))) {
			ec.setCwkc(Integer.parseInt(vehispara.elementText("cwkc")));
		}
		
		if(!StringUtils.isEmpty(vehispara.elementText("cwkg"))) {
			ec.setCwkg(Integer.parseInt(vehispara.elementText("cwkg")));
		}
		
		if(!StringUtils.isEmpty(vehispara.elementText("cwkk"))) {
			ec.setCwkk(Integer.parseInt(vehispara.elementText("cwkk")));
		}
		
		if(!StringUtils.isEmpty(vehispara.elementText("zbzl"))) {
			ec.setZbzl(Integer.parseInt(vehispara.elementText("zbzl")));
		}
		
		
		ec.setJylsh(vehispara.elementText("jylsh"));
		ec.setHphm(vehispara.elementText("hphm"));
		ec.setHpzl(vehispara.elementText("hpzl"));
		ec.setJycs(Integer.parseInt(vehispara.elementText("jycs")));
		ec.setJyjgbh(vehispara.elementText("jyjgbh"));
		
		logger.info("wgjcjyy="+vehispara.elementText("wgjcjyy"));
		
		ec.setWgjcjyy(vehispara.elementText("wgjcjyy"));
		ec.setWgjcjyysfzh(vehispara.elementText("wgjcjyysfzh"));
		
		
	}
	
	
	private void crateExternalCheckC1(ExternalCheck ec,String xml) throws UnsupportedEncodingException, DocumentException {
		
		xml =URLDecoder.decode(xml,"UTF-8");
		
		Document doc =  DocumentHelper.parseText(xml);
		Element root = doc.getRootElement();
		Element vehispara = root.element("vehispara");
		
		ec.setItem46(vehispara.elementText("rzxxbj"));
		ec.setItem47(vehispara.elementText("rzxxbj"));
		ec.setItem48(vehispara.elementText("rzxxbj"));
		ec.setItem49(vehispara.elementText("rzxxbj"));
		ec.setItem50(vehispara.elementText("rzxxbj"));
		ec.setDpjcjyy(vehispara.elementText("dpjcjyy"));
		ec.setDpjyysfzh(vehispara.elementText("dpjyysfzh"));
		
		ec.setJylsh(vehispara.elementText("jylsh"));
		ec.setHphm(vehispara.elementText("hphm"));
		ec.setHpzl(vehispara.elementText("hpzl"));
		ec.setJycs(Integer.parseInt(vehispara.elementText("jycs")));
		ec.setJyjgbh(vehispara.elementText("jyjgbh"));
		
	}
	
	private void crateExternalCheckDC(ExternalCheck ec,String xml) throws UnsupportedEncodingException, DocumentException {
		
		xml =URLDecoder.decode(xml,"UTF-8");
		
		Document doc =  DocumentHelper.parseText(xml);
		Element root = doc.getRootElement();
		Element vehispara = root.element("vehispara");
		
		ec.setItem42(vehispara.elementText("rzxx"));
		ec.setItem43(vehispara.elementText("rcdx"));
		ec.setItem44(vehispara.elementText("rzdx"));
		ec.setItem45(vehispara.elementText("rybzsq"));
		ec.setDpdtjyy(vehispara.elementText("dpdtjyy"));
		ec.setDpdtjyysfzh(vehispara.elementText("dpdtjyysfzh"));
		
		ec.setJylsh(vehispara.elementText("jylsh"));
		ec.setHphm(vehispara.elementText("hphm"));
		ec.setHpzl(vehispara.elementText("hpzl"));
		ec.setJycs(Integer.parseInt(vehispara.elementText("jycs")));
		ec.setJyjgbh(vehispara.elementText("jyjgbh"));
		
	}
	
	
	
	
	
	public String getSZPTOutCheck(VehCheckLogin wj,String jyxm) {
		 HttpHeaders headers = new HttpHeaders();
		// headers.setContentType(MediaType);
		 
		 Document doc = DocumentHelper.createDocument();
		 Element root = doc.addElement("root");
		 
		 root.addElement("jylsh").setText(wj.getJylsh());
		 root.addElement("jyjgbh").setText(wj.getJyjgbh());
		 root.addElement("jyxm").setText(jyxm);
		 root.addElement("jycs").setText(String.valueOf(wj.getJycs()));
		 //请求体
		 HttpEntity<String> formEntity = new HttpEntity<>(doc.asXML(), headers);
		 ResponseEntity<String> responseEntity = restTemplate.postForEntity("http://190.205.0.11:6230/Interface/VehiclePDAQueryCheck.ashx?cmd=pdacheck", formEntity, String.class);                                  
		 return responseEntity.getBody();
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
		List<BaseParams> bps = baseParamsManager.getBaseParamByType("lwsc");
		if(!CollectionUtils.isEmpty(bps)) {
			if("false".equals(bps.get(0).getParamValue())) {
				return ;
			}
		}
		
//		if (!isNetwork || !getUploadSwitch()) {
//			return;
//		}
		List<CheckEvents> list = (List<CheckEvents>) eventManger.getEvents();
		List<BaseParams> paams = BaseParamsUtil.getBaseParamsByType("szdsfpt");
		for (CheckEvents e : list) {
			Thread.sleep(1000);
			try {
				String viewName = "V" + e.getEvent();

				String checkItem = e.getJyxm();

				if (RCAConstant.V18C63.equals(e.getEvent())) {
					uploadImage(e);
					continue;
				}
				
				
				if(!CollectionUtils.isEmpty(paams)
						&&"true".equals(paams.get(0).getParamValue())
						&&"M1".equals(e.getJyxm())) {
					
					 SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					 VehCheckLogin vehCheckLogin = checkDataManager.getVehCheckLogin(e.getJylsh());
					 StringBuilder sb=new StringBuilder();
					 sb.append("^^zpzp^^");
					 sb.append(vehCheckLogin.getJylsh());
					 sb.append("^^");
					 sb.append(vehCheckLogin.getJyjgbh());
					 sb.append("^^");
					 sb.append(vehCheckLogin.getJcxdh());
					 sb.append("^^");
					 sb.append(vehCheckLogin.getJycs());
					 sb.append("^^");
					 sb.append(vehCheckLogin.getHphm());
					 sb.append("^^");
					 sb.append(vehCheckLogin.getHpzl());
					 sb.append("^^");
					 sb.append(vehCheckLogin.getClsbdh());
					 sb.append("^^");
					 sb.append("^^");
					 sb.append(sdf.format(new Date()));
					 sb.append("^^");
					 sb.append("M1");
					 sb.append("^^");
					if(RCAConstant.V18C55.equals(e.getEvent())) {
						 sb.append("0360");
					}else if(RCAConstant.V18C58.equals(e.getEvent())) {
						 sb.append("0361");
					}
					 sb.append("^^");
					 logger.info("拍照指令="+sb.toString());
					 try {
						TakePicture.toSzServerSocket(sb.toString());
					} catch (IOException e2) {
						logger.error("深圳平台拍照指令异常",e2);
					}
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
						if(!CollectionUtils.isEmpty(paams)) {
							 String szdsfpt = paams.get(0).getParamValue();
							 if("true".equals(szdsfpt)) {
								 Document xml = BeanXMLUtil.map2xml(data, "vehispara");
								 logger.info("开始写入深圳平台");
								 writeSZPT(e.getEvent(),xml.asXML());
							 }
						}
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
	
	
	public void writeSZPT(String jkid,String xml) throws Exception {
		try {
			IaspecTmriOutAccessStub itoa =new IaspecTmriOutAccessStub();
			IaspecTmriOutAccessStub.WriteObjectOut wout= new IaspecTmriOutAccessStub.WriteObjectOut();
			wout.setJkid(jkid);
			wout.setJkxlh(jkxlh);
			wout.setWriteXmlDoc(xml);
			wout.setXtlb("18");
			
			IaspecTmriOutAccessStub.WriteObjectOutResponse woo = itoa.writeObjectOut(wout);
			String response = woo.getWriteObjectOutResult();
			logger.info(response);
			
		}catch (Exception e) {
			logger.info("写入深圳第三方平台报错！");
			throw e;
		}
		
	}
	

	public void uploadImage(CheckEvents e) {

		String zpzl = e.getZpzl();

		CheckPhoto photo = checkDataManager.getCheckPhoto(e.getJylsh(), zpzl, e.getJycs());
		
		List<BaseParams> paams = BaseParamsUtil.getBaseParamsByType("szdsfpt");
		if(!CollectionUtils.isEmpty(paams)&&"true".equals(paams.get(0).getParamValue())) {
			eventManger.delete(e);
			return;
		}

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
			String imageCode =null;
			if (photo.getZp() != null) {
				BASE64Encoder encode = new BASE64Encoder();
				imageCode = encode.encode(photo.getZp());
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
	
	
	@Scheduled(fixedDelay = 1000*5)
	private void dowloadVideo() throws Exception{
		
		List<VehCheckProcess> datas = this.vehProcessManager.getDowloadsData();
		
		List<VideoConfig> configs = videoManager.getConfig(jyjgbh);
		
		if(CollectionUtils.isEmpty(configs)) {
			return;
		}
		
		for(VehCheckProcess vcp: datas) {
			
			VehCheckLogin vehCheckLogin = vehManager.getVehCheckLoginByJylsh(jyjgbh, vcp.getJylsh());
			String jcxdh = vcp.getJcxdh()==null?vehCheckLogin.getJcxdh():String.valueOf(vcp.getJcxdh());
			
			for(VideoConfig config: configs) {
				if(config.getJyxm().indexOf(vcp.getJyxm())!=-1&&config.getJcxdh().equals(jcxdh)) {
					try {
						Date  kssj =vcp.getKssj();
						if(vcp.getJyxm().equals("H4")&&vcp.getJycs()==1) {
							
							Calendar calendar = Calendar.getInstance();
							calendar.setTime(vcp.getKssj());
							calendar.add(Calendar.SECOND, 20);
							kssj=calendar.getTime();
						}
						
						hkUtil.downLoad(config, hkUtil.convert(kssj), hkUtil.convert(vcp.getJssj()), vcp.getJylsh()+"_"+vcp.getJycs()+"_"+vcp.getJyxm()+"_"+config.getChannel()); 
						vcp.setVoideSate(1);
						vehProcessManager.saveVehProcessSync(vcp);
					}catch (Exception e) {
						vcp.setVoideSate(2);
						vehProcessManager.saveVehProcessSync(vcp);
					}
					
					
				}
				
			}
			
			
		}
		
		
	}

	


}
