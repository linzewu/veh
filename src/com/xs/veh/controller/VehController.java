package com.xs.veh.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.rca.ws.client.OutServerServiceStub;
import com.xs.veh.entity.CheckLog;
import com.xs.veh.entity.User;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.entity.VehInfo;
import com.xs.veh.manager.VehManager;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

@Controller
@RequestMapping(value = "/veh")
@Modular(modelCode="veh",modelName="机动车检测")
public class VehController {
	
	static Log logger = LogFactory.getLog( VehController.class);
	

	@Value("${jyjgbh}")
	private String jyjgbh;

	@Value("${sf}")
	private String sf;

	@Value("${cs}")
	private String cs;
	
	@Value("${jkxlh}")
	private String jkxlh;

	@Resource(name = "vehManager")
	private VehManager vehManager;

	@Autowired
	private HttpSession session;

	/**
	 * 注册时间类型的属性编辑器，将String转化为Date
	 */
	@InitBinder
	public void initBinder(ServletRequestDataBinder binder) {
		binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true));
	}

	@UserOperation(code="getVehInfo",name="联网查询机动车信息")
	@RequestMapping(value = "getVehInfo", method = RequestMethod.POST)
	public @ResponseBody String getVehInfo(@RequestParam Map param)
			throws RemoteException, UnsupportedEncodingException, DocumentException {
		JSON json = vehManager.getVehInfoOfws(param);
		logger.info("获取基本信息返回：="+json);
		return json.toString();
	}

	@UserOperation(code="getVehInfo",name="查询机动车查验项",isMain=false)
	@RequestMapping(value = "getVehCheckItem", method = RequestMethod.POST)
	public @ResponseBody String getVehCheckItem(@RequestParam Map param)
			throws RemoteException, UnsupportedEncodingException, DocumentException {
		JSON json = vehManager.getVehCheckItem(param);
		return json.toString();
	}

	@UserOperation(code="getVehCheckeProcess",name="查询机动车检测过程")
	@RequestMapping(value = "getVehCheckeProcess", method = RequestMethod.POST)
	public @ResponseBody List<VehCheckProcess> getVehCheckeProcess(String jylsh) {
		List<VehCheckProcess> vcps = vehManager.getVehCheckPrcoessByJylsh(jylsh);
		return vcps;
	}

	@UserOperation(code="getVehChecking",name="查询已登录机动车")
	@RequestMapping(value = "getVehChecking", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> getVehChecking(Integer page, Integer rows, VehCheckLogin vehCheckLogin,
			@RequestParam(required = false) String statusArry) {

		Integer[] jczt = null;
		if (statusArry != null && !"".equals(statusArry.trim())) {
			String[] ss = statusArry.split(",");
			jczt = new Integer[ss.length];
			int i = 0;
			for (String s : ss) {
				jczt[i] = Integer.parseInt(s);
				i++;
			}
		}
		List<VehCheckLogin> vcps = vehManager.getVehChecking(page, rows, vehCheckLogin, jczt);
		
		Integer total = vehManager.getVehCheckingCount(page, rows, vehCheckLogin, jczt);
		
		Map<String,Object> data =new HashMap<String,Object>();
		
		data.put("rows", vcps);
		data.put("total", total);
		
		
		return data;
	}

	@UserOperation(code="vehLogin",name="机动车登录")
	@RequestMapping(value = "vehLogin", method = RequestMethod.POST)
	public @ResponseBody String vehLogin(VehCheckLogin vehCheckLogin, VehInfo vehInfo)
			throws RemoteException, UnsupportedEncodingException, DocumentException, InterruptedException {

		if (!vehManager.isLoged(vehCheckLogin)) {
			String jylsh = this.vehManager.getJylsh();
			String jyxm = vehCheckLogin.getJyxm();
			vehCheckLogin.setJylsh(jylsh.trim());
			vehCheckLogin.setJyjgbh(jyjgbh);
			vehCheckLogin.setJycs(1);
			vehCheckLogin.setDlsj(new Date());
			vehCheckLogin.setVehjczt(VehCheckLogin.JCZT_DL);

			if (jyxm.indexOf("F1") != -1) {
				vehCheckLogin.setVehwjzt(VehCheckLogin.ZT_WKS);
			} else {
				vehCheckLogin.setVehwjzt(VehCheckLogin.ZT_BJC);
			}

			if (jyxm.indexOf("C1") != -1) {
				vehCheckLogin.setVehdpzt(VehCheckLogin.ZT_WKS);
			} else {
				vehCheckLogin.setVehdpzt(VehCheckLogin.ZT_BJC);
			}

			if (jyxm.indexOf("DC") != -1) {
				vehCheckLogin.setVehdtdpzt(VehCheckLogin.ZT_WKS);
			} else {
				vehCheckLogin.setVehdtdpzt(VehCheckLogin.ZT_BJC);
			}

			// 路试状态
			if (jyxm.indexOf("R") != -1) {
				vehCheckLogin.setVehlszt(VehCheckLogin.ZT_WKS);
			} else {
				vehCheckLogin.setVehlszt(VehCheckLogin.ZT_BJC);
			}
			
			// 整备质量测量
			if (jyxm.indexOf("Z") != -1) {
				vehCheckLogin.setVehzbzlzt(VehCheckLogin.ZT_WKS);
			} else {
				vehCheckLogin.setVehzbzlzt(VehCheckLogin.ZT_BJC);
			}
			
			// 外廓尺寸测量
			if (jyxm.indexOf("M") != -1) {
				vehCheckLogin.setVehwkzt(VehCheckLogin.ZT_WKS);
			} else {
				vehCheckLogin.setVehwkzt(VehCheckLogin.ZT_BJC);
			}

			// 上线状态
			if (jyxm.indexOf("H") != -1 || jyxm.indexOf("B") != -1 || jyxm.indexOf("S") != -1
					|| jyxm.indexOf("A") != -1||jyxm.indexOf("X") != -1) {
				vehCheckLogin.setVehsxzt(VehCheckLogin.ZT_WKS);
			} else {
				vehCheckLogin.setVehsxzt(VehCheckLogin.ZT_BJC);
			}
			User user = (User) session.getAttribute("user");
			if (user != null) {
				vehCheckLogin.setDly(user.getRealName());
				vehCheckLogin.setDlysfzh(user.getIdCard());
			}
			JSONObject json = this.vehManager.vehLogin(vehCheckLogin);
			
			for(int i=0;i<50;i++){
				Thread.sleep(100);
				CheckLog checkLog = vehManager.getLoginCheckLog(vehCheckLogin.getJylsh());
				if(checkLog!=null){
					json.put("checkLog", checkLog);
					if("1".equals(checkLog.getCode())) {
						upLoadBk(vehCheckLogin);
					}
					
					break;
				}
			}
			
			return json.toString();
		} else {
			JSONObject head = new JSONObject();
			head.put("code", "-1");
			head.put("message", "登陆失败，该车辆已登陆。");
			JSONObject messager = new JSONObject();
			messager.put("head", head);
			return messager.toString();
		}

	}

	/**
	 * 
	 * @param id
	 * @return
	 * @throws DocumentException
	 * @throws UnsupportedEncodingException
	 * @throws RemoteException
	 */
	@UserOperation(code="vheUnLogin",name="机动车退办")
	@RequestMapping(value = "vheUnLogin", method = RequestMethod.POST)
	public @ResponseBody String vehDelete(Integer id)
			throws RemoteException, UnsupportedEncodingException, DocumentException {
		return this.vehManager.unLoginVeh(id).toString();
	}

	@UserOperation(code="getVehInfo",name="联网查询机动车信息")
	@RequestMapping(value = "getDefaultConfig", method = RequestMethod.POST)
	public @ResponseBody String getDefaultConfig(VehCheckLogin vehCheckLogin, VehInfo vehInfo) {
		JSONObject json = new JSONObject();
		json.put("sf", sf);
		json.put("cs", cs);
		return json.toString();
	}
	
	
	@UserOperation(code="relogin",name="复检登陆")
	@RequestMapping(value = "relogin", method = RequestMethod.POST)
	public @ResponseBody String relohin(String jylsh, String fjjyxm) {
		
		this.vehManager.saveRelogin2(jylsh, fjjyxm);
		
		JSONObject json = new JSONObject();
		json.put("state", "OK");
		return json.toString();
	
	}
	
	

//	@UserOperation(code="getVehSjr",name="获取机动车送检人")
//	@RequestMapping(value = "getVehSjr", method = RequestMethod.POST)
//	public @ResponseBody String getVehSjr(@RequestParam String hphm,
//			@RequestParam String hpzl,
//			@RequestParam String sjdw,
//			@RequestParam String sfzmhm,
//			@RequestParam String jclb,
//			@RequestParam String syxz
//			) throws AxisFault, UnsupportedEncodingException, DocumentException {
//		
//		ServiceClient serviceClient = new ServiceClient();
//        //创建服务地址WebService的URL,注意不是WSDL的URL
//        String url = "http://172.16.90.18:9081/remoteServerZx/services/zxServer?wsdl";
//        EndpointReference targetEPR = new EndpointReference(url);
//        Options options = serviceClient.getOptions();
//        options.setTo(targetEPR);
//        //确定调用方法（wsdl 命名空间地址 (wsdl文档中的targetNamespace) 和 方法名称 的组合）
//        options.setAction("http://172.16.90.18:9081/remoteServerZx/services/zxServer/queryObjectOut");
//
//        OMFactory fac = OMAbstractFactory.getOMFactory();
//        /*
//         * 指定命名空间，参数：
//         * uri--即为wsdl文档的targetNamespace，命名空间
//         * perfix--可不填
//         */
//        OMNamespace omNs = fac.createOMNamespace("http://172.16.90.18:9081/remoteServerZx/services/zxServer/", "");
//        // 指定方法
//        OMElement method = fac.createOMElement("queryObjectOut", omNs);
//        // 指定方法的参数
//        OMElement xtlb  = fac.createOMElement("xtlb", omNs);
//        xtlb.setText("01");
//        OMElement jkxlh  = fac.createOMElement("jkxlh", omNs);
//        jkxlh.setText("123");
//        OMElement jkid  = fac.createOMElement("jkid", omNs);
//        jkid.setText("01C04");
//        
//        
//        OMElement queryXmlDoc  = fac.createOMElement("queryXmlDoc", omNs);
//        
//        Document document = DocumentHelper.createDocument();
//        Element root =  document.addElement("root");
//        Element queryCondition = root.addElement("queryCondition");
//        
//        logger.info("hpzl=" + hpzl);
//        logger.info("hphm=" + hphm);
//        logger.info("sjdw=" + sjdw);
//        logger.info("sfzmhm=" + sfzmhm);
//        logger.info("jclb=" + jclb);
//        logger.info("syxz=" + syxz);
//        
//        queryCondition.addElement("hpzl").setText(hpzl);
//        queryCondition.addElement("hphm").setText(hphm);
//        queryCondition.addElement("sjdw").setText(sjdw);
//        queryCondition.addElement("sfzmhm").setText(sfzmhm);
//        
//        queryCondition.addElement("jclb").setText(jclb);
//        queryCondition.addElement("syxz").setText(syxz);
//        
//        logger.info("param:="+URLEncoder.encode(document.asXML(),"UTF-8"));
//        
//        queryXmlDoc.setText(URLEncoder.encode(document.asXML(),"UTF-8")); 
//        
//        method.addChild(xtlb);
//        method.addChild(jkxlh);
//        method.addChild(jkid);
//        method.addChild(queryXmlDoc);
//        method.build();
//        //远程调用web服务
//        OMElement result = serviceClient.sendReceive(method);
//        
//        logger.info("布控平台返回="+result.toString());
//        
//        result =  result.getFirstElement();
//        
//        String text = URLDecoder.decode(result.toString(),"UTF-8");
//        logger.info("布控平台返回="+text);
//        return  new XMLSerializer().read(text).toString();
//        
//	}
	
	
	private void upLoadBk(VehCheckLogin vehCheckLogin) throws UnsupportedEncodingException, RemoteException {
		
		SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ServiceClient serviceClient = new ServiceClient();
        //创建服务地址WebService的URL,注意不是WSDL的URL
        String url = "http://172.16.90.18:9081/remoteServerZx/services/zxServer?wsdl";
        EndpointReference targetEPR = new EndpointReference(url);
        Options options = serviceClient.getOptions();
        options.setTo(targetEPR);
        //确定调用方法（wsdl 命名空间地址 (wsdl文档中的targetNamespace) 和 方法名称 的组合）
        options.setAction("http://172.16.90.18:9081/remoteServerZx/services/zxServer/writeObjectOut");

        OMFactory fac = OMAbstractFactory.getOMFactory();
        /*
         * 指定命名空间，参数：
         * uri--即为wsdl文档的targetNamespace，命名空间
         * perfix--可不填
         */
        OMNamespace omNs = fac.createOMNamespace("http://172.16.90.18:9081/remoteServerZx/services/zxServer/", "");
        // 指定方法
        OMElement method = fac.createOMElement("writeObjectOut", omNs);
        // 指定方法的参数
        OMElement xtlb  = fac.createOMElement("xtlb", omNs);
        xtlb.setText("01");
        
        OMElement jkxlh  = fac.createOMElement("jkxlh", omNs);
        jkxlh.setText("");
        
        OMElement jkid  = fac.createOMElement("jkid", omNs);
        jkid.setText("01A33");
        
        
        OMElement writeXmlDoc  = fac.createOMElement("writeXmlDoc", omNs);
        
        Document document = DocumentHelper.createDocument();
        Element root =  document.addElement("root");
        Element vehispara = root.addElement("vehispara");
		
	     vehispara.addElement("hpzl").setText(vehCheckLogin.getHpzl());
	     vehispara.addElement("hphm").setText(vehCheckLogin.getHphm());
	     vehispara.addElement("dlrmc").setText(vehCheckLogin.getVehsjr());
	     vehispara.addElement("dlrsfzh").setText(vehCheckLogin.getVehsjrsfzmhm());
	     vehispara.addElement("jclb").setText(vehCheckLogin.getJylb());
	     vehispara.addElement("jyjgbh").setText(jyjgbh);
	     vehispara.addElement("clsbdh").setText(vehCheckLogin.getClsbdh());
	     vehispara.addElement("jyrq").setText(sdf.format(vehCheckLogin.getDlsj()));
	     vehispara.addElement("syr").setText(vehCheckLogin.getSyr());
	     logger.info("参数="+document.asXML());
	     writeXmlDoc.setText(document.asXML()); 
        method.addChild(xtlb);
        method.addChild(jkxlh);
        method.addChild(jkid);
        method.addChild(writeXmlDoc);
        method.build();
        //远程调用web服务
        OMElement result = serviceClient.sendReceive(method);
        
        logger.info(URLDecoder.decode(result.toString(),"UTF-8"));
	}
	
	@UserOperation(code="getVehSjr",name="获取机动车送检人")
	@RequestMapping(value = "getVehSjr", method = RequestMethod.POST)
	public @ResponseBody String getVehSjr(@RequestParam String hphm,
			@RequestParam String hpzl,
			@RequestParam String sjdw,
			@RequestParam String sfzmhm,
			@RequestParam String jclb,
			@RequestParam String syxz
			) throws UnsupportedEncodingException, DocumentException, RemoteException {
		
		
		OutServerServiceStub stub =new OutServerServiceStub();
		OutServerServiceStub.QueryObjectOut queryObjectOut=new OutServerServiceStub.QueryObjectOut();
		queryObjectOut.setJkid("01C04");
		queryObjectOut.setXtlb("01");
		queryObjectOut.setJkxlh("");
        Document document = DocumentHelper.createDocument();
        
	      Element root =  document.addElement("root");
	      Element queryCondition = root.addElement("QueryCondition");
	      logger.info("hpzl=" + hpzl);
	      logger.info("hphm=" + hphm);
	      logger.info("sjdw=" + sjdw);
	      logger.info("sfzmhm=" + sfzmhm);
	      logger.info("jclb=" + jclb);
	      logger.info("syxz=" + syxz);
	      queryCondition.addElement("hpzl").setText(hpzl);
	      queryCondition.addElement("hphm").setText(hphm);
	      queryCondition.addElement("sjdw").setText(sjdw);
	      queryCondition.addElement("sfzmhm").setText(sfzmhm);
	      
	     queryCondition.addElement("jclb").setText(jclb);
	     queryCondition.addElement("syxz").setText(syxz);
        
        logger.info("hpzl=" + hpzl);
        logger.info("hphm=" + hphm);
        logger.info("sjdw=" + sjdw);
        logger.info("sfzmhm=" + sfzmhm);
        logger.info("jclb=" + jclb);
        logger.info("syxz=" + syxz);
        logger.info("param:="+URLEncoder.encode(document.asXML(),"UTF-8"));
        queryObjectOut.setQueryXmlDoc(URLEncoder.encode(document.asXML(),"UTF-8"));
        OutServerServiceStub.QueryObjectOutResponse res = stub.queryObjectOut(queryObjectOut);
        String xml =  res.getQueryObjectOutReturn();
        logger.info("布控平台返回xml="+xml);
        if(!StringUtils.isEmpty(xml)) {
        	  String text = URLDecoder.decode(xml,"UTF-8");
              logger.info("布控平台返回="+text);
              return  new XMLSerializer().read(text).toString();
        }else {
        	return "";
        }
      
        
	}
	

}
