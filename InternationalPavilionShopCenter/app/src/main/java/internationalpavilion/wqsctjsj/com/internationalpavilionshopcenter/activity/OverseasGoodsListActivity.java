package internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.activity;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.chrisjason.baseui.ui.BaseAppcompatActivity;
import com.chrisjason.baseui.util.DpUtils;
import com.jaeger.library.StatusBarUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.R;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.adapter.BondedGoodsListAdapter;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.application.IPSCApplication;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.entitys.BondedGoodsBean;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.entitys.PriceData;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.entitys.RightClassBean;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.entitys.RightClassInChildBean;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.entitys.homeBondedGoodsBean.HomeBondedGoodsBean;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.presenters.presenterImp.OverseasGoodsImp;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.presenters.presenterInterface.OverseasGoodsInterface;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.urls.MainUrls;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.utils.ToastUtils;
import internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.views.OnOverseasGoodsCallBack;


public class OverseasGoodsListActivity extends BaseAppcompatActivity implements OnOverseasGoodsCallBack {
    @BindView(R.id.rv_bonded_goods)
    RecyclerView rvBondedGoods;
    @BindView(R.id.ll_screen_container)
    LinearLayout llScreenContainer;
    @BindView(R.id.sv_animal)
    ScrollView svAnimal;
    @BindView(R.id.rl_animal)
    RelativeLayout rlAnimal;

    @BindView(R.id.ll_touch)
    LinearLayout llTouch;

    @BindView(R.id.tv_filter_popularity)
    TextView tvFilterPopularity;
    @BindView(R.id.tv_filter_discount)
    TextView tvFilterDiscount;
    @BindView(R.id.tv_filter_screen)
    TextView tvFilterScreen;
    @BindView(R.id.tv_filter_price)
    TextView tvFilterPrice;
    @BindView(R.id.iv_price_pointer)
    ImageView ivPricePointer;
    @BindView(R.id.iv_filter_screen_pointer)
    ImageView ivFilterScreenPointer;
    @BindView(R.id.ll_goods_tag_container_price)
    internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.widget.LinearLayout llPriceContainer;
    @BindView(R.id.ll_goods_tag_container_brand)
    internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.widget.LinearLayout llBrandsContainer;
    @BindView(R.id.ll_goods_tag_container_class)
    internationalpavilion.wqsctjsj.com.internationalpavilionshopcenter.widget.LinearLayout llClassContainer;

    @BindView(R.id.v_scroll)
    View viewScroll;
    @BindView(R.id.iv_brand_more)
    ImageView ivBrandMore;
    @BindView(R.id.iv_class_more)
    ImageView ivClassMore;
    @BindView(R.id.tv_remind)
    TextView tvRemind;
    @BindView(R.id.srl_content)
    SmartRefreshLayout srlContent;

    @BindView(R.id.et_min_price)
    EditText etMinPrice;
    @BindView(R.id.et_max_price)
    EditText etMaxPrice;

    @BindView(R.id.tv_reset)
    TextView tvReset;
    @BindView(R.id.tv_confirm)
    TextView tvConfirm;


    private ArrayList<HomeBondedGoodsBean> data = new ArrayList<>();
    private BondedGoodsListAdapter adapter;
    private int screenWidth;
    private int currentPosition = 1;
    private boolean brandIsOpen = false;
    private boolean classIsOpen = false;
    private int pageIndex = 1;
    private int priceState = 2;

    private OverseasGoodsInterface overseasPresenter;

    private int currentBrandId = -1;
    private int currentClassId = -1;
    private int currentPriceId = -1;

    private ArrayList<RightClassInChildBean> branList = new ArrayList<>();
    private ArrayList<RightClassInChildBean> rightClassData = new ArrayList<>();
    private ArrayList<PriceData> priceData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setTranslucentForImageViewInFragment(this, 0, null);
        WindowManager windowManager = getWindowManager();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        overseasPresenter = new OverseasGoodsImp();
        initView();
        RequestParams paramsBrand = new RequestParams(MainUrls.getClassBrandUrl);
        paramsBrand.addBodyParameter("access_token", IPSCApplication.accessToken);
        paramsBrand.addBodyParameter("page", "1");
        paramsBrand.addBodyParameter("limit", "100");
        overseasPresenter.getBrandData(paramsBrand, this);

