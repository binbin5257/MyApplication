package cn.lds.ui.fragment;

import android.content.Intent;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.MenuPopupWindow;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.lds.R;
import cn.lds.common.api.HttpApiKey;
import cn.lds.common.base.BaseFragment;
import cn.lds.common.base.UIInitListener;
import cn.lds.common.data.CarInfoShowConfigModel;
import cn.lds.common.data.ConditionReportModel;
import cn.lds.common.http.HttpRequestErrorEvent;
import cn.lds.common.http.HttpRequestEvent;
import cn.lds.common.http.HttpResult;
import cn.lds.common.manager.CarControlManager;
import cn.lds.common.photoselector.utils.ScreenUtils;
import cn.lds.common.table.CarsTable;
import cn.lds.common.table.base.DBManager;
import cn.lds.common.utils.CacheHelper;
import cn.lds.common.utils.OnItemClickListener;
import cn.lds.common.utils.SpacesItemDecoration;
import cn.lds.common.utils.TimeHelper;
import cn.lds.common.utils.ToolsHelper;
import cn.lds.common.utils.json.GsonImplHelp;
import cn.lds.databinding.FragmentCarDetailBinding;
import cn.lds.ui.CarInfoShowConfigActivity;
import cn.lds.ui.MainActivity;
import cn.lds.ui.MessageActivity;
import cn.lds.ui.SettingActivity;
import cn.lds.ui.adapter.CarDetailOthersAdapter;
import cn.lds.ui.adapter.MenuCarListAdapter;
import cn.lds.widget.captcha.Utils;
import cn.lds.widget.dialog.LoadingDialogUtils;
import cn.lds.widget.pullToRefresh.PullToRefreshBase;
import io.realm.Realm;
import io.realm.RealmResults;


/**
 * 车况界面
 */
public class DetailFragment extends BaseFragment implements UIInitListener, View.OnClickListener, OnItemClickListener {

    private FragmentCarDetailBinding mBinding;
    private List<CarsTable> carsTableList;
    private Typeface typeface;//字体
    private List<String> data_list;
    private ConditionReportModel.DataBean dataBean;

