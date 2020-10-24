package com.xs.veh.network;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.xs.common.BaseParamsUtil;
import com.xs.common.MyHibernateTemplate;
import com.xs.veh.entity.BaseParams;
import com.xs.veh.entity.CheckPhoto;
import com.xs.veh.entity.Device;
import com.xs.veh.entity.VehCheckLogin;
import com.xs.veh.entity.VehFlow;
import com.xs.veh.manager.CheckDataManager;
import com.xs.veh.manager.CheckEventManger;
import com.xs.veh.util.HKVisionUtil;
import com.xs.veh.util.SpringUtil;

import net.sf.json.JSONObject;

public class TakePicture implements Runnable {

	private static Logger logger = Logger.getLogger(TakePicture.class);

	private Integer yc;

	private VehCheckLogin vehCheckLogin;

	private String jyxm;

	private String ml;
	
	private String zpzl;
	
	private Map custom;
	
	private String jcxdh;
	
	public Map getCustom() {
		return custom;
	}

	public void setCustom(Map custom) {
		this.custom = custom;
	}

	public String getZpzl() {
		return zpzl;
	}

	public void setZpzl(String zpzl) {
		this.zpzl = zpzl;
	}

	public Integer getYc() {
		return yc;
	}

	public void setYc(Integer yc) {
		this.yc = yc;
	}

	public VehCheckLogin getVehCheckLogin() {
		return vehCheckLogin;
	}

	public void setVehCheckLogin(VehCheckLogin vehCheckLogin) {
		this.vehCheckLogin = vehCheckLogin;
	}

	public String getJyxm() {
		return jyxm;
	}

	public void setJyxm(String jyxm) {
		this.jyxm = jyxm;
	}

	public static void createNew(VehCheckLogin vehCheckLogin, String jyxm) {
		TakePicture tp = new TakePicture(vehCheckLogin.getJylsh(), vehCheckLogin.getJcxdh(), vehCheckLogin.getJycs(),
				vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(), jyxm, 0);
		tp.setYc(0);
		tp.setVehCheckLogin(vehCheckLogin);
		tp.setJyxm(jyxm);
		Thread t = new Thread(tp);
		t.start();
	}

	public static void createNew(VehCheckLogin vehCheckLogin, String jyxm, Integer yc) {
		TakePicture tp = new TakePicture(vehCheckLogin.getJylsh(), vehCheckLogin.getJcxdh(), vehCheckLogin.getJycs(),
				vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(), jyxm, 0);
		tp.setYc(yc);
		tp.setVehCheckLogin(vehCheckLogin);
		tp.setJyxm(jyxm);
		Thread t = new Thread(tp);
		t.start();

	}
	
	public static void createNew(VehCheckLogin vehCheckLogin, String jyxm, Integer yc,Integer jcxdh) {
		TakePicture tp = new TakePicture(vehCheckLogin.getJylsh(),jcxdh==null?vehCheckLogin.getJcxdh(): jcxdh.toString(), vehCheckLogin.getJycs(),
				vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(), jyxm, 0);
		tp.setYc(yc);
		tp.setVehCheckLogin(vehCheckLogin);
		tp.setJyxm(jyxm);
		Thread t = new Thread(tp);
		t.start();

	}
	

	public static void createNew(VehCheckLogin vehCheckLogin, String jyxm, Integer yc, String zpzl) {
		TakePicture tp = new TakePicture(vehCheckLogin.getJylsh(), vehCheckLogin.getJcxdh(), vehCheckLogin.getJycs(),
				vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(), jyxm, 0, zpzl);
		tp.setYc(yc);
		tp.setVehCheckLogin(vehCheckLogin);
		tp.setJyxm(jyxm);
		tp.setZpzl(zpzl);
		Thread t = new Thread(tp);
		t.start();
	}
	
	
	public static void custom(VehCheckLogin vehCheckLogin, String jyxm, Integer yc, String zpzl,Map custom) {
		TakePicture tp = new TakePicture(vehCheckLogin.getJylsh(), vehCheckLogin.getJcxdh(), vehCheckLogin.getJycs(),
				vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(), jyxm, 0, zpzl);
		tp.setYc(yc);
		tp.setVehCheckLogin(vehCheckLogin);
		tp.setJyxm(jyxm);
		tp.setZpzl(zpzl);
		tp.setCustom(custom);
		Thread t = new Thread(tp);
		t.start();
	}
	

