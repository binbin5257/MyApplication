package cn.lds.common.constants;

import android.os.Environment;

/**
 * Created by leadingsoft on 2017/11/30.
 */

public class Constants {

    /************************** 系统个性化配置 STA ************************************/
    /**
     * 文件存储地址
     */
    public final static String SYS_CONFIG_FILE_PATH = Environment.getExternalStorageDirectory() + "/leoppard/";
    /**
     * 控车轮询时长
     * 单位 ms
     */
    public final static long SYS_CONFIG_LOOP_TIME = 1000 * 60 * 2;
    /**
     * 控车引擎等待时长
     * 单位 分钟
     */
    public final static long SYS_CONFIG_ENGINE_WAIT = 15;
    /**
     * logout 标识
     */
    public final static String SYS_CONFIG_LOGOUT_FLITER = "com.cusc.lieparrdcar.logout";
}
