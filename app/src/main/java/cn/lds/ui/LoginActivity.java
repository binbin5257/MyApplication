package cn.lds.ui;

import android.Manifest;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import cn.jpush.android.api.JPushInterface;
import cn.lds.MyApplication;
import cn.lds.R;
import cn.lds.common.api.HttpApiKey;
import cn.lds.common.base.BaseActivity;
import cn.lds.common.base.IPermission;
import cn.lds.common.http.HttpRequestErrorEvent;
import cn.lds.common.http.HttpRequestEvent;
import cn.lds.common.http.HttpResult;
import cn.lds.common.manager.AccountManager;
import cn.lds.common.utils.CacheHelper;
import cn.lds.common.utils.PictureHelper;
import cn.lds.common.utils.ToolsHelper;
import cn.lds.databinding.ActivityLoginBinding;
import cn.lds.widget.ToastUtil;
import cn.lds.widget.captcha.Utils;
import cn.lds.widget.dialog.LoadingDialogUtils;

/**
 * 登录界面
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private ActivityLoginBinding binding;

    private boolean rememberPassword = false;
    private Drawable remeberPaswDrawable;
    private Drawable unRemeberPaswDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        initView();
        initListener();
    }

    public void initView() {
        remeberPaswDrawable = mContext.getResources().getDrawable(
                R.drawable.bg_remeber_pas_select);
        unRemeberPaswDrawable = mContext.getResources().getDrawable(
                R.drawable.bg_remeber_pas_unselect);
        remeberPaswDrawable.setBounds(0, 0, remeberPaswDrawable.getMinimumWidth(), remeberPaswDrawable.getMinimumHeight());
        unRemeberPaswDrawable.setBounds(0, 0, unRemeberPaswDrawable.getMinimumWidth(), unRemeberPaswDrawable.getMinimumHeight());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!ToolsHelper.isNull(CacheHelper.getLoginId()))
            binding.accountUsername.setText(CacheHelper.getLoginId());
        if(!ToolsHelper.isNull(CacheHelper.getPassworld())){
            rememberPassword = true;
            binding.accountPassword.setText(CacheHelper.getPassworld());
            binding.remeberPassword.setCompoundDrawables(remeberPaswDrawable,null,null,null);
        }else{
            rememberPassword = false;
            binding.remeberPassword.setCompoundDrawables(unRemeberPaswDrawable,null,null,null);

        }
        binding.remeberPassword.setCompoundDrawablePadding(Utils.dp2px(mContext,8));

    }

    @Override
    public void initListener() {
        binding.next.setOnClickListener(this);
        binding.tvForgetPassword.setOnClickListener(this);
        binding.userRule.setOnClickListener(this);
        binding.remeberPassword.setOnClickListener(this);
    }

    /**
     * 检查是否动态添加获取手机状态的权限
     */
    private void requestDeviceStatePermission() {
        requestRunTimePermission(new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.ACCESS_COARSE_LOCATION}, new IPermission() {
            @Override
            public void onGranted() {
                requestLogin();
            }

            @Override
            public void onDenied(List<String> deniedPermissions) {
                for (String deniedPermission : deniedPermissions) {
                    ToastUtil.showToast(mContext, "被拒绝的权限是" + deniedPermission);
                }
            }
        });
    }

    /**
     * 界面点击事件
     *
     * @param view
     *         点击的view
     */
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (R.id.next == id) {
            requestDeviceStatePermission();
        } else if (R.id.tv_forget_password == id) {
            Intent intent = new Intent(this, VerificationLoginPswdActivity.class);
            intent.putExtra("FLAG","FORGETPASSWORD");
            startActivity(intent);
        } else if (R.id.user_rule == id) {
            Intent intent1 = new Intent(LoginActivity.this, WebHostActivity.class);
            intent1.putExtra("url", "http://appdb-leopaard.cu-sc.com:1080/view/instruction.html");
            intent1.putExtra("title", "用户协议");
            startActivity(intent1);
        } else if(R.id.remeber_password == id){
            if(rememberPassword){
                rememberPassword = false;
                binding.remeberPassword.setCompoundDrawables(unRemeberPaswDrawable,null,null,null);
            }else{
                rememberPassword = true;
                binding.remeberPassword.setCompoundDrawables(remeberPaswDrawable,null,null,null);
            }
            binding.remeberPassword.setCompoundDrawablePadding(Utils.dp2px(mContext,8));
        }

    }

    /**
     * 封装登录请求数据
     */
    private void requestLogin() {
        String userName = binding.accountUsername.getText().toString();
        if (ToolsHelper.isNull(userName)
                || !ToolsHelper.isMobile(userName)) {
            ToolsHelper.showInfo(mContext, "请输入正确手机号");
            return;
        }
        CacheHelper.setLoginId(userName);
        String password = binding.accountPassword.getText().toString();
        if (ToolsHelper.isNull(password)) {
            ToolsHelper.showInfo(mContext, "请输入密码");
            return;
        }
        String jpushRegistrationID = JPushInterface.getRegistrationID(MyApplication.getInstance().getApplicationContext());

      //  LoadingDialogUtils.showVertical(mContext, "请稍候");
//        startMainActivity();
        AccountManager.getInstance().login(userName, password, jpushRegistrationID);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            EventBus.getDefault().register(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @param event
     *         请求成功返回
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void requestLogin(HttpRequestEvent event) {
        HttpResult httpResult = event.getResult();
        String apiNo = httpResult.getApiNo();
        if (!HttpApiKey.login.equals(apiNo))
            return;
        if(rememberPassword){
            CacheHelper.setPassword(binding.accountPassword.getText().toString());
        }else{
            CacheHelper.setPassword("");
        }
        LoadingDialogUtils.dissmiss();
        startMainActivity();
    }

    /**
     * @param event
     *         请求失败返回
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void requestLogin(HttpRequestErrorEvent event) {
        HttpResult httpResult = event.getResult();
        String apiNo = httpResult.getApiNo();
        if (!HttpApiKey.login.equals(apiNo))
            return;
        LoadingDialogUtils.dissmiss();
        ToolsHelper.showHttpRequestErrorMsg(mContext, httpResult);
    }

    /**
     * 跳转主界面方法
     */
    private void startMainActivity() {
        startActivity(new Intent(mContext, MainActivity.class));
        this.finish();
    }

}
