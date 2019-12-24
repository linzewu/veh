package com.xs.veh.network.driver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.xs.common.CharUtil;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.AbstractDeviceSuspension;
import com.xs.veh.network.DeviceDisplay;
import com.xs.veh.network.DeviceSuspension;
import com.xs.veh.network.SimpleRead.ProtocolType;
import com.xs.veh.network.data.SuspensionData;

/**
 * 汽车悬架装置
 * 
 * @author linzewu
 *
 */
public class DeviceSuspensionDriverOfJxxj extends AbstractDeviceSuspension {
	private static Logger logger = Logger.getLogger(DeviceSuspensionDriverOfJxxj.class);

	// 开始检测
	private String ksjc;
	// 取该次检测结果数据
	private String dqsj;
	// 对仪表进行清零
	private String ybql;
	
	private String jcjs="41046754";
	
	private List<Integer> lDatas=new ArrayList<Integer>();
	private List<Integer> rDatas=new ArrayList<Integer>();
	
	

	public ProtocolType getProtocolType(byte[] bs) {
		return ProtocolType.DATA;
	}

	private void createNew() {
		this.suspensionData = new SuspensionData();
		this.getTemp().clear();
	}

	@Override
	public List<SuspensionData> startCheck(VehCheckLogin vehCheckLogin, VehFlow vehFlow)
			throws IOException, InterruptedException {
		
		List<SuspensionData> array=new ArrayList<SuspensionData>();
		
		SuspensionData data1 = check(vehCheckLogin,vehFlow,1);
		
		this.display.sendMessage("前轴完毕向前行驶", DeviceDisplay.XP);
		boolean flag = true;
		while (flag) {
			flag = this.signal.getSignal(s1);
			Thread.sleep(200);
		}
		SuspensionData data2 = check(vehCheckLogin,vehFlow,2);
		
		array.add(data1);
		array.add(data2);
		return array;
	}
	
	
	public SuspensionData check(VehCheckLogin vehCheckLogin, VehFlow vehFlow,Integer zs)
			throws IOException, InterruptedException {
		
		String pf;
		
		if(zs==1) {
			pf="前轴";
		}else {
			pf="后轴";
		}
		
		lDatas.clear();
		rDatas.clear();
		this.getTemp().clear();
		// 仪表清零
		deviceSuspension.sendMessage(ybql);
		logger.info("发送清零命令:"+ybql);
		//logger.info("仪表清0返回："+CharUtil.byte2HexOfString(this.getDevData(new byte[4],(byte)0x41)));
		
		// 开始新的一次检测
		createNew();
//		this.display.sendMessage(vehCheckLogin.getHphm(), DeviceDisplay.SP);
//		this.display.sendMessage("前轴，请到位", DeviceDisplay.XP);
		dw(vehCheckLogin,pf);
		this.display.sendMessage(pf+"到位开始检测", DeviceDisplay.XP);
		deviceSuspension.sendMessage(ksjc);
		
		logger.info("开始检测返回："+CharUtil.byte2HexOfString(this.getDevData(new byte[4],(byte)0x41)));
		
		logger.info("检测左轮悬挂："+CharUtil.byte2HexOfString(this.getDevData(new byte[4],(byte)0x41)));
		this.display.sendMessage("检测左轮悬挂", DeviceDisplay.XP);
		//获取过程数据
		getProcessData();
		logger.info("检测右轮悬挂："+CharUtil.byte2HexOfString(this.getDevData(new byte[4],(byte)0x41)));
		this.display.sendMessage("检测右轮悬挂", DeviceDisplay.XP);
		//获取过程数据
		getProcessData();
		
		byte[] endCommon = this.getDevData(new byte[4],(byte)0x41);
		
		logger.info("检测结束命令：："+CharUtil.byte2HexOfString(endCommon));
		
		//发送读取数据命令
		deviceSuspension.sendMessage(dqsj);
		//this.display.sendMessage("右电机启动", DeviceDisplay.XP);
		
		
		byte[] data = this.getDevData(new byte[27],(byte)0x41);
		
		//左悬挂效率
		String zxgxl =new String(new byte[] {data[3],data[4],data[5],data[6],data[7]});
		//右悬挂效率
		String yxgxl =new String(new byte[] {data[8],data[9],data[10],data[11],data[12]});
		//左右差
		String zyc =new String(new byte[] {data[13],data[14],data[15],data[16],data[17]});
		//左静态轮种
		String zjtlz =new String(new byte[] {data[18],data[19],data[20],data[21]});
		//左静态轮种
		String yjtlz =new String(new byte[] {data[22],data[23],data[24],data[25]});
		
		
		suspensionData.setZxsl(zxgxl);
		suspensionData.setYxsl(yxgxl);
		suspensionData.setZjtlh(zjtlz);
		suspensionData.setYjtlh(yjtlz);
		suspensionData.setZyc(zyc);
		
		suspensionData.setZpd();
		
		String strpd = "X";
		if(suspensionData.getZpd()==SuspensionData.PDJG_HG) {
			 strpd = "O";
		}
		
		this.display.sendMessage("检测结果"+strpd, DeviceDisplay.SP);
		this.display.sendMessage(zxgxl+"/"+yxgxl+"/"+zyc, DeviceDisplay.XP);
		Thread.sleep(3000);
		
		return this.suspensionData;
	}
	
	
	
	
	
	

	private void getProcessData() throws InterruptedException {
		for(int i=0;i<320;i++){
			byte[] array = this.getDevData(new byte[8],(byte)0x41);
			if(0x4C==array[2]) {
				String strData = new String(new byte[]{array[3],array[4],array[5],array[6]});
				lDatas.add(Integer.parseInt(strData));
			}
			
			if(0x52==array[2]) {
				String strData = new String(new byte[]{array[3],array[4],array[5],array[6]});
				rDatas.add(Integer.parseInt(strData));
			}
			
		}
		
	}

	private void dw(VehCheckLogin vehCheckLogin,String pf) throws InterruptedException, IOException {
		int i = 0;
		while (true) {
			if (this.signal.getSignal(s1)) {
				this.display.sendMessage(vehCheckLogin.getHphm(), DeviceDisplay.SP);
				this.display.sendMessage(pf+"悬架已到位", DeviceDisplay.XP);
				i++;
			} else {
				this.display.sendMessage(vehCheckLogin.getHphm(), DeviceDisplay.SP);
				this.display.sendMessage(pf+"悬架请到位", DeviceDisplay.XP);
				i = 0;
			}
			if (i >= 6) {
				break;
			}
			Thread.sleep(500);
		}

		
		
	}

	@Override
	public void init(DeviceSuspension deviceSuspension) {
		super.init(deviceSuspension);
		ksjc="41046556";
		dqsj="41046853";
		ybql="41043388";

	}

}
