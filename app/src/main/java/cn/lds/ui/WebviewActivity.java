package cn.lds.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.lds.BuildConfig;
import cn.lds.R;
import cn.lds.common.base.BaseActivity;
import cn.lds.common.base.IPermission;
import cn.lds.common.utils.CacheHelper;
import cn.lds.common.utils.FileHelper;
import cn.lds.common.utils.NetWorkHelper;
import cn.lds.common.utils.PictureHelper;
import cn.lds.databinding.ActivityWebviewBinding;
import cn.lds.widget.ToastUtil;
import cn.lds.widget.dialog.CameraOrAlbumBottomDialog;
import cn.lds.widget.dialog.CenterListDialog;
import cn.lds.widget.dialog.annotation.ClickPosition;
import cn.lds.widget.dialog.callback.OnDialogClickListener;
import cn.lds.widget.dialog.callback.OnDialogOnItemClickListener;

/**
 * 彩虹H5页面
 * Created by sibinbin on 18-1-31.
 */

@SuppressLint("Registered")
public class WebviewActivity extends BaseActivity {

    private ActivityWebviewBinding mWebviewBinding;
    /**
     * 获取到的分享图片地址
     */
    private String imgUrl;
    private List<String> shareTypeList;
    private CenterListDialog shareDialog;
    private CameraOrAlbumBottomDialog cameraOrAlbumBottomDialog;
    protected static final int PHOTO_REQUEST_CAMERA = 1;// 拍照
    protected static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private String h5PhotoPath;
    private String enCodePic;
    private String loadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWebviewBinding = DataBindingUtil.setContentView(this, R.layout.activity_webview);
        initShareDialog();
        initCameraDialog();
        initView();
        initListener();

//        requesTakePhotoPermission();
    }

    /**
     * 初始化相机对话框
     */
    private void initCameraDialog() {
        cameraOrAlbumBottomDialog = getCameraOrAlbumBottomDialog();
        cameraOrAlbumBottomDialog.setOnDialogClickListener(new OnDialogClickListener() {
            @Override
            public void onDialogClick(Dialog dialog, String clickPosition) {
                dialog.dismiss();
                switch (clickPosition){
                    case ClickPosition.TAKE_PHOTO:
                        requesTakePhotoPermission();
                        break;
                    case ClickPosition.TAKE_ALBUM:
                        requestWriteStoragePermission();
                        break;
                    case ClickPosition.CANCEL:
                        break;

                }
            }
        });
    }

    @NonNull
    private CameraOrAlbumBottomDialog getCameraOrAlbumBottomDialog() {
        return new CameraOrAlbumBottomDialog(this);
    }

    /**
     * 初始化分享对话框
     */
    private void initShareDialog() {
        shareTypeList = new ArrayList<>();
        shareTypeList.add("分享微信好友");
        shareTypeList.add("分享朋友圈");
        shareDialog = new CenterListDialog(this,this,shareTypeList).setOnDialogOnItemClickListener(new OnDialogOnItemClickListener() {
            @Override
            public void onDialogItemClick(Dialog dialog, final int position) {
                dialog.dismiss();

                //JS调用APP提供的分享方法，唤起微信，将图片分享给微信好友或者朋友圈，呈现为图片缩略图形式。
//                分享方法名：wechatShare(imgUrl, type)
                 mWebviewBinding.webview.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebviewBinding.webview.loadUrl("javascript:wechatShare('" + imgUrl +',' + position + "')");
                    }
                });
            }
        });
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    public void initView() {
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            loadUrl = bundle.getString("URL");
        }


        //创建CookieSyncManager
        CookieSyncManager.createInstance(this);
        //得到CookieManager
        CookieManager cookieManager = CookieManager.getInstance();
        //得到向URL中添加的Cookie的值
        String cookieString = CacheHelper.getCookie();
