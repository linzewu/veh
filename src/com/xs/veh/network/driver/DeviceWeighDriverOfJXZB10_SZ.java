package com.xs.veh.network.driver;

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;

import com.xs.common.CharUtil;
import com.xs.veh.entity.VehCheckProcess;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.AbstractDeviceWeigh;
import com.xs.veh.network.DeviceDisplay;
import com.xs.veh.network.DeviceWeigh;
import com.xs.veh.network.TakePicture;
import com.xs.veh.network.SimpleRead.ProtocolType;
import com.xs.veh.network.data.BrakRollerData;

public class DeviceWeighDriverOfJXZB10_SZ extends AbstractDeviceWeigh {
	
	static Logger logger = Logger.getLogger(DeviceWeighDriverOfJXZB10_SZ.class);

	// 开始称重
	private String dqsj="41046853";

	private String ql="41046259";
	
	private String sdcz="41046754";
	
	private String jcsd="4104704B";
	
	public static final byte  A= CharUtil.hexStringToByte("41")[0];
	

	public ProtocolType getProtocolType(byte[] bs) {
		return ProtocolType.DATA;
	}
	@Override
	public BrakRollerData startCheck(VehFlow vehFlow) throws IOException, InterruptedException{

		Date kssj=new Date();
		
		//deviceWeigh.sendMessage(ql);
		//logger.info("清零返回："+CharUtil.byte2HexOfString(this.getDevData(new byte[4])));
		
		//解出锁定
	//	deviceWeigh.sendMessage(jcsd);
	//	logger.info("解出锁定："+CharUtil.byte2HexOfString(this.getDevData(new byte[4],A)));
		
		String zs = vehFlow.getJyxm().substring(1, 2);

		String hphm = vehFlow.getHphm();

		// 开始新的一次检测
		createNew();
		
		if(this.deviceWeigh.getVehCheckLogin().getJycs()==1&&this.deviceWeigh.getVehCheckLogin().getJyxm().indexOf("Z1")!=-1) {
			if(zs.equals("1")) {
				this.deviceWeigh.updateZ1VehCheckProcessStart();
			}
		}
		
		
		
		// 显示屏显示信息
		this.display.sendMessage(hphm, DeviceDisplay.SP);
		this.display.sendMessage(zs + "轴称重请到位", DeviceDisplay.XP);
		int i = 0;
		while (true) {
			if (this.signal.getSignal(s1)) {
				deviceWeigh.sendMessage(dqsj);
				byte[] data = this.getDevData(new byte[19],A);
				logger.info("称重开始："+CharUtil.byte2HexOfString(data));
				
				Integer zlh=Integer.parseInt(new String(new byte[]{data[3],data[4],data[5],data[6],data[7]}));
				Integer ylh=Integer.parseInt(new String(new byte[]{data[8],data[9],data[10],data[11],data[12]}));
				
				this.display.sendMessage(zs + "轴称重已到位", DeviceDisplay.SP);
				this.display.sendMessage(zlh+"KG/"+ylh + "KG",
						DeviceDisplay.XP);
				
//				this.display.sendMessage("倒数："+String.valueOf((5-i)),DeviceDisplay.XP);
				i++;
			} else {
				this.display.sendMessage(vehFlow.getHphm(), DeviceDisplay.SP);
				this.display.sendMessage(zs + "轴称重请到位", DeviceDisplay.XP);
				i = 0;
			}

			if (i >= 5) {
				break;
			}

			Thread.sleep(1000);
		}
		
		if(this.deviceWeigh.getVehCheckLogin().getJycs()==1&&this.deviceWeigh.getVehCheckLogin().getJyxm().indexOf("Z1")!=-1) {
			if(zs.equals("1")) {
				TakePicture.createNew(this.deviceWeigh.getVehCheckLogin(),"Z1",0,"0362");
			}
			if(zs.equals("2")) {
				TakePicture.createNew(this.deviceWeigh.getVehCheckLogin(),"Z1",0,"0363");
			}
		}
		
		
		
		//deviceWeigh.sendMessage(sdcz);
		//logger.info("称重结果锁定："+CharUtil.byte2HexOfString(this.getDevData(new byte[4])));
		
		deviceWeigh.sendMessage(dqsj);
		byte[] data = this.getDevData(new byte[19],A);
		Integer zlh=Integer.parseInt(new String(new byte[]{data[3],data[4],data[5],data[6],data[7]}));
		Integer ylh=Integer.parseInt(new String(new byte[]{data[8],data[9],data[10],data[11],data[12]}));
		brakRollerData.setZlh(zlh);
		brakRollerData.setYlh(ylh);

		this.display.sendMessage(zs + "轴称重结束", DeviceDisplay.SP);
		this.display.sendMessage((brakRollerData.getZlh() + brakRollerData.getYlh()) + "KG", DeviceDisplay.XP);
		
		

		return brakRollerData;

	}

	private void createNew() {
		this.brakRollerData = new BrakRollerData();
	}

	
	@Override
	public void init(DeviceWeigh deviceWeigh) {
		super.init(deviceWeigh);
	}

}
