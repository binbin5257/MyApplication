package cn.lds.ui;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
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
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.lds.R;
import cn.lds.common.api.HttpApiKey;
import cn.lds.common.api.ModuleUrls;
import cn.lds.common.base.BaseActivity;
import cn.lds.common.data.CollectionsModel;
import cn.lds.common.data.DealerListModel;
import cn.lds.common.data.postbody.PoiPostBean;
import cn.lds.common.http.HttpRequestErrorEvent;
import cn.lds.common.http.HttpRequestEvent;
import cn.lds.common.http.HttpResult;
import cn.lds.common.manager.CarControlManager;
import cn.lds.common.manager.RequestManager;
import cn.lds.common.utils.CacheHelper;
import cn.lds.common.utils.LogHelper;
import cn.lds.common.utils.OnItemClickListener;
import cn.lds.common.utils.ToolsHelper;
import cn.lds.common.utils.json.GsonImplHelp;
import cn.lds.databinding.ActivityDealerListBinding;
import cn.lds.ui.adapter.DealerListAdapter;
import cn.lds.widget.ToastUtil;
import cn.lds.widget.dialog.LoadingDialogUtils;

/**
 * 经销商列表
 */
public class DealerListActivity extends BaseActivity implements View.OnClickListener, OnItemClickListener, LocationSource, AMapLocationListener {
    ActivityDealerListBinding mBinding;
    DealerListAdapter listAdapter;
    private AMap mAmap;
    // 我的位置监听器
    private OnLocationChangedListener mLocationChangeListener = null;
    // 声明AMapLocationClient类对象
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private AMapLocation mMyLocationPoint;
    private DealerListModel.DataBean bean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_dealer_list);
        mBinding.mapMap.onCreate(savedInstanceState);
        initView();
        initMap();
        initListener();
        initData();
    }

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
    }

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
    }

    /**
     * 初始化 请求数据
     */
    private void initData() {
        LoadingDialogUtils.showVertical(mContext, "请稍候");
        String url = ModuleUrls.dealer.
                replace("{vin}", CacheHelper.getVin())
                .replace("{latitude}", "23.119089")
                .replace("{longitude}", "114.499328");
//                .replace("{latitude}", CacheHelper.getLatitude())
//                .replace("{longitude}", CacheHelper.getLongitude());
        RequestManager.getInstance().get(url, HttpApiKey.dealer);
    }

    /**
     * 初始化view
     */
    @Override
    public void initView() {
        TextView titile = mBinding.getRoot().findViewById(R.id.top_title_tv);
        titile.setText("经销商");
        listAdapter = new DealerListAdapter(new ArrayList<DealerListModel.DataBean>(), mContext, this);
        mBinding.dealerListView.setAdapter(listAdapter);
        mBinding.dealerListView.setLayoutManager(new LinearLayoutManager(mContext));
    }

    /**
     * 初始化点击事件
     */
    @Override
    public void initListener() {
        mBinding.getRoot().findViewById(R.id.top_back_iv).setOnClickListener(this);
        mBinding.poiLocatedPostPoi.setOnClickListener(this);
        mBinding.poiLocatedCollectLlyt.setOnClickListener(this);
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
        switch (id) {
            case R.id.top_back_iv:
                finish();
                break;
            case R.id.poi_located_post_poi:
                postPoi();
                break;
            case R.id.poi_located_subscribe_llyt:
                toggleSubscriber();
            case R.id.poi_located_collect_llyt:
                toggleCollection();
        }
    }

    /**
     * 收藏/取消收藏
     */
    private void toggleCollection() {
        if (bean.isCollected() && !TextUtils.isEmpty(bean.getCollectionId())) {
            //取消收藏
            CarControlManager.getInstance().deleCollection(bean.getCollectionId());
        } else {
            //增加收藏
            CollectionsModel.DataBean dataBean = new CollectionsModel.DataBean();
            dataBean.setTel(bean.getDealerPhone());
            dataBean.setName(bean.getDealerName());
            dataBean.setLongitude(bean.getLongitude());
            dataBean.setLatitude(bean.getLatitude());
            dataBean.setDesc(bean.getDealerCode());
            dataBean.setAddress(bean.getAddress());
            CarControlManager.getInstance().addCollection(dataBean, null);
        }
    }

    /**
     * 订阅/取消订阅
     */
    private void toggleSubscriber() {
        String json = GsonImplHelp.get().toJson(bean);
        LoadingDialogUtils.showVertical(this,getString(R.string.loading_waitting));
        if(bean.isSubscriberDealer()){
            String actionUrl = ModuleUrls.cancelSubscribe.replace("{vin}",CacheHelper.getVin());
            RequestManager.getInstance().post(actionUrl,HttpApiKey.cancelSubscribe,json);

        }else{
            String actionUrl = ModuleUrls.subscribe.replace("{vin}",CacheHelper.getVin());
            RequestManager.getInstance().post(actionUrl,HttpApiKey.subscribe,json);
        }
    }

    /**
     * 下发poi
     */
    private void postPoi() {
        LoadingDialogUtils.showVertical(this, getString(R.string.loading_waitting));
        String url = ModuleUrls.postPoi.replace("{vin}", CacheHelper.getVin());
        PoiPostBean postPoi = new PoiPostBean();
        PoiPostBean.PoiNodeBean poiNodeBean = new PoiPostBean.PoiNodeBean();
        poiNodeBean.setDestinations(bean.getDealerCode());
        poiNodeBean.setLatitude(bean.getLatitude());
        poiNodeBean.setLongitude(bean.getLongitude());
        postPoi.setPoiNode(poiNodeBean);
        String json = GsonImplHelp.get().toJson(postPoi);
        RequestManager.getInstance().post(url, HttpApiKey.postPoi, json);
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
     * 请求成功
     *
     * @param event
     *         成功返回
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void requestDealerSuccess(HttpRequestEvent event) {
        HttpResult httpResult = event.getResult();
        String apiNo = httpResult.getApiNo();
        if (!(HttpApiKey.dealer.equals(apiNo)
                || HttpApiKey.cancelSubscribe.equals(apiNo)
                || HttpApiKey.subscribe.equals(apiNo)
                || HttpApiKey.postPoi.equals(apiNo)
                || HttpApiKey.addCollections.equals(apiNo)
                || HttpApiKey.deleteCollections.equals(apiNo)
         ))
            return;
        LoadingDialogUtils.dissmiss();
        switch (apiNo){
            case HttpApiKey.addCollections:
                processAddCollection(httpResult);
                break;
            case HttpApiKey.deleteCollections:
                ToastUtil.showToast(this,"取消收藏成功");
                break;
            case HttpApiKey.dealer:
                processDealerList(httpResult);
                break;
            case HttpApiKey.subscribe:
                subscribeSuccess(httpResult);
                break;
            case HttpApiKey.cancelSubscribe:
                cancelSubscribeSuccess(httpResult);
                break;

        }

    }

    private void processAddCollection( HttpResult httpResult ) {
        DealerListModel.DataBean dataBean = GsonImplHelp.get().toObject(httpResult.getResult(), DealerListModel.DataBean.class);
        bean.setCollectionId(dataBean.getCollectionId());
        ToastUtil.showToast(this,"收藏成功");
        mBinding.mapSearchCollect.performClick();
        mBinding.mapSearchCollectText.setText("已订阅");
        mBinding.mapSearchCollectText.setTextColor(getResources().getColor(R.color.white));
        mBinding.mapSearchCollect.setChecked(true);
    }

    /**
     * 经销商取消订阅成功
     * @param httpResult
     */
    private void cancelSubscribeSuccess( HttpResult httpResult ) {
        ToastUtil.showToast(this,"取消订阅成功");
        bean.setSubscriberDealer(false);
        mBinding.mapSearchSubscribeText.setText("待订阅");
        mBinding.mapSearchSubscribeText.setTextColor(Color.parseColor("#1affffff"));
        mBinding.mapSearchSubscribe.performClick();
        mBinding.mapSearchSubscribe.setChecked(false);
    }

    /**
     * 经销商订阅成功
     * @param httpResult
     */
    private void subscribeSuccess( HttpResult httpResult ) {
        ToastUtil.showToast(this,"订阅成功");
        bean.setSubscriberDealer(true);
        mBinding.mapSearchSubscribe.performClick();
        mBinding.mapSearchSubscribeText.setText("已订阅");
        mBinding.mapSearchSubscribeText.setTextColor(getResources().getColor(R.color.white));
        mBinding.mapSearchSubscribe.setChecked(true);
    }

    /**
     *
     * 解析经销商列表
     * @param httpResult
     */
    private void processDealerList( HttpResult httpResult ) {
        DealerListModel model = GsonImplHelp.get().toObject(httpResult.getResult(), DealerListModel.class);
        if (null == model || null == model.getData() || model.getData().isEmpty())
            return;
        List<DealerListModel.DataBean> list = model.getData();
        for(DealerListModel.DataBean d : list) {
            drawaRedDots(d.getDealerCode(),new LatLng(d.getLatitude(),d.getLongitude()));
        }

        listAdapter.updateAdapter(list);
    }

    /**
     * 在地图上绘制红色marker
     */
    private void drawaRedDots(String dealerCode,LatLng latLng) {
        Marker marker = mAmap.addMarker(
                new MarkerOptions().icon(
                        BitmapDescriptorFactory
                                .fromResource(R.drawable.navi_poiitem_icon))
                                .position(latLng));
        marker.setObject(dealerCode);//点击mark 用来做标记，快速找到对应的poitem对象
    }

    /**
     * 请求失败
     *
     * @param event
     *         失败返回
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void requestDealerFailed(HttpRequestErrorEvent event) {
        HttpResult httpResult = event.getResult();
        String apiNo = httpResult.getApiNo();
        if (!(HttpApiKey.dealer.equals(apiNo))
                || HttpApiKey.cancelSubscribe.equals(apiNo)
                || HttpApiKey.subscribe.equals(apiNo)
                || HttpApiKey.postPoi.equals(apiNo))
            return;
        LoadingDialogUtils.dissmiss();
        ToolsHelper.showHttpRequestErrorMsg(mContext, httpResult);
    }

    @Override
    public void onItemClick(Object data, int position) {
        bean = (DealerListModel.DataBean) data;
        mBinding.dealerListView.setVisibility(View.GONE);
        mBinding.singlePoiLlyt.setVisibility(View.VISIBLE);
        mBinding.poiName.setText(bean.getDealerName());
        mBinding.poiAddress.setText(bean.getAddress());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("距您");
        if (bean.getDistance() >= 1000) {
            stringBuilder.append(bean.getDistance() / 1000).append("km");
        } else {
            stringBuilder.append(bean.getDistance()).append("m");
        }
        mBinding.poiKm.setText(stringBuilder.toString());

        if (!bean.isSubscriberDealer()) {//待收藏
            mBinding.mapSearchSubscribe.setChecked(false);
            mBinding.mapSearchSubscribeText.setText("待订阅");
            mBinding.mapSearchSubscribeText.setTextColor(Color.parseColor("#1affffff"));
        } else {//已收藏
            mBinding.mapSearchSubscribe.setChecked(true);
            mBinding.mapSearchSubscribeText.setText("已订阅");
            mBinding.mapSearchSubscribeText.setTextColor(getResources().getColor(R.color.white));

        }
        ToolsHelper.showInfo(mContext, bean.getAddress());
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

    private void initLocation() {
        mLocationClient = new AMapLocationClient(this);
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

    @Override
    public void onLocationChanged( AMapLocation amapLocation ) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                if (null == mMyLocationPoint) {//第一次定位
                    LatLng latLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                    mAmap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
                mMyLocationPoint = amapLocation;
                mLocationChangeListener.onLocationChanged(mMyLocationPoint);
                CacheHelper.setLatitude(amapLocation.getLatitude());
                CacheHelper.setLongitude(amapLocation.getLongitude());
                CacheHelper.setCity(amapLocation.getCity());


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
}
