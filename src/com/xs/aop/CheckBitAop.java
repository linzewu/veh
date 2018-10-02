package com.xs.aop;

import java.io.UnsupportedEncodingException;

import javax.annotation.Resource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.annotation.CheckBit;
import com.xs.common.exception.TamperWithDataException;
import com.xs.veh.entity.BaseEntity;

@Service
@Aspect
public class CheckBitAop {
	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	@Pointcut("execution(* com.xs.common.MyHibernateTemplate.save*(..))")
	public void save(){}
	@Pointcut("execution(* com.xs.common.MyHibernateTemplate.update*(..))")
	public void update(){}
	@Pointcut("execution(* com.xs.common.MyHibernateTemplate.merge*(..))")
	public void merge(){}
	
	/**
	 * 方法开始执行
	 * @throws UnsupportedEncodingException 
	 */
	@Before("save() || update()  || merge()")
	public void doBefore(JoinPoint joinPoint) throws UnsupportedEncodingException,TamperWithDataException {
		Object[] params = joinPoint.getArgs();
		if(params!=null) {
			for(Object obj:params) {
				if(obj.getClass().isAnnotationPresent(CheckBit.class)&&obj instanceof BaseEntity) {
					BaseEntity be =(BaseEntity)obj;					
					String str = be.toString();
					String md5 = BaseEntity.md5(str);
					System.out.println("before:"+be.toString());
					be.setVehjyw(md5);
					if(be.getId()!=null) {
						BaseEntity base = (BaseEntity)this.hibernateTemplate.load(obj.getClass(), be.getId());
						base.checkBit();
						if(!base.isCheckBitOk()) {							
							throw new TamperWithDataException("数据非法篡改!");
							
						}
					}
				}
			}
		}
		
	}

}
