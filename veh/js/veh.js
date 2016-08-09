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
		if(jg==null){
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
			
			$("#vehinfo").form("load",temp);
		}
	},
	loadVehCheckInfo:function(index,row){
		$("#win_form_veh").form("clear");
		$("#win_checke_veh_info").window("open");
		if(row){
			var ss=row;
			ss['sf']=ss['hphm'].substring(0,1);
			var hpmh = ss['hphm'].substring(1);
			$("#win_form_veh").form("load",ss);
			$("#win_form_veh input[sid=hphm]").textbox("setValue",hpmh);
			var jyxmArry = row.jyxm.split(",");
			$(".vehCheckeItem").empty();
			$.each(jyxmArry,function(i,n){
				var itemName=comm.getParamNameByValue('jyxm',n);
				var li ="<li><a href=\"#\" class=\"easyui-linkbutton c6\" >"+itemName+"</a></li>";
				$(".vehCheckeItem").append(li);
				$.parser.parse('.vehCheckeItem');
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
						// $("#vehinfo").form("clear");
							$("#panel-vheInfo").panel("refresh");
						// veh.setDefaultConfig();
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
				 // $("#str_jyxm").val(str_jyxm);
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
	loadVehCheckInfo:function(index,row){
		if(row){
			$("#report1").panel({"href":"/veh/html/report/report1.html","onLoad":report.getReport1,baseInfo:row});
			$("#report2").panel({"href":"/veh/html/report/report2.html","onLoad":report.getReport2,baseInfo:row});
			$("#report3").panel({"href":"/veh/html/report/report3.html","onLoad":report.getReport3,baseInfo:row});
			$("#report4").panel({"href":"/veh/html/report/report4.html","onLoad":report.getReport4,baseInfo:row});
			$("#tab-report").tabs("getSelected").panel("refresh");
		}
	},
	getReport1:function(){
		var baseInfo = $(this).panel("options").baseInfo;
		
		 $.messager.progress({
				title:"提示",
				msg:"正在努力加载报表中"
		 });
		
		$.post("/veh/report/getReport1",{jylsh:baseInfo.jylsh},function(data){
			$("#report1_jyjgmc").text(data.title);
			
			$("#report1 [name^='report-baseInfo-']").each(function(i,n){
				var name = $(n).attr("name").replace("report-baseInfo-","");
				
				$(n).text(baseInfo[name]==null?"":comm.getParamNameByValue(name, baseInfo[name]));
			});
			
			$.each(data,function(i,n){
				// 处理灯光
				if(i.indexOf("H")==0){
					var tt = i.split("_");
					$.each(n,function(j,k){
						$("#report1 tr[name="+tt[0]+"] td[name="+tt[1].toLowerCase()+"_"+j+"]").text(k);
					});
					$("#report1 tr[name="+tt[0]+"] td[name=xmpd]").text(veh.jgpd(n.zpd));
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
					var bsStr=report.getBsStr(n.jszt);
					var starts=bsStr.split(",");
					$("#report1 tr[name="+tt[1]+"] td[name=zd_zzdl]").text(n.zzdl+starts[0]);
					$("#report1 tr[name="+tt[1]+"] td[name=zd_yzdl]").text(n.yzdl+starts[1]);
					$("#report1 tr[name="+tt[1]+"] td[name=zd_zzdlcd]").text(n.zzdlcd);
					$("#report1 tr[name="+tt[1]+"] td[name=zd_yzdlcd]").text(n.yzdlcd);
					$("#report1 tr[name="+tt[1]+"] td[name=zd_kzbphl]").text(n.kzbphl);
					$("#report1 tr[name="+tt[1]+"] td[name=zd_kzxczdl]").text(n.kzxczdl);
					$("#report1 tr[name="+tt[1]+"] td[name=xmpd]").text(veh.jgpd(n.zpd));
					$("#report1 tr[name="+tt[1]+"] td[name=dxcs]").text(n.dxcs);
					if(tt[1]=="B0"){
						$("#report1 tr[name=B"+n.zw+"] td[name=zd_b"+n.zw+"_zczdl]").text((Number(n.zzdl)+Number(n.yzdl)))
					}
					
				}else if(i.indexOf("other")==0){
					$("#report1 tr[name=ZC] td[name=other_jczczbzl]").text(n.jczczbzl);
					$("#report1 tr[name=ZC] td[name=other_zdlh]").text(n.zdlh);
					
					$("#report1 tr[name=ZC] td[name=other_zczdl]").text(n.zczdl);
					$("#report1 tr[name=ZC] td[name=zczdpd]").text(veh.jgpd(n.zczdpd));
					$("#report1 tr[name=ZC] td[name=dxcs]").text(baseInfo.jycs);
				}else if(i.indexOf("par")==0){
					$("#report1 tr[name=par] td[name=par_tczclh]").text(n.tczclh);
					$("#report1 tr[name=par] td[name=par_tczdl]").text(n.tczdl);
					$("#report1 tr[name=par] td[name=par_zczczdl]").text(n.zczczdl);
					$("#report1 tr[name=par] td[name=xmpd]").text(veh.jgpd(n.tczdpd));
					$("#report1 tr[name=par] td[name=dxcs]").text(n.dxcs);
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
			
			$.each(yqsbjyjg,function(i,n){
				var tr="<tr><td>"+n.xh+"</td><td>"+n.yqjyxm+"</td><td>"+		
						n.yqjyjg+"</td><td>"+n.yqbzxz+"</td><td>"+(n.yqjgpd==1?"合格":(n.yqjgpd==2?"不合格":""))+"</td><td>"+
						(n.yqjybz==null?"":n.yqjybz)+"</td></tr>";
				$("#tbody_yqsbjyjg").append(tr);
			});
			$.each(rgjyjg,function(i,n){
				var tr="<tr><td>"+n.xh+"</td><td>"+n.rgjyxm+"</td><td>"+n.rgjgpd+"</td><td>"+
				n.rgjysm+"</td><td>"+n.rgjybz+"</td><td>";
			});
		});
		
	},getReport3:function(){
		var baseInfo = $(this).panel("options").baseInfo;
		$("#report3 [name^='report-baseInfo-']").each(function(i,n){
			var name = $(n).attr("name").replace("report-baseInfo-","");
			$(n).text(baseInfo[name]==null?"":comm.getParamNameByValue(name, baseInfo[name]));
		});
	},
	getReport4:function(){
		var baseInfo = $(this).panel("options").baseInfo;
		$.post("/veh/report/getReport4",{jylsh:baseInfo.jylsh},function(datas){
			if(datas.length==0){
				 $("#zdltabs").tabs("add",{
					 title:"制动力曲线",
					 content:"<div class='nullData'>无制动力过程数据</div>"
				 });
			}
			 $.each(datas,function(i,data){
				 var rdata=[];
				 var ldata=[];
					
			    if(data.rigthDataStr!=null){
					var temp=data.rigthDataStr.split(",");
					$.each(temp.splice(300,700),function(i,n){
						rdata.push(Number(n));
					});
				}
				
				if(data.leftDataStr!=null){
					var temp=data.leftDataStr.split(",");
					$.each(temp.splice(300,700),function(i,n){
						ldata.push(Number(n));
					});
				}
				
				 var template="<div style='text-align:center;margin-top: 10px;'><a id='report4Print"+data.zw+"'></a>&nbsp;&nbsp;<a id='showReport4Detail"+data.zw+"'></a><div/>"+
				 "<div style='margin:0 auto;width:740px;' id='report4Contex"+data.zw+"'><div id='container"+data.zw+"'></div></div>";
				 
				 $("#zdltabs").tabs("add",{
					 title:data.zw+"轴制动力曲线",
					 index:data.zw,
					 content:template,
					 selected:i==0?true:false,
					 baseInfo:baseInfo,
					 rdata:rdata,
					 ldata:ldata,
					 data:data
				 });
				 
				 $("#report4Print"+data.zw).linkbutton({
					 iconCls: 'icon-print',
					 text:'打印',
					 onClick:function(){
						 report.createReport($("#report4Contex"+data.zw));
					 }
				 });
				 
				 $("#showReport4Detail"+data.zw).linkbutton({
					 iconCls: 'icon-search',
					 text:'显示详细',
					 onClick:function(){
						 var temp= report.createReport4Teblae(data.zw, ldata, rdata);
						 $("#report4Contex"+data.zw).append(temp);
					 }
				 });
				 
				 
			 });
		});
	},
	createReport4Teblae:function (zw,ldata,rdata){
		
		var report4Table="<table id='report4Table"+zw+"' class='reportTable4'><thead><tr><td colspan='8'><h4>"+zw+"轴制动力</h4></td></tr><tr><td>序号</td><td>左制动力</td><td>右制动力</td><td>制动力差</td><td>序号</td><td>左制动力</td><td>右制动力</td><td>制动力差</td></tr></thead><tbody>";
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
            			console.log("返回非JSON对象");
        			}
            	}else if(typeof(data) == "object"&&!$.isArray(data)){
            		temp=data;
            	}
                if(temp['state'] == 600) {
                    window.location.href="/veh/html/login.html";
                    return;
                }
                fn.success(data, textStatus);
            }  
        });  
        return _ajax(_opt);  
    };  
});

$(document).ajaxStart(function(){
	$.messager.progress({
		title:"请等待",
		msg:"数据请求中。。。"
	}); 
});

$(document).ajaxComplete(function(){
	$.messager.progress('close');
});

