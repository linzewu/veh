document
		.write("<script language='javascript' src='/veh/bps/all.js' ></script>");

var veh = {
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
			
			$("#vehinfo").form("load",temp);
		}
	},
	loadVehCheckInfo:function(index,row){
		$("#win_form_veh").form("clear");
		$("#win_checke_veh_info").window("open");
		if(row){
			var ss=row;
			ss['sf']=ss['hphm'].substring(0,1);
			ss['hphm']=ss['hphm'].substring(1);
			$("#win_form_veh").form("load",ss);
			
			var jyxmArry = row.jyxm.split(",");
			$(".mainConsole_checkeItem").empty();
			$.each(jyxmArry,function(i,n){
				var itemName=comm.getParamNameByValue('jyxm',n);
				var li ="<li><a href=\"#\" class=\"easyui-linkbutton c6\" >"+itemName+"</a></li>";
				$(".vehCheckeItem").append(li);
				$.parser.parse('.vehCheckeItem');
			});
		}
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
		var hpzl = $("#quer_checked_hpzl").combobox("getValue");
		var hphm = $("#quer_checked_hphm").val();
		var param={};
		if(hphm&&$.trim(hphm)!=""){
			param.hphm=hphm;
		}
		if(hpzl&&$.trim(hpzl)!=""){
			param.hpzl=hpzl;
		}
		param.statusArry="2";
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
				 console.log(str_jyxm);
				// $("#str_jyxm").val(str_jyxm);
				 
				 var param=$("#vehinfo").serializeJson();
				 param['hphm'] = param['sf']+param['hphm'];
				 param['jcxdh']=jcxdh;
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
						//	$("#vehinfo").form("clear");
							$("#panel-vheInfo").panel("refresh");
						//	veh.setDefaultConfig();
							$(":checkbox[name=jyxm]").prop("checked",false);
							$("#checkingVehList").datagrid("reload");
							$.messager.alert("提示","车辆登录成功。");
						}else{
							$.messager.alert("提示",head.message,"error");
						}
					},"json").error(function(msg){
						$.messager.progress("close");
						console.log(msg);
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
				 console.log(str_jyxm);
				 //$("#str_jyxm").val(str_jyxm);
				var param=$("#vehinfo").serializeJson();
				param['hphm'] = param['sf']+param['hphm'];
				param['jyxm']=str_jyxm;
				console.log(param);
				$.post("/veh/veh/vehLogin",param,function(data){
					data=$.parseJSON(data);
					var head = null;
					var body = null;
					console.log(data);
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
						$("#checkingVehList").datagrid("reload");
						$.messager.alert("提示","车辆登录成功。");
					}
				},"json").error(function(msg){
					$.messager.progress("close");
					console.log(msg);
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

		if (sf == "" || hphm == "" || hpzl == "" || clsbdh == "") {
			$.messager.alert("提示", "获取车辆基本信息必须输入号牌号码、号牌种类、车辆识别代号后4位");
			return false;
		}

		var param = {
			"hphm" : hphm,
			"hpzl" : hpzl,
			"clsbdh" : clsbdh
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

			console.log(data);
			if ($.isArray(data)) {
				head = data[0];
			} else {
				head = data["head"];
				body = data["body"]
			}

			if (head["code"] == 1) {
				$.messager.progress("close");
				body[0]['jyrq'] = body['djrq']
				body[0]['jyyxqz'] = body['yxqz']
				$("#vehinfo").form("load", body[0]);

				console.log("did" + did);
				if (did) {
					clearInterval(did);
					did = null;
					tempCount = 0;
				}
			} else {
				if (!did) {
					did = setInterval(
							function() {
								if (veh.tempCount >= 3) {
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
										text : "第" + veh.tempCount + 1
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
			console.log(data);
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
		$(":checkbox[name=jyxm][value^=H]").prop("checked", false);
		console.log(cllx == "")
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
			} else if (qzdz == "05") {
				$(":checkbox[name=jyxm][value=H1]").prop("checked", true);
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

		var cllxChar = cllx.substring(0, 1);
		var cllxChar2 = cllx.substring(0, 2);

		$(":checkbox[name=jyxm][value=S1]").prop("checked", false);

		if (cllxChar == "H") {
			$(":checkbox[name=jyxm][value=S1]").prop("checked", true);
		}

		if (cllxChar == "K") {
			if (syxz != "A" || (cllxChar2 != "K3" && cllxChar2 != "K4")) {
				$(":checkbox[name=jyxm][value=S1]").prop("checked", true);
			}
		}

	},
	intiEvents : function() {
		$("input[numberboxname=zs]").numberbox({
			"onChange" : function(newValue, oldValue) {
				veh.setVehB16(newValue);
			}
		});

		$("input[textboxname=cllx]").combobox({
			"onChange" : function(newValue, oldValue) {
				veh.setVehH14();
				veh.setVehB0();
				veh.setVehS1();
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
			}
		});

		$("input[textboxname=ccdjrq]").datebox({
			"onChange" : function(newValue, oldValue) {
				veh.setVehB0();
			}
		});

		$("input[textboxname=hdzk]").numberbox({
			"onChange" : function(newValue, oldValue) {
				veh.setVehB0();
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
	createMume : function(id, data) {
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
			
			if (i == 0) {
				li.find("a").click();
			}
		});
		
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
				
				console.log(rows[g.editIndex])
				
				$.post(options["url"]+"/save",rows[g.editIndex],function(rd){
					$.messager.progress("close");
					if(rd.state==1){
						g.editIndex = null;
						rows[g.editIndex]=rd.data;
						if(callback){
							callback.call();
						}
					}else{
						console.log("错误信息：")
						console.log(rd)
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
	menus : [{
		"icon" : "/veh/images/system.png",
		"title" : "系统参数",
		href : "/veh/html/systemInfo.html",
		target : "#systemContex"
	}, {
		"icon" : "/veh/images/user.png",
		"title" : "用户管理",
		href : "/veh/html/UserManager.html",
		target : "#systemContex"
	},{
		"icon" : "/veh/images/device.png",
		"title" : "设备管理",
		href : "/veh/html/DeviceManager.html",
		target : "#systemContex"
	},{
		"icon" : "/veh/images/Workflow.png",
		"title" : "检测流程",
		href : "/veh/html/flowConfig.html",
		target : "#systemContex"
	}],
	initEvents : function() {
		comm.createMume("sysMune", system.menus);
	}
}

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
		}
	});

var mainConsole={
	loadVehCheckInfo:function(index,row){
		if(row){
			$.messager.progress({"title":"正在加载过程数据..."});
			$.post("/veh/veh/getVehCheckeProcess",{'jylsh':row.jylsh},function(data){
				if($.isArray(data)){
					$(".mainConsole_checkeItem").empty();
					$.each(data,function(i,n){
						var itemName=comm.getParamNameByValue('jyxm',n.jyxm);
						var li;
						if(n.status==0){
							li ="<li><a href=\"#\" class=\"easyui-linkbutton c2\" onclick=\"mainConsole.loadCheckItemInfo('"+n.jylsh+"','"+n.jyxm+"')\">"+itemName+"(未检)</a></li>";
						}else if(n.status==1){
							li ="<li><a href=\"#\" class=\"easyui-linkbutton c6\" onclick=\"mainConsole.loadCheckItemInfo('"+n.jylsh+"','"+n.jyxm+"')\">"+itemName+"(检验中)</a></li>";
						}else if(n.status==2){
							li ="<li><a href=\"#\" class=\"easyui-linkbutton c1\" onclick=\"mainConsole.loadCheckItemInfo('"+n.jylsh+"','"+n.jyxm+"')\">"+itemName+"(已检)</a></li>";
						}else if(n.status==3){
							li ="<li><a href=\"#\" class=\"easyui-linkbutton c5\" onclick=\"mainConsole.loadCheckItemInfo('"+n.jylsh+"','"+n.jyxm+"')\">"+itemName+"(复检)</a></li>";
						}
						$(".mainConsole_checkeItem").append(li);
						$.parser.parse('.mainConsole_checkeItem');
					});
				}
			},"json").error(function(){
				$.messager.alert("提示","数据发送失败","error")
			}).complete(function(){
				$.messager.progress("close");
			});
		}
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
		
	}

}
