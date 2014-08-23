package com.android.cim.fun;

import android.content.Context;

public class PhoneImpl implements IPhone {

    private Context mContext;
    public PhoneImpl(Context context) {
        mContext = context;
    }
    @Override
    public void dial(String phoneNumber) {

    }

    @Override
    public int getPhoneState() {
        return 0;
    }

    @Override
    public long getPhoneTime() {
        return 0;
    }

}
