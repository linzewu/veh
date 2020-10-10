function loadCLHGZ(barArray, strbarcode) {
	var dataInfo = {};
	dataInfo["hgzbh"] = barArray[2];
	dataInfo["ccrq"] = barArray[3];

	dataInfo["zzcmc"] = barArray[4];

	var clpps = barArray[7].split("/");
	if (clpps.length > 1) {
		dataInfo["clpp1"] = clpps[0];
		dataInfo["clpp2"] = clpps[1];
	} else {
		dataInfo["clpp1"] = clpps[0];
	}
	dataInfo["clxh"] = barArray[8];
	
	dataInfo["zzg"] = "156";

	dataInfo["clsbdh"] = barArray[13];
	dataInfo["fdjh"] = barArray[15];
	dataInfo["fdjxh"] = barArray[16];

	dataInfo["rlzl"] = barArray[17];
	dataInfo["pl"] = barArray[19];
	dataInfo["gl"] = barArray[20];
	dataInfo["zxxs"] = barArray[21];

	var qljs = barArray[22].split("/");

	dataInfo["qlj"] = qljs[0];

	// 计算后轮距 29是轴数
	var hlj = getHlj(barArray[23], barArray[29]);

	dataInfo["hlj"] = hlj;

	dataInfo["lts"] = barArray[24];
	dataInfo["ltgg"] = barArray[25];
	dataInfo['hbdbqk'] = barArray[18];
	dataInfo['dpid'] = barArray[11];

	var zxzs = 1;
	if (qljs.length > 0) {
		zxzs = qljs.length;
	}

	var gbthps = "";
	if (barArray[6] == "挂车") {
		gbthps = getGbthps("-/" + barArray[26], 1);
	} else {
		gbthps = getGbthps(barArray[26], zxzs);
	}

	dataInfo["gbthps"] = gbthps;
	var zj = getZj(barArray[27]);
	dataInfo["zj"] = zj;

	dataInfo["zs"] = barArray[29];
	dataInfo["cwkc"] = barArray[30];
	dataInfo["cwkk"] = barArray[31];

	dataInfo["cwkg"] = barArray[32];
	dataInfo["hxnbcd"] = barArray[33];
	dataInfo["hxnbkd"] = barArray[34];
	dataInfo["hxnbgd"] = barArray[35];
	dataInfo["zzl"] = barArray[36];

	dataInfo["hdzzl"] = barArray[37];
	dataInfo["zbzl"] = barArray[38];
	dataInfo["zqyzl"] = barArray[40];

	var hdzks = barArray[41].split("/");
	dataInfo["hdzk"] = hdzks[0];

	var qpzks = barArray[43].split("+");

	dataInfo["qpzk"] = qpzks[0];
	dataInfo['hdzk']=qpzks[0];
	if (qpzks.length > 1) {
		dataInfo["hpzk"] = qpzks[1];
		dataInfo['hdzk']=qpzks[0]+qpzks[1];
	}
	dataInfo["bz"] = barArray[50];
	$("#vehinfo").form("load", dataInfo);
	
}

function setSFZXX(barArray) {
	$("#syrs").text(barArray[1]);
	$("#sfzs").text( barArray[6]);
	$("#syr").val(barArray[1]);
	$("#sfz").val( barArray[6]);
	$("#dz").val(barArray[5]);
}

function getHlj(m_SourceValue, m_zs) {
	// 是否为数字
	var newgbthps;
	var i_lj = 0;

	if (m_SourceValue.length == 0) {
		return "";
	}
	var i_zs = parseInt(m_zs);
	if (m_SourceValue.indexOf("/") > 0) {
		var paraArray = m_SourceValue.split("/"); // 根据","分割成字符串数组
		if (i_zs == 3) {
			i_lj = parseInt(paraArray[0]);
		} else {
			i_lj = parseInt(paraArray[paraArray.length - 2]);
		}
	} else {
		if (m_SourceValue.indexOf("+") > 0) {
			var paraArray = m_SourceValue.split("+"); // 根据","分割成字符串数组
			var i;
			for (i = 0; i < paraArray.length; i++) {
				if (!isNaN(paraArray[i])) {
					// 如为数字，直接判断
					i_lj += parseInt(paraArray[i]);
				}
			}
		} else {
			i_lj = parseInt(m_SourceValue);
		}
	}
	return i_lj;
}

function getGbthps(m_SourceValue, m_zxzs) {

	// 是否为数字
	var newgbthps;
	var i_gbthps = 0;
	if (m_SourceValue.length == 0) {
		return "";
	}
	// m_SourceValue="-/"+m_SourceValue;
	var paraArray = m_SourceValue.split(","); // 根据","分割成字符串数组
	var i;
	var j;
	var k
	for (i = 0; i < paraArray.length; i++) {
		if (!isNaN(paraArray[i])) {
			// 如为数字，直接判断
			i_gbthps = parseInt(paraArray[i]);
		} else {
			i_gbthps = 0;
			if (paraArray[i].indexOf("/") > -1) {

				for (k = 0; k < m_zxzs; k++) {
					paraArray[i] = paraArray[i].substr(paraArray[i]
							.indexOf("/") + 1)
				}
				paraArray[i] += "/";

				while (paraArray[i].indexOf("/") > -1) {
					newgbthps = paraArray[i].substring(0, paraArray[i]
							.indexOf("/"));
					if (!isNaN(newgbthps)) {
						i_gbthps += parseInt(newgbthps);
					} else {
						if (newgbthps.indexOf("+") > 0) {
							var valueSplit = newgbthps.split("+");
							for (j = 0; j < valueSplit.length; j++) {
								// 为了应对钢板弹簧片数形式为‘-/8+-’的情况，所以增加valueSplit[j]是否为数字的判断
								if (!isNaN(valueSplit[j])) {
									i_gbthps += parseInt(valueSplit[j]);
								}
							}
							// i_gbthps = i_gbthps+(parseInt(valueSplit[0]) +
							// parseInt(valueSplit[1]));
						}
					}
					paraArray[i] = paraArray[i].substr(paraArray[i]
							.indexOf("/") + 1);
				}
			} else {
				if (paraArray[i].indexOf("+") > 0) {
					var valueSplit = paraArray[i].split("+");
					for (j = 0; j < valueSplit.length; j++) {
						if (!isNaN(valueSplit[j])) {
							i_gbthps += parseInt(valueSplit[j]);
						}
					}
					// i_gbthps=parseInt(valueSplit[0]) +
					// parseInt(valueSplit[1]);
				}
			}
		}
	}
	if (i_gbthps == 0) {
		return "";
	} else {
		return i_gbthps;
	}
}

function getZj(m_SourceValue) {

	// 是否为数字
	var newgbthps;
	var i_zj = 0;
	if (m_SourceValue.length == 0) {
		i_zj = 0;
	}
	var paraArray = m_SourceValue.split("+"); // 根据","分割成字符串数组
	var i;
	for (i = 0; i < paraArray.length; i++) {
		if (!isNaN(paraArray[i])) {
			// 如为数字，直接判断
			i_zj += parseInt(paraArray[i]);
		}
	}
	return i_zj;
}

function readQrLabel() {
		
		var barcodetemp = "";
		try{
			var strbarcode = vehPrinter.GetQrText();
			if (strbarcode == "") {
				return 0;
			}
			if (strbarcode == "-1") {
				$.messager.alert("提示", "条码信息有误!");
				return 0;
			}
			
			var barArray = strbarcode.split("|");
		
			var strBarCodeType = barArray[0];
			if (strBarCodeType.split("_")[0] == "ZCCCHGZ") {
				loadCLHGZ(barArray, strbarcode);
				return;
			}
		
			if (strBarCodeType == "SFZXX_2.0") {
				setSFZXX(barArray);
				return;
			}
		}catch (e) {
			//alert(e);
			return ;
		}
}

var userInfo = $.ajax({
	url : "/veh/user/getCurrentUser",
	async : false,
	type:'POST'
}).responseText;

userInfo=$.parseJSON(userInfo);

var allRole = $.ajax({
	url : "/veh/user/getAllRole",
	async : false,
	type:'POST'
}).responseText;
allRole=$.parseJSON(allRole);

var sysMId = 0;
var removeCou = 0;

var userRoleInfo = $.ajax({
	url : "/veh/user/getRolesByUser",
	async : false,
	type:'POST'
}).responseText;

userRoleInfo=$.parseJSON(userRoleInfo);

if(userInfo.state==600){
	 window.location.href="/veh/html/login.html";
}

$(function(){
	//if(userInfo.userState==0||userInfo.userState==null||userInfo.pwOverdue=="Y"){
	if(userInfo.pwOverdue=="Y"){
		$('#win_password').window('open');
	}
	
	//启动扫描二维码功能
	window.setInterval(function(){
		if(vehPrinter){
			readQrLabel();
		}
	},200);
	
});

document
		.write("<script language='javascript' src='/veh/bps/all.js' ></script>");

