package com.android.cim.serv.req;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import net.asfun.jangod.template.Processor;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.android.cim.Constants.Config;
import com.android.cim.WSApplication;
import com.android.cim.fun.IPhone;
import com.android.cim.fun.ISms;
import com.android.cim.serv.support.HttpPostParser;
import com.android.cim.serv.support.Progress;
import com.android.cim.serv.view.ViewFactory;
import com.android.cim.util.CommonUtil;
import com.android.cim.util.Log;

public class CimHandler implements HttpRequestHandler {

    private CommonUtil mCommonUtil = CommonUtil.getSingleton();
    private ViewFactory mViewFactory = ViewFactory.getSingleton();
    private IPhone mPhone = WSApplication.getInstance().getPhone();
    private ISms mSms = WSApplication.getInstance().getSms();

    private String webRoot;
    public CimHandler(final String webRoot) {
        this.webRoot = webRoot;
    }
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext)
            throws HttpException, IOException {
        String target = URLDecoder.decode(request.getRequestLine().getUri(), Config.ENCODING);
        Log.d(Log.TAG, "target = " + target);
        HttpEntity entity;
        String contentType = "text/html;charset=" + Config.ENCODING;
        File file;
        if (target.equals("/action.do")) {
            processAction(request, response, httpContext);
            return ;
        } else if (target.equals("/")) {
            entity = respView(request);
            response.setHeader("Content-Type", contentType);
            response.setEntity(entity);
            Progress.clear();
            return ;
        } else if (!target.startsWith(Config.SERV_ROOT_DIR) && !target.startsWith(this.webRoot)) {
            response.setStatusCode(HttpStatus.SC_FORBIDDEN);
            response.setEntity(resp403(request));
            return;
        } else {
            file = new File(target);
        }

        if (!file.exists() || file.isDirectory()) { // 不存在
            response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            entity = resp404(request);
        } else if (file.canRead()) { // 可读
            response.setStatusCode(HttpStatus.SC_OK);
            entity = respFile(request, file);
            contentType = entity.getContentType().getValue();
        } else { // 不可读
            response.setStatusCode(HttpStatus.SC_FORBIDDEN);
            entity = resp403(request);
        }

        response.setHeader("Content-Type", contentType);
        response.setEntity(entity);

        Progress.clear();
    }
    private HttpEntity respSmsConversationView(HttpRequest request) throws IOException {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("conversations", mSms.getAllConversation());
        return mViewFactory.renderTemp(request, "sms.html", data);
    }
    private HttpEntity respSmsListView(HttpRequest request, String address) throws IOException {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("smslist", mSms.getSms(address));
        return mViewFactory.renderTemp(request, "sms.html", data);
    }
    private HttpEntity respView(HttpRequest request) throws IOException {
        Map<String, Object> data = new HashMap<String, Object>();
        return mViewFactory.renderTemp(request, "cim.html", data);
    }
    private HttpEntity resp403(HttpRequest request) throws IOException {
        return mViewFactory.renderTemp(request, "403.html");
    }

    private HttpEntity resp404(HttpRequest request) throws IOException {
        return mViewFactory.renderTemp(request, "404.html");
    }
    private HttpEntity respFile(HttpRequest request, File file) throws IOException {
        return mViewFactory.renderFile(request, file);
    }

    private void processAction(HttpRequest request, HttpResponse response, HttpContext httpContext)
            throws HttpException, IOException{
        String method = request.getRequestLine().getMethod();
        Log.d(Log.TAG, "method = " + method);
        HttpEntity entity = null;
        HttpPostParser parser = new HttpPostParser();
        Map<String, String> params = parser.parse(request);
        String type = params.get("action");
        Log.d(Log.TAG, "type = " + type);
        if ("sendsms".equals(type)) { // 发送短信
            String address = params.get("smsnumber");
            address = URLDecoder.decode(address, "utf-8");
            String smsContent = params.get("smscontent");
            smsContent = URLDecoder.decode(smsContent, "utf-8");
            Log.d(Log.TAG, "type = " + type + " , address = " + address + " , smsContent = " + smsContent);
            mSms.sendSms(address, smsContent);
        } else if ("dial".equals(type)){ // 打电话
            String address = params.get("dialnumber");
            Log.d(Log.TAG, "type = " + type + " , address = " + address);
            if (!TextUtils.isEmpty(address)) {
                address = URLDecoder.decode(address, "utf-8");
                mPhone.dial(address);
            }
        } else if ("endcall".equals(type)) { // 挂电话
            Log.d(Log.TAG, "type = " + type);
            mPhone.endCall();
        } else if ("getsmsconversations".equals(type)) { // 获取短信会话
            entity = respSmsConversationView(request);
        } else if ("getsmslist".equals(type)) { // 获取短信
            String address = params.get("smsnumber");
            Log.d(Log.TAG, "type = " + type + " , address = " + address);
            if (address != null) {
                address = URLDecoder.decode(address, "utf-8");
                if (address.startsWith("+86")) {
                    address = address.substring("+86".length());
                }
            }
            entity = respSmsListView(request, address);
        }
        String contentType = "text/html;charset=" + Config.ENCODING;
        response.setHeader("Content-Type", contentType);
        response.setEntity(entity);
        Progress.clear();
    }
}
