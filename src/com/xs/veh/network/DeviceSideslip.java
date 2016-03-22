package com.xs.veh.network;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.xs.common.CharUtil;
import com.xs.veh.entity.Device;
import com.xs.veh.manager.CheckDataManager;

import gnu.io.SerialPortEvent;

/**
 * 
 * @author linze
 *
 */
@Service("deviceSideslip")
@Scope("prototype")
public class DeviceSideslip extends SimpleRead {

	private IDeviceSideslipDecode sd;

	private SideslipData sideslipData;

	private DeviceDisplay display;

	@Autowired
	private ServletContext servletContext;

	@Resource(name = "checkDataManager")
	private CheckDataManager checkDataManager;

	// 进入检测状态
	private String jrclzt;

	// 取仪表数据
	private String qs;

	// 结束检测
	private String jsjc;

	// 开始检测数据
	private String ksjcjj;

	// 数据检测结束
	private String sjjcjs;

	@Override
	public void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialPortEvent.DATA_AVAILABLE:// 当有可用数据时读取数据,并且给串口返回数据

			byte[] readBuffer = new byte[1024 * 128];
			int length = 0;
			int lengthTemp = 0;
			try {
				while (inputStream.available() > 0) {
					lengthTemp = inputStream.read(readBuffer);
					length += lengthTemp;
					logger.info("数据长度" + length);
					if (length >= 1024 * 128) {
						logger.debug("读入的数据超过1024 * 128");
						break;
					}
				}
				byte[] endodedData = new byte[length];
				System.arraycopy(readBuffer, 0, endodedData, 0, length);
				logger.info("有侧滑数据返回");
				ProtocolType type = sd.getProtocolType(endodedData);
				logger.info(sd);
				// 响应数据的处理方法
				if (type == ProtocolType.DATA) {
					sd.setData(endodedData, sideslipData);
				}

				// 响应通知的方法
				if (type == ProtocolType.NOTICE) {
					String ml = CharUtil.byte2HexOfString(endodedData);
					logger.info("返回命令：" + ml);
					if (ml == sjjcjs) {
						this.sendMessage(qs);
					}
				}

			} catch (IOException e) {
				logger.error("读取灯光仪数据流异常", e);
			}
			break;
		}

	}

	@Override
	public void run() {

	}


	@Override
	public void init() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		String temp = (String) this.getQtxxObject().get("kzsb-xsp");
		// 加载挂载设备
		if (temp != null) {
			Integer deviceid = Integer.parseInt(temp);
			display = (DeviceDisplay) servletContext.getAttribute(deviceid + "_" + Device.KEY);
		}
		
		sd = (IDeviceSideslipDecode) Class.forName(this.getDevice().getDeviceDecode()).newInstance();

		jrclzt = (String) this.getQtxxObject().get("t-jrclzt");

		qs = (String) this.getQtxxObject().get("g-qs");

		jsjc = (String) this.getQtxxObject().get("t-jsjc");

		ksjcjj = (String) this.getQtxxObject().get("r-ksjcsj");

		sjjcjs = (String) this.getQtxxObject().get("r-sjjcjs");

	}

	/**
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void checkStart() throws IOException, InterruptedException {

		// 等待光电到位

		// 开始新的一次检测
		createNew();

		// 进入测量状态
		this.sendMessage(jrclzt);

		// 显示屏显示信息
		this.display.sendMessage("苏J00001", DeviceDisplay.SP);
		this.display.sendMessage("侧滑开始检测", DeviceDisplay.XP);
		logger.info("进入等待。。");
		// 等待测量结束
		while (true) {
			if (this.sideslipData.getSideslip() != null) {
				break;
			}
			Thread.sleep(300);
		}

		this.sendMessage(jsjc);

		// 保存检测数据 //计算检测结果
		this.checkDataManager.saveSideslipData(sideslipData);
		this.display.sendMessage("侧滑：O", DeviceDisplay.XP);

	}

	private void createNew() {
		this.sideslipData = new SideslipData();

	}

}
