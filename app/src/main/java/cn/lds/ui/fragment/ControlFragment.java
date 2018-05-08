package cn.lds.ui.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Map;

import cn.lds.MyApplication;
import cn.lds.R;
import cn.lds.common.api.HttpApiKey;
import cn.lds.common.base.BaseApplication;
import cn.lds.common.base.BaseFragment;
import cn.lds.common.base.UIInitListener;
import cn.lds.common.data.TransactionsModel;
import cn.lds.common.enums.TransactionsType;
import cn.lds.common.http.HttpRequestErrorEvent;
import cn.lds.common.http.HttpRequestEvent;
import cn.lds.common.http.HttpResult;
import cn.lds.common.manager.CarControlManager;
import cn.lds.common.utils.ToolsHelper;
import cn.lds.common.utils.json.GsonImplHelp;
import cn.lds.databinding.FragmentCarControlBinding;
import cn.lds.ui.CarInpectActivity;
import cn.lds.ui.ControlHistoryActivity;
import cn.lds.widget.dialog.InputPinDialog;
import cn.lds.widget.dialog.LoadingDialogUtils;
import cn.lds.widget.dialog.annotation.ClickPosition;
import cn.lds.widget.dialog.callback.OnDialogClickListener;


/**
 * 车控界面
 */
public class ControlFragment extends BaseFragment implements UIInitListener, View.OnClickListener {
    private FragmentCarControlBinding mBinding;
    private ImageView tsp_log, check_iv;

    private CarControlManager carControlManager;
    private InputPinDialog inputPinDialog;
    private String currentApiNo;

