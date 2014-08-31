package com.android.cim.service;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.android.cim.Constants;
import com.android.cim.Constants.Config;
import com.android.cim.R;
import com.android.cim.ftpclient.FtpClient;
import com.android.cim.manager.RecordManager;
import com.android.cim.manager.TmpStorageManager;
import com.android.cim.receiver.NetworkReceiver;
import com.android.cim.receiver.OnNetworkListener;
import com.android.cim.receiver.OnStorageListener;
import com.android.cim.receiver.StorageReceiver;
import com.android.cim.receiver.WSReceiver;
import com.android.cim.util.CommonUtil;
import com.android.cim.util.Log;

/**
 * @brief 应用后台服务
 * @author join
 */
public class AppService extends Service implements OnNetworkListener, OnStorageListener {

    static final String TAG = "WSService";
    static final boolean DEBUG = false || Config.DEV_MODE;

    public static final String ACTION = "org.join.service.AppService";

    public boolean isWebServAvailable = false;

    private boolean isNetworkAvailable;
    private boolean isStorageMounted;

    private RecordManager mRecordManager;
    private String mPhoneNumber;
    @Override
    public void onCreate() {
        super.onCreate();
        NetworkReceiver.register(this, this);
        StorageReceiver.register(this, this);

        CommonUtil mCommonUtil = CommonUtil.getSingleton();
        isNetworkAvailable = mCommonUtil.isNetworkAvailable();
        isStorageMounted = mCommonUtil.isExternalStorageMounted();

        isWebServAvailable = isNetworkAvailable && isStorageMounted;
        notifyWebServAvailable(isWebServAvailable);

        mRecordManager = RecordManager.getInstance(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NetworkReceiver.unregister(this);
        StorageReceiver.unregister(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(boolean isWifi) {
        isNetworkAvailable = true;
        notifyWebServAvailableChanged();
    }

    @Override
    public void onDisconnected() {
        isNetworkAvailable = false;
        notifyWebServAvailableChanged();
    }

    @Override
    public void onMounted() {
        isStorageMounted = true;
        notifyWebServAvailableChanged();
    }

    @Override
    public void onUnmounted() {
        isStorageMounted = false;
        notifyWebServAvailableChanged();
    }

    private void notifyWebServAvailable(boolean isAvailable) {
        if (DEBUG)
            Log.d(TAG, "isAvailable:" + isAvailable);
        // Notify if web service is available.
        String action = isAvailable ? WSReceiver.ACTION_SERV_AVAILABLE
                : WSReceiver.ACTION_SERV_UNAVAILABLE;
        Intent intent = new Intent(action);
        sendBroadcast(intent, WSReceiver.PERMIT_WS_RECEIVER);
    }

    private void notifyWebServAvailableChanged() {
        boolean isAvailable = isNetworkAvailable && isStorageMounted;
        if (isAvailable != isWebServAvailable) {
            notifyWebServAvailable(isAvailable);
            isWebServAvailable = isAvailable;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_STICKY;
        }
        if (Constants.ACTION_INCOMING_PHONE.equals(intent.getAction())) {
            String phoneNumber = intent.getStringExtra(Constants.EXTRA_PHONE_NUMBER);
            int state = intent.getIntExtra(Constants.EXTRA_PHONE_STATE, TelephonyManager.CALL_STATE_IDLE);
            TmpStorageManager.inCallRing(this, phoneNumber, Constants.FLAG_INCOMING, System.currentTimeMillis());
            onCallStateChanged(state);
            Log.d(Log.TAG, "onStartCommand Incoming PhoneNumber" + " : " + phoneNumber);
            Log.getLog(getBaseContext()).recordOperation("Incoming call : " + phoneNumber);
        } else if (Constants.ACTION_OUTGOING_PHONE.equals(intent.getAction())) {
            String phoneNumber = intent.getStringExtra(Constants.EXTRA_PHONE_NUMBER);
            TmpStorageManager.outCallOffHook(this, phoneNumber, Constants.FLAG_OUTGOING, System.currentTimeMillis());
            Log.d(Log.TAG, "onStartCommand Outgoing PhoneNumber" + " : " + phoneNumber);
            startRecord();
            Log.getLog(getBaseContext()).recordOperation("Outgoing call : " + phoneNumber);
        } else if (Constants.ACTION_PHONE_STATE.equals(intent.getAction())) {
            int state = intent.getIntExtra(Constants.EXTRA_PHONE_STATE, TelephonyManager.CALL_STATE_IDLE);
            Log.d(Log.TAG, "onStartCommand state = " + stateToString(state));
            onCallStateChanged(state);
        }
        return START_STICKY;
    }

    private void onCallStateChanged(int state) {
        int lastState = TmpStorageManager.getCallState(this);
        String phoneNumber = TmpStorageManager.getPhoneNumber(this);
        int callFlag = TmpStorageManager.getCallFlag(this);
        boolean callBlock = TmpStorageManager.callBlock(this);
        switch(state) {
        case TelephonyManager.CALL_STATE_IDLE:
            Log.d(Log.TAG, "onCallStateChanged lastState = " + stateToString(lastState) + " , state = [CALL_STATE_IDLE]" + " , CallFlag = " + callFlag + " , callBlock = " + callBlock);
            if (callBlock) {
                TmpStorageManager.clear(this);
                return ;
            }
            TmpStorageManager.callIdle(this, System.currentTimeMillis());
            if (lastState == TelephonyManager.CALL_STATE_OFFHOOK) {
                stopRecord();
            }
            String operation = null;
            if (callFlag == Constants.FLAG_INCOMING) {
                operation = "Incoming phoneNumber : " + phoneNumber + " idle";
             } else if (callFlag == Constants.FLAG_OUTGOING) {
                 operation = "Outgoing phoneNumber : " + phoneNumber + " idle";
            }

            Log.getLog(getBaseContext()).recordOperation(operation);
            TmpStorageManager.toString(this);
            if (lastState == TelephonyManager.CALL_STATE_OFFHOOK || lastState == TelephonyManager.CALL_STATE_RINGING) {
                TmpStorageManager.clear(this);
            }
            TmpStorageManager.toString(this);
            break;
        case TelephonyManager.CALL_STATE_OFFHOOK:
            Log.d(Log.TAG, "onCallStateChanged lastState = " + stateToString(lastState) + " , state = [CALL_STATE_OFFHOOK]" + " , CallFlag = " + callFlag);
            if (callFlag == Constants.FLAG_INCOMING) {
                TmpStorageManager.inCallOffHook(this, System.currentTimeMillis());
                startRecord();
            }

            operation = null;
            if (callFlag == Constants.FLAG_INCOMING) {
                operation = "Incoming phoneNumber : " + phoneNumber + " offhook";
             } else if (callFlag == Constants.FLAG_OUTGOING) {
                 operation = "Outgoing phoneNumber : " + phoneNumber + " offhook";
            }
            Log.getLog(getBaseContext()).recordOperation(operation);
            break;
        case TelephonyManager.CALL_STATE_RINGING:
            Log.d(Log.TAG, "onCallStateChanged lastState = " + stateToString(lastState) + " , state = [CALL_STATE_RINGING]" + " , CallFlag = " + callFlag);
            break;
        default:
            break;
        }
        TmpStorageManager.callState(this, state);
    }
    private void ensureRecordManager() {
        if (mRecordManager == null) {
            mRecordManager = RecordManager.getInstance(this);
        }
    }
    private boolean needRecord() {
        String recordContent = PreferenceManager.getDefaultSharedPreferences(this).getString("key_record_content", "all");
        int callFlag = TmpStorageManager.getCallFlag(this);
        if ("all".equals(recordContent)) {
            return true;
        } else if ("incoming".equals(recordContent) && callFlag == Constants.FLAG_INCOMING) {
            return true;
        } else if ("outgoing".equals(recordContent) && callFlag == Constants.FLAG_OUTGOING) {
            return true;
        }
        return true;
    }
    private void startRecord() {
        ensureRecordManager();
        if (!mRecordManager.recording()) {
            long time = TmpStorageManager.getStartTime(this);

            String phoneNumber = TmpStorageManager.getPhoneNumber(this);
            String fileName = RecordManager.getInstance(AppService.this).getProperName(phoneNumber, time);
            TmpStorageManager.recordName(this, "recorder_" + time + "_" + phoneNumber + ".amr", fileName);
            if (needRecord()) {
                mRecordManager.initRecorder(fileName);
                mRecordManager.startRecorder();
                showNotification();
            }
        }
    }
    private void stopRecord() {
        ensureRecordManager();
        String fileName = TmpStorageManager.getRecordFile(this);
        if (needRecord()) {
            if (mRecordManager.recording()) {
                mRecordManager.stopRecorder();
                Log.d(Log.TAG, "Saved record file " + fileName);
                cancel();
                long size = 0;
                if (fileName != null) {
                    File file = new File(fileName);
                    if (file.exists()) {
                        size = file.length();
                    }
                }
                TmpStorageManager.recordSize(this, size);
            }
        }
        FtpClient ftpClient = new FtpClient("192.168.1.104", "21", "anonymous", "", "record");
        ftpClient.execUpload();
    }
    private void showNotification() {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setWhen(System.currentTimeMillis());
        builder.setOngoing(true);
        builder.setSmallIcon(R.drawable.ic_recording);
        builder.setTicker(getResources().getString(R.string.recording));
        builder.setContentText(getResources().getString(R.string.recording));
        builder.setContentTitle(getResources().getString(R.string.app_name));

        Notification notification = builder.getNotification();
        NotificationManager nm = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        startForeground(123456, notification);
    }
    private void cancel() {
        NotificationManager nm = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        stopForeground(true);
    }
    private String stateToString(int state) {
        switch(state) {
        case TelephonyManager.CALL_STATE_IDLE:
            return "[CALL_STATE_IDLE]";
        case TelephonyManager.CALL_STATE_OFFHOOK:
            return "[CALL_STATE_OFFHOOK]";
        case TelephonyManager.CALL_STATE_RINGING:
            return "[CALL_STATE_RINGING]";
        default:
            return "";
        }
    }
}
