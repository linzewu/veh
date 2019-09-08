<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

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
<script src="/veh/codebase/webVideoCtrl.js"></script>

<style type="text/css">
.plugin{
	margin: 0 auto;
}
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
	

	var cIP=null;

	var zkjms=0;
	
	var currentPlayIndex=null;
	
	var currentIp;
	var currentParam;
	
	var longTime;
	
	var autoPlay=true;

	$(function () {
		// 检查插件是否已经安装过
	    var iRet = WebVideoCtrl.I_CheckPluginInstall();
		if (-2 == iRet) {
			alert("您的Chrome浏览器版本过高，不支持NPAPI插件！");
			return;
		} else if (-1 == iRet) {
	        $.messager.alert("提示","您还未安装过插件，请安装WebComponentsKit.exe！","info",function(){
				window.open("/veh/codebase/WebComponentsKit.exe")
			});
			return;
	    }

		var oPlugin = {
			iWidth: 800,			// plugin width
			iHeight: 500			// plugin height
		};
		
		// 初始化插件参数及插入插件
		WebVideoCtrl.I_InitPlugin(oPlugin.iWidth, oPlugin.iHeight, {
	        bWndFull: true,//是否支持单窗口双击全屏，默认支持 true:支持 false:不支持
	        iWndowType: 1,
			cbSelWnd: function (xmlDoc) {
				
			}
		});
		WebVideoCtrl.I_InsertOBJECTPlugin("divPlugin");

		// 检查插件是否最新
		if (-1 == WebVideoCtrl.I_CheckPluginVersion()) {
			  $.messager.alert("提示","您还未安装过插件，请安装WebComponentsKit.exe！","info",function(){
					window.open("/veh/codebase/WebComponentsKit.exe")
				});
			return;
		}

		var name = getjczmc('${param.jyjgbh}');
		$("#jyjgbh").val(name);

		if($.trim(playInfo)==""){
			LogMessage("无法获取车辆播放信息");
			return;
		}
		playInfo=$.parseJSON(playInfo);
		$("#info_hphm").val(playInfo[0].hphm);
		$.each(playInfo,function(i,n){
			var li=$("<li><a  href='javascript:void(0)' class=\"easyui-linkbutton c6 check-item\">"+getjyxm(n.jyxm)+"  "+n.jycs+"</a></li>");
			$(".check-menu").append(li);
			$.parser.parse('.check-menu');
			li.find("a").click(function(){
				 $("#es").slider("setValue",0);
				logout();
				var oLiveView = {
						iProtocol: 1,			// protocol 1：http, 2:https
						szIP: n.ip,	// protocol ip
						szPort: "80",			// protocol port
						szUsername: n.userName,	// device username
						szPassword: n.password,	// device password
						iStreamType: 1,			// stream 1：main stream  2：sub-stream  3：third stream  4：transcode stream
						iChannelID: n.channel,			// channel no
						bZeroChannel: false		// zero channel
					};
				
					// 登录设备
					WebVideoCtrl.I_Login(oLiveView.szIP, oLiveView.iProtocol, oLiveView.szPort, oLiveView.szUsername, oLiveView.szPassword, {
						success: function (xmlDoc) {
							cIP=n.ip;

							var kssj=n.kssj;
			            	var jssj=n.jssj;
			            	
			            	
			            	
			            	
			            	if(n.jyxm=="H4"){
			            		var ksl = StringToDate(kssj);
		            			var t1 = ksl.getTime();
		            			ksl.setTime(t1+(20*1000));
		            			kssj=ksl.Format("yyyy-MM-dd HH:mm:ss");
		            		}
			            	
			            	var tysj=parseInt($("#tysj").val());
			            	tysj+=zkjms;

			            	if(tysj!=null){
			        			zkjms+=tysj;
			        		}else{
			        			zkjms=0;
			        		}
			            	
			            	if(tysj!=0||tysj!=""){
			            		var ksl = StringToDate(kssj).getTime()+(tysj*1000);
			            		var jsl = StringToDate(jssj).getTime()+(tysj*1000);
			            		
			            		kssj=new Date(ksl).Format("yyyy-MM-dd HH:mm:ss");
			            		jssj=new Date(jsl).Format("yyyy-MM-dd HH:mm:ss");
			            	}
			            	longTime = StringToDate(jssj).getTime()-StringToDate(kssj).getTime();
			            	
			            	currentParam={
									iChannelID: n.channel,
									szStartTime: kssj,
									szEndTime: jssj,
									ysStartTime:kssj
							};
			            	currentIp=n.ip;
			            	
							var isPaly = WebVideoCtrl.I_StartPlayback(n.ip,currentParam);
							currentPlayIndex=i;
			                //LogMessage("开始时间回放成功，起止时间："+kssj+" ~ "+jssj+"！推移时间："+ tysj);
			                
			                
			                
						},
						error:function(){
							 LogMessage("登录录像机失败");
						}
					});
			}); 
		});
		
		setInterval(function(){
			var playWindowInfo = WebVideoCtrl.I_GetWindowStatus();
			
			if(playWindowInfo.length==0&&currentPlayIndex==null){
				$(".check-menu li:eq(0) a").click();
			}else if(currentPlayIndex+1<playInfo.length&&playWindowInfo.length==0){
				$(".check-menu li:eq("+(currentPlayIndex+1)+") a").click();
				
			}
			
		},2000);
		
		
		setInterval(function(){
			var playWindowInfo = WebVideoCtrl.I_GetWindowStatus();
			var now = $("#es").slider("getValue");
			if(playWindowInfo.length==1&&now<100){
				var s = (100/(longTime/(1000))); 
				 $("#es").slider("setValue",s+now);
			}
			
		},1000);
		
		
		
		// 关闭浏览器
		$(window).unload(function () {
			WebVideoCtrl.I_Stop();
		});
	});
	

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
	
	
		
	function pause(){

		var iRet = WebVideoCtrl.I_Pause();
		if (0 == iRet) {
			LogMessage("暂停成功！");
		} else {
			LogMessage("暂停失败！");
		}
	}

	function resume(){

		var iRet = WebVideoCtrl.I_Resume();
		if (0 == iRet) {
			LogMessage("恢复成功！");
		} else {
			LogMessage("恢复失败！");
		}
	}

	function playFast(){

		var iRet = WebVideoCtrl.I_PlayFast();
		if (0 == iRet) {
			LogMessage("快放成功！");
		} else {
			LogMessage("快放失败！");
		}
	}

	// 慢放
	function playSlow() {

		var iRet = WebVideoCtrl.I_PlaySlow();
		if (0 == iRet) {
			LogMessage("慢放成功！");
		} else {
			LogMessage("慢放失败！");
		}
	}

	
	
	window.onbeforeunload=function(){
		if(event.clientY<0){
			unLogin();
		}
	}
	
	function logout(){
		WebVideoCtrl.I_Stop();
		if(cIP!=null){
			WebVideoCtrl.I_Logout(cIP);
		}
		
	}
	
	
	function unLogin(){
		WebVideoCtrl.I_Stop();
		WebVideoCtrl.I_Logout(cIP);
	}
