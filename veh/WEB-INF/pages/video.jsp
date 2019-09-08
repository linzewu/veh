<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<% 
String jylsh =(String)request.getParameter("jylsh");

String jyjgbh =(String)request.getParameter("jyjgbh");

if(jyjgbh.equals("")){
	request.getRequestDispatcher("video2.jsp").forward(request, response);
}

String[] lsArray=new String[]{"32090201512090040","3209112015001297","3209112015001950"
		,"15110401VJR3088","320906151945001","32090201508030002","3209021614193"};

for(String lsh:lsArray){
	if(lsh.equals(jylsh)){
		response.sendRedirect("/video/"+lsh+"/index.html");
	}
}

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>动车检测视频监控系统</title>

<link rel="stylesheet" type="text/css"
	href="/veh/js/easyui/themes/default/easyui.css">
<link rel="stylesheet" type="text/css"
	href="/veh/js/easyui/themes/color.css">

<script type="text/javascript" src="/veh/js/easyui/jquery.min.js"></script>
<script type="text/javascript"
	src="/veh/js/easyui/jquery.easyui.min.js"></script>
<script type="text/javascript"
	src="/veh/js/easyui/locale/easyui-lang-zh_CN.js"></script>
<script type="text/javascript" src="/veh/js/data.js"></script>
<style type="text/css">
.smallocxdiv {
	width: 99%;
	height: 98%;
	margin: 0 auto;
	padding-top: 2px;
}

.check-item {
	width: 120px;
}

.check-menu {
	list-style: none;
}

.check-menu li {
	margin: 10px 5px 5px 5px;
}

.playTool ul li{
	float: left;
	margin: 2px 5px 2px 5px;
	
}

.playTool{
	margin: 0 auto;
	width: 400px;
	height: 30px;
}

.playTool ul{
	list-style: none;
}
.btn-play{
	width: 60px;
}

