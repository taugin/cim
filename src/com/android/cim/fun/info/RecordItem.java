package com.android.cim.fun.info;


public class RecordItem implements Comparable<RecordItem>{
    public String no;
    public String filename;
    public String filesize;
    public String filetime;
    public long time;
    @Override
    public int compareTo(RecordItem item) {
        return Long.valueOf(item.time - time).intValue();
    }
}
