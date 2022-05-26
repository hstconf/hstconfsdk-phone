package com.infowarelab.conference.ui.activity.preconf.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infowarelab.conference.ConferenceApplication;
import com.infowarelab.conference.localDataCommon.impl.ContactDataCommonImpl;
import com.infowarelab.conference.ui.activity.inconf.ConferenceActivity;
import com.infowarelab.conference.ui.activity.preconf.ActOrganization;
import com.infowarelab.conference.ui.activity.preconf.BaseFragment;
import com.infowarelab.conference.ui.activity.preconf.LoginActivity;
import com.infowarelab.conference.ui.view.PopSpinnerDuration;
import com.infowarelab.hongshantongphone.R;
import com.infowarelabsdk.conference.common.CommonFactory;
import com.infowarelabsdk.conference.common.impl.AudioCommonImpl;
import com.infowarelabsdk.conference.common.impl.ConferenceCommonImpl;
import com.infowarelabsdk.conference.confctrl.ConferenceCommon;
import com.infowarelabsdk.conference.domain.ConferenceBean;
import com.infowarelabsdk.conference.domain.LoginBean;
import com.infowarelabsdk.conference.transfer.Config;
import com.infowarelabsdk.conference.util.Constants;
import com.infowarelabsdk.conference.util.FileUtil;
import com.infowarelabsdk.conference.util.StringUtil;

import java.util.Date;

public class FragCreate extends BaseFragment implements OnClickListener, OnFocusChangeListener {
    private View createView;

    //private Logger log = Logger.getLogger(getClass().getName());

    private LinearLayout ll1, ll2, ll4;
    private EditText et1, et2;
    private Button btnCreate, btnOrg;
    private TextView tvErrMsg, tvDuration;

    //	private View view;
//	private Dialog createDialog;
    private View focusView;
    private AlertDialog.Builder alertDialog;

    private Intent createIntent;
    public SharedPreferences preferences;
    private CommonFactory commonFactory = CommonFactory.getInstance();
    private ConferenceCommonImpl conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();
    private Config config;
    private final ConferenceBean conferenceBean = new ConferenceBean();
    private LoginBean login;

    private String confId;
    private String result = "";
    public String topic = "";
    public String password = "";

    protected static final int INIT_SDK = 101;
    protected static final int INIT_SDK_FAILED = 102;
    protected static final int CREATECONF_ERROR = -1;
    protected static final int IDENTITY_FAILED = -2;
    protected static final int CONF_CONFLICT = -5;
    protected static final int BEYOUND_MAXNUM = -7;
    protected static final int PASSWORD_NULL = -9;
    protected static final int CONF_OVER = -10;
    protected static final int NOT_HOST = -16;
    protected static final int BYOUND_STARTTIME = -17;
    protected static final int CHECK_SITE = -18;
    protected static final int SHOWALTER = 10;
    protected static final int AUTO_START = 200;

    public Handler createHandler;

    private String attIds = "";
    private String attNames = "";
    private String attEmails = "";

    private int duration = 120;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        createView = inflater.inflate(R.layout.a6_preconf_create, container, false);

        commonFactory = CommonFactory.getInstance();
        conferenceCommon = (ConferenceCommonImpl) commonFactory.getConferenceCommon();