.vehinfo{
	padding-top: 20px;
}
</style>
<script type="text/javascript">

	var playInfo='${playInfo}';
	
	var m_bDVRControl =null;
	var m_iLoginUserId;
	var m_iPlay = 0;                             //是否播放
	var m_iProtocolType = 0;                         //协议类型，0 – TCP， 1 - UDP
	var m_iStreamType = 0;                           //码流类型，0 表示主码流， 1 表示子码流
	var m_iChannelNum;
	var m_iIPChannelNum;
	var m_iIPChannelNum;
	var m_szDeviceType;
	var channelList=[];
	var m_iPlayback=0;

	$(function(){
		var name = getjczmc('${param.jyjgbh}');
		$("#jyjgbh").val(name);

		if($.trim(playInfo)==""){
			LogMessage("无法获取车辆播放信息");
			return;
		}

		playInfo=$.parseJSON(playInfo);
	
		$("#info_hphm").val(playInfo[0].hphm);

		
		$.each(playInfo,function(i,n){
			var li="<li><a  href='javascript:void(0)' class=\"easyui-linkbutton c6 check-item\" onclick=\"play('"+n.jyxm+"',"+n.jycs+")\" >"+getjyxm(n.jyxm)+"  "+n.jycs+"</a></li>";
			$(".check-menu").append(li);
			$.parser.parse('.check-menu'); 
		});
		init();
	});
	
	function init()
	{ 
		var myDate = new Date();
		var iYear = myDate.getFullYear();        
		if(iYear < 1971 || iYear > 2037)
		{
			alert("提示","为了正常使用本软件，请将系统日期年限设置在1971-2037范围内！");
		}
		if(document.getElementById("HIKOBJECT1").object == null)
		{
			alert("提示","请先下载控件并注册！");
			m_bDVRControl = null;
		}
		else
		{
			m_bDVRControl = document.getElementById("HIKOBJECT1");
			//getChannel();
		}
	}
	
	function login(info){
		m_iLoginUserId = m_bDVRControl.Login(info.ip,info.port,info.userName,info.password);
		if(m_iLoginUserId == -1)
		{
            var dRet = m_bDVRControl.GetLastError();
            LogMessage("登陆影像录像机失败,错误码：" + dRet);
		}
		return m_iLoginUserId;
	}
	
	

	function getChannel(info){
		m_iLoginUserId =  login(info);
		if(m_iLoginUserId>-1){
			var szServerInfo = m_bDVRControl.GetServerInfo();
			var xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
			xmlDoc.async="false"
			xmlDoc.loadXML(szServerInfo)
			m_iChannelNum = parseInt(xmlDoc.documentElement.childNodes[0].childNodes[0].nodeValue);
	        m_iIPChannelNum = parseInt(xmlDoc.documentElement.childNodes[8].childNodes[0].nodeValue);
	        m_szDeviceType = xmlDoc.documentElement.childNodes[1].childNodes[0].nodeValue;
			if(m_iChannelNum < 1)
			{
				LogMessage("获取模拟通道失败！");
			}
			else
			{
				LogMessage("获取模拟通道成功！");	
				
				for(var i = 0; i < m_iChannelNum; i ++)
				{
					var channel={};
					var szChannelName = m_bDVRControl.GetChannelName(i);
					if(szChannelName == "")
					{
						szChannelName = "通道" + (i + 1);
					}
					channel.id=i;
					channel.name=szChannelName;
					channelList.push(channel);
				}
			}
	        if (m_iIPChannelNum < 1) {
	            LogMessage("获取IP通道失败！");
	        }
	        else {
	            LogMessage("获取IP通道成功！");

	            if (m_iIPChannelNum >= 64) {
	                     LogMessage("IP通道个数大于等于64，" + "通道号取值从0开始！");
	                     m_iIPChanStart = 0;
	                }

	            else{
	                     LogMessage("如果设备有IP通道，IP通道号取值从32开始！");
	                     m_iIPChanStart = 32;
	            }

	            m_bDVRControl.GetIPParaCfg();
	            szIPChanInfo = m_bDVRControl.GetIPCConfig();
	            var xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
	            xmlDoc.async = "false"
	            xmlDoc.loadXML(szIPChanInfo)
	            for (var i = m_iChannelNum; i < m_iChannelNum+m_iIPChannelNum; i++) {                   
	                m_iIPConnet = parseInt(xmlDoc.documentElement.childNodes[i].childNodes[3].childNodes[0].nodeValue);
	                if (m_iIPConnet == 1) {
	                	var channel={};
	                    var szChannelName = m_bDVRControl.GetChannelName(i+m_iIPChanStart );
	                    if (szChannelName == "") {
	                        szChannelName = "IP通道" + (i-m_iChannelNum + 1);
	                    }
	                    channel.name=szChannelName;
	                    channel.id=i;
	                    channelList.push(channel);
	                }
	            }
	        }
		}
	}
	
	function LogMessage(msg){
		
		 $.messager.show({
	         title:'消息',
	         msg:msg,
	         showType:'show',
	         width:200,
	         timeout:3000,
	         style:{
	             left:0,
	             right:'',
	             top:'',
	             bottom:-document.body.scrollTop-document.documentElement.scrollTop
	         }
	     });
	}
	
	var zkjms=0;
	
	var jyxm_,jycs_;
	
	function play(jyxm,jycs,kjms){
		
		logout();
		
		var info;
		jyxm_=jyxm;
		jycs_=jycs;
		
		if(kjms!=null){
			zkjms+=kjms;
		}else{
			zkjms=0;
		}
		
		$.each(playInfo,function(i,n){
			if(jyxm==n.jyxm&&jycs==n.jycs){
				info=n;
				return false;
			}
		});
		
		if(!info){
			return;
		}
		
		getChannel(info);
		
		if (m_iIPChannelNum >= 64) {

            LogMessage("IP通道个数大于等于64，" + "IP通道号取值从0开始！");

            m_iIPChanStart = 0;

        }

        else {
            LogMessage("如果设备有IP通道，IP通道号取值从32开始！");
            m_iIPChanStart = 32;
        }

        m_iNowChanNo = parseInt(info.channel)
        if (m_iNowChanNo >= m_iChannelNum) {
            m_iNowChanNo = m_iNowChanNo - m_iChannelNum + m_iIPChanStart;
        }
        if (m_iLoginUserId > -1) {
            if (m_iPlayback == 1) {
                m_bDVRControl.StopPlayBack();
                m_iPlayback = 0;
            }
            
            
            if (m_iPlayback == 0) {
            	
            	var kssj=info.kssj;
            	var jssj=info.jssj;
            	
            	var tysj=parseInt($("#tysj").val());
            	tysj+=zkjms;
            	
            	if(tysj!=0||tysj!=""){
            		var ksl = StringToDate(kssj).getTime()+(tysj*1000);
            		var jsl = StringToDate(jssj).getTime()+(tysj*1000);
            		
            		kssj=new Date(ksl).Format("yyyy-MM-dd HH:mm:ss");
            		jssj=new Date(jsl).Format("yyyy-MM-dd HH:mm:ss");
            	}
            	
            	
                if (m_bDVRControl.PlayBackByTime(m_iNowChanNo, kssj, jssj)) {
                    LogMessage("开始时间回放成功，起止时间："+kssj+" ~ "+jssj+"！推移时间："+ tysj);
                }
                m_iPlayback = 1;
            }
        }
        else {
            LogMessage("请注册设备！");
        }
		
	}
		
	function playConsoel(param){
		
		m_bDVRControl.PlayBackControl(param,0);
		
	}
	
	window.onbeforeunload=function(){
	
		if(event.clientY<0){
			unLogin();
			
		}
	}
	
	function p10(){
		play(jyxm_, jycs_, 10);
	}
	
	function logout(){
		m_bDVRControl.StopPlayBack();
		m_bDVRControl.Logout();
	}
	
	
	function unLogin(){
		m_bDVRControl.StopPlayBack();
		m_bDVRControl.Logout();
		m_bDVRControl.ClearOCX();
	}
