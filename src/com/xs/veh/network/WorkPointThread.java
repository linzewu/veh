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
				logger.error(e.getMessage());
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					logger.error(e1);
				}
			} catch (SystemException e) {
				logger.error(e.getMessage());
			}catch(Exception e){
				logger.error(e);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					logger.error(e1);
				}
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
							workPointManager.check(checkDevice, vehCheckLogin, checkQueue, cc,null);

						}
					} else if (device.getType() == Device.ZDPBSB) {
						// 平板检测
						cc.add(vehFlow);
						if (cc.size() == vehFlows.size()) {
							logger.info("开始检测平板");
							/*if(cc.size()==1&&cc.get(0).getJyxm().equals("B0")){
								workPointManager.check(checkDevice, vehCheckLogin, checkQueue, cc,null);
							}else{*/
							Map<String,Object> param=new HashMap<String,Object>();
							Integer zclh = checkDataManager.getZCZH(vehCheckLogin);
							param.put("zclh", zclh);
							workPointManager.check(checkDevice, vehCheckLogin, checkQueue, cc,param);
						/*	}*/
							
						}
					} else {
						// 普通单项检测
						Map<String,Object> param=new HashMap<String,Object>();
						if(device.getType()==Device.ZDJCSB){
							BrakRollerData brakRollerData;
							/*if(vehCheckLogin.getJycs()==1) {*/
								brakRollerData = checkDataManager.getBrakRollerDataOfVehLoginInfo(vehCheckLogin,vehFlow.getJyxm());
							/*}else {
								BrakRollerData weightBrakRollerData = checkDataManager.getLastBrakRollerDataOfVehLoginInfo(vehCheckLogin,vehFlow.getJyxm());
								brakRollerData =new BrakRollerData();
								brakRollerData.setZlh(weightBrakRollerData.getZlh());
								brakRollerData.setYlh(weightBrakRollerData.getYlh());
								brakRollerData.setJzzlh(weightBrakRollerData.getJzzlh());
								brakRollerData.setJzylh(weightBrakRollerData.getJzylh());
							}*/
							
							param.put("brakRollerData", brakRollerData);
							
							if(vehFlow.getJyxm().equals("B0")){
								List<BrakRollerData> brakRollerB0Datas = checkDataManager.getBrakRollerDataB0(vehCheckLogin);
								param.put("B0", brakRollerB0Datas);
								Integer zclh = checkDataManager.getZCZH(vehCheckLogin);
								param.put("zclh", zclh);
							}
						}
						workPointManager.check(checkDevice, vehCheckLogin, checkQueue, vehFlow,param);
					}
				}
			}
		}
	}
	
}
