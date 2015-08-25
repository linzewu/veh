document.write("<script language='javascript' src='bps/all.js' ></script>");
document.write("<script language='javascript' src='js/vehLogin.js' ></script>");

var veh={
	getBaseParames:function(type){
		var array=[];
		for(var i in bps){
			var bp=bps[i];
			if(bp.type==type){
				var map={};
				map['value']=bp.paramName;
				map['id']=bp.paramValue;
				array.push(map);
			}
		}
		return array;
	}
}

webix.ready(function() {
	webix.ui({
		type : "space",
		rows : [ {
			type : "clean",
			rows : [ {
				view : "label",
				label : "<span class='heading'>上海翔尚机动车检测业务系统</span>"
			}, {
				view : "tabbar",
				tabOffset : 0,
				id : "top-tabbar",
				multiview : true,
				borderless : true,
				options : [ {
					id : "orders",
					value : "机动车检测登陆"
				}, {
					id : "products",
					value : "主控程序"
				}, {
					id : "customers",
					value : "机动车检测报告"
				}, {
					id : "staff",
					value : "系统管理"
				} ]
			}, {
				view : "multiview",
				height : '100%',
				animate : true,
				cells : [ vheLogin ]
			} ]
		} ]
	});
});