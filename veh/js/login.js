document.onkeydown = function(e) {

	var e = window.event || e;
	var element = e.srcElement || e.target;

	if (e.keyCode == 13 && element.type != "submit" && element.type != "button"
			&& element.type != "textarea" && element.type != "reset") {
		if (document.all) {
			e.keyCode = 9;
		} else {
			var nexElement = getNextInput(element);
			nexElement.focus();
			e.preventDefault();
		}
	}
}

function getNextInput(input) {

	var form = input.form;
	for (var i = 0; i < form.elements.length; i++) {
		if (form.elements[i] == input) {
			break;
		}
	}
	while (true) {
		if (i++ < form.elements.length) {
			if (form.elements[i].type != "hidden"
					&& form.elements[i].type != 'checkbox') {
				return form.elements[i];
			}
		} else {
			return null;
		}
	}
}

function login() {
	
	var userName=$("#userName").val();
	var password=$("#password").val();
	
	if(userName==""){
		webix.alert("请输入用户名");
		return;
	}
	
	if(password==""){
		webix.alert("请输入密码");
		return;
	}
	
	webix.ajax().post("../user/login",{userName:userName,password:password}, function(text, data){
		var jdata = data.json();
		console.log(jdata)
		if(jdata.state=1){
			window.location.href="/veh/";
		}else{
			webix.alert(jdata.message);
		}
		
	});
}
