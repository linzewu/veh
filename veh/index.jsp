<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
	Object user = session.getAttribute("user");
	if (user == null)
		response.sendRedirect("html/login.html");
%>
<html>
<head>
<link rel="stylesheet" href="css/veh.css" type="text/css">
<link rel="stylesheet" href="js/edge/webix.css" type="text/css">
<script src="js/edge/webix.js" type="text/javascript"></script>
<script src="js/veh.js" type="text/javascript"></script>

<style>
body {
	background: #F2EFEA;
}

.transparent {
	background-color: transparent;
}

.heading {
	font-size: 26px;
}

.bradius {
	border-radius: 4px;
}

.webix_modal {
	opacity: 0.01;
}
</style>
</head>
<body>
</body>
</html>