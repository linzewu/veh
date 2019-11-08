package com.xs.veh.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.xs.veh.entity.Device;
import com.xs.veh.network.DeviceDyno;

import net.sf.json.JSONObject;

@Component("MyWebSocket")
@ServerEndpoint(value = "/websocket/{clienName}")
public class MyWebSocket {
	
	public static Map<String,Session> sessionMap=new HashMap<String, Session>();
	


	private static Logger logger = Logger.getLogger(MyWebSocket.class);
	
	@Autowired
	private ServletContext servletContext;

	
	//session为与某个客户端的连接会话，需要通过它来给客户端发送数据
		private Session session;
	 
		/**
		 * 连接建立成功调用的方法
		 * @param session  可选的参数
		 * @throws Exception 
		 */
		@OnOpen
		public void onOpen(Session session,@PathParam("clienName") String clienName) throws Exception {
			this.session = session;
			System.out.println(clienName);
			System.out.println("Open");
			sessionMap.put(clienName, session);
		}
	 
		/**
		 * 连接关闭调用的方法
		 * @throws Exception 
		 */
		@OnClose
		public void onClose(Session session) throws Exception {
			String key = session.getPathParameters().get("clienName");
			sessionMap.remove(key);
			System.out.println("Close " +key);
		}
	 
		/**
		 * 收到消息后调用的方法
		 * @param message 客户端发送过来的消息
		 * @param session 可选的参数
		 * @throws Exception 
		 */
		@OnMessage
		public void onMessage(String message, Session session) throws Exception {
			
		
			
			JSONObject jo=JSONObject.fromObject(message);
			
			String common = jo.getString("common");
			List<String> param=null;
			if(jo.has("param")) {
				param=jo.getJSONArray("param");
			}
			
			if (common != null){
		        	switch (common) {        	
					case "send":
						sendCommon(jo.getString("message"),session,param);
						break;				
					case "close":
						System.out.println("关闭连接");
						onClose(session);
						break;
					default:
							break;
					}
		        }
		}
		
		private void sendCommon(String common,Session session,List<String> param) throws IOException {
			String key = session.getPathParameters().get("clienName");
			String deviceId = key.split("_")[1];
			
			WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
			servletContext = wac.getBean(ServletContext.class);
			
			DeviceDyno dl = (DeviceDyno)servletContext.getAttribute(deviceId+"_"+Device.KEY);
			
			if(!CollectionUtils.isEmpty(param)) {
				logger.info(param);
				logger.info(param.size());
				dl.getDs().sendCommon(common,param.toArray(new Object[param.size()]));
			}else {
				dl.getDs().sendCommon(common);
			}
			
			
			
		}
	 
		/**
		 * 发生错误时调用
		 * @param session
		 * @param error
		 */
		@OnError
		public void onError(Session session, Throwable error) {
			error.printStackTrace();
		}
	 
		/**
		 * 发送消息方法。
		 * @param message
		 * @throws IOException
		 */
		public void sendMessage(String message) throws IOException {
			this.session.getBasicRemote().sendText(message);   //向客户端发送数据
		}
}
