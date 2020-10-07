package com.xs.veh.network;

import java.io.IOException;
import java.util.List;
import java.util.TooManyListenersException;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.xs.common.BaseParamsUtil;
import com.xs.common.exception.SystemException;
import com.xs.veh.entity.BaseParams;
import com.xs.veh.entity.Device;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.driver.LED192;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPortEvent;
import gnu.io.UnsupportedCommOperationException;

@Service("deviceDisplay")
@Scope("prototype")
public class DeviceDisplay extends SimpleRead {

	public final static Integer SP = 0;

	public final static Integer XP = 1;
	
	private LED192 led;

	public LED192 getLed() {
		return led;
	}

	public void setLed(LED192 led) {
		this.led = led;
	}

	public DeviceDisplay() {
	}

	public DeviceDisplay(Device device) throws NoSuchPortException, TooManyListenersException, PortInUseException,
			UnsupportedCommOperationException, IOException {
		super(device);
		init();
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
	}

	@Override
	public void run() {
	}

	public String getRtx() {
		return null;
	}

	public void sendMessage(String message, int ph) throws IOException {
		
		Integer dhcf = Integer.parseInt(this.getQtxxObject().get("dhcf").toString());

		String xy = null;
		if (ph == 1) {
			xy = (String) this.getQtxxObject().get("xpxy");
		} else {
			xy = (String) this.getQtxxObject().get("spxy");
		}

		this.sendHead(xy);

		if (dhcf == 0) {
			message += "\r\n";
		}
		this.outputStream.write(message.getBytes("GBK"));
		
	}
	
	public static void main(String[] age) {
		
		LED192.led192(1, "#12宋体", "测试", "测试");
		
	}
	
	
	public void setDefault() throws IOException{
		String xpmr = (String) this.getQtxxObject().get("xpmr");
		String spmr = (String) this.getQtxxObject().get("spmr");
		sendMessage(xpmr,XP);
		sendMessage(spmr,SP);
	}
	

	@Override
	public void init() {
		// 没有返回值不启用监听器
		this.setAddListener(false);

	}

	@Override
	public String getDeviceSpringName() {
		return "deviceDisplay";
	}
}
