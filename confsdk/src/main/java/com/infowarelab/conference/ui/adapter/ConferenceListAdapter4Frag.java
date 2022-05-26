package com.infowarelab.conference.ui.adapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

////import org.apache.log4j.Logger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infowarelab.hongshantongphone.R;
import com.infowarelab.conference.ui.activity.preconf.ActHome;
import com.infowarelab.conference.ui.activity.preconf.BaseFragmentActivity;
import com.infowarelab.conference.ui.activity.preconf.fragment.FragJoin;
import com.infowarelabsdk.conference.domain.ConferenceBean;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;

/**
 * @author joe.xiao
 * @Date 2013-9-10下午1:46:13
 * @Email joe.xiao@infowarelab.com
 */
public class ConferenceListAdapter4Frag extends BaseAdapter {

    private BaseFragmentActivity mContext;
    private FragJoin fragJoin;

    //private static final Logger log = Logger.getLogger(ConferenceListAdapter4Frag.class);

    /**
     * 正在进行的会议列表
     */
//	private List<ConferenceBean> progressList;

    /**
     * 未开始会议列表
     */
//	private List<ConferenceBean> noStartConfList;

    /**
     * 全部的会议列表
     */
    private List<ConferenceBean> confList;

    private int selectItem;
    private String userName;

    private LayoutInflater mInflater;
    private String[] titles;
    private String pre;
    private String end;

    public ConferenceListAdapter4Frag(Context context, FragJoin fragJoin, List<ConferenceBean> confs, int defaultSelected) {
        this.mContext = (BaseFragmentActivity) context;
        this.fragJoin = fragJoin;
        this.selectItem = defaultSelected;
        titles = new String[]{mContext.getResources().getString(R.string.myconf),
                mContext.getResources().getString(R.string.publicconf)};
        mInflater = LayoutInflater.from(context);

        setAdapterData(confs);
    }

    /**
     * 设置Adapter的数据内容
     */
    public void setAdapterData(List<ConferenceBean> confs) {
        if (confList != null) {
            confList = null;
        }
        confList = confs;
        userName = FileUtil.readSharedPreferences(mContext, Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME);
//		progressList = confs.get(Config.PROGRESSCONFS);
//		noStartConfList = confs.get(Config.CONFS);
//		confList = new ArrayList<ConferenceBean>();
//		confList.addAll(noStartConfList);
//		confList.addAll(0, progressList);
//		//log.info("progressList=" + progressList.size());

    }

    /**
     * @param selectItem
     */
    public void setSelectItem(int selectItem) {
        this.selectItem = selectItem;
    }

    @Override
    public int getCount() {
        return confList.size();
    }

    @Override
    public Object getItem(int position) {
        return confList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.a6_preconf_join_page_list_item, null);
            mViewHolder = new ViewHolder();
            mViewHolder.llType = (LinearLayout) convertView.findViewById(R.id.llmeetingType);
            mViewHolder.meetingType = (TextView) convertView.findViewById(R.id.meetingType);
            mViewHolder.meetingTitle = (TextView) convertView.findViewById(R.id.meetingTitle);
//			mViewHolder.meetingNumber = (TextView) convertView.findViewById(R.id.meetingNumber);
            mViewHolder.meetingHostName = (TextView) convertView.findViewById(R.id.meetingHostName);
            mViewHolder.meetingStartTime = (TextView) convertView.findViewById(R.id.meetingStartTime);
            mViewHolder.meetingState = (ImageView) convertView.findViewById(R.id.meetingState);
//			mViewHolder.startPublcMeeting = (ImageButton)convertView.findViewById(R.id.startMeeting_public);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        setItemClick(convertView, (position + 1));
//		setItemClick(mViewHolder.startMeeting, (position + 1));
        contaionConfs(mViewHolder, position);
        Date starDate = confList.get(position).getStartDate();
        mViewHolder.meetingTitle.setText(confList.get(position).getName());
        mViewHolder.meetingTitle.getPaint().setFakeBoldText(true);

//		pre = confList.get(position).getId().substring(0, 4);
//		end = confList.get(position).getId().substring(4);
//		mViewHolder.meetingNumber.setText(mContext.getResources().getString(R.string.more_id) + pre + " " + end);
        if (confList.get(position).getHostDisplayName() == null ||
                confList.get(position).getHostDisplayName().trim().equals("")) {
            mViewHolder.meetingHostName.setText(confList.get(position).getHostName()
//					+ mContext.getResources().getString(R.string.host)
            );
        } else {
            mViewHolder.meetingHostName.setText(confList.get(position).getHostDisplayName()
//					+ mContext.getResources().getString(R.string.host)
            );
        }

        mViewHolder.meetingStartTime.setText(dateToString(confList.get(position).getStartDate()));
//		//log.info("progresslist length:" + progressList.size() + " position=" + position + " conflist length="
//				+ confList.size());

