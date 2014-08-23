package com.android.cim.serv.req;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.util.Log;

import com.android.cim.Constants.Config;
import com.android.cim.WSApplication;
import com.android.cim.fun.IPhone;
import com.android.cim.fun.ISms;
import com.android.cim.serv.support.HttpPostParser;
import com.android.cim.serv.view.ViewFactory;
import com.android.cim.util.CommonUtil;

public class CimHandler implements HttpRequestHandler {

    private CommonUtil mCommonUtil = CommonUtil.getSingleton();
    private ViewFactory mViewFactory = ViewFactory.getSingleton();
    private IPhone mPhone = WSApplication.getInstance().getPhone();
    private ISms mSms = WSApplication.getInstance().getSms();
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext httpContext)
            throws HttpException, IOException {
        String method = request.getRequestLine().getMethod();
        Log.d("taugin", "method = " + method);
        if ("GET".equalsIgnoreCase(method)) {
            HttpEntity entity;
            String contentType = "text/html;charset=" + Config.ENCODING;
            entity = respView(request);
            response.setHeader("Content-Type", contentType);
            response.setEntity(entity);
            return ;
        } else {
            HttpPostParser parser = new HttpPostParser();
            Map<String, String> params = parser.parse(request);
            Log.d("taugin", "params = " + params.get("number"));
            HttpEntity entity;
            String contentType = "text/html;charset=" + Config.ENCODING;
            entity = respView(request);
            response.setHeader("Content-Type", contentType);
            response.setEntity(entity);
        }
    }
    private HttpEntity respView(HttpRequest request) throws IOException {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("conversations", mSms.getAllConversation());
        return mViewFactory.renderTemp(request, "cim.html", data);
    }
}
