package com.android.cim.fun;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

public class Telephony {

    public static Telephony sCallManager = null;
    private Context mContext;
    private ITelephony telephony;
    
    public static Telephony getInstance(Context context) {
        if (sCallManager == null) {
            sCallManager = new Telephony(context);
        }
        return sCallManager;
    }
    private Telephony(Context context) {
        mContext = context;
        try {
            Method method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder)method.invoke(null, new Object[]{Context.TELEPHONY_SERVICE});
            telephony = ITelephony.Stub.asInterface(binder);
        } catch (NoSuchMethodException e) {
            Log.d("taugin", e.getLocalizedMessage());
        } catch (ClassNotFoundException e) {
            Log.d("taugin", e.getLocalizedMessage());
        } catch (IllegalAccessException e) {
            Log.d("taugin", e.getLocalizedMessage());
        } catch (IllegalArgumentException e) {
            Log.d("taugin", e.getLocalizedMessage());
        } catch (InvocationTargetException e) {
            Log.d("taugin", e.getLocalizedMessage());
        }
    }

    public void endCall() {
        Log.d("taugin", "CallManager endCall");
        try {
            telephony.endCall();
        } catch (RemoteException e) {
            Log.d("taugin", e.getLocalizedMessage());
        } catch (IllegalArgumentException e) {
            Log.d("taugin", e.getLocalizedMessage());
        }
    }
    
    public void muteCall() {
        Log.d("taugin", "CallManager muteCall");
        try {
            telephony.silenceRinger();
        } catch (RemoteException e) {
            Log.d("taugin", e.getLocalizedMessage());
        }
    }
}
