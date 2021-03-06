package internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chrisjason.baseui.ui.BaseAppcompatActivity;
import com.google.gson.Gson;
import com.jaeger.library.StatusBarUtil;
import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoImpl;
import com.jph.takephoto.model.CropOptions;
import com.jph.takephoto.model.InvokeParam;
import com.jph.takephoto.model.TContextWrap;
import com.jph.takephoto.model.TResult;
import com.jph.takephoto.permission.InvokeListener;
import com.jph.takephoto.permission.PermissionManager;
import com.jph.takephoto.permission.TakePhotoInvocationHandler;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import org.xutils.http.RequestParams;
import java.io.File;
import butterknife.BindView;
import butterknife.OnClick;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.R;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.application.IPSCApplication;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.entitys.eventBusBean.TokenEvent;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.entitys.userInfo.UserBean;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.presenters.presenterImp.UpLoadFileImp;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.presenters.presenterInterface.UpLoadFileInterface;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.urls.MainUrls;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.utils.ToastUtils;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.views.OnUpLoadFileCallback;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.widget.dialog.SweetAlertDialog;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.widget.popupwindow.ImageSelectPop;
import static internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.widget.dialog.SweetAlertDialog.WARNING_TYPE;

public class SettingActivity extends BaseAppcompatActivity implements TakePhoto.TakeResultListener, InvokeListener, OnUpLoadFileCallback {
    private TakePhoto takePhoto;
    private File file;
    private InvokeParam invokeParam;
    private Bitmap bitmap;

    @BindView(R.id.tv_nick_name)
    TextView tvNickName;
    @BindView(R.id.tv_account)
    TextView tvAccount;
    @BindView(R.id.iv_head)
    ImageView civHead;
    @BindView(R.id.ll_parent)
    LinearLayout llParent;

