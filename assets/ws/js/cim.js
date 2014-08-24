$(document).ready(function() {
    $("#smsbutton").click(function () {
        $.post(
        "action.do",
        {action:"sendsms",smsnumber:$("#smsnumber").val(),smscontent:$("#smscontent").val()}
        );
    });
    
    $("#dialbutton").click(function () {
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
 });