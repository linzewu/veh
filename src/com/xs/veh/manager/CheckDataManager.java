package com.xs.veh.manager;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.xs.veh.network.BrakRollerData;
import com.xs.veh.network.data.LightData;
import com.xs.veh.network.data.SideslipData;
import com.xs.veh.network.data.SpeedData;
import com.xs.veh.network.data.WeighData;

@Service("checkDataManager")
public class CheckDataManager {

	Logger logger = Logger.getLogger(CheckDataManager.class);

	@Resource(name = "hibernateTemplate")
	private HibernateTemplate hibernateTemplate;

	@Resource(name = "vehManager")
	private VehManager vehManager;

	/**
	 * 保存灯光数据
	 * 
	 * @param data
	 */
	public void lightSave(LightData highBeamOfMain, LightData lowBeamOfMain, LightData highBeamOfSide) {
		
		
		logger.info("保存数据");
		logger.info("主远光灯"+highBeamOfMain);
		logger.info("主近光灯"+lowBeamOfMain);
		logger.info("副远光灯"+highBeamOfSide);
	}

	public void saveBrakRoller(BrakRollerData brakRollerData) {
		
		System.out.println("制动数据"+brakRollerData);

	}
	
	public void saveSpeedData(SpeedData speedData){
		
	}
	
	public void saveSideslipData(SideslipData sideslipData){
		
	}
	
	public void saveWeighData(WeighData weighData){
		
	}

}
