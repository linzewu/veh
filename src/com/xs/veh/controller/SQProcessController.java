package com.xs.veh.controller;

import java.util.Calendar;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.manager.SQProcessManager;
import com.xs.veh.manager.VehManager;

@Controller
@RequestMapping({ "/sqp" })
public class SQProcessController {

	@Value("${jyjgbh}")
	private String jyjgbh;
	@Resource(name = "vehManager")
	private VehManager vehManager;
	@Resource(name = "sqProcessManager")
	private SQProcessManager sqProcessManager;

	@RequestMapping(value = { "save" }, method = { org.springframework.web.bind.annotation.RequestMethod.POST })
	@ResponseBody
	public String vehLogin(@RequestParam("jylsh") String jylsh) {
		VehCheckLogin vehInfo = this.vehManager.getVehCheckLoginByJylsh(this.jyjgbh, jylsh);

		List<VehCheckProcess> vcps = this.vehManager.getVehCheckPrcoessByJylsh(jylsh);

		VehCheckProcess sqvpc = null;
		for (VehCheckProcess vpc : vcps) {
			if (vpc.getJyxm().equals("sq")) {
				sqvpc = vpc;
				break;
			}
		}
		if (sqvpc == null) {
			sqvpc = new VehCheckProcess();
			sqvpc.setClsbdh(vehInfo.getClsbdh());
			sqvpc.setJylsh(jylsh);
			sqvpc.setHphm(vehInfo.getHphm());
			sqvpc.setHpzl(vehInfo.getHpzl());
			sqvpc.setJyxm("sq");
		}
		Calendar cc = Calendar.getInstance();
		sqvpc.setKssj(cc.getTime());
		cc.add(13, 30);
		sqvpc.setJssj(cc.getTime());
		sqvpc.setJycs(vehInfo.getJycs());
		sqvpc.setVoideSate(0);
		this.sqProcessManager.save(sqvpc);

		return "OK";
	}

}