window.onunload=unLogin;

function slideEnd(value){
	
	if(currentIp!=null&&currentParam!=null){
		var addTime = Math.ceil((value/100)*longTime);
		
		var ksl = StringToDate(currentParam.ysStartTime).getTime();
		currentParam.szStartTime=new Date(ksl+addTime).Format("yyyy-MM-dd HH:mm:ss");
		WebVideoCtrl.I_Stop();
		WebVideoCtrl.I_StartPlayback(currentIp,currentParam)
		

	}
}


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
				<label>监测站名称：</label>
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
					<div id="divPlugin" class="plugin"></div>
					<div style="width: 800px;margin-left: 100px;">
						<input class="easyui-slider" id="es" style="width:600px;" data-options="showTip:true,rule: [0,'|',25,'|',50,'|',75,'|',100],onSlideEnd:slideEnd,onComplete:slideEnd" >
					</div>
					
				</div>
				<div data-options="region:'south'" style="height: 50px;">
					<div class="playTool">
						<ul>
							<li><a id="btn1" href="#" class="easyui-linkbutton btn-play" onclick="pause()">暂停</a></li>
							<li><a id="btn2" href="#" class="easyui-linkbutton btn-play" onclick="resume()">播放</a></li>
						<!-- 	<li><a id="btn3" href="#" class="easyui-linkbutton btn-play" onclick="playFast()">快放</a></li>
							<li><a id="btn4" href="#" class="easyui-linkbutton btn-play" onclick="playSlow()">慢放</a></li> -->
						</ul>
					</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>