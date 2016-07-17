package com.xs.veh.network;

import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;

public interface ICheckDevice {

	public void startCheck(VehCheckLogin vehCheckLogin, VehFlow vehFlow)
			throws Exception;

}
