package cn.lds.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.lds.R;
import cn.lds.common.api.HttpApiKey;
import cn.lds.common.api.ModuleUrls;
import cn.lds.common.base.BaseActivity;
import cn.lds.common.data.TripListModel;
import cn.lds.common.http.HttpRequestErrorEvent;
import cn.lds.common.http.HttpRequestEvent;
import cn.lds.common.http.HttpResult;
import cn.lds.common.manager.RequestManager;
import cn.lds.common.utils.CacheHelper;
import cn.lds.common.utils.OnItemClickListener;
import cn.lds.common.utils.TimeHelper;
import cn.lds.common.utils.ToolsHelper;
import cn.lds.common.utils.json.GsonImplHelp;
import cn.lds.databinding.ActivityTripListBinding;
import cn.lds.ui.adapter.TripListAdapter;
import cn.lds.ui.view.calendar.CalendarView;
import cn.lds.ui.view.group.BaseRecyclerAdapter;
import cn.lds.ui.view.group.GroupItemDecoration;
import cn.lds.ui.view.group.GroupRecyclerView;
import cn.lds.widget.dialog.LoadingDialogUtils;

/**
 * 行程记录列表
 * 每次获取数据时间跨度最大为7天
 * todo 垃圾数据 比较多，需要有一个比较高效的数据拉取机制
 */
