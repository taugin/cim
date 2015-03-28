package com.android.cim.fun;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.ParseException;
import android.net.Uri;
import android.telephony.TelephonyManager;

import com.android.cim.Constants;
import com.android.cim.fun.info.RecordItem;
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

    public List<RecordItem> getRecordFiles() {
        File dir = new File(Constants.RECORD_PATH);
        String files[] = dir.list();
        ArrayList<RecordItem> list = new ArrayList<RecordItem>();
        RecordItem item = null;
        File recordFile = null;
        int index = 1;
        String splits[] = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (String file : files) {
            if (".nomedia".equals(file)) {
                continue;
            }
            item = new RecordItem();
            item.no = String.valueOf(index);
            splits = file.split("_");
            if (splits.length >= 3) {
                if (splits[2] != null) {
                    item.filename = splits[2];
                } else {
                    item.filename = file;
                }
                long time = 0;
                try {
                     time = Long.parseLong(splits[1]);
                } catch(NumberFormatException e) {
                    time = System.currentTimeMillis();
                }
                item.time = time;
                item.filetime = sdf.format(new Date(time));
            } else {
                item.filename = file;
            }

            recordFile = new File(Constants.RECORD_PATH + file);
            if (recordFile.exists()) {
                item.filesize = formatFileSize(recordFile.length());
            }
            index++;
            list.add(item);
        }
        Collections.sort(list);
        index = 1;
        for (RecordItem recordItem : list) {
            recordItem.no = String.valueOf(index);
            index++;
        }
        return list;
    }
    
    public boolean deleteRecord(String file) {
        File recordFile = new File(file);
        if (recordFile.exists()) {
            return recordFile.delete();
        }
        return false;
    }

    public String formatFileSize(long fileS) {// 转换文件大小
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

}
