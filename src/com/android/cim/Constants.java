package com.android.cim;

import java.io.File;

import android.os.Environment;

/**
 * @brief 应用设置常量
 * @author join
 */
public final class Constants {

    public static String APP_DIR_NAME = "/.cim/";
    public static String APP_DIR = Environment.getExternalStorageDirectory() + APP_DIR_NAME;

    public static class Config {
        public static final boolean DEV_MODE = false;

        public static int PORT = 7766;

        public static String WEBROOT = "/";

        /** 服务资源文件 */
        public static final String SERV_ROOT_DIR = APP_DIR + "root/";

        /** 渲染模板目录 */
        public static final String SERV_TEMP_DIR = SERV_ROOT_DIR + "temp/";

        /** 统一编码 */
        public static final String ENCODING = "UTF-8";

        /** 是否允许下载 */
        public static boolean ALLOW_DOWNLOAD = true;
        /** 是否允许删除 */
        public static boolean ALLOW_DELETE = true;
        /** 是否允许上传 */
        public static boolean ALLOW_UPLOAD = true;

        /** The threshold, in bytes, below which items will be retained in memory and above which they will be stored as a file. */
        public static final int THRESHOLD_UPLOAD = 1024 * 1024; // 1MB

        /** 是否使用GZip */
        public static boolean USE_GZIP = true;
        /** GZip扩展名 */
        public static final String EXT_GZIP = ".gz"; // used in cache

        /** 是否使用文件缓存 */
        public static boolean USE_FILE_CACHE = true;
        /** 文件缓存目录 */
        public static final String FILE_CACHE_DIR = APP_DIR + "cache/";

        /** 缓冲字节长度=1024*4B */
        public static final int BUFFER_LENGTH = 4096;
    }

    public static final String ACTION_INCOMING_PHONE = "com.android.cim.action.INCOMING_PHONE";
    public static final String ACTION_OUTGOING_PHONE = "com.android.cim.action.OUTGOING_PHONE";
    public static final String ACTION_START_RECORDING = "com.android.cim.action.START_RECORDING";

    public static final String ACTION_PHONE_STATE = "com.android.cim.action.PHONE_STATE";

    public static final String EXTRA_PHONE_NUMBER = "com.android.cim.extra.PHONE_NUMBER";
    public static final String EXTRA_PHONE_STATE = "com.android.cim.extra.PHONE_STATE";

    public static final String FILE_RECORD_FOLDER = "cim";

    public static final String RECORD_PATH = Environment.getExternalStorageDirectory() + File.separator + FILE_RECORD_FOLDER + File.separator;

    public static final int FLAG_NONE = 0;
    public static final int FLAG_INCOMING = 1;
    public static final int FLAG_MISSCALL = 2;
    public static final int FLAG_BLOCKCALL = 3;
    public static final int FLAG_OUTGOING = 4;

    public static final String TABLE_RECORD = "record_table";
    public static final String RECORD_CONTACT_ID = "record_contact_id";
    public static final String RECORD_NAME = "record_name";
    public static final String RECORD_FILE = "record_file";
    public static final String RECORD_NUMBER = "record_number";
    public static final String RECORD_FLAG = "record_flag";
    public static final String RECORD_SIZE = "record_size";
    public static final String RECORD_RING = "record_ring";
    public static final String RECORD_START = "record_start";
    public static final String RECORD_END = "record_end";
}