    public DetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DetailFragment.
     */
    public static DetailFragment newInstance() {
        DetailFragment fragment = new DetailFragment();
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
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.fragment_car_detail, null, false);
        initView();
        initListener();
        return mBinding.getRoot();
    }



    @Override
    public void initView() {
        data_list = new ArrayList<>();
        DBManager.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<CarsTable> carsTables = realm.where(CarsTable.class).findAll();
                carsTableList = realm.copyFromRealm(carsTables);
                for (int i = 0; i < carsTables.size(); i++) {
                    CarsTable carsTable = carsTables.get(i);
                    if (carsTable != null) {
                        String text = "无车牌";
                        if (ToolsHelper.isNull(carsTable.getLicensePlate())) {
                            if (!ToolsHelper.isNull(carsTable.getMode())) {
                                text = carsTable.getMode();
                            }
                        } else {
                            text = carsTable.getLicensePlate();
                        }
                        data_list.add(text);
                    }
                }
            }
        });
        if(carsTableList != null && carsTableList.size() > 0){
            CacheHelper.setUsualcar(carsTableList.get(0));
        }

        if(data_list.size() > 1){
            Drawable drawable = getResources().getDrawable(R.drawable.main_top_down_btn);
            drawable.setBounds(0,0,drawable.getMinimumWidth(),drawable.getMinimumHeight());
            mBinding.carLisenceNoTv.setCompoundDrawables(null,null,drawable,null);
        }
        mBinding.carLisenceNoTv.setText(data_list.get(0));

        typeface = Typeface.createFromAsset(getActivity().getAssets(), "DINCond-Bold.otf");

        //创建一个GridLayout管理器,设置为4列
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //设置GridView方向为:垂直方向
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        //添加到RecyclerView容器里面
        mBinding.carDetailOthers.setLayoutManager(layoutManager);
        //设置自动适应配置的大小
        mBinding.carDetailOthers.setHasFixedSize(true);
        //创建适配器对象
        List<String> gridlist = new ArrayList<>();
        gridlist.add("UBI车险");
        gridlist.add("爱车保养");
        gridlist.add("违章代缴");
        gridlist.add("违章what");
        gridlist.add("why");
        gridlist.add("anyway");
        CarDetailOthersAdapter adapter = new CarDetailOthersAdapter(gridlist, getActivity(), this);
        mBinding.carDetailOthers.setAdapter(adapter);

        int spacingInPixels = ToolsHelper.dpToPx(6);
        mBinding.carDetailOthers.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        mBinding.pullScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                mBinding.pullScrollView.onRefreshComplete();
                LoadingDialogUtils.showVertical(getActivity(), getString(R.string.loading_waitting));
                CarControlManager.getInstance().conditionReport();
            }
        });
    }


    @Override
    public void onItemClick(Object data, int position) {

    }

    @Override
    public void initListener() {
        mBinding.carLisenceNoLlyt.setOnClickListener(this);
        mBinding.menuIcon.setOnClickListener(this);
        mBinding.menuNotices.setOnClickListener(this);
        mBinding.carLisenceNoTv.setOnClickListener(this);
//        mBinding.carInfoConfigLeft.setOnClickListener(this);
//        mBinding.carInfoConfigRight.setOnClickListener(this);

        mBinding.carLisenceNo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                TextView tv = (TextView) view;
                tv.setTextColor(getResources().getColor(R.color.white));    //设置颜色
                tv.setTypeface(typeface);//设置字体
                if (null != carsTableList && !carsTableList.isEmpty()) {
                    CarsTable carsTable = carsTableList.get(i);
                    CacheHelper.setUsualcar(carsTable);
                    LoadingDialogUtils.showVertical(getActivity(), getString(R.string.loading_waitting));
                    CarControlManager.getInstance().conditionReport();
                    MainActivity activity = (MainActivity) getActivity();
                    activity.updateCarInfo();

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.car_lisence_no_llyt:
                mBinding.carLisenceNo.performClick();
                break;
            case R.id.menu_icon:
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
            case R.id.menu_notices:
                startActivity(new Intent(getActivity(), MessageActivity.class));
                break;
            case R.id.car_lisence_no_tv:
                if(data_list.size() > 1){
                    showPopu(data_list);
                }
                break;
            case R.id.car_info_config_left:
                enterCarInfoShowConfigActivity(0);
                break;
            case R.id.car_info_config_right:
                enterCarInfoShowConfigActivity(1);
                break;

        }
    }

    /**
     * 进入车辆信息配置页面
     * @param positon
     */
    private void enterCarInfoShowConfigActivity( int positon ) {
        Intent intent = new Intent(getActivity(), CarInfoShowConfigActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("data",dataBean);
        bundle.putInt("position",positon);
        intent.putExtras(bundle);
        startActivityForResult(intent,positon);
    }

    private void showPopu( final List<String> list) {
        View popuView = View.inflate(getActivity(),R.layout.layout_car_list,null);
        ListView listView = popuView.findViewById(R.id.list_car);
        listView.setAdapter(new MenuCarListAdapter(getActivity(),list,carsTableList));
        final PopupWindow popupWindow = new PopupWindow(popuView,
                Utils.dp2px(getContext(),150), RelativeLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setContentView(popuView);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(mBinding.carLisenceNoTv,-mBinding.carLisenceNoTv.getWidth()/2 + Utils.dp2px(getActivity(),20),0);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
                mBinding.carLisenceNoTv.setText(list.get(position));
                if (null != carsTableList && !carsTableList.isEmpty()) {
                    CarsTable carsTable = carsTableList.get(position);
                    CacheHelper.setUsualcar(carsTable);
                    LoadingDialogUtils.showVertical(getActivity(), getString(R.string.loading_waitting));
                    CarControlManager.getInstance().conditionReport();
                    MainActivity activity = (MainActivity) getActivity();
                    activity.updateCarInfo();

                }
                popupWindow.dismiss();
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        initCarInfo();
    }

    /**
     * 初始化车辆信息
     */
    private void initCarInfo() {
        String vin = CacheHelper.getVin();
        if(carsTableList != null && carsTableList.size()>0){
            for(int i = 0;i<carsTableList.size();i++){
                if(carsTableList.get(i) != null
                        && carsTableList.get(i).getVin() != null
                        && carsTableList.get(i).getVin().equals(vin)){
                    if(!TextUtils.isEmpty(carsTableList.get(i).getLicensePlate())){
                        mBinding.carLisenceNoTv.setText(carsTableList.get(i).getLicensePlate());
                    }else{
                        mBinding.carLisenceNoTv.setText(carsTableList.get(i).getMode());
                    }
                    CarControlManager.getInstance().conditionReport();

                }
            }
        }
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

    /**
     * 车辆详情 api 成功
     *
     * @param event
     *         返回数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void conditionReportSuccess(HttpRequestEvent event) {
        HttpResult httpResult = event.getResult();
        String apiNo = httpResult.getApiNo();
        if (!(HttpApiKey.conditionReport.equals(apiNo)
        ))
            return;
        LoadingDialogUtils.dissmiss();
        final ConditionReportModel model = GsonImplHelp.get().toObject(httpResult.getResult(), ConditionReportModel.class);
        if (null == model || null == model.getData()) {
            CarControlManager.getInstance().setCarDetail(null);
            updateUi(null);
        } else {
            dataBean = model.getData();
            CarControlManager.getInstance().setCarDetail(dataBean);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    dataBean.setCarAdress(getString(R.string.unknown_address));
                    try {
                        LatLng latLng = new LatLng(dataBean.getLatitude(), dataBean.getLongitude());
                        LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
                        final RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 100,
                                GeocodeSearch.GPS);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
                        GeocodeSearch geocoderSearch = new GeocodeSearch(getActivity());
                        RegeocodeAddress regeocodeAddress = geocoderSearch.getFromLocation(query);// 设置同步逆地理编码请求
                        if (null != regeocodeAddress && !ToolsHelper.isNull(regeocodeAddress.getFormatAddress())) {
                            dataBean.setCarAdress(regeocodeAddress.getFormatAddress());
                        }
                    } catch (AMapException e) {
                        e.printStackTrace();
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mBinding.mainLocationAddress.setText(dataBean.getCarAdress());
                            updateUi(dataBean);
                        }
                    });
                }
            }).start();

        }
    }

    /**
     * 设置数据
     *
     * @param data
     */
    private void updateUi(ConditionReportModel.DataBean data) {
        if (null == data) {
            mBinding.carDetailKm.setText(getString(R.string.null_string));//续航里程
            mBinding.carDetailKmHint.setVisibility(View.GONE);
            mBinding.ivElectricOil.setImageResource(R.drawable.car_detail_oil);
            mBinding.carDetailOil.setText(getString(R.string.null_string));//剩余电量，油量
            mBinding.carDetailOilHint.setVisibility(View.GONE);
            mBinding.carDetailAver.setText(getString(R.string.null_string));//平均功耗，油耗
            mBinding.carDetailAverHint.setVisibility(View.GONE);
            mBinding.carDetailTotalKm.setText(getString(R.string.null_string));//总里程
            mBinding.carDetailTotalKmHint.setVisibility(View.GONE);
            mBinding.mainLocationAddress.setText(getString(R.string.unknown_address));
            mBinding.mainUpdateTime.setText("");//更新时间

        } else {
            mBinding.mainUpdateTime.setText(new StringBuilder().append("更新时间：").
                    append(TimeHelper.getTimeByType(System.currentTimeMillis(), TimeHelper.FORMAT2)).toString());//更新时间
            mBinding.mainLocationAddress.setText(data.getCarAdress());//车位置信息
            mBinding.carDetailKmHint.setVisibility(View.VISIBLE);
            mBinding.carDetailOilHint.setVisibility(View.VISIBLE);
            mBinding.carDetailAverHint.setVisibility(View.VISIBLE);
            mBinding.carDetailTotalKmHint.setVisibility(View.VISIBLE);
            int totalMilege = data.getTotalMileage() / 10;
            mBinding.carDetailTotalKm.setText(ToolsHelper.toString(totalMilege));//总里程
            int fuelType = CacheHelper.getUsualcar().getFuelType();
            int enduranceMileage = 0;//续航里程
            double averPower = 0;//平均功耗／油耗
            switch (fuelType) {
                case 2://EV车
                    enduranceMileage = data.getEnduranceMileage();
                    mBinding.carDetailAverText.setText(getString(R.string.aver_text_ev));//平均功耗
                    mBinding.carDetailAverHint.setText(getString(R.string.aver_hint_ev));
                    mBinding.carDetailAverHint.setVisibility(View.VISIBLE);
                    if(!data.getChargingStatus().equals("3")){
                        mBinding.ivPlug.setVisibility(View.VISIBLE);
                        mBinding.carDetailOilText.setText("充电中");
                        mBinding.carDetailOilText.setTextColor(Color.parseColor("#32F4FF"));
                    }else{
                        mBinding.ivPlug.setVisibility(View.INVISIBLE);
                        mBinding.carDetailOilText.setText(getString(R.string.enerage_text_ev));
                        mBinding.carDetailOilText.setTextColor(Color.parseColor("#99ffffff"));
                    }
                    averPower = Math.abs(data.getAveragePower() / 10);
                    mBinding.ivElectricOil.setImageResource(getEvDrawableRes(data.getSoc()));
                   //剩余电量
                    mBinding.carDetailOil.setText(ToolsHelper.toString(data.getSoc()));
                    if (data.getCarLockStatus() != null && !data.getCarLockStatus().substring(0, 1).contains("1")) {
                        //车门未锁
                        mBinding.ivCar.setImageResource(R.drawable.bg_car_door_unlock);
                        mBinding.ivLocationAddress.setImageResource(R.drawable.ic_car_door_unlock);
                        mBinding.mainLocationAddress.setText("车门未关闭");
                        mBinding.mainLocationAddress.setTextColor(Color.parseColor("#FF2555"));
                    }else {
                        mBinding.ivCar.setImageResource(R.drawable.car_detail_iv);
                        mBinding.ivLocationAddress.setImageResource(R.drawable.car_detail_location);
                        mBinding.mainLocationAddress.setTextColor(Color.parseColor("#ffabb3c8"));

                    }

                    break;
                default://油车
                    mBinding.carDetailAverText.setText(getString(R.string.aver_text_oil));//平均油耗
                    mBinding.carDetailAverHint.setText(getString(R.string.aver_hint_oil));
                    mBinding.carDetailAverHint.setVisibility(View.VISIBLE);
                    mBinding.carDetailOilText.setText(getString(R.string.enerage_text_oil));//剩余油量
                    mBinding.carDetailOil.setText(ToolsHelper.toString(10));
                    mBinding.ivElectricOil.setImageResource(getOilDrawableRes(data.getRemianOil()));
                    mBinding.carDetailOilText.setTextColor(Color.parseColor("#99ffffff"));
                    mBinding.ivPlug.setVisibility(View.INVISIBLE);
                    String displayMode = data.getInstrumentDisplayMode();
                    String showType = "";
                    if (!ToolsHelper.isNull(displayMode)) {
                        if (displayMode.length() == 8) {
                            showType = displayMode.substring(7);
                        } else if (displayMode.length() == 2) {
                            showType = displayMode.substring(1);
                        }
                        if (showType.equals("0")) {
                            enduranceMileage = Math.abs(data.getTripAEnduranceMileage());
                            averPower = Math.abs(data.getTripAAvergeFuleCon() / 10);
                        } else if (showType.equals("1")) {
                            enduranceMileage = Math.abs(data.getTripBEnduranceMileage());
                            averPower = Math.abs(data.getTripBAvergeFuleCon() / 10);
                        }
                    }
                    if (data.getDoorStatus() != null && data.getDoorStatus().substring(4, 5).contains("1")) {
                        //车门未锁
                        mBinding.ivCar.setImageResource(R.drawable.bg_car_door_unlock);
                        mBinding.ivLocationAddress.setImageResource(R.drawable.ic_car_door_unlock);
                        mBinding.mainLocationAddress.setText("车门未关闭");
                        mBinding.mainLocationAddress.setTextColor(Color.parseColor("#FF2555"));
                    }else{
                        mBinding.ivCar.setImageResource(R.drawable.car_detail_iv);
                        mBinding.ivLocationAddress.setImageResource(R.drawable.car_detail_location);
                        mBinding.mainLocationAddress.setTextColor(Color.parseColor("#ffabb3c8"));
                    }

                    break;
            }
            if (enduranceMileage == 65535) {//电量/电量  不足
                enduranceMileage = 0;
            } else {
                enduranceMileage = enduranceMileage / 10;
            }
            mBinding.carDetailKm.setText(ToolsHelper.toString(220));//续航里程
            mBinding.carDetailAver.setText(ToolsHelper.toString(40));//平均功耗／油耗
        }
        //设置车辆信息显示配置
        setCarInfoShowConfig();



    }

    private int getEvDrawableRes( int soc ) {
        if(soc == 0){
            return R.drawable.bg_ev_0;
        }else if(soc > 0&&  soc <= 10){
            return R.drawable.bg_ev_10;
        }else if(soc > 10&&  soc <= 20){
            return R.drawable.bg_ev_10;
        }else if(soc > 20&&  soc <= 30){
            return R.drawable.bg_ev_20;
        }else if(soc > 30&&  soc <= 40){
            return R.drawable.bg_ev_30;
        }else if(soc > 40&&  soc <= 50){
            return R.drawable.bg_ev_40;
        }else if(soc >50 &&  soc <= 60){
            return R.drawable.bg_ev_50;
        }else if(soc > 60&&  soc <= 70){
            return R.drawable.bg_ev_60;
        }else if(soc > 70&&  soc <= 80){
            return R.drawable.bg_ev_70;
        }else if(soc > 80&&  soc <= 90){
            return R.drawable.bg_ev_80;
        }else if(soc > 90&&  soc < 100){
            return R.drawable.bg_ev_90;
        }else if(soc ==100){
            return R.drawable.bg_ev_100;
        }
        return R.drawable.bg_ev_100;
    }

    private int getOilDrawableRes( int remianOil ) {
        if(remianOil == 0){
            return R.drawable.bg_oil_0;
        }else if(remianOil > 0&&  remianOil <= 10){
            return R.drawable.bg_oil_10;
        }else if(remianOil > 10&&  remianOil <= 20){
            return R.drawable.bg_oil_10;
        }else if(remianOil > 20&&  remianOil <= 30){
            return R.drawable.bg_oil_20;
        }else if(remianOil > 30&&  remianOil <= 40){
            return R.drawable.bg_oil_30;
        }else if(remianOil > 40&&  remianOil <= 50){
            return R.drawable.bg_oil_40;
        }else if(remianOil >50 &&  remianOil <= 60){
            return R.drawable.bg_oil_50;
        }else if(remianOil > 60&&  remianOil <= 70){
            return R.drawable.bg_oil_60;
        }else if(remianOil > 70&&  remianOil <= 80){
            return R.drawable.bg_oil_70;
        }else if(remianOil > 80&&  remianOil <= 90){
            return R.drawable.bg_oil_80;
        }else if(remianOil > 90&&  remianOil < 100){
            return R.drawable.bg_oil_90;
        }else if(remianOil ==100){
            return R.drawable.bg_oil_100;
        }
        return R.drawable.bg_oil_100;
    }

    /**
     * 设置车辆信息显示配置
     */
    private void setCarInfoShowConfig() {
        String leftType = CacheHelper.getCarInfoShowConfigLeft();
        String rightType = CacheHelper.getCarInfoShowConfigRight();
        if(TextUtils.isEmpty(leftType)){
            leftType = "MILEAGE";
        }
        if(TextUtils.isEmpty(rightType)){
            rightType = "AVERAGE";
        }
        updateLeftTab(leftType);
        updateRightTab(rightType);
    }

    /**
     * 车辆详情 api 失败
     *
     * @param event
     *         返回数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void conditionReportFailed(HttpRequestErrorEvent event) {
        HttpResult httpResult = event.getResult();
        String apiNo = httpResult.getApiNo();
        if (!(HttpApiKey.conditionReport.equals(apiNo)
        ))
            return;
        LoadingDialogUtils.dissmiss();
        CarControlManager.getInstance().setCarDetail(null);
        updateUi(null);
        ToolsHelper.showHttpRequestErrorMsg(getActivity(), httpResult);
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == getActivity().RESULT_OK){
            if(requestCode == 0x02){
                initView();
            }else if(requestCode == 0){ // 左侧tab 显示车辆配置项目
                String type = data.getStringExtra("TYPE");
                CacheHelper.setCarInfoShowConfigLeft(type);
                updateLeftTab(type);
            }else if(requestCode == 1){
                String type = data.getStringExtra("TYPE");
                CacheHelper.setCarInfoShowConfigRight(type);
                updateRightTab(type);
            }


        }

    }

    /**
     * 更新右侧车辆信息显示tab
     * @param type
     */
    private void updateRightTab( String type ) {
        int fuelType = CacheHelper.getUsualcar().getFuelType();
        if(fuelType == 2){ //电车
            switch (type){
                case "MILEAGE":
                    int totalMilege = dataBean.getTotalMileage() / 10;
                    mBinding.carDetailAver.setText(ToolsHelper.toString(totalMilege));//总里程
                    mBinding.carDetailAverHint.setVisibility(View.VISIBLE);
                    mBinding.carDetailAverHint.setText("km");
                    mBinding.carDetailAverText.setText("总里程");
                    break;
                case "AVERAGE":
                    mBinding.carDetailAver.setText(ToolsHelper.toString(Math.abs(dataBean.getAveragePower() / 10)));
                    mBinding.carDetailAverHint.setVisibility(View.VISIBLE);
                    mBinding.carDetailAverHint.setText("kmh");
                    mBinding.carDetailAverText.setText("平均功耗");
                    break;
                case "INSTANTANEOUS":
                    mBinding.carDetailAver.setText(ToolsHelper.toString(Math.abs(dataBean.getInstantanePower() / 10)));
                    mBinding.carDetailAverHint.setVisibility(View.VISIBLE);
                    mBinding.carDetailAverHint.setText("kmh");
                    mBinding.carDetailAverText.setText("瞬时功耗");
                    break;
                case "AIR_CONDITIONER":
                    mBinding.carDetailAver.setText("关闭");
                    mBinding.carDetailAverHint.setVisibility(View.GONE);
                    mBinding.carDetailAverText.setText("空调状态");
                    break;

            }
        }else{ //油车
            switch (type){
                case "MILEAGE":
                    int totalMilege = dataBean.getTotalMileage() / 10;
                    mBinding.carDetailAver.setText(ToolsHelper.toString(totalMilege));//总里程
                    mBinding.carDetailAverHint.setVisibility(View.VISIBLE);
                    mBinding.carDetailAverHint.setText("km");
                    mBinding.carDetailAverText.setText("总里程");
                    break;
                case "AVERAGE":
                    mBinding.carDetailAver.setText(ToolsHelper.toString(Math.abs(dataBean.getTripAAvergeFuleCon() / 10)));
                    mBinding.carDetailAverHint.setVisibility(View.VISIBLE);
                    mBinding.carDetailAverHint.setText("km/L");
                    mBinding.carDetailAverText.setText("平均油耗");
                    break;
                case "INSTANTANEOUS":
                    mBinding.carDetailAver.setText(ToolsHelper.toString(Math.abs(dataBean.getInstantaneFuleCon() / 10)));
                    mBinding.carDetailAverHint.setVisibility(View.VISIBLE);
                    mBinding.carDetailAverHint.setText("km/L");
                    mBinding.carDetailAverText.setText("瞬时油耗");
                    break;
                case "AIR_CONDITIONER":
                    mBinding.carDetailAver.setText("关闭");
                    mBinding.carDetailAverHint.setVisibility(View.GONE);
                    mBinding.carDetailAverText.setText("空调状态");
                    break;


            }
        }


    }
    /**
     * 更新左侧车辆信息显示tab
     * @param type
     */
    private void updateLeftTab( String type ) {
        int fuelType = CacheHelper.getUsualcar().getFuelType();
        if(fuelType == 2){ //电车
            switch (type){
                case "MILEAGE":
                    int totalMilege = dataBean.getTotalMileage() / 10;
                    mBinding.carDetailTotalKm.setText(ToolsHelper.toString(totalMilege));//总里程
                    mBinding.carDetailTotalKmHint.setVisibility(View.VISIBLE);
                    mBinding.leftTabTv.setText("总里程");
                    break;
                case "AVERAGE":
                    mBinding.carDetailTotalKm.setText(ToolsHelper.toString(Math.abs(dataBean.getAveragePower() / 10)));
                    mBinding.carDetailTotalKmHint.setVisibility(View.VISIBLE);
                    mBinding.carDetailTotalKmHint.setText("kmh");
                    mBinding.leftTabTv.setText("平均功耗");
                    break;
                case "INSTANTANEOUS":
                    mBinding.carDetailTotalKm.setText(ToolsHelper.toString(Math.abs(dataBean.getInstantanePower() / 10)));
                    mBinding.carDetailTotalKmHint.setVisibility(View.VISIBLE);
                    mBinding.carDetailTotalKmHint.setText("kmh");
                    mBinding.leftTabTv.setText("瞬时功耗");
                    break;
                case "AIR_CONDITIONER":
                    mBinding.carDetailTotalKm.setText("关闭");
                    mBinding.carDetailTotalKmHint.setVisibility(View.GONE);
                    mBinding.leftTabTv.setText("空调状态");
                    break;

            }
        }else{ //油车
            switch (type){
                case "MILEAGE":
                    CacheHelper.setCarInfoShowConfigLeft("MILEAGE");
                    int totalMilege = dataBean.getTotalMileage() / 10;
                    mBinding.carDetailTotalKm.setText(ToolsHelper.toString(totalMilege));//总里程
                    mBinding.carDetailTotalKmHint.setVisibility(View.VISIBLE);
                    mBinding.leftTabTv.setText("总里程");
                    break;
                case "AVERAGE":
                    mBinding.carDetailTotalKm.setText(ToolsHelper.toString(Math.abs(dataBean.getTripAAvergeFuleCon() / 10)));
                    mBinding.carDetailTotalKmHint.setVisibility(View.VISIBLE);
                    mBinding.carDetailTotalKmHint.setText("km/L");
                    mBinding.leftTabTv.setText("平均油耗");
                    break;
                case "INSTANTANEOUS":
                    mBinding.carDetailTotalKm.setText(ToolsHelper.toString(Math.abs(dataBean.getInstantaneFuleCon() / 10)));
                    mBinding.carDetailTotalKmHint.setVisibility(View.VISIBLE);
                    mBinding.carDetailTotalKmHint.setText("km/L");
                    mBinding.leftTabTv.setText("瞬时油耗");
                    break;
                case "AIR_CONDITIONER":
                    mBinding.carDetailTotalKm.setText("关闭");
                    mBinding.carDetailTotalKmHint.setVisibility(View.GONE);
                    mBinding.leftTabTv.setText("空调状态");
                    break;

            }
        }
    }
}
