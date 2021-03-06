package com.android.cim;

import java.io.IOException;

import net.asfun.jangod.lib.TagLibrary;
import net.asfun.jangod.lib.tag.ResColorTag;
import net.asfun.jangod.lib.tag.ResStrTag;
import net.asfun.jangod.lib.tag.UUIDTag;
import android.app.Application;
import android.content.Intent;

import com.android.cim.Constants.Config;
import com.android.cim.fun.IPhone;
import com.android.cim.fun.ISms;
import com.android.cim.fun.PhoneImpl;
import com.android.cim.fun.SmsImpl;
import com.android.cim.serv.TempCacheFilter;
import com.android.cim.service.AppService;
import com.android.cim.ui.PreferActivity;
import com.android.cim.util.CopyUtil;

/**
 * @brief 应用全局
 * @author join
 */
public class WSApplication extends Application {

    private static WSApplication self;

    private Intent wsServIntent;
    private IPhone mPhone;
    private ISms mSms;

    @Override
    public void onCreate() {
        super.onCreate();

        self = this;
        wsServIntent = new Intent(AppService.ACTION);
        mPhone = new PhoneImpl(this);
        mSms = new SmsImpl(this);
        initAppDir();
        initJangod();
        initAppFilter();

        if (false && !Config.DEV_MODE) {
            /* 全局异常崩溃处理 */
            new CrashHandler(this);
        }

        PreferActivity.restoreAll();
    }

    public static WSApplication getInstance() {
        return self;
    }

    /**
     * @brief 开启全局服务
     */
    public void startWsService() {
        startService(wsServIntent);
    }

    /**
     * @brief 停止全局服务
     */
    public void stopWsService() {
        stopService(wsServIntent);
    }

    /**
     * @brief 初始化应用目录
     */
    private void initAppDir() {
        CopyUtil mCopyUtil = new CopyUtil(getApplicationContext());
        // mCopyUtil.deleteFile(new File(Config.SERV_ROOT_DIR)); // 清理服务文件目录
        try {
            // 重新复制到SDCard，仅当文件不存在时
            mCopyUtil.assetsCopy("ws", Config.SERV_ROOT_DIR, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @brief 初始化Jangod，添加自定义内容
     */
    private void initJangod() {
        /* custom tags */
        TagLibrary.addTag(new ResStrTag());
        TagLibrary.addTag(new ResColorTag());
        TagLibrary.addTag(new UUIDTag());
        /* custom filters */
    }

    /**
     * @brief 初始化应用过滤器
     */
    private void initAppFilter() {
        /* TempCacheFilter */
        TempCacheFilter.addCacheTemps("403.html", "404.html", "503.html");
        /* GzipFilter */
    }

    public IPhone getPhone() {
        return mPhone;
    }

    public ISms getSms() {
        return mSms;
    }
}
