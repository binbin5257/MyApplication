package cn.lds.common.utils;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.lds.common.constants.Constants;

public class FileHelper {

    public static final String rootPathName = "com.cusc.lieparrdcar";// 根目录

    public static final String downloadPathName = "Download";// 下载文件存放根目录

    public static final String avatarPathName = "avatar";// 头像文件存放本地目录

    public static final String avatarName = "temp_photo.jpg";// 头像文件存放本地目录

    public static final String carPathName = "car";// 车辆文件存放本地目录

    public static final String carName = "car_photo.jpg";// 车辆文件存放本地目录

    public static final String RTF_IMAGE_JPG = ".jpg";// 图片文件后缀


    /**
     * 检查内存卡是否可用
     *
     * @return
     */
    public static boolean existSDCard() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        } else
            return false;
    }
    /**
     * 调用系统相机头像图片存储位置 生成path
     *
     * @return
     */
    public static String getCropPath() {
        StringBuffer stbPath = new StringBuffer();
        if (existSDCard()) {

            stbPath.append(Environment.getExternalStorageDirectory().getPath());
            stbPath.append(File.separator);
        }
        stbPath.append(rootPathName);
        stbPath.append(File.separator);
        stbPath.append(CacheHelper.getAccount());
        stbPath.append(File.separator);
        stbPath.append(avatarPathName);
        stbPath.append(File.separator);
        return stbPath.toString() + getpicname();
    }
    /**
     * 调用系统相机车辆图片存储位置 生成path
     *
     * @return
     */
    public static String getCarCropPath() {
        StringBuffer stbPath = new StringBuffer();
        if (existSDCard()) {

            stbPath.append(Environment.getExternalStorageDirectory().getPath());
            stbPath.append(File.separator);
        }
        stbPath.append(rootPathName);
        stbPath.append(File.separator);
        stbPath.append(CacheHelper.getAccount());
        stbPath.append(File.separator);
        stbPath.append(carPathName);
        stbPath.append(File.separator);
        return stbPath.toString() + getpicname();
    }

    /**
     * 随机生成图片文件名 文件名是当前时间加六位随机数
     *
     * @return
     */
    public static String getpicname() {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String str = formatter.format(curDate);
        int i = (int) (Math.random() * (999999 - 100000) + 100000);

        return str + String.valueOf(i) + RTF_IMAGE_JPG;
    }
    /**
     * 车辆图片存放地址
     *
     * @return
     */
    public static String getTakeCarPath() {
        StringBuffer stbPath = new StringBuffer();
        if (existSDCard()) {
            stbPath.append(Environment.getExternalStorageDirectory().getPath());
            stbPath.append(File.separator);
        }
        stbPath.append(rootPathName);
        stbPath.append(File.separator);
        stbPath.append(downloadPathName);
        stbPath.append(File.separator);
        stbPath.append(carPathName);
        stbPath.append(File.separator);
        stbPath.append(carName);
        return stbPath.toString();
    }
    /**
     * 头像存放地址
     *
     * @return
     */
    public static String getTakeAvatarPath() {
        StringBuffer stbPath = new StringBuffer();
        if (existSDCard()) {
            stbPath.append(Environment.getExternalStorageDirectory().getPath());
            stbPath.append(File.separator);
        }
        stbPath.append(rootPathName);
        stbPath.append(File.separator);
        stbPath.append(downloadPathName);
        stbPath.append(File.separator);
        stbPath.append(avatarPathName);
        stbPath.append(File.separator);
        stbPath.append(avatarName);
        return stbPath.toString();
    }
    /**
     * 车辆图片存放地址
     *
     * @return
     */
    public static String getCarIconPath() {
        StringBuffer stbPath = new StringBuffer();
        if (existSDCard()) {
            stbPath.append(Environment.getExternalStorageDirectory().getPath());
            stbPath.append(File.separator);
        }
        stbPath.append(rootPathName);
        stbPath.append(File.separator);
        stbPath.append(downloadPathName);
        stbPath.append(File.separator);
        stbPath.append(carPathName);
        stbPath.append(File.separator);
        stbPath.append(carName);
        return stbPath.toString();
    }
    /**
     * 获取车辆图片压缩路径
     * @return
     */
    public static File getCarTemps(){
        File temps = new File(getCarCropPath());
        if (!temps.getParentFile().exists())
            temps.getParentFile().mkdirs();
        return temps;
    }
    /**
     * 获取头像图片压缩路径
     * @return
     */
    public static File getTemps(){
        File temps = new File(getCropPath());
        if (!temps.getParentFile().exists())
            temps.getParentFile().mkdirs();
        return temps;
    }


}
