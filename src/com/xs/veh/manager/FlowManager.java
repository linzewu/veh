package com.xs.veh.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.common.Message;
import com.xs.veh.entity.Flow;
import com.xs.veh.entity.WorkPoint;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service("flowManager")
public class FlowManager {

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	public List<Flow> getFlows(){
		List<Flow> flows =  (List<Flow>) hibernateTemplate.find("from Flow");
		return flows;
	}
	
	public Serializable save(Flow flow){
		
		return this.hibernateTemplate.save(flow);
	}
	
	public void delete(Flow flow){
		this.hibernateTemplate.delete(flow);
	}
	
	public Message update(Flow flow){
		Message message =new Message();
		this.hibernateTemplate.update(flow);
		message.setState(Message.STATE_SUCCESS);
		message.setMessage("更新成功");
		return message;
	}
	
	public Flow getFlow(Integer jcxdh,Integer jclclx){
		
		List list = this.hibernateTemplate.find("from Flow where jcxdh=? and jclclx=?", jcxdh,jclclx);
		
		if(list!=null&&!list.isEmpty()){
			return (Flow) list.get(0);
		}else{
			return null;
		}
		
	}

}
