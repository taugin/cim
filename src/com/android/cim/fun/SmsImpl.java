package com.android.cim.fun;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.SmsManager;
import android.text.TextUtils;

import com.android.cim.fun.info.Conversation;
import com.android.cim.fun.info.SmsInfo;
import com.android.cim.util.Log;

public class SmsImpl implements ISms {

    private static String SENT_SMS_ACTION = "SENT_SMS_ACTION";
    public static final Uri MMSSMS_FULL_CONVERSATION_URI = Uri.parse("content://mms-sms/conversations");
    public static final Uri CONVERSATION_URI = MMSSMS_FULL_CONVERSATION_URI.buildUpon().appendQueryParameter("simple", "true").build();
    public static final Uri MMSSMS_URI = Uri.parse("content://sms");
    public static final Uri CANONICAL_URI = Uri.parse("content://canonical_address");
    private static final String[] ALL_THREADS_PROJECTION = {
        "_id",
        "date",
        "message_count",
        "recipient_ids",
        "readcount",
        "snippet",
        "read"
        };
    private static final String[] SMS_PROJECTION = {
        "_id",
        "address",
        "body",
        "date",
        "date_sent",
        "read"
    };

    private Object mLock = new Object();
    private int mSmsState = 0;
    private HashMap<String, String> mHashMap = null;

