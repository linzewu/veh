package com.xs.veh.network.driver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.xs.common.CharUtil;
import com.xs.common.exception.SystemException;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.AbstractDeviceLight;
import com.xs.veh.network.DeviceDisplay;
import com.xs.veh.network.DeviceLight;
import com.xs.veh.network.TakePicture;
import com.xs.veh.network.data.LightData;

import groovy.lang.GroovyObject;
import groovy.util.GroovyScriptEngine;

public class DeviceLightDriverOfNHD6108 extends AbstractDeviceLight {

	static Logger logger = Logger.getLogger(DeviceLightDriverOfNHD6108.class);

	private List<String> leftMessageList = new ArrayList<String>();
	private List<String> rightMessageList = new ArrayList<String>();

	private List<Byte> dataList = new ArrayList<Byte>();

	// 正常
	public static Integer ZT_ZC = 0;

	// 无灯
	public static Integer ZT_WD = 1;

	// 出错
	public static Integer ZT_CC = 2;

	public boolean isIllegal = false;
	
	private VehCheckLogin vehCheckLogin;

	@Override
	public void sysSetting() {
	}
	
	public static void main(String[] age) {
		byte[] db=new byte[] {0b0,0b0,0b0,0b0,0b0,0b1,0b0,0b1};
		
		String strDB = Integer.toHexString(Integer.parseInt(CharUtil.byte2String(db),2));
		
		if(strDB.length()<2) {
			strDB="0"+strDB;
		}
		
		String comm="020503005436"+strDB;
		
		comm=comm+ CharUtil.getCheckSum2(comm.substring(2))+"03";
		
		logger.info("灯光检测命令："+comm);
	}

