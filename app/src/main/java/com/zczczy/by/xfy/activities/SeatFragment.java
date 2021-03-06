package com.zczczy.by.xfy.activities;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.zczczy.by.xfy.MyApplication;
import com.zczczy.by.xfy.R;

import com.zczczy.by.xfy.adapters.FireInfoAdapter;
import com.zczczy.by.xfy.adapters.MyBaseAdapter;
import com.zczczy.by.xfy.dao.MessageDao;
import com.zczczy.by.xfy.model.BaseModelJson;
import com.zczczy.by.xfy.model.FireInfo;
import com.zczczy.by.xfy.model.UserLogin;
import com.zczczy.by.xfy.prefs.MyPrefs_;
import com.zczczy.by.xfy.pullableview.PullToRefreshLayout;
import com.zczczy.by.xfy.pullableview.PullableListView;
import com.zczczy.by.xfy.rest.MyErrorHandler;
import com.zczczy.by.xfy.rest.MyRestClient;
import com.zczczy.by.xfy.tools.AndroidTool;
import com.zczczy.by.xfy.viewgroup.BadgeView;
import com.zczczy.by.xfy.viewgroup.MyAlertDialog;
import com.zczczy.by.xfy.viewgroup.MyTitleLeftView;

import com.zczczy.by.xfy.viewgroup.NoDataView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.rest.RestService;
import org.androidannotations.annotations.sharedpreferences.Pref;

/**
 * Created by zczczy on 2015/10/21.
 * 故障座席
 */
@EFragment(R.layout.fireseat_layout)
public class SeatFragment extends  BaseFragment  implements PullToRefreshLayout.OnRefreshListener {
    @ViewById
    MyTitleLeftView title;
    @ViewById
    RadioGroup rg_rg;
    @ViewById
    RadioButton txt_nosolve,txt_solve;
    @ViewById
    LinearLayout ll_nosolve,ll_solve,img_back;

    @StringRes
    String txt_seat;
    @ViewById
    NoDataView no_data;

    @ViewById
    PullToRefreshLayout refresh_view;

    @ViewById
    PullableListView lv_list;

    @RestService
    MyRestClient myRestClient;

    @StringRes
    String no_net;
    @Bean
    MessageDao messageDao;

    @Pref
    MyPrefs_ pre;
    String flag ="0";
    int pageIndex=1;

    @Bean(FireInfoAdapter.class)
    MyBaseAdapter<FireInfo> myAdapter;
    @ViewById
    RelativeLayout ps_msg;
    @Bean
    MyErrorHandler myErrorHandler;

    BadgeView badgeView ;
    @AfterInject
    void afterInject() {
        myRestClient.setRestErrorHandler(myErrorHandler);
    }

    @AfterViews
    void afterView() {
        title.setListener(this);
        title.setTitle(txt_seat);
        refresh_view.setOnRefreshListener(this);
        lv_list.setAdapter(myAdapter);
        badgeView = new BadgeView(this.getActivity(),img_back);
        badgeView.setBadgePosition(BadgeView.POSITION_TOP_LEFT);
    }


    //个人中心
    @Click
    void img_back() {
        if(isNetworkAvailable(this.getActivity())){
            PersonalCenterActivity_.intent(this).start();}
        else {
            Toast.makeText(getActivity(), no_net, Toast.LENGTH_SHORT).show();
        }
    }

    //列表点击进入详情
    @ItemClick
    void  lv_list(FireInfo fireInfo ){
        if(isNetworkAvailable(this.getActivity())){
            FaultDetailActivity_.intent(getActivity()).A_ID(fireInfo.A_ID).start();
        }else{
            Toast.makeText(this.getActivity(), no_net, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden){
            changeBB();
            String tonken = pre.token().get();
            if (tonken == null || tonken == "" || tonken.isEmpty()) {
                myAdapter.getList().clear();
                myAdapter.notifyDataSetChanged();
                ((MainActivity)getActivity()).setTabSelection(0);
            } else {
                myAdapter.getMoreData(1, 10, pre.C_name().get(),"FDE",flag);
            }
        }
    }

    @Override
    public void changeBB() {
        badgeView.hide();
        long i =messageDao.getStatus();
        if (i>0){
            badgeView.setText(i+"");
            badgeView.setTextSize(10);
            badgeView.show();
        }
    }

    //未处理

    @CheckedChange
    void txt_nosolve(boolean checked){
        if(isNetworkAvailable(this.getActivity())) {
            String tonken = pre.token().get();
            if (tonken != null && tonken != "" && !tonken.isEmpty()){
                pageIndex = 1;
                if (checked) {
                    flag = "0";
                    myAdapter.getMoreData(pageIndex, 10, pre.C_name().get(), "FDE", flag);
                    ll_nosolve.setVisibility(View.VISIBLE);

                } else {
                    flag = "1";
                    ll_nosolve.setVisibility(View.GONE);
                }
            }
        }else {
            Toast.makeText(this.getActivity(), no_net, Toast.LENGTH_SHORT).show();
        }
    }
    //已处理

    @CheckedChange
    void txt_solve(boolean checked){
        if(isNetworkAvailable(this.getActivity())) {
            String tonken = pre.token().get();
            if (tonken != null && tonken != "" && !tonken.isEmpty()){
                pageIndex = 1;
                if (checked) {
                    flag = "1";
                    myAdapter.getMoreData(pageIndex, 10, pre.C_name().get(), "FDE", flag);
                    ll_solve.setVisibility(View.VISIBLE);

                } else {
                    flag = "0";
                    ll_solve.setVisibility(View.GONE);
                }
            }
        }else {
            Toast.makeText(this.getActivity(), no_net, Toast.LENGTH_SHORT).show();
        }

    }



    @Override
    public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
        String tonken = pre.token().get();
        if (tonken != null && tonken != "" && !tonken.isEmpty()){
            pageIndex=1;
            myAdapter.getMoreData(pageIndex,10,pre.C_name().get(),"FDE",flag);}
    }

    @Override
    public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
        if(myAdapter.getList().size()>=myAdapter.getTotal()){
            AndroidTool.showToast(this.getActivity(), "没有更多数据了");
            changeLoadTime();
        }else{
            pageIndex++;
            myAdapter.getMoreData(pageIndex,10,pre.C_name().get(),"FDE",flag);
        }

    }


    public void changeLoadTime(){
        if (pageIndex == 1) {
            refresh_view.refreshFinish(PullToRefreshLayout.SUCCEED);
        } else {
            refresh_view.loadmoreFinish(PullToRefreshLayout.SUCCEED);
        }
        if(myAdapter.getCount()==0){
            no_data.setVisibility(View.VISIBLE);
        }else{
            no_data.setVisibility(View.GONE);
        }
        AndroidTool.dismissLoadDialog();
    }


}
