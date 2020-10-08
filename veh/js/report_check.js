
function viewToWord(str){
	var myary = new Array();
	myary = str.split(',');
	//获取web应用的根目录
	var url = window.location.toString();
	var pos = url.indexOf('apply');
	url = url.substring(0,pos);
	
}

function printWord(url){
	
	var wdapp;
	try{
		wdapp = new ActiveXObject("Word.Application");
	}catch(e){
		alert("无法调用Office对象，请确保您的机器已安装了Office并已将本系统的站点名加入到IE的信任站点列表中！");
		//wdapp.quit();
		wdapp = null;
		return;
	}
	wdapp.Documents.Open(url);//打开word模板url
	wddoc = wdapp.ActiveDocument;
	wdapp.Application.Printout();//调用自动打印功能
	wdapp.quit();
	wdapp = null;
}

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
	printObj.RESELECT_ORIENT=true;
//	if(params.isView == "true"){
//		printObj.prview = true;
//	}
	printCYD(printObj);
	
//	printWord($("#printTemplet img").attr("src").replace(".jpg",".doc"));
	
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
	
	printObj.left=50;
	printObj.top=50;
	
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
	var top =0;
	var left=0;
	if(option.top){
		top=option.top;
	}
	if(option.left){
		left=option.left;
	}
	
	var LODOP = getLodop(document.getElementById('LODOP_OB'),
			document.getElementById('LODOP_EM'));
	
	LODOP.ADD_PRINT_IMAGE(top, left, 1600 ,1024, document
			.getElementById(option.template).innerHTML);
	//LODOP.SET_PRINT_STYLEA(0,"AngleOfPageInside",90)
	LODOP.SET_PRINT_STYLEA(0,"Stretch",2);
	
	if(option.RESELECT_ORIENT){
		LODOP.SET_PRINT_PAGESIZE(2, 0, 0, "A4");
	}
	
	if(prview){
		LODOP.PREVIEW();
	}else{
		LODOP.PRINT();
	}
	
	
	
}