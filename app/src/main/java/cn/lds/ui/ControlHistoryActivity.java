package cn.lds.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.lds.R;
import cn.lds.common.api.HttpApiKey;
import cn.lds.common.api.ModuleUrls;
import cn.lds.common.base.BaseActivity;
import cn.lds.common.data.ControlHistoryListModel;
import cn.lds.common.data.TripListModel;
import cn.lds.common.http.HttpRequestErrorEvent;
import cn.lds.common.http.HttpRequestEvent;
import cn.lds.common.http.HttpResult;
import cn.lds.common.manager.RequestManager;
import cn.lds.common.utils.CacheHelper;
import cn.lds.common.utils.TimeHelper;
import cn.lds.common.utils.ToolsHelper;
import cn.lds.common.utils.json.GsonImplHelp;
import cn.lds.databinding.ActvityControlHistoryBinding;
import cn.lds.ui.adapter.ControlHistoryAdapter;
import cn.lds.ui.adapter.TripListAdapter;
import cn.lds.ui.view.calendar.CalendarView;
import cn.lds.ui.view.group.GroupItemDecoration;
import cn.lds.widget.dialog.LoadingDialogUtils;

/**
 * 控车操作历史界面
 * Created by sibinbin on 18-3-9.
 */

public class ControlHistoryActivity extends BaseActivity implements View.OnClickListener {

    private ActvityControlHistoryBinding mBinding;
    private int page = 0;
    //是否是最后一页 true 是； false 否
//    private boolean last = false;
    private List<ControlHistoryListModel.DataBean> controlHistoryList = new ArrayList<>();
    private ControlHistoryAdapter controlHistoryAdapter;
    private int LONG_AGO = 30;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        initView();
        initListener();
        initData();
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
     * api请求成功
     *
     * @param event
     *         成功返回
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void requestTspLogSuccess(HttpRequestEvent event) {
        HttpResult httpResult = event.getResult();
        String apiNo = httpResult.getApiNo();
        if (!(HttpApiKey.tspLog.equals(apiNo)))
            return;
        LoadingDialogUtils.dissmiss();
        ControlHistoryListModel model = GsonImplHelp.get().toObject(httpResult.getResult(), ControlHistoryListModel.class);
        if (null == model || null == model.getData() || model.getData().isEmpty()) {
            return;
        }
        //更新列表
        controlHistoryList.addAll(model.getData());
        controlHistoryAdapter.updateAdapter(controlHistoryList);
        mBinding.recyclerView.notifyDataSetChanged();
    }

    /**
     * api请求失败
     *
     * @param event
     *         失败返回
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void requestTspLogFailed(HttpRequestErrorEvent event) {
        HttpResult httpResult = event.getResult();
        String apiNo = httpResult.getApiNo();
        if (!(HttpApiKey.tspLog.equals(apiNo)))
            return;
        LoadingDialogUtils.dissmiss();
        ToolsHelper.showHttpRequestErrorMsg(mContext, httpResult);

    }

    /**
     * 初始化 请求数据
     */
    private void initData() {
        LoadingDialogUtils.showVertical(mContext, getString(R.string.loading_waitting));
        String url = ModuleUrls.tspLog.
                replace("{vin}", CacheHelper.getVin());
        RequestManager.getInstance().get(url, HttpApiKey.tspLog);

    }

    @Override
    public void initView() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.actvity_control_history);
        TextView titile = mBinding.getRoot().findViewById(R.id.top_title_tv);
        titile.setText("操作历史");
        controlHistoryAdapter = new ControlHistoryAdapter(mContext);
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        GroupItemDecoration<String, ControlHistoryListModel.DataBean> itemDecoration = new GroupItemDecoration<>(this);
        mBinding.recyclerView.addItemDecoration(itemDecoration);
        mBinding.recyclerView.setAdapter(controlHistoryAdapter);
        mBinding.calendarView.setWeeColor(
                getResources().getColor(R.color.car_detail_group_bg)
                , getResources().getColor(R.color.half_white));

        setCalendarViewRange();

    }

    /**
     * 设置日历控件显示范围
     */
    private void setCalendarViewRange() {
        long  end = System.currentTimeMillis();
        Date date = new Date(end);
        Calendar theCa = Calendar.getInstance();
        theCa.setTime(date);
        theCa.add(theCa.DATE, -LONG_AGO);//最后一个数字30可改，30天的意思
        Calendar calendar = Calendar.getInstance();
        mBinding.calendarView.setRange(theCa.get(Calendar.YEAR), theCa.get(Calendar.MONTH) + 1
                , calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
    }

    @Override
    public void initListener() {
        mBinding.getRoot().findViewById(R.id.top_back_iv).setOnClickListener(this);
        mBinding.calendarView.setOnDateSelectedListener(new CalendarView.OnDateSelectedListener() {
            @Override
            public void onDateSelected( cn.lds.ui.view.calendar.Calendar calendar, boolean isClick ) {
                if (isClick) {
                    String get = calendar.toDateString();
                    if (mBinding.calendarLayout.isExpand()) {
                        mBinding.calendarLayout.shrink();
                    }
                    int position = controlHistoryAdapter.getPositionByGroup(get);
                    mBinding.recyclerView.scrollToPosition(position);
                }
            }
        });
        mBinding.recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged( RecyclerView recyclerView, int newState ) {
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
                        String s = controlHistoryAdapter.getGroupByChildPosition(middleItemPosition);

                        if (!ToolsHelper.isNull(s)) {//选中的天
                            String[] strings = s.split("-");
                            setSeletedDay(Integer.parseInt(strings[0]), Integer.parseInt(strings[1]), Integer.parseInt(strings[2]));
                        }
                    }
                }
            }

            @Override
            public void onScrolled( RecyclerView recyclerView, int dx, int dy ) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
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


    @Override
    public void onClick( View v ) {
        switch (v.getId()) {
            case R.id.top_back_iv:
                finish();
                break;
        }
    }


}
