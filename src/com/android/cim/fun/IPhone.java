package com.android.cim.fun;

import android.telephony.TelephonyManager;

public interface IPhone {
    static final int IDLE = TelephonyManager.CALL_STATE_IDLE;
    static final int OFFHOOK = TelephonyManager.CALL_STATE_OFFHOOK;
    static final int RINGING = TelephonyManager.CALL_STATE_RINGING;

    void dial(String phoneNumber);

    int getPhoneState();

    long getPhoneTime();
}