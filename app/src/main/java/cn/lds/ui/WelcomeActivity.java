package cn.lds.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import cn.lds.MyApplication;
import cn.lds.R;
import cn.lds.common.base.BaseActivity;
import cn.lds.common.manager.ImageManager;
import cn.lds.common.utils.NetWorkHelper;
import cn.lds.common.utils.ToolsHelper;
import cn.lds.databinding.ActivityWelcomeBinding;

/**
 * 欢迎界面，存在oncetoken则自动登录
 * 消息tag列表需要登录鉴权之后才能够获取，因此自动登录代码，注释，与1期保持一直
 */
public class WelcomeActivity extends BaseActivity {
    private static final String TAG = WelcomeActivity.class.getSimpleName();
    private ActivityWelcomeBinding binding;
//    private AutoLoginType autoLoginType = AutoLoginType.WAITING;//自动登录的状态
//    private boolean isFinshGif = false;


    /**
     * 初始化界面
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_welcome);


        ImageManager.getInstance().loadLocalGif(R.drawable.welcom, binding.welcomeGif);
        if (!NetWorkHelper.isNetworkConnected()) {
            ToolsHelper.showInfo(getApplicationContext(), "无网络连接");
        }
        initView();
        initListener();

//        try {
//            EventBus.getDefault().register(this);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
        MyApplication.getInstance().runOnUiThreadDelay(runnable, 3000);
//        String token = CacheHelper.getToken();
//        if (ToolsHelper.isNull(token)) {
//            autoLoginType = AutoLoginType.FAILED;
//        } else {
//            AccountManager.getInstance().autoLogin();
//        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
//            switch (autoLoginType) {
//                case FAILED:
            startLoginActivity();
//                    break;
//                case SUCCESS:
//                    startMainActivity();
//                    break;
//                default://waitting
//                    isFinshGif = true;
//                    break;
//            }
        }
    };

    /**
     * 跳转登录界面
     */
    private void startLoginActivity() {
        startActivity(new Intent(mContext, LoginActivity.class));
        finish();
    }

//    /**
//     * 跳转主界面
//     */
//    private void startMainActivity() {
//        startActivity(new Intent(mContext, MainActivity.class));
//        finish();
//    }
//
//    /**
//     * 请求成功
//     *
//     * @param event
//     *         请求成功返回
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void requestLogin(HttpRequestEvent event) {
//        HttpResult httpResult = event.getResult();
//        String apiNo = httpResult.getApiNo();
//        if (!HttpApiKey.login.equals(apiNo))
//            return;
//        if (isFinshGif) {
//            startMainActivity();
//        }
//        autoLoginType = AutoLoginType.SUCCESS;
//    }
//
//    /**
//     * 请求失败
//     *
//     * @param event
//     *         请求失败返回
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void requestLogin(HttpRequestErrorEvent event) {
//        HttpResult httpResult = event.getResult();
//        String apiNo = httpResult.getApiNo();
//        if (!HttpApiKey.login.equals(apiNo))
//            return;
//        ToolsHelper.showHttpRequestErrorMsg(mContext, httpResult);
//        if (isFinshGif) {
//            startLoginActivity();
//        }
//        autoLoginType = AutoLoginType.FAILED;
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        try {
//            EventBus.getDefault().unregister(this);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
        MyApplication.getInstance().getHandler().removeCallbacks(runnable);
        runnable = null;
    }

}