    public ControlFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DetailFragment.
     */
    public static ControlFragment newInstance() {
        ControlFragment fragment = new ControlFragment();
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
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.fragment_car_control, null, false);
        initView();
        initListener();
        return mBinding.getRoot();
    }


    @Override
    public void initView() {
        TextView topTitle = mBinding.getRoot().findViewById(R.id.top_title_tv);
        topTitle.setText("车控");
        tsp_log = mBinding.getRoot().findViewById(R.id.top_back_iv);
        tsp_log.setImageResource(R.drawable.top_tsp_log);

        check_iv = mBinding.getRoot().findViewById(R.id.top_menu_iv);
        check_iv.setImageResource(R.drawable.top_check_iv);


    }

    @Override
    public void initListener() {
        tsp_log.setOnClickListener(this);
        check_iv.setOnClickListener(this);
        mBinding.contrlDoorLlyt.setOnClickListener(this);
        mBinding.contrlLightingLlyt.setOnClickListener(this);
        mBinding.contrlColderLlyt.setOnClickListener(this);
        mBinding.contrlHeatLlyt.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.top_back_iv:
                getActivity().startActivity(new Intent(getActivity(), ControlHistoryActivity.class));//车控历史记录
                break;
            case R.id.top_menu_iv:
//                getActivity().startActivity(new Intent(getActivity(), ControlHistoryListActivity.class));//车辆体检
                getActivity().startActivity(new Intent(getActivity(), CarInpectActivity.class));//车控历史记录
                break;
            case R.id.contrl_door_llyt:
                if (mBinding.contrlDoor.isChecked()) {//车门已关，将要开启车门
                    requestControl(HttpApiKey.unlock);
                } else {//车门已开,将要关闭车门
                    requestControl(HttpApiKey.lock);
                }
                break;
            case R.id.contrl_lighting_llyt:
                if (!mBinding.contrlLighting.isChecked()) {//开启闪灯鸣笛，只持续两秒
                    requestControl(HttpApiKey.flashLightWhistle);
                }
                break;
            case R.id.contrl_colder_llyt:
                if (mBinding.contrlColder.isChecked()) {//关闭空调
                    requestControl(HttpApiKey.airConditionTurnOff);
                } else {//空调制冷
                    requestControl(HttpApiKey.airConditionRefrigerate);
                }
                break;
            case R.id.contrl_heat_llyt:
                if (mBinding.contrlHeat.isChecked()) {//关闭空调
                    requestControl(HttpApiKey.airConditionTurnOff);
                } else {//空调制热
                    requestControl(HttpApiKey.airConditionHeat);
                }
                break;
        }

    }

    /**
     * api请求
     *
     * @param apiNo
     *         url类别
     */
    private void requestControl(String apiNo) {
        currentApiNo = apiNo;
        if (null == carControlManager) {
            carControlManager = CarControlManager.getInstance();
        }
        BaseApplication.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null == inputPinDialog) {
                    inputPinDialog = new InputPinDialog(getActivity()).setOnDialogClickListener(new OnDialogClickListener() {
                        @Override
                        public void onDialogClick(Dialog dialog, String clickPosition) {
                            switch (clickPosition) {
                                case ClickPosition.SUBMIT:
//                                    LoadingDialogUtils.showVertical(getActivity(), "请稍候", false);
                                    mBinding.rlControlWaite.setVisibility(View.VISIBLE);
                                    if(currentApiNo.equals(HttpApiKey.unlock)){
                                        mBinding.tvControlStatus.setText("关闭电源执行中");
//                                        hiddenAnimation(mBinding.ivCarDoor);
//                                        mBinding.ivCarDoor.setVisibility(View.GONE);

                                    }else if(currentApiNo.equals(HttpApiKey.lock)){
                                        mBinding.tvControlStatus.setText("打开电源执行中");
//                                        showAnimation(mBinding.ivCarDoor);
//                                        mBinding.ivCarDoor.setVisibility(View.VISIBLE);
                                    }else if(currentApiNo.equals(HttpApiKey.flashLightWhistle)){
                                        mBinding.tvControlStatus.setText("近距离寻车执行中");
//                                        carLightAnimation(mBinding.ivCarLight);
//                                        mBinding.ivCarLight.setVisibility(View.GONE);
                                    }else if(currentApiNo.equals(HttpApiKey.airConditionTurnOff)){
                                        mBinding.tvControlStatus.setText("电子锁关闭执行中");
//                                        if(mBinding.contrlHeat.isChecked()){
//                                            if(mBinding.ivCarAir.getVisibility() == View.GONE){
//                                                mBinding.ivCarAir.setImageResource(R.drawable.bg_air_heat);
//                                                showAnimation(mBinding.ivCarAir);
//                                                mBinding.ivCarAir.setVisibility(View.VISIBLE);
//                                            }else{
//                                                mBinding.ivCarAir.setImageResource(R.drawable.bg_air_heat);
//                                                hiddenAnimation(mBinding.ivCarAir);
//                                                mBinding.ivCarAir.setVisibility(View.GONE);
//                                            }
//                                        }else
//                                        if(mBinding.contrlColder.isChecked()){
//                                            if(mBinding.ivCarAir.getVisibility() == View.GONE){
//                                                mBinding.ivCarAir.setImageResource(R.drawable.bg_air_cool);
//                                                showAnimation(mBinding.ivCarAir);
//                                                mBinding.ivCarAir.setVisibility(View.VISIBLE);
//                                            }else{
//                                                mBinding.ivCarAir.setImageResource(R.drawable.bg_air_cool);
//                                                hiddenAnimation(mBinding.ivCarAir);
//                                                mBinding.ivCarAir.setVisibility(View.GONE);
//                                            }
//                                        }

                                    }else if(currentApiNo.equals(HttpApiKey.airConditionRefrigerate)){
                                        mBinding.tvControlStatus.setText("电子锁开启执行中");
//                                        if(mBinding.contrlHeat.isChecked()){
//                                            mBinding.ivCarAir.setVisibility(View.GONE);
//                                        }
//                                        if(mBinding.ivCarAir.getVisibility() == View.GONE){
//                                            mBinding.ivCarAir.setImageResource(R.drawable.bg_air_cool);
//                                            showAnimation(mBinding.ivCarAir);
//                                            mBinding.ivCarAir.setVisibility(View.VISIBLE);
//                                        }else{
//                                            mBinding.ivCarAir.setImageResource(R.drawable.bg_air_cool);
//                                            hiddenAnimation(mBinding.ivCarAir);
//                                            mBinding.ivCarAir.setVisibility(View.GONE);
//                                        }
                                    }else if(currentApiNo.equals(HttpApiKey.airConditionHeat)){
                                        mBinding.tvControlStatus.setText("电子锁关闭启执行中");
//                                        if(mBinding.contrlColder.isChecked()){
//                                            mBinding.ivCarAir.setVisibility(View.GONE);
//                                        }
//                                        if(mBinding.ivCarAir.getVisibility() == View.GONE){
//                                            mBinding.ivCarAir.setImageResource(R.drawable.bg_air_heat);
//                                            showAnimation(mBinding.ivCarAir);
//                                            mBinding.ivCarAir.setVisibility(View.VISIBLE);
//                                        }else{
//                                            mBinding.ivCarAir.setImageResource(R.drawable.bg_air_heat);
//                                            hiddenAnimation(mBinding.ivCarAir);
//                                            mBinding.ivCarAir.setVisibility(View.GONE);
//                                        }
                                    }
                                    controlCarEvent(false);
                                    carControlManager.requestControl(currentApiNo, inputPinDialog.getPin());
                                    break;
                            }
                            dialog.dismiss();
                        }
                    });
                }
                String str = "";
                if (currentApiNo.equals(HttpApiKey.lock) || currentApiNo.equals(HttpApiKey.unlock)) {//车门
                    str = "请确认车辆处于以下状态：\n" +
                            "1.停车熄火，手刹点亮\n" +
                            "2.车门，机舱盖全关闭\n" +
                            "3.档位在P挡或空挡\n" +
                            "4.钥匙不在车内\n";
                } else if (currentApiNo.equals(HttpApiKey.flashLightWhistle)) {
                    //闪灯鸣笛
                    str = "请确认车辆处于以下状态：\n" +
                            "1.停车熄火，手刹点亮\n" +
                            "2.车门，机舱盖全关闭\n" +
                            "3.档位在P挡或空挡\n";
                } else if (currentApiNo.equals(HttpApiKey.airConditionRefrigerate) || currentApiNo.equals(HttpApiKey.airConditionHeat)) {
                    //空调启动
                    str = "请确认车辆处于以下状态：\n" +
                            "1.停车熄火，手刹点亮\n" +
                            "2.车门，机舱盖全关闭\n" +
                            "3.档位在P挡或空挡\n" +
                            "4.车辆已落锁，钥匙不在车内\n";
                } else if (currentApiNo.equals(HttpApiKey.airConditionTurnOff)) {
                    //空调关闭
                    str = "请确认车辆处于以下状态：\n" +
                            "1.空调已经远程启动\n" +
                            "2.车辆已停车落锁，手刹点亮\n" +
                            "3.车门，机舱盖全关闭\n" +
                            "4.档位在P挡或空挡\n" +
                            "5.钥匙不在车内";
                }
                inputPinDialog.setHint(str);
                inputPinDialog.show();
            }
        });
    }

    /**
     * 控车事件
     * @param type true 启用控车 false 禁止控车
     */
    private void controlCarEvent(boolean type) {

        mBinding.contrlDoorLlyt.setClickable(type);
        mBinding.contrlDoorLlyt.setFocusable(type);
        mBinding.contrlDoorLlyt.setFocusableInTouchMode(type);

        mBinding.contrlLightingLlyt.setClickable(type);
        mBinding.contrlLightingLlyt.setFocusable(type);
        mBinding.contrlLightingLlyt.setFocusableInTouchMode(type);

        mBinding.contrlColderLlyt.setClickable(type);
        mBinding.contrlColderLlyt.setFocusable(type);
        mBinding.contrlColderLlyt.setFocusableInTouchMode(type);

        mBinding.contrlHeatLlyt.setClickable(type);
        mBinding.contrlHeatLlyt.setFocusable(type);
        mBinding.contrlHeatLlyt.setFocusableInTouchMode(type);
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 控车api请求成功
     *
     * @param event
     *         成功返回
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void requestControl(HttpRequestEvent event) {
        HttpResult httpResult = event.getResult();
        String apiNo = httpResult.getApiNo();
        if (!(HttpApiKey.transactions.equals(apiNo)))
            return;
        TransactionsModel transactionsModel = GsonImplHelp.get().toObject(httpResult.getResult(), TransactionsModel.class);
        TransactionsType type = transactionsModel.getData().getResult();
        if (type == TransactionsType.WAITING_SEND || type == TransactionsType.SENT) {
            carControlManager.startLoop(1000, apiNo);
        } else if (type == TransactionsType.SUCCESS) {
            MyApplication.getInstance().runOnUiThreadDelay(new Runnable() {
                @Override
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mBinding.rlControlWaite.setVisibility(View.GONE);
                        }
                    });
                }
            }, 2000);

            //可以继续控车
            controlCarEvent(true);
            Map<String, String> map = httpResult.getExtras();
            String controlApiNo = map.get("apiNo");
            if (controlApiNo.equals(HttpApiKey.lock)) {
                mBinding.contrlDoor.setChecked(true);
                mBinding.ivCarDoor.setVisibility(View.GONE);
            } else if (controlApiNo.equals(HttpApiKey.unlock)) {
                mBinding.contrlDoor.setChecked(false);
                mBinding.ivCarDoor.setVisibility(View.GONE);
            } else if (controlApiNo.equals(HttpApiKey.flashLightWhistle)) {
                mBinding.contrlLighting.setChecked(true);//2秒后自主 关闭
                mBinding.ivCarLight.setVisibility(View.GONE);
                MyApplication.getInstance().runOnUiThreadDelay(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mBinding.ivCarLight.setVisibility(View.GONE);
                                mBinding.contrlLighting.setChecked(false);
                            }
                        });
                    }
                }, 2000);
            } else if (controlApiNo.equals(HttpApiKey.airConditionRefrigerate)) {
                mBinding.contrlColder.setChecked(true);
                mBinding.ivCarAir.setVisibility(View.GONE);
                mBinding.ivCarAir.setImageResource(R.drawable.bg_air_cool);
                mBinding.contrlHeat.setChecked(false);
            } else if (controlApiNo.equals(HttpApiKey.airConditionHeat)) {
                mBinding.contrlColder.setChecked(false);
                mBinding.ivCarAir.setVisibility(View.GONE);
                mBinding.ivCarAir.setImageResource(R.drawable.bg_air_heat);
                mBinding.contrlHeat.setChecked(true);
            } else if (controlApiNo.equals(HttpApiKey.airConditionTurnOff)) {
                mBinding.contrlColder.setChecked(false);
                mBinding.ivCarAir.setVisibility(View.GONE);
                mBinding.contrlHeat.setChecked(false);
            }
            ToolsHelper.showInfo(getActivity(), type.getValue());
        } else {
            LoadingDialogUtils.dissmiss();
            ToolsHelper.showInfo(getActivity(), type.getValue());
        }
    }

    /**
     * 控车api请求失败
     *
     * @param event
     *         失败返回
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void requestControlFailed(HttpRequestErrorEvent event) {
        HttpResult httpResult = event.getResult();
        String apiNo = httpResult.getApiNo();
        if (!(HttpApiKey.transactions.equals(apiNo)))
            return;
        LoadingDialogUtils.dissmiss();
        if(HttpApiKey.transactions.equals(apiNo)){
            mBinding.rlControlWaite.setVisibility(View.GONE);
            //可以继续控车
            controlCarEvent(true);
        }
        ToolsHelper.showHttpRequestErrorMsg(getActivity(), httpResult);
    }

    public void hiddenAnimation(View view){
        AlphaAnimation alphaAnimation = (AlphaAnimation) AnimationUtils.loadAnimation(getActivity(), R.anim.alpha_hidden);
        view.startAnimation(alphaAnimation);
    }
    public void showAnimation(View view){
        AlphaAnimation alphaAnimation = (AlphaAnimation) AnimationUtils.loadAnimation(getActivity(), R.anim.alpha_show);
        view.startAnimation(alphaAnimation);
    }
    public void carLightAnimation(View view){
        AlphaAnimation alphaAnimation = (AlphaAnimation) AnimationUtils.loadAnimation(getActivity(), R.anim.alpha_car_light);
        view.startAnimation(alphaAnimation);
    }


}
