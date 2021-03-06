package com.xs.veh.controller;

import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.enums.CommonUserOperationEnum;
import com.xs.veh.entity.CheckLog;
import com.xs.veh.entity.User;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.entity.VehInfo;
import com.xs.veh.manager.VehManager;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

@Controller
@RequestMapping(value = "/veh")
@Modular(modelCode="veh",modelName="机动车检测")
public class VehController {

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
					|| jyxm.indexOf("A") != -1) {
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

}
