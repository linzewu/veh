package com.xs.veh.util;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.sun.jna.NativeLong;
import com.xs.common.BaseParamsUtil;
import com.xs.veh.entity.BaseParams;
import com.xs.veh.entity.VideoConfig;
import com.xs.veh.util.HCNetSDK.NET_DVR_TIME;

@Component
public class HKVisionUtil {
	
	private String ip;
	
	private int port;
	
	private String userName;
	
	private String password;
	
	private long channel;
	
	//private String picPath=getConfigPath();
	
	
	
	private String devIp;
	
	private int devPort;
	
	private String devUserName;
	
	private String devPassword;
	
	private long devChannel=1;
	
	public static String getConfigPath() {
		
		List<BaseParams> datas = BaseParamsUtil.getBaseParamsByType("splj");
		if(!CollectionUtils.isEmpty(datas)) {
			return datas.get(0).getParamValue();
		}
		return "D:\\pic";
	}

	
	
	protected static Log log = LogFactory.getLog(HKVisionUtil.class);
	
	static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
	
	HCNetSDK.NET_DVR_DEVICEINFO_V30 m_strDeviceInfo;// 设备信息
	
	public void cameraInit() {
		// 初始化
		boolean initSuc = hCNetSDK.NET_DVR_Init();
		if (initSuc != true) {
			log.info("初始化失败");
		} else {
			log.info("初始化成功");
		}
	}

	// 注册
	public NativeLong register( String username, String password, String m_sDeviceIP, int iPort) throws Exception {

		log.info("注册，端口：" + iPort);
		iPort = 8000;
		log.info("注册，设备IP：" + m_sDeviceIP);
		// 注册
		m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
		// int iPort = 8000;
		log.info("注册，设备IP：" + m_sDeviceIP);
		NativeLong lUserID = hCNetSDK.NET_DVR_Login_V30(m_sDeviceIP, (short) iPort, username, password, m_strDeviceInfo);

		long userID = lUserID.longValue();
		if (userID == -1) {
			log.info("注册失败");
			throw new Exception("注册失败");
		} else {
			log.info("注册成功,lUserID:" + userID);
		}
		return lUserID;
	}
	