//        //使用cookieManager..setCookie()向URL中添加Cookie
        if(!TextUtils.isEmpty(loadUrl)){
            cookieManager.setCookie(loadUrl, cookieString);
        }
        CookieSyncManager.getInstance().sync();



        WebSettings settings = mWebviewBinding.webview.getSettings();
        // 缓存(cache)
        settings.setAppCacheEnabled(true);      // 默认值 false
        settings.setAppCachePath(getCacheDir().getAbsolutePath());
        // 存储(storage)
        settings.setDomStorageEnabled(true);    // 默认值 false
        settings.setDatabaseEnabled(true);      // 默认值 false
        // 是否支持viewport属性，默认值 false
        // 页面通过`<meta name="viewport" ... />`自适应手机屏幕
        settings.setUseWideViewPort(true);
        // 是否使用overview mode加载页面，默认值 false
        // 当页面宽度大于WebView宽度时，缩小使页面宽度等于WebView宽度
        settings.setLoadWithOverviewMode(true);
        // 是否支持Javascript，默认值false
        settings.setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 5.0以上允许加载http和https混合的页面(5.0以下默认允许，5.0+默认禁止)
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        if (NetWorkHelper.isNetworkConnected()) {
            // 根据cache-control决定是否从网络上取数据
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            // 没网，离线加载，优先加载缓存(即使已经过期)
            settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
//        String loadUrl = getLoadUrl();
//        String loadUrl = "http://192.168.1.103:1086/view/testH5.html";
        if(!TextUtils.isEmpty(loadUrl)){
            mWebviewBinding.webview.loadUrl(loadUrl);
        }
        //参数2：Java对象名
        mWebviewBinding.webview.addJavascriptInterface(new AndroidtoJs(), "AndroidWebView");//AndroidtoJS类对象映射到js的app对象


    }

    @Override
    public void initListener() {
        mWebviewBinding.webview.setWebChromeClient(new MyWebChromeClient());
        mWebviewBinding.webview.setWebViewClient(new MyWebViewClient());
    }
    /**
     * 需求说明：
     *  APP通过URL（query参数）携带用户唯一标识（如userId）给H5页面，同时猎豹方面提供获取用户信息的外部接口，通过该接口以userId作为参数获取用户其他必需信息（附下表）。
     *  示例：
     *  H5页面URL: http://xxxxx/demo.html?userid=xxx
     */
    public String getLoadUrl() {
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            //url: http://xxxxx/demo.html?userid=xxx
            String url = bundle.getString("url");
            return url;
        }
        return "";
    }

    /**
     * WebViewClient 主要提供网页加载各个阶段的通知，比如网页开始加载onPageStarted，网页结束加载onPageFinished等
     */
    public class MyWebViewClient extends WebViewClient{
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {

        }
    }

    public class MyWebChromeClient extends WebChromeClient{

    }

    // 继承自Object类
    public class AndroidtoJs extends Object {

        // 定义JS需要调用的方法
        // 被JS调用的方法必须加入@JavascriptInterface注解
        @JavascriptInterface
        public void getImageUrl(String url) {
            imgUrl = url;
            //显示分享对话框
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    shareDialog.show();
                }
            });
        }
        @JavascriptInterface
        public void takePhoto() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cameraOrAlbumBottomDialog.show();
                }
            });
        }
        @JavascriptInterface
        public void closeAppWebview() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WebviewActivity.this.finish();
                }
            });
        }

    }

    /**
     * 请求读写内存卡权限
     */
    private void requestWriteStoragePermission() {
        requestRunTimePermission(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, new IPermission() {
            @Override
            public void onGranted() {
                PictureHelper.enterAlbum(WebviewActivity.this,PHOTO_REQUEST_GALLERY);
            }

            @Override
            public void onDenied(List<String> deniedPermissions) {
                for (String deniedPermission : deniedPermissions) {
                    ToastUtil.showToast(mContext, "被拒绝的权限是"+deniedPermission);
                }
            }
        });
    }

    /**
     * 请求照相权限
     */
    private void requesTakePhotoPermission() {
        BaseActivity.requestRunTimePermission(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, new IPermission() {

            @Override
            public void onGranted() {
                //启动相机
                // 获取SD卡路径
                h5PhotoPath = FileHelper.getTakeCarPath();
                PictureHelper.takePhoto(WebviewActivity.this,PHOTO_REQUEST_CAMERA, h5PhotoPath);


            }

            @Override
            public void onDenied(List<String> deniedPermissions) {
                for (String deniedPermission : deniedPermissions) {
                    ToastUtil.showToast(mContext, "被拒绝的权限是"+deniedPermission);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case PHOTO_REQUEST_CAMERA:
                    enCodePic = PictureHelper.compressImageAndBase64EnCode(h5PhotoPath,60);
                    break;
                case PHOTO_REQUEST_GALLERY:
                    if (data != null) {
                        // 得到图片的全路径
                        Uri uri = data.getData();
                        File file = new File(uri.getPath());
                        enCodePic = PictureHelper.compressImageAndBase64EnCode(file.getPath(),100);
                    }
                    break;
            }
            mWebviewBinding.webview.post(new Runnable() {
                @Override
                public void run() {
                    mWebviewBinding.webview.loadUrl("javascript:getBase64ByApp('"+ enCodePic + "')");

                }
            });
        }
    }
}
