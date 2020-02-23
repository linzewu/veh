package com.xs.veh.network;

import java.io.IOException;

public abstract class  AbstractDevice {
	public abstract void device2pc(byte[] data) throws IOException,InterruptedException;
	
}
