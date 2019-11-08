package com.xs.veh.job;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.websocket.Session;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.xs.veh.entity.BlackList;
import com.xs.veh.entity.SecurityAuditPolicySetting;
import com.xs.veh.manager.BlackListManager;
import com.xs.veh.manager.SecurityAuditPolicySettingManager;
import com.xs.veh.websocket.MyWebSocket;

@Component("blackListJob")
public class BlackListJob {
	
	@Resource(name = "blackListManager")
	private BlackListManager blackListManager;
	
	@Resource(name = "securityAuditPolicySettingManager")
	private SecurityAuditPolicySettingManager securityAuditPolicySettingManager;
	
	
	/**
	 * 每天0点删除所有黑名单
	 * @throws Exception
	 */
	@Scheduled(cron="0 0 0 * * ? ")
	private void deleteBlackList() throws Exception{
		blackListManager.deleteSystemBlackList();
	}
	
	@Scheduled(cron="0 0/5 * * * ? ")
	private void unlockedBlackList() throws Exception{
		SecurityAuditPolicySetting set = securityAuditPolicySettingManager.getPolicyByCode(SecurityAuditPolicySetting.IP_LOCK);
		List<BlackList> list = blackListManager.getEnableList();
		int clz = set.getClz() == null?0:Integer.parseInt(set.getClz());
		for(BlackList black:list) {
			int hours = (int) (((new Date()).getTime() - black.getLastUpdateTime().getTime())/(1000 * 60 * 60));
			if(black.getFailCount() >= clz && hours>=1) {
				black.setEnableFlag("N");
				black.setFailCount(0);
				black.setLastUpdateTime(new Date());
				blackListManager.saveBlackList(black);
			}
		}
	}
	
//	@Scheduled(fixedDelay = 5000)
//	private void vehOutlineReport() throws IOException {
//		if(!MyWebSocket.sessionMap.isEmpty()) {
//			
//			for(String key : MyWebSocket.sessionMap.keySet()) {
//				
//				Session session = MyWebSocket.sessionMap.get(key);
//				session.getBasicRemote().sendText("你好！"+key);
//			}
//			
//		}
//	}

	

}
