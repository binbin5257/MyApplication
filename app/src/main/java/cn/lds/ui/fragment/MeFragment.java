package cn.lds.ui.fragment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.lds.R;
import cn.lds.common.api.HttpApiKey;
import cn.lds.common.api.ModuleUrls;
import cn.lds.common.base.BaseFragment;
import cn.lds.common.base.UIInitListener;
import cn.lds.common.data.UserInfoModel;
import cn.lds.common.http.HttpRequestErrorEvent;
import cn.lds.common.http.HttpRequestEvent;
import cn.lds.common.http.HttpResult;
import cn.lds.common.manager.AccountManager;
import cn.lds.common.table.CarsTable;
import cn.lds.common.utils.CacheHelper;
import cn.lds.common.utils.ToolsHelper;
import cn.lds.common.utils.json.GsonImplHelp;
import cn.lds.databinding.FragmentMeBinding;
import cn.lds.ui.CarListActivity;
import cn.lds.ui.MainActivity;
import cn.lds.ui.MessageActivity;
import cn.lds.ui.ProfileActivity;
import cn.lds.ui.SettingActivity;
import cn.lds.ui.TripListActivity;
import cn.lds.ui.VoiceListActivity;
import cn.lds.ui.WebviewActivity;


/**
 * 我的界面
 */
public class MeFragment extends BaseFragment implements UIInitListener, View.OnClickListener {
    private FragmentMeBinding mBinding;
    private ImageView main_logo, msg_notice;

    private UserInfoModel model;

    public MeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DetailFragment.
     */
    public static MeFragment newInstance() {
        MeFragment fragment = new MeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.fragment_me, null, false);
        initView();
        initListener();
        return mBinding.getRoot();
    }


    @Override
    public void initView() {
        TextView topTitle = mBinding.getRoot().findViewById(R.id.top_title_tv);
        topTitle.setText("我的");
        main_logo = mBinding.getRoot().findViewById(R.id.top_back_iv);
        main_logo.setImageResource(R.drawable.main_top_icon);
        main_logo.setVisibility(View.INVISIBLE);

        msg_notice = mBinding.getRoot().findViewById(R.id.top_menu_iv);
        msg_notice.setImageResource(R.drawable.main_top_notices);
        AccountManager.getInstance().getPesionInfo();


    }

    @Override
    public void initListener() {
        main_logo.setOnClickListener(this);
        msg_notice.setOnClickListener(this);

        mBinding.mePersonLlyt.setOnClickListener(this);
        mBinding.meOrderLlyt.setOnClickListener(this);
        mBinding.meTripIcon.setOnClickListener(this);
        mBinding.mePageIcon.setOnClickListener(this);
        mBinding.meVoiceIcon.setOnClickListener(this);
        mBinding.meCarIcon.setOnClickListener(this);
        mBinding.meSettingLlyt.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.top_menu_iv:
                startActivity(new Intent(getActivity(), MessageActivity.class));
                break;
            case R.id.me_trip_icon:
                startActivity(new Intent(getActivity(), TripListActivity.class));
                break;
            case R.id.me_page_icon:
                ToolsHelper.showInfo(getActivity(), getString(R.string.happy_waitting));
                break;
            case R.id.me_voice_icon:
//                startActivity(new Intent(getActivity(), VoiceListActivity.class));
                ToolsHelper.showInfo(getActivity(), getString(R.string.happy_waitting));
                break;
            case R.id.me_car_icon:
                Intent carListIntent = new Intent(getActivity(), CarListActivity.class);
                startActivityForResult(carListIntent,0x02);
                break;
            case R.id.me_setting_llyt:
            case R.id.top_back_iv:
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
            case R.id.me_order_llyt:
//                ToolsHelper.showInfo(getActivity(), getString(R.string.happy_waitting));
                enterMyOrderActivity();
                break;
            case R.id.me_person_llyt:
                if(model != null){
                    Intent intent = new Intent(getActivity(), ProfileActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("USERINFO", model.getData());
                    intent.putExtras(bundle);
                    startActivityForResult(intent,500);
                }

                break;
        }
    }

    private void enterMyOrderActivity() {
        Intent intent = new Intent(getActivity(), WebviewActivity.class);
        intent.putExtra("URL","http://123.125.218.29:1082/#/myOrder");
        startActivity(intent);

    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            EventBus.getDefault().register(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    @Override
    public void onStop() {
        super.onStop();
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void persionInfo(HttpRequestEvent event) {
        HttpResult httpResult = event.getResult();
        String apiNo = httpResult.getApiNo();
        if (!HttpApiKey.getPersonalInfo.equals(apiNo))
            return;
        model = GsonImplHelp.get().toObject(httpResult.getResult(), UserInfoModel.class);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                ToastUtil.showToast(getActivity(), "获取个人信息成功！");
                initPersonInfo(model);
            }

        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void persionInfo(HttpRequestErrorEvent event) {
        HttpResult httpResult = event.getResult();
        String apiNo = httpResult.getApiNo();
        if (!HttpApiKey.getPersonalInfo.equals(apiNo))
            return;
        ToolsHelper.showHttpRequestErrorMsg(getActivity(), event.httpResult);
    }

    private void initPersonInfo(UserInfoModel model) {
        mBinding.userName.setText(model.getData().getName());
        if (!TextUtils.isEmpty(model.getData().getAvatarFileRecordNo())) {
            //根据id加载图片
            mBinding.bgAvatar.setImageURI(ModuleUrls.displayFile + model.getData().getAvatarFileRecordNo());
        }
        CarsTable table = CacheHelper.getUsualcar();
        if (null != table) {
            if(TextUtils.isEmpty(table.getMode())){
                mBinding.currentCar.setText(table.getLicensePlate().toString());

            }else{
                mBinding.currentCar.setText(new StringBuilder().append(table.getMode()).append("|").append(table.getLicensePlate()).toString());
            }
        }

    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult(requestCode, resultCode, data);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.onActivityResult(requestCode, resultCode, data);
        if(resultCode == getActivity().RESULT_OK){
            if(requestCode == 0x02){
                String licensePlate = data.getStringExtra("no");
                String mode = data.getStringExtra("mode");
                if(TextUtils.isEmpty(mode)){
                    mBinding.currentCar.setText(licensePlate);

                }else{
                    mBinding.currentCar.setText(new StringBuilder().append(mode).append("|").append(licensePlate).toString());
                }
            }
        }
        if(requestCode == 500){
            AccountManager.getInstance().getPesionInfo();
        }

    }

    public void updateCarInfo() {
        AccountManager.getInstance().getPesionInfo();
    }
}
