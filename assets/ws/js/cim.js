$(document).ready(function() {

    function setlayoutsize() {
        var phonedialw = $("#phone_dial").width();
        var phonedialh = $("#phone_dial").height();

        var contactsw = $("#contacts").width();

        var smsw = $("#sms").width();
        var divw = phonedialw + contactsw + smsw;
        $("#div_all").width(divw);
        $("#div_all").height(phonedialh);
    }

    function requestContacts() {
    
    }
    
    function requestSms() {
        $.post(
            "action.do",
            {action:"getsmsconversations"},
            function(data){
                $("#sms").html(data);
            }
        );
    }

    function generateConversation() {
        var w = $("#sms").width();
        var h = $("#sms").height();
        var t = $("#sms").offset().top;
        var l = $("#sms").offset().left;
        var index = $("#sms").css("z-index");
        //alert("w : " + w + " , h : " + h + " , top : " + t + " , left : " + l + " , index : " + index);
        var styleStr = "top:" + t + ";left:" + l + ";position:fixed;z-index:10000;background:#F00;width:" + w + "px;height:" + h + "px;";
        var isIe = (document.all) ? true : false;
        var isIE6 = isIe && !window.XMLHttpRequest;
        styleStr += (isIe) ? "filter:alpha(opacity=80);" : "opacity:0.8;";
        var divele = $("<div></div>");
        divele.attr("id", "smsconversation");
        divele.css({"top":t + 'px',"left":l + 'px', "position":"absolute", "z-index":"10000", "background":"#888", "width":w + 'px', "height":h + 'px', "overflow":"auto"});
        divele.show().appendTo("body");
        $("#sms").hide();
    }
    $(window).load(function() {
        setlayoutsize();
        requestContacts();
        requestSms();
    });

    $("#smsbutton").click(function () {
        $.post(
        "action.do",
        {action:"sendsms",smsnumber:$("#smsnumber").val(),smscontent:$("#smscontent").val()}
        );
    });
    
    $("#dialbutton").click(function () {
        var dialnumber = $("#dialnumber").val();
        alert(dialnumber);
        $.post(
            "action.do",
            {action:"dial",dialnumber:$("#dialnumber").val()}
        );
    });
    
    $("#endcallbutton").click(function (){
        $.post(
        "action.do",
        {action:"endcall"}
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
    $("a").live("click", function () {
        generateConversation();
        $.post(
            "action.do",
            {action:"getsmslist", smsnumber:$(this).val()},
            function(data){
                $("#smsconversation").html(data);
            }
        );
    });

    $("#back").live("click", function() {
        $("#smsconversation").remove();
        $("#sms").show();
    });

    $(".dial_num_button").click(function (){
        var s = $("#smscontent").val();
        s += $(this).val();
        $("#smscontent").attr("value", s);
    });
    $("#phone_dial_delete").click(function (){
        var s = $("#phonenumber").val();
        if (s != null && s.length > 0) {
            s = s.substring(0, s.length - 1);
        }
        $("#phonenumber").attr("value", s);
    });
    
    var timeout = undefined; 

	$("#dial_button").click(function () {
	});
	
	

	$("#phone_dial_delete").bind("mousedown", function() { 
	    timeout = setTimeout(function() { 
	        $("#smscontent").attr("value", "");
	    }, 2000); 
	}); 
	
	$("#phone_dial_delete").bind("mouseup", function() { 
	    clearTimeout(timeout); 
	});
 });
