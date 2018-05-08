package cn.lds.common.base;


import cn.lds.common.manager.UMengManager;


import android.support.v4.app.Fragment;

/**
 * Created by leadingsoft on 17/12/11.
 */

public class BaseFragment extends Fragment {
    protected String className;

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public void onResume() {
        super.onResume();
        UMengManager.getInstance().onResumePage(className);
    }

    @Override
    public void onPause() {
        super.onPause();
        UMengManager.getInstance().onResumePage(className);
    }
}
