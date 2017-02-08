package com.xs.veh.network;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.xs.common.exception.SystemException;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;

public interface ICheckDevice {

	public  void startCheck(VehCheckLogin vehCheckLogin, VehFlow vehFlow,Map<String,Object> otherParam) throws IOException, InterruptedException, SystemException;
	
	public void startCheck(VehCheckLogin vehCheckLogin, List<VehFlow> vehFlows,Map<String,Object> otherParam) throws InterruptedException, IOException, SystemException;

}
