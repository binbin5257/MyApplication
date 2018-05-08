package cn.lds.ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.lds.R;
import cn.lds.common.api.HttpApiKey;
import cn.lds.common.api.ModuleUrls;
import cn.lds.common.base.BaseActivity;
import cn.lds.common.data.base.BaseModel;
import cn.lds.common.http.HttpRequestErrorEvent;
import cn.lds.common.http.HttpRequestEvent;
import cn.lds.common.http.HttpResult;
import cn.lds.common.manager.AccountManager;
import cn.lds.common.utils.CacheHelper;
import cn.lds.common.utils.ToolsHelper;
import cn.lds.common.utils.json.GsonImplHelp;
import cn.lds.databinding.ActivityVerficationLoginPasswordBinding;
import cn.lds.widget.PwdEditText;
import cn.lds.widget.ToastUtil;
import cn.lds.widget.captcha.Captcha;
import cn.lds.widget.dialog.LoadingDialogUtils;

/**
 * 验证登录界面
 * Created by sibinbin on 18-2-27.
 */

public class VerificationLoginPswdActivity extends BaseActivity implements View.OnClickListener {

    private ActivityVerficationLoginPasswordBinding binding;
    private ImageView phoneIv;
    private CountDownTimer mTimer;
    private String mCode;
    private String flag;
    private String loginId;
    private InputMethodManager imm;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_verfication_login_password);
        initView();
        initListener();
    }

    @Override
    public void initView() {
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){ //忘记密码
            flag = bundle.getString("FLAG");
            imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(binding.etInput,InputMethodManager.SHOW_FORCED);
            binding.llInputPhone.setVisibility(View.VISIBLE);
            binding.rlPicVer.setVisibility(View.GONE);
            binding.etInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged( CharSequence s, int start, int count, int after ) {

                }

                @Override
                public void onTextChanged( CharSequence s, int start, int before, int count ) {

                }

                @Override
                public void afterTextChanged( Editable s ) {
                    int length = binding.etInput.getText().toString().length();
                    if(length == 11){
                        if(ToolsHelper.isMobile(binding.etInput.getText().toString())){
                            if(imm != null){
                                imm.hideSoftInputFromWindow(binding.etInput.getWindowToken(), 0); //强制隐藏键盘
                            }
                            binding.llInputPhone.setVisibility(View.GONE);
                            binding.rlPicVer.setVisibility(View.VISIBLE);
                            loginId = binding.etInput.getText().toString();
                        }else{
                            ToastUtil.showToast(mContext,"手机号输入有误");
                        }
                    }
                }
            });
        }else{ //重置密码
            binding.llInputPhone.setVisibility(View.GONE);
            binding.rlPicVer.setVisibility(View.VISIBLE);
            loginId = CacheHelper.getLoginId();
//            binding.mobile.setText("验证码已发送至" + loginId.substring(0,3) + "****" + loginId.substring(7,11));

        }
        TextView topTitle = binding.getRoot().findViewById(R.id.top_title_tv);
        topTitle.setText("验证");
        phoneIv = binding.getRoot().findViewById(R.id.top_menu_iv);
        phoneIv.setImageResource(R.drawable.bg_phone);




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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mTimer != null){
            mTimer.cancel();
        }

    }

    /**
     * 倒计时
     */
    private void countTime() {
        binding.mobile.setText("验证码已发送至" + loginId.substring(0,3) + "****" + loginId.substring(7,11));
        binding.rlSmsVer.setVisibility(View.VISIBLE);
        binding.rlPicVer.setVisibility(View.GONE);
        mTimer = new CountDownTimer(120*1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.time.setText(millisUntilFinished/1000 + "s");
            }

            @Override
            public void onFinish() {
                binding.time.setText(0 + "s");
            }
        }.start();

    }

    @Override
    public void initListener() {
        binding.getRoot().findViewById(R.id.top_back_iv).setOnClickListener(this);
        binding.getRoot().findViewById(R.id.top_menu_lyt).setOnClickListener(this);
        binding.verCode.setOnInputFinishListener(new PwdEditText.OnInputFinishListener() {
            @Override
            public void onInputFinish( String code ) {
                mCode = code;
                //TODO 请求服务器校验验证码
                LoadingDialogUtils.showHorizontal(mContext,"请稍后...");
                AccountManager.getInstance().checkVerificationCode(CacheHelper.getLoginId(),code);
            }
        });
        binding.captCha.setCaptchaListener(new Captcha.CaptchaListener() {
            @Override
            public String onAccess(long time) {
                //请求服务器获取验证码
                getVerificationCode(loginId);//获取验证码
                return "验证通过";
            }

            @Override
            public String onFailed(int count) {
                Toast.makeText(VerificationLoginPswdActivity.this, "验证失败,失败次数" + count, Toast.LENGTH_SHORT).show();
                return "验证失败";
            }

            @Override
            public String onMaxFailed() {
                Toast.makeText(VerificationLoginPswdActivity.this, "验证超过次数，你的帐号被封锁", Toast.LENGTH_SHORT).show();
                return "可以走了";
            }

        });

    }

    /**
     * 进入修改密码页面
     * @param code
     */
    private void enterUpdatePasswordActivity( String code ) {
        Intent intent = new Intent(mContext,UpdatePasswordActivity.class);
        intent.putExtra("VER_CODE", code);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick( View v ) {
        switch (v.getId()){
            case R.id.top_back_iv:
                finish();
                break;
            case R.id.top_menu_lyt:
                ToastUtil.showToast(this,"给客服打电话");
                break;
        }
    }

    /**
     * 通过手机号获取验证码
     */
    public void getVerificationCode(String mobile) {
        LoadingDialogUtils.showHorizontal(this,"请稍后...");
        AccountManager.getInstance().getVerificationCode(mobile);
    }

    /**
     * 服务器请求成功
     *
     * @param event
     *         成功返回
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void reponseSuccess(HttpRequestEvent event) {
        HttpResult httpResult = event.getResult();
        String apiNo = httpResult.getApiNo();
        if (!(HttpApiKey.getVerificationCode.equals(apiNo)
                || HttpApiKey.checkVerificationCode.equals(apiNo)))
            return;
        LoadingDialogUtils.dissmiss();
        switch (apiNo){
            case HttpApiKey.getVerificationCode:
                //120s倒计时
                countTime();
                break;
            case HttpApiKey.checkVerificationCode:
                enterUpdatePasswordActivity(mCode);
                break;

        }
    }
    /**
     * 服务器请求失败
     *
     * @param event
     *         失败返回
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void reponseFailure(HttpRequestErrorEvent event) {
        HttpResult httpResult = event.getResult();
        String apiNo = httpResult.getApiNo();
        if (!(HttpApiKey.getVerificationCode.equals(apiNo)
                || HttpApiKey.checkVerificationCode.equals(apiNo)))
            return;
        LoadingDialogUtils.dissmiss();
        BaseModel model = GsonImplHelp.get().toObject(httpResult.getResult(), BaseModel.class);
        if(model != null && model.getErrors().size() > 0){
            ToastUtil.showToast(this,model.getErrors().get(0).getErrmsg());
        }
        switch (apiNo){
            case HttpApiKey.getVerificationCode:
                break;
            case HttpApiKey.checkVerificationCode:
                break;

        }
    }
}