var veh = {
	jgpd:function(jg){
		if(jg==1){
			return "O";
		}
		if(jg==2){
			return "X";
		}
		if(jg==null||jg==0){
			return "—";
		}
		
	},
	clone:function(){
		var row = $("#checkedVehList").datagrid("getSelected");
		if(!row){
			$.messager.alert("提示","请选择车辆！");
			return;
		}else{
			var temp =row;
			temp['hphm']='';
			temp['ccrq']='';
			temp['ccdjrq']='';
			temp['jyyxqz']='';
			temp['bxzzrq']='';
			temp['jyrq']='';
			temp['ycy']='';
			$("#vehinfo").form("load",temp);
			veh.setVehB16(temp['zs']);
			veh.setVehH14();
			veh.setVehB0();
			veh.setVehS1();
			
		}
	},
	loadVehCheckInfo:function(index,row){
		
		$("#win_form_veh").form("clear");
		$("#win_checke_veh_info").window("open");
		if(row){
			var ss=row;
			ss['sf']=ss['hphm'].substring(0,1);
			var hpmh = ss['hphm'].substring(1);
			
			 $.post("/veh/veh/getTestVehBylsh",{jylsh:ss.jylsh},function(data){
				 	
				 	if(ss.checkType==1){
				 		$.extend(ss, data);
				 	}
				 
					$("#win_form_veh").form("load",ss);
					$("#win_form_veh input[sid=hphm]").textbox("setValue",hpmh);
					var jyxmArry = row.jyxm.split(",");
					$(".vehCheckeItem").empty();
					$.each(jyxmArry,function(i,n){
						var itemName;
						if(ss.checkType==1){
							itemName= comm.getParamNameByValue('zhjyxm',n)
						}else{
							itemName=comm.getParamNameByValue('jyxm',n);
						}
						
						
						var li ="<li><a href=\"#\" class=\"easyui-linkbutton c6\" >"+itemName+"</a></li>";
						$(".vehCheckeItem").append(li);
						$.parser.parse('.vehCheckeItem');
					});
			 });
			
		
		}
		$("#win_checke_veh_info .easyui-textbox").textbox("disable");
		$("#win_checke_veh_info .easyui-combobox").combobox("disable");
		$("#win_checke_veh_info .easyui-datebox").datebox("disable");
		$("#win_checke_veh_info .easyui-numberbox").numberbox("disable");
		$("#win_checke_veh_info .easyui-linkbutton").linkbutton("disable");
	},
	uphphm:function(n,o){
		$(this).textbox("setValue",n.toUpperCase());
	},
	vehCheckingQuery:function(){
		var hpzl = $("#quer_checking_hpzl").combobox("getValue");
		var hphm = $("#quer_checking_hphm").val();
		var param={};
		if(hphm&&$.trim(hphm)!=""){
			param.hphm=hphm;
		}
		if(hpzl&&$.trim(hpzl)!=""){
			param.hpzl=hpzl;
		}
		param.statusArry="0,1";
		$("#checkingVehList").datagrid("reload",param);
	},
	vehCheckedQuery:function(){
		var clxh = $("#quer_checked_clxh").val();
		var param={};
		if(clxh&&$.trim(clxh)!=""){
			param.clxh=clxh;
		}
		param.statusArry="2,3";
		$("#checkedVehList").datagrid("reload",param);
	}
	,login:function(){
		
		var flag = $("#vehinfo").form("validate");
		if(flag){
			var xms = $(".panel_cyxm :checked").length;
			 var jcxdh= $("#_jcxdh").combobox("getValue");
			 if(jcxdh==""){
				 $.messager.alert("提示","请选择检测线！");
				 return false;
			 }
			 if(xms<=0){
				 $.messager.alert("提示","请选择检验项目！");
				 return false;
			 }else{
				 $.messager.progress({
						title:"提示",
						msg:"数据保存中。。。"
				 });
				 var str_jyxm="";
				 $.each($(".panel_cyxm :checked"),function(i,n){
					 str_jyxm+=","+$(n).val();
				 });
				 if(str_jyxm.length>0){
					 str_jyxm=str_jyxm.substring(1, str_jyxm.length);
				 }
				// $("#str_jyxm").val(str_jyxm);
				 
				 var param=$("#vehinfo").serializeJson();
				 param['hphm'] = param['sf']+param['hphm'];
				 param['jcxdh']=jcxdh;
				 param['jyxm']=str_jyxm;
				 param['ycysfzh']=$("#_ycy").combobox("getText");
				 $.post("/veh/veh/vehLogin",param,function(data){
					 	data=$.parseJSON(data);
						var head = null;
						var body = null;
						if ($.isArray(data)) {
							head = data[0];
						} else {
							head = data["head"];
							body = data["body"]
						}
						if (head["code"] == 1){
						// $("#vehinfo").form("clear");
							$("#panel-vheInfo").panel("refresh");
						// veh.setDefaultConfig();
							$(":checkbox[name=jyxm]").prop("checked",false);
							$(":checkbox[name=jyxm][value=F1]").prop("checked",true);
							$("#checkingVehList").datagrid("reload");
							if(data["checkLog"]){
								$.messager.alert("提示","本地登录成功！\n监控平台登录："+data["checkLog"].message);
							}else{
								$.messager.alert("提示","本地登录成功！");
							}
						}else{
							$.messager.alert("提示",head.message,"error");
						}
					},"json").error(function(msg){
						$.messager.progress("close");
						if(msg.status==500){
							$.messager.alert("错误","服务器响应错误！","error");
							return;
						}
						if(msg.status==400){
							$.messager.alert("错误","无法访问该资源！","error");
							return;
						}
						$.messager.alert("错误","请求失败！","error");
					}).complete(function(){
						$.messager.progress("close");
					});
			 }
		}
	},
	lsLogin:function(){
		var flag = $("#vehinfo").form("validate");
		if(flag){
			 var xms = $(".panel_lsxm :checked").length;
			 if(xms<=0){
				 $.messager.alert("提示","请选择路试项目！");
				 return false;
			 }else{
				 $.messager.progress({
						title:"提示",
						msg:"数据加载中。。。"
					});
				 var str_jyxm="";
				 $.each($(".panel_lsxm :checked"),function(i,n){
					 str_jyxm+=","+$(n).val();
				 });
				 if(str_jyxm.length>0){
					 str_jyxm=str_jyxm.substring(1, str_jyxm.length);
				 }
				 // $("#str_jyxm").val(str_jyxm);
				var param=$("#vehinfo").serializeJson();
				param['hphm'] = param['sf']+param['hphm'];
				param['jyxm']=str_jyxm;
				$.post("/veh/veh/vehLogin",param,function(data){
					data=$.parseJSON(data);
					var head = null;
					var body = null;
					if ($.isArray(data)) {
						head = data[0];
					} else {
						head = data["head"];
						body = data["body"]
					}
					if (head["code"] == 1){
						$("#vehinfo").form("clear");
						veh.setDefaultConfig();
						$(":checkbox[name=jyxm]").prop("checked",false);
						$(":checkbox[name=jyxm][value=F1]").prop("checked",true);
						$("#checkingVehList").datagrid("reload");
						$.messager.alert("提示","车辆登录成功。");
					}
				},"json").error(function(msg){
					$.messager.progress("close");
					if(msg.status==500){
						$.messager.alert("错误","服务器响应错误！","error");
						return;
					}
					if(msg.status==400){
						$.messager.alert("错误","无法访问该资源！","error");
						return;
					}
					$.messager.alert("错误","请求失败！","error");
					
				}).complete(function(){
					$.messager.progress("close");
				});
			 }
		}
	},
	vehUnlogin:function(){
		
		var row = $("#checkingVehList").datagrid("getSelected");
		if(row){
			$.messager.confirm("请确认","您是否要退办："+row.hphm,function(r){
				if(r){
					$.messager.progress({
						title:'提示',
						msg:"正在退办车辆。。。"
					});
					$.post("/veh/veh/vheUnLogin",row,function(data){
					},"json").complete(function(data){
						$.messager.progress("close");
						 $("#checkingVehList").datagrid("reload");
					}).error(veh.error);
				}
			});
		}else{
			$.messager.alert("提示","请选择车辆。");
		}
	}
	,getVehinfo : function() {
		var sf = $("input[sid=sf]").combobox("getText");
		var hphm = sf + $("input[sid=hphm]").textbox("getValue");
		var hpzl = $("input[sid=hpzl]").combobox("getValue");
		var clsbdh = $("input[sid=clsbdh]").textbox("getValue");
		
		var jylb = $("#jylb").combobox("getValue");

		if (sf == "" || hphm == "" || hpzl == "" || clsbdh == "") {
			$.messager.alert("提示", "获取车辆基本信息必须输入号牌号码、号牌种类、车辆识别代号后4位");
			return false;
		}

		var param = {
			"hphm" : hphm,
			"hpzl" : hpzl,
			"clsbdh" : clsbdh,
			"jylb":jylb
		}

		$.messager.progress({
			title : "请稍等",
			msg : "获取车辆基本信息",
			text : "拼命加载..."
		});

		// 定时器ID
		var did;
		var tempCount = 0;

		var callback = function(data) {
			var head = null;
			var body = null;
			data = $.parseJSON(data);

			if ($.isArray(data)) {
				head = data[0];
			} else {
				head = data["head"];
				body = data["body"]
			}

			if (head["code"] == 1) {
				$.messager.progress("close");
				body[0]['jyrq'] = body['djrq']
				body[0]['jyyxqz'] = body['yxqz'];
				try{
					$("#vehinfo").form("load", body[0]);
				}catch (e) {
					$("input[sid=clsbdh]").textbox("setValue",body[0].clsbdh);
					$("input[sid=zbzl]").numberbox("setValue",body[0].clsbdh);
				}
				
				if(body[0]['zt']!='A'){
					$.messager.alert("提示","该机动车状态："+comm.getParamNameByValue('jdczt',body[0]['zt']));
				}
				
				if (did) {
					clearInterval(did);
					did = null;
					tempCount = 0;
				}
			} else {
				if (!did) {
					did = setInterval(
							function() {
								if (tempCount >= 3) {
									clearInterval(did);
									tempCount = 0;
									did = null;
									$.messager.progress("close");
									$.messager
											.alert("警告",
													"无法获取该机动车基本信息，请检查输入的信息是否正确，如信息正确，请检查查验平台数据是否正常交换。")

								} else {
									$.messager.progress({
										title : "请稍等",
										msg : "获取车辆基本信息",
										text : "第" + (tempCount + 1)
												+ "次尝试获取基本信息"
									});
									veh.ajaxVeh("/veh/veh/getVehInfo", param,
											callback);
								}
								tempCount++;
							}, 1000 * 5);
				}
			}
		};
		this.ajaxVeh("/veh/veh/getVehInfo", param, callback);
	},
	ajaxVeh : function(url, param, success) {
		$.ajax({
			url : url,
			data : param,
			success : success,
			dataType : "json",
			type : "post",
			timeout : 1000 * 10,
			error : this.error
		})
	},
	error : function(XMLHttpRequest, textStatus, errorThrown) {
		$.messager.progress("close");
		if ("timeout" == textStatus) {
			$.messager.alert("提示", "请求超时，请检查监管平台网络是否畅通", 'error');
		} else {
			$.messager.alert("错误", "请求发送失败", 'error');
		}
	},

	getVehCheckItem : function() {
		var param = {
			"jylb" : "01",
			"syxz" : ""
		}
		this.ajaxVeh("/veh/veh/getVehCheckItem", param, function(data) {
			data = $.parseJSON(data);
		});
	},

	selectCheckItem : function() {

	},
	setVehB16 : function(zs) {
		for (var i = 1; i <= 6; i++) {
			if (i <= zs) {
				$(":checkbox[name=jyxm][value=B" + i + "]").prop("disabled",
						false);
				$(":checkbox[name=jyxm][value=B" + i + "]").prop("checked",
						true);
			} else {
				$(":checkbox[name=jyxm][value=B" + i + "]").prop("checked",
						false);
				$(":checkbox[name=jyxm][value=B" + i + "]").prop("disabled",
						true);
			}

		}
	},
	setVehH14 : function() {

		var qzdz = $("input[textboxname=qzdz]").combobox("getValue");
		var cllx = $("input[textboxname=cllx]").combobox("getValue");
		$(":checkbox[name=jyxm][value^=H]").prop("disabled", false);
		$(":checkbox[name=jyxm][value^=H]").prop("checked", false);
		
		if (cllx && cllx != "") {
			var cllxChar = cllx.substring(0, 1);
			if (cllxChar == "B" || cllxChar == "G") {
				return false;
			}
		} else {
			return false;
		}
		if (qzdz) {
			if (qzdz == "01" || qzdz == "02") {
				$(":checkbox[name=jyxm][value^=H]").prop("checked", true);
			} else if (qzdz == "03" || qzdz == "04") {
				$(":checkbox[name=jyxm][value=H1]").prop("checked", true);
				$(":checkbox[name=jyxm][value=H4]").prop("checked", true);
				$(":checkbox[name=jyxm][value=H2]").prop("disabled", true);
				$(":checkbox[name=jyxm][value=H3]").prop("disabled", true);
				
			} else if (qzdz == "05") {
				$(":checkbox[name=jyxm][value=H1]").prop("checked", true);
				$(":checkbox[name=jyxm][value=H2]").prop("disabled", true);
				$(":checkbox[name=jyxm][value=H3]").prop("disabled", true);
				$(":checkbox[name=jyxm][value=H4]").prop("disabled", true);
			}
		}
	},
	setVehL14:function(){
		var zs = $("input[textboxname=zs]").numberbox("getValue");
		var cllx = $("input[textboxname=cllx]").combobox("getValue");
		for (var i = 1; i <= 4; i++){
			$(":checkbox[name=jyxm][value=L" + i + "]").prop("checked",
					false);
			$(":checkbox[name=jyxm][value=L" + i + "]").prop("disabled",
					false);
		}
		
		if(zs&&zs>=3){
			for (var i = 1; i <= zs; i++) {
				if (i == zs) {
					$(":checkbox[name=jyxm][value=L" + i + "]").prop("checked",
							false);
					/*$(":checkbox[name=jyxm][value=L" + i + "]").prop("disabled",
							true);*/
				}else if(i ==1 &&(cllx.indexOf("G")>=0||cllx.indexOf("B")>=0)){
					$(":checkbox[name=jyxm][value=L" + i + "]").prop("disabled",
							false);
					$(":checkbox[name=jyxm][value=L" + i + "]").prop("checked",
							true);
				}else if(i >1 &&(cllx.indexOf("G")>=0||cllx.indexOf("B")>=0||cllx.indexOf("H")>=0||cllx.indexOf("Z")>=0||cllx.indexOf("Q")>=0)){
					$(":checkbox[name=jyxm][value=L" + i + "]").prop("disabled",
							false);
					$(":checkbox[name=jyxm][value=L" + i + "]").prop("checked",
							true);
				} else {
					$(":checkbox[name=jyxm][value=L" + i + "]").prop("checked",
							false);
					/*$(":checkbox[name=jyxm][value=L" + i + "]").prop("disabled",
							true);*/
				}
			}
		}
		
		
	},
	setVehB0 : function() {
		// var zs = $("input[numberboxname=zs]").numberbox("getValue");
		var ccdjrq = $("input[textboxname=ccdjrq]").datebox("getValue");
		var hdzk = $("input[textboxname=hdzk]").numberbox("getValue");
		var cllx = $("input[textboxname=cllx]").combobox("getValue");
		var syxz = $("input[textboxname=syxz]").combobox("getValue");
		// var qzdz = $("input[textboxname=qzdz]").combobox("getValue");

		$(":checkbox[name=jyxm][value=B0]").prop("checked", false);

		var cllxChar = cllx.substring(0, 1);
		var cllxChar2 = cllx.substring(0, 2);

		var isTimeout = true;

		if (ccdjrq != "") {
			var dateArray = ccdjrq.split("-");
			var djrqDate = new Date(Number(dateArray[0]) + 10,
					dateArray[1] - 1, dateArray[2]);
			var nowTime = new Date();
			var nowDate = new Date(nowTime.getFullYear(), nowTime.getMonth(),
					nowTime.getDate());

			if (djrqDate.getTime() > nowDate.getTime()) {
				isTimeout = false;
			}
		}

		if (cllxChar == "M") {
			return;
		} else {
			if (syxz != "A" || cllxChar != "K" || cllxChar2 == "K1"
					|| cllxChar2 == "K2" || isTimeout || hdzk >= 7
					|| cllx == "K39" || cllx == "K49") {
				$(":checkbox[name=jyxm][value=B0]").prop("checked", true);

			}
		}
	},
	setVehS1 : function() {
		var cllx = $("input[textboxname=cllx]").combobox("getValue");
		var syxz = $("input[textboxname=syxz]").combobox("getValue");
		var jylb = $("input[textboxname=jylb]").combobox("getValue");

		var cllxChar = cllx.substring(0, 1);
		var cllxChar2 = cllx.substring(0, 2);

		$(":checkbox[name=jyxm][value=S1]").prop("checked", false);
		
		if(jylb!="00"){
			return;
		}

		if (cllxChar == "H") {
			$(":checkbox[name=jyxm][value=S1]").prop("checked", true);
		}

		if (cllxChar == "K") {
			if (syxz != "A" || (cllxChar2 != "K3" && cllxChar2 != "`")) {
				$(":checkbox[name=jyxm][value=S1]").prop("checked", true);
			}
		}
	},
	setVehDC:function(){
		var ccdjrq = $("input[textboxname=ccdjrq]").datebox("getValue");
		var hdzk = $("input[textboxname=hdzk]").numberbox("getValue");
		var cllx = $("input[textboxname=cllx]").combobox("getValue");
		var syxz = $("input[textboxname=syxz]").combobox("getValue");
		
		$(":checkbox[name=jyxm][value=DC]").prop("checked", false);

		var cllxChar = cllx.substring(0, 1);
		var cllxChar2 = cllx.substring(0, 2);

		var isTimeout = true;

		if (ccdjrq != "") {
			var dateArray = ccdjrq.split("-");
			var djrqDate = new Date(Number(dateArray[0]) + 10,
					dateArray[1] - 1, dateArray[2]);
			var nowTime = new Date();
			var nowDate = new Date(nowTime.getFullYear(), nowTime.getMonth(),
					nowTime.getDate());

			if (djrqDate.getTime() > nowDate.getTime()) {
				isTimeout = false;
			}
		}

		if (cllxChar == "M"||cllxChar=="B"||cllxChar=="G") {
			return;
		} else {
			if (syxz != "A" || cllxChar != "K" || cllxChar2 == "K1"
					|| cllxChar2 == "K2" || isTimeout || hdzk >= 7
					|| cllx == "K39" || cllx == "K49") {
				$(":checkbox[name=jyxm][value=DC]").prop("checked", true);

			}
		}
		
	},
	setVehC1:function(){
		var ccdjrq = $("input[textboxname=ccdjrq]").datebox("getValue");
		var hdzk = $("input[textboxname=hdzk]").numberbox("getValue");
		var cllx = $("input[textboxname=cllx]").combobox("getValue");
		var syxz = $("input[textboxname=syxz]").combobox("getValue");
		
		$(":checkbox[name=jyxm][value=C1]").prop("checked", false);

		var cllxChar = cllx.substring(0, 1);
		var cllxChar2 = cllx.substring(0, 2);

		var isTimeout = true;

		if (ccdjrq != "") {
			var dateArray = ccdjrq.split("-");
			var djrqDate = new Date(Number(dateArray[0]) + 10,
					dateArray[1] - 1, dateArray[2]);
			var nowTime = new Date();
			var nowDate = new Date(nowTime.getFullYear(), nowTime.getMonth(),
					nowTime.getDate());

			if (djrqDate.getTime() > nowDate.getTime()) {
				isTimeout = false;
			}
		}

		if (cllxChar == "M") {
			return;
		} else {
			if (syxz != "A" || cllxChar != "K" || cllxChar2 == "K1"
					|| cllxChar2 == "K2" || isTimeout || hdzk >= 7
					|| cllx == "K39" || cllx == "K49") {
				$(":checkbox[name=jyxm][value=C1]").prop("checked", true);

			}
		}
		
	},
	intiEvents : function() {
		$("input[numberboxname=zs]").numberbox({
			"onChange" : function(newValue, oldValue) {
				veh.setVehB16(newValue);
				veh.setVehL14();
			}
		});
		
		$("input[textboxname=jylb]").combobox({
			"onChange" : function(newValue, oldValue) {
				veh.setVehS1(newValue);
			}
		});

		$("input[textboxname=cllx]").combobox({
			"onChange" : function(newValue, oldValue) {
				veh.setVehH14();
				veh.setVehB0();
				veh.setVehS1();
				veh.setVehDC();
				veh.setVehC1();
				veh.setVehL14();
			}
		});

		$("input[textboxname=qzdz]").combobox({
			"onChange" : function(newValue, oldValue) {
				var cllx = $("input[textboxname=cllx]").combobox("getValue");
				veh.setVehH14();
			}
		});

		$("input[textboxname=syxz]").combobox({
			"onChange" : function(newValue, oldValue) {
				veh.setVehB0();
				veh.setVehS1();
				veh.setVehDC();
				veh.setVehC1();
			}
		});

		$("input[textboxname=ccdjrq]").datebox({
			"onChange" : function(newValue, oldValue) {
				veh.setVehB0();
				veh.setVehDC();
				veh.setVehC1();
			}
		});

		$("input[textboxname=hdzk]").numberbox({
			"onChange" : function(newValue, oldValue) {
				veh.setVehB0();
				veh.setVehDC();
				veh.setVehC1();
			}
		});
		
		$("input[textboxname=zxzxjxs]").combobox({
			"onChange" : function(newValue, oldValue) {
				if(newValue==1){
					$(":checkbox[name=jyxm][value=A1]").prop("checked", true);
				}else{
					$(":checkbox[name=jyxm][value=A1]").prop("checked", false);
				}
			}
		});

		$("input[textboxname=cllx]").combobox(
				{
					filter : function(q, row) {
						var opts = $(this).combobox('options');
						q = q.toUpperCase();
						return row[opts.valueField].indexOf(q) == 0
								|| row[opts.textField].indexOf(q) >= 0;
					}
				});
		veh.setDefaultConfig();
	},
	setDefaultConfig:function(){
		$.post("/veh/veh/getDefaultConfig",function(data){
			data=$.parseJSON(data);
			$("input[sid=sf]").combobox("setValue",data.sf);
			$("input[sid=hphm]").textbox("setValue",data.cs);
		},"json").error(function(e){
			$.messager.alert("获取默认配置错误","错误类型:"+e.status);
		});
	},
	loginPageReload:function(){
		$("#panel-vheInfo").panel("refresh");
	}
}

