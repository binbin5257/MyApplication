package cn.lds.common.utils;

import cn.lds.common.base.BaseApplication;
import cn.lds.common.table.CarsTable;
import cn.lds.common.utils.json.GsonImplHelp;

/**
 * 缓存方法(未涉及数据库)
 * Created by leadingsoft on 2017/11/30.
 */

public class CacheHelper {
    private static final String TAG = CacheHelper.class.getSimpleName();
    private static final String LOGINID = "loginid";//账号
    private static final String PASSWORLD = "passworld";//账号
    private static final String CAR_INFO_SHOW_CONFIG_LEFT = "car_info_show_config_left";//账号
    private static final String CAR_INFO_SHOW_CONFIG_RIGHT = "car_info_show_config_right";//账号
    //    private static final String TOKEN = "token";//token
    private static final String ACCOUNT = "account";//unserNo
    private static final String JPUSHREGISRATIONID = "registrationID";//极光注册id
    private static final String USUALCAR = "usualcar"; //常用车辆
    private static final String VIN = "vin"; //常用车辆车架号
    private static final String LATITUDE = "longitude"; //经度
    private static final String LONGITUDE = "latitude"; //纬度
    private static final String CITY = "city"; //所在城市
    private static final String COOKIE = "cookie";
    private static final String ADCODE = "adcode";


    /**
     * 用户名信息
     *
     * @param loginId
     */
    public static void setLoginId(String loginId) {
        BaseApplication.getInstance().getCache().put(LOGINID, loginId);
    }

    public static String getLoginId() {
        return BaseApplication.getInstance().getCache().getAsString(LOGINID);
    }

    /**
     * 用户密码
     * @param password 密码
     */
    public static void setPassword(String password){
        BaseApplication.getInstance().getCache().put(PASSWORLD,password);
    }

    public static String getPassworld(){
        return BaseApplication.getInstance().getCache().getAsString(PASSWORLD);
    }

    /**
     * 车辆信息显示配置/左侧
     * @param type 配置项
     */
    public static void setCarInfoShowConfigLeft(String type){
        BaseApplication.getInstance().getCache().put(CAR_INFO_SHOW_CONFIG_LEFT,type);
    }
    /**
     * 车辆信息显示配置/右侧
     * @param type 配置项
     */
    public static void setCarInfoShowConfigRight(String type){
        BaseApplication.getInstance().getCache().put(CAR_INFO_SHOW_CONFIG_RIGHT,type);
    }

    public static String getCarInfoShowConfigRight(){
        return BaseApplication.getInstance().getCache().getAsString(CAR_INFO_SHOW_CONFIG_RIGHT);
    }
    public static String getCarInfoShowConfigLeft(){
        return BaseApplication.getInstance().getCache().getAsString(CAR_INFO_SHOW_CONFIG_LEFT);
    }
//    /**
//     * 登录成功之后返回的token
//     *
//     * @param token
//     */
//    public static void setToken(String token) {
//        BaseApplication.getInstance().getCache().put(TOKEN, token);
//    }
//
//    public static String getToken() {
//        return BaseApplication.getInstance().getCache().getAsString(TOKEN);
//    }

    /**
     * 用户no，好像就新建数据库时有用到
     *
     * @param account
     */
    public static void setAccount(String account) {
        BaseApplication.getInstance().getCache().put(ACCOUNT, account);
    }

    public static String getAccount() {
        return BaseApplication.getInstance().getCache().getAsString(ACCOUNT) == null ? ACCOUNT : BaseApplication.getInstance().getCache().getAsString(ACCOUNT);
    }

    /**
     * 常用车辆信息
     *
     * @param usualcar
     */
    public static void setUsualcar(CarsTable usualcar) {
        if (null == usualcar) {
            setVin("nullVin");
            BaseApplication.getInstance().getCache().put(USUALCAR, "");
        } else {
            setVin(usualcar.getVin());
            String s = GsonImplHelp.get().toJson(usualcar);
            BaseApplication.getInstance().getCache().put(USUALCAR, s);
        }
    }

    public static CarsTable getUsualcar() {
        String s = BaseApplication.getInstance().getCache().getAsString(USUALCAR);
        return GsonImplHelp.get().toObject(s, CarsTable.class);
    }

    /**
     * Vin车架号
     *
     * @param vin
     */
    public static void setVin(String vin) {
        BaseApplication.getInstance().getCache().put(VIN, vin);
    }

    public static String getVin() {
        return ToolsHelper.isNull(BaseApplication.getInstance().getCache().getAsString(VIN)) ? "" : BaseApplication.getInstance().getCache().getAsString(VIN);
//        return "LN86WL17A25165900";
    }

    /**
     * 当前定位位置信息
     *
     * @param lat
     *         纬度
     */
    public static void setLatitude(double lat) {
        BaseApplication.getInstance().getCache().put(LATITUDE, ToolsHelper.toString(lat));
    }

    /**
     * 当前定位位置信息
     *
     * @param lng
     *         经度
     */
    public static void setLongitude(double lng) {
        BaseApplication.getInstance().getCache().put(LONGITUDE, ToolsHelper.toString(lng));
    }

    /**
     * 当前定位所在城市
     *
     * @param city
     *         城市
     */
    public static void setCity(String city) {
        BaseApplication.getInstance().getCache().put(CITY, city);
    }

    public static String getCity() {
        return BaseApplication.getInstance().getCache().getAsString(CITY);
    }

    public static String getLatitude() {
        return BaseApplication.getInstance().getCache().getAsString(LATITUDE);
    }

    public static String getLongitude() {
        return BaseApplication.getInstance().getCache().getAsString(LONGITUDE);
    }

    public static void setCookie( String cookie ) {
        BaseApplication.getInstance().getCache().put(COOKIE, cookie);
    }
    public static String getCookie() {
        return BaseApplication.getInstance().getCache().getAsString(COOKIE);
    }

    public static void setCityAdCode( String cityAdCode ) {
        BaseApplication.getInstance().getCache().put(ADCODE, cityAdCode);
    }

    public static String getCityAdCode() {
        return BaseApplication.getInstance().getCache().getAsString(ADCODE);
    }
}
