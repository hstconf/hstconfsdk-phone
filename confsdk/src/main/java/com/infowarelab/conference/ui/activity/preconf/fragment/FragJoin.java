package com.infowarelab.conference.ui.activity.preconf.fragment;


import java.util.LinkedList;
import java.util.List;

import com.infowarelab.hongshantongphone.R;
import com.infowarelab.conference.ui.activity.preconf.BaseFragment;
import com.infowarelab.conference.ui.activity.preconf.BaseFragmentActivity;
import com.infowarelab.conference.ui.activity.preconf.view.ConferencePagerListView;
import com.infowarelab.conference.ui.activity.preconf.view.ConferencePagerNumber;
import com.infowarelab.conference.ui.adapter.ConferencePagerAdapter;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.domain.ConferenceBean;

import android.os.Bundle;
import android.os.Message;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class FragJoin extends BaseFragment implements OnClickListener {
    private View joinView;
    private ViewPager mConferencePager;
    private List<View> views;
    private ConferencePagerAdapter mPagerAdapter;
    private ConferencePagerListView mConferencePagerListView;
    private ConferencePagerNumber mConferencePagerNumber;

    private boolean isEnterFromItem = false;
    private onSwitchPageListener switchPageListener;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        joinView = inflater.inflate(R.layout.a6_preconf_join, container, false);
        initView();
        return joinView;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
    }

    private void initView() {
        mConferencePager = (ViewPager) joinView.findViewById(R.id.frag_join_vp);
        initPageView();
    }

    private void initPageView() {
        mConferencePagerListView = new ConferencePagerListView((BaseFragmentActivity) getActivity(), this);
        mConferencePagerNumber = new ConferencePagerNumber((BaseFragmentActivity) getActivity(), this);
        views = new LinkedList<View>();
        views.add(mConferencePagerListView.getNewView());
        views.add(mConferencePagerNumber.getNewView());
        mPagerAdapter = new ConferencePagerAdapter(views);
        mConferencePager.setAdapter(mPagerAdapter);
        mConferencePager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int postion) {
                checkPosition(postion);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub
            }
        });

        CommonFactory commonFactory = CommonFactory.getInstance();
        ConferenceCommonImpl conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();
        if (null != conferenceCommon) {
            if (null != conferenceCommon.mConfId) {
                switchPage(1);
            }
        }

    }

    public void switchPage(int postion) {
//		if(index == 1){
//			mConferencePagerNumber.resumeLayout();
//		}
        mConferencePager.setCurrentItem(postion);
    }

    public void enterFromItem(ConferenceBean conferenceBean) {
        isEnterFromItem = true;
        mConferencePager.setCurrentItem(1); //上次改错了
        Message msg = mConferencePagerNumber.handler.obtainMessage(100, conferenceBean);
        mConferencePagerNumber.handler.sendMessage(msg);
    }

    public ConferenceBean getConfBean(String condId) {
        CommonFactory commonFactory = CommonFactory.getInstance();
        ConferenceCommonImpl conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();
        if (null != conferenceCommon) {
            return conferenceCommon.getConfById(condId);
        }
        return null;
        //return mConferencePagerListView.getConferenceById(condId);
    }

    public void setJoin(int position) {
        mConferencePagerListView.setJoin(position);
    }

    private void checkPosition(int postion) {
        if (postion == 0) {
            if (getActivity().getCurrentFocus() != null) {
                //切换到会议列表时总是关闭输入法
//				InputMethodManager inputMethodManager = (InputMethodManager) 
//						getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//				inputMethodManager.hideSoftInputFromWindow(
//						getActivity().getCurrentFocus().getWindowToken(),
//				InputMethodManager.HIDE_NOT_ALWAYS);
            }
            mConferencePagerListView.refreshAdapter();
        } else if (postion == 1) {
            mConferencePagerNumber.resumeLayout();
        }
        doSelect(postion);
    }

    public ConferencePagerListView getmConferencePagerListView() {
        return mConferencePagerListView;
    }

    public void onMyBackPressed() {
        if (mConferencePager.getCurrentItem() == 1 && isEnterFromItem) {
            mConferencePager.setCurrentItem(0);
            isEnterFromItem = false;
            return;
        }
    }

    public int getCurrentItem() {
        return mConferencePager.getCurrentItem();
    }

    public void setCurrentItem(int postion) {
        mConferencePager.setCurrentItem(postion);
    }

    public boolean isEnterFromItem() {
        return isEnterFromItem;
    }

    public void setEnterFromItem(boolean isEnterFromItem) {
        this.isEnterFromItem = isEnterFromItem;
    }

    public interface onSwitchPageListener {
        public void doSelect(int postion);
    }

    public void setOnSwitchPageListener(onSwitchPageListener listener) {
        this.switchPageListener = listener;
    }

    private void doSelect(int selectId) {
        if (switchPageListener != null) {
            switchPageListener.doSelect(selectId);
        }
    }

    public void showErrMsgList() {
        if (mConferencePagerListView != null) {
//			mConferencePagerListView.
        }
    }

    public void showErrMsgNumber(int id) {
        if (mConferencePagerNumber != null) {
            mConferencePagerNumber.showErrMsg(id);
        }
    }

    public void showErrMsgNumber(String s) {
        if (mConferencePagerNumber != null) {
            mConferencePagerNumber.showErrMsg(s);
        }
    }

    public void showLoading() {
        super.showLoading();
    }

    public void hideLoading() {
        super.hideLoading();
    }

    public void hideInput(View v) {
        super.hideInput(v);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        // TODO Auto-generated method stub
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (mConferencePagerListView != null) mConferencePagerListView.refreshAdapter();
            if (mConferencePagerNumber != null) mConferencePagerNumber.resumeLayout();
        }
    }

    public boolean isFromItem() {
        if (mConferencePagerNumber != null) {
            return mConferencePagerNumber.isFromItem();
        } else {
            return false;
        }
    }
}