var comm = {
	getBaseParames : function(type) {
		var array = [];
		for ( var i in bps) {
			var bp = bps[i];
			if (bp.type == type) {
				var map = {};
				map['value'] = bp.paramName;
				map['id'] = bp.paramValue;
				array.push(map);
			}
		}
		return array;
	},
	
	getParamNameByValue:function(type,value){
		for ( var i in bps) {
			var bp = bps[i];
			if (bp.type == type && bp.paramValue==value) {
				 return bp.paramName;
			}
		}
		return value;
	},
	toPage : function(target, title, url, param) {
		$(target).panel("setTitle",title);
		if (param) {
			$(target).panel({
				"queryParams" : param
			});
		}
		$(target).panel("refresh", url);
	},
	createMume : function(id, data, showPage) {
		var ul = $("#" + id);
		ul.empty();
		$.each(data,function(i,n){
			
			var li = $("<li><a id='_menu"+i+"' href=\"javascript:void(0)\"><img></a></li>");
			
			li.find("img").attr("src", n.icon);
			li.find("a").append(n.title);
			if (n.callbak) {
				li.find("a").bind("click", n.callbak)
			} else {
				li.find("a").bind(
						"click",
						function() {
							comm.toPage(n.target, n.title,
									n.href, n.param);
						});
			}
			ul.append(li);
			
			if (showPage == "Y" && i == 0) {
				li.find("a").click();
			}
		});
		
		
	},
	createMumeAuth : function(id, data, pIndex) {
		var ul = $("#" + id);
		ul.empty();
		var cou = 0;
		$.each(data,function(i,n){
			var authRole = n.authorize.split(",");			
			for(var r in authRole){
				if(userRoleInfo.roleName == authRole[r]){
					var li = $("<li><a id='_menu"+i+"' href=\"javascript:void(0)\"><img></a></li>");
					
					li.find("img").attr("src", n.icon);
					li.find("a").append(n.title);
					if (n.callbak) {
						li.find("a").bind("click", n.callbak)
					} else {
						li.find("a").bind(
								"click",
								function() {
									comm.toPage(n.target, n.title,
											n.href, n.param);
								});
					}
					ul.append(li);
					
					if (sysMId == 0) {
						li.find("a").click();
					}
					sysMId++;
					cou++;
				}
			}
		});
		if(cou == 0){
			$("#accordMemu").accordion("remove",pIndex-removeCou);
			removeCou++;
		}
		
	}
}

