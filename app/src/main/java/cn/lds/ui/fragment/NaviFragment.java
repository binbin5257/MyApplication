package cn.lds.ui.fragment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.lds.R;
import cn.lds.common.api.HttpApiKey;
import cn.lds.common.base.BaseFragment;
import cn.lds.common.base.UIInitListener;
import cn.lds.common.data.CollectionsModel;
import cn.lds.common.data.ConditionReportModel;
import cn.lds.common.data.HomeAndCompanyModel;
import cn.lds.common.data.ConditionReportPositionModel;
import cn.lds.common.http.HttpRequestErrorEvent;
import cn.lds.common.http.HttpRequestEvent;
import cn.lds.common.http.HttpResult;
import cn.lds.common.manager.CarControlManager;
import cn.lds.common.utils.CacheHelper;
import cn.lds.common.utils.LogHelper;
import cn.lds.common.utils.ToolsHelper;
import cn.lds.common.utils.json.GsonImplHelp;
import cn.lds.databinding.FragmentCarNaviBinding;
import cn.lds.ui.CarLocationActivity;
import cn.lds.ui.CollectionsActivity;
import cn.lds.ui.DealerListActivity;
import cn.lds.ui.MapSearchActivity;
import cn.lds.ui.MessageActivity;
import cn.lds.ui.SettingActivity;
import cn.lds.widget.dialog.LoadingDialogUtils;


/**
 * 导航界面
 */
public class NaviFragment extends BaseFragment implements UIInitListener, AMapLocationListener, LocationSource, View.OnClickListener {

    private FragmentCarNaviBinding mBinding;
    private AMap mAmap;
    // 声明AMapLocationClient类对象
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    // 声明定位回调监听器
    public AMapLocationListener mLocationListener;
    private AMapLocation mMyLocationPoint;
    // 我的位置监听器
    private OnLocationChangedListener mLocationChangeListener = null;
    private ImageView notices;
    private ImageView top_icon;
    private Marker carMarker;
    private int GET_SEARCH_DATA = 1001;

    public NaviFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DetailFragment.
     */
    public static NaviFragment newInstance() {
        NaviFragment fragment = new NaviFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.fragment_car_navi, null, false);
        mBinding.mapMap.onCreate(savedInstanceState);
        initView();
        initListener();
        initMap();
        return mBinding.getRoot();
    }


    /**
     * 初始化 家和公司地址
     */
    private void initHomeAndCompany() {
        CarControlManager.getInstance().getHomeAndCompany();
    }



    @Override
    public void initView() {
        TextView topTitle = mBinding.getRoot().findViewById(R.id.top_title_tv);
        topTitle.setText("地图导航");
        top_icon = mBinding.getRoot().findViewById(R.id.top_back_iv);
        top_icon.setImageResource(R.drawable.main_top_icon);
        top_icon.setVisibility(View.INVISIBLE);

        notices = mBinding.getRoot().findViewById(R.id.top_menu_iv);
        notices.setImageResource(R.drawable.main_top_notices);
    }

    /**
     * 设置监听
     */
    @Override
    public void initListener() {
        mBinding.collect.setOnClickListener(this);
        mBinding.mapSearch.setOnClickListener(this);
        mBinding.home.setOnClickListener(this);
        mBinding.company.setOnClickListener(this);
        mBinding.location.setOnClickListener(this);
        mBinding.ivCar.setOnClickListener(this);
        mBinding.distributor.setOnClickListener(this);

        top_icon.setOnClickListener(this);
        notices.setOnClickListener(this);
    }


    /**
     * 初始化地图
     */
    private void initMap() {
        if (mAmap == null) {
            mAmap = mBinding.mapMap.getMap();
            initMyLocation();
        }
        mAmap.setLocationSource(this);// 设置定位监听
        mAmap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        mAmap.getUiSettings().setMyLocationButtonEnabled(false); // 是否显示默认的定位按钮
        mAmap.getUiSettings().setScaleControlsEnabled(true);
        mAmap.setMyLocationEnabled(true);// 是否可触发定位并显示定位层
        mAmap.setTrafficEnabled(true);// 显示实时交通状况
        // 自定义系统定位蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(
                BitmapDescriptorFactory.fromResource(R.drawable.navi_me_location));
        myLocationStyle.strokeColor(getResources().getColor(R.color.map_stroke_color));//设置定位蓝点精度圆圈的边框颜色的方法。
        myLocationStyle.radiusFillColor(getResources().getColor(R.color.map_stroke_color));//设置定位蓝点精度圆圈的填充颜色的方法。
