package com.xs.veh.network.driver;

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;

import com.xs.common.CharUtil;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.AbstractDeviceWeigh;
import com.xs.veh.network.DeviceDisplay;
import com.xs.veh.network.DeviceWeigh;
import com.xs.veh.network.SimpleRead.ProtocolType;
import com.xs.veh.network.TakePicture;
import com.xs.veh.network.data.BaseDeviceData;
import com.xs.veh.network.data.BrakRollerData;
import com.xs.veh.network.data.CurbWeightData;

public class DeviceWeighDriverOfJXZB10_SZ extends AbstractDeviceWeigh {
	
	static Logger logger = Logger.getLogger(DeviceWeighDriverOfJXZB10_SZ.class);

	// 开始称重
	private String dqsj="41046853";

	private String ql="41046259";
	
	private String sdcz="41046754";
	
	private String jcsd="4104704B";
	
	private String ljml = "41046A51";
	
	public boolean qzdw=false;
	public boolean hzdw=false;
	
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
	
	
	
	public CurbWeightData startCheckSZ(VehCheckLogin vehCheckLogin) throws Exception{
		
		this.qzdw=false;
		this.hzdw=false;
		

		Date kssj=new Date();
		
		this.deviceWeigh.updateZ1VehCheckProcessStart();
		
		deviceWeigh.sendMessage(ql);
		logger.info("清零返回："+CharUtil.byte2HexOfString(this.getDevData(new byte[4])));

		String hphm = vehCheckLogin.getHphm();

		// 开始新的一次检测
		createNew();
		
		Integer qz = check(vehCheckLogin, "前");
		
		logger.info("前轴检测完成");
		this.qzdw=false;
		Thread.sleep(5000);
		this.display.sendMessage("请向前行驶", DeviceDisplay.XP);
		Thread.sleep(2000);
		
		Integer hz = check(vehCheckLogin, "后"); 
		this.hzdw=false;
		CurbWeightData curbWeightData=new CurbWeightData();
		
		curbWeightData.setBaseDeviceData(vehCheckLogin, vehCheckLogin.getJycs(), "Z1");
		
		curbWeightData.setQzzl(qz);
		
		curbWeightData.setHzzl(hz);
		
		curbWeightData.setZbzl((qz+hz)-65);
		
		String cllx=vehCheckLogin.getCllx();
		int xzgj=100;
		String temp1="±3%或±";
		Float temp2=0.03f;
		
		if(cllx.indexOf("H1")==0||cllx.indexOf("H2")==0||cllx.indexOf("Z1")==0||cllx.indexOf("Z2")==0||cllx.indexOf("Z5")==0||cllx.indexOf("G")==0||cllx.indexOf("B")==0){
			xzgj=500;
		}else if(cllx.indexOf("H3")==0||cllx.indexOf("H4")==0||cllx.indexOf("Z3")==0||cllx.indexOf("Z4")==0){
			xzgj=100;
		}else if(cllx.indexOf("N")==0){
			xzgj=100;
			temp2=0.05f;
			temp1="±5%或±";
		}else if(cllx.indexOf("M")==0){
			xzgj=10;
		}
		Integer cz = vehCheckLogin.getZbzl()-(curbWeightData.getZbzl());
		
		Integer pd = Math.abs(cz)<xzgj?BaseDeviceData.PDJG_HG:BaseDeviceData.PDJG_BHG;
		
		Integer pd2 = Math.abs(cz*1.0/vehCheckLogin.getZbzl()*1.0)>temp2?BaseDeviceData.PDJG_BHG:BaseDeviceData.PDJG_HG;
		
		
		if(pd==BaseDeviceData.PDJG_HG||pd2==BaseDeviceData.PDJG_HG) {
			curbWeightData.setZbzlpd(BaseDeviceData.PDJG_HG);
		}else {
			curbWeightData.setZbzlpd(BaseDeviceData.PDJG_BHG);
		}


		return curbWeightData;

	}
	
	
	private Integer check(VehCheckLogin vehCheckLogin,String zw) throws Exception, InterruptedException {
		try {
			//解出锁定
			deviceWeigh.sendMessage(jcsd);
			logger.info("解出锁定："+CharUtil.byte2HexOfString(this.getDevData(new byte[4],A)));
			
			// 显示屏显示信息
			this.display.sendMessage(vehCheckLogin.getHphm(), DeviceDisplay.SP);
			this.display.sendMessage(zw+"轴称重请到位", DeviceDisplay.XP);
			int i = 0;
			while (true) {
//				deviceWeigh.sendMessage(dqgd);
//				byte[] singData = this.getDevData(new byte[12],A);
				
				
				deviceWeigh.sendMessage(dqsj);
				logger.info("读取称重数据："+dqsj);
				byte[] data = this.getDevData(new byte[19],A);
				logger.info("称重开始："+CharUtil.byte2HexOfString(data));
				
				Integer zlh=Integer.parseInt(new String(new byte[]{data[3],data[4],data[5],data[6],data[7]}));
				Integer ylh=Integer.parseInt(new String(new byte[]{data[8],data[9],data[10],data[11],data[12]}));
				
				boolean dw =false;
				
				if("前".equals(zw)) {
					dw=qzdw;
				}
				
				if("后".equals(zw)||"驱动".equals(zw)) {
					dw=hzdw;
				}
				
				if (dw) {
					this.display.sendMessage( zw+"轴称重已到位", DeviceDisplay.SP);
					this.display.sendMessage(zlh+"KG/"+ylh + "KG",
							DeviceDisplay.XP);
					i++;
				} else {
					this.display.sendMessage(vehCheckLogin.getHphm(), DeviceDisplay.SP);
					this.display.sendMessage(zw+"轴称重请到位", DeviceDisplay.XP);
					i = 0;
				}

				if (i >= 12) {
					break;
				}

				Thread.sleep(500);
			}
			
			deviceWeigh.sendMessage(sdcz);
			logger.info("称重结果锁定："+CharUtil.byte2HexOfString(this.getDevData(new byte[4])));
			deviceWeigh.sendMessage(dqsj);
			byte[] data = this.getDevData(new byte[19],A);
			
			deviceWeigh.sendMessage(ljml);
			logger.info("称重结果累加："+CharUtil.byte2HexOfString(this.getDevData(new byte[4])));
			
			Integer zlh=Integer.parseInt(new String(new byte[]{data[3],data[4],data[5],data[6],data[7]}));
			Integer ylh=Integer.parseInt(new String(new byte[]{data[8],data[9],data[10],data[11],data[12]}));
			
			logger.info(zw+"轴称重结束"+(zlh + ylh));
			this.display.sendMessage(zw+"轴称重结束", DeviceDisplay.SP);
			this.display.sendMessage((zlh + ylh) + "KG", DeviceDisplay.XP);
			
			return (zlh + ylh);
		}catch (Exception e) {
			logger.error("整备质量检测异常",e);
			throw e;
		}
	
	}
	
	

	private void createNew() {
		this.brakRollerData = new BrakRollerData();
	}

	
	@Override
	public void init(DeviceWeigh deviceWeigh) {
		super.init(deviceWeigh);
	}

}