var gridUtil = {
	createNew : function(grid,options) {
		var g = {};
		g.editIndex = null;

		g.endEditing = function(callback) {

			if (g.editIndex == null) {
				if(callback){
					callback.call();
				}
				return
			}
			
			if ($(grid).datagrid('validateRow', g.editIndex)) {
				
				$.messager.progress({"title":"数据保存中！"});
				
				$(grid).datagrid('endEdit', g.editIndex);
				
				var rows=$(grid).datagrid("getRows");
				
				
				$.post(options["url"]+"/save",rows[g.editIndex],function(rd){
					$.messager.progress("close");
					if(rd.state==1){
						g.editIndex = null;
						rows[g.editIndex]=rd.data;
						if(callback){
							callback.call();
						}
					}else{
						var errors="";
						$.each(rd.errors,function(i,n){
							errors+=(i+1)+"、"+n.defaultMessage+"<br>";
						});
						
						$.messager.alert("保存错误",errors,"error");
						$(grid).datagrid('beginEdit', g.editIndex);
					}
				});
				
			}
		};
		g.append = function() {
			g.endEditing(function(){
				$(grid).datagrid('appendRow', {});
				g.editIndex = $(grid).datagrid('getRows').length - 1;
				$(grid).datagrid('selectRow', g.editIndex).datagrid(
						'beginEdit', g.editIndex);
			});
			
			
		};
		g.remove = function(call) {
			var row = $(grid).datagrid("getSelected");
			if (!row) {
				$.messager.alert("提示", "请选择要删除的数据！")
				return;
			}
			if (g.editIndex!= null) {
				$.messager.confirm("请确认","您目前正在编辑数据，是否先取消编辑",function(r){
					if(r){
						g.reject();
					}
				});
				return;
			}
			$.messager.confirm("请确认","您确认删除该数据？",function(r){
				if (r) {
					var rowIndex = $(grid).datagrid("getRowIndex", row);
					
					$.messager.progress({"title":"处理删除中。。。"});
					
					$.post(options["url"]+"/delete",row,function(rd){
						$.messager.progress("close");
						$.messager.alert("提示","删除成功");
						$(grid).datagrid('deleteRow', rowIndex);
						g.editIndex = null;
					}).complete(function(){
						if(call){
							call.call();
						}
					})
				}
			});

		};
		g.accept = function() {
			g.endEditing(function(){
				$(grid).datagrid('acceptChanges');
				$(grid).datagrid("reload");
			})
		};
		g.reject = function() {
			if(g.editIndex!=null){
				$(grid).datagrid('cancelEdit', g.editIndex);
				
				if($(grid).datagrid("getRows")[g.editIndex][options["idField"]]==null){
					$(grid).datagrid(
							'deleteRow', g.editIndex);
				}
				g.editIndex = null;
			}
		};
		g.editData = function() {
			
			var row = $(grid).datagrid("getSelected");
			if (!row) {
				$.messager.alert("提示", "请选择要编辑的数据！")
				return;
			}
			var index = $(grid).datagrid("getRowIndex", row);
			
			if(g.editIndex != index) {
				g.endEditing(function(){
					$(grid).datagrid('beginEdit',index);
					g.editIndex = index;
				});
			}
		}
		return g;
	}

}

var system = {
	menus1 : [{
		"icon" : "/veh/images/system.png",
		"title" : "系统参数",
		href : "/veh/html/systemInfo.html",
		target : "#systemContex",
		authorize : "超级管理员"
	},{
		"icon" : "/veh/images/dictionary.png",
		"title" : "数据字典",
		href : "/veh/html/dataDictionary.html",
		target : "#systemContex",
		authorize : "超级管理员,系统管理员"
	},{		
		"icon" : "/veh/images/limit.png",
		"title" : "检测项目和标准限值",
		href : "/veh/html/limitStandard.html",
		target : "#systemContex",
		authorize : "系统管理员"
	},{
		"icon" : "/veh/images/security.png",
		"title" : "安全审计策略设置",
		href : "/veh/html/securityAuditPolicySetting.html",
		target : "#systemContex",
		authorize : "安全管理员"
	}],
	menus2:[
		{
			"icon" : "/veh/images/device.png",
			"title" : "设备管理",
			href : "/veh/html/DeviceManager.html",
			target : "#systemContex",
			authorize : "系统管理员,超级管理员"
		},{
			"icon" : "/veh/images/Workflow.png",
			"title" : "检测流程",
			href : "/veh/html/flowConfig.html",
			target : "#systemContex",
			authorize : "系统管理员,超级管理员"
		},{
			"icon" : "/veh/images/video_editing.png",
			"title" : "视频配置",
			href : "/veh/html/config.html",
			target : "#systemContex",
			authorize : "系统管理员,超级管理员"
		}
	],
	menus3:[
		{
			"icon" : "/veh/images/user.png",
			"title" : "用户管理",
			href : "/veh/html/UserManager.html",
			target : "#systemContex",
			authorize : "超级管理员,系统管理员"
		},{
			"icon" : "/veh/images/group.png",
			"title" : "角色管理",
			href : "/veh/html/roleManager.html",
			target : "#systemContex",
			authorize : "超级管理员"
		},{
			"icon" : "/veh/images/blackList.png",
			"title" : "黑名单管理",
			href : "/veh/html/BlackListManager.html",
			target : "#systemContex",
			authorize : "超级管理员"
		},{
			"icon" : "/veh/images/function.png",
			"title" : "核心功能管理",
			href : "/veh/html/coreFunction.html",
			target : "#systemContex",
			authorize : "超级管理员"			
		},{
			"icon" : "/veh/images/warning.png",
			"title" : "非常规功能管理",
			href : "/veh/html/SpecialCoreFunction.html",
			target : "#systemContex",
			authorize : "超级管理员"			
		},{
			"icon" : "/veh/images/police.png",
			"title" : "警员功能管理",
			href : "/veh/html/policeCoreFunction.html",
			target : "#systemContex",
			authorize : "超级管理员"			
		}
	],
	menus4:[
		{
			"icon" : "/veh/images/backup.png",
			"title" : "检验机构备案信息",
			href : "/veh/html/recordInfoDownLoad.html",
			target : "#systemContex",
			authorize : "系统管理员"
		},
		{
			"icon" : "/veh/images/backup_user.png",
			"title" : "检测线备案信息",
			href : "/veh/html/recordInfoOfJcxDownLoad.html",
			target : "#systemContex",
			authorize : "系统管理员"
		}
//		{
//			"icon" : "/veh/images/backup_user.png",
//			"title" : "检验机构人员备案信息",
//			href : "/veh/html/recordInfoOfCheckStaffDownLoad.html",
//			target : "#systemContex",
//			authorize : "系统管理员"
//		}
	],
	menus5:[
		{
			"icon" : "/veh/images/LOG.png",
			"title" : "操作日志",
			href : "/veh/html/operationLog.html",
			target : "#systemContex",
			authorize : "审计管理员,超级管理员"
		},{
			"icon" : "/veh/images/loginlog.png",
			"title" : "登录日志",
			href : "/veh/html/loginOperationLog.html",
			target : "#systemContex",
			authorize : "审计管理员,超级管理员"
		},{		
			"icon" : "/veh/images/logsec.png",
			"title" : "安全日志",
			href : "/veh/html/securityLog.html",
			target : "#systemContex",
			authorize : "安全管理员"
		}
	],
	initEvents : function() {
//		if(userRoleInfo.roleName == "审计管理员"){
//			comm.createMume("backMune", system.menus4, "Y");
//		}else{
		sysMId = 0;
		removeCou = 0
			comm.createMumeAuth("sysMune", system.menus1, 0);
			comm.createMumeAuth("deviceMune", system.menus2, 1);
			comm.createMumeAuth("userMune", system.menus3, 2);
			comm.createMumeAuth("backMune", system.menus4, 3);
			comm.createMumeAuth("logMune", system.menus5, 4);
//		}
	}
}

