package com.xs.veh.manager;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.xs.common.Message;
import com.xs.veh.entity.CheckQueue;
import com.xs.veh.entity.Flow;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.entity.WorkPoint;

@Service("vehLineCheckManager")
public class VehLineCheckManager {

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	@Resource(name = "checkQueueManager")
	private CheckQueueManager checkQueueManager;

	public Message upLine(String jylsh, Integer jycs) {
		Message message = new Message();
		List<VehCheckLogin> list = (List<VehCheckLogin>) hibernateTemplate
				.find("from VehCheckLogin where jylsj=? and jycs =?", jylsh, jycs);

		List<VehCheckProcess> process = (List<VehCheckProcess>) hibernateTemplate
				.find("from VehCheckProcess where jylsj=? and jycs =?", jylsh, jycs);
		

		if (list == null || list.isEmpty()) {
			message.setMessage("无法查询到流水号：" + jylsh + " 检验次数：" + jycs);
			message.setState(Message.STATE_ERROR);
			return message;
		} else if (process == null || list.isEmpty()) {
			message.setMessage("该流水号：" + jylsh + " 检验次数：" + jycs+" 没有存在检验项目");
			message.setState(Message.STATE_ERROR);
			return message;
		} else {
			VehCheckLogin vcl = list.get(0);
			
			List<Flow> flows = (List<Flow>) hibernateTemplate.find("from Flow where jcxdh=?", vcl.getJcxdh());
			
			if(flows==null||flows.isEmpty()){
				message.setMessage("无法获取检测线代号："+vcl.getJcxdh()+"的检测流程");
				message.setState(Message.STATE_ERROR);
				return message;
			}
			
			Flow flow = flows.get(0);
			
			List<WorkPoint> wps = null;
			
			if(wps==null||wps.isEmpty()){
				message.setMessage("无法获取检测工位，请联系管理员");
				message.setState(Message.STATE_ERROR);
				return message;
			}
			
			//获取第一工位数据
			WorkPoint wp = wps.get(0);
			
			CheckQueue cq=new CheckQueue();
			
			cq.setGwsx(wp.getSort());
			
			cq.setJcxdh(Integer.parseInt(vcl.getJcxdh()));
			
			cq.setHphm(vcl.getHphm());
			
			cq.setJycs(jycs);
			
			cq.setJylsh(jylsh);
			
			cq.setPdxh(checkQueueManager.getPdxh(vcl.getJcxdh(), wp.getSort()));
			
			this.hibernateTemplate.save(cq);
			
			message.setMessage("上线排队成功，请等待");
			
			message.setState(Message.STATE_SUCCESS);
			 
			return message;
		}
	}
	
	/*@Scheduled(fixedDelay = 200)
	private void vehUplineScheduled(){
		
		
	}*/

}
