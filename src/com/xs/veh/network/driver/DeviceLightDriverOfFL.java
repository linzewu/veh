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

public class DeviceLightDriverOfFL extends AbstractDeviceLight {

	static Logger logger = Logger.getLogger(DeviceLightDriverOfFL.class);

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
			logger.info("福利灯光仪开始检测");
			
			this.vehCheckLogin=vehCheckLogin;
			
			String qzdz = vehCheckLogin.getQzdz();
			// 设置检测参数
			//setting(vehCheckLogin, vheFlows);
			reset();
			
			logger.info("重置完成，等待到位");
			// 等待到位
			dw(vehCheckLogin);
			
			logger.info("到位完成，开始检测");
			
			
			byte[] db=new byte[] {0b0,0b0,0b0,0b0,0b0,0b0,0b0,0b0};
			for (VehFlow vehFlow : vheFlows) {
				if(qzdz.equals("01")) {
					if(vehFlow.getJyxm().equals("H1")) {
						db[1]=0b1;
						db[3]=0b1;
					}
					if(vehFlow.getJyxm().equals("H2")) {
						db[2]=0b1;
					}
					
					if(vehFlow.getJyxm().equals("H3")) {
						db[6]=0b1;
					}
					if(vehFlow.getJyxm().equals("H4")) {
						db[5]=0b1;
						db[7]=0b1;
					}
				}
				
				if(qzdz.equals("02")) {
					if(vehFlow.getJyxm().equals("H1")) {
						db[1]=0b1;
					}
					if(vehFlow.getJyxm().equals("H2")) {
						db[2]=0b1;
					}
					
					if(vehFlow.getJyxm().equals("H3")) {
						db[6]=0b1;
					}
					if(vehFlow.getJyxm().equals("H4")) {
						db[5]=0b1;
					}
				}
				
				if(qzdz.equals("03")) {
					if(vehFlow.getJyxm().equals("H1")) {
						db[1]=0b1;
						db[3]=0b1;
					}
					
					if(vehFlow.getJyxm().equals("H4")) {
						db[5]=0b1;
						db[7]=0b1;
					}
				}
				
				if(qzdz.equals("04")) {
					if(vehFlow.getJyxm().equals("H1")) {
						db[3]=0b1;
					}
					
					if(vehFlow.getJyxm().equals("H4")) {
						db[7]=0b1;
					}
				}
				
			}
			
			String strDB = Integer.toHexString(Integer.parseInt(CharUtil.byte2String(db),2));
			if(strDB.length()<2) {
				strDB="0"+strDB;
			}
			String comm="020503005436"+strDB;
			
			comm=comm+ CharUtil.getCheckSum2(comm.substring(2))+"03";
			
			logger.info("灯光检测命令："+comm);
			
			this.deviceLight.sendMessage(comm);
			
			this.setKssj(new Date());

			
			byte[] jcfh= getDevData(new byte[7]);
			
			logger.info("检测返回命令："+CharUtil.byte2HexOfString(jcfh));
			
