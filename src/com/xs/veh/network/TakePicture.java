package com.xs.veh.network;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
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

	private TakePicture(String jylsh, String jcxdh, Integer jycs, String hphm, String hpzl, String clsbdh, String jyxm,
			Integer jyzt) {
		ml = jylsh + "," + jcxdh + "," + jycs + "," + hphm + "," + hpzl + "," + clsbdh + "," + jyxm + "," + jyzt
				+ ",by1,by2";
		logger.info("拍照命令=" + ml);
	}

	private TakePicture(String jylsh, String jcxdh, Integer jycs, String hphm, String hpzl, String clsbdh, String jyxm,
			Integer jyzt, String zpzl) {
		ml = jylsh + "," + jcxdh + "," + jycs + "," + hphm + "," + hpzl + "," + clsbdh + "," + jyxm + "," + jyzt + ","
				+ zpzl + ",by2";
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

	public void run() {
		try {
			Thread.sleep(yc);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		logger.info("新拍照程序，开始，拍照项目："+jyxm);
		try {
			if(jyxm.equals("C1")||jyxm.equals("R1")||jyxm.equals("R2")||jyxm.equals("B1")||jyxm.equals("DC")) {
				createOther();
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
			
			
			if(pzjyxm.equals(jyxm)&&jcxdh.equals(vehCheckLogin.getJcxdh())&&(StringUtils.isEmpty(param.getMemo())||zpzl.equals(param.getMemo()))) {
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
