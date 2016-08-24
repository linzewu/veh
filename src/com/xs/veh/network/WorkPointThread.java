package com.xs.veh.network;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Component;

import com.xs.veh.entity.WorkPoint;
import com.xs.veh.manager.WorkPointManager;

@Component("workPointThread")
@Scope("prototype")
public class WorkPointThread implements Runnable {

	Logger logger = Logger.getLogger(WorkPointThread.class);

	private WorkPoint workPoint;

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	@Resource(name = "workPointManager")
	private WorkPointManager workPointManager;


	public WorkPoint getWorkPoint() {
		return workPoint;
	}

	public void setWorkPoint(WorkPoint workPoint) {
		this.workPoint = workPoint;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			workPointManager.check(workPoint);
		}

	}
}
