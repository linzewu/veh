document
		.write("<script language='javascript' src='/veh/bps/all.js' ></script>");

var veh = {
	getVehinfo : function() {
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
	toPage : function(target, title, url, param) {
		$(target).panel({
			"title" : title
		});
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
		for ( var i in data) {
			var li = $("<li><a href=\"javascript:void(0)\"><img></a></li>");
			li.find("img").attr("src", data[i].icon);
			li.find("a").append(data[i].title);
			if (data[i].callbak) {
				li.find("a").bind("click", data[i].callbak)
			} else {
				li.find("a").bind(
						"click",
						function() {
							comm.toPage(data[i].target, data[i].title,
									data[i].href, data[i].param);
						})
			}
			ul.append(li);
			if (i == 0) {
				li.find("a").click();
			}
		}
	}
}

var gridUtil = {
	createNew : function(grid) {
		var g = {};
		g.editIndex = null;

		g.endEditing = function() {

			if (g.editIndex == null) {
				return true;
			}
			if ($(grid).datagrid('validateRow', g.editIndex)) {
				$(grid).datagrid('endEdit', g.editIndex);
				$(grid).datagrid("getRows")[g.editIndex]._isOver_=1;
				g.editIndex = null;
				return true;
			} else {
				return false;
			}
		};
		g.append = function() {
			if (g.endEditing()) {
				$(grid).datagrid('appendRow', {_isOver_:0});
				g.editIndex = $(grid).datagrid('getRows').length - 1;
				$(grid).datagrid('selectRow', g.editIndex).datagrid(
						'beginEdit', g.editIndex);
			}
		};
		g.remove = function() {
			var row = $(grid).datagrid("getSelected");
			if (!row) {
				$.messager.alert("提示", "请选择要删除的数据！")
				return;
			}
			if (g.editIndex!= null) {
				$.messager.confirm("请确认","您目前正在编辑数据，是否先取消编辑",function(r){
					if(r){
						g.reject();
//						$(grid).datagrid('cancelEdit', g.editIndex).datagrid(
//								'deleteRow', g.editIndex);
//						g.editIndex = null;
					}
				});
				return;
			}
			$.messager.confirm("请确认","您确认删除该数据？",function(r){
				if (r) {
					var rowIndex = $(grid).datagrid("getRowIndex", row);
					console.log(rowIndex)
					$(grid).datagrid('deleteRow', rowIndex);
					g.editIndex = null;
				}
			});

		};
		g.accept = function() {
			if (g.endEditing()) {
				$(grid).datagrid('acceptChanges');
			}
		};
		g.reject = function() {
			if(g.editIndex!=null){
				$(grid).datagrid('cancelEdit', g.editIndex);
				
				if($(grid).datagrid("getRows")[g.editIndex]._isOver_==0){
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
			if (g.editIndex != index) {
				if (g.endEditing()) {
					$(grid).datagrid('beginEdit',index);
					g.editIndex = index;
				}
			}
		}

		return g;
	}

}

var system = {
	menus : [ {
		"icon" : "/veh/images/user.png",
		"title" : "用户管理",
		href : "/veh/html/UserManager.html",
		target : "#systemContex"
	} ],
	initEvents : function() {
		console.log(system.menus)
		comm.createMume("sysMune", system.menus);
	}
}

$
		.extend(
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
