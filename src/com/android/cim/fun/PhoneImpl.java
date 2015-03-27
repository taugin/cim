package com.android.cim.fun;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.TelephonyManager;

import com.android.cim.Constants;
import com.android.cim.manager.TmpStorageManager;

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

    @Override
    public String getExpTime() {
        long startTime = TmpStorageManager.getStartTime(mContext);
        if (startTime == 0) {
            return "";
        } else {
            long curTime = System.currentTimeMillis();
            long expTime = curTime - startTime;
            return change(expTime / 1000);
        }
    }

    public String change(long second) {
        long h = 0;
        long d = 0;
        long s = 0;
        long temp = second % 3600;
        if (second > 3600) {
            h = second / 3600;
            if (temp != 0) {
                if (temp > 60) {
                    d = temp / 60;
                    if (temp % 60 != 0) {
                        s = temp % 60;
                    }
                } else {
                    s = temp;
                }
            }
        } else {
            d = second / 60;
            if (second % 60 != 0) {
                s = second % 60;
            }
        }
        return String.format("%02d:%02d:%02d", h, d, s);
    }

    public List<String> getRecordFiles() {
        File dir = new File(Constants.RECORD_PATH);
        String files[] = dir.list();
        ArrayList<String> list = new ArrayList<String>();
        for (String file : files) {
            if (".nomedia".equals(file)) {
                continue;
            }
            list.add(file);
        }
        return list;
    }

}
