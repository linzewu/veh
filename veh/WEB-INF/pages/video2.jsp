<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>动车检测视频监控系统</title>


<script type="text/javascript" src="/veh/js/easyui/jquery.min.js"></script>
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
	var tempInfo=${tempVideo};
	if((playInfo==""||playInfo=="[]")&&tempInfo.length>0){
		
		$(function(){
			
			var  vlc=document.getElementById("vlc");
			var vlcSound; // vlc音量大小（初始化默认为50）
			var videoLength; // 视频总时长
			var then_time; // 播放开始时间(播放开始的日期，看下面实现代码，它是毫秒哦)
			var isPlaying=0; // 是否播放状态 （0 未播放 1 播放）
			$.each(tempInfo,function(i,n){
				$("#info_hphm").val(n.hphm);
				var li=$("<li> <input  type=\"button\"  value='"+n.title+"' style=\"width:180px;\" /></li>");
				$(".check-menu").append(li);
				var urlList =n.url.split("\\");
				alert("http://192.168.51.201:8080/video2/"+ urlList[4]+"/"+ urlList[5]);
				var itemId = vlc.playlist.add(n.url);
				li.find("input").click(function(){
					vlc.playlist.playItem(itemId);
				});
			});
			
		})
		
	}else{
		var cIP=null;

		var zkjms=0;
		
		var currentPlayIndex=null;
		
		var currentIp;
		var currentParam;
		
		var longTime;
		
		var autoPlay=true;

		$(function () {
			var  vlc=document.getElementById("vlc");
			var vlcSound; // vlc音量大小（初始化默认为50）
			var videoLength; // 视频总时长
			var then_time; // 播放开始时间(播放开始的日期，看下面实现代码，它是毫秒哦)
			var isPlaying=0; // 是否播放状态 （0 未播放 1 播放）

			var name = getjczmc('${param.jyjgbh}');
			$("#jyjgbh").val(name);
			
			if($.trim(playInfo)==""){
				LogMessage("无法获取车辆播放信息");
				return;
			}
			playInfo=$.parseJSON(playInfo);
			playInfo = playInfo.sort(function(a,b){
				if(a.jyxm.localeCompare(b.jyxm)!=0){
					return a.jyxm.localeCompare(b.jyxm);
				}else{
				
					if(a.jycs>b.jycs){
						return 1;
					}else{
						return -1;
					}
				}
			});
			$("#info_hphm").val(playInfo[0].hphm);
			
			playInfo=playInfo.sort(function(a,b){
				if(a.jyxm=="F1"){
					return -1;
				}else{
					return 0;
				}
			});
			
			$.each(playInfo,function(i,n){
				var li=$("<li> <input  type=\"button\"  value='"+(getjyxm(n.jyxm)+"  "+n.jycs)+"' style=\"width:180px;\" /></li>");
				$(".check-menu").append(li);
				//$.parser.parse('.check-menu');
				var channel=n.channel==null?"0":n.channel;
				
				var itemId = vlc.playlist.add("http://192.168.51.201:8080/video/${param.jylsh}_"+n.jycs+"_"+n.jyxm+"_"+channel+".mp4");
				
				li.find("input").click(function(){
					vlc.playlist.playItem(itemId);
				});
			});
			
			$(".check-menu li input").each(function(i,n){
				$(n).val();
			});
			
			
			setTimeout(function(){
				$(".check-menu li:eq(0) input").click();
			},1000);
			
		});
	}
	

	
	

	function LogMessage(msg){
		
		
	}
	
	function screenFull(){
		var  vlc=document.getElementById("vlc");
		vlc.video.toggleFullscreen();
	}
	
	function kj(steep){
		vlc.input.rate=steep;
	}
	
</script>
</head>
<body>
	<div id="cc">

		<div   style="height: 50px;"  >
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
		<div style="width: 1366px;">
			<div  style="width: 250px;  height: 542px;float: left;">
				<ul class="check-menu">
					
				</ul>
			</div>
			<div style="float: left;">
				<!--[if IE]>
				   <object type='application/x-vlc-plugin' id='vlc' events='True'
				       classid='clsid:9BE31822-FDAD-461B-AD51-BE1D1C159921' width="1024" height="580">
				          <param name='mrl' value='' />
				          <param name='volume' value='50' />
				          <param name='autoplay' value='false' />
				          <param name='loop' value='false' />
				          <param name='fullscreen' value='true' />
				          <param name='controls' value='true'>
				          <param name='vout' value='direct2d'>
				          
				    </object>
				<![endif]-->
				<!--[if !IE]><!-->
				    <object type='application/x-vlc-plugin' id='vlc' events='True' style="width: 1024px;height: 580px;">
				        <param name='mrl' value='' />
				        <param name='volume' value='50' />
				        <param name='autoplay' value='true' />
				         <param name='controls' value='true'>
				        <param name='vout' value='direct2d'>
				    </object>
				<!--<![endif]-->
				<div style="text-align: center;width: 100%;height: 30px; ">
					<input value="正常" type="button"  style="margin-left: 20px;margin-top: 5px;" onclick="kj(1)">
					<input value="快进x2" type="button" style="margin-left: 20px;margin-top: 5px;" onclick="kj(2)">
					<input value="快进x4" type="button" style="margin-left: 20px;margin-top: 5px;" onclick="kj(4)">
				</div>
			</div>
			
		</div>
	</div>
	
</body>
</html>