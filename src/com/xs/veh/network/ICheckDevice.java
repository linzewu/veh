package com.xs.veh.network;

import java.io.IOException;

import com.xs.common.exception.SystemException;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;

public interface ICheckDevice {

	public void startCheck(VehCheckLogin vehCheckLogin, VehFlow vehFlow)
			throws SystemException, IOException, InterruptedException;

}
