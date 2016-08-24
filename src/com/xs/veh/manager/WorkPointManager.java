package com.xs.veh.manager;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.xs.common.Message;
import com.xs.veh.entity.CheckQueue;
import com.xs.veh.entity.Device;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.entity.WorkPoint;
import com.xs.veh.network.DeviceLight;
import com.xs.veh.network.ICheckDevice;
import com.xs.veh.network.WorkPointThread;

@Service("workPointManager")
public class WorkPointManager {

	Logger logger = Logger.getLogger(WorkPointManager.class);

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	@Resource(name = "taskExecutor")
	private ThreadPoolTaskExecutor executor;

	@Autowired
	private ServletContext servletContext;

	@Resource(name = "checkQueueManager")
	private CheckQueueManager checkQueueManager;

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;

	@Resource(name = "vehManager")
	private VehManager vehManager;

	public ServletContext getServletContext() {
		return servletContext;
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public List<WorkPoint> getWorkPoints() {
		return (List<WorkPoint>) this.hibernateTemplate.find("from WorkPoint order by jcxdh asc , sort asc");
	}

	public WorkPoint saveWorkPoint(WorkPoint workPoint) {
		return this.hibernateTemplate.merge(workPoint);
	}

	public void deleteWorkPoint(WorkPoint workPoint) {
		this.hibernateTemplate.delete(workPoint);
	}

	public List<WorkPoint> getWorkPointsByJcxdh(Integer jcxdh) {
		return (List<WorkPoint>) this.hibernateTemplate.find("from WorkPoint where jcxdh = ? order by sort asc", jcxdh);
	}

	public Message startWorkpoint(Integer id) {
		WorkPoint workPoint = this.hibernateTemplate.load(WorkPoint.class, id);
		Message message = new Message();

		WorkPointThread workPointThread = (WorkPointThread) servletContext.getAttribute(workPoint.getThreadKey());
		if (workPointThread == null) {
			WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(servletContext);
			workPointThread = wac.getBean(WorkPointThread.class);
			workPoint.setGwzt(WorkPoint.GWZT_QY);
			this.hibernateTemplate.update(workPoint);
			workPointThread.setWorkPoint(workPoint);
			servletContext.setAttribute(workPoint.getThreadKey(), workPointThread);
			executor.execute(workPointThread);
			message.setState(Message.STATE_SUCCESS);
			message.setMessage("启动成功");
		} else {
			message.setMessage("该工位已启动");
		}
		return message;
	}

	public Message stopWorkpoint(Integer id) {
		WorkPoint workPoint = this.hibernateTemplate.load(WorkPoint.class, id);
		Message message = new Message();
		workPoint.setGwzt(WorkPoint.GWZT_TY);
		this.hibernateTemplate.update(workPoint);
		WorkPointThread workPointThread = (WorkPointThread) this.servletContext.getAttribute(workPoint.getThreadKey());
		workPointThread.setWorkPoint(workPoint);
		this.servletContext.setAttribute(workPoint.getThreadKey(), null);

		message.setState(Message.STATE_SUCCESS);
		message.setMessage("工位停止成功");

		return message;

	}

	/**
	 * 灯光检测
	 * 
	 * @param deviceLight
	 * @param vehCheckLogin
	 * @param checkQueue
	 * @param lightVehFlow
	 */
	public void check(DeviceLight deviceLight, VehCheckLogin vehCheckLogin, CheckQueue checkQueue,
			List<VehFlow> lightVehFlow) {
		try {

			VehFlow vehFlow = lightVehFlow.get(lightVehFlow.size() - 1);
			deviceLight.startCheck(vehCheckLogin, lightVehFlow);
			checkAfter(vehCheckLogin, lightVehFlow);
		} catch (Exception e) {
			logger.error("检测过程异常", e);
		}
	}

	/**
	 * 检测结束后数据处理
	 * 
	 * @param vehCheckLogin
	 * @param checkQueue
	 * @param lightVehFlow
	 */
	private void checkAfter(VehCheckLogin vehCheckLogin, CheckQueue checkQueue, VehFlow vehFlow) {
		// 检测完成，删除队列
		this.hibernateTemplate.delete(checkQueue);
		// 创建一条新队列
		checkQueue = createNextQueue(vehFlow, vehCheckLogin);
		// 如果队列为空，则检测过程结束
		if (checkQueue == null) {
			checkDataManager.createOtherDataOfAnjian(vehCheckLogin.getJylsh());
		}
	}

	private void checkAfter(VehCheckLogin vehCheckLogin, List<VehFlow> vehFlows) {
		// 检测完成，删除队列

		Integer[] param = new Integer[vehFlows.size()];
		int i = 0;
		for (VehFlow vehFlow : vehFlows) {
			param[i] = vehFlow.getSx();
			i++;
		}

		DetachedCriteria dc = DetachedCriteria.forClass(CheckQueue.class);

		dc.add(Restrictions.eq("jylsh", vehCheckLogin.getJylsh()));
		dc.add(Restrictions.eq("jycs", vehCheckLogin.getJycs()));
		dc.add(Restrictions.in("lcsx", param));
		
		
		List<CheckQueue> checkQueues = (List<CheckQueue>) this.hibernateTemplate.findByCriteria(dc);

		for (CheckQueue checkQueue : checkQueues) {
			this.hibernateTemplate.delete(checkQueue);
		}

		// 创建一条新队列
		CheckQueue checkQueue = createNextQueue(vehFlows.get(vehFlows.size() - 1), vehCheckLogin);
		// 如果队列为空，则检测过程结束
		if (checkQueue == null) {
			checkDataManager.createOtherDataOfAnjian(vehCheckLogin.getJylsh());
		}
	}

	/**
	 * 
	 * @param checkDevice
	 * @param vehCheckLogin
	 * @param checkQueue
	 * @param vehFlow
	 */
	public void check(ICheckDevice checkDevice, VehCheckLogin vehCheckLogin, CheckQueue checkQueue, VehFlow vehFlow) {
		try {
			logger.info(vehFlow.getJyxm() + "项目开始检测");
			checkDevice.startCheck(vehCheckLogin, vehFlow);
			// 检测完成，删除队列
			logger.info("检测结束");
			checkAfter(vehCheckLogin, checkQueue, vehFlow);
		} catch (Exception e) {
			logger.error("检测过程异常", e);
		}
	}

	public void check(WorkPoint workPoint) {
		CheckQueue checkQueue = getQueue(workPoint);

		if (checkQueue != null) {
			setWorkPointState(workPoint, checkQueue);
			List<VehFlow> vehFlows = getVehFlow(checkQueue);

			// 灯光检测项目集合
			List<VehFlow> lightVehFlow = new ArrayList<VehFlow>();

			VehCheckLogin vehCheckLogin = getVehCheckLogin(checkQueue);

			int dgcount = getDGCount(vehCheckLogin.getJyxm());

			for (VehFlow vehFlow : vehFlows) {
				Device device = this.hibernateTemplate.load(Device.class, vehFlow.getSbid());
				ICheckDevice checkDevice = (ICheckDevice) servletContext.getAttribute(device.getThredKey());
				if (device.getType() == Device.DGJCSB) {
					lightVehFlow.add(vehFlow);
					if (lightVehFlow.size() == dgcount) {
						DeviceLight deviceLight = (DeviceLight) checkDevice;
						check(deviceLight, vehCheckLogin, checkQueue, lightVehFlow);
					}
				} else {
					// 普通单项检测
					check(checkDevice, vehCheckLogin, checkQueue, vehFlow);
				}
			}
		}
	}

	// public void check(WorkPoint workPoint) {
	//
	// if (workPoint.getGwzt() == WorkPoint.GWZT_TY) {
	// return;
	// } else {
	// CheckQueue checkQueue = getQueue(workPoint); // 获取工位排队队列
	// if (checkQueue != null) { // 设置工位状态 setWorkPointState(workPoint,
	// // checkQueue);
	//
	// List<VehFlow> vehFlows = getVehFlow(checkQueue);
	//
	// VehCheckLogin vehCheckLogin = getVehCheckLogin(checkQueue); // 灯光检测项目集合
	// List<VehFlow> lightVehFlow = new ArrayList<VehFlow>();
	//
	// int dgcount = getDGCount(vehCheckLogin.getJyxm());
	//
	// for (VehFlow vehFlow : vehFlows) {
	// Device device = this.hibernateTemplate.load(Device.class,
	// vehFlow.getSbid());
	// ICheckDevice checkDevice = (ICheckDevice)
	// servletContext.getAttribute(device.getThredKey()); // 灯光项目
	// // 特殊处理
	// if (device.getType() == Device.DGJCSB) {
	// lightVehFlow.add(vehFlow);
	// if (lightVehFlow.size() == dgcount) {
	// DeviceLight deviceLight = (DeviceLight) checkDevice;
	// try {
	// deviceLight.startCheck(vehCheckLogin, lightVehFlow);
	// // 检测完成，删除队列
	// this.hibernateTemplate.delete(checkQueue);
	// // 创建一条新队列
	// checkQueue = createNextQueue(vehFlow, vehCheckLogin);
	// // 如果队列为空，则检测过程结束
	// if (checkQueue == null) {
	// checkDataManager.createOtherDataOfAnjian(vehCheckLogin.getJylsh());
	// }
	// } catch (Exception e) {
	// logger.error("检测过程异常", e);
	// }
	// }
	// } else {
	// // 非灯光设备
	// try {
	// logger.info(vehFlow.getJyxm() + "项目开始检测");
	// checkDevice.startCheck(vehCheckLogin, vehFlow);
	// // 检测完成，删除队列
	// logger.info("检测结束");
	// this.hibernateTemplate.delete(checkQueue);
	// logger.info("删除队列完成");
	// // 创建一条新队列
	// logger.info("创建下一队列完成");
	// checkQueue = createNextQueue(vehFlow, vehCheckLogin);
	//
	// // 如果队列为空，则检测过程结束
	// if (checkQueue == null) {
	// checkDataManager.createOtherDataOfAnjian(vehCheckLogin.getJylsh());
	// }
	// } catch (Exception e) {
	// logger.error("检测过程异常 :", e);
	// }
	// }
	// }
	// } else {
	// // 设备工位为空闲状态
	// setWorkPointIsNotUse(workPoint);
	// }
	// }
	// }

	public int getDGCount(String jyxm) {

		int count = 0;
		int index = jyxm.indexOf("H");
		while (index != -1) {
			jyxm = jyxm.substring(index + 1, jyxm.length());
			index = jyxm.indexOf("H");
			count++;
		}
		return count;
	}

	@SuppressWarnings("unchecked")
	public VehCheckLogin getVehCheckLogin(CheckQueue checkQueue) {
		List<VehCheckLogin> list = (List<VehCheckLogin>) this.hibernateTemplate
				.find("from VehCheckLogin where jylsh=? and jycs = ?", checkQueue.getJylsh(), checkQueue.getJycs());
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	public void setWorkPointIsNotUse(WorkPoint workPoint) {

		if (workPoint.getIsUse() == WorkPoint.ISUSE_YES) {
			workPoint.setJylsh(null);
			workPoint.setJycs(null);
			workPoint.setHphm(null);
			workPoint.setHpzl(null);
			workPoint.setIsUse(WorkPoint.ISUSE_NO);
			this.hibernateTemplate.update(workPoint);
		}
	}

	public void setWorkPointState(WorkPoint workPoint, CheckQueue checkQueue) {
		workPoint.setJylsh(checkQueue.getJylsh());
		workPoint.setJycs(checkQueue.getJycs());
		workPoint.setHphm(checkQueue.getHphm());
		workPoint.setHpzl(checkQueue.getHpzl());
		workPoint.setIsUse(WorkPoint.ISUSE_YES);
		this.hibernateTemplate.update(workPoint);
	}

	public List<VehFlow> getVehFlow(CheckQueue checkQueue) {

		List<VehFlow> vehFlows = (List<VehFlow>) this.hibernateTemplate.find(
				"from VehFlow where jylsh=? and jycs=? and gwsx=? and sx>=? order by sx asc", checkQueue.getJylsh(),
				checkQueue.getJycs(), checkQueue.getGwsx(), checkQueue.getLcsx());

		List<VehFlow> temp = new ArrayList<VehFlow>();

		if (vehFlows.get(0).getJyxm().indexOf("H") == -1) {
			temp.add(vehFlows.get(0));
			return temp;
		} else {
			for (VehFlow vehFlow : vehFlows) {
				if (vehFlow.getJyxm().indexOf("H") == 0) {
					temp.add(vehFlow);
				}
			}
		}
		return temp;
	}

	public CheckQueue getQueue(final WorkPoint workPoint) {

		DetachedCriteria dc = DetachedCriteria.forClass(CheckQueue.class);
		dc.addOrder(Order.asc("pdxh"));
		dc.add(Restrictions.eq("jcxdh", workPoint.getJcxdh()));
		dc.add(Restrictions.eq("gwsx", workPoint.getSort()));
		List<CheckQueue> queues = (List<CheckQueue>) hibernateTemplate.findByCriteria(dc, 0, 1);

		if (queues == null || queues.isEmpty()) {
			return null;
		} else {

			// CheckQueue firstQueues = queues.get(0);
			//
			// DetachedCriteria dc1 =
			// DetachedCriteria.forClass(CheckQueue.class);
			// dc1.addOrder(Order.asc("pdxh"));
			// dc1.add(Restrictions.eq("jcxdh", workPoint.getJcxdh()));
			// dc1.add(Restrictions.eq("gwsx", workPoint.getSort()));
			// dc1.add(Restrictions.eq("jylsh", firstQueues.getJylsh()));
			// dc1.add(Restrictions.eq("jycs", firstQueues.getJycs()));
			//
			// List<CheckQueue> gwQueues = (List<CheckQueue>)
			// hibernateTemplate.findByCriteria(dc1);

			return queues.get(0);
		}

	}

	public CheckQueue createNextQueue(VehFlow vehFlow, VehCheckLogin vehCheckLogin) {

		List<VehFlow> vehFlows = (List<VehFlow>) this.hibernateTemplate.find(
				"from VehFlow where jylsh=? and jycs=? and sx>? order by sx asc", vehFlow.getJylsh(), vehFlow.getJycs(),
				vehFlow.getSx());

		List<CheckQueue> queues = new ArrayList<CheckQueue>();

		if (vehFlows != null && !vehFlows.isEmpty()) {
			VehFlow firstFlow = vehFlows.get(0);

			// 查询是否存在队列
			List<CheckQueue> qs = (List<CheckQueue>) this.hibernateTemplate.find(
					"from CheckQueue where jylsh=? and jycs=? and lcsx=?  order by pdxh asc", firstFlow.getJylsh(),
					firstFlow.getJycs(), firstFlow.getSx());

			// 如果存在直接返回
			if (qs != null && qs.size() > 0) {
				return qs.get(0);
			} else {
				// 获取同一工位的流程
				List<VehFlow> nextFlows = (List<VehFlow>) this.hibernateTemplate.find(
						"from VehFlow where jylsh=? and jycs=? and gw=? order by sx asc", vehCheckLogin.getJylsh(),
						vehCheckLogin.getJycs(), firstFlow.getGw());
				for (VehFlow newFlow : nextFlows) {
					CheckQueue queue = new CheckQueue();
					queue.setGwsx(newFlow.getGwsx());
					queue.setHphm(newFlow.getHphm());
					queue.setHpzl(newFlow.getHpzl());
					queue.setJcxdh(Integer.parseInt(vehCheckLogin.getJcxdh()));
					queue.setJycs(newFlow.getJycs());
					queue.setJylsh(newFlow.getJylsh());
					queue.setLcsx(newFlow.getSx());
					queue.setPdxh(checkQueueManager.getPdxh(vehCheckLogin.getJcxdh(), newFlow.getGwsx()));
					this.hibernateTemplate.save(queue);
					queues.add(queue);
				}
				return queues.get(0);
			}

		}

		return null;
	}

	public void startAllWorkPonit() {

		List<WorkPoint> workPoints = (List<WorkPoint>) this.hibernateTemplate.find("from WorkPoint where gwzt=?",
				WorkPoint.GWZT_QY);

		logger.info("启动工位：" + workPoints.size());

		for (WorkPoint workPoint : workPoints) {
			this.startWorkpoint(workPoint.getId());
		}
	}

	public VehFlow getNextFlow(VehFlow vehFlow) {

		List<VehFlow> vehFlows = (List<VehFlow>) this.hibernateTemplate.find(
				"from VehFlow where jylsh=? and jycs=? and sx=?", vehFlow.getJylsh(), vehFlow.getJycs(),
				vehFlow.getSx() + 1);

		if (vehFlows == null || vehFlows.isEmpty()) {
			return null;
		} else {
			return vehFlows.get(0);
		}

	}

}
