package cn.lds;


import android.content.Context;
import android.support.multidex.MultiDex;

import cn.jpush.android.api.JPushInterface;
import cn.lds.common.base.BaseApplication;
import cn.lds.common.data.ServerHostModel;
import cn.lds.common.manager.ConfigManager;
import cn.lds.common.manager.ImageManager;
import cn.lds.common.manager.RequestManager;
import cn.lds.common.manager.SystemConfigManager;
import cn.lds.common.manager.UMengManager;
import cn.lds.common.utils.json.GsonImplHelp;
import cn.lds.common.table.base.DBManager;
import cn.lds.common.table.base.MyMigration;

/**
 * Created by leadingsoft on 2017/11/29.
 */

public class MyApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        ConfigManager.getInstance().setModel(GsonImplHelp.get().toObject(BuildConfig.SERVER_HOST, ServerHostModel.class));

        ImageManager.getInstance().initFresco(getApplicationContext(), RequestManager.getInstance().getmOkHttpClient());//自定义 httpclient 保持session cookie 一致
        DBManager.getInstance().initDB(getApplicationContext(), "cn.lds_database", 8, new MyMigration());
        //jpush 初始化
        JPushInterface.setDebugMode(true);
        JPushInterface.init(getApplicationContext());
        //umeng 初始化
        UMengManager.getInstance().initUment(getApplicationContext(), true);
        //获取系统配置
        SystemConfigManager.getInstance().getSystemConfig();
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
