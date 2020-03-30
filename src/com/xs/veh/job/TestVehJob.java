package com.xs.veh.job;

import java.io.FileInputStream;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.xs.common.BaseParamsUtil;
import com.xs.veh.entity.BaseParams;
import com.xs.veh.entity.CheckPhoto;
import com.xs.veh.entity.TaskPicture;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.util.HKVisionUtil;

@Component("TestVehJob")
public class TestVehJob {

	@Autowired
	private HibernateTemplate hibernateTemplate;

	@Autowired
	private CheckDataManager checkDataManager;

	@Scheduled(fixedDelay = 1000)
	public void scanTaskPic() throws Exception {

		List<TaskPicture> tps = (List<TaskPicture>) hibernateTemplate.find("from TaskPicture where status='0'");
		List<BaseParams> params = BaseParamsUtil.getBaseParamsByType("sxtpz");

		for (TaskPicture t : tps) {

			BaseParams param = null;
			for (BaseParams bp : params) {
				String[] paramName = bp.getParamName().split("_");
				String pzjyxm = paramName[0];
				String jcxdh = "1";
				if (paramName.length > 1) {
					jcxdh = paramName[1];
				}
				if("EP".equals(pzjyxm)) {
					param = bp;
				}else if("S1".equals(pzjyxm)) {
					param = bp;
				}
				
			}

			if (param != null) {
				VehCheckLogin vehCheckLogin = checkDataManager.getVehCheckLogin(t.getJylsh());

				String value = param.getParamValue();

				JSONObject jo = JSONObject.parseObject(value);

				String sxtip = (String) jo.get("sxtip");
				String sxtdk = (String) jo.get("sxtdk");
				String sxtzh = (String) jo.get("sxtzh");
				String sxtmm = (String) jo.get("sxtmm");

				HKVisionUtil hk = new HKVisionUtil();
				FileInputStream fis = null;

				try {
					String file = hk.taskPicture(sxtzh, sxtmm, sxtip, Integer.parseInt(sxtdk),
							vehCheckLogin.getJylsh() + "_" + vehCheckLogin.getJycs() + "_" + t.getJyxm());
					fis = new FileInputStream(file);
					byte[] zp = new byte[fis.available()];
					fis.read(zp);
					CheckPhoto checkPhoto = new CheckPhoto();
					checkPhoto.setJcxdh(vehCheckLogin.getJcxdh());
					checkPhoto.setClsbdh(vehCheckLogin.getClsbdh());
					checkPhoto.setHphm(vehCheckLogin.getHphm());
					checkPhoto.setHpzl(vehCheckLogin.getHpzl());
					checkPhoto.setJycs(vehCheckLogin.getJycs());
					checkPhoto.setJyjgbh(vehCheckLogin.getJyjgbh());
					checkPhoto.setJylsh(vehCheckLogin.getJylsh());
					checkPhoto.setJyxm(t.getJyxm());
					checkPhoto.setPssj(new Date());
					checkPhoto.setStatus(0);
					checkPhoto.setZp(zp);
					
					if("EP".equals(t.getJyxm())) {
						checkPhoto.setZpzl("0999");
					}else if("S1".equals(t.getJyxm())) {
						checkPhoto.setZpzl("0347");
					}

					this.checkDataManager.saveCheckPhoto(checkPhoto);
					
					this.checkDataManager.deleteTaskPaice(t);
					continue;
					
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			t.setStatus("1");
			this.checkDataManager.updateTaskPaice(t);
		}
	}
}