package cn.lds.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.lds.R;
import cn.lds.common.base.BaseActivity;
import cn.lds.common.manager.VersionManager;
import cn.lds.databinding.ActivityAboutBinding;

/**
 * 关于界面
 * Created by sibinbin on 18-3-30.
 */

public class AboutActivity extends BaseActivity implements View.OnClickListener {

    private ActivityAboutBinding mBinding;
    private ImageView backIv;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        initView();
        initListener();

    }

    @Override
    public void initView() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_about);
        TextView titile = mBinding.getRoot().findViewById(R.id.top_title_tv);
        backIv = findViewById(R.id.top_back_iv);
        titile.setText("关于");
        mBinding.versionDetail.setContent(VersionManager.getLocalVersionName(this));


    }

    @Override
    public void initListener() {
        backIv.setOnClickListener(this);
        mBinding.versionDetail.setOnClickListener(this);
    }

    @Override
    public void onClick( View v ) {
        switch (v.getId()){
            case R.id.top_back_iv:
                finish();
                break;
            case R.id.version_detail:
                getVersionDetail();
                break;
        }
    }

    public void getVersionDetail() {
        VersionManager.getInstance().getVersionDetail();
    }
}