	@Override
	public List<LightData> startCheck(final VehCheckLogin vehCheckLogin, List<VehFlow> vheFlows)
			throws IOException, InterruptedException, SystemException {
		
		try {
			logger.info("南华6108灯光仪开始检测");
			
			this.vehCheckLogin=vehCheckLogin;
			
			String qzdz = vehCheckLogin.getQzdz();
			// 设置检测参数
			//setting(vehCheckLogin, vheFlows);
			reset();
			
			logger.info("重置完成，等待到位");
			// 等待到位
			dw(vehCheckLogin);
			
			logger.info("到位完成，开始检测");
			
			if(qzdz.equals("01")) {
				logger.info("设置成4灯制 46");
				this.deviceLight.sendMessage(new byte[] {0x46});
				logger.info("返回命令："+CharUtil.byte2HexOfString(getDevData(new byte[1])));
				logger.info("4灯远近都测  45");
				this.deviceLight.sendMessage(new byte[] {0x45});
				logger.info("返回命令："+CharUtil.byte2HexOfString(getDevData(new byte[1])));
			}
			
			if(qzdz.equals("02")) {
				logger.info("设置成4灯制 46");
				this.deviceLight.sendMessage(new byte[] {0x46});
				logger.info("返回命令："+CharUtil.byte2HexOfString(getDevData(new byte[1])));
				logger.info("4灯远近都测  43");
				this.deviceLight.sendMessage(new byte[] {0x43});
				logger.info("返回命令："+CharUtil.byte2HexOfString(getDevData(new byte[1])));
			}
			checkVolume();
			if(qzdz.equals("03")) {
				logger.info("设置成2灯制 44");
				this.deviceLight.sendMessage(new byte[] {0x44});
				logger.info("返回命令："+CharUtil.byte2HexOfString(getDevData(new byte[1])));
				logger.info("2灯远近都测  45");
				this.deviceLight.sendMessage(new byte[] {0x45});
				logger.info("返回命令："+CharUtil.byte2HexOfString(getDevData(new byte[1])));
			}
			
			if(qzdz.equals("04")) {
				logger.info("设置成2灯制 44");
				this.deviceLight.sendMessage(new byte[] {0x44});
				logger.info("返回命令："+CharUtil.byte2HexOfString(getDevData(new byte[1])));
				logger.info("2灯近关  4B");
				this.deviceLight.sendMessage(new byte[] {0x4B});
				logger.info("返回命令："+CharUtil.byte2HexOfString(getDevData(new byte[1])));
			}
			
			if(qzdz.equals("05")) {
				logger.info("设置成2灯制 44");
				this.deviceLight.sendMessage(new byte[] {0x44});
				logger.info("返回命令："+CharUtil.byte2HexOfString(getDevData(new byte[1])));
				logger.info("2灯远关  43");
				this.deviceLight.sendMessage(new byte[] {0x43});
				logger.info("返回命令："+CharUtil.byte2HexOfString(getDevData(new byte[1])));
			}
			
			
			List<String> jyxmArray=new ArrayList<String>();
			
			for (VehFlow vehFlow : vheFlows) {
				jyxmArray.add(vehFlow.getJyxm());		
			}
			
			boolean lefFlag=false;
			boolean rigthFlag = false;
			
			lefFlag=jyxmArray.contains("H1")||jyxmArray.contains("H2");
			rigthFlag=jyxmArray.contains("H3")||jyxmArray.contains("H4");
			
			
			if(lefFlag&&rigthFlag) {
				logger.info("自动检测 41H");
				this.deviceLight.sendMessage(new byte[] {0x41});
				logger.info("返回命令："+CharUtil.byte2HexOfString(getDevData(new byte[1])));
			}else if(lefFlag) {
				logger.info("左灯自动检测 5AH");
				this.deviceLight.sendMessage(new byte[] {0x5A});
				logger.info("返回命令："+CharUtil.byte2HexOfString(getDevData(new byte[1])));
			}else if(rigthFlag) {
				logger.info("右灯自动检测 59H");
				this.deviceLight.sendMessage(new byte[] {0x59});
				logger.info("返回命令："+CharUtil.byte2HexOfString(getDevData(new byte[1])));
			}
			
			boolean checking=true;
		
			if(jyxmArray.contains("H2")) {
				TakePicture.createNew(this.deviceLight.getVehCheckLogin(), "H2",1000);
				while(checking) {
					String rt = CharUtil.byte2HexOfString(getDevData(new byte[1]));
					logger.info("返回命令："+ rt);
					
					if("55".equals(rt)) {
						this.deviceLight.getDisplay().sendMessage("请开启左副灯", DeviceDisplay.XP);
						
					}
					
					if("4F".equals(rt)) {
						break;
					}
				}
				this.deviceLight.getDisplay().sendMessage("左副灯检测结束", DeviceDisplay.XP);
			}
			
				
			if(jyxmArray.contains("H1")) {
				TakePicture.createNew(this.deviceLight.getVehCheckLogin(), "H1",1000);
				while(checking) {
					String rt = CharUtil.byte2HexOfString(getDevData(new byte[1]));
					logger.info("返回命令："+ rt);
					if("56".equals(rt)) {
						this.deviceLight.getDisplay().sendMessage("检测左主远光灯,请开启远光灯", DeviceDisplay.XP);
					}
					
					if("58".equals(rt)) {
						this.deviceLight.getDisplay().sendMessage("检测左主近光灯,请开启近光灯", DeviceDisplay.XP);
					}
					if("4F".equals(rt)) {
						break;
					}
				}
				this.deviceLight.getDisplay().sendMessage("左主灯检测结束", DeviceDisplay.XP);
			}
			
			if(jyxmArray.contains("H3")) {
				TakePicture.createNew(this.deviceLight.getVehCheckLogin(), "H3",1000);
				while(checking) {
					String rt = CharUtil.byte2HexOfString(getDevData(new byte[1]));
					logger.info("返回命令："+ rt);
					
					if("48".equals(rt)) {
						this.deviceLight.getDisplay().sendMessage("检测右副灯,请开启右副灯", DeviceDisplay.XP);
					}
					
					if("4F".equals(rt)) {
						break;
					}
				}
			}
				
			if(jyxmArray.contains("H4")) {
				TakePicture.createNew(this.deviceLight.getVehCheckLogin(), "H4",1000);
				while(checking) {
					String rt = CharUtil.byte2HexOfString(getDevData(new byte[1]));
					logger.info("返回命令："+ rt);
					
					if("49".equals(rt)) {
						this.deviceLight.getDisplay().sendMessage("检测右主远光灯,请打开远光灯", DeviceDisplay.XP);
					}
					
					if("4A".equals(rt)) {
						this.deviceLight.getDisplay().sendMessage("检测右主近光灯,请打开近光灯", DeviceDisplay.XP);
					}
					
					if("4F".equals(rt)) {
						break;
					}
				}
			}
			
			
			if(jyxmArray.contains("H1")) {
				logger.info("取左主灯数据(四灯制)或取左灯数据(两灯制)命令");
				this.deviceLight.sendMessage(new byte[] {0x4D});
				byte[] bd = getDevData(new byte[34]);
				
				logger.info("取左主灯灯高70");
				this.deviceLight.sendMessage(new byte[] {0x70});
				byte[] bd2= getDevData(new byte[9]);
				
				String ygspcc= new String(new byte[] {bd[2],bd[3],bd[4],bd[5],bd[6]});
				String ygczpc = new String(new byte[] {bd[7],bd[8],bd[9],bd[10],bd[11]});
				Integer yggq = Integer.parseInt(new String(new byte[] {bd[12],bd[13],bd[14],bd[15]}));
				Integer ygdg =null;
				try {
					ygdg = Integer.parseInt(new String(new byte[] {bd2[1],bd2[2],bd2[3]}));
				}catch (Exception e) {
					ygdg=1;
				}
				
				
				logger.info("ygspcc="+ygspcc);
				logger.info("ygczpc="+ygczpc);
				
				Integer jgdg= Integer.parseInt(new String(new byte[] {bd[16],bd[17],bd[18]}));
				String jgspcc= new String(new byte[] {bd[19],bd[20],bd[21],bd[22],bd[23]});
				String jgczpc = new String(new byte[] {bd[24],bd[25],bd[26],bd[27],bd[28]});
				//Integer jggq = Integer.parseInt(new String(new byte[] {bd[24],bd[25],bd[26],bd[27]}));
				
				logger.info("jgspcc="+jgspcc);
				logger.info("jgczpc="+jgczpc);
				
				LightData  data =new LightData();
				data.setWz(LightData.WZ_Z);
				data.setDx(LightData.DX_ZD);
				data.setGx(LightData.GX_YGD);
				data.setSppc(ygspcc);
				data.setCzpc(ygczpc);
				data.setGq(yggq*100);
				data.setDg(ygdg==0?1:ygdg*10);
				data=extend(data);
				lightDatas.add(data);
				
				LightData  data2 =new LightData();
				data2.setWz(LightData.WZ_Z);
				data2.setDx(LightData.DX_ZD);
				data2.setGx(LightData.GX_JGD);
				data2.setSppc(jgspcc);
				data2.setCzpc(jgczpc);
				data2.setGq(0);
				data2.setDg(jgdg==0?1:jgdg*10);
				data2=extend(data2);
				lightDatas.add(data);
				
			}
			
			if(jyxmArray.contains("H4")) {
				logger.info("取右主灯数据(四灯制)或取右灯数据(两灯制)命令。");
				this.deviceLight.sendMessage(new byte[] {0x4E});
				byte[] bd = getDevData(new byte[34]);
				
				logger.info("取左主灯灯高71");
				this.deviceLight.sendMessage(new byte[] {0x71});
				byte[] bd2= getDevData(new byte[9]);
				
				String ygspcc= new String(new byte[] {bd[2],bd[3],bd[4],bd[5],bd[6]});
				String ygczpc = new String(new byte[] {bd[7],bd[8],bd[9],bd[10],bd[11]});
				Integer yggq = Integer.parseInt(new String(new byte[] {bd[12],bd[13],bd[14],bd[15]}));
				Integer ygdg =null;
				try {
					ygdg = Integer.parseInt(new String(new byte[] {bd2[1],bd2[2],bd2[3]}));
				}catch (Exception e) {
					ygdg=1;
				}
				
				Integer jgdg= Integer.parseInt(new String(new byte[] {bd[16],bd[17],bd[18]}));
				String jgspcc= new String(new byte[] {bd[19],bd[20],bd[21],bd[22],bd[23]});
				String jgczpc = new String(new byte[] {bd[24],bd[25],bd[26],bd[27],bd[28]});
				//Integer jggq = Integer.parseInt(new String(new byte[] {bd[24],bd[25],bd[26],bd[27]}));
				
				LightData  data =new LightData();
				data.setWz(LightData.WZ_Y);
				data.setDx(LightData.DX_ZD);
				data.setGx(LightData.GX_YGD);
				data.setSppc(ygspcc);
				data.setCzpc(ygczpc);
				data.setGq(yggq*100);
				data.setDg(ygdg==0?1:ygdg*10);
				data=extend(data);
				lightDatas.add(data);
				
				LightData  data2 =new LightData();
				data2.setWz(LightData.WZ_Y);
				data2.setDx(LightData.DX_ZD);
				data2.setGx(LightData.GX_JGD);
				data2.setSppc(jgspcc);
				data2.setCzpc(jgczpc);
				data2.setGq(0);
				data2.setDg(jgdg==0?1:jgdg*10);
				data2=extend(data2);
				lightDatas.add(data);
				
				
				
			}
			
			if(jyxmArray.contains("H2")) {
				logger.info("取左副灯数据命令.");
				this.deviceLight.sendMessage(new byte[] {0x4F});
				byte[] bd = getDevData(new byte[34]);
				
				String ygspcc= new String(new byte[] {bd[2],bd[3],bd[4],bd[5],bd[6]});
				String ygczpc = new String(new byte[] {bd[7],bd[8],bd[9],bd[10],bd[11]});
				Integer yggq = Integer.parseInt(new String(new byte[] {bd[12],bd[13],bd[14],bd[15]}));
				
				Integer jgdg= Integer.parseInt(new String(new byte[] {bd[16],bd[17],bd[18]}));
				
				LightData  data =new LightData();
				data.setWz(LightData.WZ_Z);
				data.setDx(LightData.DX_FD);
				data.setGx(LightData.GX_YGD);
				data.setSppc(ygspcc);
				data.setCzpc(ygczpc);
				data.setGq(yggq*100);
				data.setDg(jgdg==0?1:jgdg*10);
				data=extend(data);
				lightDatas.add(data);
			}
			
			
			if(jyxmArray.contains("H3")) {
				logger.info("取右副灯数据命令。");
				this.deviceLight.sendMessage(new byte[] {0x50});
				byte[] bd = getDevData(new byte[34]);
				
				String ygspcc= new String(new byte[] {bd[2],bd[3],bd[4],bd[5],bd[6]});
				String ygczpc = new String(new byte[] {bd[7],bd[8],bd[9],bd[10],bd[11]});
				Integer yggq = Integer.parseInt(new String(new byte[] {bd[12],bd[13],bd[14],bd[15]}));
				
				Integer jgdg= Integer.parseInt(new String(new byte[] {bd[16],bd[17],bd[18]}));
				
				LightData  data =new LightData();
				data.setWz(LightData.WZ_Y);
				data.setDx(LightData.DX_FD);
				data.setGx(LightData.GX_YGD);
				data.setSppc(ygspcc);
				data.setCzpc(ygczpc);
				data.setGq(yggq*100);
				data.setDg(jgdg==0?1:jgdg*10);
				data=extend(data);
				lightDatas.add(data);
			}
			
			
			// 仪器归位
		//	this.deviceLight.sendMessage(mYqgw);
			// 取数据
		//	this.deviceLight.sendMessage(gQqbclsj);


			return lightDatas;
		}catch (Exception e) {
			Thread.sleep(1000*10);
			logger.error("灯光检测异常",e);
			throw new SystemException("灯光检测异常",e);
		}
		
		

	}
	
	
	private void checkVolume() throws IOException, InterruptedException {
		
		boolean f1 = vehCheckLogin.getJyxm().indexOf("VL")>=0;
		logger.info("声级计：f1="+f1+"vehCheckLogin.getJycs()="+vehCheckLogin.getJycs()+"object="+deviceLight.getDeviceVolume());
		if(f1&&deviceLight.getDeviceVolume()!=null) {
			deviceLight.getDisplay().sendMessage("按喇叭3秒", deviceLight.getDisplay().SP);
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						deviceLight.getDeviceVolume().startCheck(vehCheckLogin);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}catch (Exception e) {
						e.printStackTrace();
						logger.error("声级计检测异常！",e);
					}
				}
			}).start();
			Thread.sleep(4000);
		}
	}


	
	private LightData extend(LightData lightData) {
		File file=new File("Z:\\extend.groovy");
		if(file.exists()) {
			logger.info("进入扩展！");
			try {
				lightData.setDgpdxz(vehCheckLogin);
				GroovyScriptEngine engine = new GroovyScriptEngine("Z:\\");
				Class scriptClass = engine.loadScriptByName("extend.groovy");
				GroovyObject scriptInstance = (GroovyObject)scriptClass.newInstance();
				LightData ret = (LightData)scriptInstance.invokeMethod("extend", new Object[]{lightData});
				return ret;
			}catch (Exception e) {
				logger.info("扩展类执行异常",e);
			}
		}
		return lightData;
		
		
	}

	private boolean isEmptyData(byte[] highData) {

		if (new String(highData).equals("000000000000000000")) {
			return true;
		}
		return false;
	}


	private void dw(VehCheckLogin vehCheckLogin) throws IOException, InterruptedException,SystemException {
		try {
			String hphm = vehCheckLogin.getHphm();
			int i = 0;
			this.deviceLight.getDisplay().sendMessage(hphm, DeviceDisplay.SP);
			this.deviceLight.getDisplay().sendMessage("前进准备检测灯光", DeviceDisplay.XP);
			while (true) {
				if (deviceSignal1.getSignal(s1) && !deviceSignal2.getSignal(s2)) {
					i++;
					if (i == 1) {
						this.deviceLight.getDisplay().sendMessage(hphm, DeviceDisplay.SP);
						this.deviceLight.getDisplay().sendMessage("停止", DeviceDisplay.XP);
					}
				}

				if (!deviceSignal1.getSignal(s1)) {
					this.deviceLight.getDisplay().sendMessage(hphm, DeviceDisplay.SP);
					this.deviceLight.getDisplay().sendMessage("前进准备检测灯光", DeviceDisplay.XP);
					i = 0;
				} else if (deviceSignal2.getSignal(s2)) {
					this.deviceLight.getDisplay().sendMessage(hphm, DeviceDisplay.SP);
					this.deviceLight.getDisplay().sendMessage("退后", DeviceDisplay.XP);
					i = 0;
				}
				if (i >= 6) {
					break;
				}
				Thread.sleep(500);
			}
		}catch (Exception e) {
			throw new SystemException("灯光仪器等待到位异常",e);
		}
		
	}

	private void reset() {
		isIllegal = false;
		leftMessageList.clear();
		rightMessageList.clear();
		dataList.clear();
		getTemp().clear();
	}

	


	@Override
	public void setDeviceLight(DeviceLight deviceLight) {
		this.deviceLight = deviceLight;
	}

}
