package com.xs.veh.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContext;

import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.common.Constant;
import com.xs.common.ResultHandler;
import com.xs.enums.CommonUserOperationEnum;
import com.xs.veh.entity.BlackList;
import com.xs.veh.entity.Role;
import com.xs.veh.entity.SecurityAuditPolicySetting;
import com.xs.veh.entity.SecurityLog;
import com.xs.veh.entity.User;
import com.xs.veh.manager.BlackListManager;
import com.xs.veh.manager.OperationLogManager;
import com.xs.veh.manager.RoleManager;
import com.xs.veh.manager.SecurityAuditPolicySettingManager;
import com.xs.veh.manager.SecurityLogManager;
import com.xs.veh.manager.UserManager;
import com.xs.veh.util.PageInfo;

@Controller
@RequestMapping(value = "/user",produces="application/json")
@Modular(modelCode="user",modelName="用户管理")
public class UserController {
	
	@Autowired
	private HttpServletRequest request;
	
	@Resource(name = "userManager")
	private UserManager userManager;
	
	@Resource(name = "operationLogManager")
	private OperationLogManager operationLogManager;
	
	@Resource(name = "roleManager")
	private RoleManager roleManager;
	
	@Autowired
	private BlackListManager blackListManager;
	@Autowired
	private SecurityLogManager securityLogManager;
	
