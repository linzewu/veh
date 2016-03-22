package com.xs.veh.controller;

import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckeProcess;
import com.xs.veh.entity.VehInfo;
import com.xs.veh.manager.VehManager;
import com.xs.veh.util.RCAConstant;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

@Controller
@RequestMapping(value = "/veh")
public class VehController {
	
	

	@Value("${jyjgbh}")
	private String jyjgbh;
	
	@Resource(name = "vehManager")
	private VehManager vehManager;

	/**
	 * 注册时间类型的属性编辑器，将String转化为Date
	 */
	@InitBinder
	public void initBinder(ServletRequestDataBinder binder) {
		binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true));
	}

	

	@RequestMapping(value = "getVehInfo", method = RequestMethod.POST)
	public @ResponseBody String getVehInfo(@RequestParam Map param)
			throws RemoteException, UnsupportedEncodingException, DocumentException {

		JSON json = vehManager.getVehInfoOfws(param);

		return json.toString();
	}

	@RequestMapping(value = "getVehCheckItem", method = RequestMethod.POST)
	public @ResponseBody String getVehCheckItem(@RequestParam Map param)
			throws RemoteException, UnsupportedEncodingException, DocumentException {

		JSON json = vehManager.getVehCheckItem(param);

		return json.toString();
	}

	@RequestMapping(value = "getVehCheckeProcess", method = RequestMethod.POST)
	public @ResponseBody List<VehCheckeProcess> getVehCheckeProcess(String jylsh) {
		List<VehCheckeProcess> vcps = vehManager.getVehCheckPrcoessByJylsh(jylsh);
		return vcps;
	}

	@RequestMapping(value = "getVehChecking", method = RequestMethod.POST)
	public @ResponseBody List<VehCheckLogin> getVehChecking(Integer page, Integer rows, VehCheckLogin vehCheckLogin,@RequestParam(required=false) String statusArry) {
		
		Integer[] status =null;
		if(statusArry!=null&&!"".equals(statusArry.trim())){
			String[] ss = statusArry.split(",");
			status=new Integer[ss.length];
			int i=0;
			for(String s: ss){
				status[i]=Integer.parseInt(s);
				i++;
			}
		}
		
		List<VehCheckLogin> vcps = vehManager.getVehChecking(page,rows,vehCheckLogin,status);
		return vcps;
	}

	@RequestMapping(value = "vehLogin", method = RequestMethod.POST)
	public @ResponseBody String vehLogin(VehCheckLogin vehCheckLogin, VehInfo vehInfo)
			throws RemoteException, UnsupportedEncodingException, DocumentException {

		String jylsh = this.vehManager.getJylsh();
		vehCheckLogin.setJylsh(jylsh.trim());
		vehCheckLogin.setJyjgbh(jyjgbh);
		vehCheckLogin.setJycs(1);
		vehCheckLogin.setStatus(RCAConstant.LOGGED);
		JSONObject json = this.vehManager.vehLogin(vehCheckLogin);
		return json.toString();
	}

	/**
	 * 
	 * @param id
	 * @return
	 * @throws DocumentException
	 * @throws UnsupportedEncodingException
	 * @throws RemoteException
	 */
	@RequestMapping(value = "vheUnLogin", method = RequestMethod.POST)
	public @ResponseBody String vehDelete(Integer id)
			throws RemoteException, UnsupportedEncodingException, DocumentException {
		return this.vehManager.deleteVeh(id).toString();
	}
	
	
	

}
