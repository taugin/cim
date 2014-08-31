package com.android.cim.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.android.cim.Constants;
import com.android.cim.service.AppService;
import com.android.cim.util.Log;

public class CallStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return ;
        }

        Log.d(Log.TAG, "action = " + intent.getAction());
        if (Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(Constants.ACTION_OUTGOING_PHONE);
            serviceIntent.putExtra(Constants.EXTRA_PHONE_NUMBER, getResultData());
            context.startService(serviceIntent);
        } else if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent
                .getAction())) {
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (tm.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
                Intent serviceIntent = new Intent(Constants.ACTION_INCOMING_PHONE);
                serviceIntent.setClass(context, AppService.class);
                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                serviceIntent.putExtra(Constants.EXTRA_PHONE_NUMBER, incomingNumber);
                serviceIntent.putExtra(Constants.EXTRA_PHONE_STATE, tm.getCallState());
                context.startService(serviceIntent);
            } else {
                Intent serviceIntent = new Intent(Constants.ACTION_PHONE_STATE);
                serviceIntent.setClass(context, AppService.class);
                serviceIntent.putExtra(Constants.EXTRA_PHONE_STATE, tm.getCallState());
                context.startService(serviceIntent);
            }
        }
    }

}
