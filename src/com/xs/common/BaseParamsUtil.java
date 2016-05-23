package com.xs.common;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.xs.veh.entity.BaseParams;

public class BaseParamsUtil {

	public static List<BaseParams> getBaseParamsByType(String type) {

		WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();
		ServletContext servletContext = webApplicationContext.getServletContext();
		
		List<BaseParams> bps = (List<BaseParams>) servletContext
				.getAttribute("bps");
		
		List<BaseParams> types=new ArrayList<BaseParams>();
		
		for(BaseParams bp:bps){
			
			if(type.equals(bp.getType())){
				types.add(bp);
			}
			
		}
		
		return types;

	}

}
