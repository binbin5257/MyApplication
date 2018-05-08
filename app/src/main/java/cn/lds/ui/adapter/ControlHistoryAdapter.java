package cn.lds.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import cn.lds.R;
import cn.lds.common.data.ControlHistoryListModel;
import cn.lds.common.data.TripListModel;
import cn.lds.common.enums.CarControlType;
import cn.lds.common.enums.TransactionsType;
import cn.lds.common.table.CarsTable;
import cn.lds.common.utils.OnItemClickListener;
import cn.lds.common.utils.TimeHelper;
import cn.lds.common.utils.ToolsHelper;
import cn.lds.ui.view.group.BaseRecyclerAdapter;
import cn.lds.ui.view.group.GroupRecyclerAdapter;
import io.realm.RealmResults;

/**
 * Created by leadingsoft on 17/12/5.
 */

public class ControlHistoryAdapter extends GroupRecyclerAdapter<String, ControlHistoryListModel.DataBean> {


    public ControlHistoryAdapter(Context context) {
        super(context);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateDefaultViewHolder(ViewGroup parent, int type) {
        ControlHistoryHolder holder = new ControlHistoryHolder(mInflater.inflate(R.layout.item_control_history_list, parent, false));
        return holder;
    }

    @Override
    protected void onBindViewHolder(RecyclerView.ViewHolder holder, ControlHistoryListModel.DataBean item, int position) {
        ControlHistoryHolder h = (ControlHistoryHolder) holder;
        h.controlTime.setText(TimeHelper.getTimeByType(item.getLastUpdateTime(), TimeHelper.FORMAT6));
        switch (item.getCommandType()){
            case LOCK:
                h.controlType.setText(CarControlType.LOCK.getValue());
                break;
            case UNLOCK:
                h.controlType.setText(CarControlType.UNLOCK.getValue());
                break;
            case FLASHLIGHTWHISTLE:
                h.controlType.setText(CarControlType.FLASHLIGHTWHISTLE.getValue());
                break;
            case AIRCONDITIONHEAT:
                h.controlType.setText(CarControlType.AIRCONDITIONHEAT.getValue());
                break;
            case AIRCONDITIONREFRIGERATE:
                h.controlType.setText(CarControlType.AIRCONDITIONREFRIGERATE.getValue());
                break;
             case AIRCONDITIONTURNOFF:
                h.controlType.setText(CarControlType.AIRCONDITIONTURNOFF.getValue());
                break;
            case ENGINESTART:
                h.controlType.setText(CarControlType.ENGINESTART.getValue());
                break;
        }
        switch (item.getCommandResult()){
            case UNKNOW:
                h.controlResult.setText(TransactionsType.UNKNOW.getValue());
                h.ivResult.setImageResource(R.drawable.bg_red_failure);
                break;
            case SUCCESS:
                h.controlResult.setText(TransactionsType.SUCCESS.getValue());
                h.ivResult.setImageResource(R.drawable.bg_green_success);
                break;
            case WAITING_SEND:
                h.controlResult.setText(TransactionsType.WAITING_SEND.getValue());
                h.ivResult.setImageResource(R.drawable.bg_red_failure);
                break;
            case SENT:
                h.controlResult.setText(TransactionsType.SENT.getValue());
                h.ivResult.setImageResource(R.drawable.bg_green_success);
                break;
            case NOT_ONLINE:
                h.controlResult.setText(TransactionsType.NOT_ONLINE.getValue());
                h.ivResult.setImageResource(R.drawable.bg_red_failure);
                break;
            case UPGRADING:
                h.controlResult.setText(TransactionsType.UPGRADING.getValue());
                h.ivResult.setImageResource(R.drawable.bg_red_failure);
                break;
            case FAIL:
                h.controlResult.setText(TransactionsType.FAIL.getValue());
                h.ivResult.setImageResource(R.drawable.bg_red_failure);
                break;
            case REQUEST_TSP_FAIL:
                h.controlResult.setText(TransactionsType.REQUEST_TSP_FAIL.getValue());
                h.ivResult.setImageResource(R.drawable.bg_red_failure);
                break;
            case PIN_ERROR:
                h.controlResult.setText(TransactionsType.PIN_ERROR.getValue());
                h.ivResult.setImageResource(R.drawable.bg_red_failure);
                break;


        }


    }

    private static class ControlHistoryHolder extends RecyclerView.ViewHolder {

        private TextView controlType;
        private TextView controlResult;
        private TextView controlTime;
        private ImageView ivResult;

        private ControlHistoryHolder(View itemView) {
            super(itemView);
            controlType = itemView.findViewById(R.id.tv_type);
            controlResult = itemView.findViewById(R.id.tv_result);
            controlTime = itemView.findViewById(R.id.tv_time);
            ivResult = itemView.findViewById(R.id.iv_resulet);
        }
    }

    public void updateAdapter(List<ControlHistoryListModel.DataBean> dataBeans) {
        LinkedHashMap<String, List<ControlHistoryListModel.DataBean>> map = new LinkedHashMap<>();
        List<String> titles = new ArrayList<>();
        for (int i = 0; i < dataBeans.size(); i++) {
            ControlHistoryListModel.DataBean dataBean = dataBeans.get(i);
            String time = TimeHelper.getTimeByType(dataBean.getLastUpdateTime(), TimeHelper.FORMAT3);
            if (map.containsKey(time)) {
                map.get(time).add(dataBean);
            } else {
                List<ControlHistoryListModel.DataBean> list = new ArrayList<>();
                list.add(dataBean);
                map.put(time, list);
                titles.add(time);
            }
        }

        resetGroups(map, titles);
    }




}