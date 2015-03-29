$(document).ready(function() {

    function setlayoutsize() {
        var phonedialw = $("#phone_dial").width();
        var phonedialh = $("#phone_dial").height();

        var contactsw = $("#recordlist").width();

        var smsw = $("#sms").width();
        var divw = phonedialw + contactsw + smsw;

        $("#div_all").width(divw);
        $("#div_all").height(phonedialh);
    }

    function requestSms() {
        showLoading("sms", "短信列表加载中...");
        $.post(
            "action.do",
            {action:"getsmsconversations"},
            function(data){
                $("#sms").html(data);
                removeLoading("sms");
            }
        ).error(function () {
            removeLoading("sms");
        });
    }
    
    function requestRecords() {
        showLoading("record", "录音列表加载中...");
        $.post(
            "action.do",
            {action:"recordlist"},
            function(data){
                $("#recordlist").html(data);
                removeLoading("record");
            }
        ).error(function () {
            removeLoading("record");
        });
    }

    function generateConversation() {
        var w = $("#sms").width();
        var h = $("#sms").height();
        var t = $("#sms").offset().top;
        var l = $("#sms").offset().left;
        var index = $("#sms").css("z-index");
        //alert("w : " + w + " , h : " + h + " , top : " + t + " , left : " + l + " , index : " + index);
        var styleStr = "top:" + t + "px;left:" + l + "px;position:absolute;z-index:10000;background:#FFF;width:" + w + "px;height:" + h + "px;overflow:auto;";
        var isIe = (document.all) ? true : false;
        var isIE6 = isIe && !window.XMLHttpRequest;
        styleStr += (isIe) ? "filter:alpha(opacity=80);" : "opacity:0.8;";
        var divele = $("<div></div>");
        divele.attr("id", "smsconversation");
        //divele.css({"top":t + 'px',"left":l + 'px', "position":"absolute", "z-index":"10000", "background":"#888", "width":w + 'px', "height":h + 'px', "overflow":"auto"});
        divele.attr("style", styleStr);
        divele.show().appendTo("body");
        $("#sms").hide();
    }

    var loadingboxinfo ={"sms":"smsloadingbox", "full":"fullloadingbox", "record":"recordloadingbox", "sendsms":"sendsmsbox"};
    function showLoading(mode, msg) {
        var w, h, t, l;
        var loadingBoxId = "";
        if (mode == "sms") {
            w = $("#sms").width();
            h = $("#sms").height();
            t = $("#sms").offset().top;
            l = $("#sms").offset().left;
        } else if (mode == "full") {
            w = $(window).width();
            h = $(window).height();
            t = 0;
            l = 0;
        } else if (mode == "record") {
            w = $("#recordlist").width();
            h = $("#recordlist").height();
            t = $("#recordlist").offset().top;
            l = $("#recordlist").offset().left;
        } else if (mode == "sendsms") {
            w = $("#phone_dial").width();
            h = $("#phone_dial").height();
            t = $("#phone_dial").offset().top;
            l = $("#phone_dial").offset().left;
        }

        var styleStr = "top:" + t + "px;left:" + l + "px;position:absolute;z-index:10000;background:#888;width:" + w + "px;height:" + h + "px;overflow:auto;";
        var isIe = (document.all) ? true : false;
        var isIE6 = isIe && !window.XMLHttpRequest;
        styleStr += (isIe) ? "filter:alpha(opacity=80);" : "opacity:0.8;";
        var divele = $("<div></div>");
        divele.attr("id", loadingboxinfo[mode]);
        divele.attr("style", styleStr);
        var leftpos = l + (w - 200) / 2;
        var toppos = t + h / 2 - 50;

        var loadingStyle = 'display:block;position:fixed;_position:absolute;left:' + leftpos + 'px;top:' + toppos + 'px;_top:' + toppos + 'px;'; //弹出框的位置
        var loadingBox = $("<div></div>");
        loadingBox.attr("class", "loadingbox");
        loadingBox.attr("style", loadingStyle);
        //创建弹出框里面的内容P标签
        var loadingInfo = $("<p></p>");
        loadingInfo.attr("style", "text-align:center;");
        loadingInfo.html(msg);
        loadingBox.append(loadingInfo);
        divele.append(loadingBox);
        divele.show().appendTo("body");
    }
    
    function removeLoading(mode) {
        $("#"+loadingboxinfo[mode]).remove();
    }
    
    function createEndCall() {
        var w = $("#dial_button_layout").width();
        var h = $("#dial_button").height();
        var t = $("#dial_button").offset().top;
        var l = $("#dial_button").offset().left;
        var index = $("#dial_button").css("z-index");
        //alert("w : " + w + " , h : " + h + " , top : " + t + " , left : " + l + " , index : " + index);
        var styleStr = "top:" + t + ";left:" + l + ";position:fixed;z-index:10000;background:#F00;width:" + w + "px;height:" + h + "px;";
        var isIe = (document.all) ? true : false;
        var isIE6 = isIe && !window.XMLHttpRequest;
        styleStr += (isIe) ? "filter:alpha(opacity=80);" : "opacity:0.8;";
        var divele = $("<div></div>");
        divele.attr("id", "endcalllayout");
        t += 1;
        h += 4;
        divele.css({"top":t + 'px',"left":l + 'px', "position":"absolute", "z-index":"10000", "background":"#FF0", "width":w + 'px', "height":h + 'px', "overflow":"auto"});
        var endbutton = $("<button id='endcallbutton'>挂断</button>")
        endbutton.css({"float":"right", "width":"100%", "height":"100%", "font-size":"50px", "color":"red"});
        divele.append(endbutton);
        divele.show().appendTo("body");
        //$("#dial_button").hide();
    }

     /***
     * 判断返回是否json格式
     */
    function isJson(obj){
        return typeof(obj) == "object";
    }

    function queryPhoneState() {
        //alert("queryPhoneState");
        $.post(
            "action.do",
            {action:"queryphonestate"},
            function(data) {
                if (!isJson(data)) data = eval('('+data+')');
                if ("0" == data.state) {
                    $("#endcalllayout").remove();
                    $("#call_time").html("就绪");
                } else {
                    if (data.time != "") {
                        $("#call_time").html(data.time);
                    }
                    setTimeout(queryPhoneState, 1000);
                }
            }
        );
    }

    function needCreateEndCall() {
        $.post(
            "action.do",
            {action:"queryphonestate"},
            function(data) {
                if (!isJson(data)) data = eval('('+data+')');
                if ("0" != data.state) {
                    createEndCall();
                    setTimeout(queryPhoneState, 1000);
                }
            }
        );
    }
    
    function querysmsstate() {
        $.post(
            "action.do",
            {action:"querysmsstate"},
            function(data) {
                if ("2" == data) {
                    $("#smscontent").attr("value", "");
                    $("#smssend").removeAttr("disabled");
                    removeLoading("sendsms");
                    alert("短信发送成功");
                } else {
                    setTimeout(querysmsstate, 1000);
                }
            }
        );
    }
    
    function sendsmsfun() {
        var number = $("#phonenumber").val();
        var content = $("#smscontent").val();
        if (number.length <= 0 || content.length <= 0) {
            alert("号码或内容不能为空");
            return ;
        }
        if (!number.match(/^1\d{10}$/)) {
            alert("手机号码格式不正确");
            return;
        }
        var value = confirm("确定要发送短信给 : " + number);
        if (!value) {
            alert("取消发送短信");
            return ;
        }

        $("#smssend").attr("disabled", "disabled");
        showLoading("sendsms", "短信发送中...");
        $.post(
	        "action.do",
	        {action:"sendsms",smsnumber:number,smscontent:content},
	        function() {
	            setTimeout(querysmsstate, 1000);
	        }
        ).error(function () {
            removeLoading("sendsms");
        });
        return false;
    }

    $(window).load(function() {
        //setlayoutsize();
        requestRecords();
        requestSms();
        needCreateEndCall();
    });

    $("#smssend").click(function () {
        sendsmsfun();
    });

    $("#dial_button").click(function () {
        var dialnumber = $("#phonenumber").val();
        if (dialnumber == "") {
            alert("电话号码为空");
            return;
        }
        if (!confirm("确定要拨打此号码 ：" + dialnumber + " ?")) {
            return;
        }
        createEndCall();
        $.post(
            "action.do",
            {action:"dial",dialnumber:$("#phonenumber").val()},
            function(data) {
                if ("dial" == data) {
                    setTimeout(queryPhoneState, 3000);
                }
            }
        );
    });

    $("#endcallbutton").live("click", function (){
        $.post(
	        "action.do",
	        {action:"endcall"},
	        function(data) {
	            $("#endcalllayout").remove();
	        }
        );
    });

    $("#getsmsconversations").click(function () {
        $.post(
            "action.do",
            {action:"getsmsconversations"},
            function(data){
                $("#conversations").html(data);
            }
        );
    });
    
    $("#updatesmslist").live("click", function() {
        requestSms();
    });
    
    $("#updaterecordlist").live("click", function() {
        requestRecords();
    });

    $(".sms_href").live("click", function () {
        var number = $(this).children().val();
        showLoading("sms", "短信会话加载中...");
        $.post(
            "action.do",
            {action:"getsmslist", smsnumber:number},
            function(data){
                generateConversation();
                $("#smsconversation").html(data);
                removeLoading("sms");
            }
        ).error(function () {
            removeLoading("sms");
        });
    });

    $("#back").live("click", function() {
        $("#smsconversation").remove();
        $("#sms").show();
    });

    $("#smsnumber").live("click", function() {
        $("#phonenumber").attr("value", $("#smsnumber").html());
    });

    $(".delete_recordfile").live("click", function() {
        var recordfile = $(this).children().val();
        var index = recordfile.lastIndexOf('/');
        var rFile = "";
        if (index != -1) {
            fFile = recordfile.substring(index + 1);
        }
        if(!confirm("确定删除此录音文件 ？\n" + fFile)) {
            return ;
        }
        
        $.post(
            "action.do",
            {action:"deleterecord", recordfile:recordfile},
            function(data){
                requestRecords();
            }
        );
    });

    $(".dial_num_button").click(function (){
        var s = $("#phonenumber").val();
        s += $(this).val();
        $("#phonenumber").attr("value", s);
    });

    $("#phone_dial_delete").click(function (){
        var s = $("#phonenumber").val();
        if (s != null && s.length > 0) {
            s = s.substring(0, s.length - 1);
        }
        $("#phonenumber").attr("value", s);
    });
    
    var timeout = undefined; 


	$("#phone_dial_delete").bind("mousedown", function() { 
	    timeout = setTimeout(function() { 
	        $("#phonenumber").attr("value", "");
	    }, 2000); 
	}); 
	
	$("#phone_dial_delete").bind("mouseup", function() { 
	    clearTimeout(timeout); 
	});
 });
