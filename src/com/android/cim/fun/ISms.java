package com.android.cim.fun;

import java.util.List;

import com.android.cim.fun.info.Conversation;
import com.android.cim.fun.info.SmsInfo;

public interface ISms {

    List<Conversation> getAllConversation();
    List<SmsInfo> getSms(String address);
    int sendSms(String address, String smsContent);
}
