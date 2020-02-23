package com.xs.veh.network;

import java.io.IOException;

public interface IHBCommon {
	
	public abstract void sendCommon(String common,Object... param) throws IOException, InterruptedException;

}