        return convertView;
    }

    private void setItemClick(View convertView, final int position) {
        convertView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                fragJoin.setJoin(position);

            }
        });

    }

    private class ViewHolder {
        private LinearLayout llType;
        private TextView meetingType, meetingTitle, meetingHostName, meetingStartTime;
        private ImageView meetingState;
        private ImageButton startMeeting;
        private ImageButton startPublcMeeting;
    }

    public void setTitleText(View mHeader, int firstVisiblePosition) {
        String title = "";
        if (fragJoin.getmConferencePagerListView().getMyConfsCount() == 0) {
            title = titles[1];
        } else {
            if (firstVisiblePosition > fragJoin.getmConferencePagerListView().getMyConfsCount()) {
                title = titles[1];
            } else {
                title = titles[0];
            }
        }

        TextView sectionHeader = (TextView) mHeader;
        sectionHeader.setText(title);
    }

    public int getTitleState(int position) {
        if (position <= 0 || getCount() == 0) {
            return 0;
        }

        if (ActHome.isLogin && fragJoin.getmConferencePagerListView().getMyConfsCount() != 0
                && position == fragJoin.getmConferencePagerListView().getMyConfsCount()) {
            return 2;
        }
        return 1;
    }

//	private void setClick(View view, int position){
    //view.setOnClickListener(this)
//	}

    /**
     * 进行中会议存在于所有会议信息
     *
     * @param viewHolder
     * @param position
     */
    private void contaionConfs(ViewHolder viewHolder, int position) {
//		if(position<progressList.size()){

//		}
        setListTitle(viewHolder, position);
//		setStartMeetingButton(viewHolder, position);
        setConfStatus(viewHolder, position);

    }

//	private void setStartMeetingButton(ViewHolder viewHolder, int position) {
//		viewHolder.startMeeting.setVisibility(View.GONE);
//		viewHolder.startPublcMeeting.setVisibility(View.GONE);
//		if(ActHome.isLogin){
//			if(confList.get(position).getHostName().equals(userName)){
//				if(position < fragJoin.getmConferencePagerListView().getMyConfsCount() && confList.get(position).getStatus().equals("0")){
//					viewHolder.startMeeting.setVisibility(View.VISIBLE);
//					confList.get(position).setNeedStartConf(1);
//				}else{
//					viewHolder.startPublcMeeting.setVisibility(View.VISIBLE);
//				}
//			}
//		}else{
//			if(position < fragJoin.getmConferencePagerListView().getMyConfsCount()){
//				viewHolder.startPublcMeeting.setVisibility(View.VISIBLE);
//			}
//		}
//	}

    private void setConfStatus(ViewHolder viewHolder, int position) {
        ConferenceBean conferenceBean = confList.get(position);
        int id = 0;
        //log.info("status = "+conferenceBean.getStatus()+" type = "+conferenceBean.getType());
        if (conferenceBean.getStatus().equals("1")) {
            if (conferenceBean.getType().equals(Config.MEETING)) {
//				id = R.drawable.meetinglist_status_start;
                id = R.drawable.a6_icon_join_list_started;
            } else {
//				id = R.drawable.meetinglistlive_status_start;
                id = R.drawable.a6_icon_join_list_started;
            }
        } else {
            if (conferenceBean.getType().equals(Config.MEETING)) {
//				id = R.drawable.meetinglist_status_nostarted;
                id = R.drawable.a6_icon_join_list_nostarted;
            } else {
//				id = R.drawable.meetinglistlive_status_nostarted;
                id = R.drawable.a6_icon_join_list_nostarted;
            }
        }
        viewHolder.meetingState.setImageResource(id);
    }

    private void setListTitle(ViewHolder viewHolder, int position) {
        if (ActHome.isLogin) {
            if (position == fragJoin.getmConferencePagerListView().getMyConfsCount()) {
//				viewHolder.meetingType.setVisibility(View.VISIBLE);
                viewHolder.llType.setVisibility(View.VISIBLE);
                viewHolder.meetingType.setText(titles[1]);
            } else if (position == 0) {
//				viewHolder.meetingType.setVisibility(View.VISIBLE);
                viewHolder.llType.setVisibility(View.VISIBLE);
                viewHolder.meetingType.setText(titles[0]);
            } else {
//				viewHolder.meetingType.setVisibility(View.GONE);
                viewHolder.llType.setVisibility(View.GONE);
            }
        } else {
            if (position == 0) {
//				viewHolder.meetingType.setVisibility(View.VISIBLE);
                viewHolder.llType.setVisibility(View.VISIBLE);
                viewHolder.meetingType.setText(titles[1]);
                //log.info("position:"+position);
            } else {
//				viewHolder.meetingType.setVisibility(View.GONE);
                viewHolder.llType.setVisibility(View.GONE);
            }
        }
    }

    private String dateToString(Date startDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        try {
            return format.format(startDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
