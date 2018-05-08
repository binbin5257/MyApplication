package cn.lds.ui;

import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.lds.MyApplication;
import cn.lds.R;
import cn.lds.common.api.HttpApiKey;
import cn.lds.common.base.BaseFragmentActivity;
import cn.lds.common.data.LoginModel;
import cn.lds.common.data.UserInfoModel;
import cn.lds.common.file.OnDownloadListener;
import cn.lds.common.http.HttpRequestEvent;
import cn.lds.common.http.HttpResult;
import cn.lds.common.manager.VersionManager;
import cn.lds.common.utils.DeviceHelper;
import cn.lds.common.utils.json.GsonImplHelp;
import cn.lds.databinding.ActivityMainBinding;
import cn.lds.ui.fragment.ControlFragment;
import cn.lds.ui.fragment.DetailFragment;
import cn.lds.ui.fragment.NaviFragment;
import cn.lds.ui.fragment.ServiceFragment;
import cn.lds.ui.fragment.MeFragment;
import cn.lds.widget.ToastUtil;
import cn.lds.widget.dialog.CircleProgressDialog;
import cn.lds.widget.dialog.VersionUpdateDialog;
import cn.lds.widget.dialog.annotation.ClickPosition;
import cn.lds.widget.dialog.callback.OnDialogClickListener;

/**
 * 主界面
 */
public class MainActivity extends BaseFragmentActivity implements View.OnClickListener, DrawerLayout.DrawerListener, RadioGroup.OnCheckedChangeListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding mBinding;
    private ArrayList<Fragment> fragmentList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initView();
        initListener();
    }


    private void initView() {

        //底部菜单栏 和 fragent配置
        initFragments();

    }

    private void initFragments() {
        if (fragmentList == null) {
            fragmentList = new ArrayList<>();
            fragmentList.add(new DetailFragment());
            fragmentList.add(new ControlFragment());
            fragmentList.add(new ServiceFragment());
            fragmentList.add(new NaviFragment());
            fragmentList.add(new MeFragment());

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            for (int i = 0; i < fragmentList.size(); i++) {
                Fragment fragment = fragmentList.get(i);
                transaction.add(R.id.content_flyt, fragment);
                transaction.hide(fragment);
            }
            transaction.show(fragmentList.get(0));
            transaction.commit();

        }
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int checkFragment = 0;
        switch (checkedId) {
            case R.id.main_car_detail:
                checkFragment = 0;
                break;
            case R.id.main_car_control:
                checkFragment = 1;
                break;
            case R.id.main_car_service:
                checkFragment = 2;
                break;
            case R.id.main_car_navi:
                checkFragment = 3;
                break;
            case R.id.main_car_me:
                checkFragment = 4;
                break;
            default:
                break;
        }
        //切换Fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (int i = 0; i < fragmentList.size(); i++) {
            if (checkFragment == i) {
                transaction.show(fragmentList.get(i));
            } else {
                transaction.hide(fragmentList.get(i));
            }
        }
        transaction.commit();
    }

    private void initListener() {
        mBinding.radioGroup.setOnCheckedChangeListener(this);
    }

    /**
     * 点击事件
     *
     * @param view
     *         点击的view
     */
    @Override
    public void onClick(View view) {
        int id = view.getId();

    }


    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {
    }

    @Override
    public void onDrawerClosed(View drawerView) {
    }

    @Override
    public void onDrawerStateChanged(int newState) {

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
    protected void onResume() {
        super.onResume();
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void requestUpdate(final LoginModel.DataBean.DetailsBean detailsBean) {
        //设置极光tag分组
        Set<String> strings = new HashSet<>();
        String[] s = detailsBean.getTagList().split(",");
        strings.addAll(Arrays.asList(s));
        JPushInterface.setTags(mContext, 1, strings);

        int localVersion = DeviceHelper.getAppVersionCode();
        if (localVersion == detailsBean.getAndroidLatestVersion()) {
            return;
        } else {
            VersionUpdateDialog updateDialog = new VersionUpdateDialog(mContext).setOnDialogClickListener(new OnDialogClickListener() {
                @Override
                public void onDialogClick(Dialog dialog, String clickPosition) {
                    dialog.dismiss();
                    switch (clickPosition) {
                        case ClickPosition.SUBMIT:
                            final CircleProgressDialog circleProgressDialog = new CircleProgressDialog(mContext);
                            circleProgressDialog.show();
                            VersionManager.getInstance().downloadApk(detailsBean.getApkDownloadUrl(), new OnDownloadListener() {
                                @Override
                                public void onDownloadSuccess() {
                                    circleProgressDialog.dismiss();
                                    VersionManager.getInstance().installApp();
                                }

                                @Override
                                public void onDownloading(final int progress) {
                                    MyApplication.getInstance().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            circleProgressDialog.setProgress(progress);
                                        }
                                    });
                                }

                                @Override
                                public void onDownloadFailed() {
                                    circleProgressDialog.dismiss();
                                }
                            });
                            break;
                    }
                }
            });
            if (detailsBean.getAndroidMinVersion() > localVersion) {//强制更新
                updateDialog.setMustUpdate(true);
            } else {
                updateDialog.setMustUpdate(false);
            }
            updateDialog.setUpdateContent(detailsBean.getVersionDescription());
            updateDialog.show();
        }
    }

    public void updateCarInfo(){
        if(fragmentList != null && fragmentList.size() == 5){
            MeFragment fragment = (MeFragment) fragmentList.get(4);
            fragment.updateCarInfo();
        }
    }


    // 按两次返回键退出
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            if (keyCode == KeyEvent.KEYCODE_BACK) {

                if (!MyApplication.isExiting) {
                    // ToolsUtil.showInfo(this, "再按一次返回键退出程序！");
                    MyApplication.getInstance().exitApp();
                    return true;
                } else {
                    Intent home = new Intent(Intent.ACTION_MAIN);
                    home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    home.addCategory(Intent.CATEGORY_HOME);
                    startActivity(home);
                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == 0x02){
                if(fragmentList != null && fragmentList.size() == 5){
                    DetailFragment fragment = (DetailFragment) fragmentList.get(0);
                    fragment.initView();
                }
            }

        }
    }
}
