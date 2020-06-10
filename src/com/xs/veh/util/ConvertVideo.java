package com.xs.veh.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConvertVideo {

	protected static Log log = LogFactory.getLog(ConvertVideo.class);


	public final static  String outputPath = "D:\\temp\\";

	public final static String ffmpegPath = ClientDemo.EXTEND_PATH + "\\ffmpeg\\";


	public static void main(String args[]) throws Exception {
		
		if(processMp4("D:\\pic\\video\\32130020042301433_1_F2_0.mp4","32130020042301433_1_F2_0.mp4")) {
			
			copy("D:\\temp\\32130020042301433_1_F2_0.mp4", "D:\\pic\\video\\");
			
		}
		
		
		
		System.out.println(new Date().getTime());
		Calendar now = Calendar.getInstance();  
        System.out.println("年: " + now.get(Calendar.YEAR));  
        System.out.println("月: " + (now.get(Calendar.MONTH) + 1) + "");  
        System.out.println("日: " + now.get(Calendar.DAY_OF_MONTH));  
        System.out.println("时: " + now.get(Calendar.HOUR_OF_DAY));  
        System.out.println("分: " + now.get(Calendar.MINUTE));  
        System.out.println("秒: " + now.get(Calendar.SECOND));  
	}


	public static boolean processMp4(String oldfilepath,String fileName) throws Exception {
		
		List<String> command = new ArrayList<String>();
		command.add(ffmpegPath + "ffmpeg");
		command.add("-i");
		command.add(oldfilepath);
		command.add("-c:v");
		command.add("libx265");
		command.add("-mbd");
		command.add("0");
		command.add("-c:a");
		command.add("aac");
		command.add("-strict");
		command.add("-2");
		command.add("-pix_fmt");
		command.add("yuv420p");
		command.add("-movflags");
		command.add("faststart");
		command.add(outputPath+"\\"+fileName);
		command.add("-y");
		try {
			// 方案2
			Process videoProcess = new ProcessBuilder(command).redirectErrorStream(true).start();
			new PrintStream(videoProcess.getErrorStream()).start();
			new PrintStream(videoProcess.getInputStream()).start();
			videoProcess.waitFor();
			return true;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public static void copy(String srcPathStr, String desPathStr)
	{
        //获取源文件的名称
        String newFileName = srcPathStr.substring(srcPathStr.lastIndexOf("\\")+1); //目标文件地址
        System.out.println("源文件:"+newFileName);
        desPathStr = desPathStr + File.separator + newFileName; //源文件地址
        System.out.println("目标文件地址:"+desPathStr);
        try
		{
             FileInputStream fis = new FileInputStream(srcPathStr);//创建输入流对象
             FileOutputStream fos = new FileOutputStream(desPathStr); //创建输出流对象               
             byte datas[] = new byte[1024*8];//创建搬运工具
             int len = 0;//创建长度   
             while((len = fis.read(datas))!=-1)//循环读取数据
			{
				fos.write(datas,0,len);
            } 
                fis.close();//释放资源
                fis.close();//释放资源
        }
			catch (Exception e)
			{
                e.printStackTrace();
            }
    }

}