        initView();
        ContactDataCommonImpl.preStartIndex = -1;
        ContactDataCommonImpl.preEndIndex = -1;
        initCreateHandler();
        if (conferenceCommon == null) {
            //log.error("conferenceCommon is null");
        }
        if (createHandler == null) {
            //log.error("createHandler is null");
        }
        conferenceCommon.setHandler(createHandler);
        String realName = FileUtil.readSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES, Constants.LOGIN_EXNAME);
        String loginName = FileUtil.readSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME);
        et1.setHint(!TextUtils.isEmpty(realName) ? realName + getResources().getString(R.string.preconf_create_topic_default) :
                loginName + getResources().getString(R.string.preconf_create_topic_default));

        if (conferenceCommon.mCreateConf) {
            if (false == FileUtil.readSharedPreferencesBoolean(getActivity(),
                    Constants.SHARED_PREFERENCES, Constants.INVOKED))

                createHandler.sendEmptyMessage(AUTO_START);

            FileUtil.saveSharedPreferences(getActivity(),
                    Constants.SHARED_PREFERENCES, Constants.INVOKED, true);
        }

        return createView;
    }

    private void initView() {
        ll1 = (LinearLayout) createView.findViewById(R.id.view_frag_create_ll_1);
        ll2 = (LinearLayout) createView.findViewById(R.id.view_frag_create_ll_2);
        ll4 = (LinearLayout) createView.findViewById(R.id.view_frag_create_ll_4);
        et1 = (EditText) createView.findViewById(R.id.view_frag_create_et_1);
        et2 = (EditText) createView.findViewById(R.id.view_frag_create_et_2);
        btnCreate = (Button) createView.findViewById(R.id.view_frag_create_btn);
        btnOrg = (Button) createView.findViewById(R.id.view_frag_create_btn_org);
        tvErrMsg = (TextView) createView.findViewById(R.id.view_frag_create_tv_2);
        tvDuration = (TextView) createView.findViewById(R.id.view_frag_create_tv_4);

//		view = LayoutInflater.from(getActivity()).inflate(R.layout.preconf_create_dialog, null);
//		createDialog = new Dialog(getActivity(), R.style.styleSiteCheckDialog);
//		createDialog.setContentView(view);

        et1.setText(topic);
        et2.setText(password);
        tvErrMsg.setVisibility(View.GONE);

        btnCreate.setOnClickListener(this);
        btnOrg.setOnClickListener(this);
        ll1.setOnClickListener(this);
        ll2.setOnClickListener(this);
        ll4.setOnClickListener(this);
        et1.setOnFocusChangeListener(this);
        et2.setOnFocusChangeListener(this);
    }

    private void initCreateHandler() {

        createHandler = new Handler() {
            public void handleMessage(Message msg) {
                Log.d("InfowareLab.Debug", "what = " + msg.what);
                switch (msg.what) {
                    case ConferenceCommon.RESULT_SUCCESS:
                        Log.d("InfowareLab.Debug", "join success");
                        createIntent = new Intent(getActivity(), ConferenceActivity.class);
                        startActivity(createIntent);
                        getActivity().finish();
                        ((AudioCommonImpl) commonFactory.getAudioCommon()).onOpenAudioConfirm(true);
                        break;
                    case INIT_SDK:
                        Log.d("InfowareLab.Debug", "initSDK success");
                        break;
                    case INIT_SDK_FAILED:
                        showLongToast(R.string.initSDKFailed);
                        showErrMsg(R.string.initSDKFailed);
                        break;
                    case CONF_CONFLICT:
                        showLongToast(R.string.confConflict);
                        showErrMsg(R.string.confConflict);
                        break;
                    case CREATECONF_ERROR:
                        showLongToast(R.string.preconf_create_error);
                        showErrMsg(R.string.preconf_create_error);
                        break;
                    case IDENTITY_FAILED:
                        showLongToast(R.string.preconf_create_error_2);
                        showErrMsg(R.string.preconf_create_error_2);
                        break;
                    case BEYOUND_MAXNUM:
                        showLongToast(R.string.preconf_create_error_7);
                        showErrMsg(R.string.preconf_create_error_2);
                        break;
                    case PASSWORD_NULL:
                        showLongToast(R.string.preconf_create_error_9);
                        showErrMsg(R.string.preconf_create_error_9);
                        break;
                    case CONF_OVER:
                        showLongToast(R.string.preconf_create_error_10);
                        showErrMsg(R.string.preconf_create_error_10);
                        break;
                    case NOT_HOST:
                        showAlertDialog();
                        showLongToast(R.string.preconf_create_error_16);
                        break;
                    case BYOUND_STARTTIME:
                        showLongToast(R.string.preconf_create_error_17);
                        showErrMsg(R.string.preconf_create_error_17);
                        break;
                    case CHECK_SITE:
                        hideLoading();
                        showLongToast(R.string.site_error);
                        showErrMsg(R.string.site_error);
                        break;
                    case SHOWALTER:
                        hideLoading();
                        showAlertDialog();
                        break;
                    case ConferenceCommon.LEAVE:
                        break;
                    case AUTO_START:
                        btnCreate.performClick();
                        break;
                    default:
                        //showLongToast(R.string.preconf_notknown_error);
//					showErrMsg(R.string.preconf_notknown_error);
                        break;
                }
            }

            ;
        };
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.view_frag_create_btn) {
            hideInput(v);
            if (et1.getText().toString().length() <= 0) {
                String topic = FileUtil.readSharedPreferences(getActivity(),
                        Constants.SHARED_PREFERENCES, Constants.CONF_TOPIC);
                if (null != topic && topic.length() > 0)
                    et1.setText(topic);
            }

            if (et2.getText().toString().length() <= 0) {
                String confPwd = FileUtil.readSharedPreferences(getActivity(),
                        Constants.SHARED_PREFERENCES, Constants.CONF_PWD);
                if (null != confPwd && confPwd.length() > 0)
                    et2.setText(confPwd);
            }

            String confPwd = FileUtil.readSharedPreferences(getActivity(),
                    Constants.SHARED_PREFERENCES, Constants.CONF_PWD);
            if (null != topic && topic.length() > 0)

                if (!StringUtil.checkInput(et1.getText().toString(), Constants.PATTERN)) {
                    showShortToast(R.string.preconf_create_topicerror);
                    showErrMsg(R.string.preconf_create_topicerror);
                    return;
                }

            showLoading();
            new Thread() {
                @Override
                public void run() {
                    String url = FileUtil.readSharedPreferences(getActivity(),
                            Constants.SHARED_PREFERENCES, Constants.SITE);
                    Config.SiteName = FileUtil.readSharedPreferences(getActivity(),
                            Constants.SHARED_PREFERENCES, Constants.SITE_NAME);

                    boolean bCreateConf = FileUtil.readSharedPreferencesBoolean(getActivity(),
                            Constants.SHARED_PREFERENCES, Constants.CREATE_CONF);

                    if (Config.SiteName.equals("") || Config.SiteName == null) {
                        createHandler.sendEmptyMessage(CHECK_SITE);
                        return;
                    }

                    String loginName = FileUtil.readSharedPreferences(
                            getActivity(), Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME);
                    String loginPassword = FileUtil.readSharedPreferences(getActivity(),
                            Constants.SHARED_PREFERENCES, Constants.LOGIN_PASS);

                    if (loginName != null && loginName.length() > 0) {
                        login = new LoginBean(null, loginName, loginPassword);
                        login = ((ConferenceCommonImpl) CommonFactory.getInstance().getConferenceCommon())
                                .checkUser(login);//更新登录用户的信息

                        if (login == null) {
                            //当无网络或站点不可用时会出现login值为空的情况
                            if (bCreateConf) {
                                //showShortToast(R.string.preconf_create_error_2);
                                //showErrMsg(R.string.preconf_create_error_2);
                                createHandler.sendEmptyMessage(IDENTITY_FAILED);
                                return;
                            }
                        }

                        //Login successfully ------------>

                        if (login.getNickname() != null && !login.getNickname().equals("")) {
                            FileUtil.saveSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES,
                                    Constants.LOGIN_NICKNAME, login.getNickname());
                        }

                        if (login.getRealname() != null && !login.getRealname().equals("")) {
                            FileUtil.saveSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES,
                                    Constants.LOGIN_EXNAME, login.getRealname());
                        } else if (login.getNickname() != null && !login.getNickname().equals("")) {

                            FileUtil.saveSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES,
                                    Constants.LOGIN_EXNAME, login.getNickname());
                        }

                        FileUtil.saveSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES,
                                Constants.CONF_LIST_TYPE, 0);

                        FileUtil.saveSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES,
                                Constants.USER_ID, login.getUid());

                        //保存登录用户的角色权限信息，并通过匹配字符串检测是否有开会权限
                        FileUtil.saveSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES,
                                Constants.LOGIN_ROLE, login.getCreateConfRole());

                        //-------------------------------------------------------------
                    }

                    String confId = FileUtil.readSharedPreferences(
                            getActivity(), Constants.SHARED_PREFERENCES, Constants.CONF_ID);

                    if (null == confId || confId.length() <= 0)
                        confId = "0";

                    if (bCreateConf && FileUtil.readSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES,
                            Constants.LOGIN_ROLE).contains("meeting.role.init.host;")) {
                        confId = sendConfRequire(true, confId);
                        Handler confIdHandler = ConferenceApplication.getConferenceApp().mConfIdHandler;
                        if (confIdHandler != null) {
                            Message message = confIdHandler.obtainMessage();
                            message.what = 1000;
                            message.obj = confId;
                            confIdHandler.sendMessage(message);
                        }
                        ;

                    } else if (!bCreateConf) {
                        sendConfRequire(false, confId);
                    } else {
                        createHandler.sendEmptyMessage(SHOWALTER);
                    }
                }
            }.start();
        } else if (id == R.id.view_frag_create_btn_org) {
            setAtts("", "", "");
            Intent in = new Intent(getActivity(), ActOrganization.class);
            getActivity().startActivityForResult(in, ActOrganization.REQ_ORG);
        } else if (id == R.id.view_frag_create_et_1) {
            et1.requestFocus();
        } else if (id == R.id.view_frag_create_et_2) {
            et2.requestFocus();
        } else if (id == R.id.view_frag_create_ll_4) {
            showPopDuration(v);
        }

    }

    private void showPopDuration(View v) {
        PopSpinnerDuration popSpinnerDuration = new PopSpinnerDuration(getActivity(), v.getWidth(), v.getHeight(), 10);
        popSpinnerDuration.setOnSelectListener(new PopSpinnerDuration.OnSelectListener() {
            @Override
            public void onSelect(int res, int dur) {
                tvDuration.setText(getResources().getString(res));
                duration = dur;
            }
        });
        popSpinnerDuration.showAsDropDown(v);
    }

    /**
     * 显示提示框
     */
    private void showAlertDialog() {
        alertDialog = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.notify_dialog, null);
        view.findViewById(R.id.notify_dialog_title).setVisibility(View.GONE);
        ((TextView) view.findViewById(R.id.notify_dialog_message)).setText(getResources().getString(
                R.string.preconf_forbidden));
        alertDialog.setCustomTitle(view);
        alertDialog.setPositiveButton(getResources().getString(R.string.known),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        alertDialog.setNegativeButton(getResources().getString(R.string.preconf_create_qiehuan),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createIntent = new Intent(getActivity(), LoginActivity.class);
                        createIntent.putExtra("switchID", true);
                        createIntent.putExtra("state", 1);
                        createIntent.putExtra("turnIndex", 4);
                        startActivity(createIntent);
                    }
                });
        alertDialog.show();
    }

    /**
     * 发送开启会议的申请获取会议ID
     */
    private String sendConfRequire(boolean create, String inputConfiId) {
        if (true) {
            config = conferenceCommon.initConfig();
            //设置会议开始时间为1小时后
            //final Date curDate = new Date(System.currentTimeMillis() + 60*60*1000);
            conferenceBean.setName(et1.getText().toString());
            if (conferenceBean.getName() == null || conferenceBean.getName().toString().equals("")) {
                conferenceBean.setName(et1.getHint().toString());
            }
            conferenceBean.setConfPassword(et2.getText().toString());
            //config.setStartTime(StringUtil.dateToStrInGMT(curDate, "yyyy-MM-dd hh:mm:ss"));
            conferenceBean.setConfType("0");
            conferenceBean.setConferencePattern(1);
            //		confId = Config.getConfId(ConfCreateActivity.this, conferenceBean);
            int uid = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES,
                    getActivity().MODE_PRIVATE).getInt(Constants.USER_ID, 0);
            String siteId = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES,
                    getActivity().MODE_PRIVATE).getString(Constants.SITE_ID, "");
            String userName = getActivity().getSharedPreferences(Constants.SHARED_PREFERENCES, getActivity().MODE_PRIVATE)
                    .getString(Constants.LOGIN_NAME, "");

            if (duration > 120) {
                //设置会议开始时间为1小时后
                final Date curDate = new Date(System.currentTimeMillis() + 10 * 60 * 1000);
                //			int duration = (int) (68373589 - System.currentTimeMillis()/(1000*60));
                conferenceBean.setStartTime(StringUtil.dateToStrInLocale(curDate, "yyyy-MM-dd HH:mm:ss"));
                conferenceBean.setDuration("" + duration);
                if (attIds != null && !attIds.equals("")) {
                    confId = Config.getFixedConfIdCovert(uid, userName, siteId, conferenceBean, attIds, attNames, attEmails);
                } else {
                    confId = Config.getFixedConfId(uid, userName, siteId, conferenceBean);
                }
            } else {
                if (attIds != null && !attIds.equals("")) {
                    confId = Config.getConfIdCovert(uid, userName, siteId, conferenceBean, attIds, attNames, attEmails);
                } else {
                    confId = Config.getConfId(uid, userName, siteId, conferenceBean);
                }
            }
            if (confId.startsWith("0")) {
                confId = confId.substring(2);
                conferenceBean.setId(confId);
                result = Config.startConf(uid, userName, siteId, conferenceBean);
                if (result.equals("-1:error")) {
                    createHandler.sendEmptyMessage(CREATECONF_ERROR);
                } else {
                    startConf();
                }
            } else {
                hideLoading();
                Log.d("InfowareLab.Debug", "confId = " + Integer.parseInt(confId));
                createHandler.sendEmptyMessage(Integer.parseInt(confId));
            }
            return confId;
        } else {
            confId = inputConfiId;
            startConf();
            return inputConfiId;
        }
    }

    /**
     * 开启会议
     */
    private void startConf() {
//		if (android.os.Environment.getExternalStorageState().equals( 
//				android.os.Environment.MEDIA_MOUNTED)){
//			conferenceCommon.setLogPath(Environment.getExternalStorageDirectory() + File.separator + "infowarelab");
//		}else{
//			conferenceCommon.setLogPath(getActivity().getCacheDir() + File.separator + "infowarelab");
//		}
        conferenceCommon.setLogPath(ConferenceApplication.getConferenceApp().getFilePath("Log"));
        conferenceCommon.initSDK();
        Config config = conferenceCommon.getConfig();
        login = new LoginBean(confId, FileUtil.readSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME),
                et2.getText().toString());
        conferenceCommon.joinConference(conferenceCommon.getParam(login, true));
        config.setMyConferenceBean(conferenceBean);
        ConferenceApplication.getConferenceApp().setJoined(true);
    }

    private void setFocusView(View v) {
        this.focusView = v;
    }

    private View getFocusView() {
        return focusView;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            setFocusView(v);
//			showInput(v);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        // TODO Auto-generated method stub
        if (hidden) {

        } else {
            if (getFocusView() != null) {
                ((EditText) getFocusView()).clearFocus();
            }
            String realName = FileUtil.readSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES, Constants.LOGIN_EXNAME);
            String loginName = FileUtil.readSharedPreferences(getActivity(), Constants.SHARED_PREFERENCES, Constants.LOGIN_NAME);
            et1.setHint(!TextUtils.isEmpty(realName) ? realName + getResources().getString(R.string.preconf_create_topic_default) :
                    loginName + getResources().getString(R.string.preconf_create_topic_default));
            tvDuration.setText("");
            duration = 120;
            ActOrganization.lastUsers = null;
            ActOrganization.lastUserandnames = null;
        }
        super.onHiddenChanged(hidden);
    }

    private void showErrMsg(int resId) {
        if (tvErrMsg != null) {
            if (resId == -1) {
                tvErrMsg.setVisibility(View.GONE);
            } else {
                tvErrMsg.setText(getResources().getString(resId) + "!");
                tvErrMsg.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setAtts(String ids, String names, String emails) {
        this.attIds = ids;
        this.attNames = names;
        this.attEmails = emails;
    }
}
