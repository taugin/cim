package com.android.cim.fun;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.android.cim.fun.info.Conversation;
import com.android.cim.fun.info.SmsInfo;

public class SmsImpl implements ISms {

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


    private Context mContext;
    public SmsImpl(Context context) {
        mContext = context;
    }
    @Override
    public List<Conversation> getAllConversation() {
        Cursor c = null;
        Conversation conv = null;
        List<Conversation> convList = null;;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd ahh:mm");
        try {
            c = mContext.getContentResolver().query(CONVERSATION_URI, ALL_THREADS_PROJECTION, null, null, "date desc");
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
                        int readcount = c.getInt(c.getColumnIndex("readcount"));
                        conv.readcountstr = String.valueOf(readcount);
                        conv.snippet = c.getString(c.getColumnIndex("snippet"));
                        int _id = c.getInt(c.getColumnIndex("_id"));
                        conv.address = getAddress(_id);
                        Log.d("taugin", "address = " + conv.address);
                        convList.add(conv);
                    } while (c.moveToNext());
                }
            }
        } catch (Exception e) {
            convList = null;
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return convList;
    }

    @Override
    public List<SmsInfo> getSms(String address) {
        return null;
    }

    @Override
    public int sendSms(String address, String smsContent) {
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
}
