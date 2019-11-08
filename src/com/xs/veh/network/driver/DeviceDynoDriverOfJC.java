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
	private String ksjc;
	// 取该次检测结果数据
	private String dqsj;
	// 对仪表进行清零
	private String ybql;

	private String jcjs = "41046754";

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

	@Override
	public DynoData startCheck(VehCheckLogin vehCheckLogin, VehFlow vehFlow) throws IOException, InterruptedException {
		lDatas.clear();
		rDatas.clear();
		this.getTemp().clear();
		// 仪表清零
		deviceDyno.sendMessage(ybql);
		logger.info("发送清零命令:" + ybql);
		// logger.info("仪表清0返回："+CharUtil.byte2HexOfString(this.getDevData(new
		// byte[4],(byte)0x41)));

		// 开始新的一次检测
		createNew();
		this.display.sendMessage(vehCheckLogin.getHphm(), DeviceDisplay.SP);
		this.display.sendMessage("请上悬架仪检测", DeviceDisplay.XP);
		dw(vehCheckLogin);
		this.display.sendMessage("悬架到位，开始检测", DeviceDisplay.XP);
		deviceDyno.sendMessage(ksjc);

		logger.info("开始检测返回：" + CharUtil.byte2HexOfString(this.getDevData(new byte[4], (byte) 0x41)));

		logger.info("检测左轮悬挂：" + CharUtil.byte2HexOfString(this.getDevData(new byte[4], (byte) 0x41)));
		this.display.sendMessage("检测左轮悬挂", DeviceDisplay.XP);
		// 获取过程数据
		// getProcessData();
		logger.info("检测右轮悬挂：" + CharUtil.byte2HexOfString(this.getDevData(new byte[4], (byte) 0x41)));
		this.display.sendMessage("检测右轮悬挂", DeviceDisplay.XP);
		// 获取过程数据
		// getProcessData();

		byte[] endCommon = this.getDevData(new byte[4], (byte) 0x41);

		logger.info("检测结束命令：：" + CharUtil.byte2HexOfString(endCommon));

		// 发送读取数据命令
		deviceDyno.sendMessage(dqsj);
		// this.display.sendMessage("右电机启动", DeviceDisplay.XP);

		byte[] data = this.getDevData(new byte[27], (byte) 0x41);

		// 左悬挂效率
		String zxgxl = new String(new byte[] { data[3], data[4], data[5], data[6], data[7] });
		// 右悬挂效率
		String yxgxl = new String(new byte[] { data[8], data[9], data[10], data[11], data[12] });
		// 左右差
		String zyc = new String(new byte[] { data[13], data[14], data[15], data[16], data[17] });
		// 左静态轮种
		String zjtlz = new String(new byte[] { data[18], data[19], data[20], data[21] });
		// 左静态轮种
		String yjtlz = new String(new byte[] { data[22], data[23], data[24], data[25] });

		this.display.sendMessage("检测完成", DeviceDisplay.SP);
		this.display.sendMessage(zxgxl + "/" + yxgxl + "/" + zyc, DeviceDisplay.XP);

		Thread.sleep(2000);
		return this.dynoData;
	}

	private void dw(VehCheckLogin vehCheckLogin) throws InterruptedException, IOException {
		int i = 0;
		while (true) {
			if (this.signal.getSignal(s1)) {
				this.display.sendMessage(vehCheckLogin.getHphm(), DeviceDisplay.SP);
				this.display.sendMessage("悬架到位", DeviceDisplay.XP);
				i++;
			} else {
				this.display.sendMessage(vehCheckLogin.getHphm(), DeviceDisplay.SP);
				this.display.sendMessage("请上悬架仪检测", DeviceDisplay.XP);
				i = 0;
			}
			if (i >= 6) {
				break;
			}
			Thread.sleep(500);
		}

	}

	@Override
	public void init(DeviceDyno deviceDyno) {
		super.init(deviceDyno);
		ksjc = "41046556";
		dqsj = "41046853";
		ybql = "41043388";

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
	
	public static void main(String[] age) {
		System.out.println(checkSum("2A5056303031303030"));
		System.out.println(CharUtil.byte2HexOfString("006000".getBytes()));
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
