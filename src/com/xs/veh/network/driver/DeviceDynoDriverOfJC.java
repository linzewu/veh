package com.xs.veh.network.driver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.Session;

import org.apache.log4j.Logger;

import com.xs.common.CharUtil;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.AbstractDeviceDyno;
import com.xs.veh.network.DeviceDisplay;
import com.xs.veh.network.DeviceDyno;
import com.xs.veh.network.SimpleRead.ProtocolType;
import com.xs.veh.network.data.DynoData;
import com.xs.veh.websocket.MyWebSocket;

/**
 * 底盘测功
 * 
 * @author linzewu
 *
 */
public class DeviceDynoDriverOfJC extends AbstractDeviceDyno {
	private static Logger logger = Logger.getLogger(DeviceDynoDriverOfJC.class);

	// 开始检测
	// 取该次检测结果数据
	// 对仪表进行清零


	private List<Integer> lDatas = new ArrayList<Integer>();
	private List<Integer> rDatas = new ArrayList<Integer>();
	
	private byte[] temp=new byte[34];

	public ProtocolType getProtocolType(byte[] bs) {
		return ProtocolType.DATA;
	}

	private void createNew() {
		this.dynoData = new DynoData();
		this.getTemp().clear();
	}


	private void dw(VehCheckLogin vehCheckLogin) throws InterruptedException, IOException {
		
	}

	@Override
	public void init(DeviceDyno deviceDyno) {
		super.init(deviceDyno);

	}

	@Override
	public void device2pc(byte[] ed) throws IOException {
		Session session = MyWebSocket.sessionMap.get("deviceId_" + this.deviceDyno.getDevice().getId());
		if (session != null) {
			int index=0;
			for (byte b : ed) {
				
				if(index>33) {
					index=0;
				}
				
				if(b==0x2a) {
					index=0;
				}
				temp[index]=b;
				
				if(index==33) {
					synchronized (session) {
						session.getBasicRemote().sendText(new String(temp));
					}
				}
				index++;
				
			}
			
		}

	}

	@Override
	public void sendCommon(String common,Object... param) throws IOException {
		
		switch (common) {
		case "qddj":
			deviceDyno.sendMessage("2A4D4FC60D");
			break;
		case "gbdj":
			deviceDyno.sendMessage("2A4D46BD0D");
			break;
		case "jlql":
			deviceDyno.sendMessage("2A4443B10D");
			break;
		case "ksjz":
			deviceDyno.sendMessage("2A5054CE0D");
			break;
		case "tzjz":
			deviceDyno.sendMessage("2A5053CD0D");
			break;	
		case "jzlsz":
			String jzl = zeroFill(String.valueOf(param[0]));
			String pre="2A5056"+CharUtil.byte2HexOfString(jzl.getBytes());
			String cc =pre + checkSum(pre)+"0D";
			deviceDyno.sendMessage(cc);
			break;		
		default:
			break;
		}
		
	}
	
	public static String checkSum(String hex) {
		byte[] vs = CharUtil.hexStringToByte(hex);
		byte i = 0;
		for (byte v : vs) {
			i += v;
		}
		return CharUtil.byte2HexOfString(new byte[]{i});
	}
	
	
	private String zeroFill(String param) {
		
		if(param.length()<6) {
			param="0"+param;
			return zeroFill(param);
		}else {
			return param;
		}
		
	}

}
