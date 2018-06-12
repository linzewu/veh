package com.xs.aop;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.xs.annotation.Modular;
import com.xs.annotation.UserOperation;
import com.xs.veh.entity.OperationLog;
import com.xs.veh.entity.User;
import com.xs.veh.manager.OperationLogManager;

@Service
@Aspect
public class LogAopAction {
	// 获取开始时间
	private Date BEGIN_TIME;

	// 获取结束时间
	private Date END_TIME;

	// 定义本次log实体
	private OperationLog log = new OperationLog();

	@Autowired
	private HttpSession session;

	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private OperationLogManager operationLogManager;

	@Pointcut("execution(* com.xs.veh.controller.*.*(..))")
	private void controllerAspect() {
	}

	/**
	 * 方法开始执行
	 */
	@Before("controllerAspect()")
	public void doBefore(JoinPoint joinPoint) {
		BEGIN_TIME = new Date();
	}

	/**
	 * 方法结束执行
	 */
	@After("controllerAspect()")
	public void after(JoinPoint joinPoint) {
		END_TIME = new Date();
	}

	/**
	 * 方法结束执行后的操作
	 */
	@AfterReturning("controllerAspect()")
	public void doAfter(JoinPoint joinPoint) {
		if(log.getStatus()==1){
            log.setActionTime(END_TIME.getTime()-BEGIN_TIME.getTime());
            log.setOperationDate(BEGIN_TIME);
            operationLogManager.saveOperationLog(log);
		}
	}

	/**
	 * 方法有异常时的操作
	 */
	@AfterThrowing(pointcut="controllerAspect()",throwing="e")
	public void doAfterThrow(JoinPoint joinPoint,Throwable e) {
		System.out.println("doAfterThrow++++++"+log.getStatus());
		log.setOperationResult(OperationLog.OPERATION_RESULT_ERROR);
		log.setStatus(1);
		log.setFailMsg(e.getMessage());
	}

	/**
	 * 方法执行
	 * 
	 * @param pjp
	 * @return
	 * @throws Throwable
	 */
	@Around("controllerAspect()")
	public Object around(ProceedingJoinPoint pjp) throws Throwable {
		
		System.out.println("around++++++"+log.getStatus());
		// 日志实体对象
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		// 获取当前登陆用户信息
		User loginUser = (User) session.getAttribute("user");
		if (loginUser == null) {
			log.setOperationUser("—— ——");
		} else {
			log.setOperationUser(loginUser.getUserName());
		}

		// 拦截的实体类，就是当前正在执行的controller
		Object target = pjp.getTarget();
		// 拦截的方法名称。当前正在执行的方法
		String methodName = pjp.getSignature().getName();
		// 拦截的方法参数
		Object[] args = pjp.getArgs();
		// 拦截的放参数类型

		MethodSignature msig = (MethodSignature) pjp.getSignature();

		Class[] parameterTypes = msig.getMethod().getParameterTypes();
		Object object = null;
		Class targetClass =target.getClass();
		Method method = targetClass.getMethod(methodName, parameterTypes);
		
		if(targetClass.isAnnotationPresent(Modular.class)) {
			Modular modular=(Modular) targetClass.getAnnotation(Modular.class);
			log.setModule(modular.modelName());
		}

		// 判断是否包含自定义的注解，说明一下这里的SystemLog就是我自己自定义的注解
		if (method.isAnnotationPresent(UserOperation.class)) {
			UserOperation userOperation = method.getAnnotation(UserOperation.class);
			log.setOperationType(userOperation.name());
			log.setIpAddr(getIpAdrress());
			log.setActionUrl(request.getRequestURI());
			object = pjp.proceed();
			log.setOperationResult(OperationLog.OPERATION_RESULT_SUCCESS);
			log.setStatus(1);
			
			if(userOperation.code().equals("login")) {
				Map map = (Map)object;
				if(map != null) {
					if("0".equals(map.get("state").toString())){
						log.setOperationResult(OperationLog.OPERATION_RESULT_ERROR);
						log.setFailMsg(map.get("errorMsg").toString());
					}
				}
			}
			
		}else {
			object = pjp.proceed();
		}
		return object;
	}
	
	

	private String getIpAdrress() {
		String Xip = request.getHeader("X-Real-IP");
		String XFor = request.getHeader("X-Forwarded-For");
		if (StringUtils.isNotEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)) {
			// 多次反向代理后会有多个ip值，第一个ip才是真实ip
			int index = XFor.indexOf(",");
			if (index != -1) {
				return XFor.substring(0, index);
			} else {
				return XFor;
			}
		}
		XFor = Xip;
		if (StringUtils.isNotEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)) {
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

}
