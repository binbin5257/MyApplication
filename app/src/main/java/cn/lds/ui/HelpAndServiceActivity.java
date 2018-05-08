package cn.lds.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import cn.lds.R;
import cn.lds.common.base.BaseActivity;
import cn.lds.databinding.ActivityHelpServiceBinding;
import cn.lds.widget.ToastUtil;

/**
 * 帮助与服务
 * Created by sibinbin on 18-3-14.
 */

public class HelpAndServiceActivity extends BaseActivity implements View.OnClickListener {

    private ActivityHelpServiceBinding mBinding;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        initView();
        initListener();
    }

    @Override
    public void initView() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_help_service);
        TextView title = mBinding.getRoot().findViewById(R.id.top_title_tv);
        title.setText("帮助与服务");

    }

    @Override
    public void initListener() {
        mBinding.getRoot().findViewById(R.id.top_back_iv).setOnClickListener(this);
        mBinding.service.setOnClickListener(this);
        mBinding.feedback.setOnClickListener(this);
    }

    @Override
    public void onClick( View v ) {
        switch (v.getId()) {
            case R.id.top_back_iv:
                finish();
                break;
            case R.id.feedback:
                startActivity(new Intent(mContext, FeedBackListActivity.class));
                break;
            case R.id.service:
                ToastUtil.showToast(this,"ui正在设计中...");
                break;
        }

    }
}