	@Resource(name = "securityAuditPolicySettingManager")
	private SecurityAuditPolicySettingManager securityAuditPolicySettingManager;
	
	
	private  String getIpAdrress() {
        String Xip = request.getHeader("X-Real-IP");
        String XFor = request.getHeader("X-Forwarded-For");
        if(StringUtils.isNotEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)){
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = XFor.indexOf(",");
            if(index != -1){
                return XFor.substring(0,index);
            }else{
                return XFor;
            }
        }
        XFor = Xip;
        if(StringUtils.isNotEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)){
            return XFor;
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getRemoteAddr();
        }
        return XFor;
    }
	
	/**
	 * 校验ip是否是用户设置的允许登录ip
	 * @param user
	 * @return
	 */
	private boolean checkIp(User user) {
		String ips =user.getLoginIP();
		String removeIp=getIpAdrress();
		if(!StringUtils.isEmpty(ips)) {
			
			for(String ip :ips.split(",")) {
				if(ip.equals(removeIp)) {
					return true;
				}
			}
			
		}else {
			return true;
		}
		
		return false;
	}

	@RequestMapping(value = "login", method = RequestMethod.POST)
	@UserOperation(code="login",name="登录",userOperationEnum=CommonUserOperationEnum.NoLogin)
	public @ResponseBody Map login(HttpServletRequest request, String userName, String password) {
		
		RequestContext requestContext = new RequestContext(request);
		HttpSession session = request.getSession();
		
		
		if(blackListManager.checkIpIsBan(getIpAdrress())) {
			Map data=ResultHandler.toMyJSON(0, requestContext.getMessage(Constant.ConstantMessage.LOGIN_FAILED));
			data.put("session", session.getId());
			data.put("errorMsg", "当前IP已被列入黑名单，请联系管理员！");
			return data;
		}
		
		
		User user = userManager.login(userName);
		
		if (user != null) {
			//校验用户是否被锁定
			if(user.getUserState() == 1) {
				SecurityAuditPolicySetting set = securityAuditPolicySettingManager.getPolicyByCode(SecurityAuditPolicySetting.ACCOUNT_LOCK);
				Map data=ResultHandler.toMyJSON(0, requestContext.getMessage(Constant.ConstantMessage.LOGIN_FAILED));
				data.put("session", session.getId());
				data.put("errorMsg", "登录失败，当前用户登录失败次数超过"+set.getClz()+"次已被锁定，请联系管理员解锁！");
				return data;
			}
			
			if(!checkIp(user)) {
				Map data=ResultHandler.toMyJSON(0, requestContext.getMessage(Constant.ConstantMessage.LOGIN_FAILED));
				data.put("session", session.getId());
				data.put("errorMsg", "登录IP地址不合法！");
				return data;
			}
			Date nowDate = new Date();
			
			if(nowDate.after(user.getUserNameValidDate())) {
				Map data=ResultHandler.toMyJSON(0, requestContext.getMessage(Constant.ConstantMessage.LOGIN_FAILED));
				data.put("session", session.getId());
				data.put("errorMsg", "用户账号已过期！");
				user.setPwValidDate(user.getUserNameValidDate());
				this.userManager.updateUser(user);
				return data;
			}
			//校验时间是否在允许的时间段内
			if(!checkLognTime(user)) {
				Map data=ResultHandler.toMyJSON(0, requestContext.getMessage(Constant.ConstantMessage.LOGIN_FAILED));
				data.put("session", session.getId());
				data.put("errorMsg", "不在允许的时间段内登录！");
				return data;
			}
			String encodePwd =user.encodePwd(password);
			if(!user.getPassword().equals(encodePwd)) {
				Map data=ResultHandler.toMyJSON(0, requestContext.getMessage(Constant.ConstantMessage.LOGIN_FAILED));
				
				data.put("session", session.getId());
				session.removeAttribute(Constant.ConstantKey.USER_SESSIO_NKEY);
				failLoginCount(user);
				//失败加入黑名单
				int sycou= addBlackList();
				data.put("errorMsg", "用户名密码错误！当前Ip还有"+sycou+"次机会就会被锁定.");
				return data;
			}
			
			
				//判断密码是否过期
			if (user.getPwValidDate().before(nowDate)) {
				user.setPwOverdue("Y");
			}
			//上次登录的时间跟ip记录
			user.setLastTimeLoginDate(user.getLastLoginDate());
			user.setLastTimeIP(user.getIp());
			//修改最后登录时间
			user.setLastLoginDate(new Date());
			user.setIp(getIpAdrress());
			user.setLoginFailCou(0);
			this.userManager.updateUser(user);
			session.setAttribute(Constant.ConstantKey.USER_SESSIO_NKEY, user);
			Map data=ResultHandler.toMyJSON(1, requestContext.getMessage(Constant.ConstantMessage.LOGIN_SUCCESS), user);
			data.put("session", session.getId());
			return data;
		} else {
			Map data=ResultHandler.toMyJSON(0, requestContext.getMessage(Constant.ConstantMessage.LOGIN_FAILED));
			
			data.put("session", session.getId());
			session.removeAttribute(Constant.ConstantKey.USER_SESSIO_NKEY);
			//失败加入黑名单
			int sycou= addBlackList();
			data.put("errorMsg", "用户名密码错误！当前Ip还有"+sycou+"次机会就会被锁定.");
			return data;
		}
	}
	
	private User failLoginCount(User u) {
		int cou = u.getLoginFailCou() == null? 0:u.getLoginFailCou();
		cou++;		
		u.setLoginFailCou(cou);
		if(cou == 10) {
			//锁定
			u.setUserState(1);
			u.setLoginFailCou(0);
		}
		
		this.userManager.updateUser(u);
		if (u.getUserState() == 1) {
			//写入安全日志
			SecurityLog securityLog = new SecurityLog();
			securityLog.setCreateUser(User.SYSTEM_USER);
			securityLog.setUpdateUser(User.SYSTEM_USER);
			securityLog.setUserName(u.getUserName());
			securityLog.setClbm(SecurityAuditPolicySetting.ACCOUNT_LOCK);
			securityLog.setIpAddr(getIpAdrress());
			securityLog.setContent("用户:"+u.getUserName()+"违反账户锁定安全审计策略设置，用户锁定");
			securityLogManager.saveSecurityLog(securityLog);
		}
		return u;
	}
	
	private int addBlackList() {
		int sycou = 0;
		String ip = getIpAdrress();
		SecurityAuditPolicySetting set = securityAuditPolicySettingManager.getPolicyByCode(SecurityAuditPolicySetting.IP_LOCK);
		int clz = set.getClz() == null?0:Integer.parseInt(set.getClz());
		BlackList black = blackListManager.getBlackListByIp(ip);
		if (black == null) {
			BlackList newBlack = new BlackList();
			newBlack.setCreateBy(User.SYSTEM_USER);
			newBlack.setEnableFlag("N");
			newBlack.setFailCount(1);
			newBlack.setIp(ip);
			newBlack.setLastUpdateTime(new Date());
			blackListManager.saveBlackList(newBlack);
			sycou = clz-newBlack.getFailCount();
		}else {
			black.setFailCount(black.getFailCount()+1);
			black.setLastUpdateTime(new Date());
			if(black.getFailCount()>=clz) {
				black.setEnableFlag("Y");
			}
			blackListManager.saveBlackList(black);
			sycou = clz-black.getFailCount();
			if("Y".equals(black.getEnableFlag())) {
				//写入安全日志
				SecurityLog securityLog = new SecurityLog();
				securityLog.setCreateUser(User.SYSTEM_USER);
				securityLog.setUpdateUser(User.SYSTEM_USER);
				securityLog.setClbm(SecurityAuditPolicySetting.IP_LOCK);
				securityLog.setIpAddr(ip);
				securityLog.setContent("IP终端:"+ip+"违反IP终端锁定(黑名单)安全审计策略设置，加入黑名单");
				securityLogManager.saveSecurityLog(securityLog);
			}
		}
		return sycou;
	}
	
	
	/**
	 * 校验时间是否在允许的时间段内
	 * @param user
	 * @return
	 */
	private boolean checkLognTime(User user) {
		String beginTimeStr = user.getPermitBeginTime();
		String endTimeStr = user.getPermitEndTime();
		
		beginTimeStr=beginTimeStr==null?"00:00":beginTimeStr;
		endTimeStr=endTimeStr==null?"24:00":endTimeStr;
		
		String[] arrBegin=beginTimeStr.split(":");
		String[] arrEnd=endTimeStr.split(":");
		
		int  beginTime= Integer.parseInt(arrBegin[0])*60+Integer.parseInt(arrBegin[1]);
		int  endTime= Integer.parseInt(arrEnd[0])*60+Integer.parseInt(arrEnd[1]);
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int countMinute=hour*60+minute;
	
		if(countMinute<beginTime||beginTime>endTime) {
			return false;
		}else {
			return true;
		}
		
	}


	@RequestMapping(value = "logout", method = RequestMethod.POST)
	@UserOperation(code="logout",name="登出",userOperationEnum=CommonUserOperationEnum.AllLoginUser)
	public @ResponseBody Map logout(HttpSession session) {
		//session.removeAttribute(Constant.ConstantKey.USER_SESSIO_NKEY);
		session.invalidate();
		return ResultHandler.toSuccessJSON("注销成功");
	}
	
	@UserOperation(code="getUsers",name="获取用户列表")
	@RequestMapping(value = "getAllUsers", method = RequestMethod.POST)
	public @ResponseBody List getUsers() {
		return userManager.getUsers();
	}

	@UserOperation(code="getUsers",name="获取用户列表")
	@RequestMapping(value = "getUsers", method = RequestMethod.POST)
	public @ResponseBody Map getUsers(User user, PageInfo pageInfo) {

		Map json = ResultHandler.toMyJSON(userManager.getUsers(user, pageInfo),
				userManager.getUserCount(user, pageInfo));
		return json;
	}

	@UserOperation(code="save",name="编辑用户")
	@RequestMapping(value = "save", method = RequestMethod.POST)
	public @ResponseBody Map saveUser(@Valid User user, BindingResult result) {
		System.out.println(user.getId()+" "+user.getIdCard()+" ");
		if (!result.hasErrors()) {
			User u = this.userManager.saveUser(user);
			return  ResultHandler.resultHandle(result,u ,Constant.ConstantMessage.SAVE_SUCCESS);
		}else{
			return ResultHandler.resultHandle(result,null ,null);
		}
	}

	@UserOperation(code="save",name="校验用户名",isMain=false)
	@RequestMapping(value = "validateUserName")
	public @ResponseBody boolean validateUserName(User user) {
		User querUser = this.userManager.queryUserByUserName(user);
		if(querUser==null){
			return true;
		}else{
			return false;
		}

	}
	
	@UserOperation(code="updatePassword",name="校验密码",userOperationEnum=CommonUserOperationEnum.AllLoginUser,isMain=false)
	@RequestMapping(value = "validatePassworrd")
	public @ResponseBody boolean validatePassworrd(HttpSession session,String oldPassword) {
		User user = (User)session.getAttribute("user");
		User querUser = userManager.loadUser(user.getId());
		if(querUser.getPassword().equals(user.encodePwd(oldPassword))){
			return true;
		}else{
			return false;
		}
	}
	
	@UserOperation(code="save",name="保存用户")
	@RequestMapping(value = "resetPassword", method = RequestMethod.POST)
	public @ResponseBody void resetPassword(User user){
		this.userManager.resetPassword(user);
	}
	
	@UserOperation(code="delete",name="删除用户")
	@RequestMapping(value = "delete", method = RequestMethod.POST)
	public @ResponseBody void delete(User user){
		this.userManager.deleteUser(user);
	}
	
	@UserOperation(code="getCurrentUser",name="获取当前用户",userOperationEnum=CommonUserOperationEnum.AllLoginUser)
	@RequestMapping(value = "getCurrentUser", method = RequestMethod.POST)
	public @ResponseBody User getCurrentUser(HttpSession session) {
		User user = (User)session.getAttribute("user");
		return user;
	}
	
	@UserOperation(code="updatePassword",name="修改用户密码",userOperationEnum=CommonUserOperationEnum.AllLoginUser)
	@RequestMapping(value = "updatePassword", method = RequestMethod.POST)
	public @ResponseBody Map updatePassword(HttpSession session,String newPassword) {
		User sessionUser = (User)session.getAttribute("user");
		User user = this.userManager.loadUser(sessionUser.getId());
		
		user.setPassword(user.encodePwd(newPassword));
		user.setUserState(0);
		//修改密码，密码有效期延长3个月
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(user.getPwValidDate());
		calendar.add(Calendar.MONTH, 3);
		user.setPwValidDate(calendar.getTime());
		this.userManager.updateUser(user);
		session.invalidate();
		return ResultHandler.toSuccessJSON("密碼修改成功！");
	}
	
	@UserOperation(code="getAllRole",name="获取所有角色",userOperationEnum=CommonUserOperationEnum.AllLoginUser)
	@RequestMapping(value = "getAllRole", method = RequestMethod.POST)
	public @ResponseBody List<Role> getAllRole() {
		return roleManager.getAllRole();
	}
	
	
	@UserOperation(code="getAllRoleNoAdmin",name="获取所有角色不包含超级管理员",userOperationEnum=CommonUserOperationEnum.AllLoginUser)
	@RequestMapping(value = "getAllRoleNoAdmin", method = RequestMethod.POST)
	public @ResponseBody List<Role> getAllRoleNoAdmin() {
		return roleManager.getAllRoleNoAdmin();
	}
	
	@UserOperation(code="getRolesByUser",name="获取当前用户角色",userOperationEnum=CommonUserOperationEnum.AllLoginUser)
	@RequestMapping(value = "getRolesByUser", method = RequestMethod.POST)
	public @ResponseBody Role getRolesByUser(HttpSession session) {
		User user = (User)session.getAttribute("user");
		return roleManager.queryRoleById(user.getRoleId());
	}
	
	@UserOperation(code="save",name="校验身份证",isMain=false)
	@RequestMapping(value = "validateIdCard")
	public @ResponseBody boolean validateIdCard(User user) {
		User querUser = this.userManager.queryUser(user);
		if(querUser==null){
			return true;
		}else{
			return false;
		}

	}


}