        RequestParams paramsClass = new RequestParams(MainUrls.getAllThreeLevelClassUrl);
        paramsClass.addBodyParameter("access_token", IPSCApplication.accessToken);
        overseasPresenter.getClassData(paramsClass, this);
        initData();
        if (priceData.size() == 0) {
            PriceData pd = new PriceData();
            pd.setId(1);
            pd.setPriceSection("200以下");
            pd.setChecked(false);
            priceData.add(pd);

            PriceData pd1 = new PriceData();
            pd1.setId(2);
            pd1.setChecked(false);
            pd1.setPriceSection("200-499");
            priceData.add(pd1);

            PriceData pd2 = new PriceData();
            pd2.setId(3);
            pd2.setChecked(false);
            pd2.setPriceSection("500-999");
            priceData.add(pd2);

            PriceData pd3 = new PriceData();
            pd3.setId(4);
            pd3.setChecked(false);
            pd3.setPriceSection("1000-1999");
            priceData.add(pd3);

            PriceData pd4 = new PriceData();
            pd4.setId(5);
            pd4.setPriceSection("2000以上");
            priceData.add(pd4);

        }
        //添加价格
        initPriceData();
    }

    private void initView() {
        llTouch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    animalOut(rlAnimal);
                }
                return true;
            }
        });
        srlContent.setEnableLoadmore(true);
        srlContent.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                pageIndex = 1;
                initData();
            }
        });
        srlContent.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                pageIndex++;
                initData();
            }
        });
        tvReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyAllClassUnChecked();
                notifyAllUnChecked();
                notifyAllPriceUnChecked();
                currentClassId = -1;
                currentBrandId = -1;
                currentPriceId = -1;
                initPriceData();
                initClassData();
                initBrand();
                etMaxPrice.setText("");
                etMinPrice.setText("");
            }
        });
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etMinPrice.getText().toString().trim().length() != 0 && etMaxPrice.getText().toString().trim().length() != 0) {
                    if (Integer.valueOf(etMinPrice.getText().toString().trim()) < Integer.valueOf(etMaxPrice.getText().toString().trim())) {

                    } else {
                        ToastUtils.show(OverseasGoodsListActivity.this, "最低价必须小于最高价");
                    }
                }
                pageIndex = 1;
                initData();
                animalOut(rlAnimal);
            }
        });
    }

    public void animalIn(View view) {
        Animation animator = AnimationUtils.loadAnimation(this, R.anim.pop_in);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animator);
    }

    public void animalOut(View view) {
        Animation animator = AnimationUtils.loadAnimation(this, R.anim.pop_out);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                llScreenContainer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animator);
    }

    private void initData() {
        RequestParams params = new RequestParams(MainUrls.getOverseasGoodsUrl);
        params.addBodyParameter("access_token", IPSCApplication.accessToken);
        params.addBodyParameter("page", "" + pageIndex);
        params.addBodyParameter("limit", "10");
        if (currentPosition == 1){
            params.addBodyParameter("num_r","1");
        }
        if (currentPosition == 2){
            params.addBodyParameter("is_full","1");
        }
        if (currentPosition == 3){
            if (priceState == 1){
                params.addBodyParameter("price","0");
                Log.e("TAG","升序");
            }else if (priceState == 2){
                params.addBodyParameter("price","1");
                Log.e("TAG","降序");
            }
        }
        if (currentBrandId != -1) {
            params.addBodyParameter("brand", currentBrandId + "");
        }
        if (currentClassId != -1) {
            params.addBodyParameter("cate", currentClassId + "");
        }
        if (etMinPrice.getText().toString().trim().length() != 0) {
            params.addBodyParameter("pricemin", etMinPrice.getText().toString().trim());
        }
        if (etMaxPrice.getText().toString().trim().length() != 0) {
            params.addBodyParameter("pricemax", etMaxPrice.getText().toString().trim());
        }
        if (currentPriceId != -1){
            switch (currentPriceId){
                case 1:
                    params.addBodyParameter("pricemax","200");
                    break;
                case 2:
                    params.addBodyParameter("pricemin","200");
                    params.addBodyParameter("pricemax","499");
                    break;
                case 3:
                    params.addBodyParameter("pricemin","500");
                    params.addBodyParameter("pricemax","999");
                    break;
                case 4:
                    params.addBodyParameter("pricemin","1000");
                    params.addBodyParameter("pricemax","1999");
                    break;
                case 5:
                    params.addBodyParameter("pricemin","2000");
                    break;
            }
        }
        overseasPresenter.getOverseasGoods(params, this);
        if (adapter == null) {
            adapter = new BondedGoodsListAdapter(this, data);
            rvBondedGoods.setLayoutManager(new GridLayoutManager(this, 2));
            rvBondedGoods.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    @OnClick({R.id.iv_back, R.id.ll_filter_screen, R.id.tv_filter_popularity, R.id.tv_filter_discount, R.id.ll_filter_price,
            R.id.ll_brand_more, R.id.ll_class_more
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.ll_filter_screen:
                llScreenContainer.setVisibility(View.VISIBLE);
                animalIn(rlAnimal);
                change(4);
                ivFilterScreenPointer.setImageResource(R.mipmap.icon_filter_screen_selected);
                ivPricePointer.setImageResource(R.mipmap.icon_filter_price);
                break;
            case R.id.tv_filter_popularity:
                change(1);
                ivPricePointer.setImageResource(R.mipmap.icon_filter_price);
                ivFilterScreenPointer.setImageResource(R.mipmap.icon_filter_screen);
                break;
            case R.id.tv_filter_discount:
                change(2);
                ivPricePointer.setImageResource(R.mipmap.icon_filter_price);
                ivFilterScreenPointer.setImageResource(R.mipmap.icon_filter_screen);
                break;
            case R.id.ll_filter_price:
                if (priceState == 1){
                    priceState = 2;
                }else if (priceState == 2){
                    priceState = 1;
                }
                change(3);
                ivPricePointer.setImageResource(R.mipmap.icon_filter_price_up);
                ivFilterScreenPointer.setImageResource(R.mipmap.icon_filter_screen);
                break;
            case R.id.ll_brand_more:
                if (brandIsOpen) {
                    ViewGroup.LayoutParams params = llBrandsContainer.getLayoutParams();
                    params.height = DpUtils.dpToPx(this, 80);
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    llBrandsContainer.setLayoutParams(params);
                    ivBrandMore.setImageResource(R.mipmap.icon_class_down);
                    brandIsOpen = false;
                } else {
                    ViewGroup.LayoutParams params = llBrandsContainer.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    llBrandsContainer.setLayoutParams(params);
                    ivBrandMore.setImageResource(R.mipmap.icon_class_up);
                    brandIsOpen = true;
                }
                break;
            case R.id.ll_class_more:
                if (classIsOpen) {
                    ViewGroup.LayoutParams params = llClassContainer.getLayoutParams();
                    params.height = DpUtils.dpToPx(this, 80);
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    llClassContainer.setLayoutParams(params);
                    ivClassMore.setImageResource(R.mipmap.icon_class_down);
                    classIsOpen = false;
                } else {
                    ViewGroup.LayoutParams params = llClassContainer.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    llClassContainer.setLayoutParams(params);
                    ivClassMore.setImageResource(R.mipmap.icon_class_up);
                    classIsOpen = true;
                }
                break;
        }
    }

    private void change(int index) {
        switch (index) {
            case 1:
                tvFilterPopularity.setTextColor(Color.parseColor("#ff0000"));
                tvFilterDiscount.setTextColor(Color.parseColor("#636363"));
                tvFilterPrice.setTextColor(Color.parseColor("#636363"));
                tvFilterScreen.setTextColor(Color.parseColor("#636363"));
                scrollAnimal(1);
                pageIndex = 1;
                initData();
                break;
            case 2:
                tvFilterPopularity.setTextColor(Color.parseColor("#636363"));
                tvFilterDiscount.setTextColor(Color.parseColor("#ff0000"));
                tvFilterPrice.setTextColor(Color.parseColor("#636363"));
                tvFilterScreen.setTextColor(Color.parseColor("#636363"));
                scrollAnimal(2);
                pageIndex = 1;
                initData();
                break;
            case 3:
                tvFilterPopularity.setTextColor(Color.parseColor("#636363"));
                tvFilterDiscount.setTextColor(Color.parseColor("#636363"));
                tvFilterPrice.setTextColor(Color.parseColor("#ff0000"));
                tvFilterScreen.setTextColor(Color.parseColor("#636363"));
                scrollAnimal(3);
                pageIndex = 1;
                initData();
                break;
            case 4:
                tvFilterPopularity.setTextColor(Color.parseColor("#636363"));
                tvFilterDiscount.setTextColor(Color.parseColor("#636363"));
                tvFilterPrice.setTextColor(Color.parseColor("#636363"));
                tvFilterScreen.setTextColor(Color.parseColor("#ff0000"));
                scrollAnimal(4);
                break;
        }
    }

    private void scrollAnimal(int position) {
        int width = screenWidth / 4 - 2 * DpUtils.dpToPx(this, 20);
        switch (position) {
            case 1:
                int pL = DpUtils.dpToPx(this, 20);
                if (currentPosition == 2) {
                    ObjectAnimator obA = ObjectAnimator.ofFloat(viewScroll, "translationX", width + 2 * pL, 0);
                    obA.setDuration(300);
                    obA.setInterpolator(new LinearInterpolator());
                    obA.start();
                } else if (currentPosition == 3) {
                    ObjectAnimator obA = ObjectAnimator.ofFloat(viewScroll, "translationX", 2 * width + 4 * pL, 0);
                    obA.setDuration(300);
                    obA.setInterpolator(new LinearInterpolator());
                    obA.start();
                } else if (currentPosition == 4) {
                    ObjectAnimator obA = ObjectAnimator.ofFloat(viewScroll, "translationX", 3 * width + 6 * pL, 0);
                    obA.setDuration(300);
                    obA.setInterpolator(new LinearInterpolator());
                    obA.start();
                }
                currentPosition = 1;
                break;
            case 2:
                pL = DpUtils.dpToPx(this, 20);
                if (currentPosition == 1) {
                    ObjectAnimator obA = ObjectAnimator.ofFloat(viewScroll, "translationX", 0, (width + 2 * pL));
                    obA.setDuration(300);
                    obA.setInterpolator(new LinearInterpolator());
                    obA.start();
                } else if (currentPosition == 3) {
                    ObjectAnimator obA = ObjectAnimator.ofFloat(viewScroll, "translationX", 2 * width + 4 * pL, (width + 2 * pL));
                    obA.setDuration(300);
                    obA.setInterpolator(new LinearInterpolator());
                    obA.start();
                } else if (currentPosition == 4) {
                    ObjectAnimator obA = ObjectAnimator.ofFloat(viewScroll, "translationX", 3 * width + 6 * pL, (width + 2 * pL));
                    obA.setDuration(300);
                    obA.setInterpolator(new LinearInterpolator());
                    obA.start();
                }
                currentPosition = 2;
                break;
            case 3:
                pL = DpUtils.dpToPx(this, 20);
                if (currentPosition == 1) {
                    ObjectAnimator obA = ObjectAnimator.ofFloat(viewScroll, "translationX", 0, (2 * width + 4 * pL));
                    obA.setDuration(300);
                    obA.setInterpolator(new LinearInterpolator());
                    obA.start();
                } else if (currentPosition == 2) {
                    ObjectAnimator obA = ObjectAnimator.ofFloat(viewScroll, "translationX", width + 2 * pL, (2 * width + 4 * pL));
                    obA.setDuration(300);
                    obA.setInterpolator(new LinearInterpolator());
                    obA.start();
                } else if (currentPosition == 4) {
                    ObjectAnimator obA = ObjectAnimator.ofFloat(viewScroll, "translationX", 3 * width + 6 * pL, (2 * width + 4 * pL));
                    obA.setDuration(300);
                    obA.setInterpolator(new LinearInterpolator());
                    obA.start();
                }
                currentPosition = 3;
                break;
            case 4:
                pL = DpUtils.dpToPx(this, 20);
                if (currentPosition == 1) {
                    ObjectAnimator obA = ObjectAnimator.ofFloat(viewScroll, "translationX", 0, (3 * width + 6 * pL));
                    obA.setDuration(300);
                    obA.setInterpolator(new LinearInterpolator());
                    obA.start();
                } else if (currentPosition == 2) {
                    ObjectAnimator obA = ObjectAnimator.ofFloat(viewScroll, "translationX", width + 2 * pL, (3 * width + 6 * pL));
                    obA.setDuration(300);
                    obA.setInterpolator(new LinearInterpolator());
                    obA.start();
                } else if (currentPosition == 3) {
                    ObjectAnimator obA = ObjectAnimator.ofFloat(viewScroll, "translationX", 2 * width + 4 * pL, (3 * width + 6 * pL));
                    obA.setDuration(300);
                    obA.setInterpolator(new LinearInterpolator());
                    obA.start();
                }
                currentPosition = 4;
                break;
        }
    }

    @Override
    public int initLayout() {
        return R.layout.activity_overseas_goods_list;
    }

    @Override
    public void reloadData() {

    }

    @Override
    public void onStarted() {
        showLoading(false, "加载数据中...");
    }

    @Override
    public void onFinished() {
        dismissLoading();
        srlContent.finishLoadmore();
        srlContent.finishRefresh();
    }

    @Override
    public void onError(String error) {
        Log.e("TAG", "网络访问出错:" + error);
    }

    @Override
    public void onOverSeasGoodsLoaded(String result) {
        if (result != null) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                int code = jsonObject.getInt("code");
                int state = jsonObject.getInt("state");
                if (state == 0 && code == 0) {
                    if (jsonObject.has("data")) {
                        JSONArray data = jsonObject.getJSONArray("data");
                        if (pageIndex == 1) {
                            if (currentPosition == 3){
                                if (priceState == 1){
                                    ivPricePointer.setImageResource(R.mipmap.icon_filter_price_up);
                                }else {
                                    ivPricePointer.setImageResource(R.mipmap.icon_filter_price_down);
                                }
                            }else {
                                ivPricePointer.setImageResource(R.mipmap.icon_filter_price);
                                priceState = 2;
                            }
                            OverseasGoodsListActivity.this.data.clear();
                            adapter.notifyDataSetChanged();
                        }
                        if (data != null && data.length() != 0) {

                            for (int i = 0; i < data.length(); i++) {
                                HomeBondedGoodsBean bean = new HomeBondedGoodsBean();
                                int id = data.getJSONObject(i).getJSONObject("goods_goods").getInt("id");
                                String name = data.getJSONObject(i).getJSONObject("goods_goods").getString("name");
                                String description = data.getJSONObject(i).getJSONObject("goods_goods").getString("keyword");
                                String baseAreaName = data.getJSONObject(i).getJSONObject("goods_goods").getJSONObject("base_area").getString("name");
                                double price = data.getJSONObject(i).getDouble("price");
                                String goodsPic = data.getJSONObject(i).getJSONObject("goods_goods").getJSONArray("img").getString(0);
                                bean.setGoodsId(id);
                                bean.setGoodsName(name);
                                bean.setBrandName(baseAreaName);
                                bean.setDescription(description);
                                bean.setGoodsPic(goodsPic);
                                bean.setGoodsPrice(price);
                                OverseasGoodsListActivity.this.data.add(bean);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            tvRemind.setVisibility(View.VISIBLE);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (tvRemind != null) {
                                        tvRemind.setVisibility(View.GONE);
                                    } else {
                                        tvRemind = null;
                                        System.gc();
                                    }
                                }
                            }, 2000);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.e("TAG", "海外商品列表:" + result);

    }

    @Override
    public void onBrandDataLoaded(String result) {
        if (result != null) {
            Log.e("TAG", "品牌分类:" + result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                int code = jsonObject.getInt("code");
                int state = jsonObject.getInt("state");
                if (code == 0 && state == 0) {
                    if (jsonObject.has("data")) {
                        JSONArray data = jsonObject.getJSONArray("data");
                        branList.clear();
                        if (data != null && data.length() != 0) {
                            for (int i = 0; i < data.length(); i++) {
                                RightClassBean rightClassBean = new RightClassBean();
                                int id = data.getJSONObject(i).getInt("id");
                                String name = data.getJSONObject(i).getString("name");

                                rightClassBean.setId(id);
                                rightClassBean.setMainClassName(name);

                                if (data.getJSONObject(i).has("list") && !TextUtils.isEmpty(data.getJSONObject(i).getString("list"))) {
                                    JSONArray list = data.getJSONObject(i).getJSONArray("list");
                                    if (list != null && list.length() != 0) {
                                        ArrayList<RightClassInChildBean> childBeans = new ArrayList<>();
                                        for (int j = 0; j < list.length(); j++) {
                                            RightClassInChildBean childBean = new RightClassInChildBean();
                                            int childId = list.getJSONObject(j).getInt("id");
                                            String childName = list.getJSONObject(j).getString("name");
                                            String childLogo = list.getJSONObject(j).getString("logo");
                                            childBean.setClassName(childName);
                                            childBean.setId(childId);
                                            childBean.setImgUrl(childLogo);
                                            childBean.setChecked(false);
                                            if (childBean.getClassName() != null && !TextUtils.isEmpty(childBean.getClassName())) {
                                                childBeans.add(childBean);
                                            }
                                        }
                                        rightClassBean.setChildBeans(childBeans);
                                    }
                                }
                                branList.addAll(rightClassBean.getChildBeans());
                                initBrand();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClassDataLoaded(String result) {
        if (result != null) {
            Log.e("TAG", "右边分类:" + result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                int code = jsonObject.getInt("code");
                int state = jsonObject.getInt("state");
                if (code == 0 && state == 0) {
                    if (jsonObject.has("data")) {
                        JSONArray data = jsonObject.getJSONArray("data");
                        if (data != null && data.length() != 0) {
                            rightClassData.clear();
                            for (int i = 0; i < data.length(); i++) {
                                RightClassInChildBean rightClassInChildBean = new RightClassInChildBean();
                                rightClassInChildBean.setChecked(false);
                                rightClassInChildBean.setClassName(data.getJSONObject(i).getString("name"));
                                rightClassInChildBean.setId(data.getJSONObject(i).getInt("id"));
                                rightClassData.add(rightClassInChildBean);
                            }
                            initClassData();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void initPriceData() {
        llPriceContainer.removeAllViews();
        for (int i = 0; i < priceData.size(); i++) {
            final int index = i;
            final View price = LayoutInflater.from(this).inflate(R.layout.text_tag_view, null);
            TextView tag = price.findViewById(R.id.tv_tag_name);
            if (priceData.get(i).isChecked()) {
                tag.setTextColor(Color.parseColor("#ff0000"));
                tag.setBackgroundResource(R.drawable.shape_of_goods_spe_selected);
            } else {
                tag.setTextColor(Color.parseColor("#aaaaaa"));
                tag.setBackgroundResource(R.drawable.shape_of_verify_code_btn);
            }
            tag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notifyAllPriceUnChecked();
                    priceData.get(index).setChecked(true);
                    currentPriceId = priceData.get(index).getId();
                    initPriceData();
                    etMinPrice.setText("");
                    etMaxPrice.setText("");
                }
            });
            tag.setText(priceData.get(i).getPriceSection());
            llPriceContainer.addView(price);
        }
    }

    private void initClassData() {
        llClassContainer.removeAllViews();
        if (rightClassData.size() != 0) {
            for (int i = 0; i < rightClassData.size(); i++) {
                final int index = i;
                View classView = LayoutInflater.from(this).inflate(R.layout.text_tag_view, null);
                TextView classes = classView.findViewById(R.id.tv_tag_name);
                if (rightClassData.get(i).isChecked()) {
                    classes.setTextColor(Color.parseColor("#ff0000"));
                    classes.setBackgroundResource(R.drawable.shape_of_goods_spe_selected);
                } else {
                    classes.setTextColor(Color.parseColor("#aaaaaa"));
                    classes.setBackgroundResource(R.drawable.shape_of_verify_code_btn);
                }
                classes.setText(rightClassData.get(i).getClassName());
                classes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        notifyAllClassUnChecked();
                        rightClassData.get(index).setChecked(true);
                        currentClassId = rightClassData.get(index).getId();
                        initClassData();
                    }
                });
                llClassContainer.addView(classView);
            }
        }
    }

    private void notifyAllClassUnChecked() {
        for (int i = 0; i < rightClassData.size(); i++) {
            rightClassData.get(i).setChecked(false);
        }
    }

    private void notifyAllPriceUnChecked() {
        for (int i = 0; i < priceData.size(); i++) {
            priceData.get(i).setChecked(false);
        }
    }

    private void notifyAllUnChecked() {
        for (int i = 0; i < branList.size(); i++) {
            branList.get(i).setChecked(false);
        }
    }

    private void initBrand() {
        llBrandsContainer.removeAllViews();
        if (branList.size() != 0) {
            for (int i = 0; i < branList.size(); i++) {
                final int index = i;
                final View brandView = LayoutInflater.from(this).inflate(R.layout.text_tag_view, null);

                TextView brand = brandView.findViewById(R.id.tv_tag_name);
                if (branList.get(i).isChecked()) {
                    brand.setTextColor(Color.parseColor("#ff0000"));
                    brand.setBackgroundResource(R.drawable.shape_of_goods_spe_selected);
                } else {
                    brand.setTextColor(Color.parseColor("#aaaaaa"));
                    brand.setBackgroundResource(R.drawable.shape_of_verify_code_btn);
                }
                brand.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        notifyAllUnChecked();
                        branList.get(index).setChecked(true);
                        currentBrandId = branList.get(index).getId();
                        initBrand();
                    }
                });
                brand.setText(branList.get(i).getClassName());
                llBrandsContainer.addView(brandView);
            }
        }

    }
}