var statisticalReport = {
		menus1 : [{
			"icon" : "/veh/images/statistics.png",
			"title" : "车辆类型合格率汇总",
			href : "/veh/html/statistics/vehicleTypeStatistics.html",
			target : "#reportContex"
		},{
			"icon" : "/veh/images/report2.png",
			"title" : "检验类别合格率汇总",
			href : "/veh/html/statistics/inspectionCategoryStatistics.html",
			target : "#reportContex"
		},{
			"icon" : "/veh/images/report3.png",
			"title" : "区县分类合格率汇总",
			href : "/veh/html/statistics/countryStatistics.html",
			target : "#reportContex"
		},{
			"icon" : "/veh/images/report4.png",
			"title" : "检验项目合格率汇总",
			href : "/veh/html/statistics/inspectionProjectStatistics.html",
			target : "#reportContex"
		},{
			"icon" : "/veh/images/statsyear.png",
			"title" : "客车和危货车辆检验月报表",
			href : "/veh/html/statistics/yearReport.html",
			target : "#reportContex"
		},{
			"icon" : "/veh/images/stats.png",
			"title" : "检测线车辆数分布统计",
			href : "/veh/html/statistics/vehicleDistributionStatistics.html",
			target : "#reportContex"
		},{
			"icon" : "/veh/images/workuser.png",
			"title" : "人员工作量统计表",
			href : "/veh/html/statistics/workEstimate.html",
			target : "#reportContex"
		},{
			"icon" : "/veh/images/workuser.png",
			"title" : "人员工作量统计表2",
			href : "/veh/html/statistics/workEstimate2.html",
			target : "#reportContex"
		}],
		initEvents : function() {
				comm.createMume("businessStatistics", statisticalReport.menus1, "Y");
		}
};

$.extend(
	$.fn.validatebox.defaults.rules,
	{
		userVad : {
			validator : function(value, param) {
				var reg = /^[a-zA-Z\d]\w{3,11}[a-zA-Z\d]$/;
				return reg.test(value);
			},
			message : '用户名包含4到12位数字、字母'
		},
		idCardVad : {
			validator : function(value, param) {
				// 15位和18位身份证号码的正则表达式
				var regIdCard = /^(^[1-9]\d{7}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{3}$)|(^[1-9]\d{5}[1-9]\d{3}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])((\d{4})|\d{3}[Xx])$)$/;
				// 如果通过该验证，说明身份证格式正确，但准确性还需计算
				if (regIdCard.test(value)) {
					if (value.length == 18) {
						var idCardWi = new Array(7, 9, 10, 5, 8, 4,
								2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2); // 将前17位加权因子保存在数组里
						var idCardY = new Array(1, 0, 10, 9, 8, 7,
								6, 5, 4, 3, 2); // 这是除以11后，可能产生的11位余数、验证码，也保存成数组
						var idCardWiSum = 0; // 用来保存前17位各自乖以加权因子后的总和
						for (var i = 0; i < 17; i++) {
							idCardWiSum += value
									.substring(i, i + 1)
									* idCardWi[i];
						}
						var idCardMod = idCardWiSum % 11;// 计算出校验码所在数组的位置
						var idCardLast = value.substring(17);// 得到最后一位身份证号码
						// 如果等于2，则说明校验码是10，身份证号码最后一位应该是X
						if (idCardMod == 2) {
							if (idCardLast == "X"
									|| idCardLast == "x") {
								return true;
							} else {
								return false;
							}
						} else {
							// 用计算出的验证码与最后一位身份证号码匹配，如果一致，说明通过，否则是无效的身份证号码
							if (idCardLast == idCardY[idCardMod]) {
								return true;
							} else {
								return false;
							}
						}
					}
				} else {
					return false;
				}
			},
			message : '身份证号码错误'
		},
		ipsVad:{
			validator:function(value, param){
				
				var ips=value.split(",");
				var reg =  /^([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])$/;
				for(var i in ips){
					var ip = ips[i];
					if(!reg.test(ip)){
						return false;
					}
				}
				return true;
				
			},
			message:"您输入的IP地址不正确！"
		}
	});

