package com.xs.veh.network;

import java.io.IOException;
import java.util.Map;

/**
 * 灯光仪解码器接口
 * 
 * @author linze
 *
 */
public abstract class DeviceLightDecode {

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
		lowBeamOfMainEnd
	}

	public enum DW {
		/**
		 * 右灯
		 */
		R,
		/**
		 * 左灯
		 */
		L,
	}

	protected DeviceLight deviceLight;

	// 主远光灯
	protected LightData highBeamOfMainLeft;

	protected LightData highBeamOfMainRigth;

	// 主近光灯
	protected LightData lowBeamOfMainLeft;

	protected LightData lowBeamOfMainRigth;

	// 副远光灯
	protected LightData highBeamOfSideLeft;

	protected LightData highBeamOfSideRigth;

	public abstract void sysSetting() throws IOException, InterruptedException;

	public abstract void startCheck(String clzd, String clyd) throws IOException, InterruptedException;

	public abstract void device2pc(byte[] data) throws IOException;

	public abstract void setDeviceLight(DeviceLight deviceLight);

	public abstract Map<String, String> createSettingData();

	public void clear() {
		highBeamOfMainLeft = null;
		highBeamOfSideRigth = null;

		lowBeamOfMainLeft = null;
		lowBeamOfMainRigth = null;

		highBeamOfSideLeft = null;
		highBeamOfSideRigth = null;
	}

}
