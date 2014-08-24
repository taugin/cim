package com.android.cim.fun;

import java.util.List;

import com.android.cim.fun.info.Conversation;
import com.android.cim.fun.info.SmsInfo;

public interface ISms {

    static int SMS_STATE_NORMAL = 0;
    static int SMS_STATE_SENDING = 1;
    static int SMS_STATE_SENT = 2;
    static int SMS_STATE_READ = 3;
    List<Conversation> getAllConversation();
    List<SmsInfo> getSms(String address);
    int sendSms(String address, String smsContent);
    int getSmsState();
}
