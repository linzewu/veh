<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
	Object user = session.getAttribute("user");
	if (user == null){
		response.sendRedirect("html/login.html");
	}else{
		response.sendRedirect("html/index.html");
	}
%>