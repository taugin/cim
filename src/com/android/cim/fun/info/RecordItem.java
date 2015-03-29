package com.android.cim.fun.info;


public class RecordItem implements Comparable<RecordItem>{
    public String no;
    public String shortname;
    public String filesize;
    public String filetime;
    public String filename;
    public long time;
    @Override
    public int compareTo(RecordItem item) {
        return Long.valueOf(item.time - time).intValue();
    }
}
