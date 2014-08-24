package com.android.cim.fun;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.TelephonyManager;

public class PhoneImpl implements IPhone {

    private Context mContext;
    public PhoneImpl(Context context) {
        mContext = context;
    }
    @Override
    public void dial(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        mContext.startActivity(intent);
    }

    @Override
    public int getPhoneState() {
        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) {
            return TelephonyManager.CALL_STATE_IDLE;
        }
        return telephonyManager.getCallState();
    }

    @Override
    public long getPhoneTime() {
        return 0;
    }
    @Override
    public void endCall() {
        if (getPhoneState() != TelephonyManager.CALL_STATE_IDLE) {
            Telephony.getInstance(mContext).endCall();
        }
    }

}