var report={
	getBsStr:function(jszt){
		if(jszt==0){
			return ",";
		}else if(jszt==1){
			return "*,";
		}else if(jszt==2){
			return ",*"
		}else if(jszt==3){
			return "*,*"
		}else{
			return ",";
		}
			
	},
	createReport:function (area){
		var strStyleCSS="<link href='/veh/css/veh.css' type='text/css' rel='stylesheet'>";
		var LODOP=getLodop();  
		LODOP.PRINT_INIT("打印控件功能演示_Lodop功能_表单一");
		LODOP.SET_PRINT_STYLE("FontSize",14);
		LODOP.SET_PRINT_STYLE("Bold",1);
		LODOP.ADD_PRINT_HTM("8mm",34,"RightMargin:0.9cm","BottomMargin:9mm",strStyleCSS+"<body>"+area.html()+"</body>");
		// LODOP.PRINT();
		LODOP.PREVIEW(); 
		
	},
	createReportNoView:function (area){
		var strStyleCSS="<link href='/veh/css/veh.css' type='text/css' rel='stylesheet'>";
		var LODOP=getLodop();  
		LODOP.PRINT_INIT("打印控件功能演示_Lodop功能_表单一");
		LODOP.SET_PRINT_STYLE("FontSize",14);
		LODOP.SET_PRINT_STYLE("Bold",1);
		LODOP.ADD_PRINT_HTM("8mm",34,"RightMargin:0.9cm","BottomMargin:9mm",strStyleCSS+"<body>"+area.html()+"</body>");
		LODOP.PRINT();
		
	},
	loadVehCheckInfo:function(index,row){
		if(row){
			try {
				if(ScanCtrl){
					ScanCtrl.StopPreviewEx();
				}
			} catch (e) {}
			if(row.cllx.indexOf("N")>=0){
				$("#report1").panel({"href":"/veh/html/report/tricycleReport1.html","onLoad":report.getReport1,baseInfo:row});
			}else{
				var reportArray= comm.getBaseParames("report_url");
				var report1_url="report1_jx.html";
				
				$.each(reportArray,function(i,n){
					if(n.value=="report1"){
						report1_url=n.id;
					}
				});
				$("#report1").panel({"href":"/veh/html/report/"+report1_url,"onLoad":report.getReport1,baseInfo:row});
			}
			//
			$("#report2").panel({"href":"/veh/html/report/report2.html","onLoad":report.getReport2,baseInfo:row});
			$("#report3").panel({"href":"/veh/html/report/report3.html","onLoad":report.getReport3,baseInfo:row});
			$("#report4").panel({"href":"/veh/html/report/report4.html","onLoad":report.getReport4,baseInfo:row});
			$("#upimage").panel({"href":"/veh/html/report/upimage.html",baseInfo:row});
			$("#process").panel({"href":"/veh/html/report/process.html",baseInfo:row});
			
			$("#sqprocess").panel({"href":"/veh/html/report/sqprocess.html",baseInfo:row});
			
			$("#online").panel({"href":"/veh/html/report/online.html",baseInfo:row});
			$("#commit").panel({"href":"/veh/html/report/commit.html",baseInfo:row});
			$("#roadCheck").panel({"href":"/veh/html/report/roadCheck.html",baseInfo:row});
			$("#zbzlTab").panel({"href":"/veh/html/report/curbWeight.html",baseInfo:row});
			$("#tab-report").tabs("getSelected").panel("refresh");
			$("#performanceCk").panel({"href":"/veh/html/report/performanceCk.html",baseInfo:row});
			$("#performanceCkRep").panel({"href":"/veh/html/report/performanceCkRep.html",baseInfo:row});
			$("#plateApply").panel({"href":"/veh/html/report/plateApply.html","onLoad":report.getPlateApply,baseInfo:row});
			$("#relogin").panel({"href":"/veh/html/report/relogin.html","onLoad":report.getPlateApply,baseInfo:row});
			
			$("#outLine").panel({"href":"/veh/html/report/outline.html",baseInfo:row});
			$("#zjline").panel({"href":"/veh/html/report/zjline.html",baseInfo:row});
		}
	},
	getReport1:function(panelObj,intjycs){
		
		var baseInfo = $("#tab-report").tabs("getSelected").panel("options").baseInfo;
		
		var jycsarray=[];
		
		var zcdxcs=1;
		
		if(!intjycs){
			intjycs = baseInfo.jycs;
		}
		//$("[name=xmpd]").text('');
		$("[name=zczdpd]").text('');
		$("[name=zjycs]").text(intjycs);
		
		for(var i=1;i<=baseInfo.jycs;i++){
			jycsarray.push({label:i,value:i})
		}
			
		$("#query_jycs").combobox({
			editable:false,
			prompt:'检验次数',
			valueField: 'label',
			textField: 'value',
			data:jycsarray,
			value:intjycs,
			onChange:function(newValue,oldValue){
				$("#report1 td[name=xmpd]").text('—');
				report.getReport1(panelObj,newValue);
			}
		});
		
		 $.messager.progress({
				title:"提示",
				msg:"正在努力加载报表中"
		 });
		
		$.post("/veh/report/getReport1",{jylsh:baseInfo.jylsh,jycs:intjycs},function(data){
			$("#report1_jyjgmc").text(data.title);
			$("#report1 [name=gcsj]").text(data.gcsj);
			
			$("#report1 [name^='report-baseInfo-']").each(function(i,n){
				var name = $(n).attr("name").replace("report-baseInfo-","");
				
				$(n).text(baseInfo[name]==null?"":comm.getParamNameByValue(name, baseInfo[name]));
			});
			
			var jyxmTemp = $("#report1 [name=report-baseInfo-jyxm]").text();
			var newJyxm="";
			
			$.each(jyxmTemp.split(","),function(i,n){
				if(n=="EP"||n=="OC"||n=="PF"||n=="VL"||n=="XJ"){
					
				}else{
					newJyxm =  newJyxm+"," + n;
				}
			});
			 $("#report1 [name=report-baseInfo-jyxm]").text(newJyxm.substring(1));
			
			var zczw = $("#report [name=report-baseInfo-zczw]").text();
			if(zczw){
				
				var newStr="";
				 var zczwarray = zczw.split("");
				 $.each(zczwarray,function(i,n){
					
					switch (n) {
					case "1":
						newStr+="+一轴"
						break;
					case "2":
						newStr+="+二轴"
						break;
					case "3":
						newStr+="+三轴"
						break;
					case "4":
						newStr+="+四轴"
						break;
					case "5":
						newStr+="+五轴"
						break;
					case "6":
						newStr+="+六轴"
						break;
					default:
						break;
					}
				 });
				 if(newStr){
					 newStr = newStr.substring(1);
				 }
				 $("#report [name=report-baseInfo-zczw]").text(newStr);
			}
			
			
			
			$("#report1_jczmc").text(vehComm.jyjgmc);
			
			var jyxm = $("#report1 [name^='report-baseInfo-jyxm']").text();
			var newJyxm="";
			
			if(jyxm.indexOf("B")>=0){
				newJyxm+="B";
			}
			
			if(jyxm.indexOf("H")>=0){
				newJyxm+="H";
			}
			
			
			if(jyxm.indexOf("A")>=0){
				newJyxm+="A";
			}
			
			if(jyxm.indexOf("S")>=0){
				newJyxm+="S";
			}
			
			if(jyxm.indexOf("R")>=0){
				newJyxm+="R";
			}
			
			if(jyxm.indexOf("Z")>=0){
				newJyxm+="Z";
			}
			
		//	$("#report1 [name^='report-baseInfo-jyxm']").text(newJyxm);
			
			$.each(data,function(i,n){
				// 处理灯光
				if(i.indexOf("H")==0){
					var tt = i.split("_");
					$.each(n,function(j,k){
						if(j=="czpc"){
							k=parseFloat(k);
							if(k>0){
								k="+"+k;
							}
						}
						if(j=="czpy"){
							k==null?"":(k+"H"); 
						}
						if(j=="czpy"&&n["czpypd"]==2){
							k+="x";
						}
						var cllx = baseInfo.cllx;
						var syxz= baseInfo.syxz;
//						if(j=="czpy"&&((cllx.indexOf("K3") == 0 || cllx.indexOf("K4") == 0 || cllx.indexOf("N") == 0) && syxz=="A")){
//							
//						}else{
//							$("#report1 tr[name="+tt[0]+"] td[name="+tt[1].toLowerCase()+"_"+j+"]").text(k==null?"":k);
//						}
						
						$("#report1 tr[name="+tt[0]+"] td[name="+tt[1].toLowerCase()+"_"+j+"]").text(k==null?"":k);
						
						
					});
					
					if(n.gx=="Y"){
						var  dgpd_text = $("#report1 tr[name="+tt[0]+"] td[name=xmpd]").text();
						$("#report1 tr[name="+tt[0]+"] td[name=xmpd]").text(veh.jgpd(n.gqpd)+dgpd_text.substring(1));
					}
					
					if(n.gx=="J"){
						var  dgpd_text = $("#report1 tr[name="+tt[0]+"] td[name=xmpd]").text();
						$("#report1 tr[name="+tt[0]+"] td[name=xmpd]").text(dgpd_text.substring(0,1)+veh.jgpd(n.czpypd==null?"0":n.czpypd));
					}
					
//					if($("#report1 tr[name="+tt[0]+"] td[name=xmpd]").text()!="X"){
//						$("#report1 tr[name="+tt[0]+"] td[name=xmpd]").text(veh.jgpd(n.zpd));
//					}
					$("#report1 tr[name="+tt[0]+"] td[name=dxcs]").text(n.dxcs);
					
				}else if(i.indexOf("S1")==0){
					$("#report1 tr[name=S1] div[name=speed]").text(n.speed);
					$("#report1 tr[name=S1] td[name=xmpd]").text(veh.jgpd(n.zpd));
					$("#report1 tr[name=S1] td[name=dxcs]").text(n.dxcs);
				}else if(i.indexOf("A1")==0){
					$("#report1 tr[name=A1] div[name=sideslip]").text(n.sideslip);
					$("#report1 tr[name=A1] td[name=xmpd]").text(veh.jgpd(n.zpd));
					$("#report1 tr[name=A1] td[name=dxcs]").text(n.dxcs);
				}else if(i.indexOf("ZZ")==0){
					var tt = i.split("_");
					$("#report1 tr[name="+tt[1]+"] td[name=zz_leftData]").text(n.leftData);
					$("#report1 tr[name="+tt[1]+"] td[name=zz_rightData]").text(n.rightData);
				}else if(i.indexOf("ZD")==0){
					var tt = i.split("_");
					
//					var letStart =  n.leftDataStr.split(",").length>=400?"":"*";
//					var rigStart =  n.rigthDataStr.split(",").length>=400?"":"*";
					
					var bsStr=report.getBsStr(n.jszt);
					var starts=bsStr.split(",");
					$("#report1 tr[name="+tt[1]+"] td[name=zd_zzdl]").text(n.zzdl);
					$("#report1 tr[name="+tt[1]+"] td[name=zd_yzdl]").text(n.yzdl);
					$("#report1 tr[name="+tt[1]+"] td[name=zd_zzdlcd]").text(n.zzdlcd);
					$("#report1 tr[name="+tt[1]+"] td[name=zd_yzdlcd]").text(n.yzdlcd);
					$("#report1 tr[name="+tt[1]+"] td[name=zd_kzbphl]").text(n.kzbphl==null?n.kzbphl:n.kzbphl.toFixed(1));
					$("#report1 tr[name="+tt[1]+"] td[name=zd_kzxczdl]").text(n.kzxczdl);
					
					if(n.kzxczdl>=100){
						$("#report1 tr[name="+tt[1]+"] td[name=zd_kzxczdl]").css("color","red");
					}
					
					//$("#report1 tr[name="+tt[1]+"] td[name=xmpd]").text(veh.jgpd(n.kzzdlpd)+veh.jgpd(n.kzbphlpd));
					$("#report1 tr[name="+tt[1]+"] td[name=xmpd]").text(veh.jgpd(n.zpd));
					
					$("#report1 tr[name="+tt[1]+"] td[name=zd_zlh]").text(n.zlh);
					$("#report1 tr[name="+tt[1]+"] td[name=zd_ylh]").text(n.ylh);
					
					
					$("#report1 tr[name="+tt[1]+"] td[name=dxcs]").text(n.dxcs);
					
					if(n.dxcs>zcdxcs){
						zcdxcs=n.dxcs;
					}
					
					var jzzh=n.jzzlh+n.jzylh;
					
					if(tt[1].indexOf("L")==0){
						$("#report1 tr[name=B"+n.zw+"] td[name=zd_jzzh]").text(n.zlh+n.ylh);
						$("#report1 tr[name=B"+n.zw+"] td[name=zd_jzzzdl]").text(n.kzxczdl);
						$("#report1 tr[name=B"+n.zw+"] td[name=zd_jzbphl]").text(n.kzbphl);
						var objpd=$("#report1 tr[name=B"+n.zw+"] td[name=xmpd]");
						if(objpd.text()!="X"){
							objpd.text(veh.jgpd(n.zpd));
						}
						
						$("#report1 td[name=L"+n.zw+"_zzdl]").text(n.zzdl);
						$("#report1 td[name=L"+n.zw+"_yzdl]").text(n.zzdl);
						$("#report1 td[name=L"+n.zw+"_zzdlcd]").text(n.zzdlcd);
						$("#report1 td[name=L"+n.zw+"_yzdlcd]").text(n.yzdlcd);
					}
					
					if(n.jzzlh){
						$("#report1 td[name=L"+n.zw+"] span[name=left]").text(n.jzzlh);
					}
					if(n.jzylh){
						$("#report1 td[name=L"+n.zw+"] span[name=rigth]").text(n.jzylh);
					}
					
					
					
					if(tt[1]=="B0"){
						$("#report1 tr[name=B"+n.zw+"] td[name=zd_b"+n.zw+"_zczdl]").text((Number(n.zzdl)+Number(n.yzdl)))
					}
					
					if(n.zdtlh!=null&&n.ydtlh!=null){
						$("#report1 span[name=zd_dtzlh_"+tt[1]+"]").text(n.zdtlh);
						$("#report1 span[name=zd_dtylh_"+tt[1]+"]").text(n.ydtlh);
					}else{
						$("#report1 span[name=zd_dtzlh_"+tt[1]+"]").text(0);
						$("#report1 span[name=zd_dtylh_"+tt[1]+"]").text(0);
					} 
				}else if(i.indexOf("other")==0){
					$("#report1 tr[name=ZC] td[name=other_jczczbzl]").text(n.jczczbzl);
					$("#report1 tr[name=ZC] td[name=other_zdlh]").text(n.zdlh);
					if(n.zczdl){
						$("#report1 tr[name=ZC] td[name=other_zczdl]").text(n.zczdl.toFixed(1));
					}else{
						$("#report1 tr[name=ZC] td[name=other_zczdl]").text(n.zczdl);
					}
					$("#report1 tr[name=ZC] td[name=zczdpd]").text(veh.jgpd(n.zczdpd));
					$("#report1 tr[name=ZC] td[name=dxcs]").text(zcdxcs);
					
				}else if(i.indexOf("par")==0){
					$("#report1 tr[name=par] td[name=par_tczclh]").text(n.tczclh);
					$("#report1 tr[name=par] td[name=par_tczdl]").text(n.tczdl);
					$("#report1 tr[name=par] td[name=par_zczczdl]").text(n.zczczdl);
					$("#report1 tr[name=par] td[name=xmpd]").text(veh.jgpd(n.tczdpd));
					$("#report1 tr[name=par] td[name=dxcs]").text(n.dxcs);
				}else if(i.indexOf("roadChecks")==0){
					
					var lsjg=1;
					var temStr="";
					$.each(n,function(j,k){
						var jg="—";
						if(k.yqjgpd==1){
							jg="合格";
						}else if(k.yqjgpd==2){
							jg="不合格";
							lsjg=2;
						}else if(k.yqjgpd==0){
							jg="—";
						}
						temStr+=k.yqjyxm+":"+k.yqjyjg+"("+k.yqbzxz+")"+jg+"   |   <br>"
					});
					$("#report1 td[name=roadCheck_info]").html(temStr);
					$("#report1 td[name=roadCheck_pd]").text(veh.jgpd(lsjg));
					$("#report1 td[name=roadCheck_dxcs]").text(1);
					
					var ycy = $("[name=report-baseInfo-ycy]").text();
					if(ycy==""){
						$("[name=report-baseInfo-ycy]").text(data['roadCheckInfo'].lsy);
					}
					var upLineDate = $("[report-baseInfo-upLineDate]").text();
					if(upLineDate==""){
						$("[name=report-baseInfo-upLineDate]").text(baseInfo['dlsj']);
					}
					
					
				}else if(i.indexOf("lsy")==0){
					$("#report1 td[name=roadCheck_lsy]").text(n);
				}else if(i.indexOf("wkcc")==0){
					$("#report1 [name=wkcc]").text(n.cwkc+"x"+n.cwkk+"x"+n.cwkg);
					$("#report1 [name=wkcc_pd]").text(veh.jgpd(n.clwkccpd));
					$("#report1 [name=wkcc_jycs]").text(n.jycs);
				}else if(i.indexOf("Z1")==0){
					$("#report1 [name=other_zczbzl]").text(n.zbzl);
					$("#report1 [name=other_zczbzl_jycs]").text(n.jycs);
					$("#report1 [name=other_zczbzl_jgpd]").text(veh.jgpd(n.zbzlpd));
				}
			});
		}).error(function(e){
			$.messager.alert("提示","请求失败","error");
		}).complete(function(){
			 $.messager.progress("close");
		});
	},getReport2:function(){
		var baseInfo = $(this).panel("options").baseInfo;
		$("#report2 [name^='report-baseInfo-']").each(function(i,n){
			var name = $(n).attr("name").replace("report-baseInfo-","");
			$(n).text(baseInfo[name]==null?"":comm.getParamNameByValue(name, baseInfo[name]));
		});
		$("#report2 [name=report-baseInfo-jyjgmc]").text(vehComm.jyjgmc);
		$("#report2 [name=report-baseInfo-sqrqz]").text(vehComm.sqrqz);
		
		$.post("/veh/report/getReport2",{jylsh:baseInfo.jylsh},function(data){
			
			var yqsbjyjg = data['yqsbjyjg'];
			var rgjyjg =data['rgjyjg'];
			
			var jyjl="合格";
			
			$.each(yqsbjyjg,function(i,n){
				
				
				
				if(n.yqjgpd==2){
					jyjl="不合格";
				}
				
				var jg="-";
				
				var tempPD=n.yqjgpd;
				
				if(n.yqjgpd.indexOf("/")>0){
					var jg1 = "-";
					var jg2 ="-";
					var tArray =n.yqjgpd.split("/");
					if(tArray[0]==1){
						jg1="合格";
					}else if(tArray[0]==2){
						jg1="不合格";
					}else if(tArray[0]==0){
						jg1="-";
					}else if(tArray[0]==3){
						jg1="-";
					}
					if(tArray[1]==1){
						jg2="合格";
					}else if(tArray[1]==2){
						jg2="不合格";
					}else if(tArray[1]==0){
						jg2="-";
					}else if(tArray[1]==3){
						jg2="-";
					}
					
					jg=jg1+"/"+jg2;
					
				}else{
					if(tempPD==1){
						jg="合格";
					}else if(tempPD==2){
						jg="不合格";
					}else if(tempPD==0){
						jg="-";
					}else if(tempPD==3){
						jg="-";
					}
				}
				
				
				
				var tr="<tr><td class=l >"+(i+1)+"</td><td>"+n.yqjyxm+"</td><td class=l>"+		
						n.yqjyjg+"</td><td class=l>"+n.yqbzxz+"</td><td class=l>"+jg+"</td><td class=l>"+
						(n.yqjybz==null?"":n.yqjybz)+"</td></tr>";
				$("#tbody_yqsbjyjg").append(tr);
			});
			$.each(rgjyjg,function(i,n){
				if(n.rgjgpd==2){
					jyjl="不合格";
				}
				
				var jg="-";
				if(n.rgjgpd==1){
					jg="合格";
				}else if(n.rgjgpd==2){
					jg="不合格";
				}else if(n.rgjgpd==0){
					jg="-";
				}
				
				var jysm = "";
				if(n.rgjysm!=null){
					var jysmArr = n.rgjysm.split(",");
					for(var b=0;b<jysmArr.length;b++){
						if(b == jysmArr.length-1){
							jysm = jysm + comm.getParamNameByValue("rgjysm", jysmArr[b]);
						}else{
							jysm = jysm + comm.getParamNameByValue("rgjysm", jysmArr[b])+";";
						}
					}
				}
				var tr="<tr><td class=l>"+n.xh+"</td><td>"+n.rgjyxm+"</td><td class=l>"+jg+"</td><td class=l colSpan=2>"+
				(jysm)+"</td><td class=l>"+(n.rgjybz==null?"":n.rgjybz)+"</td></tr>";
				$("#tbody_rgjyjg").append(tr);
			});
			
			$("#report2 td[name=report-baseInfo-jyjl]").text(jyjl);
		});
		
	},getReport3:function(){
		
		var baseInfo = $(this).panel("options").baseInfo;
		$("#report3 [name^='report-baseInfo-']").each(function(i,n){
			var name = $(n).attr("name").replace("report-baseInfo-","");
			$(n).text(baseInfo[name]==null?"":comm.getParamNameByValue(name, baseInfo[name]));
		});
		
		var r3 = comm.getBaseParames("report3");
		
		if(r3.length>0&&r3[0].id==1){
			$.post("/veh/report/getReport3",{jylsh:baseInfo.jylsh},function(data){
				$.each(data,function(i,n){
					var t=n;
					if(n==1){
						t="O"
					}else if(n==2){
						t="X";
					}else if(n==null||"null"==n||n==0){
						t="—";
					}
					$("#"+i).text(t);
				});
				
			});
		}
		
	},
	getReport4:function(){
		var baseInfo = $(this).panel("options").baseInfo;
		$.post("/veh/report/getReport4",{jylsh:baseInfo.jylsh},function(datas){
			
			datas = $.parseJSON(datas);
			
			if(datas['zd'].length==0 && datas['ch'].length==0){
				 $("#zdltabs").tabs("add",{
					 title:"制动力曲线",
					 content:"<div class='nullData'>无制动力过程数据</div>"
				 });
			}
			var index=0;
			$.each(datas.zd,function(i,data){
				 
				 if(data.leftDataStr!=null&&data.leftDataStr!=""){
					 report.processReport4(baseInfo, data, "zdlqx", data.leftDataStr, data.rigthDataStr, i, "轴制动力曲线","N");
				 }
				 if(data.zdtlhStr!=null&&data.zdtlhStr!=""){
					 report.processReport4(baseInfo, data, "dtlh", data.zdtlhStr, data.ydtlhStr, i, "动态轮荷曲线","KG");
				 }
				 index=i;
			 });
			index++;
			$.each(datas.ch,function(i,data){
				 if(data.strData!=null&&data.strData!=""){
					 report.processReport4CH(baseInfo, data,"chqx"+i,i+index,"侧滑数据"+data.jycs,"m/km");
				 }
				 
			 });
			
		});
	},
	processReport4CH:function(baseInfo,data,key,index,title,dw){
		var template="<div style='text-align:center;margin-top: 10px;'><a id='report4Print"+key+"'></a>&nbsp;&nbsp;<a id='showReport4Detail"+key+"'></a><div/>"+
		 "<div style='margin:0 auto;width:740px;' id='report4Contex"+key+"'><div id='container"+key+"'></div></div>";
		
		var temp = data.strData.split(",");
		var ldata=[];
		
		$.each(temp,function(i,n){
			ldata.push(Number(n));
		});
		
		 $("#zdltabs").tabs("add",{
			 title:title,
			 index:index,
			 content:template,
			 selected:index==0?true:false,
			 baseInfo:baseInfo,
			 rdata:[],
			 ldata:ldata,
			 data:data,
			 ckey:key,
			 dw:dw
		 });
		 
		 $("#report4Print"+key).linkbutton({
			 iconCls: 'icon-print',
			 text:'打印',
			 onClick:function(){
				 report.createReport($("#report4Contex"+key));
			 }
		 });
		 
		 $("#showReport4Detail"+key).linkbutton({
			 iconCls: 'icon-search',
			 text:'显示详细',
			 onClick:function(){
				 $("#report4Contex"+key+" .reportTable4").remove();
				 var temp= report.createReport4CHTeblae(key,data.zw, ldata,title);
				 $("#report4Contex"+key).append(temp);
			 }
		 });
		 
		
	}
	,
	processReport4:function(baseInfo,data,key,strLeftdata,strRigthData,index,title,dw){
		var rdata=[];
		var ldata=[];
		 
	    if(strRigthData!=null){
			var temp=strRigthData.split(",");
			if(temp.length>700){
				temp=temp.splice(300,700);
			}
			$.each(temp,function(i,n){
				rdata.push(Number(n));
			});
		}
		
		if(strLeftdata!=null){
			var temp=strLeftdata.split(",");
			
			if(temp.length>700){
				temp=temp.splice(300,700)
			}
			$.each(temp,function(i,n){
				ldata.push(Number(n));
			});
		}
		
		var ckey=data.zw+key+"_"+data.jycs+"_"+data.jyxm;
		
		 var template="<div style='text-align:center;margin-top: 10px;'><a id='report4Print"+ckey+"'></a>&nbsp;&nbsp;<a id='showReport4Detail"+ckey+"'></a><div/>"+
		 "<div style='margin:0 auto;width:740px;' id='report4Contex"+ckey+"'><div id='container"+ckey+"'></div></div>";
		 
		 $("#zdltabs").tabs("add",{
			 title: (data.jyxm.indexOf("L")==0?"加载":"") + data.zw+title+"("+data.jycs +")",
			 index:index,
			 content:template,
			 selected:index==0?true:false,
			 baseInfo:baseInfo,
			 rdata:rdata,
			 ldata:ldata,
			 data:data,
			 ckey:ckey,
			 dw:dw
		 });
		 
		 $("#report4Print"+ckey).linkbutton({
			 iconCls: 'icon-print',
			 text:'打印',
			 onClick:function(){
				 report.createReport($("#report4Contex"+ckey));
			 }
		 });
		 
		 $("#showReport4Detail"+ckey).linkbutton({
			 iconCls: 'icon-search',
			 text:'显示详细',
			 onClick:function(){
				 var temp= report.createReport4Teblae(ckey,data.zw, ldata, rdata,title);
				 $("#report4Contex"+key+" .reportTable4").remove();
				 $("#report4Contex"+ckey).append(temp);
			 }
		 });
		 
	},
	createReport4Teblae:function (ckey,zw,ldata,rdata,title){
		
		var lStr=title.indexOf("动态轮荷")>=0?"左轮荷":"左制动力";
		var rStr=title.indexOf("动态轮荷")>=0?"右轮荷":"右制动力";
		
		var cStr=title.indexOf("动态轮荷")>=0?"轮荷差":"制动力差";
		
		var report4Table="<table id='report4Table"+ckey+"' class='reportTable4'><thead><tr><td colspan='8'><h4>"+zw+title+"过程数据</h4></td></tr><tr><td>序号</td><td>"+lStr+"</td><td>"+rStr+"</td><td>"+cStr+"</td><td>序号</td><td>"+lStr+"</td><td>"+rStr+"</td><td>"+cStr+"</td></tr></thead><tbody>";
		var size=ldata.length<=rdata.length?ldata.length:rdata.length;
		size=size%2==1?(size-1):size;
		for(var i=0;i<size;i++){
			report4Table+="<tr><td>"+(i+1)+"</td>"+
			"<td>"+ldata[i]+"</td>"+
			"<td>"+rdata[i]+"</td>"+
			"<td>"+Math.abs(rdata[i]-ldata[i])+"</td>";
			i++;
			report4Table+="<td>"+(i+1)+"</td>"+
			"<td>"+ldata[i]+"</td>"+
			"<td>"+rdata[i]+"</td>"+
			"<td>"+Math.abs(rdata[i]-ldata[i])+"</td></tr>";
		}
		report4Table+="</tbody></table>";
		return report4Table;
	},
	createReport4CHTeblae:function (ckey,zw,ldata,title){
		
		
		var report4Table="<table id='report4Table"+ckey+"' class='reportTable4'><thead><tr><td colspan='8'><h4>侧滑过程数据</h4></td></tr><tr><td>序号</td><td>侧滑值</td><td>序号</td><td>侧滑值</td></tr></thead><tbody>";
		var size=ldata.length;
		//size=size%2==1?(size-1):size;
		for(var i=0;i<size;i++){
			report4Table+="<tr><td>"+(i+1)+"</td>"+
			"<td>"+ldata[i]+"</td>";
			i++;
			if(ldata[i]!=null){
			    report4Table+="<td>"+(i+1)+"</td>"+
			    "<td>"+ldata[i]+"</td></tr>";
			}else{
			    report4Table+="<td>"+(i+1)+"</td>"+
			    "<td></td></tr>";
			}
		}
		report4Table+="</tbody></table>";
		return report4Table;
	},
	
	loadCheckItemInfo:function(jylsh,jyxm){
		var center  = $("#layout-jyxm").layout("panel","center");
		var url;
		var title;
		switch (jyxm) {
		case 'R1':
			url="/veh/html/roadTest.html";
			title="路试信息";
			break;
		}
		if(url){
			center.panel("refresh",url);
			if(title){
				center.panel("setTitle",title);
			}
		}
	},
	getPlateApply:function(){
		var baseInfo = $(this).panel("options").baseInfo;
		
		$("#print_plateApply [name^='report-plateInfo-']").each(function(i,n){
			var name = $(n).attr("name").replace("report-plateInfo-","");
			$(n).text(baseInfo[name]==null?"":comm.getParamNameByValue(name, baseInfo[name]));
			if(name = "jylb" && baseInfo[name] == "01"){
				$("#hgbzhl").attr("checked",true);
			}
		});
	}
}

