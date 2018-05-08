package cn.lds.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.lds.R;
import cn.lds.amap.util.ToastUtil;
import cn.lds.common.base.BaseFragment;
import cn.lds.common.base.UIInitListener;
import cn.lds.common.utils.CacheHelper;
import cn.lds.databinding.FragmentCarServiceBinding;
import cn.lds.ui.MessageActivity;
import cn.lds.ui.SettingActivity;
import cn.lds.ui.WebviewActivity;
import cn.lds.ui.view.banner.MZBannerView;
import cn.lds.ui.view.banner.MZHolderCreator;
import cn.lds.ui.view.banner.MZViewHolder;


/**
 * 服务界面
 */
public class ServiceFragment extends BaseFragment implements UIInitListener, View.OnClickListener {
    private FragmentCarServiceBinding mBinding;
    private ImageView main_logo, msg_notice;
    private static final int[] BANNER = new int[]{
            R.drawable.car_detail_goup_bg
            , R.drawable.car_detail_goup_bg
            , R.drawable.car_detail_goup_bg
            , R.drawable.car_detail_goup_bg
            , R.drawable.car_detail_goup_bg
    };


    public ServiceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DetailFragment.
     */
    public static ServiceFragment newInstance() {
        ServiceFragment fragment = new ServiceFragment();
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
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.fragment_car_service, null, false);
        initView();
        initListener();
        return mBinding.getRoot();
    }


    @Override
    public void initView() {
        TextView topTitle = mBinding.getRoot().findViewById(R.id.top_title_tv);
        topTitle.setText("服务");
        main_logo = mBinding.getRoot().findViewById(R.id.top_back_iv);
        main_logo.setImageResource(R.drawable.main_top_icon);
        main_logo.setVisibility(View.INVISIBLE);

        msg_notice = mBinding.getRoot().findViewById(R.id.top_menu_iv);
        msg_notice.setImageResource(R.drawable.main_top_notices);


        mBinding.serviceBanner.setBannerPageClickListener(new MZBannerView.BannerPageClickListener() {
            @Override
            public void onPageClick(View view, int position) {
                Toast.makeText(getContext(), "click page:" + position, Toast.LENGTH_LONG).show();
            }
        });
        mBinding.serviceBanner.addPageChangeLisnter(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        List<Integer> bannerList = new ArrayList<>();
        for (int i = 0; i < BANNER.length; i++) {
            bannerList.add(BANNER[i]);
        }
        mBinding.serviceBanner.setIndicatorVisible(false);
        mBinding.serviceBanner.setPages(bannerList, new MZHolderCreator<BannerViewHolder>() {
            @Override
            public BannerViewHolder createViewHolder() {
                return new BannerViewHolder();
            }
        });
    }

    @Override
    public void initListener() {
        main_logo.setOnClickListener(this);
        msg_notice.setOnClickListener(this);
        mBinding.serviceIv1.setOnClickListener(this);
        mBinding.serviceIv2.setOnClickListener(this);
        mBinding.serviceIv3.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.top_back_iv:
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
            case R.id.top_menu_iv:
                startActivity(new Intent(getActivity(), MessageActivity.class));
                break;
            case R.id.service_iv1:
                ToastUtil.show(getActivity(),"开发中");

                break;
            case R.id.service_iv2:
//                ToastUtil.show(getActivity(),"开发中");?city="大连"&adCode="210211"
                enterWebviewActivity(" http://123.125.218.29:1082/#/maintance?city="
                        + CacheHelper.getCity() +"&adCode=" + CacheHelper.getCityAdCode() + "&vin=" + CacheHelper.getVin());
                break;
            case R.id.service_iv3:
                ToastUtil.show(getActivity(),"开发中");
//                enterWebviewActivity("http://123.125.218.29:1082/#/dealer");
                break;
        }
    }

    private void enterWebviewActivity(String url) {
        Intent intent = new Intent(getActivity(), WebviewActivity.class);
        intent.putExtra("URL",url);
        startActivity(intent);

    }


    public static class BannerViewHolder implements MZViewHolder<Integer> {
        private ImageView mImageView;

        @Override
        public View createView(Context context) {
            // 返回页面布局文件
            mImageView = new ImageView(context);
            return mImageView;
        }

        @Override
        public void onBind(Context context, int position, Integer data) {
            // 数据绑定
            mImageView.setImageResource(data);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        mBinding.serviceBanner.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mBinding.serviceBanner.start();
    }

}
