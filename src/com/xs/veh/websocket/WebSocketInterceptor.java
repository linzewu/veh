package com.xs.veh.websocket;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

public class WebSocketInterceptor implements HandshakeInterceptor {
	  @Override
	    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler, Map<String, Object> map) throws Exception {
	        if (request instanceof ServletServerHttpRequest) {
	            ServletServerHttpRequest serverHttpRequest = (ServletServerHttpRequest) request;
	            HttpSession session = serverHttpRequest.getServletRequest().getSession();
	            if (session != null) {
	                map.put("username", session.getAttribute("username"));
	            }

	        }
	        return true;
	    }

	    @Override
	    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {

	    }
}