function checkbit(data,errors){
	var temp;
	if(typeof(data) == "string"){
		try{
			temp=$.parseJSON(data);
		}catch (e) {
		}
	}else{
		temp=data;
	}
	if(temp){
		if(!$.isArray(temp)&&typeof(temp)=="object"){
			if(temp["checkBitOk"]== undefined){
				$.each(temp,function(i,n){
					if(typeof(n)=="object"){
						return checkbit(n,errors);
					}
				});
			}else{
				if(temp["checkBitOk"]==false){
					//return temp;
					errors.push(temp)
				}
			}
		}else if($.isArray(temp)){
			$.each(temp,function(i,n){
				return checkbit(n,errors);
			});
		}else{
			return;
		}
	}
	
}


$(function($){
    //备份jquery的ajax方法  
    var _ajax=$.ajax;  
      
    //重写jquery的ajax方法  
    $.ajax=function(opt){  
        //备份opt中error和success方法  
        var fn = {  
            error:function(XMLHttpRequest, textStatus, errorThrown){},  
            success:function(data, textStatus){}  
        }  
        if(opt.error){  
            fn.error=opt.error;  
        }  
        if(opt.success){  
            fn.success=opt.success;  
        }  
          
        //扩展增强处理  
        var _opt = $.extend(opt,{  
            error:function(XMLHttpRequest, textStatus, errorThrown){  
                fn.error(XMLHttpRequest, textStatus, errorThrown);  
            },  
            success:function(data, textStatus){
            	var temp={};
            	if(typeof(data) == "string"){
            		try{
            			 if (data.match("^\{(.+:.+,*){1,}\}$"))
                         {
            				temp=$.parseJSON(data);
                         }
            		}catch (e) {
        			}
            	}else if(typeof(data) == "object"&&!$.isArray(data)){
            		temp=data;
            	}
                if(temp['state'] == 600) {
                    window.location.href="/veh/html/login.html";
                    return;
                }
                var errors =[];
                checkbit(data,errors);
                if(errors.length > 0){
                	var msg = JSON.stringify(errors);
                	
                	$.messager.show({
                		title:'数据非法篡改！',
                		msg:msg,
                		timeout:0,
                		 showType:'fade',
                		 width:'500px',
                		 height:'500px',
                         style:{
                             right:'',
                             bottom:'',
                             
                         }
                	});
                }
                
                fn.success(data, textStatus);
            }  
        });  
        return _ajax(_opt);
    };  
});

