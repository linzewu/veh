package com.xs.veh.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContext;

import com.xs.common.Constant;
import com.xs.common.ResultHandler;
import com.xs.veh.entity.User;
import com.xs.veh.manager.UserManager;
import com.xs.veh.util.PageInfo;

@Controller
@RequestMapping(value = "/user")
public class UserController {

	@Resource(name = "userManager")
	private UserManager userManager;

	@RequestMapping(value = "login", method = RequestMethod.POST)
	public @ResponseBody Map login(HttpServletRequest request, String userName, String password) {

		User user = userManager.login(userName, password);
		HttpSession session = request.getSession();
		RequestContext requestContext = new RequestContext(request);
		if (user != null) {
			session.setAttribute(Constant.ConstantKey.USER_SESSIO_NKEY, user);
			Map data=ResultHandler.toMyJSON(1, requestContext.getMessage(Constant.ConstantMessage.LOGIN_SUCCESS), user);
			data.put("session", session.getId());
			return data;
		} else {
			Map data=ResultHandler.toMyJSON(0, requestContext.getMessage(Constant.ConstantMessage.LOGIN_FAILED));
			data.put("session", session.getId());
			session.removeAttribute(Constant.ConstantKey.USER_SESSIO_NKEY);
			return data;
		}
	}
	
	@RequestMapping(value = "logout", method = RequestMethod.POST)
	public @ResponseBody Map logout(HttpSession session) {
		session.removeAttribute(Constant.ConstantKey.USER_SESSIO_NKEY);
		session.invalidate();
		return ResultHandler.toSuccessJSON("注销成功");
	}
	
	@RequestMapping(value = "getAllUsers", method = RequestMethod.POST)
	public @ResponseBody List getUsers() {
		return userManager.getUsers();
	}

	@RequestMapping(value = "getUsers", method = RequestMethod.POST)
	public @ResponseBody Map getUsers(User user, PageInfo pageInfo) {

		Map json = ResultHandler.toMyJSON(userManager.getUsers(user, pageInfo),
				userManager.getUserCount(user, pageInfo));
		return json;
	}

	@RequestMapping(value = "save", method = RequestMethod.POST)
	public @ResponseBody Map saveUser(@Valid User user, BindingResult result) {
		if (!result.hasErrors()) {
			User u = this.userManager.saveUser(user);
			return  ResultHandler.resultHandle(result,u ,Constant.ConstantMessage.SAVE_SUCCESS);
		}else{
			return ResultHandler.resultHandle(result,null ,null);
		}
	}

	@RequestMapping(value = "validateUserName")
	public @ResponseBody boolean validateUserName(User user) {
		User querUser = this.userManager.queryUserByUserName(user);
		if(querUser==null){
			return true;
		}else{
			return false;
		}

	}
	
	@RequestMapping(value = "validatePassworrd")
	public @ResponseBody boolean validatePassworrd(HttpSession session,String oldPassword) {
		User user = (User)session.getAttribute("user");
		User querUser = userManager.loadUser(user.getId());
		if(querUser.getPassword().equals(oldPassword)){
			return true;
		}else{
			return false;
		}
	}
	
	
	
	@RequestMapping(value = "resetPassword", method = RequestMethod.POST)
	public @ResponseBody void resetPassword(User user){
		this.userManager.resetPassword(user);
	}
	
	
	@RequestMapping(value = "delete", method = RequestMethod.POST)
	public @ResponseBody void delete(User user){
		this.userManager.deleteUser(user);
	}
	
	@RequestMapping(value = "getCurrentUser", method = RequestMethod.POST)
	public @ResponseBody User getCurrentUser(HttpSession session) {
		User user = (User)session.getAttribute("user");
		return user;
	}
	
	@RequestMapping(value = "updatePassword", method = RequestMethod.POST)
	public @ResponseBody Map updatePassword(HttpSession session,String newPassword) {
		User sessionUser = (User)session.getAttribute("user");
		User user = this.userManager.loadUser(sessionUser.getId());
		
		user.setPassword(newPassword);
		user.setUserState(User.USER_STATE_NORMAL);
		this.userManager.updateUser(user);
		session.invalidate();
		return ResultHandler.toSuccessJSON("密碼修改成功！");
	}


}