			int i=0;
			if(jcfh[4]==0x41) {
				
				if(db[1]==0b1) {
					i++;
					i++;
				}
				if(db[2]==0b1) {
					i++;
					i++;
				}
				if(db[3]==0b1) {
					i++;
				}
				
				if(db[5]==0b1) {
					i++;
					i++;
				}
				if(db[6]==0b1) {
					i++;
					i++;
				}
				if(db[7]==0b1) {
					i++;
				}
				logger.info("需等待响应命令个数："+i);
				
				for(int j=0;j<i;j++) {
					
					byte[] fkxx = getDevData(new byte[8]);
					
					String message = new String(new byte[] {fkxx[4],fkxx[5]});
					
					logger.info("仪器检测过程响应："+message);
					
					message=message.substring(1);
					
					if(message.equals("1")) {
						this.deviceLight.getDisplay().sendMessage("请开启主远光灯,测量左主远光灯", DeviceDisplay.XP);
						TakePicture.createNew(this.deviceLight.getVehCheckLogin(), "H1",1000);
					}else if(message.equals("2")) {
						this.deviceLight.getDisplay().sendMessage("请开启主近光灯,测量左主近光灯", DeviceDisplay.XP);
					}else if(message.equals("3")) {
						this.deviceLight.getDisplay().sendMessage("请开启副远光灯,测量左副远光灯", DeviceDisplay.XP);
						TakePicture.createNew(this.deviceLight.getVehCheckLogin(), "H2",1000);
					}else if(message.equals("4")) {
						this.deviceLight.getDisplay().sendMessage("请开启主远光灯,测量右主远光灯", DeviceDisplay.XP);
						TakePicture.createNew(this.deviceLight.getVehCheckLogin(), "H4",5000);
					}else if(message.equals("5")) {
						this.deviceLight.getDisplay().sendMessage("请开启主近光灯,测量右主近光灯", DeviceDisplay.XP);
					}else if(message.equals("6")) {
						this.deviceLight.getDisplay().sendMessage("请开启副远光灯,测量右副远光灯", DeviceDisplay.XP);
						TakePicture.createNew(this.deviceLight.getVehCheckLogin(), "H3",1000);
					}else if(message.equals("7")) {
						this.deviceLight.getDisplay().sendMessage("左主灯检测完成", DeviceDisplay.XP);
					}else if(message.equals("8")) {
						this.deviceLight.getDisplay().sendMessage("左副灯检测完成", DeviceDisplay.XP);
					}else if(message.equals("9")) {
						this.deviceLight.getDisplay().sendMessage("右主灯检测完成", DeviceDisplay.XP);
					}else if(message.equals("a")) {
						this.deviceLight.getDisplay().sendMessage("右副灯检测完成", DeviceDisplay.XP);
					}
				}
				
				byte[] endZCom = getDevData(new byte[7]);
				
				logger.info("仪器检测结束命令："+CharUtil.byte2HexOfString(endZCom));
				
				if(endZCom[4]==0x54) {
					this.deviceLight.getDisplay().sendMessage("检测完成，获取检测数据", DeviceDisplay.XP);
					if(db[1]==0b1||db[3]==0b1) {
						logger.info("发送取数命令：02050200444C9703");
						this.deviceLight.sendMessage("02050200644CB703");
						byte[] ds = getDevData(new byte[53]);
						logger.info("左主灯返回数据："+CharUtil.byte2HexOfString(ds));
						if(db[1]==0b1) {
							String sppc=new String(new byte[] {ds[6],ds[7],ds[8],ds[9],ds[10],ds[11]}).trim();
							String czpc=new String(new byte[] {ds[12],ds[13],ds[14],ds[15],ds[16],ds[17]}).trim();
							Integer gq=Integer.parseInt(new String(new byte[] {ds[18],ds[19],ds[20],ds[21],ds[22]}).trim());
							Integer dg = Integer.parseInt(new String(new byte[] {ds[23],ds[24],ds[25],ds[26],ds[27]}).trim());
							LightData  data =new LightData();
							data.setWz(LightData.WZ_Z);
							data.setDx(LightData.DX_ZD);
							data.setGx(LightData.GX_YGD);
							data.setSppc(sppc);
							data.setCzpc(czpc);
							data.setGq(gq*100);
							data.setDg(dg==0?1:dg);
							data=extend(data);
							lightDatas.add(data);
						}
						if(db[3]==0b1) {
							String sppc=new String(new byte[] {ds[28],ds[29],ds[30],ds[31],ds[32],ds[33]}).trim();
							String czpc=new String(new byte[] {ds[34],ds[35],ds[36],ds[37],ds[38],ds[39]}).trim();
							//String dg=new String(new byte[] {ds[40],ds[41],ds[42],ds[43],ds[44]}).trim();
							Integer dg = Integer.parseInt(new String(new byte[] {ds[45],ds[46],ds[47],ds[48],ds[49]}).trim());
							LightData  data =new LightData();
							data.setWz(LightData.WZ_Z);
							data.setDx(LightData.DX_ZD);
							data.setGx(LightData.GX_JGD);
							data.setSppc(sppc);
							data.setCzpc(czpc);
							data.setDg(dg==0?1:dg);
							data=extend(data);
							lightDatas.add(data);
						}
						
					}
					if(db[2]==0b1) {
						
						this.deviceLight.sendMessage("02050200646CD703");
						byte[] ds = getDevData(new byte[53]);
						logger.info("左副灯返回数据："+CharUtil.byte2HexOfString(ds));
						String sppc=new String(new byte[] {ds[6],ds[7],ds[8],ds[9],ds[10],ds[11]}).trim();
						String czpc=new String(new byte[] {ds[12],ds[13],ds[14],ds[15],ds[16],ds[17]}).trim();
						Integer gq=Integer.parseInt(new String(new byte[] {ds[18],ds[19],ds[20],ds[21],ds[22]}).trim());
						Integer dg = Integer.parseInt(new String(new byte[] {ds[23],ds[24],ds[25],ds[26],ds[27]}).trim());
						LightData  data =new LightData();
						data.setWz(LightData.WZ_Z);
						data.setDx(LightData.DX_FD);
						data.setGx(LightData.GX_YGD);
						data.setSppc(sppc);
						data.setCzpc(czpc);
						data.setGq(gq*100);
						data.setDg(dg==0?1:dg);
						data=extend(data);
						lightDatas.add(data);
						
					}
					
					
					if(db[5]==0b1||db[7]==0b1) {
						
						this.deviceLight.sendMessage("020502006452BD03");
						byte[] ds = getDevData(new byte[53]);
						logger.info("右主灯返回数据："+CharUtil.byte2HexOfString(ds));
						if(db[5]==0b1) {
							String sppc=new String(new byte[] {ds[6],ds[7],ds[8],ds[9],ds[10],ds[11]}).trim();
							String czpc=new String(new byte[] {ds[12],ds[13],ds[14],ds[15],ds[16],ds[17]}).trim();
							Integer gq=Integer.parseInt(new String(new byte[] {ds[18],ds[19],ds[20],ds[21],ds[22]}).trim());
							Integer dg = Integer.parseInt(new String(new byte[] {ds[23],ds[24],ds[25],ds[26],ds[27]}).trim());
							LightData  data =new LightData();
							data.setWz(LightData.WZ_Y);
							data.setDx(LightData.DX_ZD);
							data.setGx(LightData.GX_YGD);
							data.setSppc(sppc);
							data.setCzpc(czpc);
							data.setGq(gq*100);
							data.setDg(dg==0?1:dg);
							data=extend(data);
							lightDatas.add(data);
						}
						if(db[7]==0b1) {
							String sppc=new String(new byte[] {ds[28],ds[29],ds[30],ds[31],ds[32],ds[33]}).trim();
							String czpc=new String(new byte[] {ds[34],ds[35],ds[36],ds[37],ds[38],ds[39]}).trim();
							//String dg=new String(new byte[] {ds[40],ds[41],ds[42],ds[43],ds[44]}).trim();
							Integer dg = Integer.parseInt(new String(new byte[] {ds[45],ds[46],ds[47],ds[48],ds[49]}).trim());
							LightData  data =new LightData();
							data.setWz(LightData.WZ_Y);
							data.setDx(LightData.DX_ZD);
							data.setGx(LightData.GX_JGD);
							data.setSppc(sppc);
							data.setCzpc(czpc);
							data.setDg(dg==0?1:dg);
							data=extend(data);
							lightDatas.add(data);
						}
						
					}
					if(db[6]==0b1) {
						
						this.deviceLight.sendMessage("020502006472DD03");
						byte[] ds = getDevData(new byte[53]);
						logger.info("右副灯返回数据："+CharUtil.byte2HexOfString(ds));
						String sppc=new String(new byte[] {ds[6],ds[7],ds[8],ds[9],ds[10],ds[11]}).trim();
						String czpc=new String(new byte[] {ds[12],ds[13],ds[14],ds[15],ds[16],ds[17]}).trim();
						Integer gq=Integer.parseInt(new String(new byte[] {ds[18],ds[19],ds[20],ds[21],ds[22]}).trim());
						Integer dg = Integer.parseInt(new String(new byte[] {ds[23],ds[24],ds[25],ds[26],ds[27]}).trim());
						LightData  data =new LightData();
						data.setWz(LightData.WZ_Y);
						data.setDx(LightData.DX_FD);
						data.setGx(LightData.GX_YGD);
						data.setSppc(sppc);
						data.setCzpc(czpc);
						data.setGq(gq*100);
						data.setDg(dg==0?1:dg);
						data=extend(data);
						lightDatas.add(data);
						
					}
				}
			}else {
				throw new SystemException("仪器响应检测异常");
			}
			
			// 仪器归位
		//	this.deviceLight.sendMessage(mYqgw);
			// 取数据
		//	this.deviceLight.sendMessage(gQqbclsj);


			return lightDatas;
		}catch (Exception e) {
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
		
		this.dwKssj=new Date();
		
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