/*var progressFlag=false;
$(document).ajaxStart(function(){
	if(!progressFlag){
		$.messager.progress({
			title:"请等待",
			msg:"数据请求中。。。"
		});
		progressFlag=true;
	}
});*/

/*$(document).ajaxComplete(function(){
	if(progressFlag){
		$.messager.progress('close');
		progressFlag=false;
	}
});
*/

setInterval(function(){
	var apr  = $("#autoPrintReport").is(":checked");
    var ywc=$("input[name=statusArry]:checked").val();
	if(apr&&ywc==2){
		$("#checkingVeh").datagrid("reload");
	}
},20000);

var prinArray=[];
function autoPrint(data){
	
	var apr  = $("#autoPrintReport").is(":checked");
    var ywc=$("input[name=statusArry]:checked").val();
    
    var r3 = comm.getBaseParames("report3");
	
	
	
	if(apr&&ywc==2){
		$.each(data.rows,function(i,n){
			if(i<=3&&n.printStatus==0&&n.vehjczt==2){
				setTimeout(function(){
					$("#tab-report").tabs("select","仪器设备检验记录");
					report.loadVehCheckInfo(i,n);
					setTimeout(function(){
						report.createReportNoView($('#print_area1'));
						$("#tab-report").tabs("select","检验报告");
						//report.loadVehCheckInfo(i,n);
						setTimeout(function(){
							report.createReportNoView($('#print_area2'));
							
							if(r3.length>0&&r3[0].id==1){
								$("#tab-report").tabs("select","外检报告");
								setTimeout(function(){
									report.createReportNoView($('#print_area3'));
								},1500);
							}
						},2000);
						$.post("/veh/veh/updatePrintStatus",{jylsh:n.jylsh})
					},2000);
				}, i*4000);
				
			}
		});
	}
	
}




