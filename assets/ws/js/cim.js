$(document).ready(function() {
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
        $.post(
            "action.do",
            {action:"getsmslist", smsnumber:$(this).val()},
            function(data){
                $("#smslist").html(data);
            }
        );
    });
 });
