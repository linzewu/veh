package com.xs.veh.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.xs.common.CharUtil;
import com.xs.common.exception.SystemException;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.network.data.LightData;

/**
 * 灯光仪解码器接口
 * 
 * @author linze
 *
 */
public abstract class AbstractDeviceLight {
	
	Logger logger = Logger.getLogger(AbstractDeviceLight.class);
	
	private Date kssj;

	public enum DX {
		/**
		 * 二灯制
		 */
		EDZ,
		/**
		 * 四等制
		 */
		SDZ
	}

	public enum GX {

		/**
		 * 单侧远光
		 */
		DCYG,

		/**
		 * 单侧近光
		 */
		DCJG,

		/**
		 * 远近光均测
		 */
		YJGJC
	}

	public enum YGTZ {
		/**
		 * 远光单独调整
		 */
		YG_YES,

		/**
		 * 远光不调整
		 */
		YG_NO

	}

	public enum JGTZ {
		/**
		 * 近光调整
		 */
		JG_YES,

		/**
		 * 近光不调整
		 */
		JG_NO
	}

	public enum LightNoticeType {
		/**
		 * 主远光灯测量结束
		 */
		highBeamOfMainEnd,
		/**
		 * 副远光灯测量结束
		 */
		highBeamOfSideEnd,

		/**
		 * 近关灯测量结束
		 */
		lowBeamOfMainEnd,
		
		/**
		 * 灯光归位
		 */
		deviceBreak,
		
		/**
		 * 灯光错误
		 */
		beamError,
		
		/**
		 * 无光
		 */
		noBeam
	}
	
	


	protected DeviceLight deviceLight;

	protected List<LightData> lightDatas;
	
	protected DeviceSignal deviceSignal1;

	protected DeviceSignal deviceSignal2;

	protected Integer s1;

	protected Integer s2;
	
	protected Integer kwfx;
	

	public abstract void sysSetting() throws IOException, InterruptedException;

	public abstract List<LightData> startCheck(VehCheckLogin vehCheckLogin, List<VehFlow> vheFlows) throws IOException, InterruptedException, SystemException;

	public void device2pc(byte[] data) throws IOException, InterruptedException{
		for (byte b : data) {
			temp.add(b);
		}
	};

	public abstract void setDeviceLight(DeviceLight deviceLight);

	public void createNewList() {
		
		if(lightDatas!=null){
			lightDatas.clear();
		}
		kssj=null;
		lightDatas=new ArrayList<LightData>();
		
	}

	public Date getKssj() {
		return kssj;
	}

	public void setKssj(Date kssj) {
		this.kssj = kssj;
	}
	
	public byte[] getDevData(byte[] contex) throws InterruptedException {
		for (int i = 0; i < contex.length; i++) {
			while (temp.isEmpty()) {
				Thread.sleep(50);
			}
			contex[i] = temp.remove(0);
		}

		return contex;
	}
	
	public byte[] getDevData(byte[] contex, byte beginByte) throws InterruptedException {
		while (temp.isEmpty()) {
			Thread.sleep(50);
		}
		while (temp.isEmpty()||temp.remove(0)!=beginByte) {
			Thread.sleep(10);
		}
		
		contex[0]=beginByte;
		for (int i = 1; i < contex.length; i++) {
			while (temp.isEmpty()) {
				Thread.sleep(50);
			}
			contex[i] = temp.remove(0);
		}

		return contex;
	}
	
	private List<Byte> temp = new LinkedList<Byte>();

	public List<Byte> getTemp() {
		return temp;
	}
	
	
	
}