window.onunload=unLogin;
</script>
</head>
<body>
	<div id="cc" class="easyui-layout" fit='true'>

		<div data-options="region:'north'" style="height: 50px;">
			<div class="vehinfo">
				<label>号牌号码：</label>
				<input value="" disabled="disabled" id="info_hphm" />
				<label>检验流水号：</label>
				<input value="${ param.jylsh}" disabled="disabled" />
				<label>检测站名称：</label>
				<input value="" disabled="disabled" id="jyjgbh" />
				
				<label>时间推移：</label><input class="easyui-numberbox" style="width: 60px;" value="0" id="tysj">秒
			</div>
		</div>
		<div data-options="region:'west'" style="width: 200px;">
			<ul class="check-menu">
				
			</ul>
		</div>

		<div data-options="region:'center'" border=0>
			<div class="easyui-layout" fit='true'>
				<div data-options="region:'center'">
					<div class="smallocxdiv" id="NetPlayOCX1">
						<object classid="CLSID:CAFCF48D-8E34-4490-8154-026191D73924"
							codebase="/veh/codebase/NetVideoActiveX23.cab#version=2,3,11,2"
							standby="Waiting..." id="HIKOBJECT1" width="100%" height="100%"
							name="HIKOBJECT1"></object>
					</div>
				</div>
				<div data-options="region:'south'" style="height: 50px;">
					<div class="playTool">
						<ul>
							<li><a id="btn" href="#" class="easyui-linkbutton btn-play" onclick="playConsoel(3)">暂停</a></li>
							<li><a id="btn" href="#" class="easyui-linkbutton btn-play" onclick="playConsoel(4)">播放</a></li>
							<li><a id="btn" href="#" class="easyui-linkbutton btn-play" onclick="p10()">快进10秒</a></li>
						</ul>
					</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>