	public static void main(String[] age) {
		HKVisionUtil h=new HKVisionUtil();
		
		try {
			String file = h.taskPicture("admin", "123456", "192.168.0.100", 8000,"12345677");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void taskPicture(String recordId) throws Exception {
		// 初始化
		cameraInit();
		NativeLong lUserID = register(devUserName,devPassword,devIp,devPort);
		try {
			NativeLong lChannel =new NativeLong(devChannel);
			HCNetSDK.NET_DVR_JPEGPARA jpgparam =new HCNetSDK.NET_DVR_JPEGPARA();
			jpgparam.wPicQuality=1;
			jpgparam.wPicSize=5;
			FileUtil.createDirectory(getConfigPath()+"\\photos\\");
			boolean flag = hCNetSDK.NET_DVR_CaptureJPEGPicture(lUserID, lChannel, jpgparam, getConfigPath()+"\\photos\\"+recordId+".jpg");
			if(!flag) {
				log.error("拍照失败:"+hCNetSDK.NET_DVR_GetLastError() );
				throw new Exception("拍照失");
			}
		}finally {
			// 注销用户
			hCNetSDK.NET_DVR_Logout(lUserID);
			// 释放SDK资源
			hCNetSDK.NET_DVR_Cleanup();
		}
	}
	
	public  String taskPicture(String devUserName,String devPassword,String devIp,int devPort,String fileName) throws Exception {
		// 初始化
		cameraInit();
		NativeLong lUserID =null;
		try {
			log.info("开始注册设备 账号："+devUserName);
			log.info("开始注册设备 ip："+devIp);
			log.info("开始注册设备 密码："+devPassword);
			log.info("开始注册设备 端口："+devPort);
			lUserID = register(devUserName,devPassword,devIp,devPort);
			log.info("lUserID："+lUserID);
			FileUtil.createDirectory(getConfigPath());
			NativeLong lChannel =new NativeLong(1);
			HCNetSDK.NET_DVR_JPEGPARA jpgparam =new HCNetSDK.NET_DVR_JPEGPARA();
			jpgparam.wPicQuality=1;
			jpgparam.wPicSize=5;
			FileUtil.createDirectory(getConfigPath()+"\\photos\\");
			boolean flag = hCNetSDK.NET_DVR_CaptureJPEGPicture(lUserID, lChannel, jpgparam, getConfigPath()+"\\photos\\"+fileName+".jpg");
			if(!flag) {
				log.error("拍照失败:"+hCNetSDK.NET_DVR_GetLastError());
				throw new Exception("拍照失");
			}
			String fileFpath =getConfigPath()+"\\photos\\"+fileName+".jpg";
			return fileFpath;
		}catch (Exception e) {
			log.error("拍照失败:",e);
			throw e;
		} finally {
			if(lUserID!=null) {
				// 注销用户
				hCNetSDK.NET_DVR_Logout(lUserID);
			}
			// 释放SDK资源
			hCNetSDK.NET_DVR_Cleanup();
			
		}
	}
	
	
	
	public void downLoad(VideoConfig vc,NET_DVR_TIME lpStartTime, NET_DVR_TIME lpStopTime,String saveFile) throws Exception {
		cameraInit();
		NativeLong lUserID = register(vc.getUserName(),vc.getPassword(),vc.getIp(),Integer.parseInt(vc.getPort()));
		try {
			log.info("视频地址："+getConfigPath());
			FileUtil.createDirectory(getConfigPath()+"\\video\\");
			NativeLong lChannel =new NativeLong(vc.getChannel());
			// 指定下载的文件
			NativeLong tRet = hCNetSDK.NET_DVR_GetFileByTime(lUserID, lChannel, lpStartTime, lpStopTime, getConfigPath()+"\\video\\"+saveFile+".mp4");
			int tError = hCNetSDK.NET_DVR_GetLastError();
			if (tRet.longValue() == -1) {
				log.error("NET_DVR_GetFileByTime fail,channel:" + lChannel + "error code:" + tError);
				throw new Exception("NET_DVR_GetFileByTime fail,channel:" + lChannel + "error code:" + tError);
			}
			hCNetSDK.NET_DVR_PlayBackControl(tRet, hCNetSDK.NET_DVR_SET_TRANS_TYPE, hCNetSDK.NET_DVR_SET_TRANS_TYPE_MP4,
					null);
			
			// 开启下载
			boolean flagPlay = hCNetSDK.NET_DVR_PlayBackControl(tRet, hCNetSDK.NET_DVR_PLAYSTART, 0, null);
			tError = hCNetSDK.NET_DVR_GetLastError();
			if (!flagPlay) {
				log.error("NET_DVR_PlayBackControl() failed ,ErrorCode:" + tError);
				throw new Exception("NET_DVR_PlayBackControl() failed ,ErrorCode:" + tError);
			}

			// 获取下载进度
			int nPos = 0;
			for (nPos = 0; nPos < 100 && nPos >= 0;) {
				Thread.sleep(2000); // millisecond
				nPos = hCNetSDK.NET_DVR_GetDownloadPos(tRet);
				log.info("channel:" + lChannel + ",  is downloading... " + nPos); // 下载进度
			}
			
		} finally {
			// 注销用户
			hCNetSDK.NET_DVR_Logout(lUserID);
			// 释放SDK资源
			hCNetSDK.NET_DVR_Cleanup();
		}
	}
	
  public NET_DVR_TIME convert(Date date) {
	  	NET_DVR_TIME lpStartTime = new HCNetSDK.NET_DVR_TIME();
		Calendar now = Calendar.getInstance();
		now.setTime(date);
		lpStartTime.dwYear = now.get(Calendar.YEAR);
		lpStartTime.dwMonth = (now.get(Calendar.MONTH) + 1);
		lpStartTime.dwDay = now.get(Calendar.DAY_OF_MONTH);
		lpStartTime.dwHour = now.get(Calendar.HOUR_OF_DAY);
		lpStartTime.dwMinute = now.get(Calendar.MINUTE);
		lpStartTime.dwSecond = now.get(Calendar.SECOND);
		return lpStartTime;
 }

}
