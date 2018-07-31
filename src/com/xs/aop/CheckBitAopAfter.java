package com.xs.aop;

import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.xs.veh.entity.BaseEntity;

@Component
@Aspect
public class CheckBitAopAfter {
	
	
	/**
	 * 方法结束执行
	 */
	@AfterReturning(returning="rvt",pointcut="execution(* org.springframework.orm.hibernate4.HibernateTemplate.find*(..)) || execution(* org.springframework.orm.hibernate4.HibernateTemplate.load(..)) || execution(* org.springframework.orm.hibernate4.HibernateTemplate.get(..))")
	public void after(JoinPoint joinPoint,Object rvt) {
		
		if(rvt instanceof List) {
			List array = (List)rvt;
			for(Object o : array) {
				if(o instanceof BaseEntity) {
					BaseEntity be = (BaseEntity)o;
					be.checkBit();
				}
			}
		}else {
			if(rvt instanceof BaseEntity) {
				BaseEntity be = (BaseEntity)rvt;
				be.checkBit();
			}
		}
	}
	
	


}
