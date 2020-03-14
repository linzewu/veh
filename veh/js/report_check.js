//刷新性能检验记录单
function refreshXnjyjld(){
	
	var baseInfo = $("#tab-report").tabs("getSelected").panel("options").baseInfo;
	
	$.post("/veh/checkReport/printJyReport",{lsh:baseInfo.jylsh},function(data){
		if(data.state==1){
			
			$("#printTemplet img").attr("src","../cache/report/"+data.data+"?time="+new Date().getTime());
//			var printObj = {};
//			printObj.prview = false;
//			if(params.isView == "true"){
//				printObj.prview = true;
//			}
//			printCYD(printObj);
		}else{
			$.messager.alert("提示",data.message,"error");
		}
		
	},"json").complete(function(){
		$.messager.progress('close');
	});
}
//打印性能检验记录单
function printXnjyjld(){
	var printObj = {};
	printObj.prview = true;
	printObj.template = "printTemplet";
//	if(params.isView == "true"){
//		printObj.prview = true;
//	}
	printCYD(printObj);
}

//刷新性能检验报告单
function refreshXnjybgd(){
	var baseInfo = $("#tab-report").tabs("getSelected").panel("options").baseInfo;
	$.post("/veh/checkReport/printJyBgReport",{lsh:baseInfo.jylsh},function(data){
		if(data.state==1){
			
			$("#printTempletRep img").attr("src","../cache/report/"+data.data+"?time="+new Date().getTime());
//			var printObj = {};
//			printObj.prview = false;
//			if(params.isView == "true"){
//				printObj.prview = true;
//			}
//			printCYD(printObj);
		}else{
			$.messager.alert("提示",data.message,"error");
		}
		
	},"json").complete(function(){
		$.messager.progress('close');
	});
}
//打印性能检验记录单
function printXnjybgd(){
	var printObj = {};
	printObj.prview = true;
	printObj.template = "printTempletRep";
//	if(params.isView == "true"){
//		printObj.prview = true;
//	}
	printCYD(printObj);
}

function printCYD(option) {
	
	var prview=false;
	if(option){
		if(option.prview){
			prview=option.prview
		}
	}
	var LODOP = getLodop(document.getElementById('LODOP_OB'),
			document.getElementById('LODOP_EM'));
	
	
	LODOP.ADD_PRINT_IMAGE(0, 0, 1361 ,983, document
			.getElementById(option.template).innerHTML);
	//LODOP.SET_PRINT_STYLEA(0,"AngleOfPageInside",90)
	LODOP.SET_PRINT_STYLEA(0,"Stretch",2);
	if(prview){
		LODOP.PREVIEW();
	}else{
		LODOP.PRINT();
	}
}