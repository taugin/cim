package com.android.cim.fun;

import java.util.List;

import android.telephony.TelephonyManager;

import com.android.cim.fun.info.RecordItem;

public interface IPhone {
    static final int IDLE = TelephonyManager.CALL_STATE_IDLE;
    static final int OFFHOOK = TelephonyManager.CALL_STATE_OFFHOOK;
    static final int RINGING = TelephonyManager.CALL_STATE_RINGING;

    void dial(String phoneNumber);

    int getPhoneState();

    long getPhoneTime();

    void endCall();

    String getExpTime();

    List<RecordItem> getRecordFiles();
    
    boolean deleteRecord(String file);
}