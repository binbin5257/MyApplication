package cn.lds.widget.dialog;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import cn.lds.widget.R;
import cn.lds.widget.ToastUtil;
import cn.lds.widget.dialog.base.CenterNormalDialog;

/**
 * 验证登录密码对话框
 * Created by sibinbin on 18-2-28.
 */

public class CommonInputDialog extends CenterNormalDialog<CommonInputDialog> {

    private View midline;
    private TextView cancelTv;
    private TextView inputPinHint;
    private EditText contentTv;
    public String pasw;
    private String mTitle;

    public String getPasw() {
        return contentTv.getText().toString();
    }

    public CommonInputDialog( Context context, String title) {
        super(context);
        this.mTitle = title;
        init();
    }


    @Override
    public int getLayoutRes() {
        return R.layout.layout_ver_login_psw;
    }

    @Override
    public void onCreateData() {
        setCanceledOnTouchOutside(true);

    }

    @Override
    public void onClick(View v, int id) {
        if (id == R.id.update_confirm) {
            if (0 == contentTv.getText().length()) {
                ToastUtil.showToast(context, "请输入登录密码");
                return;
            }
            onSubmitInputListener.onSubmitInput(contentTv.getText().toString());
        } else if (id == R.id.update_cancel) {
           dismiss();
        }
    }

    @Override
    public void show() {
        cancelTv.setVisibility(View.VISIBLE);
        midline.setVisibility(View.VISIBLE);
        super.show();
    }

    /**
     * 初始化
     */
    private void init() {
        contentTv = findViewById(R.id.edittext);
        cancelTv = findViewById(R.id.update_cancel);
        midline = findViewById(R.id.update_midline);
        inputPinHint = findViewById(R.id.input_pin_hint);
        inputPinHint.setText(mTitle);
        setOnCilckListener(R.id.update_confirm, R.id.update_cancel);
    }
    @Override
    public void dismiss() {
        super.dismiss();
        contentTv.setText("");
    }
    private OnSubmitInputListener onSubmitInputListener;

    public void setOnSubmitInputListener( OnSubmitInputListener submitInputListener ) {
        this.onSubmitInputListener = submitInputListener;
    }

    public interface OnSubmitInputListener{

        void onSubmitInput(String psw);

    }
}