	private TakePicture(String jylsh, String jcxdh, Integer jycs, String hphm, String hpzl, String clsbdh, String jyxm,
			Integer jyzt) {
		ml = jylsh + "," + jcxdh + "," + jycs + "," + hphm + "," + hpzl + "," + clsbdh + "," + jyxm + "," + jyzt
				+ ",by1,by2";
		
		this.jcxdh=jcxdh;
		logger.info("拍照命令=" + ml);
	}

	private TakePicture(String jylsh, String jcxdh, Integer jycs, String hphm, String hpzl, String clsbdh, String jyxm,
			Integer jyzt, String zpzl) {
		ml = jylsh + "," + jcxdh + "," + jycs + "," + hphm + "," + hpzl + "," + clsbdh + "," + jyxm + "," + jyzt + ","
				+ zpzl + ",by2";
		this.jcxdh=jcxdh;
		logger.info("拍照命令=" + ml);
	}

//	@Override
//	public void run() {
//		Socket client = null;
//		PrintStream out = null;
//		try {
//			Thread.sleep(yc);
//			client = new Socket("127.0.0.1", 8000);
//			client.setSoTimeout(500);
//			out = new PrintStream(client.getOutputStream());
//			System.out.println("发送拍照命令："+ml);
//			out.print(ml);
//			out.flush();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} finally {
//			if (out != null) {
//				out.close();
//			}
//			if (client != null) {
//				try {
//					client.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}
	
