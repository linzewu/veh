package com.xs.veh.network.driver;

import java.io.IOException;
import java.util.Arrays;

import javax.websocket.Session;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.xs.common.CharUtil;
import com.xs.veh.network.AbstractDeviceDyno;
import com.xs.veh.network.DeviceDyno;
import com.xs.veh.network.SimpleRead.ProtocolType;
import com.xs.veh.websocket.MyWebSocket;

/**
 * 底盘测功
 * 
 * @author linzewu
 *
 */
public class DeviceDynoDriverOfTLCGJ10 extends AbstractDeviceDyno {
	private static Logger logger = Logger.getLogger(DeviceDynoDriverOfTLCGJ10.class);
	
	private boolean cFlag=false;
	
	
	
	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		DeviceDynoDriverOfTLCGJ10.logger = logger;
	}

	public boolean iscFlag() {
		return cFlag;
	}

	public void setcFlag(boolean cFlag) {
		this.cFlag = cFlag;
	}

	public ProtocolType getProtocolType(byte[] bs) {
		return ProtocolType.DATA;
	}

	@Override
	public void init(DeviceDyno deviceDyno) {
		super.init(deviceDyno);

	}
	
	public void sendMessage(String message) throws IOException {
		Session session = MyWebSocket.sessionMap.get("deviceId_" + this.deviceDyno.getDevice().getId());
		synchronized (session) {
			session.getBasicRemote().sendText(message);
		}
	}

	
	@Override
	public void sendCommon(String common,Object... param) throws IOException, InterruptedException {
		logger.info(common);
		this.getTemp().clear();
		switch (common) {
		case "dqgd":  //读取关电信号
			deviceDyno.sendMessage("410405B6");
			String gdxh = CharUtil.byte2HexOfString(getDevData(new byte[12]));
			break;
		case "ql": //清零
			deviceDyno.sendMessage("410402B9");
			CharUtil.byte2HexOfString(getDevData(new byte[4]));
			break;
		case "jsqss":  //举升器上升
			deviceDyno.sendMessage("41044477");
			CharUtil.byte2HexOfString(getDevData(new byte[4]));
			break;
		case "jsqxj":  //举升器下降
			deviceDyno.sendMessage("41044576");
			CharUtil.byte2HexOfString(getDevData(new byte[4]));
			break;
		case "qdbpq":  //启动变频器
			deviceDyno.sendMessage("41044873");
			CharUtil.byte2HexOfString(getDevData(new byte[4]));
			break;	
		case "tzbpq":  //停止变频器
			deviceDyno.sendMessage("41044872");
			CharUtil.byte2HexOfString(getDevData(new byte[4]));
			break;
		case "qdfj":  //启动风机
			deviceDyno.sendMessage("41044C6F");
			CharUtil.byte2HexOfString(getDevData(new byte[4]));
			break;
		case "tzfj":  //停止风机
			deviceDyno.sendMessage("41044C6E");
			CharUtil.byte2HexOfString(getDevData(new byte[4]));
			break;
		case "jrzwlj":  //接入左涡流机
			deviceDyno.sendMessage("41044A71");
			CharUtil.byte2HexOfString(getDevData(new byte[4]));
			break;
		case "jrywlj":  //接入右涡流机
			deviceDyno.sendMessage("41044675");
			CharUtil.byte2HexOfString(getDevData(new byte[4]));
			break;
		case "dkzwlj":  //断开左涡流机
			deviceDyno.sendMessage("41044B70");
			CharUtil.byte2HexOfString(getDevData(new byte[4]));
			break;
		case "dkywlj":  //断开右涡流机
			deviceDyno.sendMessage("41044774");
			CharUtil.byte2HexOfString(getDevData(new byte[4]));
			break;
		case "fw":  //复位
			deviceDyno.sendMessage("4104BA01");
			CharUtil.byte2HexOfString(getDevData(new byte[4]));
			break;
		case "dqsj":  //读取数据
			deviceDyno.sendMessage("410407B4");
			//getContinuityData(0);
			byte b=0x41;
			byte[] cdata = getDevData(new byte[49],b);
			String strData = processCurnetData(cdata);
			sendMessage(strData);
			break;
		case "hcs":  //恒车速
			{
				String ml="410850";
				Float speed = (Float) param[0];
				String strSpeed = String.valueOf(speed*10);
				strSpeed = CharUtil.zeroFill(strSpeed, 4);
				byte[] bs = new byte[]{(byte) strSpeed.charAt(0),(byte) strSpeed.charAt(1),(byte) strSpeed.charAt(2),(byte) strSpeed.charAt(3)};
				ml+=CharUtil.byte2String(bs);
				String sum = CharUtil.getCheckSum(ml);
				ml+=sum;
				deviceDyno.sendMessage(ml);
			}
			break;
		case "hnl":  //恒扭力
			{
				String ml="410953";
				Integer n = (Integer) param[0];
				String strN = CharUtil.zeroFill(n.toString(), 5);
				byte[] bs = new byte[]{(byte) strN.charAt(0),(byte) strN.charAt(1),(byte) strN.charAt(2),(byte) strN.charAt(3),(byte) strN.charAt(4)};
				ml+=CharUtil.byte2String(bs);
				String sum = CharUtil.getCheckSum(ml);
				ml+=sum;
				deviceDyno.sendMessage(ml);
				getContinuityData(1);
				
			}
			break;
		case "hgl":  //恒功率
			{
				String ml="410857";
				Integer kw = (Integer) param[0];
				String strKw = CharUtil.zeroFill(kw.toString(), 4);
				byte[] bs = new byte[]{(byte) strKw.charAt(0),(byte) strKw.charAt(1),(byte) strKw.charAt(2),(byte) strKw.charAt(3)};
				ml+=CharUtil.byte2String(bs);
				String sum = CharUtil.getCheckSum(ml);
				ml+=sum;
				deviceDyno.sendMessage(ml);
			}
			break;
		default:
			break;
		}
		
	}

	private void getContinuityData(Integer type) throws InterruptedException, IOException {
		cFlag=true;
		while(cFlag) {
			byte[] data = getDevData(new byte[49]);
			
			if(type==0) {
				String strData = processCurnetData(data);
				sendMessage(strData);
			}
			
			if(type==1) {
				String strData = processHNLData(data);
				sendMessage(strData);
			}
			
			if(type==2) {
				String strData = processHGLData(data);
				sendMessage(strData);
			}
			
			if(type==2) {
				String strData = processHSDData(data);
				sendMessage(strData);
			}
		}
		
	}
	
	
	public String processHNLData(byte[] data) {
		return "";
	}
	
	public String processHGLData(byte[] data) {
		
		return "";
	}
	
	public String processHSDData(byte[] data) {
		
		return "";
	}
	
	
	public String processCurnetData(byte[] data) {
		
		Integer nl1= 0;
		Float sd= 0f;
		Float jsd= 0f;
		Integer sj =0;
		Integer jl = 0;
		
		try {
			nl1= Integer.parseInt(new String(Arrays.copyOfRange(data, 3, 8)).trim());
			 sd= Integer.parseInt(new String(Arrays.copyOfRange(data, 23, 28)).trim())/100.0f;
			 jsd= Integer.parseInt(new String(Arrays.copyOfRange(data, 28, 32)).trim())/100.0f;
			 sj = Integer.parseInt(new String(Arrays.copyOfRange(data, 32, 40)).trim());
			 jl = Integer.parseInt(new String(Arrays.copyOfRange(data, 40, 48)).trim());
		}catch (Exception e) {
			logger.error("处理数据异常！");
		}
		 
		JSONObject jo =new JSONObject();
		jo.put("nl", nl1);
		jo.put("sd", sd);
		jo.put("jsd", jsd);
		jo.put("sj", sj);
		jo.put("jl", jl);
		return jo.toString();
	}


}
