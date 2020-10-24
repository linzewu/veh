package com.xs.veh.network.driver;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.xs.common.CharUtil;
import com.xs.common.exception.SystemException;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.AbstractDeviceWeigh;
import com.xs.veh.network.DeviceDisplay;
import com.xs.veh.network.DeviceWeigh;
import com.xs.veh.network.SimpleRead.ProtocolType;
import com.xs.veh.network.data.BrakRollerData;

public class DeviceWeighDriverOfJXZB10 extends AbstractDeviceWeigh {
	
	static Logger logger = Logger.getLogger(DeviceWeighDriverOfJXZB10.class);

	// 开始称重
	private String dqsj="41046853";

	private String ql="41046259";
	
	private String sdcz="41046754";
	
	public static final byte  A= CharUtil.hexStringToByte("41")[0];
	

	public ProtocolType getProtocolType(byte[] bs) {
		return ProtocolType.DATA;
	}
	@Override
	public BrakRollerData startCheck(VehFlow vehFlow) throws IOException, InterruptedException, SystemException{
		
		//deviceWeigh.sendMessage(ql);
	//	logger.info("清零返回："+CharUtil.byte2HexOfString(this.getDevData(new byte[4])));
		String zs = vehFlow.getJyxm().substring(1, 2);
		String hphm = vehFlow.getHphm();
	
		if(this.deviceWeigh.getVehCheckLogin().getCllx().indexOf("N")!=-1&&"2".equals(zs)) {
			BrakRollerData old = this.deviceWeigh.getBrakRollerData("B1");
			if(old==null) {
				BrakRollerData brakRollerData1 = check("1",hphm);
				brakRollerData1.setBaseDeviceData(this.deviceWeigh.getVehCheckLogin(), this.deviceWeigh.getVehCheckLogin().getJycs(), "B1");
				brakRollerData1.setZw(1);
				this.deviceWeigh.saveBrakRollerData(brakRollerData1);
			}else {
				BrakRollerData brakRollerData1 = check("1",hphm);
				old.setZlh(brakRollerData1.getZlh());
				old.setYlh(brakRollerData1.getYlh());
				old.setZw(1);
				this.deviceWeigh.saveBrakRollerData(old);
			}
			
			
			Thread.sleep(2000);
			this.display.sendMessage("检测完毕向前行驶", DeviceDisplay.XP);
			boolean flag = true;
			while (flag) {
				flag = this.signal.getSignal(s1);
				Thread.sleep(200);
			}
			brakRollerData = check(zs,hphm);
		}else {
			brakRollerData = check(zs,hphm);
		}
		

		return brakRollerData;

	}
	
	
	public BrakRollerData  check(String zs,String hphm) throws IOException, InterruptedException, SystemException {

		// 开始新的一次检测
		createNew();
		
		BrakRollerData brakRollerData = new BrakRollerData();
		
		// 显示屏显示信息
		this.display.sendMessage(hphm, DeviceDisplay.SP);
		this.display.sendMessage(zs + "轴称重请到位", DeviceDisplay.XP);
		int i = 0;
		while (true) {
			if (this.signal.getSignal(s1)) {
				try {
					deviceWeigh.sendMessage(dqsj);
					byte[] data = this.getDevData(new byte[19],A);
					logger.info("称重开始："+CharUtil.byte2HexOfString(data));
					
					Integer zlh=Integer.parseInt(new String(new byte[]{data[3],data[4],data[5],data[6],data[7]}));
					Integer ylh=Integer.parseInt(new String(new byte[]{data[8],data[9],data[10],data[11],data[12]}));
					
					this.display.sendMessage(zs + "轴称重已到位", DeviceDisplay.SP);
					this.display.sendMessage(zlh+"KG/"+ylh + "KG",
							DeviceDisplay.XP);
					i++;
				}catch (Exception e) {
					logger.error("称重异常！", e);
					i=0;
					this.getTemp().clear();
				}
			} else {
				this.display.sendMessage(hphm, DeviceDisplay.SP);
				this.display.sendMessage(zs + "轴称重请到位", DeviceDisplay.XP);
				i = 0;
			}

			if (i >= 12) {
				break;
			}

			Thread.sleep(500);
		}
		/*deviceWeigh.sendMessage(sdcz);
		logger.info("称重结果锁定："+CharUtil.byte2HexOfString(this.getDevData(new byte[4])));*/
		
		deviceWeigh.sendMessage(dqsj);
		byte[] data = this.getDevData(new byte[19],A);
		Integer zlh=Integer.parseInt(new String(new byte[]{data[3],data[4],data[5],data[6],data[7]}));
		Integer ylh=Integer.parseInt(new String(new byte[]{data[8],data[9],data[10],data[11],data[12]}));
		brakRollerData.setZlh(zlh);
		brakRollerData.setYlh(ylh);

		this.display.sendMessage(zs + "轴称重结束", DeviceDisplay.SP);
		this.display.sendMessage((brakRollerData.getZlh() + brakRollerData.getYlh()) + "KG", DeviceDisplay.XP);
		
		if((brakRollerData.getZlh() + brakRollerData.getYlh())==0){
			Thread.sleep(1000);
			this.display.sendMessage(zs + "称重异常，等待复位", DeviceDisplay.SP);
			Thread.sleep(4000);
			throw new  SystemException("称重异常");
		}
		
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