	private void createCustom() {
		
		List<BaseParams> paams = BaseParamsUtil.getBaseParamsByType("szdsfpt");
	
		if(!CollectionUtils.isEmpty(paams)&&"true".equals(paams.get(0).getParamValue())) {
			 SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			 if(zpzl==null) {
				 zpzl=getZPZL(jyxm);
			 }
			 StringBuilder sb=new StringBuilder();
			 sb.append("^^zpzp^^");
			 sb.append(vehCheckLogin.getJylsh());
			 sb.append("^^");
			 sb.append(vehCheckLogin.getJyjgbh());
			 sb.append("^^");
			 sb.append(vehCheckLogin.getJcxdh());
			 sb.append("^^");
			 sb.append(vehCheckLogin.getJycs());
			 sb.append("^^");
			 sb.append(vehCheckLogin.getHphm());
			 sb.append("^^");
			 sb.append(vehCheckLogin.getHpzl());
			 sb.append("^^");
			 sb.append(vehCheckLogin.getClsbdh());
			 sb.append("^^");
			 sb.append("^^");
			 sb.append(sdf.format(new Date()));
			 sb.append("^^");
			 sb.append(jyxm);
			 sb.append("^^");
			 sb.append(zpzl);
			 sb.append("^^");
			 logger.info("拍照指令="+sb.toString());
			 try {
				toSzServerSocket(sb.toString());
			} catch (IOException e) {
				logger.info("深圳平台拍照指令异常");
			}
		}else {
			logger.info("进入线上抓拍程序");
			
			MyHibernateTemplate hibernateTemplate = (MyHibernateTemplate) SpringUtil.getBean("hibernateTemplate");
			
			CheckDataManager checkDataManager =(CheckDataManager)SpringUtil.getBean("checkDataManager");
			
			CheckEventManger checkEventManger =(CheckEventManger)SpringUtil.getBean("checkEventManger");
			
			logger.info("参数lsh:"+vehCheckLogin.getJylsh());
			logger.info("参数jycs:"+ vehCheckLogin.getJycs());
			logger.info("参数jyxm:"+jyxm);
			
			HKVisionUtil hk=new HKVisionUtil();
			FileInputStream fis=null;
			
			try {
				
				String sxtip = (String) custom.get("sxtip");
				String sxtdk = (String) custom.get("sxtdk");
				String sxtzh = (String) custom.get("sxtzh");
				String sxtmm = (String) custom.get("sxtmm");
				
				String file = hk.taskPicture(sxtzh, sxtmm, sxtip, Integer.parseInt(sxtdk),vehCheckLogin.getJylsh()+"_"+vehCheckLogin.getJycs()+"_"+jyxm);
				logger.info("拍照成功保存文件路径file：="+sxtmm);
				fis =new FileInputStream(file);
				
				byte[] zp=new byte[fis.available()];
				fis.read(zp);
				CheckPhoto checkPhoto =new CheckPhoto();
				checkPhoto.setJcxdh(vehCheckLogin.getJcxdh());
				checkPhoto.setClsbdh(vehCheckLogin.getClsbdh());
				checkPhoto.setHphm(vehCheckLogin.getHphm());
				checkPhoto.setHpzl(vehCheckLogin.getHpzl());
				checkPhoto.setJycs(vehCheckLogin.getJycs());
				checkPhoto.setJyjgbh(vehCheckLogin.getJyjgbh());
				checkPhoto.setJylsh(vehCheckLogin.getJylsh());
				checkPhoto.setJyxm(jyxm);
				checkPhoto.setPssj(new Date());
				checkPhoto.setStatus(0);
				checkPhoto.setZp(zp);
				
				if(zpzl!=null) {
					checkPhoto.setZpzl(zpzl);
				}else {
					zpzl=getZPZL(jyxm);
					checkPhoto.setZpzl(zpzl);
				}
				checkDataManager.saveCheckPhoto(checkPhoto);
				if(!jyxm.equals("EP")&&!jyxm.equals("PF")&&!jyxm.equals("OC")&&!jyxm.equals("VL")) {
					checkEventManger.createEvent(vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(), "18C63", jyxm, vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(),zpzl,0);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				if(fis!=null) {
					try {
						fis.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	
	}

	public void run() {
		try {
			Thread.sleep(yc);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		logger.info("新拍照程序，开始，拍照项目："+jyxm);
		
		if(custom!=null) {
			createCustom();
			return;
		}
		
		try {
			
			List<BaseParams> params = BaseParamsUtil.getBaseParamsByType("sxtpz");
			for(BaseParams parma:params) {
				String[] paramName =parma.getParamName().split("_");
				if(paramName[0].equals(jyxm)) {
					createOther();
				}
			}
			
			//南昌前轴照片
			//||jyxm.equals("B1")
			if(jyxm.equals("C1")||jyxm.equals("R1")||jyxm.equals("R2")||jyxm.equals("DC")||jyxm.equals("M1")) {
				//createOther();
			}else {
				onLineDevice();
			}
		}catch (Exception e) {
			logger.error("抓拍异常:",e);
		}
	}
	
	private void createOther() {
		
		List<BaseParams> params = BaseParamsUtil.getBaseParamsByType("sxtpz");
		
		CheckDataManager checkDataManager =(CheckDataManager)SpringUtil.getBean("checkDataManager");
		
		CheckEventManger checkEventManger =(CheckEventManger)SpringUtil.getBean("checkEventManger");
		
		for(BaseParams param:params) {
			
			String[] paramName =param.getParamName().split("_");
			String pzjyxm =paramName[0];
			
			String jcxdh="1";
			
			if(paramName.length>1) {
				jcxdh=paramName[1];
			}
			
			
			if(pzjyxm.equals(jyxm)&&jcxdh.equals(this.jcxdh)&&(StringUtils.isEmpty(param.getMemo())||zpzl.equals(param.getMemo()))) {
				String value = param.getParamValue();
				JSONObject jo=JSONObject.fromObject(value);
				
				String sxtip = (String) jo.get("sxtip");
				String sxtdk = (String) jo.get("sxtdk");
				String sxtzh = (String) jo.get("sxtzh");
				String sxtmm = (String) jo.get("sxtmm");
				
				HKVisionUtil hk=new HKVisionUtil();
				FileInputStream fis=null;
				 
				try {
					String file = hk.taskPicture(sxtzh, sxtmm, sxtip, Integer.parseInt(sxtdk),vehCheckLogin.getJylsh()+"_"+vehCheckLogin.getJycs()+"_"+jyxm);
					
					fis =new FileInputStream(file);
					
					byte[] zp=new byte[fis.available()];
					
					fis.read(zp);
					
					CheckPhoto checkPhoto =new CheckPhoto();
					
					checkPhoto.setJcxdh(vehCheckLogin.getJcxdh());
					checkPhoto.setClsbdh(vehCheckLogin.getClsbdh());
					checkPhoto.setHphm(vehCheckLogin.getHphm());
					checkPhoto.setHpzl(vehCheckLogin.getHpzl());
					checkPhoto.setJycs(vehCheckLogin.getJycs());
					checkPhoto.setJyjgbh(vehCheckLogin.getJyjgbh());
					checkPhoto.setJylsh(vehCheckLogin.getJylsh());
					checkPhoto.setJyxm(jyxm);
					checkPhoto.setPssj(new Date());
					checkPhoto.setStatus(0);
					checkPhoto.setZp(zp);
					
					if(zpzl!=null) {
						checkPhoto.setZpzl(zpzl);
					}else {
						zpzl=getZPZL(jyxm);
						checkPhoto.setZpzl(zpzl);
					}
					checkDataManager.saveCheckPhoto(checkPhoto);
					checkEventManger.createEvent(vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(), "18C63", jyxm, vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(),zpzl,0);
				} catch (Exception e) {
					logger.error("拍照错误：",e);
				}finally {
					if(fis!=null) {
						try {
							fis.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			
			
		}
		
	}
	
	
	private void onLineDevice() {
		
		logger.info("进入线上抓拍程序");
		
		MyHibernateTemplate hibernateTemplate = (MyHibernateTemplate) SpringUtil.getBean("hibernateTemplate");
		
		CheckDataManager checkDataManager =(CheckDataManager)SpringUtil.getBean("checkDataManager");
		
		CheckEventManger checkEventManger =(CheckEventManger)SpringUtil.getBean("checkEventManger");
		
		logger.info("参数lsh:"+vehCheckLogin.getJylsh());
		logger.info("参数jycs:"+ vehCheckLogin.getJycs());
		logger.info("参数jyxm:"+jyxm);
		
		List<BaseParams> paams = BaseParamsUtil.getBaseParamsByType("szdsfpt");
		if(!CollectionUtils.isEmpty(paams)) {
			String szdsfpt = paams.get(0).getParamValue();
			 if("true".equals(szdsfpt)) {
				 SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				 if(zpzl==null) {
					 zpzl=getZPZL(jyxm);
				 }
				 
				 StringBuilder sb=new StringBuilder();
				 sb.append("^^zpzp^^");
				 sb.append(vehCheckLogin.getJylsh());
				 sb.append("^^");
				 sb.append(vehCheckLogin.getJyjgbh());
				 sb.append("^^");
				 sb.append(vehCheckLogin.getJcxdh());
				 sb.append("^^");
				 sb.append(vehCheckLogin.getJycs());
				 sb.append("^^");
				 sb.append(vehCheckLogin.getHphm());
				 sb.append("^^");
				 sb.append(vehCheckLogin.getHpzl());
				 sb.append("^^");
				 sb.append(vehCheckLogin.getClsbdh());
				 sb.append("^^");
				 sb.append("^^");
				 sb.append(sdf.format(new Date()));
				 sb.append("^^");
				 sb.append(jyxm);
				 sb.append("^^");
				 sb.append(zpzl);
				 sb.append("^^");
				 logger.info("拍照指令="+sb.toString());
				 try {
					toSzServerSocket(sb.toString());
				} catch (IOException e) {
					logger.info("深圳平台拍照指令异常");
				}
			 }
		}else {
			List<VehFlow> vehFlows = (List<VehFlow>) hibernateTemplate.find(
					"from VehFlow where jylsh=? and jycs=? and jyxm=?", vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(),
					jyxm);
			for (VehFlow vehFlow : vehFlows) {
				
				logger.info("VehFlow 设备ID："+vehFlow.getSbid());

				if (vehFlow.getSbid() != null) {
					Device device = (Device)hibernateTemplate.find("from Device where id=?", vehFlow.getSbid()).get(0);
					//Device device = hibernateTemplate.load(Device.class, vehFlow.getSbid());

					String qtxx = device.getQtxx();
					JSONObject qtxxjo = JSONObject.fromObject(qtxx);

					String sxtip = (String) qtxxjo.get("sxtip");
					String sxtdk = (String) qtxxjo.get("sxtdk");
					String sxtzh = (String) qtxxjo.get("sxtzh");
					String sxtmm = (String) qtxxjo.get("sxtmm");
					
					logger.info("sxtip："+sxtip);
					logger.info("sxtdk："+sxtdk);
					logger.info("sxtzh："+sxtzh);
					logger.info("sxtmm"+sxtmm);

					if (!StringUtils.isEmpty(sxtip) && !StringUtils.isEmpty(sxtdk) && !StringUtils.isEmpty(sxtzh)
							&& !StringUtils.isEmpty(sxtmm)) {
						HKVisionUtil hk=new HKVisionUtil();
						FileInputStream fis=null;
						 
						try {
							String file = hk.taskPicture(sxtzh, sxtmm, sxtip, Integer.parseInt(sxtdk),vehCheckLogin.getJylsh()+"_"+vehCheckLogin.getJycs()+"_"+jyxm);
							logger.info("拍照成功保存文件路径file：="+sxtmm);
							fis =new FileInputStream(file);
							
							byte[] zp=new byte[fis.available()];
							
							fis.read(zp);
							CheckPhoto checkPhoto =new CheckPhoto();
							checkPhoto.setJcxdh(vehCheckLogin.getJcxdh());
							checkPhoto.setClsbdh(vehCheckLogin.getClsbdh());
							checkPhoto.setHphm(vehCheckLogin.getHphm());
							checkPhoto.setHpzl(vehCheckLogin.getHpzl());
							checkPhoto.setJycs(vehCheckLogin.getJycs());
							checkPhoto.setJyjgbh(vehCheckLogin.getJyjgbh());
							checkPhoto.setJylsh(vehCheckLogin.getJylsh());
							checkPhoto.setJyxm(jyxm);
							checkPhoto.setPssj(new Date());
							checkPhoto.setStatus(0);
							checkPhoto.setZp(zp);
							
							if(zpzl!=null) {
								checkPhoto.setZpzl(zpzl);
							}else {
								zpzl=getZPZL(jyxm);
								checkPhoto.setZpzl(zpzl);
							}
							
							checkDataManager.saveCheckPhoto(checkPhoto);
							checkEventManger.createEvent(vehCheckLogin.getJylsh(), vehCheckLogin.getJycs(), "18C63", jyxm, vehCheckLogin.getHphm(), vehCheckLogin.getHpzl(), vehCheckLogin.getClsbdh(),zpzl,0);
							
						} catch (Exception e) {
							e.printStackTrace();
						}finally {
							if(fis!=null) {
								try {
									fis.close();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}

				}

			}
		}
	}
	
	
	
	
	
	public static void toSzServerSocket(String message) throws IOException {
		
		
		List<BaseParams> datas =  BaseParamsUtil.getBaseParamsByType("zpdz_sz");
		
		 String host = "190.203.185.204"; 
		if(!CollectionUtils.isEmpty(datas)) {
			 host = datas.get(0).getParamValue();
		}
		
		
		
		// 要连接的服务端IP地址和端口
	     
	    int port = 6698;
	    // 与服务端建立连接
	    Socket socket = new Socket(host, port);
	    // 建立连接后获得输出流
	    OutputStream outputStream = socket.getOutputStream();
	    socket.getOutputStream().write(message.getBytes("UTF-8"));
	    outputStream.close();
	    socket.close();
	}
	
	
	
	public String getZPZL(String jyxm) {
		
		String zpzl = null;
		
		switch (jyxm) {
		case "H1":
			zpzl="0321";
			break;
		case "H4":
			zpzl="0352";
			break;
		case "B1":
			zpzl="0322";
			break;
		case "B2":
			zpzl="0348";
			break;
		case "B3":
			zpzl="0349";
			break;
		case "B4":
			zpzl="0350";
			break;
		case "B5":
			zpzl="0354";
			break;
		case "B0":
			zpzl="0351";
			break;
		case "S1":
			zpzl="0347";
			break;
		case "A1":
			zpzl="0353";
			break;
		case "C1":
			zpzl="0323";
			break;
		case "L1":
			zpzl="0356";
			break;
		case "L2":
			zpzl="0357";
			break;
		case "L3":
			zpzl="0358";
			break;
		case "L4":
			zpzl="0359";
			break;
		default:
			zpzl=null;
			
		}
		
		return zpzl;
	}
	
	
	public static void main(String[] age) {
		TakePicture tp=new TakePicture("1", "1", 1, "1", "1", "1", "A1", 0);
		
		System.out.println(tp.getZPZL("A1"));
		
		
	}

}
