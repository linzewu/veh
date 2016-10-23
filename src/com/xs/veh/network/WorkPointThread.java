package com.xs.veh.network;

import java.io.IOException;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.xs.veh.entity.WorkPoint;
import com.xs.veh.manager.WorkPointManager;

@Component("workPointThread")
@Scope("prototype")
public class WorkPointThread extends Thread {
	
	private boolean active;
	
	

	Logger logger = Logger.getLogger(WorkPointThread.class);

	private WorkPoint workPoint;

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	@Resource(name = "workPointManager")
	private WorkPointManager workPointManager;
	
	

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
				workPointManager.check(workPoint);
				logger.debug(workPoint.getJcxdh()+"号线  "+workPoint.getSort()+"工位");
			} catch (InterruptedException  e) {
				active=false;
				logger.info(workPoint.getJcxdh()+"号线"+workPoint.getSort()+"工位 停止");
				return;
			}catch(IOException e){
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
