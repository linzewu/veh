package com.xs.veh.controller;

import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.common.BaseParamsUtil;
import com.xs.common.ResultHandler;
import com.xs.veh.entity.BaseParams;
import com.xs.veh.entity.CheckLog;
import com.xs.veh.entity.TestVeh;
import com.xs.veh.entity.User;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.entity.VehInfo;
import com.xs.veh.manager.BaseParamsManager;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.manager.CheckEventManger;
import com.xs.veh.manager.ExtendManage;
import com.xs.veh.manager.TestVehManager;
import com.xs.veh.manager.VehManager;
import com.xs.veh.manager.ZHCheckDataManager;
import com.xs.veh.util.PlayUtil;
import com.xs.veh.util.RCAConstant;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

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

	@Resource(name = "vehManager")
	private VehManager vehManager;

	@Autowired
	private HttpSession session;
	
	@Autowired
	private BaseParamsManager baseParamsManager;
	
	@Autowired
	private CheckDataManager checkDataManager;
	@Autowired
	private ExtendManage et;
	@Autowired
	private ZHCheckDataManager zhCheckDataManager;
	@Autowired
	private TestVehManager testVehManager;
	
	@Resource(name = "checkEventManger")
	private CheckEventManger checkEventManger;

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
	
	
	@UserOperation(code="getVehInfoBybookNumber",name="联网查询机动车预约信息")
	@RequestMapping(value = "getVehInfoBybookNumber", method = RequestMethod.POST)
	public @ResponseBody String getVehInfoBybookNumber(@RequestParam Map param)
			throws RemoteException, UnsupportedEncodingException, DocumentException {
		
		JSON json = vehManager.getVehInfoOfbookNumberz(param);
		logger.info("获取基本信息返回：="+json);
		return json.toString();
	}
	
	
	@UserOperation(code="updatePrintStatus",name="自动打印报告")
	@RequestMapping(value = "updatePrintStatus", method = RequestMethod.POST)
	public @ResponseBody String updatePrintStatus(String jylsh) {
		vehManager.updatePrintStatus(jylsh);
		
		return "Success";
		
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
	public @ResponseBody String vehLogin(VehCheckLogin vehCheckLogin, VehInfo vehInfo,TestVeh testVeh)
			throws RemoteException, UnsupportedEncodingException, DocumentException, InterruptedException {

		if (!vehManager.isLoged(vehCheckLogin)) {
			String jylsh = this.vehManager.getJylsh();
			String jyxm = vehCheckLogin.getJyxm();
			vehCheckLogin.setJylsh(jylsh.trim());
			vehCheckLogin.setJyjgbh(jyjgbh);
			vehCheckLogin.setJycs(1);
			vehCheckLogin.setDlsj(new Date());
			vehCheckLogin.setVehjczt(VehCheckLogin.JCZT_DL);
			vehCheckLogin.setPrintStatus(0);

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
			
			// 综合检测
			if (jyxm.indexOf("EP") != -1||jyxm.indexOf("OP") != -1||jyxm.indexOf("PF") != -1) {
				vehCheckLogin.setVehzhjz(VehCheckLogin.ZT_WKS);
			} else {
				vehCheckLogin.setVehzhjz(VehCheckLogin.ZT_BJC);
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
			
			List<BaseParams> baseParams =  baseParamsManager.getBaseParamByType("sdwz");
			boolean sdFlag =false;
			if(!CollectionUtils.isEmpty(baseParams)) {
				sdFlag= baseParams.get(0).getParamValue().equals("true");
			}
			
			//如果是综合检测
			if(vehCheckLogin.getCheckType()==1) {
				
				String seq = this.vehManager.getBgdbh().trim();
				
				String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
				String cs = null;
				if(vehCheckLogin.getJycs()<10) {
					cs="0"+vehCheckLogin.getJycs();
				}else {
					cs=vehCheckLogin.getJycs()+"";
				}
				String bgdbh="321303"+year.substring(2,4)+seq+cs;
				testVeh.setBgdbh(bgdbh);
				processTestVeh(vehCheckLogin,testVeh);
				//写入综合检测表
				if(testVeh.getQlj()==null) {
					testVeh.setQlj(0);
				}
				this.vehManager.saveTestVeh(testVeh);
			}
			
			if(vehCheckLogin.getCheckType()== 0&&sdFlag&&vehCheckLogin.getJyxm().indexOf("S1")>=0&&vehCheckLogin.getZs()>=3) {
				processTestVehS1(vehCheckLogin,testVeh);
				if(testVeh.getQlj()==null) {
					testVeh.setQlj(0);
				}
				this.vehManager.saveTestVeh(testVeh);
			}
			
			JSONObject json = this.vehManager.vehLogin(vehCheckLogin);
			
			for(int i=0;i<50;i++){
				Thread.sleep(100);
				CheckLog checkLog = vehManager.getLoginCheckLog(vehCheckLogin.getJylsh());
				if(checkLog!=null){
					json.put("checkLog", checkLog);
					break;
				}
			}
			et.vehLoginAfter(vehCheckLogin);
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
	 * 综合检测数据处理
	 * @param vehCheckLogin
	 * @param testVeh
	 */
	private void processTestVeh(VehCheckLogin vehCheckLogin, TestVeh testVeh) {
		
		if(vehCheckLogin.getCheckType()==1) {
			String[] jyxmArray= vehCheckLogin.getJyxm().split(",");
			StringBuffer ajjyxm = new StringBuffer();
			StringBuffer zhjyxm = new StringBuffer();
			for(String jyxm:jyxmArray ) {
				if(sfzhjcxm(jyxm)) {
					zhjyxm.append(jyxm+",");
				}else {
					ajjyxm.append(jyxm+",");
				}
			}
//			if(ajjyxm.length()>0) {
//				
//				vehCheckLogin.setJyxm(ajjyxm.substring(0, ajjyxm.length()-1));
//				
//			}
			if(zhjyxm.length()>0) {
				testVeh.setJcxm(zhjyxm.substring(0, zhjyxm.length()-1));
			}
			
			if(vehCheckLogin.getCllx().indexOf("G")==0||vehCheckLogin.getCllx().indexOf("B")==0) {
				testVeh.setSfgc(1);
			}else {
				testVeh.setSfgc(0);
			}
			
			if(vehCheckLogin.getCllx().indexOf("K")==0) {
				testVeh.setSfkc(1);
			}else {
				testVeh.setSfkc(0);
			}
			
			//柴油车 必须先做驱动轴称重
			if(testVeh.getRlzl().equals("B")&&testVeh.getQdzkzzl()==null) {
				testVeh.setYsjc(0);
			}else {
				testVeh.setYsjc(1);
			}
			
			testVeh.setDlxpjbz("0");
			
			if(testVeh.getHccsxs()==null) {
				testVeh.setHccsxs(0);
			}
			
			testVeh.setEdnjzs(testVeh.getEdzs());
			
			
			testVeh.setCsbsx("40");
			
			testVeh.setCsbxx("32.8");
			
			testVeh.setQccd(vehCheckLogin.getCwkc());
			testVeh.setQcgd(vehCheckLogin.getCwkg());
			
			testVeh.setQycmzzl(0);
			testVeh.setJycs(vehCheckLogin.getJycs());
			
			testVeh.setJylsh(vehCheckLogin.getJylsh());
			
			testVeh.setEdgl(vehCheckLogin.getGl().intValue());
			
			testVeh.setJcwc(0);
			
			if("高级".equals(testVeh.getKcdj())) {
				testVeh.setYhcs("60");
			}else {
				testVeh.setYhcs("50");
			}
			
			testVeh.setYHxz();
			
		}
		
	}
	
	
	private void processTestVehS1(VehCheckLogin vehCheckLogin, TestVeh testVeh) {
		testVeh.setEdgl(0);
		testVeh.setEdnj(0);
		testVeh.setEdnjgl(0);
		testVeh.setEdnjzs(0);
		testVeh.setJgl(0);
		testVeh.setEdyh("0");
		testVeh.setEdzs(0);
		testVeh.setHccsxs(0);
		testVeh.setLtdmkd(0);
		testVeh.setJcxm("S1");
		testVeh.setLtlx(0);
		if(vehCheckLogin.getCllx().indexOf("G")==0||vehCheckLogin.getCllx().indexOf("B")==0) {
			testVeh.setSfgc(1);
		}else {
			testVeh.setSfgc(0);
		}
		
		if(vehCheckLogin.getCllx().indexOf("K")==0) {
			testVeh.setSfkc(1);
		}else {
			testVeh.setSfkc(0);
		}
		
		testVeh.setQdzkzzl(0);
		
		testVeh.setYsjc(1);
		
		testVeh.setCsbsx("40");
		
		testVeh.setCsbxx("32.8");
		
		testVeh.setQccd(vehCheckLogin.getCwkc());
		testVeh.setQcgd(vehCheckLogin.getCwkg());
		
		testVeh.setQycmzzl(0);
		testVeh.setJycs(vehCheckLogin.getJycs());
		
		testVeh.setJylsh(vehCheckLogin.getJylsh());
		
		testVeh.setEdgl(0);
		
		testVeh.setJcwc(0);
		
		testVeh.setYhcs("50");
		
		testVeh.setYhxz("0");
			
		
	}
	
	/**
	 * 检测是否综合检测
	 * @param jyxm
	 * @return
	 */
	private boolean sfzhjcxm(String jyxm) {
		
		BaseParams param = baseParamsManager.getBaseParam("zhjyxm", jyxm);
		
		if("zh".equals(param.getMemo())) {
			
			return true;
		}
		
		
		return false;
		
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
	public synchronized @ResponseBody String relohin(String jylsh, String fjjyxm,Integer reloginWeigth) {
		
		VehCheckLogin vehCheckLogin = this.checkDataManager.getVehCheckLogin(jylsh);
		
		if(vehCheckLogin.getVehjczt()!=VehCheckLogin.JCZT_JYJS) {
			JSONObject json = new JSONObject();
			json.put("state", "-1");
			json.put("message", "检测过程未结束，不能复检，请查看当前流水状态！");
			checkDataManager.resetEventState(jylsh);
			return json.toString();
		}
		
		boolean lwscFlag =true;
		
		List<BaseParams> bps = baseParamsManager.getBaseParamByType("lwsc");
		if(!CollectionUtils.isEmpty(bps)) {
			if("false".equals(bps.get(0).getParamValue())) {
				lwscFlag=false;
			}
		}
		
		boolean flag = this.checkEventManger.isExtendCehckEvent(jylsh,new String[] {RCAConstant.V18C55,RCAConstant.V18C81,RCAConstant.V18C80,
				RCAConstant.V18C58,RCAConstant.V18C54,RCAConstant.V18C82,RCAConstant.V18C59});
		if(flag&&lwscFlag) {
			JSONObject json = new JSONObject();
			json.put("state", "-1");
			json.put("message", "该过程存在数据未上传，正在重新上传平台数据，请等待15-30秒后重试，如果长时间无法复检登陆，请检查平台网络是否通畅！");
			checkDataManager.resetEventState(jylsh);
			return json.toString();
		}
		
		
		this.vehManager.saveRelogin2(jylsh, fjjyxm,reloginWeigth);
		
		JSONObject json = new JSONObject();
		json.put("state", "OK");
		return json.toString();
	
	}
	
	@UserOperation(code="getTestVehBylsh",name="获取综合检测基本信息")
	@RequestMapping(value = "getTestVehBylsh", method = RequestMethod.POST)
	public @ResponseBody TestVeh getTestVehBylsh(String jylsh) {
		
		VehCheckLogin vehCheckLogin = checkDataManager.getVehCheckLogin(jylsh);
		
		if(vehCheckLogin.getCheckType()==1) {
			TestVeh testVeh = this.vehManager.getTestVehBylsh(jylsh);
			return testVeh;
		}else {
			return null;
		}
	
	}
	
	
	@UserOperation(code="playVeh",name="获取综合检测基本信息")
	@RequestMapping(value = "playVeh", method = RequestMethod.POST)
	public @ResponseBody void playVeh(String hphm) {
		
		List<BaseParams> bps = BaseParamsUtil.getBaseParamsByType("yymb");
		char[] cc = hphm.toCharArray();
		String newHphm="";
		for(char c:cc) {
			newHphm+=c+" ";
		}
		PlayUtil playUtil=new PlayUtil();
		if(!CollectionUtils.isEmpty(bps)) {
			String mb = bps.get(0).getParamValue();
			String message = mb.replace("hphm", newHphm);
			playUtil.play(message, 2); 
		}else {
			playUtil.play(newHphm+"已制证，请车主到前台领取资料", 2);
		}
	}
	
	
	@UserOperation(code="getBhgJyxm",name="获取不合格检验项目")
	@RequestMapping(value = "getBhgJyxm", method = RequestMethod.POST)
	public @ResponseBody String getBhgJyxm(String jylsh) {
		return this.vehManager.getFjxm(jylsh);
	}
	
	
	@UserOperation(code="updateVehCheckLogin",name="修改基本信息")
	@RequestMapping(value = "updateVehCheckLogin")
	public @ResponseBody Map updateVehCheckLogin(@RequestParam Map map) throws ParseException {
		VehCheckLogin checkLogin = checkDataManager.getVehCheckLogin(map.get("jylsh").toString());//(vehCheckLogin.getJylsh());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		if(!StringUtils.isEmpty(map.get("ccrq"))) {			
			checkLogin.setCcrq(sdf.parse(String.valueOf(map.get("ccrq"))));
		}
		if(!StringUtils.isEmpty(map.get("ccdjrq"))) {			
			checkLogin.setCcdjrq(sdf.parse(String.valueOf(map.get("ccdjrq"))));
		}
		
		//checkLogin.setHphm(vehCheckLogin.getHphm());
		//checkLogin.setHpzl(vehCheckLogin.getHpzl());
		
		checkLogin.setZzl(Integer.parseInt(String.valueOf(map.get("zzl"))));
		checkLogin.setZbzl(Integer.parseInt(String.valueOf(map.get("zbzl"))));
		checkLogin.setCwkc(Integer.parseInt(String.valueOf(map.get("cwkc"))));
		checkLogin.setCwkk(Integer.parseInt(String.valueOf(map.get("cwkk"))));
		checkLogin.setCwkg(Integer.parseInt(String.valueOf(map.get("cwkg"))));
		checkLogin.setHdzk(Integer.parseInt(String.valueOf(map.get("hdzk"))));
		checkLogin.setCsys(String.valueOf(map.get("csys")));
		checkLogin.setRlzl(String.valueOf(map.get("rlzl")));
		checkLogin.setSyr(String.valueOf(map.get("syr")));
		if(!StringUtils.isEmpty(map.get("gl"))) {
			checkLogin.setGl(Float.valueOf(String.valueOf(map.get("gl"))));
		}
		if(!StringUtils.isEmpty(map.get("pl"))) {
			checkLogin.setPl(Float.valueOf(String.valueOf(map.get("pl"))));
		}
		this.vehManager.saveOrUpdateVehCheckLogin(checkLogin);
		if(checkLogin.getCheckType() == 1) {
			TestVeh testVeh = zhCheckDataManager.getTestVehbyJylsh(map.get("jylsh").toString());
			if(testVeh != null) {
				testVeh.setJcxz(String.valueOf(map.get("jcxz")));
				testVeh.setJqfs(String.valueOf(map.get("jqfs")));
				if(!StringUtils.isEmpty(map.get("jgl"))) {
					testVeh.setJgl(Integer.parseInt(String.valueOf(map.get("jgl"))));
				}
				if(!StringUtils.isEmpty(map.get("edzs"))) {
					testVeh.setEdzs(Integer.parseInt(String.valueOf(map.get("edzs"))));
				}
				if(!StringUtils.isEmpty(map.get("ednj"))) {
					testVeh.setEdnj(Integer.parseInt(String.valueOf(map.get("ednj"))));
				}
				if(!StringUtils.isEmpty(map.get("ednjgl"))) {
					testVeh.setEdnjgl(Integer.parseInt(String.valueOf(map.get("ednjgl"))));
				}
				testVeh.setEdyh(String.valueOf(map.get("edyh")));
				if(!StringUtils.isEmpty(map.get("ltlx"))) {
					testVeh.setLtlx(Integer.parseInt(String.valueOf(map.get("ltlx"))));
				}
				testVeh.setLtxh(String.valueOf(map.get("ltxh")));
				if(!StringUtils.isEmpty(map.get("hccsxs"))) {
					testVeh.setHccsxs(Integer.parseInt(String.valueOf(map.get("hccsxs"))));
				}
				if(!StringUtils.isEmpty(map.get("kczws"))) {
					testVeh.setKczws(Integer.parseInt(String.valueOf(map.get("kczws"))));
				}
				if(!StringUtils.isEmpty(map.get("bzzs"))) {
					testVeh.setBzzs(Integer.parseInt(String.valueOf(map.get("bzzs"))));
				}
				if(!StringUtils.isEmpty(map.get("ltdmkd"))) {
					testVeh.setLtdmkd(Integer.parseInt(String.valueOf(map.get("ltdmkd"))));
				}
				testVeh.setKcdj(String.valueOf(map.get("kcdj")));
				testVeh.setQychphm(String.valueOf(map.get("qychphm")));
				testVeh.setFdjxh(String.valueOf(map.get("fdjxh")));
				if(!StringUtils.isEmpty(map.get("pfhzz"))) {
					testVeh.setPfhzz(Integer.parseInt(String.valueOf(map.get("pfhzz"))));
				}
				testVeh.setBzzxs(String.valueOf(map.get("bzzxs")));
				if(!StringUtils.isEmpty(map.get("cxlbgd"))) {
					testVeh.setCxlbgd(Integer.parseInt(String.valueOf(map.get("cxlbgd"))));
				}
				testVeh.setCpys(String.valueOf(map.get("cpys")));
				testVeh.setZjclyt(String.valueOf(map.get("zjclyt")));
				if(!StringUtils.isEmpty(map.get("qdzkzzl"))) {
					testVeh.setQdzkzzl(Integer.parseInt(String.valueOf(map.get("qdzkzzl"))));
				}
				testVeh.setDlyxzh(String.valueOf(map.get("dlyxzh")));
				testVeh.setZjwtr(String.valueOf(map.get("zjwtr")));
				testVehManager.updateTestVeh(testVeh);
			}
		}
		
		
		return ResultHandler.toSuccessJSON("修改成功");
	}

}
