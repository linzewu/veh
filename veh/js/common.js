(function($){  
    $.fn.serializeJson=function(){  
        var serializeObj={};  
        var array=this.serializeArray();  
        var str=this.serialize();  
        $(array).each(function(){  
            if(serializeObj[this.name]){  
                if($.isArray(serializeObj[this.name])){  
                    serializeObj[this.name].push(this.value);  
                }else{  
                    serializeObj[this.name]=[serializeObj[this.name],this.value];  
                }  
            }else{  
                serializeObj[this.name]=this.value;   
            }  
        });  
        return serializeObj;  
    };  
})(jQuery);

$.extend($.fn.validatebox.defaults.rules, {
    passwordEquals: {
        validator: function(value,param){
            return value == $(param[0]).val();
        },
        message: '两次密码不匹配！'
    }
});

$.extend($.fn.validatebox.defaults.rules, {
    passwordValide: {
        validator: function(value){
        	var patrn=/^(?![a-zA-Z0-9]+$)(?![^a-zA-Z\/D]+$)(?![^0-9\/D]+$).{8,16}$/;// /^(\w){8,16}$/;
        	if (!patrn.exec(value)||value=='888888'){
        		 return false
        	} else{
        		return true 
        	}
        },
        message: '不能使用默认密码，只能输入8-16个字母、数字、特殊符号'
    }
});


function updataPassword(){
	$('#win_password').window('open');
}


Date.prototype.Format = function (fmt) {
    var o = {
        "M+": this.getMonth() + 1,
        "d+": this.getDate(),
        "h+": this.getHours(),
        "m+": this.getMinutes(),
        "s+": this.getSeconds(),
        "q+": Math.floor((this.getMonth() + 3) / 3),
        "S": this.getMilliseconds()
    };
    if (/(y+)/.test(fmt))
        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt))
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length === 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
};
