package com.xs.veh.network;

import java.util.List;

import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;

public interface ICheckDevice {

	public  void startCheck(VehCheckLogin vehCheckLogin, VehFlow vehFlow)
			throws Exception;
	
	public void startCheck(VehCheckLogin vehCheckLogin, List<VehFlow> vehFlows)
			throws Exception;

}