//        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);

        mAmap.setMyLocationStyle(myLocationStyle);
        if (null == carMarker) {
            ConditionReportModel.DataBean c = CarControlManager.getInstance().getCarDetail();
            if (null != c) {
                carMarker = mAmap.addMarker(
                        new MarkerOptions().icon(
                                BitmapDescriptorFactory.fromResource(R.drawable.navi_car_location)).position(
                                new LatLng(c.getLatitude(), c.getLongitude())));
            }
        }
    }

    /**
     * 初始化定位服务
     */
    private void initLocation() {
        mLocationClient = new AMapLocationClient(getActivity());
        mLocationClient.setLocationListener(this);
        // 初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        // 设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        // 设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        // 设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        // 设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        // 设置定位间隔,单位毫秒,默认为30s
        mLocationOption.setInterval(30000);
        // 给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        // 启动定位
        mLocationClient.startLocation();
    }

    /**
     * 初始化我的定位
     */
    private void initMyLocation() {
        mAmap.setLocationSource(this);
        mAmap.getUiSettings().setMyLocationButtonEnabled(true);
        mAmap.setMyLocationEnabled(true);
        mAmap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        mAmap.getUiSettings().setLogoPosition(
                AMapOptions.LOGO_POSITION_BOTTOM_LEFT);// logo位置
        mAmap.getUiSettings().setLogoBottomMargin(-100);//隐藏logo
        mAmap.getUiSettings().setScaleControlsEnabled(true);// 标尺开关
        mAmap.getUiSettings().setZoomControlsEnabled(false);
        mAmap.getUiSettings().setCompassEnabled(false);// 指南针开关
        LogHelper.d(
                "max = " + mAmap.getMaxZoomLevel() + "min = "
                        + mAmap.getMinZoomLevel());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        mBinding.mapMap.onPause();
        if (mLocationClient != null) {
            mLocationClient.stopLocation();//ֹͣ��λ
        }
        deactivate();
    }

    @Override
    public void onResume() {
        super.onResume();
        mBinding.mapMap.onResume();
        if (mLocationClient != null) {
            mLocationClient.startLocation();
        }
        mAmap.moveCamera(CameraUpdateFactory.zoomTo(14));
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onSaveInstanceState(Bundle arg0) {
        super.onSaveInstanceState(arg0);
        mBinding.mapMap.onSaveInstanceState(arg0);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mLocationClient != null) {
            mLocationClient.onDestroy();
        }
        mBinding.mapMap.onDestroy();
    }

    @Override
    public void deactivate() {
        mLocationChangeListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
            mLocationClient = null;
        }
    }

    @Override
    public void activate(LocationSource.OnLocationChangedListener listener) {
        mLocationChangeListener = listener;
        if (mLocationClient == null) {
            initLocation();
        }
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                // 定位成功回调信息，设置相关消息
//                amapLocation.getLocationType();// 获取当前定位结果来源，如网络定位结果，详见定位类型表
//                amapLocation.getLatitude();// 获取经度
//                amapLocation.getLongitude();// 获取纬度
//                amapLocation.getAccuracy();// 获取精度信息
//                SimpleDateFormat df = new SimpleDateFormat(
//                        "yyyy-MM-dd HH:mm:ss");
//                Date date = new Date(amapLocation.getTime());
//                df.format(date);// 定位时间
//                amapLocation.getAddress();// 地址，如果option中设置isNeedAddress为false，则没有此结果
//                amapLocation.getCountry();// 国家信息
//                amapLocation.getProvince();// 省信息
//                amapLocation.getCity();// 城市信息
//                amapLocation.getDistrict();// 城区信息
//                amapLocation.getRoad();// 街道信息
                amapLocation.getCityCode();// 城市编码
                amapLocation.getAdCode();// 地区编码

                if (null == mMyLocationPoint) {//第一次定位
                    LatLng latLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                    mAmap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
                mMyLocationPoint = amapLocation;
                mLocationChangeListener.onLocationChanged(mMyLocationPoint);
                CacheHelper.setLatitude(amapLocation.getLatitude());
                CacheHelper.setLongitude(amapLocation.getLongitude());
                CacheHelper.setCity(amapLocation.getCity());
                CacheHelper.setCityAdCode(amapLocation.getAdCode());
                CarControlManager.getInstance().conditionReportPosition();//30s 获取一次车的位置
            } else {
                // 显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                LogHelper.d(
                        "location Error, ErrCode:"
                                + amapLocation.getErrorCode() + ", errInfo:"
                                + amapLocation.getErrorInfo());
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_SEARCH_DATA) {
//            PoiItem item = data.getParcelableExtra("SEARCH_POINT");
//            if (item != null) {
//                double latitude = item.getLatitude();
//                item.getLongitude();
//            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.map_search:
                startActivityForResult(new Intent(getActivity(), MapSearchActivity.class), GET_SEARCH_DATA);
                break;
            case R.id.collect:
                startActivity(new Intent(getActivity(), CollectionsActivity.class));
                break;
            case R.id.home:
                CollectionsModel.DataBean home = CarControlManager.getInstance().getHome();
                if (null == home) {
                    ToolsHelper.showInfo(getActivity(), "未设置家地址");
                } else {
                    LoadingDialogUtils.showVertical(getActivity(), getString(R.string.loading_waitting));
                    CarControlManager.getInstance().postPoi(CarControlManager.getInstance().getHome());
                }
                break;
            case R.id.company:
                CollectionsModel.DataBean company = CarControlManager.getInstance().getCompany();
                if (null == company) {
                    ToolsHelper.showInfo(getActivity(), "未设置公司地址");
                } else {
                    LoadingDialogUtils.showVertical(getActivity(), getString(R.string.loading_waitting));
                    CarControlManager.getInstance().postPoi(CarControlManager.getInstance().getCompany());
                }
                break;
            case R.id.location:
                LatLng latLng = new LatLng(mMyLocationPoint.getLatitude(), mMyLocationPoint.getLongitude());
                mAmap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                break;
            case R.id.iv_car:
                enterCarLocationActivity();
//                if (null != carMarker) {
//                    mAmap.animateCamera(CameraUpdateFactory.newLatLng(carMarker.getPosition()));
//                }
                break;
            case R.id.distributor:
                startActivity(new Intent(getActivity(), DealerListActivity.class));
                break;
            case R.id.top_back_iv:
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
            case R.id.top_menu_iv:
                startActivity(new Intent(getActivity(), MessageActivity.class));
                break;
        }
    }

    /**
     * 进入车辆位置页面
     */
    private void enterCarLocationActivity() {
        Intent intent = new Intent(getActivity(), CarLocationActivity.class);
        Bundle bundle = new Bundle();

        startActivity(intent);

    }


    /**
     * 获取家和公司地址 api 成功
     *
     * @param event
     *         返回数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void requestHomeAndCompanySuccess(HttpRequestEvent event) {
        HttpResult httpResult = event.getResult();
        String apiNo = httpResult.getApiNo();
        if (!(HttpApiKey.getHomeAndCompany.equals(apiNo)
        ))
            return;
        HomeAndCompanyModel model = GsonImplHelp.get().toObject(httpResult.getResult(), HomeAndCompanyModel.class);
        if (null == model || null == model.getData()) {
            CarControlManager.getInstance().setCompany(null);
            CarControlManager.getInstance().setHome(null);
        } else {
            CarControlManager.getInstance().setCompany(model.getData().getCompany());
            CarControlManager.getInstance().setHome(model.getData().getHome());
        }
    }

    /**
     * 获取家和公司地址 api 失败
     *
     * @param event
     *         返回数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void requestHomeAndCompanyFailed(HttpRequestErrorEvent event) {
        HttpResult httpResult = event.getResult();
        String apiNo = httpResult.getApiNo();
        if (!(HttpApiKey.getHomeAndCompany.equals(apiNo)
        ))
            return;
        CarControlManager.getInstance().setCompany(null);
        CarControlManager.getInstance().setHome(null);
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            EventBus.getDefault().register(this);
            initHomeAndCompany();
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
     * 获取车辆位置信息
     *
     * @param event
     *         成功返回
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getCarPosition(HttpRequestEvent event) {
        HttpResult httpResult = event.getResult();
        String apiNo = httpResult.getApiNo();
        if (!(HttpApiKey.conditionReportPosition.equals(apiNo)
        ))
            return;
        ConditionReportPositionModel model = GsonImplHelp.get().toObject(httpResult.getResult(), ConditionReportPositionModel.class);
        if (null == model || null == model.getData()) {
            return;
        }
        ConditionReportPositionModel.DataBean dataBean = model.getData();
        if (null == carMarker) {
            carMarker = mAmap.addMarker(
                    new MarkerOptions().icon(
                            BitmapDescriptorFactory.fromResource(R.drawable.navi_car_location)).position(
                            new LatLng(dataBean.getLatitude(), dataBean.getLongitude())));
        } else {
            carMarker.setPosition(new LatLng(dataBean.getLatitude(), dataBean.getLongitude()));
        }
    }

    /**
     * Collections api请求成功
     *
     * @param event
     *         成功返回
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getCollectionsSuccess(HttpRequestEvent event) {
        HttpResult httpResult = event.getResult();
        String apiNo = httpResult.getApiNo();
        if (!(HttpApiKey.postPoi.equals(apiNo)
        ))
            return;
        LoadingDialogUtils.dissmiss();
        ToolsHelper.showInfo(getActivity(), "poi下发成功");
    }

    /**
     * Collections api请求失败
     *
     * @param event
     *         失败返回
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void CollectionsFailed(HttpRequestErrorEvent event) {
        HttpResult httpResult = event.getResult();
        String apiNo = httpResult.getApiNo();
        if (!(HttpApiKey.postPoi.equals(apiNo)
        ))
            return;
        LoadingDialogUtils.dissmiss();
        ToolsHelper.showHttpRequestErrorMsg(getActivity(), httpResult);
    }
}
