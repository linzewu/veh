package com.xs.veh.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.xs.common.exception.SystemException;
import com.xs.veh.entity.CheckQueue;
import com.xs.veh.entity.Device;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.entity.WorkPoint;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.manager.DeviceManager;
import com.xs.veh.manager.WorkPointManager;
import com.xs.veh.network.data.BrakRollerData;

@Component("workPointThread")
@Scope("prototype")
public class WorkPointThread extends Thread {
	
	private boolean active;
	
	@Autowired
	private ServletContext servletContext;

	Logger logger = Logger.getLogger(WorkPointThread.class);

	private WorkPoint workPoint;

	@Resource(name = "workPointManager")
	private WorkPointManager workPointManager;
	
	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;
	
	@Resource(name = "deviceManager")
	private DeviceManager deviceManager;

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public WorkPoint getWorkPoint() {
		return workPoint;
	}

	public void setWorkPoint(WorkPoint workPoint) {
		this.workPoint = workPoint;
	}

	@Override
	public void run() {
		logger.info(workPoint.getJcxdh()+"号线"+workPoint.getSort()+"工位 启动");
		while (true) {
			try {
				active=true;
				Thread.sleep(1000);
				check(workPoint);
				logger.debug(workPoint.getJcxdh()+"号线  "+workPoint.getSort()+"工位");
			} catch (InterruptedException  e) {
				active=false;
				logger.info(workPoint.getJcxdh()+"号线"+workPoint.getSort()+"工位 停止");
				return;
			}catch(IOException e){
				e.printStackTrace();
			} catch (SystemException e) {
				logger.info(e.getMessage());
			} 
		}
	}
	
	/**
	 * 检测
	 * 
	 * @param workPoint
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws SystemException 
	 * @throws Exception
	 */
	public void check(WorkPoint workPoint) throws IOException, InterruptedException, SystemException {
		CheckQueue checkQueue = workPointManager.getQueue(workPoint);

		if (checkQueue != null) {
			//setWorkPointState(workPoint, checkQueue);
			List<VehFlow> vehFlows = workPointManager.getVehFlow(checkQueue);
			// 灯光检测项目集合
			List<VehFlow> cc = new ArrayList<VehFlow>();

			VehCheckLogin vehCheckLogin = workPointManager.getVehCheckLogin(checkQueue);

			int dgcount = workPointManager.getDGCount(vehCheckLogin.getJyxm());

			logger.info("vehFlows:" + vehFlows.size());

			for (VehFlow vehFlow : vehFlows) {

				if (vehFlow.getSbid() == -1) {
					// 底盘检测
					workPointManager.checkDP(vehCheckLogin, checkQueue, vehFlow);
				} else {
					
					Device device = this.deviceManager.getDevice(vehFlow.getSbid());
					
					Hibernate.initialize(device);
					
					ICheckDevice checkDevice = (ICheckDevice) servletContext.getAttribute(device.getThredKey());

					if (device.getType() == Device.DGJCSB) {
						cc.add(vehFlow);
						if (cc.size() == dgcount) {
							workPointManager.check(checkDevice, vehCheckLogin, checkQueue, cc);

						}
					} else if (device.getType() == Device.ZDPBSB) {
						// 平板检测
						cc.add(vehFlow);
						if (cc.size() == vehFlows.size()) {
							logger.info("开始检测平板");
							workPointManager.check(checkDevice, vehCheckLogin, checkQueue, cc);
						}
					} else {
						// 普通单项检测
						Map<String,Object> param=new HashMap<String,Object>();
						if(device.getType()==Device.ZDJCSB){
							BrakRollerData brakRollerData = checkDataManager.getBrakRollerDataOfVehLoginInfo(vehCheckLogin,vehFlow.getJyxm());
							logger.info("brakRollerData:"+brakRollerData);
							param.put("brakRollerData", brakRollerData);
						}
						workPointManager.check(checkDevice, vehCheckLogin, checkQueue, vehFlow,param);
					}
				}
			}
		}
	}
	
}