    private Context mContext;
    public SmsImpl(Context context) {
        mContext = context;
        new Thread() {
            public void run() {
                queryContact();
            }
        }.start();
    }
    @Override
    public List<Conversation> getAllConversation() {
        Cursor c = null;
        Conversation conv = null;
        List<Conversation> convList = null;;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd ahh:mm");
        try {
            c = mContext.getContentResolver().query(CONVERSATION_URI, /*ALL_THREADS_PROJECTION*/null, null, null, "date desc");
            if (c != null) {
                if (c.moveToFirst()) {
                    convList = new ArrayList<Conversation>();
                    do {
                        conv = new Conversation();
                        conv.date = c.getLong(c.getColumnIndex("date"));
                        conv.datestr = sdf.format(new Date(conv.date));
                        int msgcount = c.getInt(c.getColumnIndex("message_count"));
                        conv.msgcountstr = String.valueOf(msgcount);
                        conv.recipient_ids = c.getString(c.getColumnIndex("recipient_ids"));
                        int readcount = 0;// c.getInt(c.getColumnIndex("readcount"));
                        conv.readcountstr = String.valueOf(readcount);
                        conv.snippet = c.getString(c.getColumnIndex("snippet"));
                        if (!TextUtils.isEmpty(conv.snippet)) {
                            if (conv.snippet.length() > 50) {
                                conv.snippet = conv.snippet.substring(0, 50);
                                conv.snippet += "...";
                            }
                        }
                        int _id = c.getInt(c.getColumnIndex("_id"));
                        conv.address = getAddress(_id);
                        //Log.d("taugin", "id : " + _id + " , address = " + conv.address);
                        if (!TextUtils.isEmpty(conv.address)) {
                            int index = conv.address.indexOf(':');
                            if (index != -1) {
                                conv.address = conv.address.substring(0, index);
                            }
                            if (!TextUtils.isEmpty(conv.address) && conv.address.startsWith("+86")) {
                                conv.address = conv.address.substring("+86".length());
                            }
                            if (mHashMap != null) {
                                conv.name = mHashMap.get(conv.address);
                            }
                            if (TextUtils.isEmpty(conv.name)) {
                                conv.name = getContactNameFromPhoneBook(mContext, conv.address);
                            }
                            convList.add(conv);
                        }
                    } while (c.moveToNext());
                }
                /*
                if (c.moveToFirst()) {
                    int colCount = c.getColumnCount();
                    String colName = null;
                    String colValue = null;
                    for (int index = 0; index < colCount; index++) {
                        colName = c.getColumnName(index);
                        colValue = c.getString(c.getColumnIndex(colName));
                        Log.d(Log.TAG, colName + " : " + colValue);
                    }
                }*/
            }
        } catch (Exception e) {
            convList = null;
            Log.d(Log.TAG, "error : " + e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return convList;
    }

    @Override
    public List<SmsInfo> getSms(String address) {
        Cursor c = null;
        SmsInfo smsInfo = null;
        List<SmsInfo> smsInfoList = null;
        String selection = null;
        if (!TextUtils.isEmpty(address)) {
            selection = "address like '%" + address + "%'";
        }
        Log.d(Log.TAG, "selection = " + selection);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd h:mm a");
        try {
            c = mContext.getContentResolver().query(MMSSMS_URI, /*SMS_PROJECTION*/
                    null, selection, null, "date desc");
            if (c != null) {
                if (c.moveToFirst()) {
                    smsInfoList = new ArrayList<SmsInfo>();
                    do {
                        smsInfo = new SmsInfo();
                        int _id = c.getInt(c.getColumnIndex("_id"));
                        smsInfo.smsid = String.valueOf(_id);
                        smsInfo.address = c.getString(c.getColumnIndex("address"));
                        smsInfo.body = c.getString(c.getColumnIndex("body"));
                        long date = c.getLong(c.getColumnIndex("date"));
                        smsInfo.date = sdf.format(new Date(date));
                        long date_sent = c.getLong(c.getColumnIndex("date_sent"));
                        smsInfo.date_sent = sdf.format(new Date(date_sent));
                        int type = c.getInt(c.getColumnIndex("type"));
                        if (type == 1) {
                            smsInfo.isSend = null;
                        } else {
                            smsInfo.isSend = "true";
                        }
                        int read = c.getInt(c.getColumnIndex("read"));
                        smsInfo.read = String.valueOf(read);
                        if (mHashMap != null) {
                            smsInfo.name = mHashMap.get(smsInfo.address);
                        }
                        if (TextUtils.isEmpty(smsInfo.name)) {
                            smsInfo.name = getNameFromContact(mContext, smsInfo.address);
                        }
                        smsInfoList.add(smsInfo);
                    } while (c.moveToNext());
                }
            }
        } catch (Exception e) {
            smsInfoList = null;
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return smsInfoList;
    }

    @Override
    public int sendSms(String address, String smsContent) {
        if (TextUtils.isEmpty(address)) {
            return -1;
        }
        setSmsState(SMS_STATE_SENDING);
        SmsManager smsManager = SmsManager.getDefault();
        List<String> divideContents = smsManager.divideMessage(smsContent);
        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        mContext.registerReceiver(mSmsReceiver, new IntentFilter(SENT_SMS_ACTION));
        PendingIntent pandingIntent = PendingIntent.getBroadcast(mContext, 0, sentIntent, 0);
        for (String text : divideContents) {
            smsManager.sendTextMessage(address, null, text, pandingIntent, null);
        }
        ContentValues values = new ContentValues();
        values.put("date", System.currentTimeMillis());
        values.put("type", 2);
        values.put("address", address);
        values.put("body", smsContent);
        Uri uri = mContext.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
        Log.d(Log.TAG, "uri = " + uri);
        return 0;
    }

    
    private String getAddress(int _id) {
        Cursor c = null;
        String selection = "thread_id = " + _id;
        try {
            c = mContext.getContentResolver().query(MMSSMS_URI, null, selection, null, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    String address = c.getString(c.getColumnIndex("address"));
                    return address;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return null;
    }
    private void setSmsState(int state) {
        synchronized (mLock) {
            mSmsState = state;
        }
    }
    @Override
    public int getSmsState() {
        synchronized (mLock) {
            return mSmsState;
        }
    }
    private BroadcastReceiver mSmsReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            setSmsState(SMS_STATE_SENT);
            context.unregisterReceiver(this);
        }
    };

    private static String getNameFromContact(Context context, String phoneNumber) {
        Cursor c = null;
        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, phoneNumber);
            String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE '%" + phoneNumber + "'";
            c = context.getContentResolver().query(uri, null, null, null, null);
            if (c != null && c.moveToFirst()) {
                return c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            }
        } catch (Exception e) {
            Log.d("taugin", e.getLocalizedMessage());
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return null;
    }
    
    public String getContactNameFromPhoneBook(Context context, String phoneNum) {
        String contactName = null;
        ContentResolver cr = context.getContentResolver();
        String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?";
        Cursor pCur = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                selection, new String[] { phoneNum }, null);
        if (pCur.moveToFirst()) {
            contactName = pCur
                    .getString(pCur
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            pCur.close();
        }
        return contactName;
    }

    @Override
    public void resetSmsState() {
        setSmsState(SMS_STATE_NORMAL);
    }
    

    private void queryContact() {
        if (mHashMap != null) {
            return ;
        }
        mHashMap = new HashMap<String, String>();
        String[] PHONES_PROJECTION = new String[] {Phone.DISPLAY_NAME, Phone.NUMBER };
        Cursor c = null;
        try {
            c = mContext.getContentResolver().query(Phone.CONTENT_URI, PHONES_PROJECTION, 
                    null, null, null);
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        String phoneNumber = c.getString(c.getColumnIndex(Phone.NUMBER));
                        String displayName = c.getString(c.getColumnIndex(Phone.DISPLAY_NAME));
                        if (phoneNumber.startsWith("+86")) {
                            phoneNumber = phoneNumber.substring("+86".length());
                        }
                        phoneNumber = phoneNumber.replaceAll("-", "");
                        phoneNumber = phoneNumber.replaceAll("\\s+", "");
                        //Log.d(Log.TAG, phoneNumber + " : " + displayName);
                        if (!TextUtils.isEmpty(displayName)) {
                            mHashMap.put(phoneNumber, displayName);
                        }
                    } while (c.moveToNext());
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
}
