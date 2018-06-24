package com.xs.aop;

import java.io.UnsupportedEncodingException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Service;

import com.xs.annotation.CheckBit;
import com.xs.veh.entity.BaseEntity;

@Service
@Aspect
public class CheckBitAop {
	
	/**
	 * 方法开始执行
	 * @throws UnsupportedEncodingException 
	 */
	@Before("execution(* com.xs.common.MyHibernateTemplate.save*(..)) || execution(* com.xs.common.MyHibernateTemplate.update(..)) || execution(* com.xs.common.MyHibernateTemplate.merge(..))")
	public void doBefore(JoinPoint joinPoint) throws UnsupportedEncodingException {
		Object[] params = joinPoint.getArgs();
		if(params!=null) {
			for(Object obj:params) {
				if(obj.getClass().isAnnotationPresent(CheckBit.class)&&obj instanceof BaseEntity) {
					BaseEntity be =(BaseEntity)obj;
					String str = be.toString();
					String md5 = BaseEntity.md5(str);
					be.setVehjyw(md5);
				}
			}
		}
		
	}
	
	
	/**
	 * 方法结束执行
	 */
	public void after(JoinPoint joinPoint) {
		
		
	}
	
	


}