public class TripListActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private ActivityTripListBinding mBinding;
    private List<TripListModel.DataBean> tripList;
    private TripListAdapter tripListAdapter;
    private Set<String> set = new HashSet<>();
    List greenDays = new ArrayList<>();
    List<Calendar> allTripDays = new ArrayList<>();
    private int LONG_AGO = 30;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_trip_list);
        initView();
        initListener();
        initData();
    }

    /**
     * 初始化 请求数据
     */
    private void initData() {
        long start, end;
        //获取三十天前日期
        end = System.currentTimeMillis();
//        end = 1512086400000L;
        Date date = new Date(end);
        Calendar theCa = Calendar.getInstance();
        theCa.setTime(date);
        theCa.add(theCa.DATE, -30);//最后一个数字30可改，30天的意思
        Date date1 = theCa.getTime();
        start = date1.getTime();
        LoadingDialogUtils.showVertical(this, "请稍候");
//        String url = ModuleUrls.trip.
//                replace("{vin}", CacheHelper.getVin()).
//                replace("{s_startTime}", ToolsHelper.toString(start))
//                .replace("{s_endTime}", ToolsHelper.toString(end));
        String url = ModuleUrls.trip.
                replace("{vin}", CacheHelper.getVin()).
                replace("{s_startTime}", 1512086400000L + "")
                .replace("{s_endTime}", 1514764800000L + "");
        RequestManager.getInstance().get(url, HttpApiKey.trip);

    }

    /**
     * 初始化view
     */
    public void initView() {
        TextView titile = mBinding.getRoot().findViewById(R.id.top_title_tv);
        titile.setText("行程历史");
        tripList = new ArrayList<>();
        tripListAdapter = new TripListAdapter(mContext);
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mBinding.recyclerView.addItemDecoration(new GroupItemDecoration<String, TripListModel.DataBean>(this));
        mBinding.recyclerView.setAdapter(tripListAdapter);


        mBinding.calendarView.setOnDateSelectedListener(new CalendarView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(cn.lds.ui.view.calendar.Calendar calendar, boolean isClick) {
                if (isClick) {
                    String get = calendar.toDateString();
                    boolean isSelectGreen = true; //选中的是否是绿色出行
                    if(set != null && set.size() != 0 ){
                        for(String noGreenTrip : set){
                            if(get.equals(noGreenTrip)){
                                isSelectGreen = false;
                                if (mBinding.calendarLayout.isExpand()) {
                                    mBinding.calendarLayout.shrink();
                                }

                                int position = tripListAdapter.getPositionByGroup(get);
                                mBinding.recyclerView.scrollToPosition(position);
                            }
                        }

                    }
                    if(Integer.parseInt(TimeHelper.getTimeByType(System.currentTimeMillis(),TimeHelper.FORMAT5)) < Integer.parseInt(calendar.toString())){
                        ToolsHelper.showInfo(mContext, get + "大于当前日期，未产生行程历史");
                    }else if(Integer.parseInt(TimeHelper.getTimeByType(System.currentTimeMillis() - (LONG_AGO * 24* 60 * 60 * 1000L),TimeHelper.FORMAT5)) >= Integer.parseInt(calendar.toString())){
                        ToolsHelper.showInfo(mContext, "行程只保存当前日期" + LONG_AGO + "天内历史记录");
                    }else if(isSelectGreen){
                        ToolsHelper.showInfo(mContext, get + "为绿色出行");
                    }


                }
            }
        });
        mBinding.calendarView.setWeeColor(
                getResources().getColor(R.color.car_detail_group_bg)
                , getResources().getColor(R.color.half_white));

        long  end = System.currentTimeMillis();
        Date date = new Date(end);
        Calendar theCa = Calendar.getInstance();
        theCa.setTime(date);
        theCa.add(theCa.DATE, -LONG_AGO);//最后一个数字30可改，30天的意思
        Calendar calendar = Calendar.getInstance();
        mBinding.calendarView.setRange(theCa.get(Calendar.YEAR), theCa.get(Calendar.MONTH) + 1
                , calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);

        for(int i = 0; i < LONG_AGO;i ++){
            Calendar instance = Calendar.getInstance();
            instance.setTime(date);
            instance.add(instance.DATE,-i);
            allTripDays.add(instance);

        }
        Log.e("","");

    }

    /**
     * 初始化点击事件
     */


    public void initListener() {
        mBinding.getRoot().findViewById(R.id.top_back_iv).setOnClickListener(this);
        mBinding.recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (RecyclerView.SCROLL_STATE_IDLE == newState) {//滚动结束
                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    //判断是当前layoutManager是否为LinearLayoutManager
                    // 只有LinearLayoutManager才有查找第一个和最后一个可见view位置的方法
                    if (layoutManager instanceof LinearLayoutManager) {
                        LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                        //获取最后一个可见view的位置
                        int lastItemPosition = linearManager.findLastVisibleItemPosition();
                        //获取第一个可见view的位置
                        int firstItemPosition = linearManager.findFirstVisibleItemPosition();
                        int middleItemPosition = (firstItemPosition + lastItemPosition) / 2;
                        String s = tripListAdapter.getGroupByChildPosition(middleItemPosition);

                        if (!ToolsHelper.isNull(s)) {//选中的天
                            String[] strings = s.split("-");
                            setSeletedDay(Integer.parseInt(strings[0]), Integer.parseInt(strings[1]), Integer.parseInt(strings[2]));
                        }
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        tripListAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick( int position, long itemId ) {
                TripListModel.DataBean dataBean = tripList.get(position);
                Intent intent = new Intent(mContext, TripDetailActivity.class);
                intent.putExtra("uuid", dataBean.getUuid());
                startActivity(intent);
            }
        });

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
        }
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
     * 行程里表api请求成功
     *
     * @param event
     *         成功返回
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void requestTripSuccess(HttpRequestEvent event) {
        HttpResult httpResult = event.getResult();
        String apiNo = httpResult.getApiNo();
        if (!(HttpApiKey.trip.equals(apiNo)))
            return;
        LoadingDialogUtils.dissmiss();
        TripListModel model = GsonImplHelp.get().toObject(httpResult.getResult(), TripListModel.class);
        if (null != model && null != model.getData()) {
            if(model.getData().isEmpty()){
                greenDays.clear();
                for(Calendar tripDay:allTripDays){
                    greenDays.add(setGreenTripDate(tripDay.get(Calendar.YEAR), tripDay.get(Calendar.MONTH)+1, tripDay.get(Calendar.DAY_OF_MONTH), 0xFF40db25, ""));
                }
                mBinding.calendarView.setSchemeDate(greenDays);
                return;
            }

        }
        //更新列表
        tripList.addAll(model.getData());
        tripListAdapter.updateAdapter(tripList);
        mBinding.recyclerView.notifyDataSetChanged();
        //绘制绿色出行
        drawaGreenTrip(model.getData());
    }

    private void drawaGreenTrip( List<TripListModel.DataBean> datas ) {
        set.clear();
        for(TripListModel.DataBean dataBean : datas){
            String time = TimeHelper.getTimeByType(dataBean.getCreateTime(), TimeHelper.FORMAT3);
            set.add(time);
        }
        greenDays.clear();
        for(String noGreenDate : set){
            Calendar calendar = TimeHelper.getCalendarByString(noGreenDate, TimeHelper.FORMAT3);
            setSeletedDay(calendar.get(calendar.YEAR), calendar.get(calendar.MONTH)+1, calendar.get(calendar.DAY_OF_MONTH));
            for(Calendar tripDay:allTripDays){
                if(calendar.get(Calendar.DAY_OF_YEAR) != tripDay.get(Calendar.DAY_OF_YEAR)){
                    greenDays.add(setGreenTripDate(tripDay.get(Calendar.YEAR), tripDay.get(Calendar.MONTH)+1, tripDay.get(Calendar.DAY_OF_MONTH), 0xFF40db25, ""));
                }
            }

        }
        mBinding.calendarView.setSchemeDate(greenDays);

    }

    /**
     * 设置选中日期
     * @param year
     * @param month
     * @param day
     */
    private void setSeletedDay( int year, int month, int day ) {
        cn.lds.ui.view.calendar.Calendar calendars = new cn.lds.ui.view.calendar.Calendar();
        calendars.setYear(year);
        calendars.setMonth(month);
        calendars.setDay(day);
        mBinding.calendarView.selectDate(calendars);
    }

    /**
     * 标记绿色出行
     */
    private cn.lds.ui.view.calendar.Calendar setGreenTripDate(int year, int month, int day, int color, String text) {
        cn.lds.ui.view.calendar.Calendar calendar = new cn.lds.ui.view.calendar.Calendar();
        calendar.setYear(year);
        calendar.setMonth(month);
        calendar.setDay(day);
        calendar.setSchemeColor(color);//如果单独标记颜色、则会使用这个颜色
        calendar.setScheme(text);
        return calendar;
    }

    /**
     * 行程里表api请求失败
     *
     * @param event
     *         失败返回
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void requestTripFailed(HttpRequestErrorEvent event) {
        HttpResult httpResult = event.getResult();
        String apiNo = httpResult.getApiNo();
        if (!(HttpApiKey.trip.equals(apiNo)))
            return;
        LoadingDialogUtils.dissmiss();
        ToolsHelper.showHttpRequestErrorMsg(mContext, httpResult);
    }

    /**
     * 列表点击事件
     * 点击进入行驶轨迹
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(mContext, TripDetailActivity.class);
        intent.putExtra("uuid", tripList.get(i).getUuid());
        startActivity(intent);
    }





}
