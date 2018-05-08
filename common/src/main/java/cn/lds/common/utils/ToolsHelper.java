package cn.lds.common.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.lds.common.base.BaseApplication;
import cn.lds.common.http.HttpResult;
import cn.lds.widget.dialog.LoadingDialog;

/**
 * Created by leadingsoft on 2017/8/10.
 */

public class ToolsHelper {
    private static final String TAG = ToolsHelper.class.getSimpleName();

    public static boolean isNull(String str) {
        if (null == str || "".equals(str) || " ".equals(str)
                || "null".equals(str)) {
            return true;
        }
        return false;
    }


    /**
     * 将对象转换成String
     *
     * @param obj
     * @return
     */
    public static String toString(Object obj) {
        return null == obj ? "" : String.valueOf(obj);
    }

    /**
     * 弹出提示信息
     *
     * @param mContext
     * @param content
     */
    public static void showInfo(Context mContext, String content) {
        Toast.makeText(mContext, content, Toast.LENGTH_SHORT).show();
    }

    /**
     * 验证手机格式
     */
    public static boolean isMobile(String number) {
    /*
    移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
    联通：130、131、132、152、155、156、185、186、176
    电信：133、153、180、189、（1349卫通）
    总结起来就是第一位必定为1，第二位必定为3或5或7或8，其他位置的可以为0-9
    */
        String num = "[1][3578]\\d{9}";//"[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(number)) {
            return false;
        } else {
            //matches():字符串是否在给定的正则表达式匹配
            return number.matches(num);
        }
    }

    public static void showHttpRequestErrorMsg(Context context, HttpResult httpResult) {
        try {
            JSONObject jsonObject = httpResult.getJsonResult();
            if (httpResult.getResult().contains("SocketTimeoutException")) {
                showInfo(context, "请求超时，请稍候重试");
            }
            if (jsonObject == null) {
                return;
            }

            JSONArray jsonArray = httpResult.getJsonResult().optJSONArray("errors");
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); ++i) {
                    JSONObject json = (JSONObject) jsonArray.get(i);
                    String strErrcode = json.optString("errcode");
                    String strErrmsg = json.optString("errmsg");
                    showInfo(context, strErrmsg);
                }
            }
        } catch (Exception var8) {
            var8.printStackTrace();
        }

    }

    public static int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

}