    private UpLoadFileInterface upLoadFilePresenter;
    private String url = "";
    private UserBean userBean = null;
    private AlertDialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setTranslucentForImageViewInFragment(this, 0, null);
        upLoadFilePresenter = new UpLoadFileImp();
    }

    @OnClick({R.id.iv_back, R.id.ll_click_change_head,R.id.tv_nick_name,R.id.tv_logout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                SettingActivity.this.finish();
                break;
            case R.id.ll_click_change_head:
                ImageSelectPop pop = new ImageSelectPop(this);
                pop.setCancelAble(false);
                pop.show(llParent, new ImageSelectPop.ItemClick() {
                    @Override
                    public void onClick(int type, double rentPrice, double buyPrice) {
                        switch (type) {
                            case 1:

                                break;
                            case 2:
                                //裁剪参数
                                CropOptions cropOptions = new CropOptions.Builder().
                                        setWithOwnCrop(false).create();
                                getTakePhoto().onPickFromCaptureWithCrop(getUri(), cropOptions);
                                break;
                            case 3:
                                //裁剪参数
                                CropOptions cropOptions1 = new CropOptions.Builder()
                                        .setWithOwnCrop(false).create();
                                getTakePhoto().onPickFromGalleryWithCrop(getUri(), cropOptions1);
                                break;
                        }
                    }
                });
                break;
            case R.id.tv_nick_name:
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                View v = LayoutInflater.from(SettingActivity.this).inflate(R.layout.dialog_with_input_layout,null);
                final TextView tvCancel = v.findViewById(R.id.tv_cancel);
                TextView tvConfirm = v.findViewById(R.id.tv_confirm);
                final EditText etInput = v.findViewById(R.id.et_input_nick_name);
                etInput.setText(userBean.getNickName());

                //监听回车
                etInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                        if (TextUtils.isEmpty(etInput.getText().toString())) {
                            ToastUtils.show(SettingActivity.this, "请先输入昵称");
                            return true;
                        }


                        if (i == EditorInfo.IME_ACTION_DONE) {
                            if (etInput.getText().toString().length() >= 2 && etInput.getText().toString().trim().length() <= 8){
                                RequestParams params = new RequestParams(MainUrls.modifyUserInfoUrl);
                                params.addBodyParameter("access_token",IPSCApplication.accessToken);
                                params.addBodyParameter("nickname",etInput.getText().toString().trim());
                                params.addBodyParameter("id",((IPSCApplication)getApplication()).getUserInfo().getId()+"");
                                upLoadFilePresenter.modifyUserNickName(params,SettingActivity.this);
                                dialog.dismiss();
                            }else {
                                ToastUtils.show(SettingActivity.this,"请输入2-8位长度的昵称");
                            }
                        }


                        return true;
                    }
                });

                tvCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                tvConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (etInput.getText().toString().length() >= 2 && etInput.getText().toString().trim().length() <= 8){
                            RequestParams params = new RequestParams(MainUrls.modifyUserInfoUrl);
                            params.addBodyParameter("access_token",IPSCApplication.accessToken);
                            params.addBodyParameter("nickname",etInput.getText().toString().trim());
                            params.addBodyParameter("id",((IPSCApplication)getApplication()).getUserInfo().getId()+"");
                            upLoadFilePresenter.modifyUserNickName(params,SettingActivity.this);
                            dialog.dismiss();
                        }else {
                            ToastUtils.show(SettingActivity.this,"请输入2-8位长度的昵称");
                        }
                    }
                });
                builder.setView(v);
                builder.setCancelable(false);
                dialog = builder.create();
                dialog.show();
                break;
            case R.id.tv_logout:
                final SweetAlertDialog dialog = new SweetAlertDialog(SettingActivity.this,WARNING_TYPE);
                dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        dialog.dismissWithAnimation();
                        ((IPSCApplication)getApplication()).removeUserInfo();
                        TokenEvent event = new TokenEvent();
                        event.code = 2;
                        EventBus.getDefault().post(event);
                        SettingActivity.this.finish();
                    }
                });
                dialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        dialog.dismissWithAnimation();
                    }
                });
                dialog.showCancelButton(true);
                dialog.setCancelText("取消");
                dialog.setConfirmText("确定");
                dialog.setTitleText("温馨提示");
                dialog.setContentText("是否要退出登录?");
                dialog.setCancelable(false);
                dialog.show();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (((IPSCApplication) getApplication()).getUserInfo() != null) {
            userBean = ((IPSCApplication) getApplication()).getUserInfo();
            if (userBean != null && userBean.getNickName() != null) {
                tvNickName.setText(userBean.getNickName());
            }
            if (userBean != null && userBean.getName() != null) {
                tvAccount.setText(userBean.getName());
            }
            if (userBean != null && userBean.getImg() != null) {
                RequestOptions options = new RequestOptions();
                options.circleCrop();
                options.placeholder(R.mipmap.icon_mine_defaul_head);
                options.error(R.mipmap.icon_mine_defaul_head);
                Glide.with(this).load(userBean.getImg()).apply(options).into(civHead);
            }
        }
    }

    /**
     * 图片保存路径
     *
     * @return
     */
    private Uri getUri() {

        file = new File(Environment.getExternalStorageDirectory(), "/shoes/images/" + System.currentTimeMillis() + ".jpg");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        Uri imageUri = Uri.fromFile(file);
        return imageUri;
    }

    /**
     * 获取TakePhoto实例
     *
     * @return
     */
    public TakePhoto getTakePhoto() {
        if (takePhoto == null) {
            takePhoto = (TakePhoto) TakePhotoInvocationHandler.of(this).bind(new TakePhotoImpl(this, this));
        }
        return takePhoto;
    }

    @Override
    public int initLayout() {
        return R.layout.activity_setting;
    }

    @Override
    public void reloadData() {

    }

    @Override
    public void takeSuccess(TResult result) {
        try{
            bitmap = BitmapFactory.decodeFile(result.getImage().getPath());
        }catch (Exception e){
            e.printStackTrace();
        }

        if (null != file) {
            UpUserImg();
        }
    }

    //上传图片
    private void UpUserImg() {
        RequestParams params = new RequestParams(MainUrls.upLoadImageUrl);
        params.addBodyParameter("access_token", IPSCApplication.accessToken);
        //mimeType/image/jpg
        params.addBodyParameter("Content-Type","image/jpeg");
        params.addBodyParameter("file", file);
        params.setMultipart(true);
        upLoadFilePresenter.upLoadFile(params, this);
    }

    @Override
    public void takeFail(TResult result, String msg) {

    }

    @Override
    public void takeCancel() {

    }

    @Override
    public PermissionManager.TPermissionType invoke(InvokeParam invokeParam) {
        PermissionManager.TPermissionType type = PermissionManager.checkPermission(TContextWrap.of(this), invokeParam.getMethod());
        if (PermissionManager.TPermissionType.WAIT.equals(type)) {
            this.invokeParam = invokeParam;
        }
        return type;
    }

    /**
     * 权限申请回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //处理运行时权限
        PermissionManager.TPermissionType type = PermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.handlePermissionsResult(this, type, invokeParam, this);
    }

    /**
     * 页面跳转回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getTakePhoto().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStarted() {
        showLoading(false, "上传图片中...");
    }

    @Override
    public void onFinished() {
        dismissLoading();
    }

    @Override
    public void onError(String error) {
        Log.e("TAG", "出错:" + error);
    }

    //文件上传成功
    @Override
    public void onLoadFileSuccess(String result) {

        if (result != null) {
            Log.e("TAG", "上传图片结果:" + result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                int code = jsonObject.getInt("code");
                int state = jsonObject.getInt("state");
                String msg = jsonObject.has("msg") ? jsonObject.getString("msg") : "";
                if (code == 0 && state == 0) {
                    JSONObject data = jsonObject.getJSONObject("data");
                    url = data.getString("url");
                    String sha256 = data.has("sha256")?data.getString("sha256"):"";
                    ToastUtils.show(SettingActivity.this, "上传图片成功");
                    RequestParams params = new RequestParams(MainUrls.modifyUserInfoUrl);
                    params.addBodyParameter("id",((IPSCApplication)getApplication()).getUserInfo().getId()+"");
                    params.addBodyParameter("img",sha256);
                    params.addBodyParameter("access_token",IPSCApplication.accessToken);
                    upLoadFilePresenter.modifyUserHead(params,this);
                } else {
                    ToastUtils.show(SettingActivity.this, msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //头像修改
    @Override
    public void onModifyUserHeadSuccess(String result) {
        if (result != null) {
            try {
                Log.e("TAG", "修改头像:" + result);
                JSONObject jsonObject = new JSONObject(result);
                int code = jsonObject.getInt("code");
                int state = jsonObject.getInt("state");
                if (code == 0 && state == 0){
                    ToastUtils.show(SettingActivity.this, "修改头像成功");
                    RequestOptions options = new RequestOptions();
                    options.circleCrop();
                    options.placeholder(R.mipmap.icon_mine_defaul_head);
                    options.error(R.mipmap.icon_mine_defaul_head);
                    Log.e("TAG","头像url:"+MainUrls.areaUrl + url);
                    Glide.with(this).load(MainUrls.areaUrl + url).apply(options).into(civHead);
                    UserBean userBean = ((IPSCApplication)getApplication()).getUserInfo();
                    userBean.setImg(MainUrls.areaUrl + url);
                    ((IPSCApplication)getApplication()).saveUserInfo(new Gson().toJson(userBean));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //修改昵称
    @Override
    public void onModifyUserNickName(String result) {
        if (result != null){
            try {
                Log.e("TAG","修改昵称:"+result);
                JSONObject jsonObject = new JSONObject(result);
                int code = jsonObject.getInt("code");
                int state = jsonObject.getInt("state");
                String nickName = jsonObject.has("data")?jsonObject.getJSONObject("data").getString("nickname"):"";
                String msg = jsonObject.has("msg")?jsonObject.getString("msg"):"";
                if (code == 0 && state == 0){
                    ToastUtils.show(SettingActivity.this,"修改昵称成功");
                    UserBean userBean = ((IPSCApplication)getApplication()).getUserInfo();
                    userBean.setNickName(nickName);
                    ((IPSCApplication)getApplication()).saveUserInfo(new Gson().toJson(userBean));
                    tvNickName.setText(nickName);
                }else {
                    ToastUtils.show(SettingActivity.this,msg);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}