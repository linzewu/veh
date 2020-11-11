package com.xs.veh.network;

import java.io.IOException;
import java.util.Date;

public abstract class  AbstractDevice {
	
	protected Date dwKssj;
	
	public abstract void device2pc(byte[] data) throws IOException,InterruptedException;
	
}
