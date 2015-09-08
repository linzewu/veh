package com.xs.veh.controller;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContext;

import com.xs.common.Constant;
import com.xs.common.MyJSONUtil;
import com.xs.veh.entity.User;
import com.xs.veh.manager.UserManager;
import com.xs.veh.util.PageInfo;

@Controller
@RequestMapping(value="/user")
public class UserController {

	@Resource(name = "userManager")
	private UserManager userManager;
 
	@RequestMapping(value="login",method=RequestMethod.POST)
	public @ResponseBody Map login(HttpServletRequest request, String userName,
			String password) {

		User user = userManager.login(userName, password);
		HttpSession session = request.getSession();
		RequestContext requestContext = new RequestContext(request);
		if (user != null) {
			session.setAttribute(Constant.USER_SESSIO_NKEY, user);
			return MyJSONUtil.toMyJSON(1,
					requestContext.getMessage(Constant.LOGIN_SUCCESS),user);
		} else {
			return MyJSONUtil.toMyJSON(0,
					requestContext.getMessage(Constant.LOGIN_FAILED));
		}

	}
	
	@RequestMapping(value="getUsers",method=RequestMethod.POST)
	public @ResponseBody Map getUsers(User user,PageInfo pageInfo){
		
		 Map json = MyJSONUtil.toMyJSON(userManager.getUsers(user, pageInfo), userManager.getUserCount(user, pageInfo));
		
		return json;
	}
	
	@RequestMapping(value="save",method=RequestMethod.POST)
	public @ResponseBody Map saveUser(User user){
		return null;
	}

}
