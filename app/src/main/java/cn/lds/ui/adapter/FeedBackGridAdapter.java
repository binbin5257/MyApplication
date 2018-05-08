package cn.lds.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import cn.lds.R;
import cn.lds.common.api.ModuleUrls;
import cn.lds.ui.view.GlideRoundTransform;

/**
 * Created by sibinbin on 18-3-21.
 */

public class FeedBackGridAdapter extends BaseAdapter {


    private Context mContext;
    private List<String> mList;
    private final LayoutInflater inflater;
    private final RequestOptions myOptions;

    public FeedBackGridAdapter( Context context,List<String> pictures){
        this.mContext = context;
        this.mList = pictures;
        inflater = LayoutInflater.from(mContext);
        myOptions = new RequestOptions()
                .transform(new GlideRoundTransform(mContext,2));
    }
    @Override
    public int getCount() {
        int count = mList == null ? 1 : mList.size() + 1;
        if (count > 9) {
            return mList.size();
        } else {
            return count;
        }
    }

    @Override
    public Object getItem( int position ) {
        return mList.get(position);
    }

    @Override
    public long getItemId( int position ) {
        return position;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent ) {
        ViewHolder holder;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.item_feed_back,null);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.icon);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        if (position < mList.size()) {
            //代表+号之前的需要正常显示图片
            String picUrl = mList.get(position); //图片路径
            holder.icon.setImageURI(picUrl);
//            Glide.with(mContext).load(picUrl).apply(myOptions).into(holder.icon);
        } else {
            holder.icon.setImageResource(R.drawable.zj);//最后一个显示加号图片
        }
        return convertView;
    }
    public class ViewHolder{
        private SimpleDraweeView icon;
    }
}
